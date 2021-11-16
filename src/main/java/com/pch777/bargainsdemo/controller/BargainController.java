package com.pch777.bargainsdemo.controller;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.time.LocalDate;
import java.util.Arrays;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.pch777.bargainsdemo.exception.ForbiddenException;
import com.pch777.bargainsdemo.exception.ResourceNotFoundException;
import com.pch777.bargainsdemo.model.ActivityType;
import com.pch777.bargainsdemo.model.Bargain;
import com.pch777.bargainsdemo.model.Category;
import com.pch777.bargainsdemo.model.Comment;
import com.pch777.bargainsdemo.model.CommentDto;
import com.pch777.bargainsdemo.model.VoteDto;
import com.pch777.bargainsdemo.security.UserSecurity;
import com.pch777.bargainsdemo.service.ActivityService;
import com.pch777.bargainsdemo.service.BargainService;
import com.pch777.bargainsdemo.service.CommentService;
import com.pch777.bargainsdemo.service.UserService;
import com.pch777.bargainsdemo.utility.StringToEnumConverter;

@Controller
public class BargainController {

	private BargainService bargainService;
	private CommentService commentService;
	private UserService userService;
	private ActivityService activityService;
	private UserSecurity userSecurity;
	private StringToEnumConverter converter;
	private RestTemplate restTemplate;	
	private final String NO_BARGAIN_PHOTO_URL;
	private final String NO_USER_PHOTO_URL;
	
	public BargainController(BargainService bargainService, 
			CommentService commentService, 
			UserService userService,
			ActivityService activityService, 
			UserSecurity userSecurity,
			StringToEnumConverter converter,
			RestTemplate restTemplate, 
			@Value("${bargainapp.no-bargain-photo-url}") String nO_BARGAIN_PHOTO_URL,
			@Value("${bargainapp.no-user-photo-url}") String nO_USER_PHOTO_URL) {
		
		this.bargainService = bargainService;
		this.commentService = commentService;
		this.userService = userService;
		this.activityService = activityService;
		this.userSecurity = userSecurity;
		this.converter = converter;
		this.restTemplate = restTemplate;
		this.NO_BARGAIN_PHOTO_URL = nO_BARGAIN_PHOTO_URL;
		this.NO_USER_PHOTO_URL = nO_USER_PHOTO_URL;
	}

	@GetMapping("/")
	public String listBargains(Model model, 
			@RequestParam(defaultValue = "") String keyword,
			@RequestParam(value = "ended", required = false) String ended,
			@RequestParam(defaultValue = "1") int page, 
			@RequestParam(defaultValue = "10") int pageSize) {

		Sort sort = Sort.by("voteCount").descending();
		Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
		
		Page<Bargain> pageBargains = bargainService.getAllBargainsByTitleLike(pageable, keyword);
		long totalBargains = pageBargains.getTotalElements();
		if(("on").equals(ended)) {
			pageBargains = bargainService.getAllBargainsByTitleLikeAndClosed(pageable, keyword, false);
		}
		long totalDisplayBargains = pageBargains.getTotalElements();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
	
		model.addAttribute("loggedUser", email);
		model.addAttribute("currentUser", userService.findUserByEmail(email));
		model.addAttribute("title", keyword);
		model.addAttribute("bargains", pageBargains);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("currentPage", page);
		model.addAttribute("currentSize", pageable.getPageSize());
		model.addAttribute("totalPages", pageBargains.getTotalPages());
		model.addAttribute("voteDto", new VoteDto());
		model.addAttribute("today", LocalDate.now());	
		model.addAttribute("closed", ended);
		model.addAttribute("totalBargains", totalBargains);
		model.addAttribute("totalDisplayBargains", totalDisplayBargains);
		
		return "bargains";
	}
	
	@GetMapping("/new")
	public String listNewBargains(Model model, 
			@RequestParam(defaultValue = "") String keyword,
			@RequestParam(value = "ended", required = false) String ended,
			@RequestParam(defaultValue = "1") int page, 
			@RequestParam(defaultValue = "10") int pageSize) {

		Sort sort = Sort.by("createdAt").descending();
		Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
		Page<Bargain> pageBargains = bargainService.getAllBargainsByTitleLike(pageable, keyword);
		long totalBargains = pageBargains.getTotalElements();
		if(("on").equals(ended)) {
			pageBargains = bargainService.getAllBargainsByTitleLikeAndClosed(pageable, keyword, false);
		}
		long totalDisplayBargains = pageBargains.getTotalElements();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();

		model.addAttribute("loggedUser", email);
		model.addAttribute("currentUser", userService.findUserByEmail(email));
		model.addAttribute("title", keyword);
		model.addAttribute("bargains", pageBargains);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("currentPage", page);
		model.addAttribute("currentSize", pageable.getPageSize());
		model.addAttribute("totalPages", pageBargains.getTotalPages());
		model.addAttribute("voteDto", new VoteDto());
		model.addAttribute("today", LocalDate.now());
		model.addAttribute("closed", ended);
		model.addAttribute("totalBargains", totalBargains);
		model.addAttribute("totalDisplayBargains", totalDisplayBargains);

		return "bargains_new";
	}
	
	@GetMapping("/commented")
	public String listBargainsCommented(Model model, 
			@RequestParam(defaultValue = "") String keyword,
			@RequestParam(value = "ended", required = false) String ended,
			@RequestParam(defaultValue = "1") int page, 
			@RequestParam(defaultValue = "10") int pageSize) {

		Pageable pageable = PageRequest.of(page - 1, pageSize);
		Page<Bargain> pageBargains = bargainService.getBargainsMostCommented(pageable, keyword);
		long totalBargains = pageBargains.getTotalElements();
		if(("on").equals(ended)) {
			pageBargains = bargainService.getBargainsNotClosedMostCommented(pageable, keyword, false);
		}
		long totalDisplayBargains = pageBargains.getTotalElements();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
		
		model.addAttribute("loggedUser", email);
		model.addAttribute("currentUser", userService.findUserByEmail(email));
		model.addAttribute("title", keyword);
		model.addAttribute("bargains", pageBargains);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("currentPage", page);
		model.addAttribute("currentSize", pageable.getPageSize());
		model.addAttribute("totalPages", pageBargains.getTotalPages());
		model.addAttribute("voteDto", new VoteDto());
		model.addAttribute("today", LocalDate.now());
		model.addAttribute("closed", ended);
		model.addAttribute("totalBargains", totalBargains);
		model.addAttribute("totalDisplayBargains", totalDisplayBargains);
	
		return "bargains_commented";
	}
	
	@GetMapping("/{bargainCategory}")
	public String listBargainsByCategory(@PathVariable String bargainCategory, Model model, 
			@RequestParam(defaultValue = "") String keyword,
			@RequestParam(value = "ended", required = false) String ended,
			@RequestParam(defaultValue = "1") int page, 
			@RequestParam(defaultValue = "10") int pageSize) {

		
		Category category = converter.convert(bargainCategory);
		Category[] categories = Category.values();
		if(!Arrays.asList(categories).contains(category)) {
			return "redirect:/";
		}
		
		Sort sort = Sort.by("voteCount").descending();
		Pageable pageable = PageRequest.of(page - 1, pageSize, sort);

		Page<Bargain> pageBargainsByCategory = bargainService.getAllBargainsByTitleLikeByCategory(pageable, keyword, category);
		long totalBargains = pageBargainsByCategory.getTotalElements();
		if(("on").equals(ended)) {
			pageBargainsByCategory = bargainService.getNotClosedBargainsByTitleLikeByCategory(pageable, keyword, category, false);
		}
		long totalDisplayBargains = pageBargainsByCategory.getTotalElements();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
	
		model.addAttribute("category", category);
		model.addAttribute("loggedUser", email);
		model.addAttribute("currentUser", userService.findUserByEmail(email));
		model.addAttribute("title", keyword);
		model.addAttribute("bargains", pageBargainsByCategory);
		model.addAttribute("totalBargains", totalBargains);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("currentPage", page);
		model.addAttribute("currentSize", pageable.getPageSize());
		model.addAttribute("totalPages", pageBargainsByCategory.getTotalPages());
		model.addAttribute("voteDto", new VoteDto());
		model.addAttribute("today", LocalDate.now());
		model.addAttribute("closed", ended);
		model.addAttribute("totalDisplayBargains", totalDisplayBargains);
		
		return "category";
	}

	@GetMapping("/{bargainCategory}/new")
	public String listNewBargainsByCategory(@PathVariable String bargainCategory, Model model, 
			@RequestParam(defaultValue = "") String keyword,
			@RequestParam(value = "ended", required = false) String ended,
			@RequestParam(defaultValue = "1") int page, 
			@RequestParam(defaultValue = "10") int pageSize) {

		Sort sort = Sort.by("createdAt").descending();
		Pageable pageable = PageRequest.of(page - 1, pageSize, sort);

		Category category = converter.convert(bargainCategory);
		Page<Bargain> pageBargainsByCategory = bargainService.getAllBargainsByTitleLikeByCategory(pageable, keyword, category);
		long totalBargains = pageBargainsByCategory.getTotalElements();
		if(("on").equals(ended)) {
			pageBargainsByCategory = bargainService.getNotClosedBargainsByTitleLikeByCategory(pageable, keyword, category, false);
		}
		long totalDisplayBargains = pageBargainsByCategory.getTotalElements();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
	
		model.addAttribute("category", category);
		model.addAttribute("loggedUser", email);
		model.addAttribute("currentUser", userService.findUserByEmail(email));
		model.addAttribute("title", keyword);
		model.addAttribute("bargains", pageBargainsByCategory);
		model.addAttribute("totalBargains", totalBargains);
		model.addAttribute("totalDisplayBargains", totalDisplayBargains);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("currentPage", page);
		model.addAttribute("currentSize", pageable.getPageSize());
		model.addAttribute("totalPages", pageBargainsByCategory.getTotalPages());
		model.addAttribute("voteDto", new VoteDto());
		model.addAttribute("today", LocalDate.now());	
		model.addAttribute("closed", ended);
		
		return "category_new";
	}

	@GetMapping("/{bargainCategory}/commented")
	public String listMostCommentedBargainsByCategory(@PathVariable String bargainCategory, Model model, 
			@RequestParam(defaultValue = "") String keyword,
			@RequestParam(value = "ended", required = false) String ended,
			@RequestParam(defaultValue = "1") int page, 
			@RequestParam(defaultValue = "10") int pageSize) {

		Pageable pageable = PageRequest.of(page - 1, pageSize);

		Category category = converter.convert(bargainCategory);
		Page<Bargain> pageBargainsByCategory = bargainService.getBargainsMostCommentedByCategory(pageable, keyword, category);
		long totalBargains = pageBargainsByCategory.getTotalElements();
		if(("on").equals(ended)) {
			pageBargainsByCategory = bargainService.getBargainsNotClosedMostCommentedByCategory(pageable, keyword, category, false);
		}
		long totalDisplayBargains = pageBargainsByCategory.getTotalElements();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
	
		model.addAttribute("category", category);
		model.addAttribute("loggedUser", email);
		model.addAttribute("currentUser", userService.findUserByEmail(email));
		model.addAttribute("title", keyword);
		model.addAttribute("bargains", pageBargainsByCategory);
		model.addAttribute("totalBargains", totalBargains);
		model.addAttribute("totalDisplayBargains", totalDisplayBargains);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("currentPage", page);
		model.addAttribute("currentSize", pageable.getPageSize());
		model.addAttribute("totalPages", pageBargainsByCategory.getTotalPages());
		model.addAttribute("voteDto", new VoteDto());
		model.addAttribute("today", LocalDate.now());
		model.addAttribute("closed", ended);
		
		return "category_commented";
	}
	
	@GetMapping("/bargains/{bargainId}")
	public String getBargainById(Model model, @PathVariable Long bargainId, 
			@RequestParam(defaultValue = "1") int page, 
			@RequestParam(defaultValue = "10") int pageSize) throws ResourceNotFoundException {
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
		
		Bargain bargain = bargainService.getBargainById(bargainId);
		Sort sort = Sort.by("createdAt").ascending();
		Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
		Page<Comment> pageCommentsByBargainId = commentService.getCommentsByBargainId(pageable, bargainId);
		Category category = bargain.getCategory();

		model.addAttribute("category", category);
		model.addAttribute("commentDto", new CommentDto());
		model.addAttribute("loggedUser", email);
		model.addAttribute("currentUser", userService.findUserByEmail(email));
		model.addAttribute("totalComments", pageCommentsByBargainId.getTotalElements());
		model.addAttribute("pageComments", pageCommentsByBargainId);
		model.addAttribute("pageTotalComments", pageCommentsByBargainId.getSize());
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("currentPage", page);
		model.addAttribute("currentSize", pageable.getPageSize());
		model.addAttribute("totalPages", pageCommentsByBargainId.getTotalPages());
		model.addAttribute("bargain", bargain);
		model.addAttribute("voteDto", new VoteDto());		
		model.addAttribute("noBargainPhoto", NO_BARGAIN_PHOTO_URL);
		model.addAttribute("noUserPhoto", NO_USER_PHOTO_URL);
		
		return "bargain";
	}

	@GetMapping("/bargains/add")
	public String showAddBargainForm(Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
		
		model.addAttribute("currentUser", userService.findUserByEmail(email));	
		model.addAttribute("bargain", new Bargain());
		model.addAttribute("noBargainPhoto", NO_BARGAIN_PHOTO_URL);

		return "add_bargain_form";
	}

	@PostMapping("/bargains/add")
	@Transactional
	public String addBargain(@Valid @ModelAttribute("bargain") Bargain bargain, BindingResult bindingResult, 
			 @RequestParam("fileImage") MultipartFile multipartFile) throws IOException {

		if (bindingResult.hasErrors()) {
			return "add_bargain_form";
		}
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();

		bargainService.addBargain(bargain);
		bargain.setUser(userService.findUserByEmail(email));
		
		if (!multipartFile.isEmpty()) {			
			bargain.setPhoto(multipartFile.getBytes());
		} else {		
			byte[] imageBytes = restTemplate.getForObject(NO_BARGAIN_PHOTO_URL, byte[].class);			
			bargain.setPhoto(imageBytes);		
		}

		activityService.addActivity(bargain.getUser(), bargain.getCreatedAt(), bargain, ActivityType.BARGAIN);
		
		return "redirect:/";
	}

	@GetMapping("/bargains/{bargainId}/edit")
	public String showEditBargainForm(@PathVariable Long bargainId, Model model) throws ResourceNotFoundException {
			
		Bargain bargain = bargainService.getBargainById(bargainId);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
		if(userSecurity.isOwnerOrAdmin(bargain.getUser().getEmail(), email)) {
			model.addAttribute("currentUser", userService.findUserByEmail(email));
			model.addAttribute("bargain", bargain);
		} else {
			throw new ForbiddenException("You don't have permission to do it.");
		}
		return "edit_bargain_form";
	}

	@PostMapping("/bargains/{bargainId}/edit")
	@Transactional
	public String editBargain(@Valid @ModelAttribute("bargain") Bargain bargain, 
			BindingResult bindingResult, @PathVariable Long bargainId,
			@RequestParam("fileImage") MultipartFile multipartFile) throws IOException, ResourceNotFoundException {
		if (bindingResult.hasErrors()) {
			return "edit_bargain_form";
		}
		Bargain editedBargain = bargainService.getBargainById(bargainId);
		bargain.setVoteCount(editedBargain.getVoteCount());
		bargain.setVotes(editedBargain.getVotes());
		bargain.setComments(editedBargain.getComments());
		bargain.setActivities(editedBargain.getActivities());
		bargain.setUser(editedBargain.getUser());
	
		if (multipartFile.isEmpty()) {
			bargain.setPhoto(editedBargain.getPhoto());	
		} else {
			bargain.setPhoto(multipartFile.getBytes());
		}
			bargainService.editBargain(bargain, bargainId);
			
		return "redirect:/";
	}

	@GetMapping("/bargains/{bargainId}/delete")
	@Transactional
	public String deleteBargainById(@PathVariable Long bargainId) throws ResourceNotFoundException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
		
		if(userSecurity.isOwnerOrAdmin(bargainService.getBargainById(bargainId).getUser().getEmail(), email)) {
			bargainService.deleteBargainById(bargainId);
		} else {
			throw new ForbiddenException("You don't have permission to do it.");
		}
		return "redirect:/";
	}
	 
	@GetMapping("/bargains/{bargainId}/close")
	public String closeBargainById(@PathVariable Long bargainId) throws ResourceNotFoundException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
		if(userSecurity.isOwnerOrAdmin(bargainService.getBargainById(bargainId).getUser().getEmail(), email)) {
		bargainService.closeBargainById(bargainId);
		} else {
			throw new ForbiddenException("You don't have permission to do it.");
		}
		return "redirect:/";
	}
	
	@GetMapping("/bargains/{bargainId}/open")
	public String openBargainById(@PathVariable Long bargainId) throws ResourceNotFoundException {  
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
		if(userSecurity.isOwnerOrAdmin(bargainService.getBargainById(bargainId).getUser().getEmail(), email)) {
			bargainService.openBargainById(bargainId);
		} else {
			throw new ForbiddenException("You don't have permission to do it.");
		}
			return "redirect:/";
		}
	
	  @GetMapping("bargains/{bargainId}/photo")
	    public void getImage(@PathVariable Long bargainId, HttpServletResponse response) throws Exception {
		    Bargain bargain = bargainService.getBargainById(bargainId);
	        byte[] bytes = bargain.getPhoto();
	        InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(bytes));
	        String mimeType = URLConnection.guessContentTypeFromStream(inputStream);
	        response.setContentType(mimeType);
	        OutputStream outputStream = response.getOutputStream();
	        outputStream.write(bytes);
	        outputStream.flush();
	        outputStream.close();
	    }

}	