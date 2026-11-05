package M2fst.michelin.services;

import M2fst.michelin.dto.ImageLink;
import M2fst.michelin.entity.RestaurantEntity;
import M2fst.michelin.exceptions.NotFoundException;
import M2fst.michelin.repository.RestaurantRepository;
import M2fst.michelin.storage.S3Storage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RestaurantImageService {

    private final RestaurantRepository restaurantRepository;
    private final S3Storage restaurantStorage;

    public RestaurantImageService(RestaurantRepository restaurantRepository,
                                  @Qualifier("restaurantBucket") S3Storage restaurantStorage) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantStorage = restaurantStorage;
    }

    // Une seule image. Un nouvel envoi remplace l'identifiant, comme la couverture d'un livre.
    @Transactional
    public ImageLink requestUpload(int restaurantId) {
        RestaurantEntity restaurant = this.restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found"));
        String id = UUID.randomUUID().toString();
        restaurant.setImageId(id);
        this.restaurantRepository.save(restaurant);
        return new ImageLink(id, this.restaurantStorage.createUploadUrl(id));
    }

    @Transactional(readOnly = true)
    public ImageLink requestDownload(int restaurantId) {
        RestaurantEntity restaurant = this.restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found"));
        if (restaurant.getImageId() == null || restaurant.getImageId().isBlank()) {
            throw new NotFoundException("Image not found");
        }
        return new ImageLink(restaurant.getImageId(), this.restaurantStorage.createDownloadUrl(restaurant.getImageId()));
    }
}
