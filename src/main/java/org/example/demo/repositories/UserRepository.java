package org.example.demo.repositories;

import org.example.demo.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
  @Query(value = "select u.* from User as u where (:email = '' or :email is null or (u.email = :email))", nativeQuery = true)
  User showUsername(@Param("email") String email);
  User findByEmail(String email);
}