package rw.afriteck.pms.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import rw.afriteck.pms.auth.dtos.PlatformPrincipal;
import rw.afriteck.pms.auth.security.JwtAuthenticationFilter;
import rw.afriteck.pms.auth.security.JwtTokenProvider;
import rw.afriteck.pms.common.tenant.TenantContext;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtAuthenticationFilterTenantTest {

    private final JwtTokenProvider provider =
            new JwtTokenProvider("0123456789-0123456789-0123456789-0123", 900_000, 900_000);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(provider);

    @AfterEach
    void cleanUp() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequest requestFor(UUID hotelId) {
        String token = provider.generateAccessToken(UUID.randomUUID(), UUID.randomUUID(), "cashier1",
                hotelId, UUID.randomUUID(), Set.of("CASHIER"), Set.of("SALE_CREATE"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    @Test
    void tenantComesFromTheToken_andIsClearedAfterTheRequest() throws Exception {
        UUID hotelId = UUID.randomUUID();
        AtomicReference<Optional<UUID>> seen = new AtomicReference<>();

        filter.doFilter(requestFor(hotelId), new MockHttpServletResponse(),
                (req, res) -> seen.set(TenantContext.get()));

        assertThat(seen.get()).contains(hotelId);
        assertThat(TenantContext.get()).isEmpty();
    }

    @Test
    void tenantIsClearedEvenWhenTheChainThrows() {
        assertThatThrownBy(() -> filter.doFilter(requestFor(UUID.randomUUID()), new MockHttpServletResponse(),
                (req, res) -> { throw new IllegalStateException("boom"); }))
                .isInstanceOf(IllegalStateException.class);

        assertThat(TenantContext.get()).isEmpty();
    }

    @Test
    void aClientSuppliedHotelHeaderIsIgnored() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Hotel-Id", UUID.randomUUID().toString());
        AtomicReference<Optional<UUID>> seen = new AtomicReference<>();

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> seen.set(TenantContext.get()));

        assertThat(seen.get()).isEmpty();
    }

    @Test
    void platformTokenAuthenticatesAsOperator_andNeverSetsATenant() throws Exception {
        String token = provider.generatePlatformAccessToken(UUID.randomUUID(), "op1", Set.of("PLATFORM_VIEW_HOTELS"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        AtomicReference<Optional<UUID>> tenantSeen = new AtomicReference<>();
        AtomicReference<Object> principalSeen = new AtomicReference<>();

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            tenantSeen.set(TenantContext.get());
            principalSeen.set(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        });

        assertThat(principalSeen.get()).isInstanceOf(PlatformPrincipal.class);
        assertThat(tenantSeen.get()).isEmpty();
    }
}
