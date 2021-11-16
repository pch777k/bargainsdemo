package com.pch777.bargainsdemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pch777.bargainsdemo.model.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

	Role findRoleByName(String name);

}
