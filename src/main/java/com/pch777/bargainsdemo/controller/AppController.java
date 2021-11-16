package com.pch777.bargainsdemo.controller;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.pch777.bargainsdemo.exception.ForbiddenException;
import com.pch777.bargainsdemo.exception.ResourceNotFoundException;
import com.pch777.bargainsdemo.model.Activity;
import com.pch777.bargainsdemo.model.Bargain;
import com.pch777.bargainsdemo.model.Comment;
import com.pch777.bargainsdemo.model.User;
import com.pch777.bargainsdemo.model.UserDto;
import com.pch777.bargainsdemo.model.Vote;
import com.pch777.bargainsdemo.model.VoteDto;
//import com.pch777.bargainsdemo.security.UserSecurity;
import com.pch777.bargainsdemo.service.ActivityService;
import com.pch777.bargainsdemo.service.BargainService;
import com.pch777.bargainsdemo.service.CommentService;
import com.pch777.bargainsdemo.service.UserService;
import com.pch777.bargainsdemo.service.VoteService;

@Controller
public class AppController {

	private UserService userService;
	private BargainService bargainService;
	private CommentService commentService;
	private VoteService voteService;
	private ActivityService activityService;
//	private UserSecurity userSecurity;
	private RestTemplate restTemplate;
	private final String NO_USER_PHOTO_URL;
     
	@GetMapping("/login") 
	public String showLoginForm() {		
		return "login";  
	}
		
    public AppController(UserService userService, 
    	BargainService bargainService, 
    	CommentService commentService,
		VoteService voteService, 
		ActivityService activityService, 
//		UserSecurity userSecurity,
		RestTemplate restTemplate,
		@Value("${bargainapp.no-user-photo-url}") String nO_USER_PHOTO_URL) {
    	
		this.userService = userService;
		this.bargainService = bargainService;
		this.commentService = commentService;
		this.voteService = voteService;
		this.activityService = activityService;
//		this.userSecurity = userSecurity;
		this.restTemplate = restTemplate;
		this.NO_USER_PHOTO_URL = nO_USER_PHOTO_URL;
    }

	@GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());         
        return "signup_form";
    }
    
    @PostMapping("/process_register")
    @Transactional 
    public String processRegister(@Valid User user, BindingResult bindingResult, Model model) throws IOException {
    	if (bindingResult.hasErrors()) {
			return "signup_form";
		}
    	if (userService.isUserPresent(user.getEmail())) {
			model.addAttribute("exist", true);
			return "signup_form";
		}
    	user.setPhoto(restTemplate.getForObject(NO_USER_PHOTO_URL, byte[].class));
    	userService.registerUser(user);
		
    	model.addAttribute("nickname", user.getNickname());
        return "register_success";
    }
    
    @GetMapping("/users")
    public String listUsers(Model model,
    		@RequestParam(defaultValue = "1") int page, 
			@RequestParam(defaultValue = "10") int pageSize) {
        
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<User> pageUsers = userService.getUsers(pageable);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
    			
		model.addAttribute("currentUser", userService.findUserByEmail(email));    
		model.addAttribute("profileUser", userService.findUserByEmail(email));  
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("currentSize", pageable.getPageSize());
		model.addAttribute("currentPage", page);
        model.addAttribute("pageUsers", pageUsers);
        model.addAttribute("totalUsers", pageUsers.getTotalElements());
        model.addAttribute("totalPages", pageUsers.getTotalPages());
                
        return "users_list";
    }
    
    @GetMapping("/users/{userId}/delete")
	@Transactional
	public String deleteUserById(@PathVariable Long userId) throws ResourceNotFoundException { 
    //	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	//	String email = auth.getName();
    	
  //  	if(userSecurity.isAdmin(email)) {
    		userService.deleteUserById(userId);
   // 	} else {
	//		throw new ForbiddenException("Access denied");
	//	}
		return "redirect:/users";
	}
    
    @GetMapping("/users/{userId}/overview")
    public String showUserOverview(@PathVariable Long userId, Model model, 
			@RequestParam(defaultValue = "1") int page, 
			@RequestParam(defaultValue = "10") int pageSize) {
    	
    	User user = userService.findUserById(userId);
    	
    	Sort sort = Sort.by("createdAt").descending();
    	Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
    	Page<Activity> pageActivitiesByUser = activityService.getActivitiesByUser(pageable, user);
    	
    	double average = 0;
    	int hottest = 0;
    	List<Bargain> listBargainsByUser = bargainService.getAllBargainsByUserId(userId);
    	if(listBargainsByUser.size()>0) {
    		average = bargainService.getAllBargainsByUserId(userId)
    				.stream()
    				.collect(Collectors.averagingInt(Bargain :: getVoteCount));
    	
    		hottest = bargainService.getAllBargainsByUserId(userId)
    				.stream()
    				.max(Comparator.comparing(Bargain :: getVoteCount))
    				.get()
    				.getVoteCount();
    	}
    	    	  	    	   	
    	List<Comment> userComments = commentService.getAllCommentsByUserId(userId); 
    	List<Vote> userVotes = voteService.getAllVotesByUserId(userId); 
    	
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
    	
		model.addAttribute("currentUser", userService.findUserByEmail(email));
		model.addAttribute("profileUser", user); 	
		model.addAttribute("activities", pageActivitiesByUser);
		model.addAttribute("totalActivities", pageActivitiesByUser.getNumberOfElements());
		model.addAttribute("totalBargains", listBargainsByUser.size());
		model.addAttribute("totalComments", userComments.size());
		model.addAttribute("totalVotes", userVotes.size());
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("currentSize", pageable.getPageSize());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", pageActivitiesByUser.getTotalPages());
		model.addAttribute("voteDto", new VoteDto());
		model.addAttribute("average", (int)Math.round(average));
		model.addAttribute("hottest", hottest);	
		model.addAttribute("noUserPhoto", NO_USER_PHOTO_URL);
    	
		return "user_overview";
    }
    
    @GetMapping("/users/{userId}/bargains")
    public String showBargainsByUser(@PathVariable Long userId, Model model, 
    		@RequestParam(defaultValue = "") String keyword,
    		@RequestParam(value = "ended", required = false) String ended,
			@RequestParam(defaultValue = "1") int page, 
			@RequestParam(defaultValue = "10") int pageSize) {
    	
    	User user = userService.findUserById(userId);
    	
    	Sort sort = Sort.by("voteCount").descending();
    	Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
    	Page<Bargain> pageBargainsByUser = bargainService.getAllBargainsByTitleLikeByUserId(pageable, keyword, userId);
    	Long totalBargains = pageBargainsByUser.getTotalElements();
    	if(("on").equals(ended)) {
    		pageBargainsByUser = bargainService.getBargainsNotClosedByUserId(pageable, keyword, userId, false);
		}
    	Long totalDisplayBargains = pageBargainsByUser.getTotalElements();
    	List<Comment> userComments = commentService.getAllCommentsByUserId(userId);
    	    	
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
    	
		model.addAttribute("loggedUser", email);
		model.addAttribute("currentUser", userService.findUserByEmail(email));
		model.addAttribute("profileUser", user); 	
		model.addAttribute("title", keyword);
		model.addAttribute("bargains", pageBargainsByUser);
		model.addAttribute("totalBargains", totalBargains);
		model.addAttribute("totalDisplayBargains", totalDisplayBargains);
		model.addAttribute("totalComments", userComments.size());
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("currentSize", pageable.getPageSize());
		model.addAttribute("currentPage", page);		
		model.addAttribute("totalPages", pageBargainsByUser.getTotalPages());
		model.addAttribute("voteDto", new VoteDto());
		model.addAttribute("closed", ended);
		model.addAttribute("noUserPhoto", NO_USER_PHOTO_URL);
    	
		return "user_bargains";
    } 
    
    @GetMapping("/users/{userId}/bargains/new")
    public String showNewBargainsByUser(@PathVariable Long userId, Model model, 
    		@RequestParam(defaultValue = "") String keyword,
    		@RequestParam(value = "ended", required = false) String ended,
			@RequestParam(defaultValue = "1") int page, 
			@RequestParam(defaultValue = "10") int pageSize) {
    	
    	User user = userService.findUserById(userId);
    	
    	Sort sort = Sort.by("createdAt").descending();
    	Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
    	Page<Bargain> pageBargainsByUser = bargainService.getAllBargainsByTitleLikeByUserId(pageable, keyword, userId);
    	Long totalBargains = pageBargainsByUser.getTotalElements();
    	if(("on").equals(ended)) {
    		pageBargainsByUser = bargainService.getBargainsNotClosedByUserId(pageable, keyword, userId, false);
		}
    	Long totalDisplayBargains = pageBargainsByUser.getTotalElements();
    	
    	List<Comment> userComments = commentService.getAllCommentsByUserId(userId);
    	
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
    	
		model.addAttribute("loggedUser", email);
		model.addAttribute("currentUser", userService.findUserByEmail(email));
		model.addAttribute("profileUser", user); 	
		model.addAttribute("title", keyword);
		model.addAttribute("bargains", pageBargainsByUser);
		model.addAttribute("totalBargains", totalBargains);
		model.addAttribute("totalDisplayBargains", totalDisplayBargains);
		model.addAttribute("totalComments", userComments.size());
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("currentSize", pageable.getPageSize());
		model.addAttribute("currentPage", page);	
		model.addAttribute("totalPages", pageBargainsByUser.getTotalPages());
		model.addAttribute("voteDto", new VoteDto());
		model.addAttribute("closed", ended);
		model.addAttribute("noUserPhoto", NO_USER_PHOTO_URL);
    	
		return "user_bargains_new";
    }
    
    @GetMapping("/users/{userId}/bargains/commented")
    public String showMostCommentedBargainsByUser(@PathVariable Long userId, Model model, 
    		@RequestParam(defaultValue = "") String keyword,
    		@RequestParam(value = "ended", required = false) String ended,
			@RequestParam(defaultValue = "1") int page, 
			@RequestParam(defaultValue = "10") int pageSize) {
    	
    	User user = userService.findUserById(userId);
    	
       	Pageable pageable = PageRequest.of(page - 1, pageSize);
    	Page<Bargain> pageBargainsByUserIdOrderByCommentSize = bargainService.getBargainsMostCommentedByUserId(pageable, keyword, userId);
    	Long totalBargains = pageBargainsByUserIdOrderByCommentSize.getTotalElements();
    	if(("on").equals(ended)) {
    		pageBargainsByUserIdOrderByCommentSize = bargainService.getBargainsNotClosedMostCommentedByUserId(pageable, keyword, userId, false);
		}
    	Long totalDisplayBargains = pageBargainsByUserIdOrderByCommentSize.getTotalElements();
    	List<Comment> userComments = commentService.getAllCommentsByUserId(userId);
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
    	
		model.addAttribute("loggedUser", email);
		model.addAttribute("currentUser", userService.findUserByEmail(email));
		model.addAttribute("profileUser", user); 	
		model.addAttribute("title", keyword);
		model.addAttribute("bargains", pageBargainsByUserIdOrderByCommentSize);
		model.addAttribute("totalBargains", totalBargains);
		model.addAttribute("totalDisplayBargains", totalDisplayBargains);
		model.addAttribute("totalComments", userComments.size());
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("currentSize", pageable.getPageSize());
		model.addAttribute("currentPage", page);	
		model.addAttribute("totalPages", pageBargainsByUserIdOrderByCommentSize.getTotalPages());
		model.addAttribute("voteDto", new VoteDto());
		model.addAttribute("closed", ended);
		model.addAttribute("noUserPhoto", NO_USER_PHOTO_URL);
    	
		return "user_bargains_commented";
    }
    
    @GetMapping("/users/{userId}/comments")
    public String showCommentsByUser(@PathVariable Long userId, Model model, 
			@RequestParam(defaultValue = "1") int page, 
			@RequestParam(defaultValue = "10") int pageSize) {
    	
    	User user = userService.findUserById(userId);
    	
    	Sort sort = Sort.by("createdAt").descending();
    	Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
    	Page<Comment> pageComments = commentService.getAllCommentsByUser(pageable, user);
    	
    	int totalUserBargains = bargainService.getAllBargainsByUserId(userId).size();
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
    	
		model.addAttribute("loggedUser", email);
		model.addAttribute("currentUser", userService.findUserByEmail(email));
		model.addAttribute("profileUser", user); 	
		model.addAttribute("totalComments", pageComments.getTotalElements());
		model.addAttribute("pageComments", pageComments);
		model.addAttribute("totalBargains", totalUserBargains);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("currentSize", pageable.getPageSize());
		model.addAttribute("currentPage", page);		
		model.addAttribute("totalPages", pageComments.getTotalPages());
		model.addAttribute("noUserPhoto", NO_USER_PHOTO_URL);
		    	
		return "user_comments";
    }   
    
    @GetMapping("/users/{userId}/profile")
	public String showUserProfile(@PathVariable Long userId, Model model) {
		
		User user = userService.findUserById(userId);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
			
//		if(userSecurity.isOwnerOrAdmin(user.getEmail(), email)) {	
		
			List<Bargain> userBargains = bargainService.getAllBargainsByUserId(userId);
	
			UserDto userDto = UserDto.builder()
					.id(user.getId())
					.nickname(user.getNickname())
					.password(user.getPassword())
					.email(user.getEmail())
					.photo(user.getPhoto())
					.build(); 
			
			model.addAttribute("userDto", userDto);
			model.addAttribute("totalBargains", userBargains.size());
			model.addAttribute("currentUser", userService.findUserByEmail(email)); 
	//	} else {
	//		throw new ForbiddenException("Access denied");
	//	}
		
		return "profile";
	}   
    
    @Transactional
    @RequestMapping("/users/{userId}/profile")
	public String updatePhoto(@PathVariable Long userId, @ModelAttribute("userDto") User userDto,
			 @RequestParam("fileImage") MultipartFile multipartFile) throws IOException    {
		
		User user = userService.findUserById(userId);
		user.setNickname(userDto.getNickname());
		user.setEmail(userDto.getEmail());
	
		if (!multipartFile.isEmpty()) {
			user.setPhoto(multipartFile.getBytes());
		} 

		return "redirect:/users/" + userId + "/profile"; 	
	}
    
    @GetMapping("users/{userId}/photo")
    public void getImage(@PathVariable("userId") Long userId, HttpServletResponse response) throws Exception {
        User user = userService.findUserById(userId);
        byte[] bytes = user.getPhoto();
        InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(bytes));
        String mimeType = URLConnection.guessContentTypeFromStream(inputStream);
        response.setContentType(mimeType);
        OutputStream outputStream = response.getOutputStream();
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();
    }
    
}
