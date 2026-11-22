package rw.afriteck.pms.auth.security;

public final class AuthPermissions {
    private AuthPermissions() {}

    public static final class Code {
        private Code() {}
        public static final String STAFF_VIEW           = "STAFF_VIEW";
        public static final String STAFF_MANAGE         = "STAFF_MANAGE";
        public static final String STAFF_PASSWORD_RESET = "STAFF_PASSWORD_RESET";
        public static final String ROLE_VIEW            = "STAFF_ROLE_VIEW";
        public static final String ROLE_MANAGE          = "STAFF_ROLE_MANAGE";
    }

    public static final class Has {
        private Has() {}
        public static final String STAFF_VIEW           = "hasAuthority('" + Code.STAFF_VIEW + "')";
        public static final String STAFF_MANAGE         = "hasAuthority('" + Code.STAFF_MANAGE + "')";
        public static final String STAFF_PASSWORD_RESET = "hasAuthority('" + Code.STAFF_PASSWORD_RESET + "')";
        public static final String ROLE_VIEW            = "hasAuthority('" + Code.ROLE_VIEW + "')";
        public static final String ROLE_MANAGE          = "hasAuthority('" + Code.ROLE_MANAGE + "')";
    }
}
