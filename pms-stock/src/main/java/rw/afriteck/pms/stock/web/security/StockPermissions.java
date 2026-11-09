package rw.afriteck.pms.stock.web.security;

public final class StockPermissions {

    private StockPermissions() {}

    // Role names as pms-auth defines them, WITHOUT the "ROLE_" prefix.
    // PLACEHOLDERS: replace with your real role names.
    public static final String ROLE_STORE_MANAGER = "STORE_MANAGER";
    public static final String ROLE_STOCK_VIEWER  = "STOCK_VIEWER";   // optional: read-only access (owner, accountant)

    // Complete SpEL expressions for @PreAuthorize
    /** Posting documents, transfers, stocktakes. */
    public static final String STORE_MANAGER =
            "hasRole('" + ROLE_STORE_MANAGER + "')";

    /** Reports and details. Manager-only today; add the viewer role here if you create it. */
    public static final String CAN_VIEW_STOCK =
            "hasAnyRole('" + ROLE_STORE_MANAGER + "','" + ROLE_STOCK_VIEWER + "')";
}
