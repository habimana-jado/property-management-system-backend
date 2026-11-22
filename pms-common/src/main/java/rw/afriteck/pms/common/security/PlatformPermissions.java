package rw.afriteck.pms.common.security;

public final class PlatformPermissions {
    private PlatformPermissions() {}

    public static final class Code {
        private Code() {}
        public static final String VIEW_HOTELS      = "PLATFORM_VIEW_HOTELS";
        public static final String MANAGE_HOTELS    = "PLATFORM_MANAGE_HOTELS";
        public static final String MANAGE_OPERATORS = "PLATFORM_MANAGE_OPERATORS";
    }

    public static final class Has {
        private Has() {}
        public static final String VIEW_HOTELS      = "hasAuthority('" + Code.VIEW_HOTELS + "')";
        public static final String MANAGE_HOTELS    = "hasAuthority('" + Code.MANAGE_HOTELS + "')";
        public static final String MANAGE_OPERATORS = "hasAuthority('" + Code.MANAGE_OPERATORS + "')";
    }
}
