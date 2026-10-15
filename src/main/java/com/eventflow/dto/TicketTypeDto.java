package com.eventflow.dto;

import com.eventflow.entity.enums.TicketTier;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TicketTypeDto {

    private Long id;

    @NotNull
    private Long eventId;

    @NotBlank(message = "Ticket tier name is required")
    private String name;

    @NotNull(message = "Ticket type is required")
    private TicketTier type = TicketTier.GENERAL;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price cannot be negative")
    private BigDecimal price = BigDecimal.ZERO;

    @NotNull(message = "Total quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer totalQuantity;

    @NotNull
    @Min(value = 1)
    private Integer maxPerAttendee = 5;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime salesStart;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime salesEnd;

    public TicketTypeDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public TicketTier getType() { return type; }
    public void setType(TicketTier type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }

    public Integer getMaxPerAttendee() { return maxPerAttendee; }
    public void setMaxPerAttendee(Integer maxPerAttendee) { this.maxPerAttendee = maxPerAttendee; }

    public LocalDateTime getSalesStart() { return salesStart; }
    public void setSalesStart(LocalDateTime salesStart) { this.salesStart = salesStart; }

    public LocalDateTime getSalesEnd() { return salesEnd; }
    public void setSalesEnd(LocalDateTime salesEnd) { this.salesEnd = salesEnd; }
}
