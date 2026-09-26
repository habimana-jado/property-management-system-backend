package rw.afriteck.pms.restaurant.dtos;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PlaceOrderRequest(
        @NotEmpty List<OrderLineRequest> orderLineRequests
) {
}
