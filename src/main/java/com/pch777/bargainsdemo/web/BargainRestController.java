package com.pch777.bargainsdemo.web;

import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.pch777.bargainsdemo.exception.ResourceNotFoundException;
import com.pch777.bargainsdemo.model.ActivityType;
import com.pch777.bargainsdemo.model.Bargain;
import com.pch777.bargainsdemo.model.Category;
import com.pch777.bargainsdemo.model.User;
//import com.pch777.bargainsdemo.security.UserSecurity;
import com.pch777.bargainsdemo.service.ActivityService;
import com.pch777.bargainsdemo.service.BargainService;
import com.pch777.bargainsdemo.service.UserService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class BargainRestController {

	private BargainService bargainService;
	private UserService userService;
//	private UserSecurity userSecurity;
	private ActivityService activityService;

	@GetMapping("/bargains")
	public ResponseEntity<Map<String, Object>> getBargains( 
		@RequestParam(defaultValue = "") String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        
        try {
		      List<Bargain> bargains = new ArrayList<>();
		      
		      Pageable pageable = PageRequest.of(page, size);
		      Page<Bargain> pageBargains = bargainService.getAllBargainsByTitleLike(pageable, keyword);
		      
		      bargains = pageBargains.getContent();
		
		      Map<String, Object> response = new HashMap<>();
		      response.put("bargains", bargains);
		      response.put("currentPage", pageBargains.getNumber());
		      response.put("totalItems", pageBargains.getTotalElements());
		      response.put("totalPages", pageBargains.getTotalPages());

		      return new ResponseEntity<>(response, HttpStatus.OK);
        	} catch (Exception e) {
        		return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        	}
        
	} 
	
	@GetMapping("users/{userId}/bargains")
	public ResponseEntity<Map<String, Object>> getBargainsByTitleLikeByUserId(@PathVariable Long userId, 
		@RequestParam(defaultValue = "") String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        
        try {
		      List<Bargain> bargains = new ArrayList<>();
		      
		      Pageable pageable = PageRequest.of(page, size);
		      Page<Bargain> pageBargains = bargainService.getAllBargainsByTitleLikeByUserId(pageable, keyword, userId);
		      
		      bargains = pageBargains.getContent();
		
		      Map<String, Object> response = new HashMap<>();
		      response.put("bargains", bargains);
		      response.put("currentPage", pageBargains.getNumber());
		      response.put("totalItems", pageBargains.getTotalElements());
		      response.put("totalPages", pageBargains.getTotalPages());

		      return new ResponseEntity<>(response, HttpStatus.OK);
        	} catch (Exception e) {
        		return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        	}
        
	} 
	
	@GetMapping("/bargains/{bargainCategory}")
	public ResponseEntity<Map<String, Object>> getBargainsByTitleLikeByUserId(@PathVariable Category category, 
		@RequestParam(defaultValue = "") String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        
        try {
		      List<Bargain> bargains = new ArrayList<>();
		      
		      Pageable pageable = PageRequest.of(page, size);
		      Page<Bargain> pageBargains = bargainService.getAllBargainsByTitleLikeByCategory(pageable, keyword, category);
		      
		      bargains = pageBargains.getContent();
		
		      Map<String, Object> response = new HashMap<>();
		      response.put("bargains", bargains);
		      response.put("currentPage", pageBargains.getNumber());
		      response.put("totalItems", pageBargains.getTotalElements());
		      response.put("totalPages", pageBargains.getTotalPages());

		      return new ResponseEntity<>(response, HttpStatus.OK);
        	} catch (Exception e) {
        		return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        	}
        
	} 

	@GetMapping("/bargains/{id}")
	public ResponseEntity<Bargain> getBargainById(@PathVariable Long id) {
		return bargainService.getById(id)
				.map(bargain -> ResponseEntity.ok(bargain))
				.orElse(ResponseEntity.notFound().build());
	}
	
	@PostMapping("/bargains")
	@Transactional
	public ResponseEntity<Void> addBargain(@Valid @RequestBody Bargain bargain) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			User user = userService.findUserByEmail(authentication.getName());
			bargain.setUser(user);
			bargainService.addBargain(bargain);
			activityService.addActivity(bargain.getUser(), bargain.getCreatedAt(), bargain, ActivityType.BARGAIN);
			URI uri = ServletUriComponentsBuilder
					.fromCurrentRequestUri()
					.path("/" + bargain.getId().toString())
					.build()
					.toUri();
			return ResponseEntity.created(uri).build();
		}
		return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	}

	@PutMapping("/bargains/{id}")
	@Transactional
	public ResponseEntity<Void> updateBargain(@Valid @RequestBody Bargain bargain, @PathVariable Long id, 
			Principal principal) throws ResourceNotFoundException {
		if (bargainService.existsById(id)) {
	//		if(userSecurity.isOwnerOrAdmin(bargainService.getBargainById(id).getUser().getEmail(), principal.getName())) {			
				
				Bargain editedBargain = bargainService.getBargainById(id);
				bargain.setVoteCount(editedBargain.getVoteCount());
				bargain.setVotes(editedBargain.getVotes());
				bargain.setComments(editedBargain.getComments());
				bargain.setActivities(editedBargain.getActivities());
				bargain.setUser(editedBargain.getUser());
				
				bargainService.editBargain(bargain, id);
				return ResponseEntity.ok().build();
	//		} else {
	//			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	//		}
		}

		return ResponseEntity.notFound().build();

	} 
	
	@PatchMapping("/bargains/{id}")
	@Transactional
	public ResponseEntity<Void> updateBargainTitle(@RequestBody String title, @PathVariable Long id, Principal principal) throws ResourceNotFoundException {

		if (bargainService.existsById(id)) {
	//		Bargain bargain = bargainService.getBargainById(id);
	//		if(userSecurity.isOwnerOrAdmin(bargain.getUser().getEmail(), principal.getName())) {
				bargainService.editBargainTitle(title, id);
				return ResponseEntity.ok().build();
	//		} else {
	//			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	//		}
		}

		return ResponseEntity.notFound().build();
	}

	@DeleteMapping("/bargains/{id}")
	@Transactional
	public ResponseEntity<Void> deleteBargainById(@PathVariable Long id, Principal principal) {
		
		try {
		//	Bargain bargain = bargainService.getBargainById(id);
		//	if(userSecurity.isOwnerOrAdmin(bargain.getUser().getEmail(), principal.getName())) {
				bargainService.deleteBargainById(id);
				return ResponseEntity.noContent().build(); 
				//}
	//		else {
	//			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	//		}
		} catch (ResourceNotFoundException ex) {
			return ResponseEntity.notFound().build();
		}
	}
	
	@GetMapping("/all/bargains")
	public List<Bargain> getAllBargains() {
		return bargainService.getAllBargains();
	}
	
}
