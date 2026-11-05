package M2fst.michelin.storage;

import jakarta.annotation.PreDestroy;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.http.auth.aws.signer.RegionSet;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;

// Repris de S3Repository du TP de demo. Les buckets restaurant et evaluation existent déjà sur le serveur du cours, donc on ne les crée pas.
public class S3Storage {

    private final S3Client client;
    private final S3Presigner presigner;
    private final String bucket;

    public S3Storage(String endpoint, String accessKey, String secretKey, String bucket) {
        this.bucket = bucket;
        StaticCredentialsProvider credentials = StaticCredentialsProvider.create(
                AwsBasicCredentials.builder()
                        .accessKeyId(accessKey)
                        .secretAccessKey(secretKey)
                        .build());
        S3Configuration configuration = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();
        this.client = S3Client.builder()
                .credentialsProvider(credentials)
                .endpointOverride(URI.create(endpoint))
                .region(Region.US_EAST_1)
                .forcePathStyle(true)
                .sigv4aSigningRegionSet(RegionSet.GLOBAL)
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .build();
        this.presigner = S3Presigner.builder()
                .credentialsProvider(credentials)
                .endpointOverride(URI.create(endpoint))
                .region(Region.US_EAST_1)
                .serviceConfiguration(configuration)
                .build();
    }

    public String createUploadUrl(String objectId) {
        return this.presigner.presignPutObject(PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(15))
                        .putObjectRequest(request -> request.bucket(this.bucket).key(objectId))
                        .build())
                .url()
                .toString();
    }

    public String createDownloadUrl(String objectId) {
        return this.presigner.presignGetObject(GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(15))
                        .getObjectRequest(request -> request.bucket(this.bucket).key(objectId))
                        .build())
                .url()
                .toString();
    }

    @PreDestroy
    public void close() {
        this.client.close();
        this.presigner.close();
    }
}
