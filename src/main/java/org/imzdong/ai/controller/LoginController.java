package org.imzdong.ai.controller;

import org.imzdong.ai.model.User;
import org.imzdong.ai.model.req.UserRequest;
import org.imzdong.ai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping(path = "/user")
    public User initUser(@RequestBody UserRequest request){
        return userService.addUser(request);
    }

}
