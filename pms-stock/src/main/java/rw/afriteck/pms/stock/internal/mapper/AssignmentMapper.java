package rw.afriteck.pms.stock.internal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import rw.afriteck.pms.stock.api.setup.AssignmentView;
import rw.afriteck.pms.stock.internal.model.StoreAssignment;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AssignmentMapper {

    @Mapping(target = "storeId", source = "store.id")
    @Mapping(target = "storeName", source = "store.name")
    @Mapping(target = "defaultStore", source = "default")      // entity getter is isDefault()
    AssignmentView toView(StoreAssignment assignment);
}