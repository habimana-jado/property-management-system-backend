package rw.afriteck.pms.pos.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;
import rw.afriteck.pms.common.security.BranchAccessGuard;
import rw.afriteck.pms.common.tenant.HotelDirectory;
import rw.afriteck.pms.common.tenant.TenantContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PosBranchAccessTest {

    @Mock
    BranchAccessGuard guard;
    @Mock
    HotelDirectory hotelDirectory;
    @InjectMocks
    PosBranchAccess access;

    private final UUID hotelId = UUID.randomUUID();
    private final UUID branchId = UUID.randomUUID();

    @BeforeEach
    void setTenant() { TenantContext.set(hotelId); }

    @AfterEach
    void clearTenant() { TenantContext.clear(); }

    @Test
    void aBranchOfAnotherHotelIsReportedAsNotFound() {
        when(hotelDirectory.branchBelongsToHotel(branchId, hotelId)).thenReturn(false);

        assertThatThrownBy(() -> access.requireBranch(branchId)).isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(guard);
    }

    @Test
    void aBranchOfThisHotelIsAlsoCheckedAgainstTheUsersBranch() {
        when(hotelDirectory.branchBelongsToHotel(branchId, hotelId)).thenReturn(true);

        access.requireBranch(branchId);

        verify(guard).requireAccessTo(branchId);
    }
}
