package org.molgenis.data.file.minio;

import static java.util.Objects.requireNonNull;

import io.minio.MinioClient;
import java.io.IOException;
import java.io.UncheckedIOException;
import javax.annotation.Nullable;
import org.molgenis.data.file.BlobStore;
import org.molgenis.data.file.TransactionalBlobStoreDecorator;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.populate.IdGeneratorImpl;
import org.molgenis.data.transaction.TransactionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@Import(IdGeneratorImpl.class)
@Configuration
public class MinioStoreConfig {
  private final String bucketName;
  private final String minioEndpoint;
  private final String minioAccessKey;
  private final String minioSecretKey;
  private final String minioRegion;
  private final IdGenerator idGenerator;
  private final TransactionManager transactionManager;

  public MinioStoreConfig(
      @Value("${MINIO_BUCKET_NAME:molgenis}") String bucketName,
      @Value("${MINIO_ENDPOINT:http://127.0.0.1:9000}") String minioEndpoint,
      @Value("${MINIO_ACCESS_KEY:@null}") String minioAccessKey,
      @Value("${MINIO_SECRET_KEY:@null}") String minioSecretKey,
      @Nullable @Value("${MINIO_REGION:@null}") String minioRegion,
      IdGenerator idGenerator,
      TransactionManager transactionManager) {
    // No 'beans' of 'TransactionManager' type found error can't be resolved since the
    // application transaction manager is currently defined in molgenis-data-postgresql
    this.bucketName = bucketName;
    this.minioEndpoint = minioEndpoint;
    this.minioAccessKey = minioAccessKey;
    this.minioSecretKey = minioSecretKey;
    this.minioRegion = minioRegion;
    this.idGenerator = requireNonNull(idGenerator);
    this.transactionManager = requireNonNull(transactionManager);
  }

  @Bean
  public BlobStore blobStore() {
    MinioClientFacade minioClientFacade = minioClientFacade();
    MinioBlobStore minioBlobStore = new MinioBlobStore(minioClientFacade, idGenerator);
    TransactionalBlobStoreDecorator transactionalMinioBlobStore =
        new TransactionalBlobStoreDecorator(minioBlobStore);
    transactionManager.addTransactionListener(transactionalMinioBlobStore);
    return transactionalMinioBlobStore;
  }

  @Bean
  public MinioClientFacade minioClientFacade() {
    MinioClient minioClient;
    try {
      minioClient = minioClientFactory().createClient();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return new MinioClientFacade(minioClient, bucketName);
  }

  @Bean
  public MinioClientFactory minioClientFactory() {
    if (bucketName == null || bucketName.isEmpty()) {
      throw new IllegalArgumentException("Property 'minio.bucket.name' cannot be null or empty");
    }
    if (minioEndpoint == null || minioEndpoint.isEmpty()) {
      throw new IllegalArgumentException("Property 'minio.endpoint' cannot be null or empty");
    }
    if (minioAccessKey == null || minioAccessKey.isEmpty()) {
      throw new IllegalArgumentException("Property 'minio.access.key' cannot be null or empty");
    }
    if (minioSecretKey == null || minioSecretKey.isEmpty()) {
      throw new IllegalArgumentException("Property 'minio.secret.key' cannot be null or empty");
    }
    return new MinioClientFactoryImpl(
        bucketName, minioEndpoint, minioAccessKey, minioSecretKey, minioRegion);
  }
}
