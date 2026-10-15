package com.eventflow.controller;

import com.eventflow.dto.BookingRequestDto;
import com.eventflow.entity.Event;
import com.eventflow.entity.EventRegistration;
import com.eventflow.entity.TicketType;
import com.eventflow.entity.User;
import com.eventflow.security.CustomUserDetails;
import com.eventflow.service.EventService;
import com.eventflow.service.RegistrationService;
import com.eventflow.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final EventService eventService;
    private final TicketService ticketService;
    private final RegistrationService registrationService;

    public CheckoutController(EventService eventService,
                              TicketService ticketService,
                              RegistrationService registrationService) {
        this.eventService = eventService;
        this.ticketService = ticketService;
        this.registrationService = registrationService;
    }

    @GetMapping("/{eventId}")
    public String checkoutPage(@PathVariable Long eventId,
                               @RequestParam(required = false) Long ticketTypeId,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {
        Event event = eventService.findById(eventId);
        List<TicketType> ticketTypes = ticketService.getTicketTypesForEvent(event);

        BookingRequestDto bookingDto = new BookingRequestDto();
        bookingDto.setEventId(eventId);
        if (ticketTypeId != null) {
            bookingDto.setTicketTypeId(ticketTypeId);
        } else if (!ticketTypes.isEmpty()) {
            bookingDto.setTicketTypeId(ticketTypes.get(0).getId());
        }

        User user = userDetails.getUser();
        bookingDto.setAttendeeName(user.getFullName());
        bookingDto.setAttendeeEmail(user.getEmail());
        bookingDto.setAttendeePhone(user.getPhone());

        model.addAttribute("event", event);
        model.addAttribute("ticketTypes", ticketTypes);
        model.addAttribute("bookingDto", bookingDto);
        return "checkout/checkout";
    }

    @PostMapping("/process")
    public String processBooking(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @Valid @ModelAttribute("bookingDto") BookingRequestDto bookingDto,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            Event event = eventService.findById(bookingDto.getEventId());
            model.addAttribute("event", event);
            model.addAttribute("ticketTypes", ticketService.getTicketTypesForEvent(event));
            return "checkout/checkout";
        }

        try {
            EventRegistration registration = registrationService.processBooking(userDetails.getUser(), bookingDto);
            return "redirect:/checkout/success/" + registration.getRegistrationCode();
        } catch (Exception ex) {
            Event event = eventService.findById(bookingDto.getEventId());
            model.addAttribute("event", event);
            model.addAttribute("ticketTypes", ticketService.getTicketTypesForEvent(event));
            model.addAttribute("errorMessage", ex.getMessage());
            return "checkout/checkout";
        }
    }

    @GetMapping("/success/{code}")
    public String successPage(@PathVariable String code,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model) {
        EventRegistration registration = registrationService.findByCode(code);
        model.addAttribute("registration", registration);
        model.addAttribute("event", registration.getEvent());
        model.addAttribute("tickets", registrationService.getTicketsForRegistration(registration));
        return "checkout/success";
    }
}
