package com.pch777.bargainsdemo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
	private Long id;
	private String nickname;
	private String email;
	private String password;
	private byte[] photo;
}
