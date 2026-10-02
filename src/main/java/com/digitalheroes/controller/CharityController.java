
package com.digitalheroes.controller;

import com.digitalheroes.dto.*;
import com.digitalheroes.entity.Donation;
import com.digitalheroes.service.CharityService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/charities")
public class CharityController {

    private final CharityService s;

    public CharityController(CharityService s) {
        this.s = s;
    }

    @GetMapping
    public List<CharityResponse> list(
            @RequestParam(required = false) String q) {

        return s.list(q);
    }

    @GetMapping("/{id}")
    public CharityResponse get(@PathVariable Long id) {
        return s.get(id);
    }

    @GetMapping("/me/selection")
    public CharitySelectionResponse current(Authentication a) {
        return s.current(a.getName());
    }

    @PostMapping("/me/selection")
    public CharitySelectionResponse select(
            Authentication a,
            @Valid @RequestBody CharitySelectionRequest r) {

        return s.select(a.getName(), r);
    }

    @PostMapping("/donations")
    public Donation donate(
            Authentication a,
            @Valid @RequestBody DonationRequest r) {

        return s.donate(a.getName(), r);
    }
}

