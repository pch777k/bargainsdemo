package com.pch777.bargainsdemo.web;

import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.pch777.bargainsdemo.model.ActivityType;
import com.pch777.bargainsdemo.model.Bargain;
import com.pch777.bargainsdemo.model.Comment;
import com.pch777.bargainsdemo.model.User;
//import com.pch777.bargainsdemo.security.UserSecurity;
import com.pch777.bargainsdemo.service.ActivityService;
import com.pch777.bargainsdemo.service.BargainService;
import com.pch777.bargainsdemo.service.CommentService;
import com.pch777.bargainsdemo.service.UserService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class CommentRestController {

	private CommentService commentService;
	private BargainService bargainService;
	private UserService userService;
//	private UserSecurity userSecurity;
	private ActivityService activityService;
	
	@GetMapping("/comments")
	public ResponseEntity<Map<String, Object>> getComments( 
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        
        try {
      List<Comment> comments = new ArrayList<>();
      
      Pageable pageable = PageRequest.of(page, size);
      Page<Comment> pageComments = commentService.getAllComments(pageable); 
      
      comments = pageComments.getContent();

      Map<String, Object> response = new HashMap<>();
      response.put("comments", comments);
      response.put("currentPage", pageComments.getNumber());
      response.put("totalItems", pageComments.getTotalElements());
      response.put("totalPages", pageComments.getTotalPages());

      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
        
	} 
	
	@GetMapping("/bargains/{bargainId}/comments")
	public ResponseEntity<Map<String, Object>> getCommentsByBargainId(@PathVariable Long bargainId, 
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        
        try {
      List<Comment> comments = new ArrayList<>();
      
      Pageable pageable = PageRequest.of(page, size);
      Page<Comment> pageComments = commentService.getCommentsByBargainId(pageable, bargainId); 
      
      comments = pageComments.getContent();

      Map<String, Object> response = new HashMap<>();
      response.put("comments", comments);
      response.put("currentPage", pageComments.getNumber());
      response.put("totalItems", pageComments.getTotalElements());
      response.put("totalPages", pageComments.getTotalPages());

      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
        
	} 
	
	@GetMapping("/users/{userId}/comments")
	public ResponseEntity<Map<String, Object>> getCommentsByUserId(@PathVariable Long userId, 
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        
        try {
      List<Comment> comments = new ArrayList<>();
      
      Pageable pageable = PageRequest.of(page, size);
      Page<Comment> pageComments = commentService.getCommentsByUserId(pageable, userId); 
      
      comments = pageComments.getContent();

      Map<String, Object> response = new HashMap<>();
      response.put("comments", comments);
      response.put("currentPage", pageComments.getNumber());
      response.put("totalItems", pageComments.getTotalElements());
      response.put("totalPages", pageComments.getTotalPages());

      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
        
	} 

	@GetMapping("comments/{id}")
	public ResponseEntity<Comment> getCommentById(@PathVariable Long id) {
		return commentService
				.getById(id)
				.map(comment -> ResponseEntity.ok(comment))
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping("/bargains/{bargainId}/comments")
	public ResponseEntity<Void> addComment(@RequestBody Comment comment, @PathVariable Long bargainId) throws ResourceNotFoundException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			if(bargainService.existsById(bargainId)) {
				Bargain bargain = bargainService.getBargainById(bargainId);
				User user = userService.findUserByEmail(authentication.getName());
				comment.setUser(user);
				comment.setBargain(bargain);
			    commentService.addComment(comment);
			    activityService.addActivity(comment.getUser(), comment.getCreatedAt(), comment.getBargain(), ActivityType.COMMENT);
		
			    URI uri = ServletUriComponentsBuilder
				.fromCurrentRequestUri()
				.path("/" + comment.getId().toString())
				.build()
				.toUri();
		return ResponseEntity.created(uri).build();
			} else {
				ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}
		}
		return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	}

	@PutMapping("comments/{id}")
	public ResponseEntity<Void> updateComment(@RequestBody Comment comment, @PathVariable Long id,
			Principal principal) throws ResourceNotFoundException {
		if (commentService.existsById(id)) {
//			if(userSecurity.isOwnerOrAdmin(commentService.getCommentById(id).getUser().getEmail(), principal.getName())) {			
				Comment editedComment = commentService.getCommentById(id);
				comment.setBargain(editedComment.getBargain());
				comment.setUser(editedComment.getUser());
				commentService.editComment(comment, id);
				return ResponseEntity.ok().build();
//			} else {
//				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//			}
			
			
		}
		return ResponseEntity.notFound().build();
	}


	@DeleteMapping("comments/{id}")
	public ResponseEntity<Void> deleteCommentById(@PathVariable Long id, Principal principal) {
		try {
//			Comment comment = commentService.getCommentById(id);
//			if(userSecurity.isOwnerOrAdmin(comment.getUser().getEmail(), principal.getName())) {
				commentService.deleteCommentById(id);
				return ResponseEntity.noContent().build();		
//			} else {
//				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//			}
		} catch (ResourceNotFoundException ex) {
			return ResponseEntity.notFound().build();
		}
	}

}
