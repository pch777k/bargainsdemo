package com.pch777.bargainsdemo.model;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
public class Bargain extends AuditModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Title must not be blank")
	@Size(max = 90, message = "Title cannot be longer than 90 characters")
	private String title;

	@Column(length = 2147483647)
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String description;

	@Min(value = 0, message = "Price should not be less than 0")
	private Double reducePrice;
	@Min(value = 0, message = "Price should not be less than 0")
	private Double normalPrice;
	@Min(value = 0, message = "Delivery should not be less than 0")
	private Double delivery;

	private String coupon;
	private String link;

	private byte[] photo;
	
	private Boolean closed;

	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private LocalDate startBargain;

	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private LocalDate endBargain;

	@Builder.Default
	private Integer voteCount = 0;

	@Enumerated
	@NotNull(message = "Please choose a category of bargain")
	private Category category;

	@ManyToOne
	@JoinColumn(name = "user_id")
	@JsonIgnoreProperties({ "comments", "bargains", "votes", "activities" })
	private User user;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "bargain_id")
	@JsonIgnoreProperties({ "bargain", "user" })
	private List<Comment> comments;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "bargain_id")
	@JsonIgnoreProperties({ "bargain", "user" })
	private List<Activity> activities;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "bargain_id")
	@JsonIgnoreProperties({ "bargain", "user" })
	private List<Vote> votes;
	
	public Bargain(String title, String description, Double reducePrice, Double normalPrice, Double delivery, 
			String coupon, String link, byte[] photo, LocalDate startBargain, LocalDate endBargain, Category category) {
			this.title = title;
			this.description = description;
			this.reducePrice = reducePrice;
			this.normalPrice = normalPrice;
			this.delivery = delivery;
			this.coupon = coupon;
			this.link = link;
			this.photo = photo;
			this.startBargain = startBargain;
			this.endBargain = endBargain;
			this.category = category;
		}
}
