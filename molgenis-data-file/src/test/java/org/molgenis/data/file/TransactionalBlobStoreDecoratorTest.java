package org.molgenis.data.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.transaction.TransactionConstants.TRANSACTION_ID_RESOURCE_NAME;

import java.nio.channels.ReadableByteChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class TransactionalBlobStoreDecoratorTest extends AbstractMockitoTest {
  private static final String TRANSACTION_ID = "1";

  @Mock private BlobStore blobStore;
  private TransactionalBlobStoreDecorator transactionalBlobStoreDecorator;

  @BeforeEach
  void setUpBeforeMethod() {
    transactionalBlobStoreDecorator = new TransactionalBlobStoreDecorator(blobStore);
    TransactionSynchronizationManager.bindResource(TRANSACTION_ID_RESOURCE_NAME, TRANSACTION_ID);
  }

  @AfterEach
  void tearDownAfterMethod() {
    TransactionSynchronizationManager.unbindResource(TRANSACTION_ID_RESOURCE_NAME);
  }

  @Test
  void testTransactionalBlobStoreDecorator() {
    assertThrows(NullPointerException.class, () -> new TransactionalBlobStoreDecorator(null));
  }

  @Test
  void testStore() {
    BlobMetadata blobMetadata = mock(BlobMetadata.class);
    ReadableByteChannel readableByteChannel = mock(ReadableByteChannel.class);
    when(blobStore.store(readableByteChannel)).thenReturn(blobMetadata);
    assertEquals(blobMetadata, transactionalBlobStoreDecorator.store(readableByteChannel));
  }

  @Test
  void testDelete() {
    String blobId = "MyBlobId";
    transactionalBlobStoreDecorator.delete(blobId);
    verify(blobStore).delete(blobId);
  }

  @Test
  void testNewChannel() {
    String blobId = "MyBlobId";
    ReadableByteChannel readableByteChannel = mock(ReadableByteChannel.class);
    when(blobStore.newChannel(blobId)).thenReturn(readableByteChannel);
    assertEquals(readableByteChannel, transactionalBlobStoreDecorator.newChannel(blobId));
  }

  @Test
  void testRollbackTransaction() {
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
  void testDoCleanupAfterCompletion() {
    transactionalBlobStoreDecorator.doCleanupAfterCompletion(TRANSACTION_ID);
    // no exceptions
  }
}
