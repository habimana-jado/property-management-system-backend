package rw.afriteck.pms.pos.enums;

public enum ESaleStatus {
    DRAFT,
    HELD,
    COMPLETED,
    CANCELLED,
    PARTIALLY_REFUNDED,
    REFUNDED,
    ON_CREDIT   // closed with the unpaid remainder moved to a receivable
}
