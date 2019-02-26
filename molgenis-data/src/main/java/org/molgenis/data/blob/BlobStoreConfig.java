package org.molgenis.data.blob;

import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.populate.IdGeneratorImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import(IdGeneratorImpl.class)
@Configuration
public class BlobStoreConfig {
  private final String bucketName;
  private final String minioEndpoint;
  private final String minioAccessKey;
  private final String minioSecretKey;
  private final IdGenerator idGenerator;

  public BlobStoreConfig(
      @Value("${minio_bucket_name:molgenis}") String bucketName,
      @Value("${minio_endpoint:http://127.0.0.1:9000}") String minioEndpoint,
      @Value("${minio_access_key:molgenis}") String minioAccessKey,
      @Value("${minio_secret_key:molgenis}") String minioSecretKey,
      IdGenerator idGenerator) {
    this.bucketName = bucketName;
    this.minioEndpoint = minioEndpoint;
    this.minioAccessKey = minioAccessKey;
    this.minioSecretKey = minioSecretKey;
    this.idGenerator = idGenerator;
  }

  @Bean
  public BlobStore blobStore() {
    MinioBlobStore minioBlobStore =
        new MinioBlobStore(bucketName, minioEndpoint, minioAccessKey, minioSecretKey, idGenerator);
    return new TransactionalBlobStoreDecorator(minioBlobStore);
  }
}
