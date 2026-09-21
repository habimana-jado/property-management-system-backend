package rw.afriteck.pms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.dtos.BillSnapshotResponse;
import rw.afriteck.pms.enums.EBillStatus;
import rw.afriteck.pms.enums.ETableStatus;
import rw.afriteck.pms.exception.BusinessRuleViolationException;
import rw.afriteck.pms.exception.ResourceNotFoundException;
import rw.afriteck.pms.mapper.BillSnapshotMapper;
import rw.afriteck.pms.model.TableBill;
import rw.afriteck.pms.model.TableBillItem;
import rw.afriteck.pms.model.TableMaster;
import rw.afriteck.pms.repository.TableBillItemRepo;
import rw.afriteck.pms.repository.TableBillRepo;
import rw.afriteck.pms.repository.TableMasterRepo;
import rw.afriteck.pms.service.ITableBillService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TableBillService implements ITableBillService {
    private final TableMasterRepo tableMasterRepo;
    private final TableBillItemRepo tableBillItemRepo;
    private final TableBillRepo tableBillRepo;
    private final BillSnapshotMapper billSnapshotMapper;

    @Override
    @Transactional
    public BillSnapshotResponse requestBill(UUID tableId) {
        TableBill tableBill = tableBillRepo.findActiveBillByTableId(
                tableId,
                List.of(EBillStatus.OPEN, EBillStatus.BILL_REQUESTED)
        ).orElseThrow(() -> new ResourceNotFoundException("No active bill for this table", tableId));

        TableMaster tableMaster = tableMasterRepo.findById(tableId)
                .orElseThrow(()->new ResourceNotFoundException("Table Master", tableId));

        List<TableBillItem> tableBillItems = tableBillItemRepo.findByTableBillId(tableBill.getId());
        if(tableBillItems.isEmpty()){
            throw new BusinessRuleViolationException("EMPTY_BILL_REQUEST","Bill Requested cannot be empty Bill");
        }

        tableBill.setBillStatus(EBillStatus.BILL_REQUESTED);
        tableBillRepo.save(tableBill);

        tableMaster.setTableStatus(ETableStatus.BILLED);
        tableMasterRepo.save(tableMaster);

        return billSnapshotMapper.toBillSnapshotResponse(tableBill, tableMaster, tableBillItems);
    }
}
