package M2fst.michelin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Même forme que CoverLink du TP : un identifiant et une url signée 15 minutes.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageLink {
    private String id;
    private String url;
}
