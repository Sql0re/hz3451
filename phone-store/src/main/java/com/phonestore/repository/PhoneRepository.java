package com.phonestore.repository;

import com.phonestore.entity.Phone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhoneRepository extends JpaRepository<Phone, Long> {
    List<Phone> findByBrandContainingIgnoreCase(String brand);
    List<Phone> findByModelContainingIgnoreCase(String model);
    List<Phone> findByBrandContainingIgnoreCaseOrModelContainingIgnoreCase(String brand, String model);
}
