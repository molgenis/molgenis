package org.molgenis.data.index.bootstrap;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.index.meta.IndexActionMetadata.INDEX_ACTION;
import static org.molgenis.data.index.meta.IndexActionMetadata.INDEX_STATUS;
import static org.molgenis.data.index.meta.IndexActionMetadata.IndexStatus.PENDING;
import static org.molgenis.data.index.meta.IndexActionMetadata.IndexStatus.STARTED;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.index.IndexActionRegisterService;
import org.molgenis.data.index.IndexService;
import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.index.meta.IndexActionMetadata.IndexStatus;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.AbstractMockitoTest;

class IndexBootstrapperTest extends AbstractMockitoTest {

  @Mock private MetaDataService metaDataService;
  @Mock private IndexService indexService;
  @Mock private IndexActionRegisterService indexActionRegisterService;
  @Mock private DataService dataService;
  @Mock private AttributeMetadata attributeMetadata;

  private IndexBootstrapper indexBootstrapper;

  @BeforeEach
  void beforeMethod() {
    indexBootstrapper =
        new IndexBootstrapper(
            metaDataService,
            indexService,
            indexActionRegisterService,
            dataService,
            attributeMetadata);
  }

  @Test
  void testStartupNoIndex() {
    @SuppressWarnings("unchecked")
    Repository<Entity> repo1 = mock(Repository.class);
    EntityType entityType1 = mock(EntityType.class);
    when(repo1.getEntityType()).thenReturn(entityType1);
    @SuppressWarnings("unchecked")
    Repository<Entity> repo2 = mock(Repository.class);
    EntityType entityType2 = mock(EntityType.class);
    when(repo2.getEntityType()).thenReturn(entityType2);
    @SuppressWarnings("unchecked")
    Repository<Entity> repo3 = mock(Repository.class);
    EntityType entityType3 = mock(EntityType.class);
    when(repo3.getEntityType()).thenReturn(entityType3);

    List<Repository<Entity>> repos = Arrays.asList(repo1, repo2, repo3);

    when(indexService.hasIndex(attributeMetadata)).thenReturn(false);
    when(metaDataService.getRepositories()).thenReturn(repos.stream());
    indexBootstrapper.bootstrap();

    // verify that new jobs are registered for all repos
    verify(indexActionRegisterService).register(entityType1, null);
    verify(indexActionRegisterService).register(entityType2, null);
    verify(indexActionRegisterService).register(entityType3, null);
  }

  @Test
  void testStartupInterruptedIndexActions() {
    when(indexService.hasIndex(attributeMetadata)).thenReturn(true);
    IndexAction action = mock(IndexAction.class);
    when(action.getEntityTypeId()).thenReturn("myEntityTypeName");
    when(action.getEntityId()).thenReturn("1");
    EntityType entityType = mock(EntityType.class);
    when(dataService.findAll(
            INDEX_ACTION,
            new QueryImpl<IndexAction>().in(INDEX_STATUS, asList(STARTED, PENDING)),
            IndexAction.class))
        .thenReturn(Stream.of(action));
    when(dataService.findOneById(
            EntityTypeMetadata.ENTITY_TYPE_META_DATA, "myEntityTypeName", EntityType.class))
        .thenReturn(entityType);
    Attribute idAttribute = mock(Attribute.class);
    when(idAttribute.getDataType()).thenReturn(AttributeType.INT);
    when(entityType.getIdAttribute()).thenReturn(idAttribute);

    indexBootstrapper.bootstrap();

    // verify that we are not passing through the "missing index" code
    verify(metaDataService, never()).getRepositories();
    // verify that a new action is registered for the failed one
    verify(indexActionRegisterService).register(entityType, 1);

    // verify that the interrupted action is set to failed
    verify(action).setIndexStatus(IndexStatus.FAILED);
    verify(dataService).update(INDEX_ACTION, action);
  }

  @Test
  void testStartupInterruptedIndexActionsUnknownEntityType() {
    when(indexService.hasIndex(attributeMetadata)).thenReturn(true);
    IndexAction action = mock(IndexAction.class);
    when(action.getEntityTypeId()).thenReturn("myEntityTypeName");
    when(action.getEntityId()).thenReturn("1");
    EntityType entityType = mock(EntityType.class);
    when(dataService.findOneById(
            EntityTypeMetadata.ENTITY_TYPE_META_DATA, "myEntityTypeName", EntityType.class))
        .thenReturn(null);
    when(dataService.findAll(
            INDEX_ACTION,
            new QueryImpl<IndexAction>().in(INDEX_STATUS, asList(STARTED, PENDING)),
            IndexAction.class))
        .thenReturn(Stream.of(action));

    indexBootstrapper.bootstrap();

    // verify that we are not passing through the "missing index" code
    verify(metaDataService, never()).getRepositories();
    // verify that a new job is registered for the failed one
    verify(indexActionRegisterService, times(0)).register(entityType, 1);

    // verify that the interrupted action is set to failed
    verify(action).setIndexStatus(IndexStatus.FAILED);
    verify(dataService).update(INDEX_ACTION, action);
  }

  @Test
  void testStartupAllIsFine() {
    when(indexService.hasIndex(attributeMetadata)).thenReturn(true);

    when(dataService.findAll(
            INDEX_ACTION,
            new QueryImpl<IndexAction>().in(INDEX_STATUS, asList(STARTED, PENDING)),
            IndexAction.class))
        .thenReturn(Stream.empty());
    indexBootstrapper.bootstrap();

    // verify that no new jobs are registered
    verify(indexActionRegisterService, never()).register(any(EntityType.class), any());
  }
}
