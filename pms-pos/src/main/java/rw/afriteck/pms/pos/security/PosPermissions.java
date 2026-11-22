package rw.afriteck.pms.pos.security;

import rw.afriteck.pms.credit.security.CreditPermissions;

public final class PosPermissions {
    private PosPermissions() {}

    public static final class Code {
        private Code() {}
        public static final String CATALOG_VIEW    = "POS_CATALOG_VIEW";
        public static final String CATALOG_MANAGE  = "POS_CATALOG_MANAGE";
        public static final String TERMINAL_VIEW   = "POS_TERMINAL_VIEW";
        public static final String TERMINAL_MANAGE = "POS_TERMINAL_MANAGE";
        public static final String SESSION_VIEW    = "POS_SESSION_VIEW";
        public static final String SESSION_MANAGE  = "POS_SESSION_MANAGE";
        public static final String SALE_VIEW       = "POS_SALE_VIEW";
        public static final String SALE_CREATE     = "POS_SALE_CREATE";
        public static final String SALE_CANCEL     = "POS_SALE_CANCEL";
    }

    public static final class Has {
        private Has() {}
        public static final String CATALOG_VIEW    = "hasAuthority('" + Code.CATALOG_VIEW + "')";
        public static final String CATALOG_MANAGE  = "hasAuthority('" + Code.CATALOG_MANAGE + "')";
        public static final String TERMINAL_VIEW   = "hasAuthority('" + Code.TERMINAL_VIEW + "')";
        public static final String TERMINAL_MANAGE = "hasAuthority('" + Code.TERMINAL_MANAGE + "')";
        public static final String SESSION_VIEW    = "hasAuthority('" + Code.SESSION_VIEW + "')";
        public static final String SESSION_MANAGE  = "hasAuthority('" + Code.SESSION_MANAGE + "')";
        public static final String SALE_VIEW       = "hasAuthority('" + Code.SALE_VIEW + "')";
        public static final String SALE_CREATE     = "hasAuthority('" + Code.SALE_CREATE + "')";
        public static final String SALE_CANCEL     = "hasAuthority('" + Code.SALE_CANCEL + "')";
        public static final String COMPLETE_ON_CREDIT = "hasAuthority('" + Code.SALE_CREATE + "') and hasAuthority('"
                + CreditPermissions.Code.GRANT + "')";
    }
}
