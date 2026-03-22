package com.sunking.payg.controller;

import com.sunking.payg.dto.request.AssignDeviceRequest;
import com.sunking.payg.dto.request.CreateDeviceRequest;
import com.sunking.payg.dto.response.ApiResponse;
import com.sunking.payg.dto.response.DeviceResponse;
import com.sunking.payg.dto.response.DeviceStatusResponse;
import com.sunking.payg.entity.Device;
import com.sunking.payg.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@Tag(name = "Devices", description = "Solar device management endpoints")
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    @Operation(summary = "Register device", description = "Register a new solar device in the system (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DeviceResponse>> registerDevice(
            @Valid @RequestBody CreateDeviceRequest request) {
        DeviceResponse response = deviceService.registerDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Device registered successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get device details")
    public ResponseEntity<ApiResponse<DeviceResponse>> getDevice(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(deviceService.getDevice(id)));
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign device to customer", description = "Assign a device to a customer. Device must be INACTIVE.")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<ApiResponse<DeviceStatusResponse>> assignDevice(
            @PathVariable UUID id,
            @Valid @RequestBody AssignDeviceRequest request) {
        DeviceStatusResponse response = deviceService.assignDevice(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Device assigned successfully", response));
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Get device PAYG status", description = "Returns device status, payment progress, overdue info, and assignment details")
    public ResponseEntity<ApiResponse<DeviceStatusResponse>> getDeviceStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(deviceService.getDeviceStatus(id)));
    }

    @GetMapping
    @Operation(summary = "List devices", description = "Paginated list of devices, optionally filtered by status")
    public ResponseEntity<ApiResponse<List<DeviceResponse>>> listDevices(
            @Parameter(description = "Filter by status: ACTIVE, INACTIVE, LOCKED") @RequestParam(required = false) Device.DeviceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DeviceResponse> result = deviceService.listDevices(status, pageable);
        return ResponseEntity.ok(ApiResponse.paginated(result));
    }
}
