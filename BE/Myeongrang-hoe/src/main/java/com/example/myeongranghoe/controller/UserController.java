package com.example.myeongranghoe.controller;

import com.example.myeongranghoe.config.UserContext;
import com.example.myeongranghoe.dto.UserResponse;
import com.example.myeongranghoe.service.CommunityService;
import com.example.myeongranghoe.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final CommunityService communityService;

    public UserController(UserService userService, CommunityService communityService) {
        this.userService = userService;
        this.communityService = communityService;
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me() {
        String email = UserContext.require();
        UserResponse user = userService.getByEmail(email);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "user", user,
                "wishlist", communityService.wishlistIds(email)
        ));
    }

    @PatchMapping("/me")
    public ResponseEntity<Map<String, Object>> updateMe(@RequestBody ProfileBody body) {
        String email = UserContext.require();
        UserResponse user = userService.updateProfile(email, new UserService.ProfilePatch(
                body.name(),
                body.campus(),
                body.major(),
                body.age(),
                body.bio(),
                body.interests()
        ));
        return ResponseEntity.ok(Map.of("success", true, "message", "프로필이 저장되었어요.", "user", user));
    }

    @GetMapping("/{email}/reviews")
    public ResponseEntity<Map<String, Object>> reviews(@PathVariable String email) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "reviews", communityService.reviewsForUser(email)
        ));
    }

    public record ProfileBody(
            String name,
            String campus,
            String major,
            String age,
            String bio,
            List<String> interests
    ) {}
}
