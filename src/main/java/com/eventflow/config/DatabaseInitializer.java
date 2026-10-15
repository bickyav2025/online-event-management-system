package com.eventflow.config;

import com.eventflow.entity.Event;
import com.eventflow.entity.EventCategory;
import com.eventflow.entity.TicketType;
import com.eventflow.entity.User;
import com.eventflow.entity.enums.EventStatus;
import com.eventflow.entity.enums.Role;
import com.eventflow.entity.enums.TicketTier;
import com.eventflow.entity.enums.UserStatus;
import com.eventflow.repository.EventCategoryRepository;
import com.eventflow.repository.EventRepository;
import com.eventflow.repository.TicketTypeRepository;
import com.eventflow.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventCategoryRepository categoryRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(UserRepository userRepository,
                               EventRepository eventRepository,
                               EventCategoryRepository categoryRepository,
                               TicketTypeRepository ticketTypeRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. Initialize Default Users if not present
        User admin = userRepository.findByEmail("admin@eventflow.com").orElseGet(() -> {
            User u = new User("admin@eventflow.com", passwordEncoder.encode("Admin@123"), "Platform Administrator", "+1234567890", "San Francisco", Role.ADMIN);
            u.setStatus(UserStatus.ACTIVE);
            return userRepository.save(u);
        });

        User organizer = userRepository.findByEmail("organizer@eventflow.com").orElseGet(() -> {
            User u = new User("organizer@eventflow.com", passwordEncoder.encode("Organizer@123"), "Sarah Jenkins (EventPro)", "+1987654321", "New York", Role.ORGANIZER);
            u.setStatus(UserStatus.ACTIVE);
            return userRepository.save(u);
        });

        userRepository.findByEmail("attendee@eventflow.com").orElseGet(() -> {
            User u = new User("attendee@eventflow.com", passwordEncoder.encode("Attendee@123"), "Alex Morgan", "+1555666777", "Austin", Role.ATTENDEE);
            u.setStatus(UserStatus.ACTIVE);
            return userRepository.save(u);
        });

        // 2. Initialize Seed Events if database has 0 events
        if (eventRepository.count() == 0) {
            EventCategory techCat = categoryRepository.findBySlug("technology").orElse(null);
            EventCategory musicCat = categoryRepository.findBySlug("music").orElse(null);
            EventCategory bizCat = categoryRepository.findBySlug("business").orElse(null);

            if (techCat != null) {
                Event e1 = new Event();
                e1.setTitle("Global AI & Cloud Innovation Summit 2026");
                e1.setSlug("global-ai-cloud-innovation-summit-2026");
                e1.setShortDescription("Discover breakthroughs in Generative AI, Large Language Models, and Cloud Architecture.");
                e1.setFullDescription("Join over 1,500 industry leaders, researchers, and engineers for an immersive three-day exploration into the forefront of Artificial Intelligence, Kubernetes orchestration, and ethical tech.");
                e1.setCategory(techCat);
                e1.setOrganizer(organizer);
                e1.setBannerUrl("https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=1200&auto=format&fit=crop&q=80");
                e1.setStartTime(LocalDateTime.now().plusDays(15));
                e1.setEndTime(LocalDateTime.now().plusDays(17));
                e1.setVenue("Moscone Convention Center");
                e1.setCity("San Francisco");
                e1.setAddress("747 Howard St, San Francisco, CA 94103");
                e1.setIsOnline(false);
                e1.setMaxCapacity(1500);
                e1.setRegistrationDeadline(LocalDateTime.now().plusDays(14));
                e1.setStatus(EventStatus.PUBLISHED);
                Event savedE1 = eventRepository.save(e1);

                TicketType tt1 = new TicketType();
                tt1.setEvent(savedE1);
                tt1.setName("Early Bird Pass");
                tt1.setType(TicketTier.EARLY_BIRD);
                tt1.setDescription("Access to all general sessions and exhibitor hall.");
                tt1.setPrice(new BigDecimal("149.00"));
                tt1.setTotalQuantity(300);
                tt1.setAvailableQuantity(300);
                ticketTypeRepository.save(tt1);

                TicketType tt2 = new TicketType();
                tt2.setEvent(savedE1);
                tt2.setName("VIP All-Access & Networking Pass");
                tt2.setType(TicketTier.VIP);
                tt2.setDescription("VIP lounge, speaker meet-and-greet, and exclusive dinner gala.");
                tt2.setPrice(new BigDecimal("499.00"));
                tt2.setTotalQuantity(100);
                tt2.setAvailableQuantity(100);
                ticketTypeRepository.save(tt2);
            }

            if (musicCat != null) {
                Event e2 = new Event();
                e2.setTitle("Sunset Symphony & Electronic Beats Festival");
                e2.setSlug("sunset-symphony-electronic-beats-festival");
                e2.setShortDescription("An unforgettable fusion of live orchestral symphony and melodic electronic music under the stars.");
                e2.setFullDescription("Experience world-class acoustic ensembles harmonized with groundbreaking synthesized visual light installations along the waterfront amphitheater.");
                e2.setCategory(musicCat);
                e2.setOrganizer(organizer);
                e2.setBannerUrl("https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=1200&auto=format&fit=crop&q=80");
                e2.setStartTime(LocalDateTime.now().plusDays(25));
                e2.setEndTime(LocalDateTime.now().plusDays(25).plusHours(8));
                e2.setVenue("Pier 17 Open Air Pavilion");
                e2.setCity("New York");
                e2.setAddress("89 South St, New York, NY 10038");
                e2.setIsOnline(false);
                e2.setMaxCapacity(2000);
                e2.setRegistrationDeadline(LocalDateTime.now().plusDays(24));
                e2.setStatus(EventStatus.PUBLISHED);
                Event savedE2 = eventRepository.save(e2);

                TicketType tt3 = new TicketType();
                tt3.setEvent(savedE2);
                tt3.setName("General Admission");
                tt3.setType(TicketTier.GENERAL);
                tt3.setDescription("Lawn and standing pavilion access.");
                tt3.setPrice(new BigDecimal("59.00"));
                tt3.setTotalQuantity(1200);
                tt3.setAvailableQuantity(1200);
                ticketTypeRepository.save(tt3);

                TicketType tt4 = new TicketType();
                tt4.setEvent(savedE2);
                tt4.setName("Student Community Pass");
                tt4.setType(TicketTier.FREE);
                tt4.setDescription("Complimentary pass for students with valid student ID.");
                tt4.setPrice(BigDecimal.ZERO);
                tt4.setTotalQuantity(150);
                tt4.setAvailableQuantity(150);
                ticketTypeRepository.save(tt4);
            }

            if (bizCat != null) {
                Event e3 = new Event();
                e3.setTitle("NextGen Venture & Founders Roundtable 2026");
                e3.setSlug("nextgen-venture-founders-roundtable-2026");
                e3.setShortDescription("Connecting venture capital partners with early-stage high-growth founders.");
                e3.setFullDescription("A curated pitch showcase, investor fireside chats, and matchmaking sessions designed for Seed and Series-A stage entrepreneurs.");
                e3.setCategory(bizCat);
                e3.setOrganizer(organizer);
                e3.setBannerUrl("https://images.unsplash.com/photo-1515187029135-18ee286d815b?w=1200&auto=format&fit=crop&q=80");
                e3.setStartTime(LocalDateTime.now().plusDays(35));
                e3.setEndTime(LocalDateTime.now().plusDays(36));
                e3.setVenue("Austin Tech Exchange Hub");
                e3.setCity("Austin");
                e3.setAddress("400 Congress Ave, Austin, TX 78701");
                e3.setIsOnline(true);
                e3.setOnlineLink("https://meet.eventflow.com/founders-roundtable");
                e3.setMaxCapacity(500);
                e3.setRegistrationDeadline(LocalDateTime.now().plusDays(33));
                e3.setStatus(EventStatus.PUBLISHED);
                Event savedE3 = eventRepository.save(e3);

                TicketType tt5 = new TicketType();
                tt5.setEvent(savedE3);
                tt5.setName("Founder Virtual Pass");
                tt5.setType(TicketTier.GENERAL);
                tt5.setDescription("Full digital streaming access and pitch chat access.");
                tt5.setPrice(new BigDecimal("89.00"));
                tt5.setTotalQuantity(400);
                tt5.setAvailableQuantity(400);
                ticketTypeRepository.save(tt5);
            }
        }
    }
}
