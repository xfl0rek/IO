package pl.lodz.p.ias.io.darczyncy.config;

import com.google.maps.model.LatLng;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pl.lodz.p.ias.io.darczyncy.model.Donation;
import pl.lodz.p.ias.io.darczyncy.model.FinancialDonation;
import pl.lodz.p.ias.io.darczyncy.model.ItemDonation;
import pl.lodz.p.ias.io.darczyncy.repositories.FinancialDonationRepository;
import pl.lodz.p.ias.io.darczyncy.repositories.ItemDonationRepository;
import pl.lodz.p.ias.io.mapy.model.MapPoint;
import pl.lodz.p.ias.io.mapy.model.PointType;
import pl.lodz.p.ias.io.mapy.repository.MapPointRepository;
import pl.lodz.p.ias.io.poszkodowani.model.FinancialNeed;
import pl.lodz.p.ias.io.poszkodowani.model.MaterialNeed;
import pl.lodz.p.ias.io.poszkodowani.model.Need;
import pl.lodz.p.ias.io.poszkodowani.repository.FinancialNeedRepository;
import pl.lodz.p.ias.io.poszkodowani.repository.MaterialNeedRepository;
import pl.lodz.p.ias.io.uwierzytelnianie.enums.UserRole;
import pl.lodz.p.ias.io.uwierzytelnianie.model.Account;
import pl.lodz.p.ias.io.uwierzytelnianie.model.Role;
import pl.lodz.p.ias.io.uwierzytelnianie.repositories.AccountRepository;
import pl.lodz.p.ias.io.uwierzytelnianie.repositories.RoleRepository;
import pl.lodz.p.ias.io.uwierzytelnianie.services.AuthenticationService;
import pl.lodz.p.ias.io.zasoby.model.Warehouse;
import pl.lodz.p.ias.io.zasoby.repository.WarehouseRepository;
import pl.lodz.p.ias.io.zasoby.utils.ResourceStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Klasa odpowiedzialna za inicjalizację danych w systemie po uruchomieniu aplikacji.
 * Implementuje interfejs {@link CommandLineRunner}, co oznacza, że jej metoda run
 * zostanie wykonana podczas startu aplikacji. Służy do utworzenia przykładowych danych,
 * takich jak użytkownicy, potrzeby finansowe i materialne oraz darowizny.
 */
@Component
@RequiredArgsConstructor
@Order(10)
public class InitData implements CommandLineRunner {

    private final WarehouseRepository warehouseRepository;
    private final FinancialNeedRepository financialNeedRepository;
    private final AuthenticationService authenticationService;
    private final MaterialNeedRepository materialNeedRepository;
    private final ItemDonationRepository itemDonationRepository;
    private final FinancialDonationRepository financialDonationRepository;
    private final MapPointRepository mapPointRepository;

    /**
     * Metoda uruchamiana przy starcie aplikacji. Służy do inicjalizacji przykładowych danych
     * w bazie danych, takich jak użytkownik, potrzeby finansowe i materialne, darowizny
     * rzeczowe oraz finansowe.
     *
     * @param args argumenty wiersza poleceń (nieużywane w tej klasie)
     */
    @Override
    public void run(String... args) {

        // Example objects:
        MapPoint mapPoint1 = new MapPoint(
                new LatLng(40.712776, -74.005974), // New York City coordinates
                "Central Park",
                "A large public park in New York City.",
                PointType.VICTIM
        );

        MapPoint mapPoint2 = new MapPoint(
                new LatLng(48.858844, 2.294351), // Eiffel Tower coordinates
                "Eiffel Tower",
                "An iconic tower located in Paris, France.",
                PointType.VICTIM
        );

        MapPoint warehouseMapPoint = new MapPoint(new LatLng(21.37, 42.0), "Kędzierzyn-Koźle", "Baranie", PointType.WAREHOUSE);

        mapPointRepository.save(warehouseMapPoint);

        mapPointRepository.save(mapPoint1);
        mapPointRepository.save(mapPoint2);
        Account newUser = authenticationService.register("User",
                "Password",
                "Jan",
                "Nowak",
                "DARCZYŃCA");

        FinancialNeed financialNeed = FinancialNeed.builder()
                .collectionGoal(200)
                .collectionStatus(2)
                .description("chora córka")
                .mapPoint(mapPoint1)
                .expirationDate(Date.from(Instant.now().plus(2, ChronoUnit.DAYS)))
                .status(Need.Status.PENDING)
                .priority(2)
                .creationDate(Date.from(Instant.now().minus(1, ChronoUnit.DAYS)))
                .user(newUser)
                .build();
        financialNeedRepository.save(financialNeed);

        Warehouse warehouse = new Warehouse("magazyn", (new LatLng(21.37, 42.0)).toString(),warehouseMapPoint);
        warehouseRepository.save(warehouse);

        MaterialNeed materialNeed = MaterialNeed.builder()
                .itemCategory(MaterialNeed.ItemCategory.HOUSEHOLD)
                .mapPoint(mapPoint2)
                .expirationDate(Date.from(Instant.now().plus(2, ChronoUnit.DAYS)))
                .status(Need.Status.PENDING)
                .priority(2)
                .creationDate(Date.from(Instant.now().minus(1, ChronoUnit.DAYS)))
                .description("czajnik")
                .user(newUser)
                .build();
        materialNeedRepository.save(materialNeed);

        ItemDonation itemDonation = new ItemDonation(
                newUser,
                materialNeed,
                "czajnik",
                418,
                warehouse.getId(),
                ItemDonation.ItemCategory.HOUSEHOLD,
                "bardzo dobry teapot",
                LocalDate.now()
        );

        itemDonation.setResourceStatus(ResourceStatus.ACCEPTED);
        itemDonationRepository.save(itemDonation);

        FinancialDonation financialDonation = new FinancialDonation(
                newUser,
                financialNeed,
                null,
                100,
                FinancialDonation.Currency.PLN,
                LocalDate.now()
        );
        financialDonation.setResourceStatus(ResourceStatus.ACCEPTED);
        financialDonationRepository.save(financialDonation);
    }
}
