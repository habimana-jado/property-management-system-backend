package rw.afriteck.pms.pos.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.afriteck.pms.common.enums.EPayableType;
import rw.afriteck.pms.common.enums.EPaymentMethod;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;
import rw.afriteck.pms.common.security.CurrentUserService;
import rw.afriteck.pms.credit.dtos.GrantCreditRequest;
import rw.afriteck.pms.credit.service.CreditService;
import rw.afriteck.pms.payment.dtos.ProcessPaymentRequest;
import rw.afriteck.pms.payment.service.impl.PaymentService;
import rw.afriteck.pms.pos.dtos.CompleteSaleOnCreditRequest;
import rw.afriteck.pms.pos.dtos.TenderRequest;
import rw.afriteck.pms.pos.enums.ESaleStatus;
import rw.afriteck.pms.pos.mapper.POSSaleMapper;
import rw.afriteck.pms.pos.model.*;
import rw.afriteck.pms.pos.repository.POSSaleItemRepository;
import rw.afriteck.pms.pos.repository.POSSaleRepository;
import rw.afriteck.pms.stock.api.StockApi;
import rw.afriteck.pms.stock.api.command.ConsumeForSaleCommand;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class POSSaleServiceCompleteOnCreditTest {

    @Mock
    POSSaleRepository saleRepo;
    @Mock
    POSSaleItemRepository saleItemRepo;
    @Mock
    PaymentService paymentService;
    @Mock
    CreditService creditService;
    @Mock
    StockApi stockApi;
    @Mock
    POSSaleMapper mapper;
    @Mock
    CurrentUserService currentUserService;
    @Mock
    PosBranchAccess branchAccess;
    @InjectMocks
    POSSaleServiceImpl saleService;

    @BeforeEach
    void setUp() {
        lenient().when(currentUserService.requireCurrentStaffId()).thenReturn(UUID.randomUUID());
    }

    private POSSale draftSale(String grandTotal) {
        POSTerminal terminal = new POSTerminal();
        terminal.setId(UUID.randomUUID());
        POSRegisterSession session = new POSRegisterSession();
        session.setTerminal(terminal);

        POSSale s = new POSSale();
        s.setId(UUID.randomUUID());
        s.setSaleNumber("SALE-000001");
        s.setHotelBranchId(UUID.randomUUID());
        s.setStatus(ESaleStatus.DRAFT);
        s.setGrandTotal(new BigDecimal(grandTotal));
        s.setRegisterSession(session);
        return s;
    }

    private POSSaleItem item(boolean tracked, int qty) {
        POSProduct p = new POSProduct();
        p.setTrackInventory(tracked);
        p.setStockItemId(UUID.randomUUID());
        POSSaleItem i = new POSSaleItem();
        i.setId(UUID.randomUUID());
        i.setProduct(p);
        i.setQuantity(qty);
        return i;
    }

    private void givenSale(POSSale sale, POSSaleItem... items) {
        when(saleRepo.findByIdForUpdate(sale.getId())).thenReturn(Optional.of(sale));
        when(saleItemRepo.findBySaleId(sale.getId())).thenReturn(List.of(items));
    }

    private CompleteSaleOnCreditRequest request(List<TenderRequest> tenders) {
        return new CompleteSaleOnCreditRequest(UUID.randomUUID(), LocalDate.now().plusDays(14), tenders);
    }

    @Test
    void withUpfrontTender_creditsOnlyTheRemainder_andMarksSaleOnCredit() {
        POSSale sale = draftSale("20000");
        givenSale(sale, item(true, 1));

        saleService.completeOnCredit(sale.getId(), request(
                List.of(new TenderRequest(EPaymentMethod.CASH, new BigDecimal("5000"), null))));

        ArgumentCaptor<GrantCreditRequest> grant = ArgumentCaptor.forClass(GrantCreditRequest.class);
        verify(creditService).grantCredit(grant.capture());
        assertThat(grant.getValue().amount()).isEqualByComparingTo("15000");
        assertThat(grant.getValue().sourceType()).isEqualTo(EPayableType.POS_SALE);
        assertThat(grant.getValue().sourceId()).isEqualTo(sale.getId());
        assertThat(grant.getValue().originBranchId()).isEqualTo(sale.getHotelBranchId());   // was hotelBranchId()

        ArgumentCaptor<ProcessPaymentRequest> pay = ArgumentCaptor.forClass(ProcessPaymentRequest.class);
        verify(paymentService).processPayment(pay.capture());
        assertThat(pay.getValue().amount()).isEqualByComparingTo("5000");
        assertThat(pay.getValue().amountOwed()).isEqualByComparingTo("20000");

        assertThat(sale.getStatus()).isEqualTo(ESaleStatus.ON_CREDIT);
        assertThat(sale.getCompletedAt()).isNotNull();
        verify(stockApi).consumeForSale(any());
    }

    @Test
    void withNoTenders_putsTheWholeSaleOnCredit_andTakesNoPayment() {
        POSSale sale = draftSale("20000");
        givenSale(sale, item(false, 1));

        saleService.completeOnCredit(sale.getId(), request(null));

        ArgumentCaptor<GrantCreditRequest> grant = ArgumentCaptor.forClass(GrantCreditRequest.class);
        verify(creditService).grantCredit(grant.capture());
        assertThat(grant.getValue().amount()).isEqualByComparingTo("20000");
        verifyNoInteractions(paymentService);
    }

    @Test
    void rejectsWhenTendersAlreadyCoverTheWholeSale() {
        POSSale sale = draftSale("20000");
        givenSale(sale, item(true, 1));

        assertThatThrownBy(() -> saleService.completeOnCredit(sale.getId(), request(
                List.of(new TenderRequest(EPaymentMethod.CASH, new BigDecimal("20000"), null)))))
                .isInstanceOf(BusinessRuleViolationException.class);

        verifyNoInteractions(creditService, paymentService, stockApi);
        assertThat(sale.getStatus()).isEqualTo(ESaleStatus.DRAFT);
    }

    @ParameterizedTest
    @EnumSource(value = ESaleStatus.class, names = {"HELD", "COMPLETED", "ON_CREDIT", "CANCELLED"})
    void rejectsSalesThatAreNotDraft(ESaleStatus status) {
        POSSale sale = draftSale("20000");
        sale.setStatus(status);
        when(saleRepo.findByIdForUpdate(sale.getId())).thenReturn(Optional.of(sale));

        assertThatThrownBy(() -> saleService.completeOnCredit(sale.getId(), request(null)))
                .isInstanceOf(BusinessRuleViolationException.class);

        verifyNoInteractions(creditService, paymentService, stockApi);
        assertThat(sale.getStatus()).isEqualTo(status);
    }

    @Test
    void whenCreditIsRefused_noPaymentNoStockAndSaleStaysDraft() {
        POSSale sale = draftSale("20000");
        givenSale(sale, item(true, 1));
        when(creditService.grantCredit(any())).thenThrow(
                new BusinessRuleViolationException("CREDIT_LIMIT_EXCEEDED", "Credit limit exceeded"));

        assertThatThrownBy(() -> saleService.completeOnCredit(sale.getId(), request(
                List.of(new TenderRequest(EPaymentMethod.CASH, new BigDecimal("1000"), null)))))
                .isInstanceOf(BusinessRuleViolationException.class);

        verifyNoInteractions(paymentService, stockApi);
        assertThat(sale.getStatus()).isEqualTo(ESaleStatus.DRAFT);
    }

    @Test
    void stockIsConsumedOnce_withTrackedItemsOnly_evenWithSeveralItems() {
        POSSale sale = draftSale("30000");
        givenSale(sale, item(true, 2), item(true, 1), item(false, 5));

        saleService.completeOnCredit(sale.getId(), request(null));

        ArgumentCaptor<ConsumeForSaleCommand> cmd = ArgumentCaptor.forClass(ConsumeForSaleCommand.class);
        verify(stockApi, times(1)).consumeForSale(cmd.capture());
        assertThat(cmd.getValue().lines()).hasSize(2); // the untracked item is excluded
    }
}
