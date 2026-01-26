package com.consistencyapp.backend.controller.user;

import com.consistencyapp.backend.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CurrentUserController {

    public record CurrentUserResponse(Long id, String email, String displayName) {}

    @GetMapping("/api/users/current")
    public CurrentUserResponse current(@AuthenticationPrincipal AuthenticatedUser user) {
        return new CurrentUserResponse(user.id(), user.email(), user.displayName());
    }
}
