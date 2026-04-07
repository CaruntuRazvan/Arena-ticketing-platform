package com.arena.ticketing.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF ACTIV
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/users/register", "/api/users/login", "/api/users/verify", "/api/users/resend-code", "/h2-console/**")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                // 2. JSESSIONID
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                // 3. PROTECȚIE ENDPOINT-URI
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/register", "/api/users/login", "/api/users/verify", "/api/users/resend-code","/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/**").permitAll()

                        // ADMIN: Matches, Stadiums, Prices
                        .requestMatchers(HttpMethod.POST, "/api/matches/**", "/api/stadiums/**", "/api/prices/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/matches/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/**").hasAuthority("ADMIN")

                        // USER & ADMIN: Rezervare bilet
                        .requestMatchers("/api/tickets/buy").hasAnyAuthority("USER", "ADMIN")

                        .anyRequest().authenticated()
                )
                // 4. AUTHENTICARE (JDBC se face prin CustomUserDetailsService)
                .httpBasic(Customizer.withDefaults())

                // 5. REMEMBER ME
                .rememberMe(rm -> rm
                        .key("SecretKeyArenaTicketing")
                        .tokenValiditySeconds(86400)
                )
                // 6. LOGOUT
                .logout(logout -> logout
                        .logoutUrl("/api/users/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN", "remember-me")
                        .logoutSuccessHandler((req, res, auth) -> res.setStatus(200))
                )
                .headers(headers -> headers.frameOptions(f -> f.sameOrigin()))

                // 7. FILTRU CSRF  -> a vedea X-XSRF-TOKEN în Postman
                .addFilterAfter(new OncePerRequestFilter() {
                    @Override
                    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                            throws ServletException, IOException {
                        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
                        if (null != csrfToken && null != csrfToken.getHeaderName()) {
                            response.setHeader(csrfToken.getHeaderName(), csrfToken.getToken());
                        }
                        filterChain.doFilter(request, response);
                    }
                }, BasicAuthenticationFilter.class);

        return http.build();
    }
}