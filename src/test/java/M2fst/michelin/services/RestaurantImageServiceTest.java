package M2fst.michelin.services;

import M2fst.michelin.dto.ImageLink;
import M2fst.michelin.entity.RestaurantEntity;
import M2fst.michelin.exceptions.NotFoundException;
import M2fst.michelin.repository.RestaurantRepository;
import M2fst.michelin.storage.S3Storage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantImageServiceTest {

    @InjectMocks
    private RestaurantImageService restaurantImageService;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private S3Storage restaurantStorage;

    @Test
    void should_store_an_image_key_and_return_an_upload_link() {
        RestaurantEntity restaurant = new RestaurantEntity();
        restaurant.setId(1);
        when(this.restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(this.restaurantStorage.createUploadUrl(any())).thenReturn("http://s3/upload");

        ImageLink link = this.restaurantImageService.requestUpload(1);

        assertEquals("http://s3/upload", link.getUrl());
        assertEquals(link.getId(), restaurant.getImageId());
        verify(this.restaurantRepository).save(restaurant);
    }

    @Test
    void should_not_call_s3_when_the_restaurant_is_missing() {
        when(this.restaurantRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> this.restaurantImageService.requestUpload(1));
        verify(this.restaurantStorage, never()).createUploadUrl(any());
    }

    @Test
    void should_return_a_download_link() {
        RestaurantEntity restaurant = new RestaurantEntity();
        restaurant.setId(1);
        restaurant.setImageId("image-1");
        when(this.restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(this.restaurantStorage.createDownloadUrl("image-1")).thenReturn("http://s3/image-1");

        ImageLink link = this.restaurantImageService.requestDownload(1);

        assertEquals("image-1", link.getId());
        assertEquals("http://s3/image-1", link.getUrl());
    }

    @Test
    void should_reject_a_download_when_there_is_no_image() {
        RestaurantEntity restaurant = new RestaurantEntity();
        restaurant.setId(1);
        when(this.restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));

        assertThrows(NotFoundException.class, () -> this.restaurantImageService.requestDownload(1));
        verifyNoInteractions(this.restaurantStorage);
    }
}
