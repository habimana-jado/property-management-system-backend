package rw.afriteck.pms.restaurant.dtos;

public record TableSplitResponse(
        TableBillResponse originalBill,
        TableBillResponse newBill
) {}
