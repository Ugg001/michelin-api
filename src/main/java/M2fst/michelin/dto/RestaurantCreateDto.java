package M2fst.michelin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantCreateDto {

    @NotBlank(message = "ne doit pas être vide")
    @Size(max = 90, message = "doit contenir au plus 90 caractères")
    private String nom;

    @NotBlank(message = "ne doit pas être vide")
    @Size(max = 255, message = "doit contenir au plus 255 caractères")
    private String adresse;
}
