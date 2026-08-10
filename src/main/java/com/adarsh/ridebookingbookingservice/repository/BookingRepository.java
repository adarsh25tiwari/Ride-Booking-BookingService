package com.adarsh.ridebookingbookingservice.repository;

import com.adarsh.RideBooking_EntityService.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
}
