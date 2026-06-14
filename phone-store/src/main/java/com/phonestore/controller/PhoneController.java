package com.phonestore.controller;

import com.phonestore.dto.PhoneDto;
import com.phonestore.service.PhoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/phones")
@Tag(name = "Phones", description = "Phone management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PhoneController {

    private final PhoneService phoneService;

    public PhoneController(PhoneService phoneService) {
        this.phoneService = phoneService;
    }

    @GetMapping
    @Operation(summary = "Get all phones", description = "Retrieve a list of all available phones")
    @ApiResponse(responseCode = "200", description = "List of phones returned")
    public ResponseEntity<List<PhoneDto>> getAllPhones() {
        List<PhoneDto> phones = phoneService.findAll();
        return ResponseEntity.ok(phones);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get phone by ID", description = "Retrieve a single phone by its identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Phone found"),
            @ApiResponse(responseCode = "404", description = "Phone not found")
    })
    public ResponseEntity<PhoneDto> getPhoneById(@PathVariable Long id) {
        PhoneDto phone = phoneService.findById(id);
        return ResponseEntity.ok(phone);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a phone", description = "Add a new phone to the store (ADMIN only)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Phone created"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<PhoneDto> createPhone(@Valid @RequestBody PhoneDto phoneDto) {
        PhoneDto saved = phoneService.create(phoneDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a phone", description = "Update an existing phone by ID (ADMIN only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Phone updated"),
            @ApiResponse(responseCode = "404", description = "Phone not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<PhoneDto> updatePhone(@PathVariable Long id, @Valid @RequestBody PhoneDto phoneDto) {
        PhoneDto updated = phoneService.update(id, phoneDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a phone", description = "Remove a phone from the store (ADMIN only)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Phone deleted"),
            @ApiResponse(responseCode = "404", description = "Phone not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Void> deletePhone(@PathVariable Long id) {
        phoneService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
