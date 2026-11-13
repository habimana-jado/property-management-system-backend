package rw.afriteck.pms.stock.web.security;

/**
 * Single source of truth for stock permissions.
 *
 * Code = the permission code stored in the permissions table and carried in the JWT "authorities" claim.
 * Has  = the SpEL expression used in @PreAuthorize (permission-based, never role-based).
 *
 * Keep Code in sync with sql/stock_permissions.sql.
 */
public final class StockPermissions {

    private StockPermissions() {}

    public static final class Code {
        private Code() {}

        public static final String CATALOG_VIEW      = "STOCK_CATALOG_VIEW";
        public static final String CATALOG_MANAGE    = "STOCK_CATALOG_MANAGE";
        public static final String RECIPE_MANAGE     = "STOCK_RECIPE_MANAGE";
        public static final String STORE_MANAGE      = "STOCK_STORE_MANAGE";
        public static final String SETTINGS_MANAGE   = "STOCK_SETTINGS_MANAGE";
        public static final String VIEW              = "STOCK_VIEW";
        public static final String HISTORY_VIEW      = "STOCK_HISTORY_VIEW";
        public static final String RECEIPT_POST      = "STOCK_RECEIPT_POST";
        public static final String TRANSFER_POST     = "STOCK_TRANSFER_POST";
        public static final String DAMAGE_REPORT_POST = "STOCK_DAMAGE_REPORT_POST";
        public static final String WRITE_OFF_POST    = "STOCK_WRITE_OFF_POST";
        public static final String ADJUSTMENT_POST   = "STOCK_ADJUSTMENT_POST";
        public static final String CONSUMPTION_POST  = "STOCK_CONSUMPTION_POST";
        public static final String STOCKTAKE_COUNT   = "STOCK_STOCKTAKE_COUNT";
        public static final String STOCKTAKE_APPROVE = "STOCK_STOCKTAKE_APPROVE";
    }

    public static final class Has {
        private Has() {}

        public static final String CATALOG_VIEW      = "hasAuthority('" + Code.CATALOG_VIEW + "')";
        public static final String CATALOG_MANAGE    = "hasAuthority('" + Code.CATALOG_MANAGE + "')";
        public static final String RECIPE_MANAGE     = "hasAuthority('" + Code.RECIPE_MANAGE + "')";
        public static final String STORE_MANAGE      = "hasAuthority('" + Code.STORE_MANAGE + "')";
        public static final String SETTINGS_MANAGE   = "hasAuthority('" + Code.SETTINGS_MANAGE + "')";
        public static final String VIEW              = "hasAuthority('" + Code.VIEW + "')";
        public static final String HISTORY_VIEW      = "hasAuthority('" + Code.HISTORY_VIEW + "')";
        public static final String RECEIPT_POST      = "hasAuthority('" + Code.RECEIPT_POST + "')";
        public static final String TRANSFER_POST     = "hasAuthority('" + Code.TRANSFER_POST + "')";
        public static final String DAMAGE_REPORT_POST = "hasAuthority('" + Code.DAMAGE_REPORT_POST + "')";
        public static final String WRITE_OFF_POST    = "hasAuthority('" + Code.WRITE_OFF_POST + "')";
        public static final String ADJUSTMENT_POST   = "hasAuthority('" + Code.ADJUSTMENT_POST + "')";
        public static final String CONSUMPTION_POST  = "hasAuthority('" + Code.CONSUMPTION_POST + "')";
        public static final String STOCKTAKE_COUNT   = "hasAuthority('" + Code.STOCKTAKE_COUNT + "')";
        public static final String STOCKTAKE_APPROVE = "hasAuthority('" + Code.STOCKTAKE_APPROVE + "')";

        /**
         * Transfers share one endpoint but need different permissions by purpose:
         * DAMAGE_REPORT needs STOCK_DAMAGE_REPORT_POST, everything else STOCK_TRANSFER_POST.
         * Requires the controller parameter to be named "r" (-parameters is on in spring-boot-starter-parent).
         */
        public static final String TRANSFER_BY_PURPOSE =
                "#r.purpose()?.name() == 'DAMAGE_REPORT' ? " + DAMAGE_REPORT_POST + " : " + TRANSFER_POST;
    }
}