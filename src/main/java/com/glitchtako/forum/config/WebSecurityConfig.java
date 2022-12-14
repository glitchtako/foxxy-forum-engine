package com.glitchtako.forum.config;

import com.glitchtako.forum.filter.AuthTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class WebSecurityConfig {

  @Autowired public UserDetailsService userDetailsService;

  @Autowired private AuthTokenFilter authTokenFilter;

  @Autowired private CustomAccessDeniedHandler customAccessDeniedHandler;

  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      HttpSecurity http,
      BCryptPasswordEncoder passwordEncoder,
      UserDetailsService userDetailsService)
      throws Exception {
    return http.getSharedObject(AuthenticationManagerBuilder.class)
        .userDetailsService(userDetailsService)
        .passwordEncoder(passwordEncoder)
        .and()
        .build();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http.cors()
        .and()
        .csrf()
        .disable()
        .headers()
        .frameOptions()
        .disable()
        .and()
        //                .sessionManagement()
        //                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        //                .and()
        .authorizeRequests()
        .antMatchers(
            "/auth/**",
            "/swagger-ui",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/favicon.ico",
            "/configuration/ui",
            "/swagger-resources/**",
            "/configuration/security",
            "/actuator/**")
        .permitAll()
        .antMatchers("/auth/{id}/**")
        .access("@accessControlService.checkUserId(authentication, #id)")
        .antMatchers(HttpMethod.PUT, "/article/{id}/**")
        .access("@accessControlService.checkArticleAuthor(authentication, #id)")
        .antMatchers("/**")
        .authenticated()
        .and()
        .exceptionHandling()
        .accessDeniedHandler(customAccessDeniedHandler)
        .and()
        .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}
