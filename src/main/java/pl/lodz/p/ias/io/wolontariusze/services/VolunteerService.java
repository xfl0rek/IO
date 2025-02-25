package pl.lodz.p.ias.io.wolontariusze.services;

import org.springframework.stereotype.Service;
import pl.lodz.p.ias.io.uwierzytelnianie.model.Account;
import pl.lodz.p.ias.io.uwierzytelnianie.model.Role;
import pl.lodz.p.ias.io.uwierzytelnianie.repositories.RoleRepository;
import pl.lodz.p.ias.io.wolontariusze.constants.VolunteerConstants;
import pl.lodz.p.ias.io.wolontariusze.model.VolunteerRepository;

import java.util.List;

@Service
public class VolunteerService {
    VolunteerRepository volunteerRepository;
    RoleRepository roleRepository;
    public VolunteerService(VolunteerRepository volunteerRepository, RoleRepository roleRepository) {
        this.volunteerRepository = volunteerRepository;
        this.roleRepository = roleRepository;
    }


    public List<Account> getAllVolunteers() {
        Role role = roleRepository.findByRoleName(VolunteerConstants.ROLE);
        return volunteerRepository.findAllByRole(role);
    }

    public Account getVolunteerById(Long id) {
        return volunteerRepository.findById(id).orElse(null);
    }

    public Account updateVolunteer(Long id, String firstName, String lastName) {
        Account volunteer = volunteerRepository.findById(id).get();
        if (firstName != null && !firstName.isEmpty()) {
            volunteer.setFirstName(firstName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            volunteer.setLastName(lastName);
        }
//        volunteerRepository.update(volunteer);
        volunteerRepository.save(volunteer);
        return volunteerRepository.findById(id).get();
    }
    public void deleteVolunteer(Long id) {
        volunteerRepository.deleteById(id);
    }
}
