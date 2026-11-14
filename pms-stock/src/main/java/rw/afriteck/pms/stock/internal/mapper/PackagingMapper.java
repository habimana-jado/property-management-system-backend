package rw.afriteck.pms.stock.internal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import rw.afriteck.pms.stock.api.setup.PackagingView;
import rw.afriteck.pms.stock.internal.model.ItemPackaging;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PackagingMapper {

    @Mapping(target = "unitId", source = "unit.id")
    @Mapping(target = "unitCode", source = "unit.code")
    PackagingView toView(ItemPackaging packaging);
}