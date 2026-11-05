package M2fst.michelin.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EvaluationDto {

    private Integer id;
    private String nomEvaluateur;
    private String commentaire;
    private int note;
    private List<String> photoIds;
}
