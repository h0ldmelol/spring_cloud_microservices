package com.kulikov.inventoryservice;

import com.kulikov.inventoryservice.model.Inventory;
import com.kulikov.inventoryservice.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}

	@Bean
	public CommandLineRunner loadData(InventoryRepository inv) {
		return args -> {
			Inventory inventory = new Inventory();
			inventory.setSkuCode("1");
			inventory.setQuantity(3);

			inv.save(inventory);
		};
	}

}
