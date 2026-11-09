package rw.afriteck.pms.stock.web.dtos;

import rw.afriteck.pms.stock.api.command.DocumentLineInput;

public interface LineRequest {
    DocumentLineInput toInput();
}
