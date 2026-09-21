package rw.afriteck.pms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.dtos.PlaceOrderRequest;
import rw.afriteck.pms.dtos.TableBillItemResponse;
import rw.afriteck.pms.enums.EBillStatus;
import rw.afriteck.pms.enums.ERecordStatus;
import rw.afriteck.pms.enums.ETableStatus;
import rw.afriteck.pms.exception.InvalidStateException;
import rw.afriteck.pms.exception.ResourceNotFoundException;
import rw.afriteck.pms.mapper.TableBillItemMapper;
import rw.afriteck.pms.model.MenuMaster;
import rw.afriteck.pms.model.TableBill;
import rw.afriteck.pms.model.TableBillItem;
import rw.afriteck.pms.model.TableMaster;
import rw.afriteck.pms.repository.MenuMasterRepo;
import rw.afriteck.pms.repository.TableBillItemRepo;
import rw.afriteck.pms.repository.TableBillRepo;
import rw.afriteck.pms.repository.TableMasterRepo;
import rw.afriteck.pms.service.IOrderService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final TableMasterRepo tableRepo;
    private final TableBillRepo tableBillRepo;
    private final TableBillItemRepo tableBillItemRepo;
    private final MenuMasterRepo menuMasterRepo;
    private final TableBillItemMapper tableBillItemMapper;
    private final BillNumberGeneratorService billNumberGeneratorService;
    private static final List<EBillStatus> ACTIVE_BILL_STATUSES =
            List.of(EBillStatus.OPEN, EBillStatus.BILL_REQUESTED);

    @Override
    @Transactional
    public TableBillItemResponse placeOrder(UUID tableId, PlaceOrderRequest tableBillItemRequest) {

        TableMaster table = tableRepo.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table", tableId));

        validateTableCanAcceptOrder(table);

        MenuMaster menuItem = menuMasterRepo.findById(tableBillItemRequest.menuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu Item", tableBillItemRequest.menuItemId()));

        // Check if 1st Table Order then branch to openNewBill else retrieve existing tableBill Object
        TableBill bill = retrieveOrCreateActiveBill(table);


        TableBillItem item = new TableBillItem();
        item.setTableBill(bill);
        item.setMenuMaster(menuItem);
        item.setTransactionQuantity(tableBillItemRequest.transactionQuantity());
        item.setRemarks(tableBillItemRequest.remarks());
        item.setUnitPriceAtOrderTime(menuItem.getUnitPrice());
        item.setLineTotal(menuItem.getUnitPrice().multiply(BigDecimal.valueOf(tableBillItemRequest.transactionQuantity())));

        TableBillItem savedItem = tableBillItemRepo.save(item);

        recalculateBillTotals(bill);

        return tableBillItemMapper.toResponse(savedItem);
    }

    // OCCUPIED or AVAILABLE are both fine to accept orders — one opens a new bill,
    // the other adds to the existing one
    private void validateTableCanAcceptOrder(TableMaster table) {
        //One should not place orders on INACTIVE tables
        if (table.getRecordStatus() == ERecordStatus.INACTIVE) {
            throw new InvalidStateException("Table %s is inactive".formatted(table.getId()));
        }
    }

    private TableBill retrieveOrCreateActiveBill(TableMaster table){

        TableBill bill = tableBillRepo.findActiveBillByTableId(table.getId(), ACTIVE_BILL_STATUSES)
                .orElseGet(() -> openNewBill(table));

        if (bill.getBillStatus() == EBillStatus.BILL_REQUESTED) {
            bill.setBillStatus(EBillStatus.OPEN); // snapshot invalidated — new item is coming
        }

        if(table.getTableStatus().equals(ETableStatus.BILLED)){
            table.setTableStatus(ETableStatus.OCCUPIED);
            tableRepo.save(table);
        }

        // Check if Table had Requested Bill and has decided to continue ordering on the same Bill
        if (bill.getBillStatus() == EBillStatus.BILL_REQUESTED) {
            bill.setBillStatus(EBillStatus.OPEN);

            return tableBillRepo.save(bill);
        }

        if(table.getTableStatus().equals(ETableStatus.BILLED)){
            table.setTableStatus(ETableStatus.OCCUPIED);
            tableRepo.save(table);
        }

        return bill;
    }

    private TableBill openNewBill(TableMaster table) {
        TableBill bill = new TableBill();
        bill.setTableMaster(table);
        bill.setBillNo(billNumberGeneratorService.generateBillNo(table.getRestaurant().getId(), table.getRestaurant().getRestaurantCode()));
        bill.setBillStatus(EBillStatus.OPEN);
        bill.setSubtotal(BigDecimal.ZERO);
        //TODO: Replace with real Tax values and Discount implementation
        bill.setTaxAmount(BigDecimal.ZERO);
        bill.setDiscountAmount(BigDecimal.ZERO);
        bill.setTotalAmount(BigDecimal.ZERO);
        TableBill savedBill = tableBillRepo.save(bill);

        // Flip the table to OCCUPIED — only happens on genuinely NEW bills
        table.setTableStatus(ETableStatus.OCCUPIED);
        tableRepo.save(table);

        return savedBill;
    }

    private void recalculateBillTotals(TableBill bill) {
        BigDecimal subtotal = tableBillItemRepo.sumLineTotalsByBillId(bill.getId());
        bill.setSubtotal(subtotal);

        //TODO: Update with Tax percentages and discount implementation
        bill.setDiscountAmount(BigDecimal.ZERO);
        bill.setTaxAmount(BigDecimal.ZERO);

        bill.setTotalAmount(subtotal);
        tableBillRepo.save(bill);
    }
}
