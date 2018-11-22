package org.molgenis.navigator.copy.service;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.navigator.copy.service.CopyTestUtils.setupPredictableIdGeneratorMock;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.stream.Stream;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.jobs.Progress;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EntityTypeCopierTest extends AbstractMockitoTest {

  @Mock(answer = RETURNS_DEEP_STUBS)
  private DataService dataService;

  @Mock private IdGenerator idGenerator;
  @Mock private EntityTypeDependencyResolver entityTypeDependencyResolver;
  @Mock private EntityTypeMetadataCopier entityTypeMetadataCopier;

  private EntityTypeCopier copier;

  @BeforeMethod
  public void beforeMethod() {
    copier =
        new EntityTypeCopier(
            dataService, idGenerator, entityTypeDependencyResolver, entityTypeMetadataCopier);
  }

  @Test
  public void testUniqueLabel() {
    ignoreMetadataCopyResult();
    EntityType entityType = mockEntityType("A");
    Package targetPackage = mock(Package.class);
    EntityType entityTypeInTarget = mock(EntityType.class);
    when(targetPackage.getEntityTypes()).thenReturn(singletonList(entityTypeInTarget));
    when(entityTypeInTarget.getLabel()).thenReturn("A");
    CopyState state = CopyState.create(targetPackage, mock(Progress.class));

    copier.copy(singletonList(entityType), state);

    verify(entityType).setLabel("A (Copy)");
  }

  @Test
  public void testUniqueLabelNoChange() {
    ignoreMetadataCopyResult();
    EntityType entityType = mockEntityType("A");
    Package targetPackage = mock(Package.class);
    EntityType entityTypeInTarget = mock(EntityType.class);
    when(targetPackage.getEntityTypes()).thenReturn(singletonList(entityTypeInTarget));
    when(entityTypeInTarget.getLabel()).thenReturn("B");
    CopyState state = CopyState.create(targetPackage, mock(Progress.class));

    copier.copy(singletonList(entityType), state);

    verify(entityType).setLabel("A");
  }

  @Test
  public void testUniqueLabelRootPackage() {
    ignoreMetadataCopyResult();
    EntityType entityType = mockEntityType("A");
    EntityType entityTypeInRoot = mock(EntityType.class);
    when(entityTypeInRoot.getLabel()).thenReturn("A");
    when(dataService
            .query(ENTITY_TYPE_META_DATA, EntityType.class)
            .eq(EntityTypeMetadata.PACKAGE, null)
            .findAll())
        .thenReturn(Stream.of(entityTypeInRoot));
    CopyState state = CopyState.create(null, mock(Progress.class));

    copier.copy(singletonList(entityType), state);

    verify(entityType).setLabel("A (Copy)");
  }

  @Test
  public void copySingleEntityType() {
    setupPredictableIdGeneratorMock(idGenerator);
    EntityType entityType = mockEntityType("A");
    EntityType entityTypeCopy = mock(EntityType.class);
    Package targetPackage = mock(Package.class);
    Progress progress = mock(Progress.class);
    CopyState state = CopyState.create(targetPackage, progress);
    when(entityTypeMetadataCopier.copy(entityType, state)).thenReturn(entityTypeCopy);
    when(entityTypeDependencyResolver.resolve(singletonList(entityTypeCopy)))
        .thenReturn(singletonList(entityTypeCopy));

    copier.copy(singletonList(entityType), state);

    verify(entityTypeCopy).setId("id1");
    assertEquals(state.copiedEntityTypes(), ImmutableMap.of("A", entityTypeCopy));
    assertEquals(state.originalEntityTypeIds(), ImmutableMap.of("id1", "A"));
    assertEquals(state.referenceDefaultValues(), emptyMap());
    verify(dataService.getMeta()).addEntityType(entityTypeCopy);
    verify(progress, times(1)).increment(1);
  }

  @Test
  public void copyEntityTypesWithInternalReferences() {
    setupPredictableIdGeneratorMock(idGenerator);
    EntityType entityTypeA = mockEntityType("A");
    EntityType entityTypeB = mockEntityType("B");
    EntityType entityTypeACopy = mock(EntityType.class);
    EntityType entityTypeBCopy = mock(EntityType.class);
    Package targetPackage = mock(Package.class);
    Progress progress = mock(Progress.class);
    CopyState state = CopyState.create(targetPackage, progress);
    setupMetadataCopierAnswers(
        ImmutableMap.of(entityTypeA, entityTypeACopy, entityTypeB, entityTypeBCopy));
    when(entityTypeDependencyResolver.resolve(asList(entityTypeACopy, entityTypeBCopy)))
        .thenReturn(asList(entityTypeBCopy, entityTypeACopy));
    Attribute refAttrCopy = mock(Attribute.class);
    when(entityTypeACopy.getAtomicAttributes()).thenReturn(singletonList(refAttrCopy));
    when(refAttrCopy.getDataType()).thenReturn(AttributeType.XREF);
    when(refAttrCopy.getRefEntity()).thenReturn(entityTypeB);

    copier.copy(asList(entityTypeA, entityTypeB), state);

    verify(entityTypeACopy).setId("id1");
    verify(entityTypeBCopy).setId("id2");
    verify(refAttrCopy).setRefEntity(entityTypeBCopy);
    assertEquals(
        state.copiedEntityTypes(), ImmutableMap.of("A", entityTypeACopy, "B", entityTypeBCopy));
    assertEquals(state.originalEntityTypeIds(), ImmutableMap.of("id1", "A", "id2", "B"));
    assertEquals(state.referenceDefaultValues(), emptyMap());
    verify(dataService.getMeta()).addEntityType(entityTypeBCopy);
    verify(dataService.getMeta()).addEntityType(entityTypeACopy);
    verify(progress, times(2)).increment(1);
  }

  @Test
  public void copyWithDefaultReferenceValue() {
    setupPredictableIdGeneratorMock(idGenerator);
    EntityType entityType = mockEntityType("A");
    EntityType entityTypeCopy = mock(EntityType.class);
    Attribute refAttrCopy = mock(Attribute.class);
    when(entityTypeCopy.getAtomicAttributes()).thenReturn(singletonList(refAttrCopy));
    when(refAttrCopy.getIdentifier()).thenReturn("refAttr");
    when(refAttrCopy.getDataType()).thenReturn(AttributeType.MREF);
    when(refAttrCopy.hasDefaultValue()).thenReturn(true);
    when(refAttrCopy.getDefaultValue()).thenReturn("row1,row2,row3");
    Package targetPackage = mock(Package.class);
    Progress progress = mock(Progress.class);
    CopyState state = CopyState.create(targetPackage, progress);
    when(entityTypeMetadataCopier.copy(entityType, state)).thenReturn(entityTypeCopy);
    when(entityTypeDependencyResolver.resolve(singletonList(entityTypeCopy)))
        .thenReturn(singletonList(entityTypeCopy));

    copier.copy(singletonList(entityType), state);

    assertEquals(state.referenceDefaultValues(), ImmutableMap.of("refAttr", "row1,row2,row3"));
    verify(refAttrCopy).setDefaultValue("row1,row2,row3");
  }

  private void setupMetadataCopierAnswers(Map<EntityType, EntityType> mocks) {
    when(entityTypeMetadataCopier.copy(any(EntityType.class), any(CopyState.class)))
        .thenAnswer(
            (Answer<EntityType>)
                invocation -> {
                  EntityType entityType = invocation.getArgument(0);
                  return mocks.get(entityType);
                });
  }

  private static EntityType mockEntityType(String idLabel) {
    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn(idLabel);
    when(entityType.getLabel()).thenReturn(idLabel);
    return entityType;
  }

  /** Use for tests where copying metadata isn't under inspection */
  private void ignoreMetadataCopyResult() {
    EntityType entityTypeCopy = mock(EntityType.class);
    when(entityTypeMetadataCopier.copy(any(EntityType.class), any(CopyState.class)))
        .thenReturn(entityTypeCopy);
  }
}
