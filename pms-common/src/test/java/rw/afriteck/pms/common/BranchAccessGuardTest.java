package rw.afriteck.pms.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.afriteck.pms.common.exception.ForbiddenException;
import rw.afriteck.pms.common.security.BranchAccessGuard;
import rw.afriteck.pms.common.security.CurrentUserService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchAccessGuardTest {

    @Mock
    CurrentUserService currentUserService;
    @InjectMocks
    BranchAccessGuard guard;

    private final UUID branchA = UUID.randomUUID();
    private final UUID branchB = UUID.randomUUID();

    @Test
    void branchStaffMayActOnTheirOwnBranch() {
        when(currentUserService.requireCurrentHotelBranchId()).thenReturn(branchA);

        assertThatCode(() -> guard.requireAccessTo(branchA)).doesNotThrowAnyException();
    }

    @Test
    void branchStaffMayNotActOnAnotherBranch() {
        when(currentUserService.requireCurrentHotelBranchId()).thenReturn(branchA);

        assertThatThrownBy(() -> guard.requireAccessTo(branchB)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void branchStaffMayNotActOnAResourceWithNoBranch() {
        when(currentUserService.requireCurrentHotelBranchId()).thenReturn(branchA);

        assertThatThrownBy(() -> guard.requireAccessTo(null)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void hotelWideAccountsMayActOnAnyBranch() {
        when(currentUserService.requireCurrentHotelBranchId()).thenReturn(null);

        assertThatCode(() -> guard.requireAccessTo(branchB)).doesNotThrowAnyException();
        assertThatCode(() -> guard.requireAccessTo(null)).doesNotThrowAnyException();
    }

    @Test
    void restrictedBranchIdIsEmptyOnlyForHotelWideAccounts() {
        when(currentUserService.requireCurrentHotelBranchId()).thenReturn(branchA, (UUID) null);

        assertThat(guard.restrictedBranchId()).contains(branchA);
        assertThat(guard.restrictedBranchId()).isEmpty();
    }
}
