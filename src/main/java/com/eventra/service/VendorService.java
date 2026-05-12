package com.eventra.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.eventra.dto.request.CreateVendorProfileRequest;
import com.eventra.dto.request.UpdateVendorProfileRequest;
import com.eventra.dto.response.*;
import com.eventra.entity.*;
import com.eventra.entity.enums.VendorStatus;
import com.eventra.exception.*;
import com.eventra.repository.*;
import com.eventra.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendorService {

    private static final int MAX_PHOTOS_FREE = 5;
    private static final int MAX_PHOTOS_STANDARD = 20;
    private static final int MAX_PHOTOS_PREMIUM = 50;

    private final VendorProfileRepository vendorProfileRepository;
    private final VendorCategoryRepository categoryRepository;
    private final CountyRepository countyRepository;
    private final EventTypeRepository eventTypeRepository;
    private final VendorPhotoRepository photoRepository;
    private final UserRepository userRepository;
    private final Cloudinary cloudinary;

    // ─────────────────────────────────────────────────────────
    // PUBLIC — Referință
    // ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<VendorCategoryResponse> getAllCategories() {
        return categoryRepository
                .findAllByIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(c -> new VendorCategoryResponse(
                        c.getId(), c.getCode(), c.getName(),
                        c.getDescription(), c.getSortOrder()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CountyResponse> getAllCounties() {
        return countyRepository.findAllByOrderByNameAsc()
                .stream()
                .map(c -> new CountyResponse(c.getId(), c.getCode(), c.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventTypeResponse> getAllEventTypes() {
        return eventTypeRepository.findAllByIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(e -> new EventTypeResponse(
                        e.getId(), e.getCode(), e.getName(),
                        e.getIcon(), e.getSortOrder()))
                .toList();
    }

    // ─────────────────────────────────────────────────────────
    // PUBLIC — Discovery
    // ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<VendorProfileResponse> searchVendors(
            String categoryCode, String countyCode, Pageable pageable) {

        return vendorProfileRepository
                .search(VendorStatus.VERIFIED, categoryCode, countyCode, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public VendorProfileResponse getVendorById(UUID vendorId) {
        VendorProfile vendor = findVendorById(vendorId);
        incrementViewCount(vendor);
        return toResponse(vendor);
    }

    @Transactional
    public VendorProfileResponse getVendorBySlug(String slug) {
        VendorProfile vendor = vendorProfileRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Furnizorul nu a fost găsit."));
        incrementViewCount(vendor);
        return toResponse(vendor);
    }

    // ─────────────────────────────────────────────────────────
    // VENDOR — Propriul profil
    // ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public VendorProfileResponse getMyProfile(UUID userId) {
        VendorProfile vendor = findVendorByUserId(userId);
        return toResponse(vendor);
    }

    @Transactional
    public VendorProfileResponse createProfile(
            UUID userId, CreateVendorProfileRequest request) {

        if (vendorProfileRepository.existsByUserId(userId)) {
            throw new VendorProfileAlreadyExistsException();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilizatorul nu a fost găsit."));

        Set<VendorCategory> categories = resolveCategories(request.categoryCodes());
        Set<County> counties = resolveCounties(request.countyCodes());
        Set<EventType> eventTypes = resolveEventTypes(request.eventTypeCodes());

        // Generare slug unic
        String primaryCountyCode = counties.stream()
                .findFirst()
                .map(County::getCode)
                .orElse("ro");
        String slug = generateUniqueSlug(request.businessName(), primaryCountyCode);

        VendorProfile vendor = VendorProfile.builder()
                .user(user)
                .slug(slug)
                .businessName(request.businessName().strip())
                .phone(request.phone())
                .description(request.description())
                .websiteUrl(request.websiteUrl())
                .instagramUrl(request.instagramUrl())
                .facebookUrl(request.facebookUrl())
                .priceFrom(request.priceFrom())
                .priceTo(request.priceTo())
                .priceCurrency(request.priceCurrency() != null
                        ? request.priceCurrency() : "RON")
                .categories(categories)
                .counties(counties)
                .eventTypes(eventTypes)
                .build();

        vendorProfileRepository.save(vendor);
        log.info("Profil furnizor creat: vendorId={} userId={}",
                vendor.getId(), userId);
        return toResponse(vendor);
    }

    @Transactional
    public VendorProfileResponse updateProfile(
            UUID userId, UpdateVendorProfileRequest request) {

        VendorProfile vendor = findVendorByUserId(userId);

        if (request.businessName() != null) {
            vendor.setBusinessName(request.businessName().strip());
        }
        if (request.phone() != null) vendor.setPhone(request.phone());
        if (request.description() != null) {
            vendor.setDescription(request.description());
        }
        if (request.websiteUrl() != null) {
            vendor.setWebsiteUrl(request.websiteUrl());
        }
        if (request.instagramUrl() != null) {
            vendor.setInstagramUrl(request.instagramUrl());
        }
        if (request.facebookUrl() != null) {
            vendor.setFacebookUrl(request.facebookUrl());
        }
        if (request.priceFrom() != null) vendor.setPriceFrom(request.priceFrom());
        if (request.priceTo() != null) vendor.setPriceTo(request.priceTo());
        if (request.priceCurrency() != null) {
            vendor.setPriceCurrency(request.priceCurrency());
        }
        if (request.categoryCodes() != null && !request.categoryCodes().isEmpty()) {
            vendor.setCategories(resolveCategories(request.categoryCodes()));
        }
        if (request.countyCodes() != null && !request.countyCodes().isEmpty()) {
            vendor.setCounties(resolveCounties(request.countyCodes()));
        }
        if (request.eventTypeCodes() != null) {
            vendor.setEventTypes(resolveEventTypes(request.eventTypeCodes()));
        }

        vendorProfileRepository.save(vendor);
        log.info("Profil furnizor actualizat: vendorId={}", vendor.getId());
        return toResponse(vendor);
    }

    // ─────────────────────────────────────────────────────────
    // VENDOR — Foto
    // ─────────────────────────────────────────────────────────

    @Transactional
    public VendorProfileResponse.PhotoInfo uploadPhoto(
            UUID userId, MultipartFile file, boolean isCover) {

        VendorProfile vendor = findVendorByUserId(userId);
        int currentPhotoCount = photoRepository.countByVendorId(vendor.getId());
        int maxPhotos = getMaxPhotos(vendor);

        if (currentPhotoCount >= maxPhotos) {
            throw new UnauthorizedException(
                    "Ai atins limita de fotografii pentru planul tău (" +
                            maxPhotos + " fotografii).");
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "eventra/vendors/" + vendor.getId(),
                            "transformation", "q_auto,f_auto"
                    )
            );

            String url = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            // Dacă e cover, resetează celelalte cover-uri
            if (isCover) {
                photoRepository.findByVendorIdOrderBySortOrderAsc(vendor.getId())
                        .forEach(p -> {
                            if (p.isCover()) {
                                p.setCover(false);
                                photoRepository.save(p);
                            }
                        });
            }

            int nextOrder = currentPhotoCount;
            VendorPhoto photo = VendorPhoto.builder()
                    .vendor(vendor)
                    .cloudinaryId(publicId)
                    .url(url)
                    .isCover(isCover || currentPhotoCount == 0)
                    .sortOrder(nextOrder)
                    .build();

            photoRepository.save(photo);
            log.info("Foto upload reușit: vendorId={} publicId={}",
                    vendor.getId(), publicId);

            return new VendorProfileResponse.PhotoInfo(
                    photo.getId(), photo.getUrl(),
                    photo.getThumbnailUrl(), photo.isCover(),
                    photo.getSortOrder());

        } catch (IOException ex) {
            log.error("Eroare upload foto Cloudinary: {}", ex.getMessage());
            throw new RuntimeException("Eroare la încărcarea fotografiei.");
        }
    }

    @Transactional
    public void deletePhoto(UUID userId, UUID photoId) {
        VendorProfile vendor = findVendorByUserId(userId);

        VendorPhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fotografia nu a fost găsită."));

        if (!photo.getVendor().getId().equals(vendor.getId())) {
            throw new UnauthorizedException(
                    "Nu ai permisiunea să ștergi această fotografie.");
        }

        try {
            cloudinary.uploader().destroy(
                    photo.getCloudinaryId(), ObjectUtils.emptyMap());
        } catch (IOException ex) {
            log.warn("Nu s-a putut șterge din Cloudinary: {}", ex.getMessage());
        }

        photoRepository.delete(photo);
        log.info("Foto ștearsă: photoId={} vendorId={}",
                photoId, vendor.getId());
    }

    // ─────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────

    private VendorProfile findVendorById(UUID vendorId) {
        return vendorProfileRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Furnizorul nu a fost găsit."));
    }

    private VendorProfile findVendorByUserId(UUID userId) {
        return vendorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nu ai un profil de furnizor creat."));
    }

    private Set<VendorCategory> resolveCategories(List<String> codes) {
        if (codes == null || codes.isEmpty()) return new HashSet<>();
        return codes.stream()
                .map(code -> categoryRepository.findByCode(code)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Categoria '" + code + "' nu există.")))
                .collect(Collectors.toSet());
    }

    private Set<County> resolveCounties(List<String> codes) {
        if (codes == null || codes.isEmpty()) return new HashSet<>();
        return codes.stream()
                .map(code -> countyRepository.findByCode(code)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Județul '" + code + "' nu există.")))
                .collect(Collectors.toSet());
    }

    private Set<EventType> resolveEventTypes(List<String> codes) {
        if (codes == null || codes.isEmpty()) return new HashSet<>();
        return codes.stream()
                .map(code -> eventTypeRepository.findByCode(code)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Tipul de eveniment '" + code + "' nu există.")))
                .collect(Collectors.toSet());
    }

    private String generateUniqueSlug(String businessName, String countyCode) {
        String base = SlugUtils.generate(businessName, countyCode);
        String candidate = base;
        int attempt = 0;

        while (vendorProfileRepository.findBySlug(candidate).isPresent()) {
            attempt++;
            candidate = SlugUtils.makeUnique(base, attempt);
        }

        return candidate;
    }

    private int getMaxPhotos(VendorProfile vendor) {
        return switch (vendor.getSubscriptionPlan()) {
            case STANDARD -> MAX_PHOTOS_STANDARD;
            case PREMIUM -> MAX_PHOTOS_PREMIUM;
            default -> MAX_PHOTOS_FREE;
        };
    }

    private void incrementViewCount(VendorProfile vendor) {
        vendor.setViewCount(vendor.getViewCount() + 1);
        vendorProfileRepository.save(vendor);
    }

    private VendorProfileResponse toResponse(VendorProfile v) {
        List<VendorProfileResponse.CategoryInfo> cats = v.getCategories()
                .stream()
                .map(c -> new VendorProfileResponse.CategoryInfo(
                        c.getId(), c.getCode(), c.getName()))
                .toList();

        List<VendorProfileResponse.CountyInfo> counties = v.getCounties()
                .stream()
                .map(c -> new VendorProfileResponse.CountyInfo(
                        c.getId(), c.getCode(), c.getName()))
                .toList();

        List<VendorProfileResponse.EventTypeInfo> eventTypes = v.getEventTypes()
                .stream()
                .map(e -> new VendorProfileResponse.EventTypeInfo(
                        e.getId(), e.getCode(), e.getName(), e.getIcon()))
                .toList();

        List<VendorProfileResponse.PhotoInfo> photos = v.getPhotos()
                .stream()
                .sorted(Comparator.comparingInt(VendorPhoto::getSortOrder))
                .map(p -> new VendorProfileResponse.PhotoInfo(
                        p.getId(), p.getUrl(), p.getThumbnailUrl(),
                        p.isCover(), p.getSortOrder()))
                .toList();

        return new VendorProfileResponse(
                v.getId(), v.getUser().getId(), v.getSlug(),
                v.getBusinessName(), v.getDescription(), v.getPhone(),
                v.getWebsiteUrl(), v.getInstagramUrl(), v.getFacebookUrl(),
                v.getPriceFrom(), v.getPriceTo(), v.getPriceCurrency(),
                v.getStatus(), v.getSubscriptionPlan(),
                v.getAverageRating(), v.getReviewCount(),
                v.getViewCount(), v.getContactCount(), v.isFeatured(),
                cats, counties, eventTypes, photos, v.getCreatedAt()
        );
    }
}