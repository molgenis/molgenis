package org.molgenis.api.metadata.v3.job;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.Sort;
import org.molgenis.data.Sort.Direction;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.meta.model.TagMetadata;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.test.IsEqualJson;

class EntityTypeSerializerImplTest extends AbstractMockitoTest {
  @Mock private DataService dataService;
  @Mock private EntityTypeFactory entityTypeFactory;
  @Mock private AttributeFactory attributeFactory;
  private EntityTypeSerializerImpl entityTypeSerializerImpl;

  @BeforeEach
  void setUpBeforeEach() {
    entityTypeSerializerImpl =
        new EntityTypeSerializerImpl(dataService, entityTypeFactory, attributeFactory);
  }

  @Test
  void serializeEntityType() {
    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("MyEntityTypeId");
    when(entityType.getLabel()).thenReturn("My Entity Type");
    doReturn("My Entity Type (en)").when(entityType).getString(EntityTypeMetadata.LABEL + "En");
    when(entityType.getBackend()).thenReturn("PostgreSQL");

    String serializedEntityType =
        "{\"id\":\"MyEntityTypeId\",\"label\":\"My Entity Type\",\"labelI18n\":{\"en\":\"My Entity Type (en)\"},\"descriptionI18n\":{},\"attributes\":[],\"abstract0\":false,\"tagIds\":[],\"backend\":\"PostgreSQL\",\"indexingDepth\":0}";
    String actualSerializedEntityType = entityTypeSerializerImpl.serializeEntityType(entityType);
    assertTrue(new IsEqualJson(serializedEntityType).matches(actualSerializedEntityType));
  }

  @Test
  void serializeEntityTypeWithAttributes() {
    Package aPackage = mock(Package.class);
    when(aPackage.getId()).thenReturn("MyPackage");

    EntityType refEntityType = mock(EntityType.class);
    when(refEntityType.getId()).thenReturn("MyRefEntityType");

    Attribute mappedByAttribute = mock(Attribute.class);
    when(mappedByAttribute.getIdentifier()).thenReturn("myMappedByAttributeId");

    Tag attributeTag = mock(Tag.class);
    when(attributeTag.getId()).thenReturn("MyAttributeTagId");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("MyEntityTypeId");

    Attribute attribute = mock(Attribute.class);
    when(attribute.getIdentifier()).thenReturn("MyAttributeId");
    when(attribute.getName()).thenReturn("myAttributeName");
    when(attribute.getEntity()).thenReturn(entityType);
    when(attribute.getSequenceNumber()).thenReturn(4);
    when(attribute.getDataType()).thenReturn(AttributeType.ONE_TO_MANY);
    when(attribute.isIdAttribute()).thenReturn(true);
    when(attribute.isLabelAttribute()).thenReturn(true);
    when(attribute.getLookupAttributeIndex()).thenReturn(3);
    when(attribute.hasRefEntity()).thenReturn(true);
    when(attribute.getRefEntity()).thenReturn(refEntityType);
    when(attribute.getCascadeDelete()).thenReturn(true);
    when(attribute.getMappedBy()).thenReturn(mappedByAttribute);
    when(attribute.getOrderBy()).thenReturn(new Sort("myAttributeName", Direction.DESC));
    when(attribute.getLabel()).thenReturn("My Attribute");
    when(attribute.getDescription()).thenReturn("My Attribute description");
    when(attribute.isNillable()).thenReturn(true);
    when(attribute.isVisible()).thenReturn(true);
    when(attribute.isUnique()).thenReturn(true);
    when(attribute.isReadOnly()).thenReturn(true);
    when(attribute.isAggregatable()).thenReturn(true);
    when(attribute.getExpression()).thenReturn("MyExpression");
    when(attribute.getRangeMin()).thenReturn(null);
    when(attribute.getRangeMax()).thenReturn(null);
    when(attribute.getTags()).thenReturn(singletonList(attributeTag));
    when(attribute.getNullableExpression()).thenReturn("MyNullableExpression");
    when(attribute.getVisibleExpression()).thenReturn("MyVisibleExpression");
    when(attribute.getValidationExpression()).thenReturn("MyValidationExpression");
    when(attribute.getDefaultValue()).thenReturn("MyDefaultValue");

    EntityType extendsEntityType = mock(EntityType.class);
    when(extendsEntityType.getId()).thenReturn("MyExtendsEntityTypeId");

    Tag tag = mock(Tag.class);
    when(tag.getId()).thenReturn("MyTagId");

    when(entityType.getPackage()).thenReturn(aPackage);
    when(entityType.getLabel()).thenReturn("My Entity Type");
    when(entityType.getDescription()).thenReturn("My Entity Type description");
    when(entityType.getOwnAllAttributes()).thenReturn(singletonList(attribute));
    when(entityType.isAbstract()).thenReturn(true);
    when(entityType.getExtends()).thenReturn(extendsEntityType);
    when(entityType.getTags()).thenReturn(singletonList(tag));
    when(entityType.getBackend()).thenReturn("PostgreSQL");
    when(entityType.getIndexingDepth()).thenReturn(2);

    String serializedEntityType =
        "{\"id\":\"MyEntityTypeId\",\"packageId\":\"MyPackage\",\"label\":\"My Entity Type\",\"labelI18n\":{},\"description\":\"My Entity Type description\",\"descriptionI18n\":{},\"attributes\":[{\"id\":\"MyAttributeId\",\"name\":\"myAttributeName\",\"entityTypeId\":\"MyEntityTypeId\",\"sequenceNr\":4,\"type\":\"onetomany\",\"idAttribute\":true,\"labelAttribute\":true,\"lookupAttributeIndex\":3,\"refEntityTypeId\":\"MyRefEntityType\",\"cascadeDelete\":true,\"mappedById\":\"myMappedByAttributeId\",\"orderBy\":\"myAttributeName,DESC\",\"label\":\"My Attribute\",\"labelI18n\":{},\"description\":\"My Attribute description\",\"descriptionI18n\":{},\"nullable\":true,\"auto\":false,\"visible\":true,\"unique\":true,\"readOnly\":true,\"aggregatable\":true,\"expression\":\"MyExpression\",\"enumOptions\":[],\"tagIds\":[\"MyAttributeTagId\"],\"nullableExpression\":\"MyNullableExpression\",\"visibleExpression\":\"MyVisibleExpression\",\"validationExpression\":\"MyValidationExpression\",\"defaultValue\":\"MyDefaultValue\"}],\"abstract0\":true,\"extendsId\":\"MyExtendsEntityTypeId\",\"tagIds\":[\"MyTagId\"],\"backend\":\"PostgreSQL\",\"indexingDepth\":2}";
    String actualSerializedEntityType = entityTypeSerializerImpl.serializeEntityType(entityType);
    assertTrue(new IsEqualJson(serializedEntityType).matches(actualSerializedEntityType));
  }

  @Test
  void deserializeEntityType() {
    String serializedEntityType =
        "{\"id\":\"MyEntityTypeId\",\"label\":\"My Entity Type\",\"labelI18n\":{\"en\":\"My Entity Type (en)\"},\"descriptionI18n\":{},\"attributes\":[],\"abstract0\":false,\"tagIds\":[],\"backend\":\"PostgreSQL\",\"indexingDepth\":0}";
    EntityType entityType = mock(EntityType.class);
    when(entityTypeFactory.create()).thenReturn(entityType);
    EntityType deserializedEntityType =
        entityTypeSerializerImpl.deserializeEntityType(serializedEntityType);
    assertAll(
        () -> verify(deserializedEntityType).setId("MyEntityTypeId"),
        () -> verify(deserializedEntityType).setLabel("My Entity Type"),
        () -> verify(deserializedEntityType).setBackend("PostgreSQL"),
        () -> verify(deserializedEntityType).setLabel("en", "My Entity Type (en)"));
  }

  @SuppressWarnings("unchecked")
  @Test
  void deserializeEntityTypeWithAttributes() {
    String serializedEntityType =
        "{\"id\":\"MyEntityTypeId\",\"packageId\":\"MyPackage\",\"label\":\"My Entity Type\",\"labelI18n\":{},\"description\":\"My Entity Type description\",\"descriptionI18n\":{},\"attributes\":[{\"id\":\"MyAttributeId\",\"name\":\"myAttributeName\",\"entityTypeId\":\"MyEntityTypeId\",\"sequenceNr\":4,\"type\":\"onetomany\",\"idAttribute\":true,\"labelAttribute\":true,\"lookupAttributeIndex\":3,\"refEntityTypeId\":\"MyRefEntityType\",\"cascadeDelete\":true,\"mappedById\":\"myMappedByAttributeId\",\"orderBy\":\"myAttributeName,DESC\",\"label\":\"My Attribute\",\"labelI18n\":{},\"description\":\"My Attribute description\",\"descriptionI18n\":{},\"nullable\":true,\"auto\":false,\"visible\":true,\"unique\":true,\"readOnly\":true,\"aggregatable\":true,\"expression\":\"MyExpression\",\"enumOptions\":[],\"tagIds\":[\"MyAttributeTagId\"],\"nullableExpression\":\"MyNullableExpression\",\"visibleExpression\":\"MyVisibleExpression\",\"validationExpression\":\"MyValidationExpression\",\"defaultValue\":\"MyDefaultValue\"}],\"abstract0\":true,\"extendsId\":\"MyExtendsEntityTypeId\",\"tagIds\":[\"MyTagId\"],\"backend\":\"PostgreSQL\",\"indexingDepth\":2}";
    EntityType entityType = mock(EntityType.class);
    when(entityTypeFactory.create()).thenReturn(entityType);
    Attribute attribute = mock(Attribute.class);
    when(attributeFactory.create()).thenReturn(attribute);

    doReturn(entityType)
        .when(dataService)
        .findOneById(
            EntityTypeMetadata.ENTITY_TYPE_META_DATA,
            "MyEntityTypeId",
            new Fetch().field(EntityTypeMetadata.ID),
            EntityType.class);
    EntityType extendsEntityType = mock(EntityType.class);
    doReturn(extendsEntityType)
        .when(dataService)
        .findOneById(
            EntityTypeMetadata.ENTITY_TYPE_META_DATA,
            "MyExtendsEntityTypeId",
            new Fetch().field(EntityTypeMetadata.ID),
            EntityType.class);
    EntityType refEntityType = mock(EntityType.class);
    doReturn(refEntityType)
        .when(dataService)
        .findOneById(
            EntityTypeMetadata.ENTITY_TYPE_META_DATA,
            "MyRefEntityType",
            new Fetch().field(EntityTypeMetadata.ID),
            EntityType.class);
    Package entityTypePackage = mock(Package.class);
    when(dataService.findOneById(
            PackageMetadata.PACKAGE,
            "MyPackage",
            new Fetch().field(PackageMetadata.ID),
            Package.class))
        .thenReturn(entityTypePackage);
    Attribute mappedByAttribute = mock(Attribute.class);
    when(dataService.findOneById(
            AttributeMetadata.ATTRIBUTE_META_DATA,
            "myMappedByAttributeId",
            new Fetch().field(AttributeMetadata.ID),
            Attribute.class))
        .thenReturn(mappedByAttribute);
    Tag entityTypeTag = mock(Tag.class);
    Tag attributeTag = mock(Tag.class);
    when(dataService.findAll(
            eq(TagMetadata.TAG),
            any(Stream.class),
            eq(new Fetch().field(TagMetadata.ID)),
            eq(Tag.class)))
        .thenReturn(Stream.of(entityTypeTag))
        .thenReturn(Stream.of(attributeTag));

    EntityType deserializedEntityType =
        entityTypeSerializerImpl.deserializeEntityType(serializedEntityType);

    assertAll(
        () -> verify(deserializedEntityType).setId("MyEntityTypeId"),
        () -> verify(deserializedEntityType).setPackage(entityTypePackage),
        () -> verify(deserializedEntityType).setLabel("My Entity Type"),
        () -> verify(deserializedEntityType).setDescription("My Entity Type description"),
        () -> verify(deserializedEntityType).setOwnAllAttributes(singletonList(attribute)),
        () -> verify(deserializedEntityType).setAbstract(true),
        () -> verify(deserializedEntityType).setExtends(extendsEntityType),
        () -> verify(deserializedEntityType).setTags(singletonList(entityTypeTag)),
        () -> verify(deserializedEntityType).setBackend("PostgreSQL"),
        () -> verify(deserializedEntityType).setIndexingDepth(2),
        () -> verifyNoMoreInteractions(deserializedEntityType),
        () -> verify(attribute).setIdentifier("MyAttributeId"),
        () -> verify(attribute).setName("myAttributeName"),
        () -> verify(attribute).setEntity(entityType),
        () -> verify(attribute).setSequenceNumber(4),
        () -> verify(attribute).setDataType(AttributeType.ONE_TO_MANY),
        () -> verify(attribute).setIdAttribute(true),
        () -> verify(attribute).setLabelAttribute(true),
        () -> verify(attribute).setLabelAttribute(true),
        () -> verify(attribute).setLookupAttributeIndex(3),
        () -> verify(attribute).setRefEntity(refEntityType),
        () -> verify(attribute).setCascadeDelete(true),
        () -> verify(attribute).setMappedBy(mappedByAttribute),
        () -> verify(attribute).setOrderBy(new Sort("myAttributeName", Direction.DESC)),
        () -> verify(attribute).setLabel("My Attribute"),
        () -> verify(attribute).setDescription("My Attribute description"),
        () -> verify(attribute).setNillable(true),
        () -> verify(attribute).setAuto(false),
        () -> verify(attribute).setVisible(true),
        () -> verify(attribute).setUnique(true),
        () -> verify(attribute).setReadOnly(true),
        () -> verify(attribute).setAggregatable(true),
        () -> verify(attribute).setExpression("MyExpression"),
        () -> verify(attribute).setEnumOptions(emptyList()),
        () -> verify(attribute).setTags(singletonList(attributeTag)),
        () -> verify(attribute).setNullableExpression("MyNullableExpression"),
        () -> verify(attribute).setVisibleExpression("MyVisibleExpression"),
        () -> verify(attribute).setValidationExpression("MyValidationExpression"),
        () -> verify(attribute).setDefaultValue("MyDefaultValue"),
        () -> verify(attribute).setRangeMin(null),
        () -> verify(attribute).setRangeMax(null),
        () -> verify(attribute).setParent(null),
        () -> verifyNoMoreInteractions(attribute));
  }
}
