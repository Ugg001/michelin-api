package M2fst.michelin.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3StorageConfiguration {

    @Bean
    @Qualifier("restaurantBucket")
    S3Storage restaurantBucket(@Value("${app.s3.endpoint}") String endpoint,
                               @Value("${app.s3.access-key}") String accessKey,
                               @Value("${app.s3.secret-key}") String secretKey,
                               @Value("${app.s3.restaurant-bucket}") String bucket) {
        return new S3Storage(endpoint, accessKey, secretKey, bucket);
    }

    @Bean
    @Qualifier("evaluationBucket")
    S3Storage evaluationBucket(@Value("${app.s3.endpoint}") String endpoint,
                               @Value("${app.s3.access-key}") String accessKey,
                               @Value("${app.s3.secret-key}") String secretKey,
                               @Value("${app.s3.evaluation-bucket}") String bucket) {
        return new S3Storage(endpoint, accessKey, secretKey, bucket);
    }
}
