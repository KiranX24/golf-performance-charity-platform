
package com.digitalheroes.controller;

import com.digitalheroes.dto.*;
import com.digitalheroes.service.DrawService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DrawController {

    private final DrawService s;

    public DrawController(DrawService s) {
        this.s = s;
    }

    @GetMapping("/draws")
    public List<DrawResponse> all() {
        return s.all();
    }

    @GetMapping("/draws/{id}")
    public DrawResponse get(@PathVariable Long id) {
        return s.get(id);
    }

    @PostMapping("/admin/draws/simulate")
    public DrawResponse simulate(
            Authentication a,
            @RequestBody DrawSimulationRequest r) {

        return s.simulate(a.getName(), r);
    }

    @PostMapping("/admin/draws/{id}/publish")
    public DrawResponse publish(
            Authentication a,
            @PathVariable Long id) {

        return s.publish(a.getName(), id);
    }
}

