package rw.afriteck.pms.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.auth.dtos.CreatePlatformOperatorRequest;
import rw.afriteck.pms.auth.dtos.PlatformOperatorResponse;
import rw.afriteck.pms.auth.service.PlatformOperatorService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/platform/operators")
@RequiredArgsConstructor
public class PlatformOperatorController {

    private final PlatformOperatorService operatorService;

    @PostMapping
    public ResponseEntity<PlatformOperatorResponse> create(@Valid @RequestBody CreatePlatformOperatorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(operatorService.create(request));
    }

    @GetMapping
    public List<PlatformOperatorResponse> findAll() { return operatorService.findAll(); }

    @PatchMapping("/{id}/enable")
    public PlatformOperatorResponse enable(@PathVariable UUID id) { return operatorService.setEnabled(id, true); }

    @PatchMapping("/{id}/disable")
    public PlatformOperatorResponse disable(@PathVariable UUID id) { return operatorService.setEnabled(id, false); }
}
