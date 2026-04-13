package com.arena.ticketing.config;

import com.arena.ticketing.model.*;
import com.arena.ticketing.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    @Profile("dev") // Rulează doar pe profilul de dev (PostgreSQL)
    CommandLineRunner initDatabase(
            StadiumRepository stadiumRepository,
            SectorRepository sectorRepository,
            SeatRepository seatRepository,
            MatchRepository matchRepository,
            MatchSectorPriceRepository priceRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // Verificăm dacă avem deja date ca să nu le duplicăm la fiecare restart
            if (userRepository.count() == 0) {

                System.out.println(">>> Start populare bază de date...");

                // 1. Creăm USER + USER_PROFILE (@OneToOne)
                User user = new User();
                user.setUsername("razvan_admin");
                user.setEmail("razvan@arena.ro");
                user.setPassword(passwordEncoder.encode("parola123"));
                user.setRole("ADMIN");
                user.setEnabled(true);

                UserProfile profile = new UserProfile();
                profile.setFirstName("Răzvan");
                profile.setLastName("Ionescu");
                profile.setPhoneNumber("0722111222");

                // LEAGĂ-LE ÎNTRE ELE (Crucial pentru JPA)
                user.setProfile(profile);
                profile.setUser(user);

                // Salvăm user-ul (va salva și profilul automat datorită CascadeType.ALL)
                userRepository.save(user);

                // 2. Creăm STADIONUL
                Stadium stadium = new Stadium();
                stadium.setName("Arena Nationala");
                stadium.setLocation("Bucuresti");
                stadium = stadiumRepository.save(stadium);

                // 3. Creăm MECIUL
                Match match = new Match();
                match.setOpponentName("Echipa Oaspete FC");
                match.setMatchDate(LocalDateTime.now().plusDays(14)); // Meci peste 2 săptămâni
                match.setStatus(MatchStatus.SCHEDULED);
                match.setStadium(stadium);
                match.setMatchImageUrl("https://link-imagine.com/match.jpg");
                match = matchRepository.save(match);

                // 4. SECTOARE, PREȚURI ȘI LOCURI (Logica ta automată)
                String[] sectorNames = {"Tribuna VIP", "Tribuna 2", "Peluza Nord"};
                Double[] basePrices = {300.0, 100.0, 40.0};

                for (int i = 0; i < sectorNames.length; i++) {
                    Sector sector = new Sector();
                    sector.setName(sectorNames[i]);
                    sector.setStadium(stadium);
                    sector = sectorRepository.save(sector);

                    // Setăm prețul pentru acest sector la acest meci specific
                    MatchSectorPrice msp = new MatchSectorPrice();
                    msp.setMatch(match);
                    msp.setSector(sector);
                    msp.setPrice(basePrices[i]);
                    priceRepository.save(msp);

                    // Generăm 2 rânduri x 5 locuri pentru test (să nu aglomerăm baza acum)
                    for (int r = 1; r <= 2; r++) {
                        for (int l = 1; l <= 5; l++) {
                            Seat seat = new Seat();
                            seat.setRowNumber(r);
                            seat.setSeatNumber(l);
                            seat.setSector(sector);
                            seatRepository.save(seat);
                        }
                    }
                }

                System.out.println("✅ Succes! Tabele create și date inserate (User+Profile, Match, Sectors, Seats).");
            }
        };
    }
}