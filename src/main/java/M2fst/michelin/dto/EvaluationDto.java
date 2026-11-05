package M2fst.michelin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EvaluationDto {

    private Integer id;
    private String nomEvaluateur;
    private String commentaire;
    private int note;
}
