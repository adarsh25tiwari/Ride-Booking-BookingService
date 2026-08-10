package com.adarsh.ridebookingbookingservice.repository;

import com.adarsh.RideBooking_EntityService.models.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {
}
