package com.adarsh.ridebookingbookingservice.repository;

import com.adarsh.RideBooking_EntityService.models.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverRepository extends JpaRepository<Driver,Long> {
}
