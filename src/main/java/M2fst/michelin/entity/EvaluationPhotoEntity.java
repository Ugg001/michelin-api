package M2fst.michelin.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class EvaluationPhotoEntity {

    // Clé de l'objet dans le bucket evaluation. Pas un entier : c'est un UUID, comme coverId.
    @Id
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "evaluation_id", nullable = false)
    private EvaluationEntity evaluation;
}
