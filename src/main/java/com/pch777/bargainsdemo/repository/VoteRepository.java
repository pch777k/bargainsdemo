package com.pch777.bargainsdemo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pch777.bargainsdemo.model.Bargain;
import com.pch777.bargainsdemo.model.User;
import com.pch777.bargainsdemo.model.Vote;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

	Optional<Vote> findTopByBargainAndUserOrderByIdDesc(Bargain bargain, User currentUser);
	List<Vote> findVoteByUserId(Long userId);
	List<Vote> findByBargainId(Long bargainId);

}
