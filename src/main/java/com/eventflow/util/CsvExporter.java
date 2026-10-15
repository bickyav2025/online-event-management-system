package com.eventflow.util;

import com.eventflow.entity.EventRegistration;
import com.opencsv.CSVWriter;

import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CsvExporter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static String exportAttendeesToCsv(List<EventRegistration> registrations) {
        StringWriter sw = new StringWriter();
        try (CSVWriter writer = new CSVWriter(sw)) {
            writer.writeNext(new String[]{
                "Registration Code",
                "Attendee Name",
                "Attendee Email",
                "Event Title",
                "Ticket Type",
                "Quantity",
                "Total Amount",
                "Status",
                "Registration Date"
            });

            for (EventRegistration reg : registrations) {
                writer.writeNext(new String[]{
                    reg.getRegistrationCode(),
                    reg.getAttendee() != null ? reg.getAttendee().getFullName() : "N/A",
                    reg.getAttendee() != null ? reg.getAttendee().getEmail() : "N/A",
                    reg.getEvent() != null ? reg.getEvent().getTitle() : "N/A",
                    reg.getTicketType() != null ? reg.getTicketType().getName() : "N/A",
                    String.valueOf(reg.getQuantity()),
                    "$" + reg.getTotalAmount().toPlainString(),
                    reg.getStatus().name(),
                    reg.getCreatedAt() != null ? reg.getCreatedAt().format(FORMATTER) : ""
                });
            }
        } catch (Exception e) {
            return "";
        }
        return sw.toString();
    }
}
