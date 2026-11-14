package rw.afriteck.pms.stock.internal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import rw.afriteck.pms.stock.api.setup.UnitView;
import rw.afriteck.pms.stock.internal.model.UnitOfMeasure;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UnitMapper {

    UnitView toView(UnitOfMeasure unit);
}