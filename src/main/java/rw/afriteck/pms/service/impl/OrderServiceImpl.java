package rw.afriteck.pms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.dtos.*;
import rw.afriteck.pms.enums.EBillStatus;
import rw.afriteck.pms.enums.ERecordStatus;
import rw.afriteck.pms.enums.ETableBillItemStatus;
import rw.afriteck.pms.enums.ETableStatus;
import rw.afriteck.pms.exception.BusinessRuleViolationException;
import rw.afriteck.pms.exception.InvalidStateException;
import rw.afriteck.pms.exception.ResourceNotFoundException;
import rw.afriteck.pms.mapper.OrderMapper;
import rw.afriteck.pms.mapper.TableBillResponseMapper;
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
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final TableMasterRepo tableMasterRepo;
    private final TableBillRepo tableBillRepo;
    private final TableBillItemRepo tableBillItemRepo;
    private final MenuMasterRepo menuMasterRepo;
    private final TableBillItemMapper tableBillItemMapper;
    private final BillNumberGeneratorServiceImpl billNumberGeneratorServiceImpl;
    private static final List<EBillStatus> ACTIVE_BILL_STATUSES =
            List.of(EBillStatus.OPEN, EBillStatus.BILL_REQUESTED);
    private final TableBillResponseMapper tableBillResponseMapper;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderPlacementResponse placeOrder(UUID tableId, PlaceOrderRequest orderRequest) {

        TableMaster table = tableMasterRepo.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table", tableId));

        validateTableCanAcceptOrder(table);

        List<UUID> menuItemIds = orderRequest.orderLineRequests().stream()
                .map(OrderLineRequest::menuItemId)
                .distinct()
                .toList();
        Map<UUID, MenuMaster> menuItems = menuMasterRepo.findAllById(menuItemIds).stream()
                .collect(Collectors.toMap(MenuMaster::getId, Function.identity()));

        if (menuItems.size() != menuItemIds.size()) {
            throw new BusinessRuleViolationException("INVALID ITEM_ID","One or more menu items not found");
        }

        // Check if 1st Table Order then branch to openNewBill else retrieve existing tableBill Object
        TableBill bill = retrieveOrCreateActiveBill(table);

        List<TableBillItem> newItems = orderRequest.orderLineRequests().stream()
                .map(line -> {
                    MenuMaster menuItem = menuItems.get(line.menuItemId());
                    TableBillItem item = new TableBillItem();
                    item.setTableBill(bill);
                    item.setMenuMaster(menuItem);
                    item.setTransactionQuantity(line.transactionQuantity());
                    item.setUnitPriceAtOrderTime(menuItem.getUnitPrice());
                    item.setLineTotal(menuItem.getUnitPrice().multiply(BigDecimal.valueOf(line.transactionQuantity())));
                    return item;
                })
                .toList();
        tableBillItemRepo.saveAll(newItems);

        List<TableBillItem> allItems = tableBillItemRepo.findByTableBillIdAndStatus(bill.getId(), ETableBillItemStatus.ACTIVE);
        recalculateTotalsFromItems(bill, allItems);

        return orderMapper.toPlacementResponse(bill, table, newItems);
    }

    @Override
    @Transactional
    public TableBillResponse voidItem(UUID itemId, VoidItemRequest request) {

        TableBillItem item = tableBillItemRepo.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found",itemId));

        TableBill bill = item.getTableBill();

        if (bill.getBillStatus() != EBillStatus.OPEN && bill.getBillStatus() != EBillStatus.BILL_REQUESTED) {
            throw new BusinessRuleViolationException(
                    "INVALID_BILL_STATE", "Cannot void an item on a bill that is not active");
        }
        if (item.getStatus() == ETableBillItemStatus.VOIDED) {
            throw new BusinessRuleViolationException("ALREADY_VOIDED", "Item is already voided");
        }

        item.setStatus(ETableBillItemStatus.VOIDED);
        item.setVoidReason(request.reason());
        item.setVoidedAt(Instant.now());
        // managed entity — dirty checking persists this on commit

        List<TableBillItem> activeItems = tableBillItemRepo
                .findByTableBillIdAndStatus(bill.getId(), ETableBillItemStatus.ACTIVE);

        recalculateTotalsFromItems(bill, activeItems);

        if (bill.getBillStatus() == EBillStatus.BILL_REQUESTED) {
            bill.setBillStatus(EBillStatus.OPEN);
        }

        return tableBillResponseMapper.toTableBillResponse(bill, bill.getTableMaster(), activeItems);
    }

    @Override
    @Transactional
    public TableSplitResponse splitTable(UUID sourceTableId, SplitTableRequest request) {

        TableMaster sourceTable = tableMasterRepo.findById(sourceTableId)
                .orElseThrow(() -> new ResourceNotFoundException("Source table not found", sourceTableId));

        TableMaster targetTable = tableMasterRepo.findById(request.targetTableId())
                .orElseThrow(() -> new ResourceNotFoundException("Target table not found", request.targetTableId()));

        // Rule: same restaurant only
        if (!sourceTable.getRestaurant().getId().equals(targetTable.getRestaurant().getId())) {
            throw new BusinessRuleViolationException("SPLIT BILL to Multiple Restaurant Tables", "Cannot split across different restaurants");
        }

        // Rule: target must be free
        if (targetTable.getTableStatus() != ETableStatus.AVAILABLE) {
            throw new InvalidStateException("Target table is not available");
        }

        // Fetch active source bill — OPEN or BILL_REQUESTED only
        TableBill sourceBill = tableBillRepo
                .findActiveBillByTableId(sourceTableId, List.of(EBillStatus.OPEN, EBillStatus.BILL_REQUESTED))
                .orElseThrow(() -> new ResourceNotFoundException("No active bill for source table", sourceTableId));

        // single fetch — every active item currently on the source bill
        List<TableBillItem> allItems = tableBillItemRepo.findByTableBillIdAndStatus(sourceBill.getId(), ETableBillItemStatus.ACTIVE);

        Set<UUID> idsToMove = new HashSet<>(request.itemIdsToMove());
        Map<Boolean, List<TableBillItem>> partitioned = allItems.stream()
                .collect(Collectors.partitioningBy(item -> idsToMove.contains(item.getId())));

        List<TableBillItem> itemsToMove = partitioned.get(true);
        List<TableBillItem> itemsRemaining = partitioned.get(false);

        if (itemsToMove.size() != idsToMove.size()) {
            throw new BusinessRuleViolationException("INVALID ITEM MOVE","One or more items do not belong to this table's bill");
        }
        if (itemsToMove.isEmpty() || itemsRemaining.isEmpty()) {
            throw new BusinessRuleViolationException("INVALID ITEM MOVE","Split must move at least one item and leave at least one behind");
        }

        // Create the new bill for the target table
        TableBill newBill = openNewBill(targetTable);
        newBill.setSplitFromBillId(sourceBill.getId());

        // Reparent the selected items
        for (TableBillItem item : itemsToMove) {
            item.setTableBill(newBill); // managed entity — dirty checking handles the update
        }

        // Recalculate totals on both bills
        recalculateTotalsFromItems(sourceBill, itemsRemaining);
        recalculateTotalsFromItems(newBill, itemsToMove);

        // Revert stale snapshot on source bill if it had been requested
        if (sourceBill.getBillStatus() == EBillStatus.BILL_REQUESTED) {
            sourceBill.setBillStatus(EBillStatus.OPEN);
        }

        return new TableSplitResponse(
                tableBillResponseMapper.toTableBillResponse(sourceBill, sourceTable, itemsRemaining),
                tableBillResponseMapper.toTableBillResponse(newBill, targetTable, itemsToMove)
        );
    }

    @Override
    @Transactional
    public TableBillResponse mergeTable(UUID destinationTableId, MergeTableRequest request) {

        TableMaster destinationTable = tableMasterRepo.findById(destinationTableId)
                .orElseThrow(() -> new ResourceNotFoundException("Target Table",destinationTableId));

        TableMaster sourceTable = tableMasterRepo.findById(request.sourceTableId())
                .orElseThrow(() -> new ResourceNotFoundException("Source Table",request.sourceTableId()));

        if (destinationTable.getId().equals(sourceTable.getId())) {
            throw new BusinessRuleViolationException("TABLE CONFLICT","Cannot merge a table into itself");
        }
        if (!destinationTable.getRestaurant().getId().equals(sourceTable.getRestaurant().getId())) {
            throw new BusinessRuleViolationException("TABLE CONFLICT","Cannot merge across different restaurants");
        }

        TableBill destinationBill = tableBillRepo
                .findActiveBillByTableId(destinationTableId, List.of(EBillStatus.OPEN, EBillStatus.BILL_REQUESTED))
                .orElseThrow(() -> new ResourceNotFoundException("Bill",destinationTableId));

        TableBill sourceBill = tableBillRepo
                .findActiveBillByTableId(sourceTable.getId(), List.of(EBillStatus.OPEN, EBillStatus.BILL_REQUESTED))
                .orElseThrow(() -> new ResourceNotFoundException("Bill", sourceTable.getId()));

        List<TableBillItem> sourceItems = tableBillItemRepo.findByTableBillIdAndStatus(sourceBill.getId(), ETableBillItemStatus.ACTIVE);
        if (sourceItems.isEmpty()) {
            throw new BusinessRuleViolationException("Source Items", "Source table has no items to merge");
        }

        List<TableBillItem> destinationExistingItems = tableBillItemRepo.findByTableBillIdAndStatus(destinationBill.getId(), ETableBillItemStatus.ACTIVE);

        // reparent all source items onto the destination bill
        for (TableBillItem item : sourceItems) {
            item.setTableBill(destinationBill);
        }

        // recompute destination totals against its full item set (existing + merged)
        List<TableBillItem> destinationItems = new ArrayList<>(destinationExistingItems);

        destinationItems.addAll(sourceItems); // in-memory union, no extra query
        recalculateTotalsFromItems(destinationBill, destinationItems);

        // revert stale snapshot if destination had already been requested
        if (destinationBill.getBillStatus() == EBillStatus.BILL_REQUESTED) {
            destinationBill.setBillStatus(EBillStatus.OPEN);
        }

        // close out the source bill
        sourceBill.setBillStatus(EBillStatus.CANCELLED);
        sourceBill.setMergedIntoBillId(destinationBill.getId());

        // free up the source table
        sourceTable.setTableStatus(ETableStatus.AVAILABLE);

        return tableBillResponseMapper.toTableBillResponse(destinationBill, destinationTable, destinationItems);
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
            tableMasterRepo.save(table);
        }

        // Check if Table had Requested Bill and has decided to continue ordering on the same Bill
        if (bill.getBillStatus() == EBillStatus.BILL_REQUESTED) {
            bill.setBillStatus(EBillStatus.OPEN);

            return tableBillRepo.save(bill);
        }

        if(table.getTableStatus().equals(ETableStatus.BILLED)){
            table.setTableStatus(ETableStatus.OCCUPIED);
            tableMasterRepo.save(table);
        }

        return bill;
    }

    private TableBill openNewBill(TableMaster table) {
        TableBill bill = new TableBill();
        bill.setTableMaster(table);
        bill.setBillNo(billNumberGeneratorServiceImpl.generateBillNo(table.getRestaurant().getId(), table.getRestaurant().getRestaurantCode()));
        bill.setBillStatus(EBillStatus.OPEN);
        bill.setSubtotal(BigDecimal.ZERO);
        //TODO: Replace with real Tax values and Discount implementation
        bill.setTaxAmount(BigDecimal.ZERO);
        bill.setDiscountAmount(BigDecimal.ZERO);
        bill.setTotalAmount(BigDecimal.ZERO);
        TableBill savedBill = tableBillRepo.save(bill);

        // Flip the table to OCCUPIED — only happens on genuinely NEW bills
        table.setTableStatus(ETableStatus.OCCUPIED);
        tableMasterRepo.save(table);

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

    @Transactional
    public void recalculateTotalsFromItems(TableBill bill, List<TableBillItem> items) {

        BigDecimal subtotal = items.stream()
                .map(TableBillItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //TODO: Implement Discount Logic & appropriate Tax
        //BigDecimal discountAmount = calculateDiscount(bill, subtotal);
        //BigDecimal taxableAmount = subtotal.subtract(discountAmount);
        //BigDecimal taxAmount = calculateTax(bill, taxableAmount);
        //BigDecimal totalAmount = taxableAmount.add(taxAmount);

        bill.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
//        bill.setDiscountAmount(discountAmount.setScale(2, RoundingMode.HALF_UP));
//        bill.setTaxAmount(taxAmount.setScale(2, RoundingMode.HALF_UP));
//        bill.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
        // managed entity — dirty checking persists this on commit
    }
    private BigDecimal calculateDiscount(TableBill bill, BigDecimal subtotal) {
        BigDecimal existingDiscount = bill.getDiscountAmount() != null ? bill.getDiscountAmount() : BigDecimal.ZERO;
        if (existingDiscount.compareTo(subtotal) > 0) {
            throw new BusinessRuleViolationException("INVALID DISCOUNT","Discount cannot exceed subtotal");
        }
        return existingDiscount;
    }

    private BigDecimal calculateTax(TableBill bill, BigDecimal taxableAmount) {
        BigDecimal taxRate = bill.getTableMaster().getRestaurant().getTaxRate(); // e.g., 0.18 for 18%
        return taxableAmount.multiply(taxRate);
    }
}
