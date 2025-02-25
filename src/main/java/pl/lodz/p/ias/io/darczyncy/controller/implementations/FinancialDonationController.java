package pl.lodz.p.ias.io.darczyncy.controller.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.ias.io.darczyncy.controller.interfaces.IFinancialDonationController;
import pl.lodz.p.ias.io.darczyncy.dto.create.FinancialDonationCreateDTO;
import pl.lodz.p.ias.io.darczyncy.dto.exception.ExceptionOutputDTO;
import pl.lodz.p.ias.io.darczyncy.dto.output.FinancialDonationOutputDTO;
import pl.lodz.p.ias.io.darczyncy.exceptions.DonationBaseException;
import pl.lodz.p.ias.io.darczyncy.exceptions.FinancialDonationNotFoundException;
import pl.lodz.p.ias.io.darczyncy.exceptions.PaymentFailedException;
import pl.lodz.p.ias.io.darczyncy.mappers.FinancialDonationMapper;
import pl.lodz.p.ias.io.darczyncy.model.FinancialDonation;
import pl.lodz.p.ias.io.darczyncy.services.interfaces.IFinancialDonationService;
import pl.lodz.p.ias.io.darczyncy.utils.I18n;
import pl.lodz.p.ias.io.poszkodowani.model.FinancialNeed;
import pl.lodz.p.ias.io.poszkodowani.service.FinancialNeedService;
import pl.lodz.p.ias.io.powiadomienia.Interfaces.INotificationService;

import java.net.URI;
import java.util.List;

/**
 * Kontroler REST obsługujący operacje związane z darowiznami finansowymi.
 * Implementuje interfejs {@link IFinancialDonationController}.
 *
 * <p>Odpowiada za tworzenie darowizn, wyszukiwanie darowizn na podstawie różnych kryteriów
 * oraz generowanie potwierdzeń darowizn w formacie PDF.
 */
@RequiredArgsConstructor
@RestController
public class FinancialDonationController implements IFinancialDonationController {

    /**
     * Serwis obsługujący logikę biznesową dla darowizn finansowych.
     */
    private final IFinancialDonationService financialDonationService;

    /**
     * Serwis obsługujący potrzeby finansowe.
     */
    private final FinancialNeedService financialNeedService;

    /**
     * Tworzy nową darowiznę finansową.
     *
     * @param financialDonationCreateDTO dane do utworzenia darowizny finansowej.
     * @return odpowiedź HTTP z kodem 201 (Created) w przypadku sukcesu,
     * lub 503 (Service Unavailable), jeśli płatność się nie powiedzie.
     */
    @PreAuthorize("hasAnyRole('DARCZYŃCA')")
    @Override
    public ResponseEntity<?> createFinancialDonation(String language, FinancialDonationCreateDTO financialDonationCreateDTO) {
        FinancialDonation financialDonation;
        try {
            financialDonation = financialDonationService.createFinancialDonation(financialDonationCreateDTO, language.substring(0,2));
            financialNeedService.updateFinancialNeedCollectionStatus(financialDonation.getNeed().getId(),
                    financialDonation.getAmount());
        }
        catch (PaymentFailedException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        return ResponseEntity.created(URI.create("/donations/%s".formatted(financialDonation.getId()))).build();
    }

    /**
     * Wyszukuje darowiznę finansową na podstawie jej identyfikatora.
     *
     * @param id identyfikator darowizny.
     * @return odpowiedź HTTP z kodem 200 (OK) i danymi darowizny,
     * lub 404 (Not Found), jeśli darowizna nie zostanie znaleziona.
     */
    @PreAuthorize("hasAnyRole('ORGANIZACJA_POMOCOWA', 'PRZEDSTAWICIEL_WŁADZ')")
    @Override
    public ResponseEntity<?> findFinancialDonationById(long id) {
        FinancialDonation financialDonation;
        try {
            financialDonation = financialDonationService.findById(id);
        }
        catch (FinancialDonationNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(convertToOutputDTO(financialDonation));
    }

    /**
     * Konwertuje obiekt {@link FinancialDonation} na DTO {@link FinancialDonationOutputDTO}.
     *
     * @param financialDonation obiekt darowizny finansowej.
     * @return obiekt DTO reprezentujący darowiznę.
     */
    private FinancialDonationOutputDTO convertToOutputDTO(FinancialDonation financialDonation) {
        FinancialNeed financialNeed = financialNeedService.getFinancialNeedById(financialDonation.getNeed().getId())
                .orElseThrow( () -> new DonationBaseException(I18n.FINANCIAL_NEED_NOT_FOUND_EXCEPTION));
        return FinancialDonationMapper.toOutputDTO(financialDonation, financialNeed);
    }

    /**
     * Wyszukuje wszystkie darowizny finansowe przypisane do danego darczyńcy.
     *
     * @param id identyfikator darczyńcy.
     * @return odpowiedź HTTP z listą darowizn lub 204 (No Content), jeśli brak wyników.
     */
    @PreAuthorize("hasAnyRole('ORGANIZACJA_POMOCOWA', 'WOLONTARIUSZ', 'PRZEDSTAWICIEL_WŁADZ')")
    @Override
    public ResponseEntity<?> findAllFinancialDonationsByDonorId(long id) {
        List<FinancialDonation> financialDonations = financialDonationService.findAllByDonorId(id);
        if (financialDonations.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(financialDonations.stream().map(this::convertToOutputDTO));
    }

    /**
     * Wyszukuje wszystkie darowizny finansowe przypisane do danego magazynu.
     *
     * @param id identyfikator magazynu.
     * @return odpowiedź HTTP z listą darowizn lub 204 (No Content), jeśli brak wyników.
     */
    @PreAuthorize("hasAnyRole('ORGANIZACJA_POMOCOWA', 'WOLONTARIUSZ', 'PRZEDSTAWICIEL_WŁADZ')")
    @Override
    public ResponseEntity<?> findAllFinancialDonationsByWarehouseId(long id) {
        List<FinancialDonation> financialDonations = financialDonationService.findAllByWarehouseId(id);
        if (financialDonations.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(financialDonations.stream().map(this::convertToOutputDTO));
    }

    /**
     * Wyszukuje wszystkie darowizny finansowe związane z aktualnie zalogowanym użytkownikiem.
     *
     * @return odpowiedź HTTP z listą darowizn lub 204 (No Content), jeśli brak wyników.
     */
    @PreAuthorize("hasAnyRole('DARCZYŃCA')")
    @Override
    public ResponseEntity<?> findAllFinancialDonationsForCurrentUser() {
        List<FinancialDonation> financialDonations = financialDonationService.findAllForCurrentUser();
        if (financialDonations.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(financialDonations.stream().map(this::convertToOutputDTO));
    }

    /**
     * Wyszukuje wszystkie darowizny finansowe.
     *
     * @return odpowiedź HTTP z listą darowizn lub 204 (No Content), jeśli brak wyników.
     */
    @PreAuthorize("hasAnyRole('ORGANIZACJA_POMOCOWA', 'PRZEDSTAWICIEL_WŁADZ')")
    @Override
    public ResponseEntity<?> findAll() {
        List<FinancialDonation> financialDonations = financialDonationService.findAll();
        if (financialDonations.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(financialDonations.stream().map(this::convertToOutputDTO));
    }

    /**
     * Generuje potwierdzenie darowizny w formacie PDF na podstawie jej identyfikatora.
     *
     * @param language język w jakim ma być wygenerowane potwierdzenie (np. "pl", "en").
     * @param id identyfikator darowizny.
     * @return odpowiedź HTTP z plikiem PDF w przypadku sukcesu,
     * lub 400 (Bad Request) w przypadku błędu.
     */
    @PreAuthorize("hasAnyRole('DARCZYŃCA')")
    @Override
    public ResponseEntity<?> getConfirmationDonationById(String language, long id) {
        byte[] pdfBytes;
        String lang = language.substring(0,2);
        try {
            pdfBytes = financialDonationService.createConfirmationPdf(lang, id);
        } catch (FinancialDonationNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ExceptionOutputDTO(e.getMessage()));
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(pdfBytes.length);
        headers.setContentType(MediaType.APPLICATION_PDF);
        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename("confirmation.pdf").build();
        headers.setContentDisposition(contentDisposition);
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}
