package rw.afriteck.pms.pos.dtos;

import rw.afriteck.pms.credit.dtos.ReceivableResponse;

public record POSCreditSaleResponse(
        POSSaleResponse sale,
        ReceivableResponse receivable        // from pms-credit: gives the receipt the due date and amount owed
) {}
