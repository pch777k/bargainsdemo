package com.pch777.bargainsdemo;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import com.pch777.bargainsdemo.exception.ResourceNotFoundException;
import com.pch777.bargainsdemo.model.User;
import com.pch777.bargainsdemo.service.UserService;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserServiceTest {

	@Autowired
	UserService userService;
	
	@Test
	public void shouldGetAllUsers() {
		// given
		userService.registerUser(givenThirdUser());
		userService.registerUser(givenFourthUser());
		
		// when
		List<User> all = userService.getAllUsers();
	
		// then
		// guest and administrator accounts are created when the application is started
		Assertions.assertEquals(4, all.size());
	}
	
	@Test
	public void shouldGetUserByEmail() {
		// given
		userService.registerUser(givenThirdUser());
		userService.registerUser(givenFourthUser());
		
		// when
		User user = userService.findUserByEmail("third@demomail.com");
	
		// then
		Assertions.assertEquals("third@demomail.com", user.getEmail());
	}
	
	@Test
	public void shouldGetUserById() {
		// given
		userService.registerUser(givenThirdUser());
		userService.registerUser(givenFourthUser());
		
		// when
		User user = userService.findUserById(4L);
	
		// then
		Assertions.assertEquals("fourth@demomail.com", user.getEmail());
		Assertions.assertNotEquals("third@demomail.com", user.getEmail());
	}
	
	@Test
	public void shouldDeleteUserById() throws ResourceNotFoundException {
		// given
		userService.registerUser(givenThirdUser());
		userService.registerUser(givenFourthUser());
		
		// when
		userService.deleteUserById(4L);
		List<User> all = userService.getAllUsers();
		Boolean isExist = userService.existsById(4L);
	
		// then
		Assertions.assertEquals(3, all.size());
		Assertions.assertFalse(isExist);
	}
	
	@Test
	public void shouldExistUserWithEmail() {
		// given
		userService.registerUser(givenThirdUser());
		userService.registerUser(givenFourthUser());
		
		// when
		Boolean isPresent = userService.isUserPresent("third@demomail.com");
	
		// then
		Assertions.assertTrue(isPresent);

	}
	
	@Test
	public void shouldExistUserWithId() {
		// given
		userService.registerUser(givenThirdUser());
		userService.registerUser(givenFourthUser());
		
		// when
		Boolean isExist = userService.existsById(4L);
	
		// then
		Assertions.assertTrue(isExist);
	}
	
	private User givenThirdUser() {
		return User.builder()
				.email("third@demomail.com")
				.nickname("third-user")
				.password("pass1234")
				.build();
	}
	
	private User givenFourthUser() {
		return User.builder()
				.email("fourth@demomail.com")
				.nickname("fourth-user")
				.password("pass1234")
				.build();
	}
}
