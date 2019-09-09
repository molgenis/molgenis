package org.molgenis.data.postgresql.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.transaction.MolgenisTransaction;
import org.molgenis.data.transaction.TransactionExceptionTranslatorRegistry;

class PostgreSqlTransactionManagerTest {
  private PostgreSqlTransactionManager molgenisTransactionManager;
  private IdGenerator idGenerator;

  @BeforeEach
  void setUpBeforeMethod() {
    idGenerator = mock(IdGenerator.class);
    DataSource dataSource = mock(DataSource.class);
    TransactionExceptionTranslatorRegistry transactionExceptionTranslatorRegistry =
        mock(TransactionExceptionTranslatorRegistry.class);
    molgenisTransactionManager =
        new PostgreSqlTransactionManager(
            idGenerator, dataSource, transactionExceptionTranslatorRegistry);
  }

  @Test
  void doGetTransaction() {
    String id = "unique_id";
    when(idGenerator.generateId()).thenReturn(id);
    Object trans = molgenisTransactionManager.doGetTransaction();
    assertNotNull(trans);
    assertTrue(trans instanceof MolgenisTransaction);

    MolgenisTransaction molgenisTransaction = (MolgenisTransaction) trans;
    assertEquals(molgenisTransaction.getId(), id);
  }
}
