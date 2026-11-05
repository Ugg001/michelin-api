package M2fst.michelin.controllers;

import M2fst.michelin.dto.RestaurantCreateDto;
import M2fst.michelin.dto.RestaurantDto;
import M2fst.michelin.dto.RestaurantUpdateDto;
import M2fst.michelin.services.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping("/restaurant")
    public List<RestaurantDto> findAll() {
        return this.restaurantService.findAll();
    }

    @GetMapping("/restaurant/{id}")
    public ResponseEntity<RestaurantDto> findById(@PathVariable(name = "id") int id) {
        return ResponseEntity.ok(this.restaurantService.findById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/restaurant")
    public ResponseEntity<RestaurantDto> create(@Valid @RequestBody RestaurantCreateDto request) {
        return ResponseEntity.ok(this.restaurantService.create(request.getNom(), request.getAdresse()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/restaurant/{id}")
    public ResponseEntity<RestaurantDto> update(@PathVariable(name = "id") int id,
                                                @Valid @RequestBody RestaurantUpdateDto request) {
        return ResponseEntity.ok(this.restaurantService.update(id, request.getNom(), request.getAdresse()));
    }
}
