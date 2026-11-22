package rw.afriteck.pms.credit.security;

public final class CreditPermissions {
    private CreditPermissions() {}

    public static final class Code {
        private Code() {}
        public static final String CUSTOMER_VIEW   = "CREDIT_CUSTOMER_VIEW";
        public static final String CUSTOMER_MANAGE = "CREDIT_CUSTOMER_MANAGE";
        public static final String GRANT           = "CREDIT_GRANT";
        public static final String RECEIVABLE_VIEW = "CREDIT_RECEIVABLE_VIEW";
        public static final String REPAY           = "CREDIT_REPAY";
        public static final String WRITE_OFF       = "CREDIT_WRITE_OFF";
        public static final String REPORT_VIEW     = "CREDIT_REPORT_VIEW";
    }

    public static final class Has {
        private Has() {}
        public static final String CUSTOMER_VIEW   = "hasAuthority('" + Code.CUSTOMER_VIEW + "')";
        public static final String CUSTOMER_MANAGE = "hasAuthority('" + Code.CUSTOMER_MANAGE + "')";
        public static final String RECEIVABLE_VIEW = "hasAuthority('" + Code.RECEIVABLE_VIEW + "')";
        public static final String REPAY           = "hasAuthority('" + Code.REPAY + "')";
        public static final String WRITE_OFF       = "hasAuthority('" + Code.WRITE_OFF + "')";
        public static final String REPORT_VIEW     = "hasAuthority('" + Code.REPORT_VIEW + "')";
    }
}
