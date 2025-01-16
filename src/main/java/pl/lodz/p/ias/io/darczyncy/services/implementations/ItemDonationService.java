package pl.lodz.p.ias.io.darczyncy.services.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.lodz.p.ias.io.darczyncy.dto.create.ItemDonationCreateDTO;
import pl.lodz.p.ias.io.darczyncy.exceptions.DonationBaseException;
import pl.lodz.p.ias.io.darczyncy.exceptions.ItemDonationNotFoundException;
import pl.lodz.p.ias.io.darczyncy.model.ItemDonation;
import pl.lodz.p.ias.io.darczyncy.providers.CertificateProvider;
import pl.lodz.p.ias.io.darczyncy.repositories.ItemDonationRepository;
import pl.lodz.p.ias.io.darczyncy.services.interfaces.IItemDonationService;
import pl.lodz.p.ias.io.darczyncy.utils.I18n;
import pl.lodz.p.ias.io.poszkodowani.model.MaterialNeed;
import pl.lodz.p.ias.io.poszkodowani.repository.MaterialNeedRepository;

import pl.lodz.p.ias.io.uwierzytelnianie.model.Account;
import pl.lodz.p.ias.io.uwierzytelnianie.repositories.AccountRepository;
import pl.lodz.p.ias.io.zasoby.model.Warehouse;
import pl.lodz.p.ias.io.zasoby.repository.WarehouseRepository;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
@Service
public class ItemDonationService implements IItemDonationService {

    private final ItemDonationRepository itemDonationRepository;

    private final MaterialNeedRepository materialNeedRepository;

    private final WarehouseRepository warehouseRepository;

    private final AccountRepository accountRepository;

    private final CertificateProvider certificateProvider = new CertificateProvider();

    @Override
    public ItemDonation createDonation(ItemDonationCreateDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account donor = accountRepository.findByUsername(auth.getName());
        MaterialNeed need = materialNeedRepository.findById(dto.needId())
                .orElseThrow(() -> new DonationBaseException(I18n.MATERIAL_NEED_NOT_FOUND_EXCEPTION));

        try {
            ItemDonation.ItemCategory.valueOf(dto.category());
        } catch (IllegalArgumentException e) {
            throw new DonationBaseException("Invalid category value");
        }

        List<Warehouse> warehouses = warehouseRepository.findAll();

        // Losowanie magazynu w którym zostanie umieszczona darowizna
        Random rand = new Random();
        Warehouse selectedWarehouse = warehouses.get(rand.nextInt(warehouses.size()));

        ItemDonation itemDonation = new ItemDonation(
                donor,
                need,
                dto.name(),
                dto.resourceQuantity(),
                selectedWarehouse.getId(),
                ItemDonation.ItemCategory.valueOf(dto.category()),
                dto.description(),
                LocalDate.now()
        );
        return itemDonationRepository.save(itemDonation);
    }

    @Override
    public ItemDonation findById(long id) {
        ItemDonation itemDonation = itemDonationRepository.findById(id).orElse(null);
        if (itemDonation == null) {
            throw new ItemDonationNotFoundException();
        }
        return itemDonation;
    }

    @Override
    public List<ItemDonation> findAllItemDonations() {
        return itemDonationRepository.findAll();
    }

    @Override
    public List<ItemDonation> findAllByDonorId(long donorId) {
        accountRepository.findById(donorId).orElseThrow(() -> new DonationBaseException(I18n.DONOR_NOT_FOUND_EXCEPTION));
        return itemDonationRepository.findAllByDonor_Id(donorId);
    }

    @Override
    public List<ItemDonation> findAllByWarehouseId(long warehouseId) {
        warehouseRepository.findById(warehouseId).orElseThrow(() -> new DonationBaseException(I18n.WAREHOUSE_NOT_FOUND_EXCEPTION));
        return itemDonationRepository.findAllByWarehouseId(warehouseId);
    }

    private static void getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass());
        }
    }

    @Override
    public List<ItemDonation> findAllByCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account currentUser = accountRepository.findByUsername(auth.getName());
        return itemDonationRepository.findAllByDonor_Id(currentUser.getId());
    }

    @Override
    public byte[] createConfirmationPdf(String language, long donationId) {
        String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();
        ItemDonation itemDonation = itemDonationRepository.findByIdAndDonor_Username(donationId, currentUserName)
                .orElseThrow(ItemDonationNotFoundException::new);
        // todo check resource status
        return certificateProvider.generateItemCertificate(itemDonation.getDonor(), itemDonation, language);
    }

    @Override
    public ItemDonation update(long id, ItemDonation updatedItemDonation) {
        ItemDonation foundDonation = itemDonationRepository.findById(id).orElseThrow(ItemDonationNotFoundException::new);

        List<Field> fields = new ArrayList<>();

        getAllFields(fields, foundDonation.getClass());

        fields = fields.stream().filter( (field) ->
        {
            try {
                field.setAccessible(true);
                return field.get(updatedItemDonation) != null && !field.getName().equals("id");
            } catch (IllegalAccessException e) {
                throw new DonationBaseException(e.getMessage());
            }
        }).toList();

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                field.set(foundDonation, field.get(updatedItemDonation));
                field.setAccessible(false);
            } catch (IllegalAccessException e) {
                throw new DonationBaseException(e.getMessage());
            }
        }
        return itemDonationRepository.save(foundDonation);
    }

    @Override
    public void deleteById(Long id) {
        ItemDonation itemDonation = itemDonationRepository.findById(id).orElseThrow(ItemDonationNotFoundException::new);
        itemDonationRepository.delete(itemDonation);
    }
}
