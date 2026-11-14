package rw.afriteck.pms.stock.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import rw.afriteck.pms.common.security.CurrentUserService; // adjust to the real package in pms-common
import rw.afriteck.pms.property.model.HotelBranch;
import rw.afriteck.pms.stock.internal.model.Store;

@ExtendWith(MockitoExtension.class)
class BranchGuardTest {

    @Mock CurrentUserService currentUser;

    @Test
    void acceptsOnlyStoresOfTheCallersBranch() {
        UUID mine = UUID.randomUUID();
        when(currentUser.requireCurrentHotelBranchId()).thenReturn(mine);
        BranchGuard guard = new BranchGuard(currentUser);

        assertThat(guard.currentBranchId()).isEqualTo(mine);
        assertThat(guard.inCurrentBranch(storeIn(mine))).isTrue();
        assertThat(guard.inCurrentBranch(storeIn(UUID.randomUUID()))).isFalse();
    }

    private static Store storeIn(UUID branchId) {
        HotelBranch b = new HotelBranch();
        ReflectionTestUtils.setField(b, "id", branchId);
        Store s = new Store();
        s.setBranch(b);
        return s;
    }
}