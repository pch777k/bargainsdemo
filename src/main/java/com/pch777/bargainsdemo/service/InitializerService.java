package com.pch777.bargainsdemo.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.pch777.bargainsdemo.model.ActivityType;
import com.pch777.bargainsdemo.model.Bargain;
import com.pch777.bargainsdemo.model.Category;
import com.pch777.bargainsdemo.model.Comment;
import com.pch777.bargainsdemo.model.Role;
import com.pch777.bargainsdemo.model.User;
import com.pch777.bargainsdemo.model.VoteType;
import com.pch777.bargainsdemo.repository.RoleRepository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Service
public class InitializerService {

	private UserService userService;
	private RoleRepository roleRepository;
	private BargainService bargainService;
	private CommentService commentService;
	private VoteService voteService;
	private ActivityService activityService;
	private RestTemplate restTemplate;
	private final String GUEST_USER_PHOTO_URL;
	
	public InitializerService(UserService userService, 
			RoleRepository roleRepository, 
			BargainService bargainService,
			CommentService commentService, 
			VoteService voteService, 
			ActivityService activityService,
			RestTemplate restTemplate, 
			@Value("${bargainapp.no-user-photo-url}") String gUEST_USER_PHOTO_URL) {
		this.userService = userService;
		this.roleRepository = roleRepository;
		this.bargainService = bargainService;
		this.commentService = commentService;
		this.voteService = voteService;
		this.activityService = activityService;
		this.restTemplate = restTemplate;
		this.GUEST_USER_PHOTO_URL = gUEST_USER_PHOTO_URL;
	}
	
	public void initUserDate() {
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource("users.csv").getInputStream()))){
			CsvToBean<CsvUser> build = new CsvToBeanBuilder<CsvUser>(reader)
					.withType(CsvUser.class)
					.withIgnoreLeadingWhiteSpace(true)
					.build();
			build.stream().forEach(this::initUser);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to parse csv file users.csv", e);
		}
	}
	
	public void initBargainDate() {
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource("bargains.csv").getInputStream()))){
			CsvToBean<CsvBargain> build = new CsvToBeanBuilder<CsvBargain>(reader)
					.withType(CsvBargain.class)
					.withIgnoreLeadingWhiteSpace(true)
					.build();
			build.stream().forEach(this::initBargain);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to parse csv file bargains.csv", e);
		}
	}
	
	public void initGuestBargainDate() {
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource("bargains-guest.csv").getInputStream()))){
			CsvToBean<CsvBargain> build = new CsvToBeanBuilder<CsvBargain>(reader)
					.withType(CsvBargain.class)
					.withIgnoreLeadingWhiteSpace(true)
					.build();
			build.stream().forEach(this::initGuestsBargain);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to parse csv file bargains.csv", e);
		}
	}
	
	public void initCommentDate() {
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource("comments.csv").getInputStream()))){
			CsvToBean<CsvComment> build = new CsvToBeanBuilder<CsvComment>(reader)
					.withType(CsvComment.class)
					.withIgnoreLeadingWhiteSpace(true)
					.build();
			build.stream().forEach(this::initComment);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to parse csv file comments.csv", e);
		}
	}
	
	private void initUser(CsvUser csvUser) {
		if(!userService.isUserPresent(csvUser.getEmail())) {
		
			String url = csvUser.getPhoto();
			byte[] imageBytes = restTemplate.getForObject(url, byte[].class);
			User user = new User(csvUser.getEmail(), csvUser.getNickname(), csvUser.getPassword(), imageBytes);
			userService.registerUser(user);

		}
	}
	
	public void initGuestUser() {
		if(!userService.isUserPresent("guest@demomail.com")) {
			User user = new User("guest@demomail.com", "guest", "guest");
			byte[] imageBytes = restTemplate.getForObject(GUEST_USER_PHOTO_URL, byte[].class);
			user.setPhoto(imageBytes);
			userService.registerUser(user);
		}
	}
	
	public void initAdmin() {
		if(!userService.isUserPresent("admin@demomail.com")) {
			User admin = new User("admin@demomail.com", "admin", "admin");
			byte[] imageBytes = restTemplate.getForObject(GUEST_USER_PHOTO_URL, byte[].class);
			admin.setPhoto(imageBytes);
			userService.registerAdmin(admin);
		}
	}
	
	public void initRoles() {
		if(roleRepository.findAll().isEmpty()) {
			roleRepository.save(new Role("USER"));
			roleRepository.save(new Role("ADMIN"));
		}	
	}
	
	public void initGuestsBargain(CsvBargain csvBargain) {
		String guestEmail = "guest@demomail.com";
			String url = csvBargain.getPhoto();
		
			byte[] imageBytes = restTemplate.getForObject(url, byte[].class);
			int plusDays = randomStartBargain();
			
			Bargain bargain = Bargain.builder()
					.title(csvBargain.getTitle())
					.description(csvBargain.getDescription())
					.reducePrice(csvBargain.getReducePrice())
					.normalPrice(csvBargain.getNormalPrice())
					.delivery(csvBargain.getDelivery())
					.coupon(csvBargain.getCoupon())
					.link(csvBargain.getLink())
					.photo(imageBytes)
					.closed(csvBargain.getClosed())
					.voteCount(0)				
					.startBargain(LocalDate.now().plusDays(plusDays))
					.endBargain(LocalDate.now().plusDays(randomEndBargain(plusDays)))
					.category(csvBargain.getCategory())
					.user(userService.findUserByEmail(guestEmail))
					.build(); 
			
			bargainService.addBargain(bargain);

			activityService.addActivity(bargain.getUser(), bargain.getCreatedAt(), bargain, ActivityType.BARGAIN);

	}
	
	private void initBargain(CsvBargain csvBargain) {
		String url = csvBargain.getPhoto();
	
		byte[] imageBytes = restTemplate.getForObject(url, byte[].class);
		int plusDays = randomStartBargain();
		
		Bargain bargain = Bargain.builder()
				.title(csvBargain.getTitle())
				.description(csvBargain.getDescription())
				.reducePrice(csvBargain.getReducePrice())
				.normalPrice(csvBargain.getNormalPrice())
				.delivery(csvBargain.getDelivery())
				.coupon(csvBargain.getCoupon())
				.link(csvBargain.getLink())
				.photo(imageBytes)
				.closed(csvBargain.getClosed())
				.voteCount(0)				
				.startBargain(LocalDate.now().plusDays(plusDays))
				.endBargain(LocalDate.now().plusDays(randomEndBargain(plusDays)))
				.category(csvBargain.getCategory())
				.user(userService.randomUser())
				.build(); 
		
		bargainService.addBargain(bargain);
		activityService.addActivity(bargain.getUser(), bargain.getCreatedAt(), bargain, ActivityType.BARGAIN);


	}
	
	private void initComment(CsvComment csvComment) {
		Comment comment = new Comment(csvComment.getContent());
		comment.setBargain(randomBargain());
		comment.setUser(userService.randomUser());
		commentService.addComment(comment);	
		activityService.addActivity(comment.getUser(), comment.getCreatedAt(), comment.getBargain(), ActivityType.COMMENT);
	}
	
	
	public Bargain randomBargain() {
		List<Bargain> bargains = bargainService.getAllBargains();
		Random rand = new Random();
		return bargains.get(rand.nextInt(bargains.size()));
	}
		
	public void randomVote() throws Exception {
		Random rand = new Random();
		int number = rand.nextInt(10);
		VoteType voteType = VoteType.UPVOTE;
		if(number % 5 == 0) voteType = VoteType.DOWNVOTE;
		voteService.initVote(randomBargain().getId(), userService.randomUser().getId(), voteType);
	}
	
	public void initVoteDate(int amountOfVotes) throws Exception {		
		for(int i=0; i<amountOfVotes; i++) {
			randomVote();
		}		
	}
	
	public int randomEndBargain(int startBargain) {
		Random rand = new Random();
		int number = rand.nextInt(14);
		return number + startBargain;
	}
	
	public int randomStartBargain() {
		Random rand = new Random();
		return rand.nextInt(7);
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class CsvUser {
		
		@CsvBindByName
		private String email;
		
		@CsvBindByName
		private String nickname;
		
		@CsvBindByName
		private String password;
		
		@CsvBindByName
		private String photo;
		
	}
	
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class CsvBargain {
		
		@CsvBindByName
		private String title;
		
		@CsvBindByName
		private String description;
		
		@CsvBindByName
		private double reducePrice;
		
		@CsvBindByName
		private double normalPrice;
		
		@CsvBindByName
		private double delivery;
		
		@CsvBindByName
		private String coupon;
		
		@CsvBindByName
		private String link;
		
		@CsvBindByName
		private String photo;
		
		@CsvBindByName
		private String photoName;
		
		@CsvBindByName
		private Boolean closed;
		
		@CsvBindByName
		private Category category;
		
	}
	
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class CsvComment {
		
		@CsvBindByName
		private String content;
		
	}

	
}
