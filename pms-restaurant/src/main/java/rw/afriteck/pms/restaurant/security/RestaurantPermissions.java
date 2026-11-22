package rw.afriteck.pms.restaurant.security;

import rw.afriteck.pms.credit.security.CreditPermissions;

public final class RestaurantPermissions {
    private RestaurantPermissions() {}

    public static final class Code {
        private Code() {}
        public static final String SETUP_VIEW         = "RESTAURANT_SETUP_VIEW";
        public static final String SETUP_MANAGE       = "RESTAURANT_SETUP_MANAGE";
        public static final String MENU_PRICE_MANAGE  = "RESTAURANT_MENU_PRICE_MANAGE";
        public static final String ORDER_PLACE        = "RESTAURANT_ORDER_PLACE";
        public static final String BILL_VIEW          = "RESTAURANT_BILL_VIEW";
        public static final String BILL_REQUEST       = "RESTAURANT_BILL_REQUEST";
        public static final String BILL_PAY           = "RESTAURANT_BILL_PAY";
        public static final String BILL_VOID_ITEM     = "RESTAURANT_BILL_VOID_ITEM";
        public static final String BILL_COMP          = "RESTAURANT_BILL_COMP";
        public static final String TABLE_SPLIT_MERGE  = "RESTAURANT_TABLE_SPLIT_MERGE";
    }

    public static final class Has {
        private Has() {}
        public static final String SETUP_VIEW         = "hasAuthority('" + Code.SETUP_VIEW + "')";
        public static final String SETUP_MANAGE       = "hasAuthority('" + Code.SETUP_MANAGE + "')";
        public static final String MENU_PRICE_MANAGE  = "hasAuthority('" + Code.MENU_PRICE_MANAGE + "')";
        public static final String ORDER_PLACE        = "hasAuthority('" + Code.ORDER_PLACE + "')";
        public static final String BILL_VIEW          = "hasAuthority('" + Code.BILL_VIEW + "')";
        public static final String BILL_REQUEST       = "hasAuthority('" + Code.BILL_REQUEST + "')";
        public static final String BILL_PAY           = "hasAuthority('" + Code.BILL_PAY + "')";
        public static final String BILL_VOID_ITEM     = "hasAuthority('" + Code.BILL_VOID_ITEM + "')";
        public static final String BILL_COMP          = "hasAuthority('" + Code.BILL_COMP + "')";
        public static final String TABLE_SPLIT_MERGE  = "hasAuthority('" + Code.TABLE_SPLIT_MERGE + "')";
        /** Closing a bill on credit settles the bill's remainder and extends credit, so both are needed. */
        public static final String CLOSE_ON_CREDIT    = "hasAuthority('" + Code.BILL_PAY + "') and hasAuthority('"
                + CreditPermissions.Code.GRANT + "')";
    }
}
