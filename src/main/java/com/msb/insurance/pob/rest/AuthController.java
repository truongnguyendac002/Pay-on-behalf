package com.msb.insurance.pob.rest;

import com.msb.insurance.pob.jwt.JwtTokenProvider;
import com.msb.insurance.pob.model.request.LoginRequest;
import com.msb.insurance.pob.model.request.SignupRequest;
import com.msb.insurance.pob.model.response.JwtResponse;
import com.msb.insurance.pob.model.response.ResponseMessage;
import com.msb.insurance.pob.repository.entity.Users;
import com.msb.insurance.pob.service.CustomUserDetail;
import com.msb.insurance.pob.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/partner/v1/oauth")
public class AuthController {
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest request){
        if (userService.existsByUsername(request.getUsername())){
            return ResponseEntity.badRequest().body(new ResponseMessage("Username da ton tai! Vui long thu lai!!"));
        }
        Users user = new Users();
        user.setId(request.getId());
        user.setUsername(request.getUsername());
        user.setPassword(encoder.encode(request.getPassword()));
        userService.saveAndUpdate(user);
        return ResponseEntity.ok(new ResponseMessage("Tao thanh cong"));
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request, HttpServletResponse status){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(),request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetail customUserDetail = (CustomUserDetail) authentication.getPrincipal();
        String jwt = jwtTokenProvider.generateToken(customUserDetail);
        return  ResponseEntity.ok(new JwtResponse(status.getStatus(),"",jwt,"Bearer",jwtTokenProvider.getJWT_EXPIRATION(),""));
    }
}
