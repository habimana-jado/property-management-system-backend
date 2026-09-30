package rw.afriteck.pms.pos.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import rw.afriteck.pms.pos.dtos.*;
import rw.afriteck.pms.pos.model.POSProductCategory;
import rw.afriteck.pms.pos.model.POSTerminal;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface POSTerminalMapper {

    @org.mapstruct.Mapping(target = "id", ignore = true)
    @org.mapstruct.Mapping(target = "status", ignore = true)
    POSTerminal toEntity(CreateTerminalRequest request);

    POSTerminalResponse toResponse(POSTerminal entity);

}
