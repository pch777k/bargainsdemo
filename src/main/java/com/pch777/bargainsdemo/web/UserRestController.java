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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.pch777.bargainsdemo.exception.ResourceNotFoundException;
import com.pch777.bargainsdemo.model.User;
//import com.pch777.bargainsdemo.security.UserSecurity;
import com.pch777.bargainsdemo.service.UserService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class UserRestController {

	private UserService userService;
//	private UserSecurity userSecurity;
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	

	@GetMapping("/users")
	public ResponseEntity<Map<String, Object>> getUsers( 
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        
        try {
		      List<User> users = new ArrayList<>();
		      
		      Pageable pageable = PageRequest.of(page, size);
		      Page<User> pageUsers = userService.getUsers(pageable);
		      
		      users = pageUsers.getContent();
		
		      Map<String, Object> response = new HashMap<>();
		      response.put("users", users);
		      response.put("currentPage", pageUsers.getNumber());
		      response.put("totalItems", pageUsers.getTotalElements());
		      response.put("totalPages", pageUsers.getTotalPages());

		      return new ResponseEntity<>(response, HttpStatus.OK);
        	} catch (Exception e) {
        		return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        	}
        
	} 
	
	@GetMapping("/users/{id}")
	public ResponseEntity<User> getUserById(@PathVariable Long id) {
		
		if(userService.existsById(id)) {
			User user = userService.findUserById(id);
			return ResponseEntity.ok(user);
		}
		return ResponseEntity.notFound().build();
	} 
	
	@PostMapping("/users")
	public ResponseEntity<Void> addUser(@Valid @RequestBody User user) {
		if(userService.isUserPresent(user.getEmail())) {
			return ResponseEntity.badRequest().build();
			}
		userService.registerUser(user);
		URI uri = ServletUriComponentsBuilder
				.fromCurrentRequestUri()
				.path("/" + user.getId().toString())
				.build()
				.toUri();
		return ResponseEntity.created(uri).build();
	}
	
	@PostMapping("/admin")
	public ResponseEntity<Void> addAdmin(@Valid @RequestBody User admin) {
		if(userService.isUserPresent(admin.getEmail())) {
			return ResponseEntity.badRequest().build();
			}
		userService.registerAdmin(admin);
		URI uri = ServletUriComponentsBuilder
				.fromCurrentRequestUri()
				.path("/" + admin.getId().toString())
				.build()
				.toUri();
		return ResponseEntity.created(uri).build();
	}
	
	@PutMapping("users/{id}")
	public ResponseEntity<Void> updateUser(@Valid @RequestBody User user, @PathVariable Long id, Principal principal) {
		if (userService.existsById(id)) {
			User editedUser = userService.findUserById(id);
					
//			if(userSecurity.isOwnerOrAdmin(editedUser.getEmail(), principal.getName())) {
				if(userService.isUserPresent(user.getEmail()) &&  
						!(editedUser.getEmail().equalsIgnoreCase(user.getEmail())))  {
					user.setEmail(editedUser.getEmail());
				}
				user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
				user.setBargains(editedUser.getBargains());
				user.setComments(editedUser.getComments());
				user.setActivities(editedUser.getActivities());
				user.setRoles(editedUser.getRoles());
				user.setId(editedUser.getId());
				userService.editUser(user);
				return ResponseEntity.ok().build();
//				}
//			else {
//				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//			}
		}
		
		return ResponseEntity.notFound().build();	
	} 
	
	@DeleteMapping("users/{id}")
	public ResponseEntity<Void> deleteUserById(@PathVariable Long id, Principal principal) {
		
		try {
	//		if(userSecurity.isAdmin(principal.getName())) {
				userService.deleteUserById(id);
				return ResponseEntity.noContent().build();
	//		}
	//		else {
	//			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	//		}
		} catch (ResourceNotFoundException ex) {
			return ResponseEntity.notFound().build();
		}
	}
	
}
