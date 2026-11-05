package M2fst.michelin.services;

import M2fst.michelin.dto.RestaurantDto;
import M2fst.michelin.entity.RestaurantEntity;
import M2fst.michelin.exceptions.InvalidRestaurantException;
import M2fst.michelin.exceptions.NotFoundException;
import M2fst.michelin.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    @Transactional(readOnly = true)
    public List<RestaurantDto> findAll() {
        return this.restaurantRepository.findAll().stream()
                .map(RestaurantService::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public RestaurantDto findById(int id) {
        return this.restaurantRepository.findById(id)
                .map(RestaurantService::toDto)
                .orElseThrow(() -> new NotFoundException("Restaurant not found"));
    }

    @Transactional
    public RestaurantDto create(String nom, String adresse) {
        this.check(nom, adresse);
        RestaurantEntity entity = new RestaurantEntity();
        entity.setNom(nom);
        entity.setAdresse(adresse);
        return toDto(this.restaurantRepository.save(entity));
    }

    @Transactional
    public RestaurantDto update(int id, String nom, String adresse) {
        this.check(nom, adresse);
        RestaurantEntity entity = this.restaurantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Restaurant not found"));
        entity.setNom(nom);
        entity.setAdresse(adresse);
        return toDto(this.restaurantRepository.save(entity));
    }

    // Même idée que BookService.updateBook : on revérifie avant d'aller en base.
    private void check(String nom, String adresse) {
        if (nom == null || nom.isBlank()) {
            throw new InvalidRestaurantException("Le nom du restaurant ne doit pas être vide");
        }
        if (nom.length() > 90) {
            throw new InvalidRestaurantException("Le nom du restaurant doit comporter au maximum 90 caractères.");
        }
        if (adresse == null || adresse.isBlank()) {
            throw new InvalidRestaurantException("L'adresse ne doit pas être vide");
        }
        if (adresse.length() > 255) {
            throw new InvalidRestaurantException("L'adresse doit comporter au maximum 255 caractères.");
        }
    }

    private static RestaurantDto toDto(RestaurantEntity entity) {
        return RestaurantDto.builder()
                .id(entity.getId())
                .nom(entity.getNom())
                .adresse(entity.getAdresse())
                // Pas encore d'évaluation donc on met -1.
                .moyenne(-1)
                .evaluations(List.of())
                .build();
    }
}
