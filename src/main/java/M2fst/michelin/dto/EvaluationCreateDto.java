package M2fst.michelin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationCreateDto {

    // Obligatoire seulement sans JWT. Avec le profil oidc, le nom vient du token.
    @Size(max = 50, message = "doit contenir au plus 50 caractères")
    private String nomEvaluateur;

    @NotBlank(message = "ne doit pas être vide")
    @Size(max = 255, message = "doit contenir au plus 255 caractères")
    private String commentaire;

    @NotNull(message = "ne doit pas être vide")
    @Min(value = 0, message = "doit être 0, 1, 2 ou 3")
    @Max(value = 3, message = "doit être 0, 1, 2 ou 3")
    private Integer note;
}
