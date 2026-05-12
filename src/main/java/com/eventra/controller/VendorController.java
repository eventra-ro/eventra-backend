package com.eventra.controller;

import com.eventra.dto.request.CreateVendorProfileRequest;
import com.eventra.dto.request.UpdateVendorProfileRequest;
import com.eventra.dto.response.*;
import com.eventra.service.VendorService;
import com.eventra.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    // ── PUBLIC ───────────────────────────────────────────────

    @GetMapping("/api/v1/vendor-categories")
    public ResponseEntity<ApiResponse<List<VendorCategoryResponse>>> getCategories() {
        return ResponseEntity.ok(
                ApiResponse.success(vendorService.getAllCategories()));
    }

    @GetMapping("/api/v1/counties")
    public ResponseEntity<ApiResponse<List<CountyResponse>>> getCounties() {
        return ResponseEntity.ok(
                ApiResponse.success(vendorService.getAllCounties()));
    }

    @GetMapping("/api/v1/event-types")
    public ResponseEntity<ApiResponse<List<EventTypeResponse>>> getEventTypes() {
        return ResponseEntity.ok(
                ApiResponse.success(vendorService.getAllEventTypes()));
    }

    @GetMapping("/api/v1/vendors")
    public ResponseEntity<ApiResponse<Page<VendorProfileResponse>>> searchVendors(
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) String countyCode,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                vendorService.searchVendors(categoryCode, countyCode, pageable)));
    }

    @GetMapping("/api/v1/vendors/{id}")
    public ResponseEntity<ApiResponse<VendorProfileResponse>> getVendorById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success(vendorService.getVendorById(id)));
    }

    @GetMapping("/api/v1/vendors/by-slug/{slug}")
    public ResponseEntity<ApiResponse<VendorProfileResponse>> getVendorBySlug(
            @PathVariable String slug) {
        return ResponseEntity.ok(
                ApiResponse.success(vendorService.getVendorBySlug(slug)));
    }

    // ── VENDOR — Propriul profil ─────────────────────────────

    @GetMapping("/api/v1/vendors/me")
    public ResponseEntity<ApiResponse<VendorProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                vendorService.getMyProfile(
                        SecurityUtils.getUserId(userDetails))));
    }

    @PostMapping("/api/v1/vendors/me")
    public ResponseEntity<ApiResponse<VendorProfileResponse>> createProfile(
            @Valid @RequestBody CreateVendorProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        VendorProfileResponse response = vendorService.createProfile(
                SecurityUtils.getUserId(userDetails), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PutMapping("/api/v1/vendors/me")
    public ResponseEntity<ApiResponse<VendorProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateVendorProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.success(
                vendorService.updateProfile(
                        SecurityUtils.getUserId(userDetails), request)));
    }

    @PostMapping(value = "/api/v1/vendors/me/photos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<VendorProfileResponse.PhotoInfo>> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean isCover,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        vendorService.uploadPhoto(
                                SecurityUtils.getUserId(userDetails),
                                file, isCover)));
    }

    @DeleteMapping("/api/v1/vendors/me/photos/{photoId}")
    public ResponseEntity<ApiResponse<Void>> deletePhoto(
            @PathVariable UUID photoId,
            @AuthenticationPrincipal UserDetails userDetails) {

        vendorService.deletePhoto(
                SecurityUtils.getUserId(userDetails), photoId);
        return ResponseEntity.ok(
                ApiResponse.message("Fotografia a fost ștearsă."));
    }
}