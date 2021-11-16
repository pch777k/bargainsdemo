package com.pch777.bargainsdemo.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pch777.bargainsdemo.exception.ResourceNotFoundException;
import com.pch777.bargainsdemo.model.Vote;
import com.pch777.bargainsdemo.service.VoteService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RequestMapping("/api/votes")
@RestController
public class VoteRestController {

	private VoteService voteService;
	
	@GetMapping
	public List<Vote> getAllVotes() {
		return voteService.getAllVotes();
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Vote> getVoteById(@PathVariable Long id) {
		return voteService
				.getById(id)
				.map(vote -> ResponseEntity.ok(vote))
				.orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteVoteById(@PathVariable Long id) {
		try {
			voteService.deleteVoteById(id);
			return ResponseEntity.noContent().build();
		} catch (ResourceNotFoundException ex) {
			return ResponseEntity.notFound().build();
		}
	}
	
	
	
}
