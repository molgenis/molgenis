package org.molgenis.data;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;

class RepositoryCollectionRegistryTest extends AbstractMockitoTest {
  @Mock RepositoryCollectionDecoratorFactory repositoryCollectionDecoratorFactory;
  private RepositoryCollectionRegistry repositoryCollectionRegistry;

  @BeforeEach
  void setUpBeforeEach() {
    repositoryCollectionRegistry =
        new RepositoryCollectionRegistry(repositoryCollectionDecoratorFactory);
  }

  @Test
  void testGetRepositoryCollection() {
    RepositoryCollection decoratedRepositoryCollection = mock(RepositoryCollection.class);
    RepositoryCollection repositoryCollection = mock(RepositoryCollection.class);
    when(repositoryCollectionDecoratorFactory.createDecoratedRepositoryCollection(
            repositoryCollection))
        .thenReturn(decoratedRepositoryCollection);
    String repositoryCollectionName = "MyRepositoryCollectionName";
    when(repositoryCollection.getName()).thenReturn(repositoryCollectionName);
    repositoryCollectionRegistry.addRepositoryCollection(repositoryCollection);
    assertEquals(
        decoratedRepositoryCollection,
        repositoryCollectionRegistry.getRepositoryCollection(repositoryCollectionName));
  }

  @Test
  void testGetRepositoryCollections() {
    RepositoryCollection decoratedRepositoryCollection = mock(RepositoryCollection.class);
    RepositoryCollection repositoryCollection = mock(RepositoryCollection.class);
    when(repositoryCollectionDecoratorFactory.createDecoratedRepositoryCollection(
            repositoryCollection))
        .thenReturn(decoratedRepositoryCollection);
    String repositoryCollectionName = "MyRepositoryCollectionName";
    when(repositoryCollection.getName()).thenReturn(repositoryCollectionName);
    repositoryCollectionRegistry.addRepositoryCollection(repositoryCollection);
    assertEquals(
        singletonList(decoratedRepositoryCollection),
        repositoryCollectionRegistry.getRepositoryCollections().collect(toList()));
  }

  @Test
  void testHasRepositoryCollection() {
    RepositoryCollection repositoryCollection = mock(RepositoryCollection.class);
    String repositoryCollectionName = "MyRepositoryCollectionName";
    when(repositoryCollection.getName()).thenReturn(repositoryCollectionName);
    repositoryCollectionRegistry.addRepositoryCollection(repositoryCollection);
    assertTrue(repositoryCollectionRegistry.hasRepositoryCollection(repositoryCollectionName));
  }

  @Test
  void testGetDefaultRepoCollection() {
    RepositoryCollection decoratedRepositoryCollection = mock(RepositoryCollection.class);
    RepositoryCollection defaultRepositoryCollection = mock(RepositoryCollection.class);
    when(repositoryCollectionDecoratorFactory.createDecoratedRepositoryCollection(
            defaultRepositoryCollection))
        .thenReturn(decoratedRepositoryCollection);
    repositoryCollectionRegistry.setDefaultRepoCollection(defaultRepositoryCollection);
    assertEquals(
        decoratedRepositoryCollection, repositoryCollectionRegistry.getDefaultRepoCollection());
  }
}
