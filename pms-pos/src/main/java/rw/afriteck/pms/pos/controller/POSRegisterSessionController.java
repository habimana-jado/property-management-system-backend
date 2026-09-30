package rw.afriteck.pms.pos.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.pos.dtos.CloseRegisterSessionRequest;
import rw.afriteck.pms.pos.dtos.OpenRegisterSessionRequest;
import rw.afriteck.pms.pos.dtos.POSRegisterSessionResponse;
import rw.afriteck.pms.pos.service.POSRegisterSessionService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/pos/register-sessions")
@RequiredArgsConstructor
public class POSRegisterSessionController {

    private final POSRegisterSessionService sessionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public POSRegisterSessionResponse open(@Valid @RequestBody OpenRegisterSessionRequest request) {
        return sessionService.open(request);
    }

    @PatchMapping("/{id}/close")
    public POSRegisterSessionResponse close(@PathVariable UUID id, @Valid @RequestBody CloseRegisterSessionRequest request) {
        return sessionService.close(id, request);
    }

    @GetMapping("/{id}")
    public POSRegisterSessionResponse findById(@PathVariable UUID id) {
        return sessionService.findById(id);
    }

    @GetMapping("/active")
    public POSRegisterSessionResponse getActive(@RequestParam UUID terminalId) {
        return sessionService.getActiveSession(terminalId);
    }
}
