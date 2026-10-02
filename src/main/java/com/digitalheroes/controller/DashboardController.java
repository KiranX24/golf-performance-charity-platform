package com.digitalheroes.controller;

import com.digitalheroes.dto.*;
import com.digitalheroes.service.*;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final DashboardService dashboard;
    private final AdminService admin;

    public DashboardController(
            DashboardService dashboard,
            AdminService admin) {

        this.dashboard = dashboard;
        this.admin = admin;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard(Authentication a) {
        return dashboard.user(a.getName());
    }

    @GetMapping("/admin/stats")
    public AdminStatsResponse stats() {
        return admin.stats();
    }

    @GetMapping("/admin/users")
    public List<AdminUserResponse> users() {
        return admin.users();
    }

    @PatchMapping("/admin/users/{id}/active")
    public AdminUserResponse active(
            @PathVariable Long id,
            @RequestParam boolean value) {

        return admin.setUserActive(id, value);
    }

    // =========================
    // CHARITIES
    // =========================

    @PostMapping("/admin/charities")
    public CharityResponse charity(
            @Valid @RequestBody AdminCharityRequest r) {

        return admin.createCharity(r);
    }

    @PutMapping("/admin/charities/{id}")
    public CharityResponse updateCharity(
            @PathVariable Long id,
            @Valid @RequestBody AdminCharityRequest r) {

        return admin.updateCharity(id, r);
    }

    @PatchMapping("/admin/charities/{id}/archived")
    public CharityResponse archiveCharity(
            @PathVariable Long id,
            @RequestParam boolean value) {

        return admin.setCharityArchived(id, value);
    }

    // =========================
    // PLANS
    // =========================

    @PostMapping("/admin/plans")
    public PlanResponse plan(
            @Valid @RequestBody AdminPlanRequest r) {

        return admin.createPlan(r);
    }

    @PutMapping("/admin/plans/{id}")
    public PlanResponse updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody AdminPlanRequest r) {

        return admin.updatePlan(id, r);
    }

    @PatchMapping("/admin/plans/{id}/active")
    public PlanResponse activePlan(
            @PathVariable Long id,
            @RequestParam boolean value) {

        return admin.setPlanActive(id, value);
    }
    
    @GetMapping("/admin/charities")
    public List<CharityResponse> adminCharities() {
        return admin.allCharities();
    }

    @GetMapping("/admin/plans")
    public List<PlanResponse> adminPlans() {
        return admin.allPlans();
    }
}