package com.adarsh.ridebookingbookingservice.repository;

import com.adarsh.RideBooking_EntityService.models.Booking;
import com.adarsh.RideBooking_EntityService.models.BookingStatus;
import com.adarsh.RideBooking_EntityService.models.Driver;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Transactional
    @Query("""
       UPDATE Booking b
       SET b.bookingStatus = :bookingStatus,
           b.driver = :driver
       WHERE b.id = :bookingId
       """)
    void updateBookingStatusAndDriverById(
            @Param("bookingId") Long bookingId,
            @Param("bookingStatus") BookingStatus bookingStatus,
            @Param("driver") Driver driver
    );


    //Atomic driver assignment for internal-booking (socket)
    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Transactional
    @Query("""
        UPDATE Booking b
        SET b.bookingStatus = :newStatus,
            b.driver = :driver
        WHERE b.id = :bookingId
          AND b.bookingStatus = :currentStatus
          AND b.driver IS NULL
        """)
    int assignDriverToBooking(
            @Param("bookingId") Long bookingId,
            @Param("currentStatus") BookingStatus currentStatus,
            @Param("newStatus") BookingStatus newStatus,
            @Param("driver") Driver driver
    );
}
