package org.molgenis.data.postgresql;

import static org.mockito.Mockito.inOrder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.postgresql.identifier.EntityTypeRegistry;
import org.molgenis.test.AbstractMockitoTest;

class PostgreSqlRepositoryCollectionDecoratorTest extends AbstractMockitoTest {
  @Mock private EntityType entityType;
  @Mock private Attribute attr;
  @Mock private Attribute updatedAttr;
  @Mock private RepositoryCollection repoCollection;
  @Mock private EntityTypeRegistry entityTypeRegistry;
  private PostgreSqlRepositoryCollectionDecorator repoCollectionDecorator;
  private InOrder inOrder;

  @BeforeEach
  void setUpBeforeMethod() {
    repoCollectionDecorator =
        new PostgreSqlRepositoryCollectionDecorator(repoCollection, entityTypeRegistry);
    inOrder = inOrder(repoCollection, entityTypeRegistry);
  }

  @Test
  void testCreateRepository() {
    repoCollectionDecorator.createRepository(entityType);

    inOrder.verify(repoCollection).createRepository(entityType);
    inOrder.verify(entityTypeRegistry).registerEntityType(entityType);
  }

  @Test
  void testDeleteRepository() {
    repoCollectionDecorator.deleteRepository(entityType);

    inOrder.verify(repoCollection).deleteRepository(entityType);
    inOrder.verify(entityTypeRegistry).unregisterEntityType(entityType);
  }

  @Test
  void testAddAttribute() {
    repoCollectionDecorator.addAttribute(entityType, attr);

    inOrder.verify(entityTypeRegistry).addAttribute(entityType, attr);
    inOrder.verify(repoCollection).addAttribute(entityType, attr);
  }

  @Test
  void testUpdateAttribute() {
    repoCollectionDecorator.updateAttribute(entityType, attr, updatedAttr);

    inOrder.verify(entityTypeRegistry).updateAttribute(entityType, attr, updatedAttr);
    inOrder.verify(repoCollection).updateAttribute(entityType, attr, updatedAttr);
  }

  @Test
  void testDeleteAttribute() {
    repoCollectionDecorator.deleteAttribute(entityType, attr);

    inOrder.verify(entityTypeRegistry).deleteAttribute(entityType, attr);
    inOrder.verify(repoCollection).deleteAttribute(entityType, attr);
  }
}
