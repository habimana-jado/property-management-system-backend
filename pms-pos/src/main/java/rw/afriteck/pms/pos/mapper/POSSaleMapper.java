package rw.afriteck.pms.pos.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import rw.afriteck.pms.pos.dtos.POSSaleItemResponse;
import rw.afriteck.pms.pos.dtos.POSSaleResponse;
import rw.afriteck.pms.pos.model.POSSale;
import rw.afriteck.pms.pos.model.POSSaleItem;

import java.util.List;

@Mapper(componentModel = "spring")
public interface POSSaleMapper {

    @Mapping(target = "items", ignore = true)
    POSSaleResponse toResponseWithoutItems(POSSale entity);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    POSSaleItemResponse toItemResponse(POSSaleItem entity);

    default POSSaleResponse toResponse(POSSale entity, List<POSSaleItem> items) {
        POSSaleResponse base = toResponseWithoutItems(entity);
        List<POSSaleItemResponse> itemResponses = items.stream().map(this::toItemResponse).toList();
        return new POSSaleResponse(
                base.id(), base.saleNumber(), base.cashierId(), base.customerId(), base.status(),
                base.subtotal(), base.discountTotal(), base.taxTotal(), base.grandTotal(),
                itemResponses, base.completedAt()
        );
    }
}
