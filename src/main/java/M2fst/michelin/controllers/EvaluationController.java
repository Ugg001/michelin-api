package M2fst.michelin.controllers;

import M2fst.michelin.dto.EvaluationCreateDto;
import M2fst.michelin.dto.EvaluationDto;
import M2fst.michelin.dto.EvaluationUpdateDto;
import M2fst.michelin.services.EvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class EvaluationController {

    private final EvaluationService evaluationService;

    @GetMapping("/evaluation/search")
    public List<EvaluationDto> search(@RequestParam(name = "q") List<String> mots) {
        return this.evaluationService.search(mots);
    }

    @PostMapping("/restaurant/{id}/evaluation")
    public ResponseEntity<EvaluationDto> add(@PathVariable(name = "id") int id,
                                             @Valid @RequestBody EvaluationCreateDto request) {
        return ResponseEntity.ok(this.evaluationService.add(
                id, request.getNomEvaluateur(), request.getCommentaire(), request.getNote()));
    }

    @PutMapping("/restaurant/{id}/evaluation/{evaluationId}")
    public ResponseEntity<EvaluationDto> update(@PathVariable(name = "id") int id,
                                                @PathVariable(name = "evaluationId") int evaluationId,
                                                @Valid @RequestBody EvaluationUpdateDto request) {
        return ResponseEntity.ok(this.evaluationService.update(
                id, evaluationId, request.getCommentaire(), request.getNote()));
    }

    @DeleteMapping("/restaurant/{id}/evaluation/{evaluationId}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") int id,
                                       @PathVariable(name = "evaluationId") int evaluationId) {
        this.evaluationService.delete(id, evaluationId);
        return ResponseEntity.noContent().build();
    }
}
