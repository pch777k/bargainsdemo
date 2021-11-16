package com.pch777.bargainsdemo.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude="roles")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends AuditModel{
     
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotEmpty(message = "Email must not be empty")
	@Column(unique = true)   
    private String email;
    
    @NotEmpty(message = "Nickname must not be empty")
    private String nickname; 
    
    @Size(min = 3, message = "Password should be at least 3 characters")
    private String password;
    
    private byte[] photo;
    
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
	@JoinTable(name = "users_roles",
	   joinColumns = { @JoinColumn(name = "user_id") },
	   inverseJoinColumns = { @JoinColumn(name = "role_id") })
    @JsonIgnoreProperties("users")
    @Builder.Default
	private Set<Role> roles = new HashSet<>();
    
    @OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "user_id")
    @JsonIgnore
    private List<Bargain> bargains;
   
    @OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "user_id")
    @JsonIgnore
	private List<Comment> comments;
    
    @OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "user_id")
    @JsonIgnore
	private List<Vote> votes;
    
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinColumn(name = "user_id")
    @JsonIgnore
	private List<Activity> activities;
	
	public void addRole(Role role) {
        this.roles.add(role);
        role.getUsers().add(this);
    }

	public void removeRole(Role role) {
        this.roles.remove(role);
        role.getUsers().remove(this); 
    }
		
	public User(String email, String nickname, String password) {
		this.email = email;
		this.nickname = nickname;
		this.password = password;
	}
	
	public User(String email, String nickname, String password, byte[] photo) {
		this.email = email;
		this.nickname = nickname;
		this.password = password;
		this.photo = photo;
	}

}
