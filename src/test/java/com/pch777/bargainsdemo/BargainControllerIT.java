package com.pch777.bargainsdemo;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import com.pch777.bargainsdemo.model.Bargain;
import com.pch777.bargainsdemo.model.Category;
import com.pch777.bargainsdemo.web.BargainRestController;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class BargainControllerIT {
	
	@Autowired
	BargainRestController bargainController;
	
	@Test
	public void getAllBargains() {
		// given
		givenHtc();
		givenAdidas();
		
		// when
		List<Bargain> all = bargainController.getAllBargains();
		
		// then
		Assertions.assertEquals(2, all.size());

	}
	
	private void givenHtc() {
		
		Bargain htc = Bargain.builder()
				.title("HTC Desire 21 Pro 5G 8/128GB Blue 90Hz")
				.description("Smartphone htc desire 21")
				.reducePrice(1499.00)
				.normalPrice(1999.00)
				.delivery(0.0)
				.coupon("coupon-discount")
				.link("https://www.x-kom.pl/p/644074-smartfon-telefon-htc-desire-21-pro-5g-8-128gb-blue-0hz.html")
				.photo(null)
				.closed(false)
				.category(Category.ELECTRONICS)
				.build();
		
		bargainController.addBargain(htc);
	}
	
private void givenAdidas() {
		
		Bargain adidas = Bargain.builder()
				.title("adidas TREFOIL HOODIE")
				.description("Adidas hoodie")
				.reducePrice(200.00)
				.normalPrice(249.00)
				.delivery(0.0)
				.coupon("sale")
				.link("https://www.zalando.pl/adidas-originals-trefoil-hoodie-unisex-bluza-crew-blue-ad122s084-k13.html?size=L")
				.photo(null)
				.closed(false)
				.category(Category.FASHION)
				.build();
		
		bargainController.addBargain(adidas);
	}
}
