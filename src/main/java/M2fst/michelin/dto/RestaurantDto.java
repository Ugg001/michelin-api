package M2fst.michelin.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RestaurantDto {

    private Integer id;
    private String nom;
    private String adresse;
    private double moyenne;
    private List<EvaluationDto> evaluations;
}
