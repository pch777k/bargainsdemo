package com.pch777.bargainsdemo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pch777.bargainsdemo.model.Activity;
import com.pch777.bargainsdemo.model.User;


@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

	Page<Activity> findByUser(Pageable pageable, User user);

	void deleteActivitiesByBargainId(Long bargainId);

	List<Activity> findByBargainId(Long bargainId);
}
