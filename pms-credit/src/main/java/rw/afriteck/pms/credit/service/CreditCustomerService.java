package rw.afriteck.pms.credit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;
import rw.afriteck.pms.common.security.CurrentUserService;
import rw.afriteck.pms.credit.dtos.CreateCreditCustomerRequest;
import rw.afriteck.pms.credit.dtos.CreditCustomerResponse;
import rw.afriteck.pms.credit.entity.CreditCustomer;
import rw.afriteck.pms.credit.enums.EReceivableStatus;
import rw.afriteck.pms.credit.repository.CreditCustomerRepository;
import rw.afriteck.pms.credit.repository.ReceivableRepository;
import rw.afriteck.pms.credit.utils.PhoneNumberNormalizer;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditCustomerService {

    private static final List<EReceivableStatus> ACTIVE =
            List.of(EReceivableStatus.OUTSTANDING, EReceivableStatus.PARTIALLY_SETTLED);

    private final CreditCustomerRepository customerRepository;
    private final ReceivableRepository receivableRepository;
    private final PhoneNumberNormalizer phoneNormalizer;
    private final CurrentUserService currentUserService;

    @Transactional
    public CreditCustomerResponse create(CreateCreditCustomerRequest request) {
        String phone = phoneNormalizer.normalize(request.phoneNumber());

        if (customerRepository.existsByHotelIdAndPhoneNumber(currentUserService.requireCurrentHotelBranchId(), phone)) {
            throw new BusinessRuleViolationException("DUPLICATE_PHONE", "A customer with this phone number already exists");
        }

        CreditCustomer customer = new CreditCustomer();
        customer.setFullName(request.fullName().trim());
        customer.setPhoneNumber(phone);
        customer.setCreditLimit(request.creditLimit());
        customer.setCreditAllowed(request.creditLimit().signum() > 0);
        customerRepository.save(customer);

        return toResponse(customer);
    }

    @Transactional(readOnly = true)
    public CreditCustomerResponse findByPhone(String rawPhone) {
        CreditCustomer customer = customerRepository.findByHotelIdAndPhoneNumber(currentUserService.requireCurrentHotelBranchId(), phoneNormalizer.normalize(rawPhone))
                .orElseThrow(() -> new ResourceNotFoundException("Customer", rawPhone));
        return toResponse(customer);
    }

    private CreditCustomerResponse toResponse(CreditCustomer c) {
        BigDecimal outstanding = receivableRepository.sumOutstandingByCustomer(c.getId(), ACTIVE);
        BigDecimal available = c.getCreditLimit().subtract(outstanding).max(BigDecimal.ZERO);
        return new CreditCustomerResponse(c.getId(), c.getFullName(), c.getPhoneNumber(),
                c.isCreditAllowed(), c.getCreditLimit(), outstanding, available);
    }
}
