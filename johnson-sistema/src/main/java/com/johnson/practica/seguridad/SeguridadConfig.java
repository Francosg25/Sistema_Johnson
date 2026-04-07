package com.johnson.practica.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.johnson.practica.servicio.DetallesUsuarioServicio;
import com.johnson.practica.config.RateLimitingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SeguridadConfig {

    private final DetallesUsuarioServicio detallesUsuarioServicio;
    private final RateLimitingFilter rateLimitingFilter;

    public SeguridadConfig(DetallesUsuarioServicio detallesUsuarioServicio, RateLimitingFilter rateLimitingFilter) {
        this.detallesUsuarioServicio = detallesUsuarioServicio;
        this.rateLimitingFilter = rateLimitingFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico", "/login", "/cambiar-password", "/olvido-password", "/recuperar-password").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/bitacora/eliminar/**", "/evidencias/eliminar/**").hasAnyRole("ADMIN", "CHAMPION")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/login?expired")
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            )
            .userDetailsService(detallesUsuarioServicio);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
