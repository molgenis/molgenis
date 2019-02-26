package org.molgenis.data.blob;

import static org.molgenis.data.transaction.TransactionConstants.TRANSACTION_ID_RESOURCE_NAME;
import static org.springframework.transaction.support.TransactionSynchronizationManager.getResource;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.nio.channels.ReadableByteChannel;
import org.molgenis.data.transaction.TransactionListener;

class TransactionalBlobStoreDecorator extends BlobStoreDecorator implements TransactionListener {
  private final Multimap<String, String> transactionBlobMap;

  TransactionalBlobStoreDecorator(BlobStore delegateBlobStore) {
    super(delegateBlobStore);
    this.transactionBlobMap = ArrayListMultimap.create();
  }

  @Override
  public BlobMetadata store(ReadableByteChannel fromChannel) {
    BlobMetadata blobMetadata = delegate().store(fromChannel);

    // store blob identifier in case of a transaction rollback
    String transactionId = (String) getResource(TRANSACTION_ID_RESOURCE_NAME);
    transactionBlobMap.put(transactionId, blobMetadata.getId());

    return blobMetadata;
  }

  @Override
  public void delete(String blobId) {
    delegate().delete(blobId);

    // delete blob if it was stored in the same transaction
    String transactionId = (String) getResource(TRANSACTION_ID_RESOURCE_NAME);
    transactionBlobMap.remove(transactionId, blobId);
  }

  @Override
  public ReadableByteChannel newChannel(String blobId) {
    return delegate().newChannel(blobId);
  }

  @Override
  public void transactionStarted(String transactionId) {
    // no op
  }

  @Override
  public void commitTransaction(String transactionId) {
    // no op
  }

  @Override
  public void afterCommitTransaction(String transactionId) {
    // no op, see doCleanupAfterCompletion
  }

  @Override
  public void rollbackTransaction(String transactionId) {
    transactionBlobMap.get(transactionId).forEach(this::delete);
  }

  @Override
  public void doCleanupAfterCompletion(String transactionId) {
    transactionBlobMap.removeAll(transactionId);
  }
}
