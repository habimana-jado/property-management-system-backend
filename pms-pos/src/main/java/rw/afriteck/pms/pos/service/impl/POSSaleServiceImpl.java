package rw.afriteck.pms.pos.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.enums.EPayableType;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;
import rw.afriteck.pms.common.exception.ForbiddenException;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;
import rw.afriteck.pms.common.security.CurrentUserService;
import rw.afriteck.pms.credit.dtos.GrantCreditRequest;
import rw.afriteck.pms.credit.dtos.ReceivableResponse;
import rw.afriteck.pms.credit.service.CreditService;
import rw.afriteck.pms.payment.dtos.ProcessPaymentRequest;
import rw.afriteck.pms.payment.service.impl.PaymentService;
import rw.afriteck.pms.pos.dtos.*;
import rw.afriteck.pms.pos.enums.ERegisterSessionStatus;
import rw.afriteck.pms.pos.enums.ESaleStatus;
import rw.afriteck.pms.pos.mapper.POSSaleMapper;
import rw.afriteck.pms.pos.model.*;
import rw.afriteck.pms.pos.repository.*;
import rw.afriteck.pms.pos.service.POSSaleService;
import rw.afriteck.pms.pos.service.SaleNumberGeneratorService;
import rw.afriteck.pms.stock.api.StockApi;
import rw.afriteck.pms.stock.api.command.ConsumeForSaleCommand;
import rw.afriteck.pms.stock.api.command.SaleLine;
import rw.afriteck.pms.stock.api.enums.EMovementSourceType;
import rw.afriteck.pms.stock.api.enums.EOutletType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class POSSaleServiceImpl implements POSSaleService {

    private final POSSaleRepository saleRepo;
    private final POSSaleItemRepository saleItemRepo;
    private final POSProductRepository productRepo;
    private final POSRegisterSessionRepository sessionRepo;
    private final SaleNumberGeneratorService saleNumberGenerator;
    private final PaymentService paymentService;
    private final POSSaleMapper mapper;
    private final CurrentUserService currentUserService;
    private final StockApi stockApi;
    private final CreditService creditService;
    private final PosBranchAccess branchAccess;

    @Override
    @Transactional
    public POSSaleResponse create(CreateSaleRequest request) {
        UUID branchId = currentUserService.requireCurrentHotelBranchId();
        if (branchId == null) {
            throw new ForbiddenException("BRANCH_REQUIRED", "Selling requires an account assigned to a branch");
        }
        UUID staffId = currentUserService.requireCurrentStaffId();

        POSRegisterSession session = sessionRepo.findByTerminalIdAndStatus(request.terminalId(), ERegisterSessionStatus.OPEN)
                .orElseThrow(() -> new BusinessRuleViolationException("NO_ACTIVE_SESSION",
                        "No open register session on this terminal — start a shift before selling"));

        POSSale sale = new POSSale();
        sale.setHotelBranchId(branchId);
        sale.setRegisterSession(session);
        sale.setCashierId(staffId);
        sale.setCustomerId(request.customerId());
        sale.setSaleNumber(saleNumberGenerator.nextSaleNumber(branchId));
        sale.setStatus(ESaleStatus.DRAFT);

        saleRepo.save(sale);
        return mapper.toResponse(sale, List.of()); // no items yet on a freshly created sale
    }

    @Override
    @Transactional
    public POSSaleResponse addItem(UUID saleId, AddSaleItemRequest request) {
        POSSale sale = getDraftOrThrow(saleId);
        POSProduct product = productRepo.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("POSProduct", request.productId()));

        POSSaleItem item = saleItemRepo.findBySaleIdAndProductId(saleId, product.getId())
                .orElseGet(() -> {
                    POSSaleItem created = new POSSaleItem();
                    created.setSale(sale);
                    created.setProduct(product);
                    created.setQuantity(0);
                    created.setUnitPriceAtSaleTime(product.getUnitPrice());
                    created.setDiscountAmount(BigDecimal.ZERO);
                    return created;
                });

        item.setQuantity(item.getQuantity() + request.quantity());
        recalculateLineTotal(item);
        saleItemRepo.save(item);

        List<POSSaleItem> items = getItems(saleId);
        recalculateSaleTotals(sale, items);
        return mapper.toResponse(sale, items);
    }

    @Override
    @Transactional
    public POSSaleResponse updateItemQuantity(UUID saleId, UUID itemId, UpdateSaleItemQuantityRequest request) {
        POSSale sale = getDraftOrThrow(saleId);
        POSSaleItem item = saleItemRepo.findById(itemId)
                .filter(i -> i.getSale().getId().equals(saleId))
                .orElseThrow(() -> new ResourceNotFoundException("POSSaleItem", itemId));

        item.setQuantity(request.quantity());
        recalculateLineTotal(item);

        List<POSSaleItem> items = getItems(saleId);
        recalculateSaleTotals(sale, items);
        return mapper.toResponse(sale, items);
    }

    @Override
    @Transactional
    public POSSaleResponse removeItem(UUID saleId, UUID itemId) {
        POSSale sale = getDraftOrThrow(saleId);
        POSSaleItem item = saleItemRepo.findById(itemId)
                .filter(i -> i.getSale().getId().equals(saleId))
                .orElseThrow(() -> new ResourceNotFoundException("POSSaleItem", itemId));

        saleItemRepo.delete(item);

        List<POSSaleItem> items = getItems(saleId);

        recalculateSaleTotals(sale, items);
        return mapper.toResponse(sale, items);
    }

    @Override
    @Transactional
    public POSSaleResponse hold(UUID saleId) {
        POSSale sale = getDraftOrThrow(saleId);
        sale.setStatus(ESaleStatus.HELD);
        return mapper.toResponse(sale, getItems(saleId));
    }

    @Override
    @Transactional
    public POSSaleResponse resume(UUID saleId) {
        POSSale sale = getOrThrow(saleId);
        if (sale.getStatus() != ESaleStatus.HELD) {
            throw new BusinessRuleViolationException("SALE_NOT_HELD", "Sale " + saleId + " is not held");
        }
        sale.setStatus(ESaleStatus.DRAFT);
        return mapper.toResponse(sale, getItems(saleId));
    }

    @Override
    @Transactional
    public POSSaleResponse cancel(UUID saleId) {
        POSSale sale = getOrThrow(saleId);
        if (sale.getStatus() != ESaleStatus.DRAFT && sale.getStatus() != ESaleStatus.HELD) {
            throw new BusinessRuleViolationException("SALE_NOT_CANCELLABLE",
                    "Only draft or held sales can be cancelled. Completed sales need a return");
        }
        sale.setStatus(ESaleStatus.CANCELLED);

        return mapper.toResponse(sale, getItems(saleId));
    }

    @Override
    @Transactional
    public POSSaleResponse complete(UUID saleId, CompleteSaleRequest request) {
        POSSale sale = getDraftOrThrow(saleId);
        List<POSSaleItem> items = getItems(saleId);
        if (items.isEmpty()) {
            throw new BusinessRuleViolationException("EMPTY_SALE", "Cannot complete a sale with no items");
        }

        //Check if all Amount Paid in any Method equals Sales Total, if not Reject
        BigDecimal tenderedTotal = request.tenders().stream()
                .map(TenderRequest::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (tenderedTotal.compareTo(sale.getGrandTotal()) != 0) {
            throw new BusinessRuleViolationException("TENDER_MISMATCH",
                    "Tendered amount " + tenderedTotal + " does not equal sale total " + sale.getGrandTotal());
        }

        for (TenderRequest tender : request.tenders()) {
            ProcessPaymentRequest paymentRequest = new ProcessPaymentRequest(
                    sale.getId(), EPayableType.POS_SALE, sale.getGrandTotal(),
                    tender.amount(), tender.method(), tender.reference());
            paymentService.processPayment(paymentRequest);
        }

        deductStockForSale(sale, items);

        sale.setStatus(ESaleStatus.COMPLETED);
        sale.setCompletedAt(LocalDateTime.now());
        return mapper.toResponse(sale, items);
    }

    @Transactional
    @Override
    public POSCreditSaleResponse completeOnCredit(UUID saleId, CompleteSaleOnCreditRequest request) {
        POSSale sale = getDraftForUpdateOrThrow(saleId);
        List<POSSaleItem> items = getItems(saleId);
        if (items.isEmpty()) {
            throw new BusinessRuleViolationException("EMPTY_SALE", "Cannot complete a sale with no items");
        }

        List<TenderRequest> tenders = request.tenders() == null ? List.of() : request.tenders();
        BigDecimal tendered = tenders.stream().map(TenderRequest::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remaining = sale.getGrandTotal().subtract(tendered);

        if (remaining.signum() <= 0) {
            throw new BusinessRuleViolationException("NOTHING_TO_CREDIT",
                    "Tendered amount covers the whole sale. Use the normal complete endpoint");
        }

        // grant first: if the limit or any credit rule fails, nothing below runs
        ReceivableResponse receivable = creditService.grantCredit(new GrantCreditRequest(
                sale.getHotelBranchId(), request.customerId(), EPayableType.POS_SALE,
                sale.getId(), sale.getSaleNumber(), remaining, request.dueDate()));

        for (TenderRequest tender : tenders) {
            paymentService.processPayment(new ProcessPaymentRequest(
                    sale.getId(), EPayableType.POS_SALE, sale.getGrandTotal(),
                    tender.amount(), tender.method(), tender.reference()));
        }

        finalizeSale(sale, items, ESaleStatus.ON_CREDIT);
        return new POSCreditSaleResponse(mapper.toResponse(sale, items), receivable);
    }

    @Override
    @Transactional(readOnly = true)
    public POSSaleResponse findById(UUID saleId) {
        POSSale sale = getOrThrow(saleId);
        return mapper.toResponse(sale, getItems(saleId));
    }

    private void deductStockForSale(POSSale sale, List<POSSaleItem> items) {
        List<SaleLine> lines = items.stream()
                .filter(i -> i.getProduct().isTrackInventory())
                .map(i -> new SaleLine(i.getId(), i.getProduct().getStockItemId(), null,
                        BigDecimal.valueOf(i.getQuantity()), true))
                .toList();
        if (lines.isEmpty()) {
            return;
        }
        stockApi.consumeForSale(new ConsumeForSaleCommand(
                EOutletType.POS_TERMINAL, sale.getRegisterSession().getTerminal().getId(), null,
                EMovementSourceType.POS_SALE_LINE, sale.getId(), lines,
                currentUserService.requireCurrentStaffId()));
    }

    private void recalculateLineTotal(POSSaleItem item) {
        BigDecimal gross = item.getUnitPriceAtSaleTime().multiply(BigDecimal.valueOf(item.getQuantity()));
        item.setLineTotal(gross.subtract(item.getDiscountAmount()));
    }

    private void recalculateSaleTotals(POSSale sale, List<POSSaleItem> items) {
        BigDecimal subtotal = items.stream()
                .map(POSSaleItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        sale.setSubtotal(subtotal);
        sale.setGrandTotal(subtotal.add(sale.getTaxTotal()).subtract(sale.getDiscountTotal()));
    }

    private POSSale getDraftOrThrow(UUID saleId) {
        POSSale sale = getOrThrow(saleId);
        if (sale.getStatus() != ESaleStatus.DRAFT) {
            throw new BusinessRuleViolationException("SALE_NOT_EDITABLE",
                    "Sale " + saleId + " is not in a draft state");
        }
        return sale;
    }

    private POSSale getOrThrow(UUID saleId) {
        POSSale sale = saleRepo.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("POSSale", saleId));
        branchAccess.require(sale.getHotelBranchId());
        return sale;
    }

    private void finalizeSale(POSSale sale, List<POSSaleItem> items, ESaleStatus finalStatus) {
        deductStockForSale(sale, items);
        sale.setStatus(finalStatus);
        sale.setCompletedAt(LocalDateTime.now());
    }

    private POSSale getDraftForUpdateOrThrow(UUID saleId) {
        POSSale sale = saleRepo.findByIdForUpdate(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("POSSale", saleId));
        branchAccess.require(sale.getHotelBranchId());

        if (sale.getStatus() != ESaleStatus.DRAFT) {
            throw new BusinessRuleViolationException("SALE_NOT_EDITABLE",
                    "Sale " + saleId + " is not in a draft state");
        }
        return sale;
    }

    private List<POSSaleItem> getItems(UUID saleId) {
        return saleItemRepo.findBySaleId(saleId);
    }
}
