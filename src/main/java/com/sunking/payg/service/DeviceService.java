package com.sunking.payg.service;

import com.sunking.payg.dto.request.AssignDeviceRequest;
import com.sunking.payg.dto.request.CreateDeviceRequest;
import com.sunking.payg.dto.response.DeviceResponse;
import com.sunking.payg.dto.response.DeviceStatusResponse;
import com.sunking.payg.entity.Customer;
import com.sunking.payg.entity.Device;
import com.sunking.payg.entity.DeviceAssignment;
import com.sunking.payg.exception.BusinessException;
import com.sunking.payg.exception.DuplicateResourceException;
import com.sunking.payg.exception.ResourceNotFoundException;
import com.sunking.payg.repository.DeviceAssignmentRepository;
import com.sunking.payg.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceAssignmentRepository assignmentRepository;
    private final CustomerService customerService;

    @Transactional
    public DeviceResponse registerDevice(CreateDeviceRequest request) {
        if (deviceRepository.existsBySerialNumber(request.getSerialNumber())) {
            throw new DuplicateResourceException(
                    "Device with serial number '" + request.getSerialNumber() + "' already exists");
        }

        Device device = Device.builder()
                .serialNumber(request.getSerialNumber())
                .model(request.getModel())
                .description(request.getDescription())
                .totalCost(request.getTotalCost())
                .dailyRate(request.getDailyRate())
                .gracePeriodDays(request.getGracePeriodDays())
                .status(Device.DeviceStatus.INACTIVE)
                .build();

        device = deviceRepository.save(device);
        log.info("Registered device: id={}, serial={}", device.getId(), device.getSerialNumber());
        return toResponse(device);
    }

    @Transactional
    public DeviceStatusResponse assignDevice(UUID deviceId, AssignDeviceRequest request) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));

        if (device.getStatus() == Device.DeviceStatus.LOCKED) {
            throw new BusinessException("Cannot assign a LOCKED device. Device must be INACTIVE.");
        }

        if (assignmentRepository.existsByDeviceIdAndIsActiveTrue(deviceId)) {
            throw new BusinessException("Device is already assigned to a customer. Unassign it first.");
        }

        Customer customer = customerService.getCustomerEntityById(request.getCustomerId());

        if (customer.getStatus() != Customer.CustomerStatus.ACTIVE) {
            throw new BusinessException("Cannot assign device to an inactive or blacklisted customer");
        }

        LocalDate today = LocalDate.now();
        // Next payment is due after 1 day (by default)
        LocalDate nextPaymentDue = today.plusDays(1);

        DeviceAssignment assignment = DeviceAssignment.builder()
                .device(device)
                .customer(customer)
                .assignedAt(LocalDateTime.now())
                .isActive(true)
                .totalCostAtAssignment(device.getTotalCost())
                .dailyRateAtAssignment(device.getDailyRate())
                .amountPaid(BigDecimal.ZERO)
                .nextPaymentDueDate(nextPaymentDue)
                .daysOverdue(0)
                .isFullyPaid(false)
                .build();

        assignment = assignmentRepository.save(assignment);

        // Activate device upon assignment
        device.setStatus(Device.DeviceStatus.ACTIVE);
        deviceRepository.save(device);

        log.info("Device assigned: deviceId={}, customerId={}, assignmentId={}",
                deviceId, request.getCustomerId(), assignment.getId());

        return toStatusResponse(device, assignment, customer);
    }

    public DeviceStatusResponse getDeviceStatus(UUID deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));

        return assignmentRepository.findByDeviceIdAndIsActiveTrue(deviceId)
                .map(assignment -> toStatusResponse(device, assignment, assignment.getCustomer()))
                .orElse(toUnassignedStatusResponse(device));
    }

    public DeviceResponse getDevice(UUID deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));
        return toResponse(device);
    }

    public Page<DeviceResponse> listDevices(Device.DeviceStatus status, Pageable pageable) {
        Page<Device> devices = (status != null)
                ? deviceRepository.findByStatus(status, pageable)
                : deviceRepository.findAll(pageable);
        return devices.map(this::toResponse);
    }

    private DeviceResponse toResponse(Device d) {
        return DeviceResponse.builder()
                .id(d.getId())
                .serialNumber(d.getSerialNumber())
                .model(d.getModel())
                .description(d.getDescription())
                .totalCost(d.getTotalCost())
                .dailyRate(d.getDailyRate())
                .gracePeriodDays(d.getGracePeriodDays())
                .status(d.getStatus())
                .lockedAt(d.getLockedAt())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    private DeviceStatusResponse toStatusResponse(Device device, DeviceAssignment assignment, Customer customer) {
        BigDecimal totalCost = assignment.getTotalCostAtAssignment();
        BigDecimal amountPaid = assignment.getAmountPaid();
        BigDecimal remaining = assignment.getRemainingBalance();
        BigDecimal progress = totalCost.compareTo(BigDecimal.ZERO) > 0
                ? amountPaid.divide(totalCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2,
                        RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return DeviceStatusResponse.builder()
                .deviceId(device.getId())
                .serialNumber(device.getSerialNumber())
                .model(device.getModel())
                .deviceStatus(device.getStatus())
                .assignmentId(assignment.getId())
                .customerId(customer.getId())
                .customerName(customer.getFirstName() + " " + customer.getLastName())
                .customerPhone(customer.getPhoneNumber())
                .totalCost(totalCost)
                .amountPaid(amountPaid)
                .remainingBalance(remaining)
                .progressPercent(progress)
                .nextPaymentDueDate(assignment.getNextPaymentDueDate())
                .lastPaymentDate(assignment.getLastPaymentDate())
                .daysOverdue(assignment.getDaysOverdue())
                .isFullyPaid(assignment.getIsFullyPaid())
                .fullyPaidAt(assignment.getFullyPaidAt())
                .assignedAt(assignment.getAssignedAt())
                .build();
    }

    private DeviceStatusResponse toUnassignedStatusResponse(Device device) {
        return DeviceStatusResponse.builder()
                .deviceId(device.getId())
                .serialNumber(device.getSerialNumber())
                .model(device.getModel())
                .deviceStatus(device.getStatus())
                .build();
    }
}
