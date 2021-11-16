package com.pch777.bargainsdemo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pch777.bargainsdemo.model.Comment;
import com.pch777.bargainsdemo.model.User;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>{
	
	List<Comment> findByBargainId(Long id);
	List<Comment> findByUserId(Long id);
	Page<Comment> findCommentsByBargainId(Pageable pageable, Long id);
	Page<Comment> findByUser(Pageable pageable, User user);
	Optional<Comment> findByIdAndBargainId(Long id, Long bargainId);
	Page<Comment> findAllByUserId(Pageable pageable, Long userId);
	

}
