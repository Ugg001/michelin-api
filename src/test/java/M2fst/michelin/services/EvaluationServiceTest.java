package M2fst.michelin.services;

import M2fst.michelin.dto.EvaluationDto;
import M2fst.michelin.entity.EvaluationEntity;
import M2fst.michelin.entity.RestaurantEntity;
import M2fst.michelin.exceptions.InvalidEvaluationException;
import M2fst.michelin.exceptions.NotFoundException;
import M2fst.michelin.repository.EvaluationRepository;
import M2fst.michelin.repository.RestaurantRepository;
import M2fst.michelin.search.EvaluationIndexRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    @InjectMocks
    private EvaluationService evaluationService;

    @Mock
    private EvaluationRepository evaluationRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private EvaluationIndexRepository evaluationIndexRepository;

    @Test
    void should_add_an_evaluation() {
        RestaurantEntity restaurant = new RestaurantEntity();
        restaurant.setId(1);
        when(this.restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(this.evaluationRepository.save(any(EvaluationEntity.class))).thenAnswer(invocation -> {
            EvaluationEntity entity = invocation.getArgument(0);
            entity.setId(4);
            return entity;
        });

        EvaluationDto result = this.evaluationService.add(1, "Lucien", "Excellent saumon", 3);

        assertEquals(4, result.getId());
        assertEquals("Lucien", result.getNomEvaluateur());
        assertEquals("Excellent saumon", result.getCommentaire());
        assertEquals(3, result.getNote());
        verify(this.restaurantRepository, times(1)).findById(1);
        verify(this.evaluationRepository, times(1)).save(any(EvaluationEntity.class));
    }

    @Test
    void should_reject_a_blank_comment_without_calling_the_database() {
        assertThrows(InvalidEvaluationException.class,
                () -> this.evaluationService.add(1, "Lucien", "   ", 2));
        verifyNoInteractions(this.evaluationRepository);
        verifyNoInteractions(this.restaurantRepository);
    }

    @Test
    void should_reject_a_name_longer_than_50_characters_without_calling_the_database() {
        assertThrows(InvalidEvaluationException.class,
                () -> this.evaluationService.add(1, "a".repeat(51), "Bon", 1));
        verifyNoInteractions(this.evaluationRepository);
        verifyNoInteractions(this.restaurantRepository);
    }

    @Test
    void should_reject_a_note_above_3_without_calling_the_database() {
        assertThrows(InvalidEvaluationException.class,
                () -> this.evaluationService.add(1, "Lucien", "Trop", 4));
        verifyNoInteractions(this.evaluationRepository);
        verifyNoInteractions(this.restaurantRepository);
    }

    @Test
    void should_reject_a_note_below_0_without_calling_the_database() {
        assertThrows(InvalidEvaluationException.class,
                () -> this.evaluationService.update(1, 4, "Correct", -1));
        verifyNoInteractions(this.evaluationRepository);
        verifyNoInteractions(this.restaurantRepository);
    }

    @Test
    void should_not_save_when_the_restaurant_is_missing() {
        when(this.restaurantRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> this.evaluationService.add(1, "Lucien", "Bon", 2));

        verify(this.restaurantRepository, times(1)).findById(1);
        verify(this.evaluationRepository, never()).save(any());
    }

    @Test
    void should_update_comment_and_note() {
        EvaluationEntity evaluation = this.existingEvaluation();
        this.stubSave();

        EvaluationDto result = this.evaluationService.update(1, 4, "Un peu juste", 1);

        assertEquals("Lucien", result.getNomEvaluateur());
        assertEquals("Un peu juste", result.getCommentaire());
        assertEquals(1, result.getNote());
        assertEquals("Un peu juste", evaluation.getCommentaire());
        assertEquals(1, evaluation.getNote());
    }

    @Test
    void should_hit_the_database_once_for_read_and_once_for_write() {
        this.existingEvaluation();
        this.stubSave();

        this.evaluationService.update(1, 4, "Un peu juste", 1);

        verify(this.evaluationRepository, times(1)).findById(4);
        verify(this.evaluationRepository, times(1)).save(any(EvaluationEntity.class));
        verifyNoInteractions(this.restaurantRepository);
    }

    @Test
    void should_not_write_when_the_evaluation_is_missing() {
        when(this.evaluationRepository.findById(4)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> this.evaluationService.delete(1, 4));

        verify(this.evaluationRepository, times(1)).findById(4);
        verify(this.evaluationRepository, never()).delete(any());
    }

    @Test
    void should_not_delete_an_evaluation_of_another_restaurant() {
        EvaluationEntity evaluation = this.evaluationOnRestaurant(2);
        when(this.evaluationRepository.findById(4)).thenReturn(Optional.of(evaluation));

        assertThrows(NotFoundException.class, () -> this.evaluationService.delete(1, 4));

        verify(this.evaluationRepository, never()).delete(any());
    }

    @Test
    void should_search_evaluations_for_the_given_words() {
        when(this.evaluationIndexRepository.searchIds("saumon citron")).thenReturn(List.of(4));
        when(this.evaluationRepository.findById(4)).thenReturn(Optional.of(this.evaluationOnRestaurant(1)));

        List<EvaluationDto> result = this.evaluationService.search(List.of("saumon", "citron"));

        assertEquals(1, result.size());
        assertEquals("Excellent saumon", result.get(0).getCommentaire());
    }

    @Test
    void should_not_call_lucene_when_the_words_are_blank() {
        List<EvaluationDto> result = this.evaluationService.search(List.of("  "));

        assertTrue(result.isEmpty());
        verifyNoInteractions(this.evaluationIndexRepository);
    }

    @Test
    void should_delete_an_evaluation() {
        EvaluationEntity evaluation = this.existingEvaluation();

        this.evaluationService.delete(1, 4);

        verify(this.evaluationRepository, times(1)).delete(evaluation);
    }

    private EvaluationEntity existingEvaluation() {
        EvaluationEntity evaluation = this.evaluationOnRestaurant(1);
        when(this.evaluationRepository.findById(4)).thenReturn(Optional.of(evaluation));
        return evaluation;
    }

    private void stubSave() {
        when(this.evaluationRepository.save(any(EvaluationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private EvaluationEntity evaluationOnRestaurant(int restaurantId) {
        RestaurantEntity restaurant = new RestaurantEntity();
        restaurant.setId(restaurantId);
        EvaluationEntity evaluation = new EvaluationEntity();
        evaluation.setId(4);
        evaluation.setNomEvaluateur("Lucien");
        evaluation.setCommentaire("Excellent saumon");
        evaluation.setNote(3);
        evaluation.setRestaurant(restaurant);
        return evaluation;
    }
}
