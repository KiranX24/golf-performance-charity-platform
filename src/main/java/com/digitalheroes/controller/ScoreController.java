
package com.digitalheroes.controller;

import com.digitalheroes.dto.*;
import com.digitalheroes.service.ScoreService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scores")
public class ScoreController {

    private final ScoreService s;

    public ScoreController(ScoreService s) {
        this.s = s;
    }

    @GetMapping
    public List<ScoreResponse> mine(Authentication a) {
        return s.mine(a.getName());
    }

    @PostMapping
    public ScoreResponse add(
            Authentication a,
            @Valid @RequestBody ScoreRequest r) {

        return s.add(a.getName(), r);
    }

    @PutMapping("/{id}")
    public ScoreResponse update(
            Authentication a,
            @PathVariable Long id,
            @Valid @RequestBody ScoreRequest r) {

        return s.update(a.getName(), id, r);
    }

    @DeleteMapping("/{id}")
    public void delete(
            Authentication a,
            @PathVariable Long id) {

        s.delete(a.getName(), id);
    }
}
