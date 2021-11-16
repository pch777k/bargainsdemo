package com.pch777.bargainsdemo.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.springframework.data.annotation.CreatedDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
   
    @Enumerated
    private VoteType voteType;
    
    @Column(name = "created_at", columnDefinition = "TIMESTAMP", updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

	@ManyToOne
	@JoinColumn(name="bargain_id")
	private Bargain bargain;
	
	@ManyToOne
	@JoinColumn(name="user_id")
	private User user;
	
	public Vote(VoteType voteType, LocalDateTime createdAt, Bargain bargain, User user) {
		this.voteType = voteType;
		this.createdAt = createdAt;
		this.bargain = bargain;
		this.user = user;
	}
}
