package com.rentoss.user.presentation.api;

import com.rentoss.core.security.annotation.CurrentUser;
import com.rentoss.core.domain.model.CurrentUserInfo;
import com.rentoss.user.application.UserService;
import com.rentoss.user.domain.User;
import com.rentoss.user.presentation.dto.request.LocationUpdateRequest;
import com.rentoss.user.presentation.dto.request.UserUpdateRequest;
import com.rentoss.user.presentation.dto.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse getMyInfo(@CurrentUser CurrentUserInfo userInfo) {
        User user = userService.getUser(userInfo.userId());
        return UserResponse.from(user);
    }

    @GetMapping("/{userId}")
    public UserResponse getUser(@PathVariable Long userId) {
        User user = userService.getUser(userId);
        return UserResponse.from(user);
    }

    @PatchMapping("/me")
    public ResponseEntity<Void> updateProfile(
            @CurrentUser CurrentUserInfo userInfo,
            @Valid @RequestBody UserUpdateRequest request) {
        userService.updateProfile(
                userInfo.userId(),
                request.nickname(),
                request.profileImageUrl()
        );
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/me/location")
    public ResponseEntity<Void> updateLocation(
            @CurrentUser CurrentUserInfo userInfo,
            @Valid @RequestBody LocationUpdateRequest request) {
        userService.updateLocation(
                userInfo.userId(),
                request.latitude(),
                request.longitude(),
                request.address()
        );
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(@CurrentUser CurrentUserInfo userInfo) {
        userService.withdraw(userInfo.userId());
        return ResponseEntity.noContent().build();
    }
}
