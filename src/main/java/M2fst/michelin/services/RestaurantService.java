package M2fst.michelin.services;

import M2fst.michelin.dto.EvaluationDto;
import M2fst.michelin.dto.RestaurantDto;
import M2fst.michelin.entity.EvaluationEntity;
import M2fst.michelin.entity.EvaluationPhotoEntity;
import M2fst.michelin.entity.RestaurantEntity;
import M2fst.michelin.exceptions.InvalidRestaurantException;
import M2fst.michelin.exceptions.NotFoundException;
import M2fst.michelin.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        List<EvaluationEntity> evaluations = entity.getEvaluations();
        return RestaurantDto.builder()
                .id(entity.getId())
                .nom(entity.getNom())
                .adresse(entity.getAdresse())
                .imageId(entity.getImageId())
                .moyenne(moyenne(evaluations))
                .evaluations(toEvaluationDtos(evaluations))
                .build();
    }

    // S'il n'y a aucune évaluation, la moyenne vaut -1.
    private static double moyenne(List<EvaluationEntity> evaluations) {
        if (evaluations == null || evaluations.isEmpty()) {
            return -1;
        }
        int total = 0;
        for (EvaluationEntity evaluation : evaluations) {
            total = total + evaluation.getNote();
        }
        return (double) total / evaluations.size();
    }

    private static List<EvaluationDto> toEvaluationDtos(List<EvaluationEntity> evaluations) {
        List<EvaluationDto> result = new ArrayList<>();
        if (evaluations == null) {
            return result;
        }
        for (EvaluationEntity evaluation : evaluations) {
            result.add(EvaluationDto.builder()
                    .id(evaluation.getId())
                    .nomEvaluateur(evaluation.getNomEvaluateur())
                    .commentaire(evaluation.getCommentaire())
                    .note(evaluation.getNote())
                    .photoIds(photoIds(evaluation))
                    .build());
        }
        return result;
    }

    private static List<String> photoIds(EvaluationEntity evaluation) {
        List<String> ids = new ArrayList<>();
        if (evaluation.getPhotos() == null) {
            return ids;
        }
        for (EvaluationPhotoEntity photo : evaluation.getPhotos()) {
            ids.add(photo.getId());
        }
        return ids;
    }
}
