package com.sunking.payg.repository;

import com.sunking.payg.entity.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {

    Optional<Device> findBySerialNumber(String serialNumber);

    boolean existsBySerialNumber(String serialNumber);

    Page<Device> findByStatus(Device.DeviceStatus status, Pageable pageable);

    @Query("SELECT d FROM Device d WHERE d.status = 'INACTIVE' AND d.id NOT IN " +
           "(SELECT da.device.id FROM DeviceAssignment da WHERE da.isActive = true)")
    List<Device> findUnassignedDevices();

    @Modifying
    @Query("UPDATE Device d SET d.status = :status WHERE d.id = :id")
    void updateStatus(@Param("id") UUID id, @Param("status") Device.DeviceStatus status);
}
