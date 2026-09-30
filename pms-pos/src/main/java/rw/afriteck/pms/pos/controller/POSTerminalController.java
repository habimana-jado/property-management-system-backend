package rw.afriteck.pms.pos.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.pos.dtos.*;
import rw.afriteck.pms.pos.service.POSProductCategoryService;
import rw.afriteck.pms.pos.service.POSTerminalService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/pos/terminals")
@RequiredArgsConstructor
public class POSTerminalController {

    private final POSTerminalService terminalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public POSTerminalResponse create(@Valid @RequestBody CreateTerminalRequest request) {
        return terminalService.create(request);
    }

    @GetMapping("/{id}")
    public POSTerminalResponse findById(@PathVariable UUID id) {
        return terminalService.findById(id);
    }

    @GetMapping
    public List<POSTerminalResponse> findByHotelBranch(@RequestParam UUID hotelBranchId) {
        return terminalService.findByHotelBranch(hotelBranchId);
    }

    @PatchMapping("/{id}/activate")
    public POSTerminalResponse activate(@PathVariable UUID id) {
        return terminalService.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    public POSTerminalResponse deactivate(@PathVariable UUID id) {
        return terminalService.deactivate(id);
    }
}
