package com.eventflow.controller;

import com.eventflow.entity.Event;
import com.eventflow.entity.EventCategory;
import com.eventflow.service.EventCategoryService;
import com.eventflow.service.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class HomeController {

    private final EventService eventService;
    private final EventCategoryService categoryService;

    public HomeController(EventService eventService, EventCategoryService categoryService) {
        this.eventService = eventService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<EventCategory> categories = categoryService.getActiveCategories();
        List<Event> featuredEvents = eventService.getFeaturedEvents();

        model.addAttribute("categories", categories);
        model.addAttribute("featuredEvents", featuredEvents);
        return "index";
    }

    @GetMapping("/explore")
    public String exploreEvents(@RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String category,
                                @RequestParam(required = false) String city,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "9") int size,
                                Model model) {

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").ascending());

        Page<Event> eventsPage = eventService.searchPublicEvents(keyword, category, city, startDateTime, null, pageable);

        model.addAttribute("events", eventsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", eventsPage.getTotalPages());
        model.addAttribute("totalElements", eventsPage.getTotalElements());
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("keyword", keyword);
        model.addAttribute("city", city);
        model.addAttribute("startDate", startDate);

        return "events/explore";
    }

    @GetMapping("/events/{slug}")
    public String eventDetails(@PathVariable String slug, Model model) {
        Event event = eventService.findBySlug(slug);
        model.addAttribute("event", event);
        model.addAttribute("ticketTypes", event.getTicketTypes());
        return "events/details";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
}
