package com.garage.service;

import com.garage.dto.*;
import com.garage.entity.HangXe;
import com.garage.entity.ModelXe;
import com.garage.exception.BadRequestException;
import com.garage.exception.DuplicateResourceException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.HangXeRepository;
import com.garage.repository.ModelXeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleBrandModelService {

    private final HangXeRepository hangXeRepository;
    private final ModelXeRepository modelXeRepository;

    public VehicleBrandModelService(HangXeRepository hangXeRepository, ModelXeRepository modelXeRepository) {
        this.hangXeRepository = hangXeRepository;
        this.modelXeRepository = modelXeRepository;
    }

    /**
     * Lấy danh sách tất cả các Hãng xe đang hoạt động
     */
    @Transactional(readOnly = true)
    public List<BrandResponse> getAllActiveBrands() {
        return hangXeRepository.findByTrangThaiTrue().stream()
                .map(this::mapToBrandResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách tất cả các Hãng xe (cho quản trị viên)
     */
    @Transactional(readOnly = true)
    public List<BrandResponse> getAllBrands() {
        return hangXeRepository.findAll().stream()
                .map(this::mapToBrandResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết Hãng xe theo ID
     */
    @Transactional(readOnly = true)
    public BrandResponse getBrandById(Integer brandId) {
        HangXe brand = hangXeRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hãng xe với ID: " + brandId));
        return mapToBrandResponse(brand);
    }

    /**
     * Tạo Hãng xe mới (SYSTEM_ADMIN)
     */
    @Transactional
    public BrandResponse createBrand(CreateBrandRequest request) {
        String brandName = request.getTenHangXe().trim();
        if (hangXeRepository.existsByTenHangXeIgnoreCase(brandName)) {
            throw new DuplicateResourceException("Hãng xe '" + brandName + "' đã tồn tại trong hệ thống");
        }
        HangXe brand = new HangXe(brandName);
        HangXe saved = hangXeRepository.save(brand);
        return mapToBrandResponse(saved);
    }

    /**
     * Lấy danh sách Model thuộc về Hãng xe (Cascading Dropdown)
     */
    @Transactional(readOnly = true)
    public List<ModelResponse> getModelsByBrand(Integer brandId) {
        if (!hangXeRepository.existsById(brandId)) {
            throw new ResourceNotFoundException("Không tìm thấy hãng xe với ID: " + brandId);
        }
        return modelXeRepository.findByHangXeMaHangXeAndTrangThaiTrue(brandId).stream()
                .map(this::mapToModelResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tạo Model xe mới thuộc Hãng xe (SYSTEM_ADMIN)
     */
    @Transactional
    public ModelResponse createModel(Integer brandId, CreateModelRequest request) {
        HangXe brand = hangXeRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hãng xe với ID: " + brandId));

        String modelName = request.getTenModel().trim();
        if (modelXeRepository.existsByHangXeMaHangXeAndTenModelIgnoreCase(brandId, modelName)) {
            throw new DuplicateResourceException("Model xe '" + modelName + "' đã tồn tại cho hãng " + brand.getTenHangXe());
        }

        ModelXe model = new ModelXe(brand, modelName);
        ModelXe saved = modelXeRepository.save(model);
        return mapToModelResponse(saved);
    }

    // --- Helpers ---
    private BrandResponse mapToBrandResponse(HangXe brand) {
        return new BrandResponse(brand.getMaHangXe(), brand.getTenHangXe(), brand.getTrangThai());
    }

    private ModelResponse mapToModelResponse(ModelXe model) {
        Integer brandId = model.getHangXe() != null ? model.getHangXe().getMaHangXe() : null;
        String brandName = model.getHangXe() != null ? model.getHangXe().getTenHangXe() : null;
        return new ModelResponse(model.getMaModel(), brandId, brandName, model.getTenModel(), model.getTrangThai());
    }
}
