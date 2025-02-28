package com.drive.flashbox.repository;

import com.drive.flashbox.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u JOIN BoxUser bu ON u.id = bu.user.id WHERE bu.box.id = :boxId")
    List<User> findUsersByBoxId(@Param("boxId") Long boxId);
}
