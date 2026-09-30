package rw.afriteck.pms.pos.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.enums.EPayableType;
import rw.afriteck.pms.common.enums.EPaymentMethod;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;
import rw.afriteck.pms.common.exception.ForbiddenException;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;
import rw.afriteck.pms.common.security.CurrentUserService;
import rw.afriteck.pms.payment.repository.PaymentRepo;
import rw.afriteck.pms.pos.dtos.CloseRegisterSessionRequest;
import rw.afriteck.pms.pos.dtos.OpenRegisterSessionRequest;
import rw.afriteck.pms.pos.dtos.POSRegisterSessionResponse;
import rw.afriteck.pms.pos.enums.ERegisterSessionStatus;
import rw.afriteck.pms.pos.mapper.POSRegisterSessionMapper;
import rw.afriteck.pms.pos.model.POSRegisterSession;
import rw.afriteck.pms.pos.model.POSTerminal;
import rw.afriteck.pms.pos.repository.POSRegisterSessionRepository;
import rw.afriteck.pms.pos.repository.POSSaleRepository;
import rw.afriteck.pms.pos.repository.POSTerminalRepository;
import rw.afriteck.pms.pos.service.POSRegisterSessionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class POSRegisterSessionServiceImpl implements POSRegisterSessionService {

    private final POSRegisterSessionRepository sessionRepo;
    private final POSTerminalRepository terminalRepo;
    private final POSSaleRepository saleRepo;
    private final PaymentRepo paymentRepo;
    private final POSRegisterSessionMapper mapper;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public POSRegisterSessionResponse open(OpenRegisterSessionRequest request) {
        POSTerminal terminal = terminalRepo.findById(request.terminalId())
                .orElseThrow(() -> new ResourceNotFoundException("POSTerminal", request.terminalId()));

        UUID currentBranchId = currentUserService.requireCurrentHotelBranchId();
        if (!terminal.getHotelBranchId().equals(currentBranchId)) {
            throw new ForbiddenException("TERMINAL_BRANCH_MISMATCH",
                    "Terminal " + terminal.getId() + " does not belong to your branch");
        }

        if (sessionRepo.existsByTerminalIdAndStatus(terminal.getId(), ERegisterSessionStatus.OPEN)) {
            throw new BusinessRuleViolationException("SESSION_ALREADY_OPEN",
                    "An open register session already exists for terminal " + terminal.getId());
        }

        POSRegisterSession entity = new POSRegisterSession();
        entity.setTerminal(terminal);
        entity.setHotelBranchId(terminal.getHotelBranchId());
        entity.setCashierId(currentUserService.requireCurrentStaffId());
        entity.setOpeningCash(request.openingCash());
        entity.setStatus(ERegisterSessionStatus.OPEN);

        sessionRepo.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public POSRegisterSessionResponse close(UUID id, CloseRegisterSessionRequest request) {
        POSRegisterSession entity = getOrThrow(id);

        if (entity.getStatus() != ERegisterSessionStatus.OPEN) {
            throw new BusinessRuleViolationException("SESSION_NOT_OPEN",
                    "Register session " + id + " is not open");
        }

        List<UUID> saleIds = saleRepo.findIdsByRegisterSessionId(entity.getId());

        //TODO: Handle both Expected Closing CASH, Closing MOMO and CARD not only CASH
        BigDecimal cashCollected = saleIds.isEmpty()
                ? BigDecimal.ZERO
                : paymentRepo.sumAmountByPayableIdsAndTypeAndMethod(saleIds, EPayableType.POS_SALE, EPaymentMethod.CASH);

        BigDecimal expectedClosingCash = entity.getOpeningCash().add(cashCollected);
        BigDecimal variance = request.actualClosingCash().subtract(expectedClosingCash);

        entity.setExpectedClosingCash(expectedClosingCash);
        entity.setActualClosingCash(request.actualClosingCash());
        entity.setVariance(variance);
        entity.setStatus(ERegisterSessionStatus.CLOSED);
        entity.setClosedAt(LocalDateTime.now());

        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public POSRegisterSessionResponse findById(UUID id) {
        return mapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public POSRegisterSessionResponse getActiveSession(UUID terminalId) {
        POSRegisterSession entity = sessionRepo.findByTerminalIdAndStatus(terminalId, ERegisterSessionStatus.OPEN)
                .orElseThrow(() -> new ResourceNotFoundException("Active POSRegisterSession", terminalId));
        return mapper.toResponse(entity);
    }

    private POSRegisterSession getOrThrow(UUID id) {
        return sessionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("POSRegisterSession", id));
    }
}
