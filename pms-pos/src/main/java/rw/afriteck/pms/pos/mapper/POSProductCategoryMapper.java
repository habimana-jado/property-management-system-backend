package rw.afriteck.pms.pos.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import rw.afriteck.pms.pos.dtos.CreatePOSProductCategoryRequest;
import rw.afriteck.pms.pos.dtos.POSProductCategoryResponse;
import rw.afriteck.pms.pos.dtos.UpdatePOSProductCategoryRequest;
import rw.afriteck.pms.pos.model.POSProductCategory;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface POSProductCategoryMapper {

    @org.mapstruct.Mapping(target = "id", ignore = true)
    @org.mapstruct.Mapping(target = "status", ignore = true)
    POSProductCategory toEntity(CreatePOSProductCategoryRequest request);

    POSProductCategoryResponse toResponse(POSProductCategory entity);

    @org.mapstruct.Mapping(target = "id", ignore = true)
    @org.mapstruct.Mapping(target = "hotelBranchId", ignore = true)
    @org.mapstruct.Mapping(target = "status", ignore = true)
    void updateEntity(@MappingTarget POSProductCategory entity, UpdatePOSProductCategoryRequest request);
}
