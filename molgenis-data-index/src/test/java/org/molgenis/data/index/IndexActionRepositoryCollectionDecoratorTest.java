package org.molgenis.data.index;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

class IndexActionRepositoryCollectionDecoratorTest {
  private static final String REPOSITORY_NAME = "repo";
  private RepositoryCollection decoratedRepositoryCollection;
  private EntityType entityType;
  private IndexActionRegisterService indexActionRegisterService;
  private IndexActionRepositoryCollectionDecorator indexActionRepositoryCollectionDecorator;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUpBeforeMethod() {
    decoratedRepositoryCollection = mock(RepositoryCollection.class);
    Repository<Entity> repository = (Repository<Entity>) mock(Repository.class);
    when(decoratedRepositoryCollection.getRepository(REPOSITORY_NAME)).thenReturn(repository);
    entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("repo");
    when(repository.getEntityType()).thenReturn(entityType);
    indexActionRegisterService = mock(IndexActionRegisterService.class);
    indexActionRepositoryCollectionDecorator =
        new IndexActionRepositoryCollectionDecorator(
            decoratedRepositoryCollection, indexActionRegisterService);
  }

  @Test
  void deleteRepository() {
    indexActionRepositoryCollectionDecorator.deleteRepository(entityType);
    verify(decoratedRepositoryCollection).deleteRepository(entityType);
    verify(indexActionRegisterService).register(entityType, null);
  }

  @Test
  void updateRepository() {
    EntityType entityType2 = mock(EntityType.class);
    indexActionRepositoryCollectionDecorator.updateRepository(entityType, entityType2);
    verify(decoratedRepositoryCollection).updateRepository(entityType, entityType2);
    verify(indexActionRegisterService).register(entityType, null);
  }

  @Test
  void addAttribute() {
    EntityType entityType =
        when(mock(EntityType.class).getId()).thenReturn(REPOSITORY_NAME).getMock();
    Attribute attribute = when(mock(Attribute.class).getName()).thenReturn("attribute").getMock();
    indexActionRepositoryCollectionDecorator.addAttribute(entityType, attribute);
    verify(decoratedRepositoryCollection).addAttribute(entityType, attribute);
    verify(indexActionRegisterService).register(entityType, null);
  }

  @Test
  void deleteAttribute() {
    EntityType entityType =
        when(mock(EntityType.class).getId()).thenReturn(REPOSITORY_NAME).getMock();
    Attribute attribute = when(mock(Attribute.class).getName()).thenReturn("attribute").getMock();
    indexActionRepositoryCollectionDecorator.deleteAttribute(entityType, attribute);
    verify(decoratedRepositoryCollection).deleteAttribute(entityType, attribute);
    verify(indexActionRegisterService).register(entityType, null);
  }

  @Test
  void addEntityType() {
    indexActionRepositoryCollectionDecorator.createRepository(entityType);
    verify(decoratedRepositoryCollection).createRepository(entityType);
    verify(indexActionRegisterService).register(entityType, null);
  }
}
