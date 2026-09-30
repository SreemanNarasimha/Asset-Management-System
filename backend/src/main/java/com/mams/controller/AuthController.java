package com.mams.controller;

import com.mams.dto.LoginRequest;
import com.mams.dto.LoginResponse;
import com.mams.exception.BusinessException;
import com.mams.exception.ErrorCode;
import com.mams.security.AuthUser;
import com.mams.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.mams.entity.User;
import com.mams.entity.UserSession;
import com.mams.repository.UserRepository;
import com.mams.repository.UserSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserSessionRepository userSessionRepository;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserSessionRepository userSessionRepository, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userSessionRepository = userSessionRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            AuthUser authUser = (AuthUser) authentication.getPrincipal();
            User userEntity = userRepository.findById(authUser.getId()).orElseThrow();
            
            UserSession session = new UserSession();
            session.setUser(userEntity);
            session.setLoginTime(LocalDateTime.now());
            session.setStatus("ACTIVE");
            session.setIpAddress(httpRequest.getRemoteAddr());
            userSessionRepository.save(session);
            
            String token = jwtService.generateToken(authUser.getUsername(), session.getId());

            return new LoginResponse(token, authUser.getUsername(), authUser.getRoleName(), authUser.getBaseId());
        } catch (AuthenticationException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid credentials");
        }
    }

    @GetMapping("/debug")
    public java.util.List<User> debugUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/me")
    public AuthUser me(@AuthenticationPrincipal AuthUser user) {
        return user;
    }

    @PostMapping("/logout")
    public void logout(@AuthenticationPrincipal AuthUser user) {
        if (user != null && user.getSessionId() != null) {
            userSessionRepository.findById(user.getSessionId()).ifPresent(session -> {
                session.setStatus("LOGGED_OUT");
                session.setLogoutTime(LocalDateTime.now());
                userSessionRepository.save(session);
            });
        }
    }
}
