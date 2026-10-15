package com.eventflow.entity.enums;

public enum Role {
    ADMIN,
    ORGANIZER,
    ATTENDEE;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
