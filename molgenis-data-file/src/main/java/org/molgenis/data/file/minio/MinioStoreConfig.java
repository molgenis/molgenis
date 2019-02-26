package org.molgenis.data.file.minio;

import static java.util.Objects.requireNonNull;

import io.minio.MinioClient;
import javax.annotation.Nullable;
import org.molgenis.data.file.BlobStore;
import org.molgenis.data.file.TransactionalBlobStoreDecorator;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.populate.IdGeneratorImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import(IdGeneratorImpl.class)
@Configuration
public class MinioStoreConfig {
  private final String bucketName;
  private final String minioEndpoint;
  private final String minioAccessKey;
  private final String minioSecretKey;
  private final String minioRegion;
  private final IdGenerator idGenerator;

  public MinioStoreConfig(
      @Value("${minio_bucket_name:molgenis}") String bucketName,
      @Value("${minio_endpoint:http://127.0.0.1:9000}") String minioEndpoint,
      @Value("${minio_access_key:molgenis}") String minioAccessKey,
      @Value("${minio_secret_key:molgenis}") String minioSecretKey,
      @Nullable @Value("${minio_region:@null}") String minioRegion,
      IdGenerator idGenerator) {
    this.bucketName = requireNonNull(bucketName);
    this.minioEndpoint = requireNonNull(minioEndpoint);
    this.minioAccessKey = requireNonNull(minioAccessKey);
    this.minioSecretKey = requireNonNull(minioSecretKey);
    this.minioRegion = minioRegion;
    this.idGenerator = requireNonNull(idGenerator);
  }

  @Bean
  public BlobStore blobStore() {
    MinioClientFacade minioClientFacade = minioClientFacade();
    MinioBlobStore minioBlobStore = new MinioBlobStore(minioClientFacade, idGenerator);
    return new TransactionalBlobStoreDecorator(minioBlobStore);
  }

  @Bean
  public MinioClientFacade minioClientFacade() {
    MinioClient minioClient = minioClientFactory().createClient();
    return new MinioClientFacade(minioClient, bucketName);
  }

  @Bean
  public MinioClientFactory minioClientFactory() {
    // TODO nice error message for missing props
    return new MinioClientFactory(
        bucketName, minioEndpoint, minioAccessKey, minioSecretKey, minioRegion);
  }
}
