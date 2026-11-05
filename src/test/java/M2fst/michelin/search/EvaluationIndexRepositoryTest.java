package M2fst.michelin.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvaluationIndexRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void should_find_an_evaluation_by_a_word() {
        EvaluationIndexRepository repository = new EvaluationIndexRepository(this.tempDir.toString());
        try {
            repository.index(1, "Excellent saumon grille");
            repository.index(2, "Juste un cafe");

            List<Integer> result = repository.searchIds("saumon");

            assertEquals(1, result.size());
            assertEquals(1, result.get(0));
        } finally {
            repository.close();
        }
    }

    @Test
    void should_find_evaluations_matching_one_of_the_words() {
        EvaluationIndexRepository repository = new EvaluationIndexRepository(this.tempDir.toString());
        try {
            repository.index(1, "Excellent saumon");
            repository.index(2, "Tarte au citron");
            repository.index(3, "Rien a signaler");

            List<Integer> result = repository.searchIds("saumon citron");

            assertEquals(2, result.size());
            assertTrue(result.contains(1));
            assertTrue(result.contains(2));
        } finally {
            repository.close();
        }
    }
}
