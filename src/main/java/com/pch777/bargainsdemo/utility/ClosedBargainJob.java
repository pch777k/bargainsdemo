package com.pch777.bargainsdemo.utility;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.pch777.bargainsdemo.model.Bargain;
import com.pch777.bargainsdemo.repository.BargainRepository;
import com.pch777.bargainsdemo.service.BargainService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class ClosedBargainJob {
	
	private BargainRepository bargainRepository;
	private BargainService bargainService;
	
	@Transactional
	//@Scheduled(cron = "0 0 0 * * *", zone="UTC+1")
	@Scheduled(fixedRate = 480_000)
	public void run() {
		List<Bargain> bargains = bargainRepository
				.findByClosedAndEndBargainLessThan(false, LocalDate.now());
		bargains.forEach(bargain -> bargainService.closeBargainById(bargain.getId()));
	
	}
}
