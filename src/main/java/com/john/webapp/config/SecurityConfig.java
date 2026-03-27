package com.john.webapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import com.john.webapp.security.ClientUserDetailsService;
import com.john.webapp.views.login.LoginView;
import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategyConfiguration;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;

@Configuration
@EnableWebSecurity
@Import(VaadinAwareSecurityContextHolderStrategyConfiguration.class)
public class SecurityConfig {
	
	private final ClientUserDetailsService userDetailsService;
	
	public SecurityConfig(ClientUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
	
	@Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.getSharedObject(AuthenticationManagerBuilder.class)
	        .userDetailsService(userDetailsService)
	        .passwordEncoder(passwordEncoder());
			
		http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/images/**",
                        "/VAADIN/**",
                        "/line-awesome/**",
                        "/icons/**",
                        "/frontend/**",
                        "/*.ico",
                        "/*.png",
                        "/*.svg"
                ).permitAll()
        );
 
        http.with(VaadinSecurityConfigurer.vaadin(), configurer ->
                configurer.loginView(LoginView.class)
        );
 
        return http.build();
    }
 
    @Bean
    UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails admin = User.builder()
                .username("admin")
                .password(encoder.encode("admin"))
                .roles("ADMIN")
                .build();
 
        UserDetails client = User.builder()
                .username("user")
                .password(encoder.encode("user"))
                .roles("CLIENT")
                .build();
 
        return new InMemoryUserDetailsManager(admin, client);
    }
    
    @Bean
    @SuppressWarnings("deprecation")
    PasswordEncoder passwordEncoder() {
        // Plain text для demo. Замінити на BCryptPasswordEncoder для production.
        return NoOpPasswordEncoder.getInstance();
    }
 
//    @Bean
//    PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }	
}
