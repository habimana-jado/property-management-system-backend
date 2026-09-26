package rw.afriteck.pms.payment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import rw.afriteck.pms.payment.dtos.PaymentResponse;
import rw.afriteck.pms.payment.dtos.RecordPaymentRequest;
import rw.afriteck.pms.payment.model.Payment;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentResponse toResponse(Payment payment);

}
