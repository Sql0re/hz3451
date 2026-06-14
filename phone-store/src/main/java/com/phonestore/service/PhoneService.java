package com.phonestore.service;

import com.phonestore.dto.PhoneDto;

import java.util.List;

public interface PhoneService {
    List<PhoneDto> findAll();
    PhoneDto findById(Long id);
    PhoneDto create(PhoneDto phoneDto);
    PhoneDto update(Long id, PhoneDto phoneDto);
    void delete(Long id);
}
