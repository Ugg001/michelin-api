package M2fst.michelin.services;

import M2fst.michelin.dto.RestaurantDto;
import M2fst.michelin.entity.EvaluationEntity;
import M2fst.michelin.entity.RestaurantEntity;
import M2fst.michelin.exceptions.InvalidRestaurantException;
import M2fst.michelin.exceptions.NotFoundException;
import M2fst.michelin.repository.RestaurantRepository;
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
class RestaurantServiceTest {

    @InjectMocks
    private RestaurantService restaurantService;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Test
    void should_create_a_restaurant_with_moyenne_minus_one() {
        when(this.restaurantRepository.save(any(RestaurantEntity.class))).thenAnswer(invocation -> {
            RestaurantEntity entity = invocation.getArgument(0);
            entity.setId(1);
            return entity;
        });

        RestaurantDto result = this.restaurantService.create("Le Jules Verne", "Tour Eiffel, Paris");

        assertEquals(1, result.getId());
        assertEquals("Le Jules Verne", result.getNom());
        assertEquals("Tour Eiffel, Paris", result.getAdresse());
        assertEquals(-1.0, result.getMoyenne(), 0.001);
        assertTrue(result.getEvaluations().isEmpty());
        verify(this.restaurantRepository, times(1)).save(any(RestaurantEntity.class));
    }

    @Test
    void should_update_name_and_address() {
        RestaurantEntity restaurant = this.existingRestaurant();

        RestaurantDto result = this.restaurantService.update(1, "Le Cinq", "31 avenue George V");

        assertEquals("Le Cinq", result.getNom());
        assertEquals("31 avenue George V", result.getAdresse());
        assertEquals(-1.0, result.getMoyenne(), 0.001);
        assertEquals("Le Cinq", restaurant.getNom());
        assertEquals("31 avenue George V", restaurant.getAdresse());
    }

    @Test
    void should_accept_a_name_of_90_characters() {
        this.existingRestaurant();
        String nom = "a".repeat(90);

        RestaurantDto result = this.restaurantService.update(1, nom, "1 rue de Metz");

        assertEquals(90, result.getNom().length());
    }

    @Test
    void should_hit_the_database_once_for_read_and_once_for_write() {
        this.existingRestaurant();

        this.restaurantService.update(1, "Le Cinq", "31 avenue George V");

        verify(this.restaurantRepository, times(1)).findById(1);
        verify(this.restaurantRepository, times(1)).save(any(RestaurantEntity.class));
    }

    @Test
    void should_reject_a_blank_name_without_calling_the_database() {
        assertThrows(InvalidRestaurantException.class, () -> this.restaurantService.create("   ", "Paris"));
        verifyNoInteractions(this.restaurantRepository);
    }

    @Test
    void should_reject_a_name_longer_than_90_characters_without_calling_the_database() {
        assertThrows(InvalidRestaurantException.class,
                () -> this.restaurantService.update(1, "a".repeat(91), "Paris"));
        verifyNoInteractions(this.restaurantRepository);
    }

    @Test
    void should_reject_a_blank_address_without_calling_the_database() {
        assertThrows(InvalidRestaurantException.class, () -> this.restaurantService.create("Le Cinq", "  "));
        verifyNoInteractions(this.restaurantRepository);
    }

    @Test
    void should_reject_an_address_longer_than_255_characters_without_calling_the_database() {
        assertThrows(InvalidRestaurantException.class,
                () -> this.restaurantService.create("Le Cinq", "a".repeat(256)));
        verifyNoInteractions(this.restaurantRepository);
    }

    @Test
    void should_not_write_when_the_restaurant_is_missing() {
        when(this.restaurantRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> this.restaurantService.update(1, "Le Cinq", "31 avenue George V"));

        verify(this.restaurantRepository, times(1)).findById(1);
        verify(this.restaurantRepository, never()).save(any());
    }

    @Test
    void should_compute_moyenne_when_the_restaurant_has_evaluations() {
        RestaurantEntity restaurant = new RestaurantEntity();
        restaurant.setId(1);
        restaurant.setNom("Le Cinq");
        restaurant.setAdresse("Paris");
        restaurant.getEvaluations().add(this.evaluation("Lucien", "Tres bon", 3));
        restaurant.getEvaluations().add(this.evaluation("Ada", "Correct", 1));
        when(this.restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));

        RestaurantDto result = this.restaurantService.findById(1);

        assertEquals(2.0, result.getMoyenne(), 0.001);
        assertEquals(2, result.getEvaluations().size());
        assertEquals(3, result.getEvaluations().get(0).getNote());
    }

    @Test
    void should_keep_moyenne_zero_when_the_only_note_is_zero() {
        RestaurantEntity restaurant = new RestaurantEntity();
        restaurant.setId(1);
        restaurant.setNom("Le Cinq");
        restaurant.setAdresse("Paris");
        restaurant.getEvaluations().add(this.evaluation("Lucien", "Decevant", 0));
        when(this.restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));

        RestaurantDto result = this.restaurantService.findById(1);

        assertEquals(0.0, result.getMoyenne(), 0.001);
    }

    @Test
    void should_return_moyenne_minus_one_when_listing() {
        RestaurantEntity restaurant = new RestaurantEntity();
        restaurant.setId(1);
        restaurant.setNom("Le Jules Verne");
        restaurant.setAdresse("Tour Eiffel, Paris");
        when(this.restaurantRepository.findAll()).thenReturn(List.of(restaurant));

        List<RestaurantDto> result = this.restaurantService.findAll();

        assertEquals(1, result.size());
        assertEquals(-1.0, result.get(0).getMoyenne(), 0.001);
        assertTrue(result.get(0).getEvaluations().isEmpty());
    }

    private EvaluationEntity evaluation(String nom, String commentaire, int note) {
        EvaluationEntity evaluation = new EvaluationEntity();
        evaluation.setNomEvaluateur(nom);
        evaluation.setCommentaire(commentaire);
        evaluation.setNote(note);
        return evaluation;
    }

    private RestaurantEntity existingRestaurant() {
        RestaurantEntity restaurant = new RestaurantEntity();
        restaurant.setId(1);
        restaurant.setNom("Le Jules Verne");
        restaurant.setAdresse("Tour Eiffel, Paris");
        when(this.restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(this.restaurantRepository.save(any(RestaurantEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        return restaurant;
    }
}
