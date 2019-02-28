package org.molgenis.data.file.minio;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.NoResponseException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xmlpull.v1.XmlPullParserException;

public class MinioClientFacadeTest extends AbstractMockitoTest {
  @Mock private MinioClient minioClient;
  private String bucketName;
  private MinioClientFacade minioClientFacade;

  @BeforeMethod
  public void setUpBeforeMethod() {
    bucketName = "molgenis";
    minioClientFacade = new MinioClientFacade(minioClient, bucketName);
  }

  @Test
  public void testPutObject()
      throws IOException, XmlPullParserException, NoSuchAlgorithmException, InvalidKeyException,
          InvalidArgumentException, InternalException, NoResponseException,
          InvalidBucketNameException, InsufficientDataException, ErrorResponseException {
    String objectName = "MyObjectName";
    InputStream inputStream = mock(InputStream.class);
    String contentType = "application/octet-stream";
    minioClientFacade.putObject(objectName, inputStream, contentType);
    verify(minioClient).putObject(bucketName, objectName, inputStream, contentType);
  }

  @Test
  public void testStatObject()
      throws IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException,
          InternalException, NoResponseException, InvalidBucketNameException,
          XmlPullParserException, ErrorResponseException {
    String objectName = "MyObjectName";
    ObjectStat objectStat = mock(ObjectStat.class);
    when(minioClient.statObject(bucketName, objectName)).thenReturn(objectStat);
    assertEquals(minioClientFacade.statObject(objectName), objectStat);
  }

  @Test
  public void testRemoveObject()
      throws IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException,
          InvalidArgumentException, InternalException, NoResponseException,
          InvalidBucketNameException, XmlPullParserException, ErrorResponseException {
    String objectName = "MyObjectName";
    minioClientFacade.removeObject(objectName);
    verify(minioClient).removeObject(bucketName, objectName);
  }

  @Test
  public void testGetObject()
      throws IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException,
          InvalidArgumentException, InternalException, NoResponseException,
          InvalidBucketNameException, XmlPullParserException, ErrorResponseException {
    String objectName = "MyObjectName";
    InputStream inputStream = mock(InputStream.class);
    when(minioClient.getObject(bucketName, objectName)).thenReturn(inputStream);
    assertEquals(minioClientFacade.getObject(objectName), inputStream);
  }
}
