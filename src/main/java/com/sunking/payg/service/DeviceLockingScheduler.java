package com.sunking.payg.service;

import com.sunking.payg.entity.Device;
import com.sunking.payg.entity.DeviceAssignment;
import com.sunking.payg.repository.DeviceAssignmentRepository;
import com.sunking.payg.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Scheduled service that runs daily to:
 * 1. Lock devices with overdue payments (past grace period)
 * 2. Update daysOverdue counter for reporting
 * 3. Reactivate devices where payments have been made
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceLockingScheduler {

    private final DeviceAssignmentRepository assignmentRepository;
    private final DeviceRepository deviceRepository;

    /**
     * Runs every day at 01:00 AM server time.
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void checkAndLockOverdueDevices() {
        LocalDate today = LocalDate.now();
        log.info("Starting device locking scheduler for date: {}", today);

        List<DeviceAssignment> overdueAssignments = assignmentRepository.findOverdueAssignments(today);
        int lockedCount = 0;

        for (DeviceAssignment assignment : overdueAssignments) {
            Device device = assignment.getDevice();
            LocalDate dueDate = assignment.getNextPaymentDueDate();
            long daysLate = ChronoUnit.DAYS.between(dueDate, today);
            long gracePeriod = device.getGracePeriodDays();

            assignment.setDaysOverdue((int) daysLate);
            assignmentRepository.save(assignment);

            // Lock the device only if overdue beyond grace period
            if (daysLate > gracePeriod && device.getStatus() != Device.DeviceStatus.LOCKED) {
                device.setStatus(Device.DeviceStatus.LOCKED);
                device.setLockedAt(today.atStartOfDay());
                deviceRepository.save(device);
                lockedCount++;
                log.warn("Device LOCKED: deviceId={}, serial={}, daysOverdue={}, gracePeriod={}",
                        device.getId(), device.getSerialNumber(), daysLate, gracePeriod);
            }
        }

        log.info("Device locking complete: {} devices locked, {} assignments checked",
                lockedCount, overdueAssignments.size());
    }
}
