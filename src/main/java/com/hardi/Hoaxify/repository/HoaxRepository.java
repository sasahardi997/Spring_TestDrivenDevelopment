package com.hardi.Hoaxify.repository;

import com.hardi.Hoaxify.domain.Hoax;
import com.hardi.Hoaxify.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoaxRepository extends JpaRepository<Hoax, Long> {

    Page<Hoax> findByUser(User user, Pageable pageable);

    Page<Hoax> findByUserUsername(String username, Pageable pageable);

    Page<Hoax> findByIdLessThan(long id, Pageable pageable);

    List<Hoax> findByIdGreaterThan(long id, Sort sort);

    Page<Hoax> findByIdLessThanAndUser(long id, User user, Pageable pageable);

    List<Hoax> findByIdGreaterThanAndUser(long id, User user, Sort sort);

    long countByIdGreaterThan(long id);

    long countByIdGreaterThanAndUser(long id, User user);
}
