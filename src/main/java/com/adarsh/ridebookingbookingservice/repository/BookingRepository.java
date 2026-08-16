package com.adarsh.ridebookingbookingservice.repository;

import com.adarsh.RideBooking_EntityService.models.Booking;
import com.adarsh.RideBooking_EntityService.models.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("UPDATE Booking b SET b.bookingStatus =:bookingStatus, b.driver =:driver WHERE b.id =:bookingId")
    void updateBookingStatusAndDriverById(@Param("bookingId") Long bookingId,
                          @Param("bookingStatus") String bookingStatus,
                          @Param("driver") Driver driver);
}
