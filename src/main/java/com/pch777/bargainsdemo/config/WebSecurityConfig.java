package com.pch777.bargainsdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.firewall.HttpStatusRequestRejectedHandler;
import org.springframework.security.web.firewall.RequestRejectedHandler;

import com.pch777.bargainsdemo.security.UserPrincipalDetailsService;

import lombok.AllArgsConstructor;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	private UserPrincipalDetailsService userPrincipalDetailsService;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) {
		auth.authenticationProvider(authenticationProvider());
	}

	@Bean
	DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		daoAuthenticationProvider.setUserDetailsService(this.userPrincipalDetailsService);

		return daoAuthenticationProvider;
	}

	@Bean
	BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers("/h2-console/**").permitAll()
				.antMatchers(HttpMethod.GET, "/api/bargains", "/api/comments", "/api/activities").permitAll()
				.antMatchers(HttpMethod.POST, "/api/bargains", "/api/comments", "/api/activities").authenticated()
				.antMatchers(HttpMethod.PUT, "/api/bargains/*", "/api/comments/*", "/api/activities/*").authenticated()
				.antMatchers(HttpMethod.PATCH, "/api/bargains/*", "/api/comments/*", "/api/activities/*").authenticated()
				.antMatchers(HttpMethod.DELETE, "/api/bargains/*", "/api/comments/*", "/api/activities/*").authenticated()
				.antMatchers("/bargains/add", "/bargains/*/delete", "/bargains/*/open", "/bargains/*/close").authenticated()
				.antMatchers("/comments/add","/bargains/*/comments/*/edit","/bargains/*/comments/*/cite", "/bargains/*/comments/*/delete").authenticated()
				.antMatchers("/votes", "/vote-bargain").authenticated()
				.antMatchers("/users","/users/*/profile").authenticated()
				.antMatchers("/users/*/delete").hasAuthority("ADMIN")				
				.antMatchers("/**").permitAll()	
				.and()
				.formLogin()
				.loginPage("/login")
				.usernameParameter("email").passwordParameter("password").defaultSuccessUrl("/").permitAll()
				.and()
				.logout().logoutSuccessUrl("/").permitAll();

		http.sessionManagement()
        		.sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
		http.httpBasic();
		http.csrf().disable();
	}
	
	@Bean
	RequestRejectedHandler requestRejectedHandler() {
	   return new HttpStatusRequestRejectedHandler();
	}

}
