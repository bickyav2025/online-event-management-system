package com.eventflow.entity;

import com.eventflow.entity.enums.TicketTier;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_types")
public class TicketType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketTier type;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Column(name = "max_per_attendee", nullable = false)
    private Integer maxPerAttendee = 5;

    @Column(name = "sales_start")
    private LocalDateTime salesStart;

    @Column(name = "sales_end")
    private LocalDateTime salesEnd;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.price == null) {
            this.price = BigDecimal.ZERO;
        }
        if (this.availableQuantity == null) {
            this.availableQuantity = this.totalQuantity;
        }
    }

    public TicketType() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

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

    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }

    public Integer getMaxPerAttendee() { return maxPerAttendee; }
    public void setMaxPerAttendee(Integer maxPerAttendee) { this.maxPerAttendee = maxPerAttendee; }

    public LocalDateTime getSalesStart() { return salesStart; }
    public void setSalesStart(LocalDateTime salesStart) { this.salesStart = salesStart; }

    public LocalDateTime getSalesEnd() { return salesEnd; }
    public void setSalesEnd(LocalDateTime salesEnd) { this.salesEnd = salesEnd; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isFree() {
        return price == null || price.compareTo(BigDecimal.ZERO) == 0;
    }

    public int getSoldQuantity() {
        return (totalQuantity != null ? totalQuantity : 0) - (availableQuantity != null ? availableQuantity : 0);
    }
}
