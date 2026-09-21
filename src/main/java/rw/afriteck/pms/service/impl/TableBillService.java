package rw.afriteck.pms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.dtos.BillSnapshotResponse;
import rw.afriteck.pms.dtos.TableBillResponse;
import rw.afriteck.pms.dtos.CombinedBillLineResponse;
import rw.afriteck.pms.enums.EBillStatus;
import rw.afriteck.pms.enums.ETableStatus;
import rw.afriteck.pms.exception.BusinessRuleViolationException;
import rw.afriteck.pms.exception.ResourceNotFoundException;
import rw.afriteck.pms.mapper.TableBillResponseMapper;
import rw.afriteck.pms.mapper.BillSnapshotMapper;
import rw.afriteck.pms.model.TableBill;
import rw.afriteck.pms.model.TableBillItem;
import rw.afriteck.pms.model.TableMaster;
import rw.afriteck.pms.repository.TableBillItemRepo;
import rw.afriteck.pms.repository.TableBillRepo;
import rw.afriteck.pms.repository.TableMasterRepo;
import rw.afriteck.pms.service.ITableBillService;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableBillService implements ITableBillService {
    private final TableMasterRepo tableMasterRepo;
    private final TableBillItemRepo tableBillItemRepo;
    private final TableBillRepo tableBillRepo;
    private final TableBillResponseMapper tableBillResponseMapper;
    private final BillSnapshotMapper billSnapshotMapper;

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
        List<TableBillItem> items = tableBillItemRepo.findByTableBillId(bill.getId());

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

        List<TableBillItem> tableBillItems = tableBillItemRepo.findByTableBillId(tableBill.getId());
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
