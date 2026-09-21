package rw.afriteck.pms.dtos;

public record TableSplitResponse(
        TableBillResponse originalBill,
        TableBillResponse newBill
) {}
