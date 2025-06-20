package vn.edu.iuh.fit.tourmanagement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import vn.edu.iuh.fit.tourmanagement.exceptions.CustomAccessDeniedHandler;

import java.util.List;

@Configuration
@EnableWebSecurity
@ComponentScan("vn.edu.iuh.fit.tourmanagement")
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter authenticationFilter;

    @Lazy
    private final UserDetailsService userService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())  // Disabling CSRF for stateless API
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:3000","http://localhost:5173", "https://18.138.107.49/api/tours","http://18.138.107.49", "https://app.botpress.cloud", "https://studio.botpress.cloud", "https://botpress.studio", "https://master.d13wgnx834f8rx.amplifyapp.com/", "https://tadatour.io.vn/"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT","PATCH", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                    source.registerCorsConfiguration("/**", config);
                    return config;
                }))
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // ✅ FIX CORS
                                .requestMatchers("/api/auth/**").permitAll()   // Allow public access to auth endpoints
                                .requestMatchers("/file/**").permitAll()   // Allow public access to file endpoints
                                .requestMatchers("/api/users/**").permitAll() // Allow public access to user endpoints
                                .requestMatchers("api/tours/**").permitAll()
                                .requestMatchers("api/customers/**").permitAll()
                                .requestMatchers("api/bookings/**").permitAll()
                                .requestMatchers("/api/manage-bookings/**").permitAll()
                                .requestMatchers("api/tour-details/**").permitAll()
                                .requestMatchers("api/discounts/**").permitAll()
                                .requestMatchers("api/employees/**").permitAll()
                                .requestMatchers("api/schedules/**").permitAll()
                                .requestMatchers("api/categories/**").permitAll()
                                .requestMatchers("api/recommendations/**").permitAll()
                                .requestMatchers("api/otp/**").permitAll()
//                                .requestMatchers("api/user/**").permitAll()
                                .requestMatchers("/api/bookings/history").authenticated()
                                .requestMatchers("/api/schedules/**").permitAll()
                                .requestMatchers("/api/categories/**").permitAll()
                                .requestMatchers("/api/recommendations/**").permitAll()
                                .requestMatchers("/api/otp/**").permitAll()
                                .requestMatchers("/api/bookings/cancel/**").permitAll()
                                .requestMatchers("/api/reviews/by-tour/**").permitAll()
                                .requestMatchers("/api/payment/**").authenticated()
                                .requestMatchers("/api/search-history/search").permitAll()
                                .requestMatchers("/api/reviews/**").authenticated()
                                .requestMatchers("/api/recommendations/me").authenticated()
                                .requestMatchers("/api/recommendations/click/**").authenticated()
                                .requestMatchers("/api/search-history/my-history").authenticated()
                                .requestMatchers("/api/users/update-profile").authenticated()
                                .requestMatchers("/api/users/upload-avatar").authenticated()
                                .requestMatchers("/api/tour-favourites/**").authenticated()
                                .requestMatchers("/api/reports/**").permitAll()
                                .requestMatchers(HttpMethod.POST,"/api/tours/upload-tour").permitAll()
                                .anyRequest().authenticated()

                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder()); // Use BCryptPasswordEncoder for password hashing
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Use BCrypt for secure password hashing
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler(); // Define custom behavior for access denied scenarios
    }
}
