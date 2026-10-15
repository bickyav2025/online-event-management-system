package com.eventflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AnnouncementDto {

    @NotNull(message = "Please select an event")
    private Long eventId;

    @NotBlank(message = "Announcement title is required")
    private String title;

    @NotBlank(message = "Announcement message content is required")
    private String message;

    public AnnouncementDto() {}

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
