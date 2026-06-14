package com.phonestore.service;

import com.phonestore.dto.PhoneDto;
import com.phonestore.entity.Phone;
import com.phonestore.exception.PhoneNotFoundException;
import com.phonestore.repository.PhoneRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PhoneServiceImpl implements PhoneService {

    private final PhoneRepository phoneRepository;

    public PhoneServiceImpl(PhoneRepository phoneRepository) {
        this.phoneRepository = phoneRepository;
    }

    @Override
    @Cacheable(value = "phones", unless = "#result == null || #result.isEmpty()")
    public List<PhoneDto> findAll() {
        return phoneRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "phone", key = "#id", unless = "#result == null")
    public PhoneDto findById(Long id) {
        Phone phone = phoneRepository.findById(id)
                .orElseThrow(() -> new PhoneNotFoundException(id));
        return toDto(phone);
    }

    @Override
    @CacheEvict(value = "phones", allEntries = true)
    public PhoneDto create(PhoneDto phoneDto) {
        Phone phone = toEntity(phoneDto);
        Phone saved = phoneRepository.save(phone);
        return toDto(saved);
    }

    @Override
    @CachePut(value = "phone", key = "#id")
    @CacheEvict(value = "phones", allEntries = true)
    public PhoneDto update(Long id, PhoneDto phoneDto) {
        Phone existing = phoneRepository.findById(id)
                .orElseThrow(() -> new PhoneNotFoundException(id));
        existing.setBrand(phoneDto.getBrand());
        existing.setModel(phoneDto.getModel());
        existing.setPrice(phoneDto.getPrice());
        existing.setStorageGb(phoneDto.getStorageGb());
        existing.setRamGb(phoneDto.getRamGb());
        existing.setColor(phoneDto.getColor());
        existing.setScreenSize(phoneDto.getScreenSize());
        existing.setBatteryCapacityMah(phoneDto.getBatteryCapacityMah());
        existing.setProcessor(phoneDto.getProcessor());
        existing.setDescription(phoneDto.getDescription());
        existing.setImageUrl(phoneDto.getImageUrl());
        existing.setStockQuantity(phoneDto.getStockQuantity());
        Phone saved = phoneRepository.save(existing);
        return toDto(saved);
    }

    @Override
    @CacheEvict(value = {"phones", "phone"}, allEntries = true)
    public void delete(Long id) {
        Phone phone = phoneRepository.findById(id)
                .orElseThrow(() -> new PhoneNotFoundException(id));
        phoneRepository.delete(phone);
    }

    private PhoneDto toDto(Phone phone) {
        PhoneDto dto = new PhoneDto();
        dto.setId(phone.getId());
        dto.setBrand(phone.getBrand());
        dto.setModel(phone.getModel());
        dto.setPrice(phone.getPrice());
        dto.setStorageGb(phone.getStorageGb());
        dto.setRamGb(phone.getRamGb());
        dto.setColor(phone.getColor());
        dto.setScreenSize(phone.getScreenSize());
        dto.setBatteryCapacityMah(phone.getBatteryCapacityMah());
        dto.setProcessor(phone.getProcessor());
        dto.setDescription(phone.getDescription());
        dto.setImageUrl(phone.getImageUrl());
        dto.setStockQuantity(phone.getStockQuantity());
        dto.setCreatedAt(phone.getCreatedAt());
        dto.setUpdatedAt(phone.getUpdatedAt());
        return dto;
    }

    private Phone toEntity(PhoneDto dto) {
        Phone phone = new Phone();
        phone.setBrand(dto.getBrand());
        phone.setModel(dto.getModel());
        phone.setPrice(dto.getPrice());
        phone.setStorageGb(dto.getStorageGb());
        phone.setRamGb(dto.getRamGb());
        phone.setColor(dto.getColor());
        phone.setScreenSize(dto.getScreenSize());
        phone.setBatteryCapacityMah(dto.getBatteryCapacityMah());
        phone.setProcessor(dto.getProcessor());
        phone.setDescription(dto.getDescription());
        phone.setImageUrl(dto.getImageUrl());
        phone.setStockQuantity(dto.getStockQuantity());
        return phone;
    }
}
