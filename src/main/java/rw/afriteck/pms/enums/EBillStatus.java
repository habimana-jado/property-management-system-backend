package rw.afriteck.pms.enums;

public enum EBillStatus {
    OPEN,            // orders being taken, table active
    BILL_REQUESTED,  // bill generated/printed for customer review
    PARTIALLY_PAID,  // for split payments
    PAID,            // fully settled — this is what locks the table
    CANCELLED
}
