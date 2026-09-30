package rw.afriteck.pms.pos.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import rw.afriteck.pms.pos.dtos.POSRegisterSessionResponse;
import rw.afriteck.pms.pos.model.POSRegisterSession;

@Mapper(componentModel = "spring")
public interface POSRegisterSessionMapper {

    @Mapping(source = "terminal.id", target = "terminalId")
    POSRegisterSessionResponse toResponse(POSRegisterSession entity);
}