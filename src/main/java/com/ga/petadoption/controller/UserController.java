package com.ga.petadoption.controller;

import com.ga.petadoption.model.User;
import com.ga.petadoption.model.UserProfile;
import com.ga.petadoption.model.enums.Role;
import com.ga.petadoption.model.request.*;
import com.ga.petadoption.model.response.ChangePasswordResponse;
import com.ga.petadoption.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * User API
 */
@RestController
@RequestMapping(path = "/auth/users")
public class UserController {
    private UserService userService;

    /**
     * Initialize User service
     * @param userService UserService
     */
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Register a new user
     * @param userObject User
     * @return User
     */
    @PostMapping("/register")
    public User createUser(@RequestBody User userObject) {
        return userService.createUser(userObject);
    }

    /**
     * Login an existing user
     * @param loginRequest LoginRequest [emailAddress, password]
     * @return ResponseEntity ? Ok message
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        return userService.loginUser(loginRequest);
    }

    /**
     * Change existing user's password
     * @param changePasswordRequest ChangePasswordRequest [oldPassword, newPassword]
     * @return ChangePasswordResponse
     */
    @PutMapping("/change-password")
    public ChangePasswordResponse changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        return userService.changePassword(changePasswordRequest);
    }

    /**
     * Update user's profile
     * @param userProfile UserProfile
     * @param cprImage MultipartFile [PNG, JPEG]
     * @return UserProfile
     */
    @PutMapping(path = "/update-profile",  consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserProfile updateProfile(@RequestPart UserProfile userProfile, @RequestParam("cprImage") MultipartFile cprImage) {
        return userService.updateProfile(userProfile, cprImage);
    }

    /**
     * Send forgot password token to registered e-mail address to gain access to reset password
     * @param request ForgetPasswordRequest emailAddress
     * @return ResponseEntity ?
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgetPasswordRequest request) {
        userService.forgotPassword(request.getEmailAddress());
        return ResponseEntity.ok("If the email exists, a password reset link has been sent");
    }

    /**
     * Reset forgotten user's password using token authentication
     * @param request ResetPasswordRequest [token, newPassword]
     * @return ResponseEntity ?
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Password reset successfully");
    }

    /**
     * Change user's role
     * @param userEmail String
     * @param role Role [ADMIN, SALESMAN, CUSTOMER]
     * @apiNote PATCH
     * @return User
     */
    @PatchMapping("/change-role")
    public User updateUserRole(@RequestParam("email") String userEmail, @RequestParam("role") Role role) {
        return userService.updateUserRole(userEmail, role);
    }

    /**
     * Verify a new user's account using verification token sent to user's email
     * @param request VerifyEmailRequest token
     * @return ResponseEntity ?
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestBody VerifyEmailRequest request) {
        userService.verifyEmail(request.getToken());
        return ResponseEntity.ok("Email has been verified successfully!");
    }

    /**
     * Deactivate a user's account
     * @param userId Long
     * @return ResponseEntity ?
     */
    @PatchMapping("/soft-delete/{userId}")
    public ResponseEntity<?> softDeleteUser(@PathVariable Long userId) {
        userService.softDeleteUser(userId);
        return ResponseEntity.ok("User soft-deleted successfully");
    }

    /**
     * Reactivate inactive (soft deleted) user account.
     * @param userId Long
     * @return User
     */
    @PatchMapping("/reactivate/{userId}")
    public User reactivateUserAccount(@PathVariable Long userId) {
        return userService.reactivateUserAccount(userId);
    }

    /**
     * Get user's info by their ID
     * @param userId Long
     * @return User
     */
    @GetMapping("/{userId}")
    public User getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    /**
     * Download stored user's CPR image.
     * @param userId Long
     * @return ResponseEntity Resource The image
     */
    @GetMapping("/image/{userId}")
    public ResponseEntity<Resource> getCPRImage(@PathVariable("userId") Long userId) {
        return userService.downloadCPRImage(userId);
    }
}
