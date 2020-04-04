package io.ivana.api.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.ivana.api.security.AuthenticationService
import io.ivana.api.security.CustomAccessDeniedHandler
import io.ivana.api.security.CustomAuthenticationEntryPoint
import io.ivana.api.security.JwtAuthenticationFilter
import io.ivana.api.web.RootApiEndpoint
import io.ivana.api.web.v1.LoginEndpoint
import io.ivana.api.web.v1.LogoutEndpoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@Configuration
class SecurityConfiguration(
    private val authService: AuthenticationService,
    private val mapper: ObjectMapper
) : WebSecurityConfigurerAdapter() {
    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    override fun configure(http: HttpSecurity) {
        http.cors()
            .and()
            .csrf().disable()
            .formLogin().disable()
            .httpBasic().disable()
            .exceptionHandling()
            .authenticationEntryPoint(CustomAuthenticationEntryPoint(mapper))
            .accessDeniedHandler(CustomAccessDeniedHandler(mapper))
            .and()
            .addFilter(JwtAuthenticationFilter(authenticationManager(), authService))
            .authorizeRequests()
            .antMatchers(HttpMethod.POST, LoginEndpoint).permitAll()
            .antMatchers(HttpMethod.GET, LogoutEndpoint).permitAll()
            .antMatchers("$RootApiEndpoint/**").authenticated()
            .anyRequest().permitAll()
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(authService).passwordEncoder(passwordEncoder())
    }
}
