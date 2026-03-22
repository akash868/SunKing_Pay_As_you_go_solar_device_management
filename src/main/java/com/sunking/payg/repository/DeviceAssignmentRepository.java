package com.sunking.payg.repository;

import com.sunking.payg.entity.DeviceAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceAssignmentRepository extends JpaRepository<DeviceAssignment, UUID> {

    Optional<DeviceAssignment> findByDeviceIdAndIsActiveTrue(UUID deviceId);

    List<DeviceAssignment> findByCustomerIdAndIsActiveTrue(UUID customerId);

    Optional<DeviceAssignment> findByIdAndCustomerIdAndIsActiveTrue(UUID id, UUID customerId);

    boolean existsByDeviceIdAndIsActiveTrue(UUID deviceId);

    /**
     * Fetch overdue assignments (not fully paid, active, and next payment due date exceeded grace period).
     * Uses a join fetch to avoid N+1 when we need device details.
     */
    @Query("SELECT da FROM DeviceAssignment da " +
           "JOIN FETCH da.device d " +
           "JOIN FETCH da.customer c " +
           "WHERE da.isActive = true " +
           "AND da.isFullyPaid = false " +
           "AND da.nextPaymentDueDate < :overdueThreshold")
    List<DeviceAssignment> findOverdueAssignments(@Param("overdueThreshold") LocalDate overdueThreshold);

    @Query("SELECT da FROM DeviceAssignment da " +
           "JOIN FETCH da.device d " +
           "WHERE da.isActive = true " +
           "AND da.isFullyPaid = false " +
           "AND d.status = 'LOCKED' " +
           "AND da.lastPaymentDate IS NOT NULL " +
           "AND da.nextPaymentDueDate >= :today")
    List<DeviceAssignment> findAssignmentsToReactivate(@Param("today") LocalDate today);

    @Modifying
    @Query("UPDATE DeviceAssignment da SET da.daysOverdue = :daysOverdue WHERE da.id = :id")
    void updateDaysOverdue(@Param("id") UUID id, @Param("daysOverdue") int daysOverdue);
}
