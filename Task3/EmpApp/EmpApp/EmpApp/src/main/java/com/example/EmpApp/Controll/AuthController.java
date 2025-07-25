package com.example.EmpApp.controller;

import com.example.EmpApp.Entity.Employee;
import com.example.EmpApp.Entity.RefreshToken;
import com.example.EmpApp.Repository.EmployeeRepository;
import com.example.EmpApp.Repository.RefreshTokenRepository;
import com.example.EmpApp.Service.EmployeeService;
import com.example.EmpApp.config.JwtService;
import com.example.EmpApp.dto.AuthRequest;

import com.example.EmpApp.dto.EmployeeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private EmployeeRepository employeeRepo;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @PostMapping("/signin")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        Employee employee = employeeRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtService.generateAccessToken(employee);
        String refreshToken = jwtService.generateRefreshToken(employee);

        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setToken(refreshToken);
        refreshTokenEntity.setEmployee(employee);
        refreshTokenEntity.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS));
        refreshTokenRepository.save(refreshTokenEntity);

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .filter(token -> token.getExpiryDate().isAfter(Instant.now()))
                .orElseThrow(() -> new RuntimeException("Invalid or expired refresh token"));

        String email = jwtService.extractEmail(refreshToken);

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtService.generateAccessToken(employee);

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        refreshTokenRepository.findByToken(refreshToken).ifPresent(refreshTokenRepository::delete);

        return ResponseEntity.ok("Logged out successfully");
    }
    //JWT IMPLEMENTED AND NO DATA SO WANT TO ENTER A NEW DAT FOLLOW THIS

    @PostMapping("/register")
    public ResponseEntity<Employee> register(@RequestBody EmployeeDTO employeeDTO) {
        Employee savedEmployee = employeeService.create(employeeDTO);
        return ResponseEntity.ok(savedEmployee);
    }
}
