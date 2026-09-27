package rw.afriteck.pms.payment.mapper;

import org.mapstruct.Mapper;
import rw.afriteck.pms.payment.dtos.PaymentResponse;
import rw.afriteck.pms.payment.model.Payment;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentResponse toResponse(Payment payment);

}
