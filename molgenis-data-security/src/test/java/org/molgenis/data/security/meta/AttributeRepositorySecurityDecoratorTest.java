package org.molgenis.data.security.meta;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.security.exception.SystemMetadataModificationException;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {AttributeRepositorySecurityDecoratorTest.Config.class})
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class AttributeRepositorySecurityDecoratorTest
    extends AbstractMockitoTestNGSpringContextTests {
  private static final String USERNAME = "user";
  private static final String ROLE_SU = "SU";
  private static final String ROLE_SYSTEM = "SYSTEM";

  private AttributeRepositorySecurityDecorator repo;
  @Mock private Repository<Attribute> delegateRepository;
  @Mock private DataService dataService;
  @Mock private MetaDataService metadataService;
  @Mock private SystemEntityTypeRegistry systemEntityTypeRegistry;
  @Mock private UserPermissionEvaluator permissionService;
  @Mock private Attribute attribute;
  @Mock private EntityType abstractEntityType;
  @Mock private EntityType concreteEntityType1;
  @Mock private EntityType concreteEntityType2;
  @Mock private RepositoryCollection backend1;
  @Mock private RepositoryCollection backend2;
  private String attributeId = "SDFSADFSDAF";
  @Captor private ArgumentCaptor<Consumer<List<Attribute>>> consumerCaptor;

  public AttributeRepositorySecurityDecoratorTest() {
    super(Strictness.WARN);
  }

  @BeforeMethod
  public void setUpBeforeMethod() {
    when(attribute.getEntity()).thenReturn(abstractEntityType);
    when(attribute.getName()).thenReturn("attributeName");
    when(dataService.getMeta()).thenReturn(metadataService);
    when(metadataService.getConcreteChildren(abstractEntityType))
        .thenReturn(Stream.of(concreteEntityType1, concreteEntityType2));
    when(metadataService.getBackend(concreteEntityType1)).thenReturn(backend1);
    when(metadataService.getBackend(concreteEntityType2)).thenReturn(backend2);
    when(attribute.getIdentifier()).thenReturn(attributeId);
    repo =
        new AttributeRepositorySecurityDecorator(
            delegateRepository, systemEntityTypeRegistry, permissionService);
  }

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SU})
  @Test
  public void countSu() {
    countSuOrSystem();
  }

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SYSTEM})
  @Test
  public void countSystem() {
    countSuOrSystem();
  }

  private void countSuOrSystem() {
    long count = 123L;
    when(delegateRepository.count()).thenReturn(count);
    assertEquals(repo.count(), 123L);
  }

  @WithMockUser(username = USERNAME)
  @Test
  public void countUser() {
    String entityType0Name = "entity0";
    EntityType entityType0 =
        when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
    String entityType1Name = "entity1";
    EntityType entityType1 =
        when(mock(EntityType.class).getId()).thenReturn(entityType1Name).getMock();
    String attr0Name = "entity0attr0";
    Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
    when(attr0.getEntity()).thenReturn(entityType0);
    String attr1Name = "entity1attr0";
    Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn(attr1Name).getMock();
    when(attr1.getEntity()).thenReturn(entityType1);
    when(delegateRepository.spliterator()).thenReturn(asList(attr0, attr1).spliterator());
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityType0Name), EntityTypePermission.READ_METADATA))
        .thenReturn(false);
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityType1Name), EntityTypePermission.READ_METADATA))
        .thenReturn(true);
    assertEquals(repo.count(), 1L);
  }

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SU})
  @Test
  public void countQuerySu() {
    countQuerySuOrSystem();
  }

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SYSTEM})
  @Test
  public void countQuerySystem() {
    countQuerySuOrSystem();
  }

  private void countQuerySuOrSystem() {
    long count = 123L;
    @SuppressWarnings("unchecked")
    Query<Attribute> q = mock(Query.class);
    when(delegateRepository.count(q)).thenReturn(count);
    assertEquals(repo.count(q), 123L);
  }

  @WithMockUser(username = USERNAME)
  @Test
  public void countQueryUser() {
    String entityType0Name = "entity0";
    EntityType entityType0 =
        when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
    String entityType1Name = "entity1";
    EntityType entityType1 =
        when(mock(EntityType.class).getId()).thenReturn(entityType1Name).getMock();
    String attr0Name = "entity0attr0";
    Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
    when(attr0.getEntity()).thenReturn(entityType0);
    String attr1Name = "entity1attr0";
    Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn(attr1Name).getMock();
    when(attr1.getEntity()).thenReturn(entityType1);
    Query<Attribute> q = new QueryImpl<>();
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Query<Attribute>> queryCaptor = forClass(Query.class);
    when(delegateRepository.findAll(queryCaptor.capture())).thenReturn(Stream.of(attr0, attr1));
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityType0Name), EntityTypePermission.READ_METADATA))
        .thenReturn(false);
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityType1Name), EntityTypePermission.READ_METADATA))
        .thenReturn(true);
    assertEquals(repo.count(q), 1L);
    assertEquals(queryCaptor.getValue().getOffset(), 0);
    assertEquals(queryCaptor.getValue().getPageSize(), Integer.MAX_VALUE);
  }

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SU})
  @Test
  public void findAllQuerySu() {
    findAllQuerySuOrSystem();
  }

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SYSTEM})
  @Test
  public void findAllQuerySystem() {
    findAllQuerySuOrSystem();
  }

  private void findAllQuerySuOrSystem() {
    Attribute attr0 = mock(Attribute.class);
    Attribute attr1 = mock(Attribute.class);
    @SuppressWarnings("unchecked")
    Query<Attribute> q = mock(Query.class);
    when(delegateRepository.findAll(q)).thenReturn(Stream.of(attr0, attr1));
    assertEquals(repo.findAll(q).collect(toList()), asList(attr0, attr1));
  }

  @WithMockUser(username = USERNAME)
  @Test
  public void findAllQueryUser() {
    String entityType0Name = "entity0";
    EntityType entityType0 =
        when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
    String entityType1Name = "entity1";
    EntityType entityType1 =
        when(mock(EntityType.class).getId()).thenReturn(entityType1Name).getMock();
    String attr0Name = "entity0attr0";
    Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
    when(attr0.getEntity()).thenReturn(entityType0);
    String attr1Name = "entity1attr0";
    Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn(attr1Name).getMock();
    when(attr1.getEntity()).thenReturn(entityType1);
    Query<Attribute> q = new QueryImpl<>();
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Query<Attribute>> queryCaptor = forClass(Query.class);
    when(delegateRepository.findAll(queryCaptor.capture())).thenReturn(Stream.of(attr0, attr1));
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityType0Name), EntityTypePermission.READ_METADATA))
        .thenReturn(false);
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityType1Name), EntityTypePermission.READ_METADATA))
        .thenReturn(true);
    assertEquals(repo.findAll(q).collect(toList()), singletonList(attr1));
    assertEquals(queryCaptor.getValue().getOffset(), 0);
    assertEquals(queryCaptor.getValue().getPageSize(), Integer.MAX_VALUE);
  }

  @WithMockUser(username = USERNAME)
  @Test
  public void findAllQueryUserOffsetLimit() {
    String entityType0Name = "entity0";
    EntityType entityType0 =
        when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
    String entityType1Name = "entity1";
    EntityType entityType1 =
        when(mock(EntityType.class).getId()).thenReturn(entityType1Name).getMock();
    String attr0Name = "entity0attr0";
    Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
    when(attr0.getEntity()).thenReturn(entityType0);
    String attr1Name = "entity1attr0";
    Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn(attr1Name).getMock();
    when(attr1.getEntity()).thenReturn(entityType1);
    @SuppressWarnings("unchecked")
    Query<Attribute> q = mock(Query.class);
    when(q.getOffset()).thenReturn(1);
    when(q.getPageSize()).thenReturn(1);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Query<Attribute>> queryCaptor = forClass(Query.class);
    when(delegateRepository.findAll(queryCaptor.capture())).thenReturn(Stream.of(attr0, attr1));
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityType0Name), EntityTypePermission.READ_METADATA))
        .thenReturn(false);
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityType1Name), EntityTypePermission.READ_METADATA))
        .thenReturn(true);
    assertEquals(repo.findAll(q).collect(toList()), emptyList());
    assertEquals(queryCaptor.getValue().getOffset(), 0);
    assertEquals(queryCaptor.getValue().getPageSize(), Integer.MAX_VALUE);
  }

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SU})
  @Test
  public void iteratorSu() {
    iteratorSuOrSystem();
  }

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SYSTEM})
  @Test
  public void iteratorSystem() {
    iteratorSuOrSystem();
  }

  private void iteratorSuOrSystem() {
    Attribute attr0 = mock(Attribute.class);
    Attribute attr1 = mock(Attribute.class);
    when(delegateRepository.iterator()).thenReturn(asList(attr0, attr1).iterator());
    assertEquals(newArrayList(repo.iterator()), asList(attr0, attr1));
  }

  @WithMockUser(username = USERNAME)
  @Test
  public void iteratorUser() {
    String entityType0Name = "entity0";
    EntityType entityType0 =
        when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
    String entityType1Name = "entity1";
    EntityType entityType1 =
        when(mock(EntityType.class).getId()).thenReturn(entityType1Name).getMock();
    String attr0Name = "entity0attr0";
    Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
    when(attr0.getEntity()).thenReturn(entityType0);
    String attr1Name = "entity1attr0";
    Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn(attr1Name).getMock();
    when(attr1.getEntity()).thenReturn(entityType1);
    when(delegateRepository.spliterator()).thenReturn(asList(attr0, attr1).spliterator());
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityType0Name), EntityTypePermission.READ_METADATA))
        .thenReturn(false);
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityType1Name), EntityTypePermission.READ_METADATA))
        .thenReturn(true);
    assertEquals(newArrayList(repo.iterator()), singletonList(attr1));
  }

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SU})
  @Test
  public void forEachBatchedSu() {
    forEachBatchedSuOrSystem();
  }

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SYSTEM})
  @Test
  public void forEachBatchedSystem() {
    forEachBatchedSuOrSystem();
  }

  private void forEachBatchedSuOrSystem() {
    Fetch fetch = mock(Fetch.class);
    @SuppressWarnings("unchecked")
    Consumer<List<Attribute>> consumer = mock(Consumer.class);
    repo.forEachBatched(fetch, consumer, 10);
    verify(delegateRepository).forEachBatched(fetch, consumer, 10);
  }

  @WithMockUser(username = USERNAME)
  @Test
  public void forEachBatchedUser() {
    List<Attribute> attributes = newArrayList();
    Attribute attribute1 = mock(Attribute.class);
    Attribute attribute2 = mock(Attribute.class);
    Attribute attribute3 = mock(Attribute.class);
    Attribute attribute4 = mock(Attribute.class);

    EntityType entityType1 = mock(EntityType.class);
    EntityType entityType2 = mock(EntityType.class);
    EntityType entityType3 = mock(EntityType.class);
    EntityType entityType4 = mock(EntityType.class);

    when(attribute1.getEntity()).thenReturn(entityType1);
    when(attribute2.getEntity()).thenReturn(entityType2);
    when(attribute3.getEntity()).thenReturn(entityType3);
    when(attribute4.getEntity()).thenReturn(entityType4);

    when(entityType1.getId()).thenReturn("EntityType1");
    when(entityType2.getId()).thenReturn("EntityType2");
    when(entityType3.getId()).thenReturn("EntityType3");
    when(entityType4.getId()).thenReturn("EntityType4");

    repo.forEachBatched(attributes::addAll, 2);

    when(permissionService.hasPermission(
            new EntityTypeIdentity("EntityType1"), EntityTypePermission.READ_METADATA))
        .thenReturn(true);
    when(permissionService.hasPermission(
            new EntityTypeIdentity("EntityType2"), EntityTypePermission.READ_METADATA))
        .thenReturn(false);
    when(permissionService.hasPermission(
            new EntityTypeIdentity("EntityType3"), EntityTypePermission.READ_METADATA))
        .thenReturn(false);
    when(permissionService.hasPermission(
            new EntityTypeIdentity("EntityType4"), EntityTypePermission.READ_METADATA))
        .thenReturn(true);

    // Decorated repo returns two batches of two entityTypes
    verify(delegateRepository).forEachBatched(eq(null), consumerCaptor.capture(), eq(2));
    consumerCaptor.getValue().accept(Lists.newArrayList(attribute1, attribute2));
    consumerCaptor.getValue().accept(Lists.newArrayList(attribute3, attribute4));

    assertEquals(attributes, newArrayList(attribute1, attribute4));
  }

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SU})
  @Test
  public void findOneQuerySu() {
    findOneQuerySuOrSystem();
  }

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SYSTEM})
  @Test
  public void findOneQuerySystem() {
    findOneQuerySuOrSystem();
  }

  private void findOneQuerySuOrSystem() {
    Attribute attr0 = mock(Attribute.class);
    @SuppressWarnings("unchecked")
    Query<Attribute> q = mock(Query.class);
    when(delegateRepository.findOne(q)).thenReturn(attr0);
    assertEquals(repo.findOne(q), attr0);
  }

  @WithMockUser(username = USERNAME)
  @Test
  public void findOneQueryUserPermissionAllowed() {
    String entityType0Name = "entity0";
    EntityType entityType0 =
        when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
    String attr0Name = "entity0attr0";
    Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
    when(attr0.getEntity()).thenReturn(entityType0);
    Query<Attribute> q = new QueryImpl<>();
    when(delegateRepository.findOne(q)).thenReturn(attr0);
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityType0Name), EntityTypePermission.READ_METADATA))
        .thenReturn(true);
    assertEquals(repo.findOne(q), attr0);
  }

  @WithMockUser(username = USERNAME)
  @Test
  public void findOneQueryUserPermissionDenied() {
    String entityType0Name = "entity0";
    EntityType entityType0 =
        when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
    String attr0Name = "entity0attr0";
    Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
    when(attr0.getEntity()).thenReturn(entityType0);
    Query<Attribute> q = new QueryImpl<>();
    when(delegateRepository.findOne(q)).thenReturn(attr0);
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityType0Name), EntityTypePermission.READ_METADATA))
        .thenReturn(false);
    assertNull(repo.findOne(q));
  }

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SU})
  @Test
  public void findOneByIdSu() {
    findOneByIdSuOrSystem();
  }

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SYSTEM})
  @Test
  public void findOneByIdSystem() {
    findOneByIdSuOrSystem();
  }

  private void findOneByIdSuOrSystem() {
    Attribute attr0 = mock(Attribute.class);
    Object id = "0";
    when(delegateRepository.findOneById(id)).thenReturn(attr0);
    assertEquals(repo.findOneById(id), attr0);
  }

  @WithMockUser(username = USERNAME)
  @Test
  public void findOneByIdUserPermissionAllowed() {
    String entityType0Name = "entity0";
    EntityType entityType0 =
        when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
    String attr0Name = "entity0attr0";
    Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
    when(attr0.getEntity()).thenReturn(entityType0);
    Object id = mock(Object.class);
    when(delegateRepository.findOneById(id)).thenReturn(attr0);
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityType0Name), EntityTypePermission.READ_METADATA))
        .thenReturn(true);
    assertEquals(repo.findOneById(id), attr0);
  }

  @WithMockUser(username = USERNAME)
  @Test
  public void findOneByIdUserPermissionAllowedAttrInCompoundWithOneAttr() {
    String entityType0Name = "entity0";
    EntityType entityType0 =
        when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
    String attr0Name = "entity0attrCompoundattr0";
    Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
    when(attr0.getEntity()).thenReturn(entityType0);
    String attrCompoundName = "entity0attrCompound";
    Attribute attrCompound =
        when(mock(Attribute.class).getName()).thenReturn(attrCompoundName).getMock();
    when(attrCompound.getEntity()).thenReturn(entityType0);
    Object id = mock(Object.class);
    when(delegateRepository.findOneById(id)).thenReturn(attr0);
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityType0Name), EntityTypePermission.READ_METADATA))
        .thenReturn(true);
    assertEquals(repo.findOneById(id), attr0);
  }

  @WithMockUser(username = USERNAME)
  @Test
  public void findOneByIdUserPermissionDenied() {
    String entityType0Name = "entity0";
    EntityType entityType0 =
        when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
    String attr0Name = "entity0attr0";
    Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
    when(attr0.getEntity()).thenReturn(entityType0);
    Object id = mock(Object.class);
    when(delegateRepository.findOneById(id)).thenReturn(attr0);
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityType0Name), EntityTypePermission.READ_METADATA))
        .thenReturn(false);
    assertNull(repo.findOneById(id));
  }

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SU})
  @Test
  public void findOneByIdFetchSu() {
    findOneByIdFetchSuOrSystem();
  }

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SYSTEM})
  @Test
  public void findOneByIdFetchSystem() {
    findOneByIdFetchSuOrSystem();
  }

  private void findOneByIdFetchSuOrSystem() {
    Attribute attr0 = mock(Attribute.class);
    Object id = "0";
    when(delegateRepository.findOneById(id)).thenReturn(attr0);
    assertEquals(repo.findOneById(id), attr0);
  }

  @WithMockUser(username = USERNAME)
  @Test
  public void findOneByIdFetchUserPermissionAllowed() {
    String entityType0Name = "entity0";
    EntityType entityType0 =
        when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
    String attr0Name = "entity0attr0";
    Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
    when(attr0.getEntity()).thenReturn(entityType0);
    Object id = mock(Object.class);
    Fetch fetch = mock(Fetch.class);
    when(delegateRepository.findOneById(id, fetch)).thenReturn(attr0);
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityType0Name), EntityTypePermission.READ_METADATA))
        .thenReturn(true);
    assertEquals(repo.findOneById(id, fetch), attr0);
  }

  @WithMockUser(username = USERNAME)
  @Test
  public void findOneByIdFetchUserPermissionDenied() {
    String entityType0Name = "entity0";
    EntityType entityType0 =
        when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
    String attr0Name = "entity0attr0";
    Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
    when(attr0.getEntity()).thenReturn(entityType0);
    Object id = mock(Object.class);
    Fetch fetch = mock(Fetch.class);
    when(delegateRepository.findOneById(id, fetch)).thenReturn(attr0);
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityType0Name), EntityTypePermission.READ_METADATA))
        .thenReturn(false);
    assertNull(repo.findOneById(id, fetch));
  }

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SU})
  @Test
  public void findAllIdsSu() {
    findAllIdsSuOrSystem();
  }

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SYSTEM})
  @Test
  public void findAllIdsSystem() {
    findAllIdsSuOrSystem();
  }

  private void findAllIdsSuOrSystem() {
    Attribute attr0 = mock(Attribute.class);
    Attribute attr1 = mock(Attribute.class);
    Stream<Object> ids = Stream.of("0", "1");
    when(delegateRepository.findAll(ids)).thenReturn(Stream.of(attr0, attr1));
    assertEquals(repo.findAll(ids).collect(toList()), asList(attr0, attr1));
  }

  @WithMockUser(username = USERNAME)
  @Test
  public void findAllIdsUser() {
    String entityType0Name = "entity0";
    EntityType entityType0 =
        when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
    String entityType1Name = "entity1";
    EntityType entityType1 =
        when(mock(EntityType.class).getId()).thenReturn(entityType1Name).getMock();
    String attr0Name = "entity0attr0";
    Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
    when(attr0.getEntity()).thenReturn(entityType0);
    String attr1Name = "entity1attr0";
    Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn(attr1Name).getMock();
    when(attr1.getEntity()).thenReturn(entityType1);
    Stream<Object> ids = Stream.of(mock(Object.class), mock(Object.class));
    when(delegateRepository.findAll(ids)).thenReturn(Stream.of(attr0, attr1));
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityType0Name), EntityTypePermission.READ_METADATA))
        .thenReturn(false);
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityType1Name), EntityTypePermission.READ_METADATA))
        .thenReturn(true);
    assertEquals(repo.findAll(ids).collect(toList()), singletonList(attr1));
  }

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SU})
  @Test
  public void findAllIdsFetchSu() {
    findAllIdsFetchSuOrSystem();
  }

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SYSTEM})
  @Test
  public void findAllIdsFetchSystem() {
    findAllIdsFetchSuOrSystem();
  }

  private void findAllIdsFetchSuOrSystem() {
    Attribute attr0 = mock(Attribute.class);
    Attribute attr1 = mock(Attribute.class);
    Stream<Object> ids = Stream.of("0", "1");
    Fetch fetch = mock(Fetch.class);
    when(delegateRepository.findAll(ids, fetch)).thenReturn(Stream.of(attr0, attr1));
    assertEquals(repo.findAll(ids, fetch).collect(toList()), asList(attr0, attr1));
  }

  @WithMockUser(username = USERNAME)
  @Test
  public void findAllIdsFetchUser() {
    String entityType0Name = "entity0";
    EntityType entityType0 =
        when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
    String entityType1Name = "entity1";
    EntityType entityType1 =
        when(mock(EntityType.class).getId()).thenReturn(entityType1Name).getMock();
    String attr0Name = "entity0attr0";
    Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn(attr0Name).getMock();
    when(attr0.getEntity()).thenReturn(entityType0);
    String attr1Name = "entity1attr0";
    Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn(attr1Name).getMock();
    when(attr1.getEntity()).thenReturn(entityType1);
    Stream<Object> ids = Stream.of(mock(Object.class), mock(Object.class));
    Fetch fetch = mock(Fetch.class);
    when(delegateRepository.findAll(ids, fetch)).thenReturn(Stream.of(attr0, attr1));
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityType0Name), EntityTypePermission.READ_METADATA))
        .thenReturn(false);
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityType1Name), EntityTypePermission.READ_METADATA))
        .thenReturn(true);
    assertEquals(repo.findAll(ids, fetch).collect(toList()), singletonList(attr1));
  }

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SU})
  @Test
  public void aggregateSu() {
    aggregateSuOrSystem();
  }

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SYSTEM})
  @Test
  public void aggregateSystem() {
    aggregateSuOrSystem();
  }

  private void aggregateSuOrSystem() {
    AggregateQuery aggregateQuery = mock(AggregateQuery.class);
    AggregateResult aggregateResult = mock(AggregateResult.class);
    when(delegateRepository.aggregate(aggregateQuery)).thenReturn(aggregateResult);
    assertEquals(repo.aggregate(aggregateQuery), aggregateResult);
  }

  @SuppressWarnings("deprecation")
  @WithMockUser(username = USERNAME)
  @Test(expectedExceptions = MolgenisDataAccessException.class)
  public void aggregateUser() {
    AggregateQuery aggregateQuery = mock(AggregateQuery.class);
    repo.aggregate(aggregateQuery);
  }

  @Test
  public void delete() {
    String attrName = "attrName";
    Attribute attr = when(mock(Attribute.class).getName()).thenReturn(attrName).getMock();
    repo.delete(attr);
    verify(delegateRepository).delete(attr);
  }

  @Test(expectedExceptions = SystemMetadataModificationException.class)
  public void deleteSystemAttribute() {
    String attrName = "attrName";
    Attribute attr = when(mock(Attribute.class).getName()).thenReturn(attrName).getMock();
    String attrIdentifier = "id";
    when(attr.getIdentifier()).thenReturn(attrIdentifier);
    when(systemEntityTypeRegistry.hasSystemAttribute(attrIdentifier)).thenReturn(true);
    repo.delete(attr);
  }

  @Test
  public void deleteStream() {
    AttributeRepositorySecurityDecorator repoSpy = spy(repo);
    doNothing().when(repoSpy).delete(any(Attribute.class));
    Attribute attr0 = mock(Attribute.class);
    Attribute attr1 = mock(Attribute.class);
    repoSpy.delete(Stream.of(attr0, attr1));
    verify(repoSpy).delete(attr0);
    verify(repoSpy).delete(attr1);
  }

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SU})
  @Test(expectedExceptions = {SystemMetadataModificationException.class})
  public void updateSystemEntity() {
    Attribute currentAttribute = mock(Attribute.class);
    when(systemEntityTypeRegistry.getSystemAttribute(attributeId)).thenReturn(currentAttribute);

    repo.update(attribute);
  }

  static class Config {}
}
