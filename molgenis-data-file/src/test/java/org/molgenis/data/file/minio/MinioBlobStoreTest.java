package org.molgenis.data.file.minio;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import io.minio.ObjectStat;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.NoResponseException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.mockito.Mock;
import org.molgenis.data.file.BlobMetadata;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xmlpull.v1.XmlPullParserException;

public class MinioBlobStoreTest extends AbstractMockitoTest {
  @Mock private MinioClientFacade minioClientFacade;
  @Mock private IdGenerator idGenerator;
  private MinioBlobStore minioBlobStore;

  @BeforeMethod
  public void setUpBeforeMethod() {
    minioBlobStore = new MinioBlobStore(minioClientFacade, idGenerator);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testMinioBlobStore() {
    new MinioBlobStore(null, null);
  }

  @Test
  public void testStore()
      throws IOException, XmlPullParserException, NoSuchAlgorithmException, InvalidKeyException,
          InvalidArgumentException, InternalException, NoResponseException,
          InvalidBucketNameException, InsufficientDataException, ErrorResponseException {
    ReadableByteChannel fromChannel = mock(ReadableByteChannel.class);
    String blobId = "MyBlobId";
    when(idGenerator.generateId()).thenReturn(blobId);
    long size = 3L;
    ObjectStat objectStat = when(mock(ObjectStat.class).length()).thenReturn(size).getMock();
    when(minioClientFacade.statObject(blobId)).thenReturn(objectStat);
    assertEquals(minioBlobStore.store(fromChannel), BlobMetadata.create(blobId, size));
    verify(minioClientFacade)
        .putObject(eq(blobId), any(InputStream.class), eq("application/octet-stream"));
  }

  @Test
  public void testDelete()
      throws IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException,
          InvalidArgumentException, InternalException, NoResponseException,
          InvalidBucketNameException, XmlPullParserException, ErrorResponseException {
    String blobId = "MyBlobId";
    minioBlobStore.delete(blobId);
    verify(minioClientFacade).removeObject(blobId);
  }

  @Test
  public void testNewChannel()
      throws IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException,
          InvalidArgumentException, InternalException, NoResponseException,
          InvalidBucketNameException, XmlPullParserException, ErrorResponseException {
    String blobId = "MyBlobId";
    InputStream inputStream = mock(InputStream.class);
    when(minioClientFacade.getObject(blobId)).thenReturn(inputStream);
    minioBlobStore.newChannel(blobId);
  }
}
