package org.molgenis.data.file;

import static org.molgenis.data.transaction.TransactionConstants.TRANSACTION_ID_RESOURCE_NAME;
import static org.springframework.transaction.support.TransactionSynchronizationManager.getResource;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import org.molgenis.data.transaction.TransactionListener;

public class TransactionalBlobStoreDecorator extends BlobStoreDecorator
    implements TransactionListener {
  private final Multimap<String, String> transactionBlobMap;

  public TransactionalBlobStoreDecorator(BlobStore delegateBlobStore) {
    super(delegateBlobStore);
    this.transactionBlobMap = ArrayListMultimap.create();
  }

  @Override
  public BlobMetadata store(ReadableByteChannel fromChannel) {
    BlobMetadata blobMetadata = super.store(fromChannel);

    // store blob identifier in case of a transaction rollback
    String transactionId = (String) getResource(TRANSACTION_ID_RESOURCE_NAME);
    transactionBlobMap.put(transactionId, blobMetadata.getId());

    return blobMetadata;
  }

  @Override
  public void delete(String blobId) {
    super.delete(blobId);

    // delete blob if it was stored in the same transaction
    String transactionId = (String) getResource(TRANSACTION_ID_RESOURCE_NAME);
    transactionBlobMap.remove(transactionId, blobId);
  }

  @Override
  public void rollbackTransaction(String transactionId) {
    List<String> blobIds = new ArrayList<>(transactionBlobMap.get(transactionId));
    blobIds.forEach(this::delete);
  }

  @Override
  public void doCleanupAfterCompletion(String transactionId) {
    transactionBlobMap.removeAll(transactionId);
  }
}
