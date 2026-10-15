package com.eventflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BookingRequestDto {

    @NotNull(message = "Event is required")
    private Long eventId;

    @NotNull(message = "Ticket type is required")
    private Long ticketTypeId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Please select at least 1 ticket")
    private Integer quantity = 1;

    @NotBlank(message = "Primary attendee name is required")
    private String attendeeName;

    @NotBlank(message = "Attendee email is required")
    @Email(message = "Please enter a valid email")
    private String attendeeEmail;

    private String attendeePhone;

    private String paymentMethod = "mock";

    public BookingRequestDto() {}

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public Long getTicketTypeId() { return ticketTypeId; }
    public void setTicketTypeId(Long ticketTypeId) { this.ticketTypeId = ticketTypeId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getAttendeeName() { return attendeeName; }
    public void setAttendeeName(String attendeeName) { this.attendeeName = attendeeName; }

    public String getAttendeeEmail() { return attendeeEmail; }
    public void setAttendeeEmail(String attendeeEmail) { this.attendeeEmail = attendeeEmail; }

    public String getAttendeePhone() { return attendeePhone; }
    public void setAttendeePhone(String attendeePhone) { this.attendeePhone = attendeePhone; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
