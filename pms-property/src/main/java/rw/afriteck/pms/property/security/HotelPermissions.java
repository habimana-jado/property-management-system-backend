package rw.afriteck.pms.property.security;

public final class HotelPermissions {
    private HotelPermissions() {}

    public static final class Code {
        private Code() {}
        public static final String PROFILE_VIEW   = "HOTEL_PROFILE_VIEW";
        public static final String PROFILE_MANAGE = "HOTEL_PROFILE_MANAGE";
        public static final String BRANCH_VIEW    = "HOTEL_BRANCH_VIEW";
        public static final String BRANCH_MANAGE  = "HOTEL_BRANCH_MANAGE";
    }

    public static final class Has {
        private Has() {}
        public static final String PROFILE_VIEW   = "hasAuthority('" + Code.PROFILE_VIEW + "')";
        public static final String PROFILE_MANAGE = "hasAuthority('" + Code.PROFILE_MANAGE + "')";
        public static final String BRANCH_VIEW    = "hasAuthority('" + Code.BRANCH_VIEW + "')";
        public static final String BRANCH_MANAGE  = "hasAuthority('" + Code.BRANCH_MANAGE + "')";
    }
}
