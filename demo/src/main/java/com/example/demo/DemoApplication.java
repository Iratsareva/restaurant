package com.example.demo;

import com.example.Restaurant.dto.ReservationResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.config.EnableHypermediaSupport;

@SpringBootApplication (scanBasePackages = {"com.example.demo", "org.example.reservationprice"})
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public PagedResourcesAssembler<ReservationResponse> reservationPagedAssembler() {
		return new PagedResourcesAssembler<>(null, null);
	}

}
