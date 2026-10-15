package com.eventflow.controller;

import com.eventflow.dto.PasswordResetDto;
import com.eventflow.dto.UserRegistrationDto;
import com.eventflow.exception.BadRequestException;
import com.eventflow.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email or password. Please verify your credentials.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been signed out successfully.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("userDto", new UserRegistrationDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("userDto") UserRegistrationDto userDto,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.registerUser(userDto);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please log in with your credentials.");
            return "redirect:/login";
        } catch (BadRequestException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm(Model model) {
        model.addAttribute("resetDto", new PasswordResetDto());
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@Valid @ModelAttribute("resetDto") PasswordResetDto resetDto,
                                        BindingResult bindingResult,
                                        RedirectAttributes redirectAttributes,
                                        Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/forgot-password";
        }

        try {
            userService.resetPassword(resetDto.getEmail(), resetDto.getNewPassword());
            redirectAttributes.addFlashAttribute("successMessage", "Password has been successfully reset! You can now log in.");
            return "redirect:/login";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "No account found with this email address.");
            return "auth/forgot-password";
        }
    }
}
