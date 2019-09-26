package org.molgenis.data.file.minio;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.test.AbstractMockitoTest;

class MinioClientFactoryImplTest extends AbstractMockitoTest {
  private static final String BUCKET_NAME = "bucket";
  private static final String MINIO_ENDPOINT = "http://localhost:12345/";
  private static final String MINIO_ACCESS_KEY = "MyAccessKey";
  private static final String MINIO_SECRET_KEY = "MySecretKey";
  private static final String REGION = "MyRegion";
  private MinioClientFactoryImpl minioClientFactoryImpl;

  @BeforeEach
  void setUpBeforeMethod() {
    minioClientFactoryImpl =
        new MinioClientFactoryImpl(
            BUCKET_NAME, MINIO_ENDPOINT, MINIO_ACCESS_KEY, MINIO_SECRET_KEY, REGION);
  }

  @Test
  void testMinioClientFactoryImpl() {
    assertThrows(
        NullPointerException.class, () -> new MinioClientFactoryImpl(null, null, null, null, null));
  }

  @Test
  void testCreateClient() {
    assertThrows(IOException.class, () -> minioClientFactoryImpl.createClient());
  }
}
