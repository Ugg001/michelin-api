package M2fst.michelin.controllers;

import M2fst.michelin.dto.ImageLink;
import M2fst.michelin.services.RestaurantImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class RestaurantImageController {

    private final RestaurantImageService restaurantImageService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/restaurant/{id}/image")
    public ResponseEntity<ImageLink> upload(@PathVariable(name = "id") int id) {
        return ResponseEntity.ok(this.restaurantImageService.requestUpload(id));
    }

    @GetMapping("/restaurant/{id}/image")
    public ResponseEntity<ImageLink> download(@PathVariable(name = "id") int id) {
        return ResponseEntity.ok(this.restaurantImageService.requestDownload(id));
    }
}
