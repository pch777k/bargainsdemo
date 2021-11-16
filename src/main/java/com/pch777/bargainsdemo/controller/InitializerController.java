package com.pch777.bargainsdemo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pch777.bargainsdemo.service.InitializerService;

@Controller
public class InitializerController {
	
	
	private InitializerService initializerService;
	private final int NUMBER_OF_VOTES;
	
	public InitializerController(InitializerService initializerService, 
			@Value("${bargainapp.number-of-votes}") int nUMBER_OF_VOTES) {
		this.initializerService = initializerService;
		this.NUMBER_OF_VOTES = nUMBER_OF_VOTES;
	}

	@RequestMapping("/init-data")
	public String initSampleData() throws Exception {
		initializerService.initUserDate();
		initializerService.initBargainDate();
		initializerService.initCommentDate();
		initializerService.initVoteDate(NUMBER_OF_VOTES);
		return "redirect:/";
	}

}
