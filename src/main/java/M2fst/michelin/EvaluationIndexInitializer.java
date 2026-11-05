package M2fst.michelin;

import M2fst.michelin.entity.EvaluationEntity;
import M2fst.michelin.repository.EvaluationRepository;
import M2fst.michelin.search.EvaluationIndexRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EvaluationIndexInitializer implements ApplicationRunner {

    private final EvaluationRepository evaluationRepository;
    private final EvaluationIndexRepository evaluationIndexRepository;

    @Override
    @Transactional(readOnly = true)
    public void run(ApplicationArguments args) {
        for (EvaluationEntity evaluation : this.evaluationRepository.findAll()) {
            this.evaluationIndexRepository.index(evaluation.getId(), evaluation.getCommentaire());
        }
        log.info("Index Lucene des évaluations synchronisé ({})", this.evaluationRepository.count());
    }
}
