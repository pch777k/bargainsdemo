package com.pch777.bargainsdemo.web;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.pch777.bargainsdemo.exception.ResourceNotFoundException;
import com.pch777.bargainsdemo.model.Activity;
import com.pch777.bargainsdemo.model.ActivityType;
import com.pch777.bargainsdemo.service.ActivityService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/activities")
public class ActivityRestController {

	private ActivityService activityService;

	@GetMapping
	public List<Activity> getAllActivities() {
		return activityService.getAllActivities();
	}

	@GetMapping("/{id}")
	public ResponseEntity<Activity> getActivityById(@PathVariable Long id) {
		return activityService.getActivityById(id).map(activity -> ResponseEntity.ok(activity))
				.orElse(ResponseEntity.notFound().build());
	}
	
	@PostMapping()
	public ResponseEntity<Void> addActivity(@RequestBody Activity activity) {

		activityService.addActivity(activity);
		URI uri = ServletUriComponentsBuilder
				.fromCurrentRequestUri()
				.path("/" + activity.getId().toString())
				.build()
				.toUri();
		return ResponseEntity.created(uri).build();
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Void> updateActivity(@RequestBody Activity activity, @PathVariable Long id) {

		if (activityService.existsById(id)) {
			activity.setId(id);
			activityService.editActivity(activity);
			return ResponseEntity.ok().build();
		}

		return ResponseEntity.notFound().build();
	}
	
	@PatchMapping("/{id}")
	public ResponseEntity<Void> updateActivityType(@RequestBody ActivityType activityType, @PathVariable Long id) {

		if (activityService.existsById(id)) {
			activityService.editActivityType(activityType, id);
			return ResponseEntity.ok().build();
		}

		return ResponseEntity.notFound().build();
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteActivityById(@PathVariable Long id) {
		try {
			activityService.deleteActivityById(id);
			return ResponseEntity.noContent().build();
		} catch (ResourceNotFoundException ex) {
			return ResponseEntity.notFound().build();
		}
	}
}
