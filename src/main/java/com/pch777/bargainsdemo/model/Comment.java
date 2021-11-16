package com.pch777.bargainsdemo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Comment extends AuditModel {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@Column(length = 67108864)
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String content;
	
	@ManyToOne
	@JoinColumn(name="bargain_id")
	@JsonIgnoreProperties({ "comments", "user", "activities", "votes" })
	private Bargain bargain;
	
	@ManyToOne
	@JoinColumn(name="user_id")
	private User user;
	
	public Comment(String content) {
		this.content = content;
	}

}
