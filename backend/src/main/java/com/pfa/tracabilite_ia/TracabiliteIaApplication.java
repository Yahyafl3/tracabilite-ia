package com.pfa.tracabilite_ia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TracabiliteIaApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(TracabiliteIaApplication.class);
		app.addListeners(new com.pfa.tracabilite_ia.config.ProductionEnvironmentValidator.EarlyProdGuard());
		app.run(args);
	}

}
