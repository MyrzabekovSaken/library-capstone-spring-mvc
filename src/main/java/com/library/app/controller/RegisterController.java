package com.library.app.controller;

import com.library.app.model.User;
import com.library.app.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

/**
 * Controller for user registration.
 */
@Controller
@RequestMapping("/register")
public class RegisterController {
    private static final String USER = "user";
    private static final String SUCCESS = "success";
    private static final String ERROR = "error";
    private static final String REGISTER_SUCCESS = "register.success";
    private static final String REGISTER_ERROR_USERNAME_EXISTS = "register.error.username.exists";

    private final UserService userService;
    private final MessageSource messageSource;


    /**
     * Constructs an instance of {@code RegisterController} and initializes its dependencies.
     *
     * @param userService   the service for managing user registration and account operations
     * @param messageSource The source of the message, such as a user input or system-generated event.
     */
    @Autowired
    public RegisterController(UserService userService, MessageSource messageSource) {
        this.userService = userService;
        this.messageSource = messageSource;
    }

    /**
     * Displays the user registration form.
     *
     * @param model the model to add new user attribute
     * @return the registration view
     */
    @GetMapping
    public String showRegisterForm(Model model) {
        model.addAttribute(USER, new User());

        return "register";
    }

    /**
     * Handles user registration form submission.
     *
     * @param user               the user to register
     * @param redirectAttributes flash attributes for post-redirect message
     * @return redirect to login page
     */
    @PostMapping
    public String registerUser(@Valid @ModelAttribute(USER) User user, BindingResult bindingResult,
                               RedirectAttributes redirectAttributes, Model model, Locale locale) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.register(user);
            String message = messageSource.getMessage(REGISTER_SUCCESS, null, locale);
            redirectAttributes.addFlashAttribute(SUCCESS, message);

            return "redirect:/login";
        } catch (RuntimeException e) {
            String errorMessage = messageSource.getMessage(REGISTER_ERROR_USERNAME_EXISTS, null, locale);
            model.addAttribute(ERROR, errorMessage);
            model.addAttribute(USER, user);

            return "register";
        }
    }
}
