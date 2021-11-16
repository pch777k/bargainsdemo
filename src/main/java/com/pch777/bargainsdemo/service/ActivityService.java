package com.pch777.bargainsdemo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.pch777.bargainsdemo.exception.ResourceNotFoundException;
import com.pch777.bargainsdemo.model.Activity;
import com.pch777.bargainsdemo.model.ActivityType;
import com.pch777.bargainsdemo.model.Bargain;
import com.pch777.bargainsdemo.model.User;
import com.pch777.bargainsdemo.repository.ActivityRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ActivityService {

	private ActivityRepository activityRepository;
	
	public void addActivity(User user, LocalDateTime createdAt, Bargain bargain, ActivityType activityType) {
		Activity activity = new Activity();
		activity.setUser(user);
		activity.setCreatedAt(createdAt);
		activity.setBargain(bargain);
		activity.setActivityType(activityType);
		activityRepository.save(activity);
	}
	
	public Page<Activity> getActivitiesByUser(Pageable pageable, User user) {
		return activityRepository.findByUser(pageable, user);
	}
	
	public List<Activity> getActivitiesByBargain(Long bargainId) {
		return activityRepository.findByBargainId(bargainId);
	}

	public void deleteActivitiesByBargainId(Long bargainId) {
		activityRepository.deleteActivitiesByBargainId(bargainId);		
	}
	
	public void deleteActivityById(Long id) throws ResourceNotFoundException {
		if (!existsById(id)) {
			throw new ResourceNotFoundException("Cannot find activity with id: " + id);
		}
		activityRepository.deleteById(id);		
	}

	public List<Activity> getAllActivities() {
		return activityRepository.findAll();
	}

	public Optional<Activity> getActivityById(Long id) {
		return activityRepository.findById(id);
	}
		
	public boolean existsById(Long id) {
		return activityRepository.existsById(id);
	}

	public void editActivityType(ActivityType activityType, Long id) {
		Activity activity = activityRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Not found an activity with id: " + id));
		activity.setActivityType(activityType);
		activityRepository.save(activity);		
	}

	public void editActivity(Activity activity) {
		activityRepository.save(activity);
	}

	public void addActivity(Activity activity) {
		activityRepository.save(activity);		
	}

	
}
