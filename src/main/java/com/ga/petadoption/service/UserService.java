package com.ga.petadoption.service;

import com.ga.petadoption.exception.AccessDeniedException;
import com.ga.petadoption.exception.AuthenticationException;
import com.ga.petadoption.exception.InformationExistException;
import com.ga.petadoption.exception.InformationNotFoundException;
import com.ga.petadoption.model.*;
import com.ga.petadoption.model.enums.Role;
import com.ga.petadoption.model.enums.UserStatus;
import com.ga.petadoption.model.request.ChangePasswordRequest;
import com.ga.petadoption.model.request.LoginRequest;
import com.ga.petadoption.model.response.ChangePasswordResponse;
import com.ga.petadoption.model.response.LoginResponse;
import com.ga.petadoption.repository.EmailVerificationTokenRepository;
import com.ga.petadoption.repository.PasswordResetTokenRepository;
import com.ga.petadoption.repository.UserRepository;
import com.ga.petadoption.security.JWTUtils;
import com.ga.petadoption.security.MyUserDetails;
import com.ga.petadoption.utility.Uploads;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private MyUserDetails myUserDetails;
    private PasswordResetTokenRepository passwordResetTokenRepository;
    private JavaMailSender mailSender;
    private EmailVerificationTokenRepository emailVerificationTokenRepository;
    final String uploadImagePath = "uploads/cpr-images";
    private final Uploads uploads;

    @Autowired
    public UserService(UserRepository userRepository,
                       @Lazy PasswordEncoder passwordEncoder,
                       JWTUtils jwtUtils,
                       @Lazy AuthenticationManager authenticationManager,
                       @Lazy MyUserDetails myUserDetails,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       @Lazy JavaMailSender mailSender,
                       EmailVerificationTokenRepository emailVerificationTokenRepository,
                       Uploads uploads) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.myUserDetails = myUserDetails;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.mailSender = mailSender;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.uploads = uploads;
    }

    public User createUser(User userObject) {
        System.out.println("service calling createUser ==> ");

        if(!userRepository.existsByEmailAddress(userObject.getEmailAddress())){
            userObject.setPassword(passwordEncoder.encode(userObject.getPassword()));

            userObject.setVerified(false);
            userObject.setUserStatus(UserStatus.ACTIVE);
            userObject.setRole(Role.CUSTOMER); // Default user role
            User savedUser = userRepository.save(userObject);

            EmailVerificationToken token = new EmailVerificationToken();
            token.setToken(UUID.randomUUID().toString());
            token.setUser(savedUser);
            token.setExpiryDate(LocalDateTime.now().plusMinutes(15));

            emailVerificationTokenRepository.save(token);
            sendVerificationEmail(userObject.getEmailAddress(), token.getToken());

            return savedUser;
        } else {
            throw new InformationExistException("User with email address " + userObject.getEmailAddress() + " already exists.");
        }
    }

    public User findUserByEmailAddress(String email) {
        return userRepository.findUserByEmailAddress(email);
    }

    public ResponseEntity<?> loginUser(LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getEmailAddress(), loginRequest.getPassword());
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmailAddress(), loginRequest.getPassword()));
            System.out.println("authentication :: "+authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            myUserDetails = (MyUserDetails) authentication.getPrincipal();
            System.out.println("myUserDetails :::: "+myUserDetails.getUsername());

            // Check if the user is inactive
            if (myUserDetails.getUser().getUserStatus() == UserStatus.INACTIVE) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("Error: This account has been deactivated. Please contact an admin for support.");
            }

            // Check if the user is verified
            if (!myUserDetails.getUser().getVerified()) { // assuming 'enabled' = email verified
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("Error: Email not verified. Please verify your email before logging in.");
            }

            final String JWT = jwtUtils.generateJwtToken(myUserDetails);
            System.out.println("jwt"+JWT);

            return ResponseEntity.ok(new LoginResponse(JWT));
        } catch (Exception e) {
            return ResponseEntity.ok(new LoginResponse("Error : user name or password is incorrect"));
        }
    }

    /**
     * Get current logged in user
     * @return User
     */
    public static User getCurrentLoggedInUser() {
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assert userDetails != null;
        return userDetails.getUser();
    }

    public ChangePasswordResponse changePassword(ChangePasswordRequest changePasswordRequest) {
        User user = userRepository.findUserByEmailAddress(UserService.getCurrentLoggedInUser().getEmailAddress());

        if (user.getUserStatus() == UserStatus.INACTIVE) throw new AccessDeniedException("This account has been deactivated. Please contact an admin for support.");

        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
            return new ChangePasswordResponse("Old password incorrect");
        }

        if (passwordEncoder.matches(changePasswordRequest.getNewPassword(), user.getPassword())) {
            return new ChangePasswordResponse("New password cannot be the same as old password");
        } else {
            user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
            userRepository.save(user);
            return new ChangePasswordResponse("Password for " + user.getEmailAddress() + " has been changed successfully!");
        }
    }

    public UserProfile updateProfile(UserProfile userProfile, MultipartFile profilePic) throws IOException {
        User user = userRepository.findUserByEmailAddress(UserService.getCurrentLoggedInUser().getEmailAddress());

        if (user.getUserStatus().equals(UserStatus.INACTIVE)) {
            throw new AccessDeniedException("This account has been deactivated. Please contact an admin for support.");
        }

        UserProfile profile = user.getUserProfile();

        profile.setFirstName(userProfile.getFirstName());
        profile.setLastName(userProfile.getLastName());
        profile.setPhoneNumber(userProfile.getPhoneNumber());

        if (profilePic != null && !profilePic.isEmpty()) {
            if (profile.getProfilePic() != null) {
                uploads.deleteImage(uploadImagePath, profile.getProfilePic());
            }
            String newProfilePic = uploads.uploadImage(uploadImagePath, profilePic);
            profile.setProfilePic(newProfilePic);
        }

        userRepository.save(user);
        return profile;
    }

    @Transactional
    public void forgotPassword(String emailAddress) {
        User user = userRepository.findUserByEmailAddress(emailAddress);
        if (user == null) throw new InformationNotFoundException("User with email address " + emailAddress + " not found");

        if (user.getUserStatus().equals(UserStatus.INACTIVE)) throw new AccessDeniedException("This account has been deactivated. Please contact an admin for support.");

        passwordResetTokenRepository.deleteByUser(user);
        passwordResetTokenRepository.flush();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(UUID.randomUUID().toString());
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));

        passwordResetTokenRepository.save(resetToken);

        sendResetEmail(user.getEmailAddress(), resetToken.getToken());
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new AuthenticationException("Invalid token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new AuthenticationException("Token expired");
        }

        User user = resetToken.getUser();

        if (user.getUserStatus().equals(UserStatus.INACTIVE)) throw new AccessDeniedException("This account has been deactivated. Please contact an admin for support.");

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }

    private void sendResetEmail(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);

        message.setFrom("no-reply@showroom.com");

        message.setSubject("Password Reset Request");
        message.setText("Reset your password using this token:\n" + token);

        mailSender.send(message);
    }

    private void sendVerificationEmail(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);

        message.setFrom("no-reply@showroom.com");

        message.setSubject("Verify Email Request");
        message.setText("Verify your email using this token:\n" + token);

        mailSender.send(message);
    }

    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new AuthenticationException("Invalid verification token"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new AuthenticationException("Verification token expired");
        }

        User user = verificationToken.getUser();

        if (user.getUserStatus().equals(UserStatus.INACTIVE)) throw new AccessDeniedException("This account has been deactivated. Please contact an admin for support.");

        user.setVerified(true);

        userRepository.save(user);
        emailVerificationTokenRepository.delete(verificationToken);
    }

    /**
     * Update existing user's role
     * @param emailAddress String
     * @param role Role [ADMIN, SALESMAN, CUSTOMER]
     * @return User
     */
    public User updateUserRole(String emailAddress, Role role) {
        if (!getCurrentLoggedInUser().getRole().equals(Role.ADMIN))
            throw new AccessDeniedException("Only an admin is authorized to change user roles.");

        User user = findUserByEmailAddress(emailAddress);

        if (user == null) throw new InformationNotFoundException("User with email address " + emailAddress + " not found.");

        if (user.getUserStatus().equals(UserStatus.INACTIVE)) throw new AccessDeniedException("This account has been deactivated. Please contact an admin for support.");

        user.setRole(role);

        return userRepository.save(user);
    }

    public void softDeleteUser(Long userId) {
        if (!getCurrentLoggedInUser().getRole().equals(Role.ADMIN))
            throw new AccessDeniedException("Only an admin is authorized to change user status.");

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new InformationNotFoundException("User not found"));


        if (user.getUserStatus().equals(UserStatus.INACTIVE)) throw new AccessDeniedException("This account has already been deactivated.");

        user.setUserStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    /**
     * Reset user account status back to active.
     * @param userId Long
     * @return User
     */
    public User reactivateUserAccount(Long userId) {
        if (!getCurrentLoggedInUser().getRole().equals(Role.ADMIN))
            throw new AccessDeniedException("Only an admin is authorized to change user status.");

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new InformationNotFoundException("User with ID " + userId + " not found"));

        if (user.getUserStatus().equals(UserStatus.ACTIVE)) throw new AccessDeniedException("This account is already activated.");

        user.setUserStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    /**
     * Get user's info by their ID
     * @param userId Long
     * @return User
     */
    public User getUserById(Long userId) {
        if (getCurrentLoggedInUser().getUserStatus().equals(UserStatus.INACTIVE)) throw new AccessDeniedException("This account has been deactivated. Please contact an admin for support.");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InformationNotFoundException("User with ID " + userId + " not found"));

        if (!Objects.equals(user.getId(), getCurrentLoggedInUser().getId()) && getCurrentLoggedInUser().getRole().equals(Role.CUSTOMER))
            throw new AccessDeniedException("You are not authorized to view this user's data. Please contact an admin, salesman or the account owner for support.");

        return  user;
    }

    /**
     * Download stored user's Profile Pic
     * @param userId Long
     * @return ResponseEntity Resource The stored image if any [PNG, JPEG]
     */
    public ResponseEntity<Resource> downloadCPRImage(Long userId) {
        if (getCurrentLoggedInUser().getUserStatus().equals(UserStatus.INACTIVE)) throw new AccessDeniedException("This account has been deactivated. Please contact an admin for support.");

        User user = getUserById(userId);

        if (user == null) throw new InformationNotFoundException("User with ID " + userId + " not found");

        if (!Objects.equals(user.getId(), getCurrentLoggedInUser().getId()) && getCurrentLoggedInUser().getRole().equals(Role.CUSTOMER))
            throw new AccessDeniedException("You are not authorized to view this user's data. Please contact an admin, salesman or the account owner for support.");

        return uploads.downloadImage(uploadImagePath, user.getUserProfile().getProfilePic());
    }
}
