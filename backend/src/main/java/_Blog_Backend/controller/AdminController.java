package _Blog_Backend.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import _Blog_Backend.dto.ResolveRequest;
import _Blog_Backend.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/reports")
    public ResponseEntity<?> getInbox(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(size, 20);
        return ResponseEntity.ok(adminService.getPendingReports(PageRequest.of(page, safeSize)));
    }

    @PutMapping("/reports/{reportId}/resolve")
    public ResponseEntity<String> resolveReport(
            @PathVariable Long reportId,
            @Valid @RequestBody ResolveRequest request) {

        adminService.resolveReport(reportId, request.action());
        return ResponseEntity.ok("Report resolved successfully.");
    }
}