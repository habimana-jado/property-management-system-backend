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

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (tokenProvider.isValid(token)) {
                Claims claims = tokenProvider.parseClaims(token);
                if ("access".equals(claims.get("type", String.class))) {
                    SecurityContextHolder.getContext().setAuthentication(buildAuthentication(claims));
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private Authentication buildAuthentication(Claims claims) {
        UUID userId = UUID.fromString(claims.getSubject());
        UUID staffId = claims.get("staffId", UUID.class);

        Collection<String> permissionCodes = claims.get("authorities", Collection.class);
        List<SimpleGrantedAuthority> authorities = permissionCodes == null
                ? List.of()
                : permissionCodes.stream().map(SimpleGrantedAuthority::new).toList();

        String hotelBranchIdStr = claims.get("hotelBranchId", String.class);
        UUID hotelBranchId = hotelBranchIdStr != null ? UUID.fromString(hotelBranchIdStr) : null;

        StaffPrincipal principal = new StaffPrincipal(
                userId,
                staffId,
                claims.get("username", String.class),
                hotelBranchId
        );

        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }
}
