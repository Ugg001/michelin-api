package M2fst.michelin.services;

import M2fst.michelin.dto.EvaluationDto;
import M2fst.michelin.entity.EvaluationEntity;
import M2fst.michelin.entity.RestaurantEntity;
import M2fst.michelin.exceptions.InvalidEvaluationException;
import M2fst.michelin.exceptions.NotFoundException;
import M2fst.michelin.repository.EvaluationRepository;
import M2fst.michelin.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final RestaurantRepository restaurantRepository;

    @Transactional
    public EvaluationDto add(int restaurantId, String nomEvaluateur, String commentaire, Integer note) {
        this.check(nomEvaluateur, commentaire, note);
        RestaurantEntity restaurant = this.restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found"));
        EvaluationEntity entity = new EvaluationEntity();
        entity.setNomEvaluateur(nomEvaluateur);
        entity.setCommentaire(commentaire);
        entity.setNote(note);
        entity.setRestaurant(restaurant);
        return toDto(this.evaluationRepository.save(entity));
    }

    @Transactional
    public EvaluationDto update(int restaurantId, int evaluationId, String commentaire, Integer note) {
        this.checkComment(commentaire);
        this.checkNote(note);
        EvaluationEntity entity = this.findEvaluation(restaurantId, evaluationId);
        entity.setCommentaire(commentaire);
        entity.setNote(note);
        return toDto(this.evaluationRepository.save(entity));
    }

    @Transactional
    public void delete(int restaurantId, int evaluationId) {
        EvaluationEntity entity = this.findEvaluation(restaurantId, evaluationId);
        this.evaluationRepository.delete(entity);
    }

    private EvaluationEntity findEvaluation(int restaurantId, int evaluationId) {
        EvaluationEntity entity = this.evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new NotFoundException("Evaluation not found"));
        if (entity.getRestaurant() == null || entity.getRestaurant().getId() != restaurantId) {
            throw new NotFoundException("Evaluation not found");
        }
        return entity;
    }

    private void check(String nomEvaluateur, String commentaire, Integer note) {
        if (nomEvaluateur == null || nomEvaluateur.isBlank()) {
            throw new InvalidEvaluationException("Le nom de l'évaluateur ne doit pas être vide");
        }
        if (nomEvaluateur.length() > 50) {
            throw new InvalidEvaluationException("Le nom de l'évaluateur doit comporter au maximum 50 caractères.");
        }
        this.checkComment(commentaire);
        this.checkNote(note);
    }

    private void checkComment(String commentaire) {
        if (commentaire == null || commentaire.isBlank()) {
            throw new InvalidEvaluationException("Le commentaire ne doit pas être vide");
        }
        if (commentaire.length() > 255) {
            throw new InvalidEvaluationException("Le commentaire doit comporter au maximum 255 caractères.");
        }
    }

    private void checkNote(Integer note) {
        if (note == null || note < 0 || note > 3) {
            throw new InvalidEvaluationException("La note doit être 0, 1, 2 ou 3");
        }
    }

    private static EvaluationDto toDto(EvaluationEntity entity) {
        return EvaluationDto.builder()
                .id(entity.getId())
                .nomEvaluateur(entity.getNomEvaluateur())
                .commentaire(entity.getCommentaire())
                .note(entity.getNote())
                .build();
    }
}
