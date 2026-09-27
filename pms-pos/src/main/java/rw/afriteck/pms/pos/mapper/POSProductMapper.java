package rw.afriteck.pms.pos.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import rw.afriteck.pms.pos.dtos.CreatePOSProductRequest;
import rw.afriteck.pms.pos.dtos.POSProductResponse;
import rw.afriteck.pms.pos.dtos.UpdatePOSProductRequest;
import rw.afriteck.pms.pos.model.POSProduct;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface POSProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "category", ignore = true) // set in service — needs a lookup, not just an id copy
    POSProduct toEntity(CreatePOSProductRequest request);

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.categoryName", target = "categoryName")
    POSProductResponse toResponse(POSProduct entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hotelBranchId", ignore = true)
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateEntity(@MappingTarget POSProduct entity, UpdatePOSProductRequest request);
}
