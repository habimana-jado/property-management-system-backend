package rw.afriteck.pms.stock.internal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import rw.afriteck.pms.stock.api.setup.StoreView;
import rw.afriteck.pms.stock.internal.model.Store;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface StoreMapper {

    @Mapping(target = "branchId", source = "branch.id")
    StoreView toView(Store store);
}