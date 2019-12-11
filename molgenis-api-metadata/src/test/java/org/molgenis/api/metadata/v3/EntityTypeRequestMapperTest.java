package org.molgenis.api.metadata.v3;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.api.metadata.v3.exception.ReadOnlyFieldException;
import org.molgenis.api.metadata.v3.model.CreateAttributeRequest;
import org.molgenis.api.metadata.v3.model.CreateEntityTypeRequest;
import org.molgenis.api.metadata.v3.model.I18nValue;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.Package;
import org.molgenis.test.AbstractMockitoTest;

class EntityTypeRequestMapperTest extends AbstractMockitoTest {
  @Mock private EntityTypeFactory entityTypeFactory;
  @Mock private AttributeRequestMapper attributeRequestMapper;
  @Mock private MetaDataService metaDataService;
  private EntityTypeRequestMapper entityTypeRequestMapper;

  @BeforeEach
  void setUpBeforeEach() {
    entityTypeRequestMapper =
        new EntityTypeRequestMapper(entityTypeFactory, attributeRequestMapper, metaDataService);
  }

  @Test
  void toEntityTypeMap() {
    EntityType entityType = mock(EntityType.class);

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("abstract", true);

    assertThrows(
        ReadOnlyFieldException.class,
        () -> entityTypeRequestMapper.toEntityType(entityType, valueMap));
  }

  @Test
  void toEntityTypePack() {
    EntityType entityType = mock(EntityType.class);
    Package pack = mock(Package.class);
    when(metaDataService.getPackage("pack1")).thenReturn(Optional.of(pack));

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("package", "pack1");
    entityTypeRequestMapper.toEntityType(entityType, valueMap);

    verify(entityType).setPackage(pack);
  }

  @Test
  void toEntityTypeExtends() {
    EntityType entityType = mock(EntityType.class);
    EntityType parent = mock(EntityType.class);
    when(metaDataService.getEntityType("parent")).thenReturn(Optional.of(parent));
    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("extends", "parent");

    entityTypeRequestMapper.toEntityType(entityType, valueMap);

    verify(entityType).setExtends(parent);
  }

  @Test
  void toEntityTypeI18n() {
    EntityType entityType = mock(EntityType.class);

    Map<String, Object> i18n = new HashMap<>();
    Map<String, String> translations = new HashMap<>();
    translations.put("nl", "nederlands");
    i18n.put("defaultValue", "default");
    i18n.put("translations", translations);
    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("label", i18n);
    valueMap.put("description", i18n);

    when(attributeRequestMapper.mapI18nValue(i18n))
        .thenReturn(
            I18nValue.builder()
                .setDefaultValue("default")
                .setTranslations(singletonMap("nl", "nederlands"))
                .build());

    entityTypeRequestMapper.toEntityType(entityType, valueMap);

    assertAll(
        () -> verify(entityType).setLabel("default"),
        () -> verify(entityType).setLabel("nl", "nederlands"),
        () -> verify(entityType).setDescription("default"),
        () -> verify(entityType).setDescription("nl", "nederlands"));
  }

  @Test
  void toEntityTypeAttrs() {
    EntityType entityType = mock(EntityType.class);

    Map<String, Object> valueMap = new HashMap<>();
    Map<String, Object> attrMap1 = new HashMap<>();
    Map<String, Object> attrMap2 = new HashMap<>();
    valueMap.put("attributes", Arrays.asList(attrMap1, attrMap2));

    Map<String, Attribute> attrs = new HashMap<>();
    Attribute attr1 = mock(Attribute.class);
    Attribute attr2 = mock(Attribute.class);
    attrs.put("1", attr1);
    attrs.put("2", attr2);
    when(attributeRequestMapper.toAttributes(Arrays.asList(attrMap1, attrMap2), entityType))
        .thenReturn(attrs);

    entityTypeRequestMapper.toEntityType(entityType, valueMap);

    verify(entityType).setOwnAllAttributes(attrs.values());
  }

  @Test
  void toEntityType() {
    String id = "MyEntityTypeId";
    I18nValue label = I18nValue.builder().setDefaultValue("My Entity Type").build();
    I18nValue description = null;
    Boolean abstract_ = false;
    String packageId = "MyPackageId";
    String extendsEntityTypeId = "MyExtendsEntityTypeId";
    ImmutableList<CreateAttributeRequest> attributes = ImmutableList.of();
    String idAttribute = null;
    String labelAttribute = null;
    ImmutableList<String> lookupAttributes = ImmutableList.of();
    CreateEntityTypeRequest createEntityTypeRequest =
        CreateEntityTypeRequest.builder()
            .setId(id)
            .setLabel(label)
            .setDescription(description)
            .setAbstract(abstract_)
            .setPackage(packageId)
            .setExtends(extendsEntityTypeId)
            .setAttributes(attributes)
            .build();

    Package aPackage = mock(Package.class);
    when(metaDataService.getPackage(packageId)).thenReturn(Optional.of(aPackage));
    EntityType extendsEntityType = mock(EntityType.class);
    when(metaDataService.getEntityType(extendsEntityTypeId))
        .thenReturn(Optional.of(extendsEntityType));
    EntityType entityType = mock(EntityType.class);
    RepositoryCollection repositoryCollection = mock(RepositoryCollection.class);
    when(repositoryCollection.getName()).thenReturn("PostgreSQL");
    when(metaDataService.getDefaultBackend()).thenReturn(repositoryCollection);
    when(entityTypeFactory.create()).thenReturn(entityType);
    EntityType mappedEntityType = entityTypeRequestMapper.toEntityType(createEntityTypeRequest);

    assertAll(
        () -> verify(mappedEntityType).setId("MyEntityTypeId"),
        () -> verify(mappedEntityType).setLabel("My Entity Type"),
        () -> verify(mappedEntityType).setBackend("PostgreSQL"),
        () -> verify(mappedEntityType).setPackage(aPackage),
        () -> verify(mappedEntityType).setExtends(extendsEntityType),
        () -> verify(mappedEntityType).setOwnAllAttributes(emptyList()),
        () -> verify(mappedEntityType).setAbstract(false),
        () -> verifyNoMoreInteractions(mappedEntityType));
  }
}
