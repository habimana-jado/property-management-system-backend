package rw.afriteck.pms.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.auth.dtos.CreatePlatformOperatorRequest;
import rw.afriteck.pms.auth.dtos.PlatformOperatorResponse;
import rw.afriteck.pms.auth.dtos.TemporaryPasswordResponse;
import rw.afriteck.pms.auth.service.PlatformOperatorService;
import rw.afriteck.pms.common.security.PlatformPermissions;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/platform/operators")
@RequiredArgsConstructor
public class PlatformOperatorController {

    private final PlatformOperatorService operatorService;

    @PostMapping
    @PreAuthorize(PlatformPermissions.Has.MANAGE_HOTELS)
    public ResponseEntity<PlatformOperatorResponse> create(@Valid @RequestBody CreatePlatformOperatorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(operatorService.create(request));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize(PlatformPermissions.Has.MANAGE_HOTELS)
    public ResponseEntity<TemporaryPasswordResponse> resetPassword(@PathVariable UUID id) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(operatorService.resetPassword(id));
    }

    @GetMapping
    @PreAuthorize(PlatformPermissions.Has.MANAGE_HOTELS)
    public List<PlatformOperatorResponse> findAll() { return operatorService.findAll(); }

    @PatchMapping("/{id}/enable")
    @PreAuthorize(PlatformPermissions.Has.MANAGE_HOTELS)
    public PlatformOperatorResponse enable(@PathVariable UUID id) { return operatorService.setEnabled(id, true); }

    @PatchMapping("/{id}/disable")
    @PreAuthorize(PlatformPermissions.Has.MANAGE_HOTELS)
    public PlatformOperatorResponse disable(@PathVariable UUID id) { return operatorService.setEnabled(id, false); }
}
