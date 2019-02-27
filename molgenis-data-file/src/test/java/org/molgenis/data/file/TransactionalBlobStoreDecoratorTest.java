package org.molgenis.data.file;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.transaction.TransactionConstants.TRANSACTION_ID_RESOURCE_NAME;
import static org.testng.Assert.assertEquals;

import java.nio.channels.ReadableByteChannel;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TransactionalBlobStoreDecoratorTest extends AbstractMockitoTest {
  private static final String TRANSACTION_ID = "1";

  @Mock private BlobStore blobStore;
  private TransactionalBlobStoreDecorator transactionalBlobStoreDecorator;

  @BeforeMethod
  public void setUpBeforeMethod() {
    transactionalBlobStoreDecorator = new TransactionalBlobStoreDecorator(blobStore);
    TransactionSynchronizationManager.bindResource(TRANSACTION_ID_RESOURCE_NAME, TRANSACTION_ID);
  }

  @AfterMethod
  public void tearDownAfterMethod() {
    TransactionSynchronizationManager.unbindResource(TRANSACTION_ID_RESOURCE_NAME);
    super.tearDownAfterMethod();
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testTransactionalBlobStoreDecorator() {
    new TransactionalBlobStoreDecorator(null);
  }

  @Test
  public void testStore() {
    BlobMetadata blobMetadata = mock(BlobMetadata.class);
    ReadableByteChannel readableByteChannel = mock(ReadableByteChannel.class);
    when(blobStore.store(readableByteChannel)).thenReturn(blobMetadata);
    assertEquals(transactionalBlobStoreDecorator.store(readableByteChannel), blobMetadata);
  }

  @Test
  public void testDelete() {
    String blobId = "MyBlobId";
    transactionalBlobStoreDecorator.delete(blobId);
    verify(blobStore).delete(blobId);
  }

  @Test
  public void testNewChannel() {
    String blobId = "MyBlobId";
    ReadableByteChannel readableByteChannel = mock(ReadableByteChannel.class);
    when(blobStore.newChannel(blobId)).thenReturn(readableByteChannel);
    assertEquals(transactionalBlobStoreDecorator.newChannel(blobId), readableByteChannel);
  }

  @Test
  public void testRollbackTransaction() {
    String blobId = "MyBlobId";
    BlobMetadata blobMetadata = mock(BlobMetadata.class);
    when(blobMetadata.getId()).thenReturn(blobId);
    ReadableByteChannel readableByteChannel = mock(ReadableByteChannel.class);
    when(blobStore.store(readableByteChannel)).thenReturn(blobMetadata);
    transactionalBlobStoreDecorator.store(readableByteChannel);

    transactionalBlobStoreDecorator.rollbackTransaction(TRANSACTION_ID);
    verify(blobStore).delete(blobId);
  }

  @Test
  public void testDoCleanupAfterCompletion() {
    transactionalBlobStoreDecorator.doCleanupAfterCompletion(TRANSACTION_ID);
    // no exceptions
  }
}
