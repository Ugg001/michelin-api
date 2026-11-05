package M2fst.michelin.services;

import M2fst.michelin.dto.EvaluationDto;
import M2fst.michelin.dto.ImageLink;
import M2fst.michelin.entity.EvaluationEntity;
import M2fst.michelin.entity.EvaluationPhotoEntity;
import M2fst.michelin.entity.RestaurantEntity;
import M2fst.michelin.exceptions.InvalidEvaluationException;
import M2fst.michelin.exceptions.NotFoundException;
import M2fst.michelin.repository.EvaluationRepository;
import M2fst.michelin.repository.RestaurantRepository;
import M2fst.michelin.search.EvaluationIndexRepository;
import M2fst.michelin.security.AuthenticatedUser;
import M2fst.michelin.storage.S3Storage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final RestaurantRepository restaurantRepository;
    private final EvaluationIndexRepository evaluationIndexRepository;
    private final S3Storage evaluationStorage;

    public EvaluationService(EvaluationRepository evaluationRepository,
                             RestaurantRepository restaurantRepository,
                             EvaluationIndexRepository evaluationIndexRepository,
                             @Qualifier("evaluationBucket") S3Storage evaluationStorage) {
        this.evaluationRepository = evaluationRepository;
        this.restaurantRepository = restaurantRepository;
        this.evaluationIndexRepository = evaluationIndexRepository;
        this.evaluationStorage = evaluationStorage;
    }

    @Value("${app.security.jwt-enabled:false}")
    private boolean jwtEnabled;

    @Transactional
    public EvaluationDto add(int restaurantId, String nomEvaluateur, String commentaire, Integer note,
                             Authentication authentication) {
        nomEvaluateur = this.resolveNom(authentication, nomEvaluateur);
        this.check(nomEvaluateur, commentaire, note);
        RestaurantEntity restaurant = this.restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found"));
        EvaluationEntity entity = new EvaluationEntity();
        entity.setNomEvaluateur(nomEvaluateur);
        entity.setCommentaire(commentaire);
        entity.setNote(note);
        entity.setUserSubject(this.resolveSubject(authentication, nomEvaluateur));
        entity.setRestaurant(restaurant);
        EvaluationEntity saved = this.evaluationRepository.save(entity);
        this.evaluationIndexRepository.index(saved.getId(), saved.getCommentaire());
        return toDto(saved);
    }

    @Transactional
    public EvaluationDto update(int restaurantId, int evaluationId, String commentaire, Integer note,
                                Authentication authentication) {
        this.checkComment(commentaire);
        this.checkNote(note);
        EvaluationEntity entity = this.findEvaluation(restaurantId, evaluationId);
        this.checkCanEdit(entity, authentication);
        entity.setCommentaire(commentaire);
        entity.setNote(note);
        EvaluationEntity saved = this.evaluationRepository.save(entity);
        this.evaluationIndexRepository.index(saved.getId(), saved.getCommentaire());
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<EvaluationDto> search(List<String> mots) {
        String text = this.joinWords(mots);
        if (text.isEmpty()) {
            return List.of();
        }
        List<Integer> ids = this.evaluationIndexRepository.searchIds(text);
        List<EvaluationDto> result = new ArrayList<>();
        for (Integer id : ids) {
            this.evaluationRepository.findById(id).ifPresent(entity -> result.add(toDto(entity)));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<EvaluationDto> findMine(String userSubject) {
        if (userSubject == null || userSubject.isBlank()) {
            throw new AccessDeniedException("Il faut être connecté");
        }
        List<EvaluationDto> result = new ArrayList<>();
        for (EvaluationEntity entity : this.evaluationRepository.findByUserSubject(userSubject)) {
            result.add(toDto(entity));
        }
        return result;
    }

    // Plusieurs photos : chaque appel ajoute une clé, il ne remplace pas les précédentes.
    @Transactional
    public ImageLink requestPhotoUpload(int restaurantId, int evaluationId, Authentication authentication) {
        EvaluationEntity entity = this.findEvaluation(restaurantId, evaluationId);
        this.checkCanEdit(entity, authentication);
        String id = UUID.randomUUID().toString();
        EvaluationPhotoEntity photo = new EvaluationPhotoEntity();
        photo.setId(id);
        photo.setEvaluation(entity);
        if (entity.getPhotos() == null) {
            entity.setPhotos(new ArrayList<>());
        }
        entity.getPhotos().add(photo);
        this.evaluationRepository.save(entity);
        return new ImageLink(id, this.evaluationStorage.createUploadUrl(id));
    }

    @Transactional(readOnly = true)
    public List<ImageLink> requestPhotoDownload(int restaurantId, int evaluationId) {
        EvaluationEntity entity = this.findEvaluation(restaurantId, evaluationId);
        if (entity.getPhotos() == null || entity.getPhotos().isEmpty()) {
            throw new NotFoundException("Photo not found");
        }
        List<ImageLink> links = new ArrayList<>();
        for (EvaluationPhotoEntity photo : entity.getPhotos()) {
            links.add(new ImageLink(photo.getId(), this.evaluationStorage.createDownloadUrl(photo.getId())));
        }
        return links;
    }

    @Transactional
    public void delete(int restaurantId, int evaluationId, Authentication authentication) {
        EvaluationEntity entity = this.findEvaluation(restaurantId, evaluationId);
        this.checkCanEdit(entity, authentication);
        this.evaluationRepository.delete(entity);
        this.evaluationIndexRepository.delete(entity.getId());
    }

    // Plusieurs q= sont collés. Lucene garde un commentaire qui contient au moins un mot.
    private String joinWords(List<String> mots) {
        if (mots == null || mots.isEmpty()) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        for (String mot : mots) {
            if (mot == null || mot.isBlank()) {
                continue;
            }
            if (text.length() > 0) {
                text.append(' ');
            }
            text.append(mot.trim());
        }
        return text.toString();
    }

    private EvaluationEntity findEvaluation(int restaurantId, int evaluationId) {
        EvaluationEntity entity = this.evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new NotFoundException("Evaluation not found"));
        if (entity.getRestaurant() == null || entity.getRestaurant().getId() != restaurantId) {
            throw new NotFoundException("Evaluation not found");
        }
        return entity;
    }

    private String resolveNom(Authentication authentication, String nomEvaluateur) {
        if (this.jwtEnabled) {
            if (!(authentication instanceof JwtAuthenticationToken)) {
                throw new AccessDeniedException("Il faut être connecté");
            }
            String nom = AuthenticatedUser.displayName(authentication);
            if (nom == null || nom.isBlank()) {
                throw new AccessDeniedException("Le token ne contient pas le nom");
            }
            return nom;
        }
        return nomEvaluateur;
    }

    // Sans JWT on ne vérifie pas le propriétaire, pour garder curl et les tests.
    // Un ADMIN peut tout modifier. Sinon le sub doit être celui de la création.
    private void checkCanEdit(EvaluationEntity entity, Authentication authentication) {
        if (!this.jwtEnabled) {
            return;
        }
        if (!(authentication instanceof JwtAuthenticationToken)) {
            throw new AccessDeniedException("Il faut être connecté");
        }
        if (this.isAdmin(authentication)) {
            return;
        }
        String subject = AuthenticatedUser.subject(authentication);
        if (subject == null || subject.isBlank() || !subject.equals(entity.getUserSubject())) {
            throw new AccessDeniedException("Vous ne pouvez modifier que vos évaluations");
        }
    }

    private boolean isAdmin(Authentication authentication) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private String resolveSubject(Authentication authentication, String nomEvaluateur) {
        if (authentication instanceof JwtAuthenticationToken) {
            return AuthenticatedUser.subject(authentication);
        }
        return "local:" + nomEvaluateur;
    }

    private void check(String nomEvaluateur, String commentaire, Integer note) {
        if (nomEvaluateur == null || nomEvaluateur.isBlank()) {
            throw new InvalidEvaluationException("Le nom de l'évaluateur ne doit pas être vide");
        }
        if (nomEvaluateur.length() > 50) {
            throw new InvalidEvaluationException("Le nom de l'évaluateur doit comporter au maximum 50 caractères.");
        }
        this.checkComment(commentaire);
        this.checkNote(note);
    }

    private void checkComment(String commentaire) {
        if (commentaire == null || commentaire.isBlank()) {
            throw new InvalidEvaluationException("Le commentaire ne doit pas être vide");
        }
        if (commentaire.length() > 255) {
            throw new InvalidEvaluationException("Le commentaire doit comporter au maximum 255 caractères.");
        }
    }

    private void checkNote(Integer note) {
        if (note == null || note < 0 || note > 3) {
            throw new InvalidEvaluationException("La note doit être 0, 1, 2 ou 3");
        }
    }

    private static EvaluationDto toDto(EvaluationEntity entity) {
        return EvaluationDto.builder()
                .id(entity.getId())
                .nomEvaluateur(entity.getNomEvaluateur())
                .commentaire(entity.getCommentaire())
                .note(entity.getNote())
                .photoIds(photoIds(entity))
                .build();
    }

    private static List<String> photoIds(EvaluationEntity entity) {
        List<String> ids = new ArrayList<>();
        if (entity.getPhotos() == null) {
            return ids;
        }
        for (EvaluationPhotoEntity photo : entity.getPhotos()) {
            ids.add(photo.getId());
        }
        return ids;
    }
}
