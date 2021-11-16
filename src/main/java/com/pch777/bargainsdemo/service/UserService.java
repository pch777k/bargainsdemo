package com.pch777.bargainsdemo.service;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pch777.bargainsdemo.exception.ResourceNotFoundException;
import com.pch777.bargainsdemo.model.Role;
import com.pch777.bargainsdemo.model.User;
import com.pch777.bargainsdemo.repository.RoleRepository;
import com.pch777.bargainsdemo.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {
	
	private UserRepository userRepository;
	private RoleRepository roleRepository;
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Transactional
	public void registerUser(User user) {
		user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
	
		Role userRole = roleRepository.findRoleByName("USER");
	  
	    Set<Role> roles = new HashSet<>();
	    roles.add(userRole);
		user.setRoles(roles); 
		userRepository.save(user);
	}
	
	@Transactional
	public void registerAdmin(User admin) {
		admin.setPassword(bCryptPasswordEncoder.encode(admin.getPassword()));
	
		Role adminRole = roleRepository.findRoleByName("ADMIN");
	  
	    Set<Role> roles = new HashSet<>();
	    roles.add(adminRole);
		admin.setRoles(roles); 
		userRepository.save(admin);
	}
	
	public User findUserByEmail(String email) {
		
		return userRepository.getUserByEmail(email);
	}
	
	public User findUserById(Long id) {
		return userRepository.findById(id).get();
	}
	
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}
	
	public Page<User> getUsers(Pageable pageable) {
		return userRepository.findAll(pageable);
	}
	
	public boolean isUserPresent(String email) {
		if(userRepository.getUserByEmail(email)!=null) {
			return true;
		}
		return false;
	}
	
	public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }
	
	public User randomUser() {
		List<User> users = userRepository.findAll();
		Random rand = new Random();
		return users.get(rand.nextInt(users.size()));
	}
	
	public boolean isEmptyUsersBargainsList(String email) {
		return userRepository.getUserByEmail(email).getBargains().isEmpty();
	}
	
	public void editUser(User user) {
		userRepository.save(user);
	}
	
	public void deleteUserById(Long id) throws ResourceNotFoundException {
		if (!existsById(id)) {
			throw new ResourceNotFoundException("Cannot find bargain with id: " + id);
		}
		userRepository.deleteById(id);		
	}

	
}
