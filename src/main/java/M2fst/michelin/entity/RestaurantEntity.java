package M2fst.michelin.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class RestaurantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 90)
    private String nom;

    @Column(nullable = false, length = 255)
    private String adresse;

    // Clé de l'objet dans le bucket restaurant. L'image elle-même n'est pas en base.
    @Column
    private String imageId;

    // Comme le livre et ses commentaires dans le sujet de demo : un restaurant, plusieurs évaluations.
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EvaluationEntity> evaluations = new ArrayList<>();
}
