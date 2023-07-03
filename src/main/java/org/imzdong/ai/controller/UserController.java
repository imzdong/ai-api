package org.imzdong.ai.controller;

import org.imzdong.ai.model.User;
import org.imzdong.ai.model.req.UserRequest;
import org.imzdong.ai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ai")
public class UserController {

    @Autowired
    JwtEncoder encoder;
    @Autowired
    private UserService userService;

    @PostMapping(path = "/user")
    public User initUser(@RequestBody UserRequest request){
        return userService.addUser(request);
    }

    @DeleteMapping(path = "/user/{userId}")
    public Boolean delUser(@PathVariable String userId){
        return userService.delUser(userId);
    }

    @PostMapping("/token")
    public String token(Authentication authentication) {
        Instant now = Instant.now();
        long expiry = 36000L;
        // @formatter:off
        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiry))
                .subject(authentication.getName())
                .claim("scope", scope)
                .build();
        // @formatter:on
        return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

}
