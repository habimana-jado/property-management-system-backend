package rw.afriteck.pms.restaurant.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;
import rw.afriteck.pms.payment.dtos.PaymentResult;
import rw.afriteck.pms.payment.dtos.ProcessPaymentRequest;
import rw.afriteck.pms.common.enums.EPayableType;
import rw.afriteck.pms.restaurant.dtos.RecordPaymentRequest;
import rw.afriteck.pms.payment.mapper.PaymentMapper;
import rw.afriteck.pms.payment.service.impl.PaymentService;
import rw.afriteck.pms.restaurant.dtos.BillSnapshotResponse;
import rw.afriteck.pms.restaurant.dtos.CombinedBillLineResponse;
import rw.afriteck.pms.restaurant.dtos.CompBillRequest;
import rw.afriteck.pms.restaurant.dtos.TableBillResponse;
import rw.afriteck.pms.restaurant.enums.EBillStatus;
import rw.afriteck.pms.restaurant.enums.ETableBillItemStatus;
import rw.afriteck.pms.restaurant.enums.ETableStatus;
import rw.afriteck.pms.restaurant.mapper.BillSnapshotMapper;
import rw.afriteck.pms.restaurant.mapper.TableBillResponseMapper;
import rw.afriteck.pms.restaurant.model.TableBill;
import rw.afriteck.pms.restaurant.model.TableBillItem;
import rw.afriteck.pms.restaurant.model.TableMaster;
import rw.afriteck.pms.restaurant.repository.TableBillItemRepo;
import rw.afriteck.pms.restaurant.repository.TableBillRepo;
import rw.afriteck.pms.restaurant.repository.TableMasterRepo;
import rw.afriteck.pms.restaurant.service.ITableBillService;

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
    private final PaymentMapper paymentMapper;
    private final PaymentService paymentService;

    @Override
    @Transactional
    public TableBillResponse requestBill(UUID tableId) {

        TableMaster tableMaster = tableMasterRepo.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table Master", tableId));

        Optional<TableBill> activeBill = tableBillRepo.findActiveBillByTableId(tableId, List.of(EBillStatus.OPEN, EBillStatus.BILL_REQUESTED, EBillStatus.PARTIALLY_PAID));

        if (activeBill.isEmpty()) {
            return TableBillResponse.empty(tableMaster.getTableNumber());
        }

        TableBill bill = activeBill.get();
        List<TableBillItem> items = tableBillItemRepo.findByTableBillIdAndStatus(bill.getId(), ETableBillItemStatus.ACTIVE);

        BigDecimal amountPaid = paymentService.sumPaidAmount(bill.getId(), EPayableType.TABLE_BILL); // call into pms-payment
        BigDecimal remaining = bill.getTotalAmount().subtract(amountPaid);

        return tableBillResponseMapper.toTableBillResponse(bill, tableMaster, items, amountPaid, remaining);
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

        TableBill bill = tableBillRepo.findByIdForUpdate(tableBillId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", tableBillId));

        if (bill.getBillStatus() == EBillStatus.PAID || bill.getBillStatus() == EBillStatus.CANCELLED) {
            throw new BusinessRuleViolationException("INVALID_BILL_STATE", "Bill is already settled or cancelled");
        }

        PaymentResult result = paymentService.processPayment(new ProcessPaymentRequest(
                bill.getId(),
                EPayableType.TABLE_BILL,
                bill.getTotalAmount(),
                request.amount(),
                request.method(),
                request.reference()
        ));

        bill.setBillStatus(result.fullySettled() ? EBillStatus.PAID : EBillStatus.PARTIALLY_PAID);

        if (result.fullySettled()) {
            bill.getTableMaster().setTableStatus(ETableStatus.AVAILABLE);
        }

        List<TableBillItem> items = tableBillItemRepo
                .findByTableBillIdAndStatus(bill.getId(), ETableBillItemStatus.ACTIVE);

        return tableBillResponseMapper.toTableBillResponse(bill, bill.getTableMaster(), items, request.amount(), result.remainingBalance());
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

        return tableBillResponseMapper.toTableBillResponse(bill, bill.getTableMaster(), activeItems, BigDecimal.ZERO, bill.getTotalAmount());
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
                    TableBillItem sample = group.getFirst();
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
