package M2fst.michelin.controllers;

import M2fst.michelin.dto.ImageLink;
import M2fst.michelin.services.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class EvaluationPhotoController {

    private final EvaluationService evaluationService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/restaurant/{id}/evaluation/{evaluationId}/photo")
    public ResponseEntity<ImageLink> upload(@PathVariable(name = "id") int id,
                                            @PathVariable(name = "evaluationId") int evaluationId,
                                            Authentication principal) {
        return ResponseEntity.ok(this.evaluationService.requestPhotoUpload(id, evaluationId, principal));
    }

    @GetMapping("/restaurant/{id}/evaluation/{evaluationId}/photo")
    public ResponseEntity<List<ImageLink>> download(@PathVariable(name = "id") int id,
                                                    @PathVariable(name = "evaluationId") int evaluationId) {
        return ResponseEntity.ok(this.evaluationService.requestPhotoDownload(id, evaluationId));
    }
}
