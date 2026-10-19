package tn.esprit.autoloc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tn.esprit.autoloc.domain.CategorieVehicule;
import tn.esprit.autoloc.domain.StatutVehicule;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.repository.VehiculeRepository;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final VehiculeRepository vehiculeRepository;

    public DataInitializer(VehiculeRepository vehiculeRepository) {
        this.vehiculeRepository = vehiculeRepository;
    }

    @Override
    public void run(String... args) {
        if (vehiculeRepository.count() == 0) {
            log.info("Initialisation des données de démonstration pour les véhicules...");

            Vehicule v1 = new Vehicule(
                    null,
                    "215-TUN-1042",
                    "Renault",
                    "Clio 5",
                    CategorieVehicule.CITADINE,
                    new BigDecimal("90.00"),
                    StatutVehicule.DISPONIBLE
            );

            Vehicule v2 = new Vehicule(
                    null,
                    "230-TUN-5589",
                    "Peugeot",
                    "508",
                    CategorieVehicule.BERLINE,
                    new BigDecimal("180.00"),
                    StatutVehicule.DISPONIBLE
            );

            Vehicule v3 = new Vehicule(
                    null,
                    "240-TUN-7812",
                    "Toyota",
                    "RAV4 Hybrid",
                    CategorieVehicule.SUV,
                    new BigDecimal("220.00"),
                    StatutVehicule.MAINTENANCE
            );

            vehiculeRepository.saveAll(List.of(v1, v2, v3));
            log.info("3 véhicules de démonstration insérés avec succès !");
        }
    }
}
