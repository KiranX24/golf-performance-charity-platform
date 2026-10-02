
package com.digitalheroes.controller;

import com.digitalheroes.dto.*;
import com.digitalheroes.service.WinnerService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class WinnerController {

    private final WinnerService s;

    public WinnerController(WinnerService s) {
        this.s = s;
    }

    @GetMapping("/winners/me")
    public List<WinnerResponse> mine(Authentication a) {
        return s.mine(a.getName());
    }

    @PostMapping(value = "/winners/{winnerId}/proof", consumes = "multipart/form-data")
    public String proof(
            Authentication a,
            @PathVariable Long winnerId,
            @RequestPart MultipartFile file) throws IOException {

        return s.uploadProof(a.getName(), winnerId, file);
    }

    @GetMapping("/admin/winners")
    public List<WinnerResponse> all() {
        return s.all();
    }

    @PostMapping("/admin/winners/{winnerId}/verify")
    public WinnerResponse verify(
            Authentication a,
            @PathVariable Long winnerId,
            @Valid @RequestBody VerificationRequest r) {

        return s.verify(a.getName(), winnerId, r);
    }

    @PostMapping("/admin/payouts/{payoutId}/paid")
    public WinnerService.PayoutResponseDto paid(
            Authentication a,
            @PathVariable Long payoutId) {

        return s.markPaid(a.getName(), payoutId);
    }
}

