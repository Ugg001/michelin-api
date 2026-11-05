package M2fst.michelin.repository;

import M2fst.michelin.entity.EvaluationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluationRepository extends JpaRepository<EvaluationEntity, Integer> {

    List<EvaluationEntity> findByUserSubject(String userSubject);
}
