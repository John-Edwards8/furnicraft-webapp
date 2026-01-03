package com.john.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;

@SpringBootApplication
@StyleSheet("styles.css")
@PWA(name = "FurniCraft", shortName = "FC")
@Theme("furnicraft-webapp")
public class WebappApplication implements AppShellConfigurator {

	public static void main(String[] args) {
		SpringApplication.run(WebappApplication.class, args);
	}

}
