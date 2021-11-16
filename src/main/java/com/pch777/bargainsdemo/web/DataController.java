package com.pch777.bargainsdemo.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pch777.bargainsdemo.service.InitializerService;

@RestController
@RequestMapping("/api")
public class DataController {
	
	private InitializerService initializerService;	
	private final int NUMBER_OF_VOTES;
	
	public DataController(InitializerService initializerService, 
			@Value("${bargainapp.number-of-votes}") int nUMBER_OF_VOTES) {
		this.initializerService = initializerService;
		this.NUMBER_OF_VOTES = nUMBER_OF_VOTES;
	}

	@PostMapping("/data")
	@Transactional
	public void initializeUsers() {
		initializerService.initUserDate();
	}
	
	@PostMapping("/data-bargains")
	@Transactional
	public void initializeBargains() {
		initializerService.initBargainDate();
	}
	
	@PostMapping("/data-comments")
	@Transactional
	public void initializeComments() {
		initializerService.initCommentDate();
	}
	
	@PostMapping("/data-votes")
	@Transactional
	public void initializeVotes() throws Exception {
		initializerService.initVoteDate(NUMBER_OF_VOTES);
	}

}
