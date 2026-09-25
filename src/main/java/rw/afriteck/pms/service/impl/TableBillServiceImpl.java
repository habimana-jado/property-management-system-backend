package rw.afriteck.pms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.dtos.*;
import rw.afriteck.pms.enums.EBillStatus;
import rw.afriteck.pms.enums.EPaymentStatus;
import rw.afriteck.pms.enums.ETableBillItemStatus;
import rw.afriteck.pms.enums.ETableStatus;
import rw.afriteck.pms.exception.BusinessRuleViolationException;
import rw.afriteck.pms.exception.ResourceNotFoundException;
import rw.afriteck.pms.mapper.TableBillResponseMapper;
import rw.afriteck.pms.mapper.BillSnapshotMapper;
import rw.afriteck.pms.model.Payment;
import rw.afriteck.pms.model.TableBill;
import rw.afriteck.pms.model.TableBillItem;
import rw.afriteck.pms.model.TableMaster;
import rw.afriteck.pms.repository.PaymentRepo;
import rw.afriteck.pms.repository.TableBillItemRepo;
import rw.afriteck.pms.repository.TableBillRepo;
import rw.afriteck.pms.repository.TableMasterRepo;
import rw.afriteck.pms.service.ITableBillService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableBillServiceImpl implements ITableBillService {
    private final TableMasterRepo tableMasterRepo;
    private final TableBillItemRepo tableBillItemRepo;
    private final TableBillRepo tableBillRepo;
    private final TableBillResponseMapper tableBillResponseMapper;
    private final BillSnapshotMapper billSnapshotMapper;
    private final PaymentRepo paymentRepo;

    @Override
    @Transactional
    public TableBillResponse requestBill(UUID tableId) {

        TableMaster tableMaster = tableMasterRepo.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table Master", tableId));

        Optional<TableBill> activeBill = tableBillRepo.findActiveBillByTableId(tableId, List.of(EBillStatus.OPEN, EBillStatus.BILL_REQUESTED));

        if (activeBill.isEmpty()) {
            return TableBillResponse.empty(tableMaster.getTableNumber());
        }

        TableBill bill = activeBill.get();
        List<TableBillItem> items = tableBillItemRepo.findByTableBillIdAndStatus(bill.getId(), ETableBillItemStatus.ACTIVE);

        return tableBillResponseMapper.toTableBillResponse(bill, tableMaster, items);
    }

    @Override
    @Transactional
    public BillSnapshotResponse requestBillItemsCombined(UUID tableId) {
        TableBill tableBill = tableBillRepo.findActiveBillByTableId(
                tableId,
                List.of(EBillStatus.OPEN, EBillStatus.BILL_REQUESTED)
        ).orElseThrow(() -> new ResourceNotFoundException("No active bill for this table", tableId));

        TableMaster tableMaster = tableMasterRepo.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table Master", tableId));

        List<TableBillItem> tableBillItems = tableBillItemRepo.findByTableBillIdAndStatus(tableBill.getId(), ETableBillItemStatus.ACTIVE);
        if (tableBillItems.isEmpty()) {
            throw new BusinessRuleViolationException("EMPTY_BILL_REQUEST", "Bill Requested cannot be empty Bill");
        }

        tableBill.setBillStatus(EBillStatus.BILL_REQUESTED);
        tableBillRepo.save(tableBill);

        tableMaster.setTableStatus(ETableStatus.BILLED);
        tableMasterRepo.save(tableMaster);

        List<CombinedBillLineResponse> itemsCombined = combineForBillSnapshot(tableBillItems);

        return billSnapshotMapper.toBillSnapshotResponse(tableBill, tableMaster, itemsCombined);
    }

    @Override
    @Transactional
    public TableBillResponse recordPayment(UUID tableBillId, RecordPaymentRequest request) {

        TableBill bill = tableBillRepo.findById(tableBillId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found", tableBillId));

        if (bill.getBillStatus() == EBillStatus.PAID || bill.getBillStatus() == EBillStatus.CANCELLED) {
            throw new BusinessRuleViolationException("INVALID_BILL_STATE", "Bill is already settled or cancelled");
        }

        BigDecimal alreadyPaid = paymentRepo.sumActiveAmountByBillId(bill.getId());
        BigDecimal remainingBalance = bill.getTotalAmount().subtract(alreadyPaid);

        if (request.amount().compareTo(remainingBalance) > 0) {
            throw new BusinessRuleViolationException("OVERPAYMENT", "Payment exceeds remaining balance");
        }

        Payment payment = new Payment();
        payment.setTableBill(bill);
        payment.setAmount(request.amount());
        payment.setMethod(request.method());
        payment.setStatus(EPaymentStatus.COMPLETED);
        payment.setPaidAt(Instant.now());
        payment.setReference(request.reference());
        paymentRepo.save(payment);

        BigDecimal newTotalPaid = alreadyPaid.add(request.amount());
        EBillStatus newStatus = newTotalPaid.compareTo(bill.getTotalAmount()) == 0
                ? EBillStatus.PAID
                : EBillStatus.PARTIALLY_PAID;

        bill.setBillStatus(newStatus);

        if (newStatus == EBillStatus.PAID) {
            bill.getTableMaster().setTableStatus(ETableStatus.AVAILABLE); // table frees up on settlement
        }

        List<TableBillItem> items = tableBillItemRepo
                .findByTableBillIdAndStatus(bill.getId(), ETableBillItemStatus.ACTIVE);

        return tableBillResponseMapper.toTableBillResponse(bill, bill.getTableMaster(), items);
    }

    @Override
    @Transactional
    public TableBillResponse compBill(UUID tableBillId, CompBillRequest request) {

        TableBill bill = tableBillRepo.findById(tableBillId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found", tableBillId));

        if (bill.getBillStatus() != EBillStatus.OPEN && bill.getBillStatus() != EBillStatus.BILL_REQUESTED) {
            throw new BusinessRuleViolationException(
                    "INVALID_BILL_STATE", "Only an active, unpaid bill can be comped");
        }

        List<TableBillItem> activeItems = tableBillItemRepo
                .findByTableBillIdAndStatus(bill.getId(), ETableBillItemStatus.ACTIVE);

        if (activeItems.isEmpty()) {
            throw new BusinessRuleViolationException("EMPTY_BILL", "Cannot comp a bill with no items");
        }

        bill.setBillStatus(EBillStatus.COMPED);
        bill.setCompReason(request.reason());
        bill.setCompAuthorizedBy(request.authorizedBy());
        bill.setCompedAt(Instant.now());
        // managed entity — dirty checking persists this on commit

        bill.getTableMaster().setTableStatus(ETableStatus.AVAILABLE);

        return tableBillResponseMapper.toTableBillResponse(bill, bill.getTableMaster(), activeItems);
    }

    public List<CombinedBillLineResponse> combineForBillSnapshot(List<TableBillItem> items) {
        record GroupKey(UUID menuItemId, BigDecimal unitPrice) {
        }

        Map<GroupKey, List<TableBillItem>> grouped = items.stream()
                .collect(Collectors.groupingBy(
                        i -> new GroupKey(i.getMenuMaster().getId(), i.getUnitPriceAtOrderTime()),
                        LinkedHashMap::new,   // preserves first-seen order — reads naturally on a printed bill
                        Collectors.toList()
                ));

        return grouped.values().stream()
                .map(group -> {
                    TableBillItem sample = group.get(0);
                    int totalQty = group.stream().mapToInt(TableBillItem::getTransactionQuantity).sum();
                    BigDecimal totalLine = group.stream()
                            .map(TableBillItem::getLineTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new CombinedBillLineResponse(
                            sample.getMenuMaster().getMenuItemName(),
                            totalQty,
                            sample.getUnitPriceAtOrderTime(),
                            totalLine
                    );
                })
                .toList();
    }
}
