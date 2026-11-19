package rw.afriteck.pms.auth.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import rw.afriteck.pms.auth.dtos.PlatformPrincipal;
import rw.afriteck.pms.common.tenant.TenantContext;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                if (tokenProvider.isValid(token)) {
                    Claims claims = tokenProvider.parseClaims(token);
                    String type = claims.get("type", String.class);

                    if ("access".equals(type)) {
                        StaffPrincipal principal = staffPrincipal(claims);
                        SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(principal, null, authorities(claims)));
                        if (principal.hotelId() != null) {
                            TenantContext.set(principal.hotelId());
                        }
                    } else if ("platform-access".equals(type)) {
                        PlatformPrincipal principal = new PlatformPrincipal(
                                UUID.fromString(claims.getSubject()), claims.get("username", String.class));
                        SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(principal, null, authorities(claims)));
                        // deliberately no tenant: platform operators never act inside a hotel
                    }
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    @SuppressWarnings("unchecked")
    private Authentication buildAuthentication(Claims claims) {
        UUID userId = UUID.fromString(claims.getSubject());

        String staffIdStr = claims.get("staffId", String.class);
        UUID staffId = staffIdStr != null ? UUID.fromString(staffIdStr) : null;

        Collection<String> permissionCodes = claims.get("authorities", Collection.class);
        List<SimpleGrantedAuthority> authorities = permissionCodes == null
                ? List.of()
                : permissionCodes.stream().map(SimpleGrantedAuthority::new).toList();

        String hotelBranchIdStr = claims.get("hotelBranchId", String.class);
        UUID hotelBranchId = hotelBranchIdStr != null ? UUID.fromString(hotelBranchIdStr) : null;
        String hotelIdStr = claims.get("hotelId", String.class);
        UUID hotelId = hotelIdStr != null ? UUID.fromString(hotelIdStr) : null;

        StaffPrincipal principal = new StaffPrincipal(userId, staffId,
                claims.get("username", String.class), hotelId, hotelBranchId);

        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }

    private List<SimpleGrantedAuthority> authorities(Claims claims) {
        Collection<?> codes = claims.get("authorities", Collection.class);
        return codes == null ? List.of()
                : codes.stream().map(String::valueOf).map(SimpleGrantedAuthority::new).toList();
    }

    private StaffPrincipal staffPrincipal(Claims claims) {
        String staffId = claims.get("staffId", String.class);
        String hotelId = claims.get("hotelId", String.class);
        String branchId = claims.get("hotelBranchId", String.class);
        return new StaffPrincipal(
                UUID.fromString(claims.getSubject()),
                staffId != null ? UUID.fromString(staffId) : null,
                claims.get("username", String.class),
                hotelId != null ? UUID.fromString(hotelId) : null,
                branchId != null ? UUID.fromString(branchId) : null);
    }
}
