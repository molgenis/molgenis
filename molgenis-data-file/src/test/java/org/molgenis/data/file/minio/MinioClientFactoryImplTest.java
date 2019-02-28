package org.molgenis.data.file.minio;

import java.io.IOException;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MinioClientFactoryImplTest extends AbstractMockitoTest {
  private static final String BUCKET_NAME = "bucket";
  private static final String MINIO_ENDPOINT = "http://localhost:12345/";
  private static final String MINIO_ACCESS_KEY = "MyAccessKey";
  private static final String MINIO_SECRET_KEY = "MySecretKey";
  private static final String REGION = "MyRegion";
  private MinioClientFactoryImpl minioClientFactoryImpl;

  @BeforeMethod
  public void setUpBeforeMethod() {
    minioClientFactoryImpl =
        new MinioClientFactoryImpl(
            BUCKET_NAME, MINIO_ENDPOINT, MINIO_ACCESS_KEY, MINIO_SECRET_KEY, REGION);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testMinioClientFactoryImpl() {
    new MinioClientFactoryImpl(null, null, null, null, null);
  }

  @Test(expectedExceptions = IOException.class)
  public void testCreateClient() throws IOException {
    minioClientFactoryImpl.createClient();
  }
}
