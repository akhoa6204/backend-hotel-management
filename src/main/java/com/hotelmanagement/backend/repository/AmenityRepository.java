package com.hotelmanagement.backend.repository;

import com.hotelmanagement.backend.entity.Amenity;
import com.hotelmanagement.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity,String> {
    boolean existsById(String id);
}
