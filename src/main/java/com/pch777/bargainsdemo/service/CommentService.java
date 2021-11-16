package com.pch777.bargainsdemo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.pch777.bargainsdemo.exception.ResourceNotFoundException;
import com.pch777.bargainsdemo.model.Comment;
import com.pch777.bargainsdemo.model.User;
import com.pch777.bargainsdemo.repository.CommentRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CommentService {

	private CommentRepository commentRepository;
	
	public Page<Comment> getAllComments(Pageable pageable) {
		return commentRepository.findAll(pageable);
	}
	
	public List<Comment> getAllCommentsByBargainId(Long bargainId) {
		return commentRepository.findByBargainId(bargainId);
	}
	
	public List<Comment> getAllCommentsByUserId(Long userId) {
		return commentRepository.findByUserId(userId);
	}
	
	public Page<Comment> getCommentsByUserId(Pageable pageable, Long userId) {
		return commentRepository.findAllByUserId(pageable, userId);
	}
	
	public Page<Comment> getCommentsByBargainId(Pageable pageable, Long bargainId) {
		return commentRepository.findCommentsByBargainId(pageable, bargainId);
	}
	
	public Page<Comment> getAllCommentsByUser(Pageable pageable, User user) {
		return commentRepository.findByUser(pageable,user);
	}
	
	public Comment getCommentById(Long commentId) throws ResourceNotFoundException {
		return commentRepository.findById(commentId)
				.orElseThrow(() -> new ResourceNotFoundException("Not found a bargain with id: " + commentId));
	}
	
	public Optional<Comment> getById(Long commentId) {
		return commentRepository.findById(commentId);
	}
	
	public void addComment (Comment comment) {
		commentRepository.save(comment);
	}
	
	public Comment editComment(Comment comment, Long id) throws ResourceNotFoundException {
		if (!existsById(id)) {
			throw new ResourceNotFoundException("Cannot find comment with id: " + id);
		}
		comment.setId(id);

		return commentRepository.save(comment);		
	}
	
	public void deleteCommentById(Long id) throws ResourceNotFoundException {
		if (!existsById(id)) {
			throw new ResourceNotFoundException("Cannot find comment with id: " + id);
		}
		commentRepository.deleteById(id);		
	}

	public boolean existsById(Long id) {
		return commentRepository.existsById(id);
	}

	public void editCommentContent(String content, Long id) {
		Comment comment = commentRepository.findById(id).get();
		comment.setContent(content);
		commentRepository.save(comment);		
	}
	
}
