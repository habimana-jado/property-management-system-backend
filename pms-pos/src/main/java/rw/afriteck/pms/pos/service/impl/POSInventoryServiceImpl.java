package rw.afriteck.pms.pos.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;
import rw.afriteck.pms.pos.dtos.POSInventoryResponse;
import rw.afriteck.pms.pos.dtos.RestockRequest;
import rw.afriteck.pms.pos.dtos.StockAdjustmentRequest;
import rw.afriteck.pms.pos.dtos.StockMovementResponse;
import rw.afriteck.pms.pos.enums.EStockMovementType;
import rw.afriteck.pms.pos.model.POSInventory;
import rw.afriteck.pms.pos.model.POSProduct;
import rw.afriteck.pms.pos.model.POSStockMovement;
import rw.afriteck.pms.pos.repository.POSInventoryRepository;
import rw.afriteck.pms.pos.repository.POSProductRepository;
import rw.afriteck.pms.pos.repository.POSStockMovementRepository;
import rw.afriteck.pms.pos.service.POSInventoryService;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class POSInventoryServiceImpl implements POSInventoryService {

    private final POSInventoryRepository inventoryRepo;
    private final POSProductRepository productRepo;
    private final POSStockMovementRepository movementRepo;

    @Override
    @Transactional
    public StockMovementResponse restock(RestockRequest request) {
        POSProduct product = getTrackedProduct(request.productId());
        POSInventory inventory = getOrCreateInventory(product, request.hotelBranchId());

        inventory.setQuantityOnHand(inventory.getQuantityOnHand() + request.quantity());
        inventoryRepo.save(inventory);

        POSStockMovement movement = new POSStockMovement();
        movement.setProduct(product);
        movement.setHotelBranchId(request.hotelBranchId());
        movement.setMovementType(EStockMovementType.RESTOCK);
        movement.setQuantity(request.quantity());
        movement.setPurchasePriceAtRestock(request.purchasePrice());
        movementRepo.save(movement);

        return toResponse(movement, inventory.getQuantityOnHand());
    }

    @Override
    @Transactional
    public StockMovementResponse adjust(StockAdjustmentRequest request) {
        POSProduct product = getTrackedProduct(request.productId());
        POSInventory inventory = getOrCreateInventory(product, request.hotelBranchId());

        int newQuantity = inventory.getQuantityOnHand() + request.quantityDelta();
        if (newQuantity < 0) {
            throw new BusinessRuleViolationException("NEGATIVE_STOCK",
                    "Adjustment would result in negative stock for product " + product.getId());
        }

        inventory.setQuantityOnHand(newQuantity);
        inventoryRepo.save(inventory);

        POSStockMovement movement = new POSStockMovement();
        movement.setProduct(product);
        movement.setHotelBranchId(request.hotelBranchId());
        movement.setMovementType(EStockMovementType.ADJUSTMENT);
        movement.setQuantity(request.quantityDelta());
        movement.setPurchasePriceAtRestock(BigDecimal.ZERO);
        movement.setAdjustmentReason(request.reason());
        movementRepo.save(movement);

        return toResponse(movement, inventory.getQuantityOnHand());
    }

    @Override
    @Transactional(readOnly = true)
    public POSInventoryResponse getStockLevel(UUID productId, UUID hotelBranchId) {
        POSInventory inventory = inventoryRepo.findByProductIdAndHotelBranchId(productId, hotelBranchId)
                .orElseThrow(() -> new ResourceNotFoundException("POSInventory", productId));
        return new POSInventoryResponse(productId, hotelBranchId, inventory.getQuantityOnHand());
    }

    private POSProduct getTrackedProduct(UUID productId) {
        POSProduct product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("POSProduct", productId));
        if (!product.isTrackInventory()) {
            throw new BusinessRuleViolationException("INVENTORY_NOT_TRACKED",
                    "Product " + productId + " does not track inventory");
        }
        return product;
    }

    private POSInventory getOrCreateInventory(POSProduct product, UUID hotelBranchId) {
        return inventoryRepo.findForUpdate(product.getId(), hotelBranchId)
                .orElseGet(() -> {
                    POSInventory inventory = new POSInventory();
                    inventory.setProduct(product);
                    inventory.setHotelBranchId(hotelBranchId);
                    inventory.setQuantityOnHand(0);
                    return inventoryRepo.save(inventory);
                });
    }

    private StockMovementResponse toResponse(POSStockMovement movement, int newQuantityOnHand) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getProduct().getId(),
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getPurchasePriceAtRestock(),
                newQuantityOnHand,
                movement.getCreatedAt()
        );
    }
}