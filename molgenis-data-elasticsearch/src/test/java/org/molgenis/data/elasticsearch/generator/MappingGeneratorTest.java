package org.molgenis.data.elasticsearch.generator;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.molgenis.data.elasticsearch.generator.model.FieldMapping;
import org.molgenis.data.elasticsearch.generator.model.Mapping;
import org.molgenis.data.elasticsearch.generator.model.MappingType;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;

@MockitoSettings(strictness = Strictness.LENIENT)
class MappingGeneratorTest extends AbstractMockitoTest {
  @Mock private DocumentIdGenerator documentIdGenerator;

  private MappingGenerator mappingGenerator;

  @BeforeEach
  void setUpBeforeMethod() {
    mappingGenerator = new MappingGenerator(documentIdGenerator);
  }

  @Test
  void testMappingGenerator() {
    assertThrows(NullPointerException.class, () -> new MappingGenerator(null));
  }

  static Iterator<Object[]> createMappingProvider() {
    List<Object[]> dataItems = new ArrayList<>();
    dataItems.add(new Object[] {AttributeType.BOOL, MappingType.BOOLEAN});
    dataItems.add(new Object[] {AttributeType.DATE, MappingType.DATE});
    dataItems.add(new Object[] {AttributeType.DATE_TIME, MappingType.DATE_TIME});
    dataItems.add(new Object[] {AttributeType.DECIMAL, MappingType.DOUBLE});
    dataItems.add(new Object[] {AttributeType.HTML, MappingType.TEXT});
    dataItems.add(new Object[] {AttributeType.HYPERLINK, MappingType.TEXT});
    dataItems.add(new Object[] {AttributeType.INT, MappingType.INTEGER});
    dataItems.add(new Object[] {AttributeType.LONG, MappingType.LONG});
    dataItems.add(new Object[] {AttributeType.SCRIPT, MappingType.TEXT});
    return dataItems.iterator();
  }

  @ParameterizedTest
  @MethodSource("createMappingProvider")
  void testCreateMapping(AttributeType attributeType, MappingType mappingType) {
    initDocumentIdGeneratorMock();
    String attrIdentifier = "attr";
    EntityType entityType = createEntityType(attrIdentifier, attributeType);
    Mapping mapping = mappingGenerator.createMapping(entityType);

    FieldMapping fieldMapping =
        FieldMapping.builder().setName(attrIdentifier).setType(mappingType).build();
    Mapping expectedMapping = createMapping(fieldMapping);
    assertEquals(expectedMapping, mapping);
  }

  static Iterator<Object[]> createMappingProviderNested() {
    List<Object[]> dataItems = new ArrayList<>();
    dataItems.add(new Object[] {AttributeType.CATEGORICAL});
    dataItems.add(new Object[] {AttributeType.CATEGORICAL_MREF});
    dataItems.add(new Object[] {AttributeType.FILE});
    dataItems.add(new Object[] {AttributeType.ONE_TO_MANY});
    dataItems.add(new Object[] {AttributeType.MREF});
    dataItems.add(new Object[] {AttributeType.XREF});
    return dataItems.iterator();
  }

  @ParameterizedTest
  @MethodSource("createMappingProviderNested")
  void testCreateMappingProviderNested(AttributeType attributeType) {
    initDocumentIdGeneratorMock();
    String refAttrIdentifier = "refAttr";
    EntityType refEntityType = createEntityType(refAttrIdentifier, AttributeType.LONG);
    String attrIdentifier = "attr";
    EntityType entityType = createEntityType(attrIdentifier, attributeType, refEntityType);

    Mapping mapping = mappingGenerator.createMapping(entityType);

    FieldMapping fieldMapping =
        FieldMapping.builder()
            .setName(attrIdentifier)
            .setType(MappingType.NESTED)
            .setNestedFieldMappings(
                singletonList(
                    FieldMapping.builder()
                        .setName(refAttrIdentifier)
                        .setType(MappingType.LONG)
                        .build()))
            .build();
    Mapping expectedMapping = createMapping(fieldMapping);
    assertEquals(expectedMapping, mapping);
  }

  static Iterator<Object[]> createMappingProviderDepth() {
    String refRefAttrIdentifier = "refRefAttr";
    EntityType refRefEntityType = createEntityType(refRefAttrIdentifier, AttributeType.LONG);
    String refAttrIdentifier = "refAttr";
    EntityType refEntityType =
        createEntityType(refAttrIdentifier, AttributeType.XREF, refRefEntityType);
    String attrIdentifier = "attr";

    EntityType entityTypeDepth0 =
        createEntityType(attrIdentifier, AttributeType.MREF, refEntityType, 0);
    when(entityTypeDepth0.toString()).thenReturn("entityTypeDepth0");
    FieldMapping fieldMapping0 =
        FieldMapping.builder().setName(attrIdentifier).setType(MappingType.LONG).build();

    EntityType entityTypeDepth1 =
        createEntityType(attrIdentifier, AttributeType.MREF, refEntityType, 1);
    when(entityTypeDepth1.toString()).thenReturn("entityTypeDepth1");
    FieldMapping fieldMapping1 =
        FieldMapping.builder()
            .setName(attrIdentifier)
            .setType(MappingType.NESTED)
            .setNestedFieldMappings(
                singletonList(
                    FieldMapping.builder()
                        .setName(refAttrIdentifier)
                        .setType(MappingType.LONG)
                        .build()))
            .build();

    EntityType entityTypeDepth2 =
        createEntityType(attrIdentifier, AttributeType.MREF, refEntityType, 2);
    when(entityTypeDepth2.toString()).thenReturn("entityTypeDepth2");
    FieldMapping fieldMapping2 =
        FieldMapping.builder()
            .setName(attrIdentifier)
            .setType(MappingType.NESTED)
            .setNestedFieldMappings(
                singletonList(
                    FieldMapping.builder()
                        .setName(refAttrIdentifier)
                        .setType(MappingType.NESTED)
                        .setNestedFieldMappings(
                            singletonList(
                                FieldMapping.builder()
                                    .setName(refRefAttrIdentifier)
                                    .setType(MappingType.LONG)
                                    .build()))
                        .build()))
            .build();

    List<Object[]> dataItems = new ArrayList<>();
    dataItems.add(new Object[] {entityTypeDepth0, fieldMapping0});
    dataItems.add(new Object[] {entityTypeDepth1, fieldMapping1});
    dataItems.add(new Object[] {entityTypeDepth2, fieldMapping2});
    return dataItems.iterator();
  }

  @ParameterizedTest
  @MethodSource("createMappingProviderDepth")
  void testCreateMappingProviderDepth(EntityType entityType, FieldMapping fieldMapping) {
    initDocumentIdGeneratorMock();
    Mapping mapping = mappingGenerator.createMapping(entityType);
    Mapping expectedMapping = createMapping(fieldMapping);
    assertEquals(expectedMapping, mapping);
  }

  private static EntityType createEntityType(String attrIdentifier, AttributeType type) {
    return createEntityType(attrIdentifier, type, null);
  }

  private static EntityType createEntityType(
      String attrIdentifier, AttributeType type, EntityType refEntityType) {
    return createEntityType(attrIdentifier, type, refEntityType, 1);
  }

  private static EntityType createEntityType(
      String attrIdentifier, AttributeType type, EntityType refEntityType, int indexingDepth) {
    Attribute attribute = mock(Attribute.class);
    when(attribute.getIdentifier()).thenReturn(attrIdentifier);
    when(attribute.getDataType()).thenReturn(type);
    if (refEntityType != null) {
      when(attribute.getRefEntity()).thenReturn(refEntityType);
    }

    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("id");
    when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));
    when(entityType.getLabelAttribute()).thenReturn(attribute);
    when(entityType.getIndexingDepth()).thenReturn(indexingDepth);
    return entityType;
  }

  private static Mapping createMapping(FieldMapping fieldMapping) {
    return Mapping.builder().setType("id").setFieldMappings(singletonList(fieldMapping)).build();
  }

  private void initDocumentIdGeneratorMock() {
    doAnswer(invocation -> invocation.<EntityType>getArgument(0).getId())
        .when(documentIdGenerator)
        .generateId(any(EntityType.class));
    doAnswer(invocation -> invocation.<Attribute>getArgument(0).getIdentifier())
        .when(documentIdGenerator)
        .generateId(any(Attribute.class));
  }
}
