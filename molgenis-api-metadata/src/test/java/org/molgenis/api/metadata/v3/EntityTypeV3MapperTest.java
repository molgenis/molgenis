package org.molgenis.api.metadata.v3;

import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.molgenis.api.metadata.v3.model.CreateAttributeRequest;
import org.molgenis.api.metadata.v3.model.CreateEntityTypeRequest;
import org.molgenis.api.metadata.v3.model.I18nValue;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.Package;
import org.molgenis.test.AbstractMockitoTest;

@MockitoSettings(strictness = Strictness.LENIENT)
class EntityTypeV3MapperTest extends AbstractMockitoTest {
  @Mock private EntityTypeFactory entityTypeFactory;
  @Mock private MetaDataService metaDataService;
  private Object metadataV3Mapper;

  @BeforeEach
  void setUpBeforeMethod() {
    metadataV3Mapper = null;
  }

  @Test
  public void toEntityTypeResponse() {}

  @Test
  public void testToEntityTypeResponse() {}

  @Test
  public void toEntityType() {
    I18nValue entityLabel = I18nValue.create("entityLabel", new HashMap<>());
    I18nValue entityDescription = I18nValue.create("entityDescription", new HashMap<>());
    I18nValue attr1Label = I18nValue.create("attr1Label", new HashMap<>());
    I18nValue attr1Description = I18nValue.create("attr1Description", new HashMap<>());
    I18nValue attr2Label = I18nValue.create("attr2Label", new HashMap<>());
    I18nValue attr2Description = I18nValue.create("attr2Description", new HashMap<>());
    CreateAttributeRequest attr1 =
        new CreateAttributeRequest(
            "id",
            "name",
            "string",
            null,
            null,
            false,
            null,
            "desc",
            "expression",
            false,
            true,
            true,
            attr1Label,
            attr1Description,
            true,
            Collections.emptyList(),
            null,
            null,
            false,
            true,
            "nullableExpression",
            "visibleExpression",
            "validationExpression",
            "defaultValue",
            1);
    CreateAttributeRequest attr2 =
        new CreateAttributeRequest(
            "id2",
            "name2",
            "string",
            null,
            null,
            false,
            null,
            "desc",
            "expression",
            false,
            true,
            true,
            attr2Label,
            attr2Description,
            true,
            Collections.emptyList(),
            null,
            null,
            false,
            true,
            "nullableExpression",
            "visibleExpression",
            "validationExpression",
            "defaultValue",
            2);
    CreateEntityTypeRequest entityTypeRequest =
        new CreateEntityTypeRequest(
            "id",
            entityLabel,
            entityDescription,
            false,
            "pack",
            "parent",
            Arrays.asList(attr1, attr2),
            "attr1",
            "attr2",
            Arrays.<String>asList("attr1", "attr2"));

    EntityType expected = entityTypeFactory.create();

    Package pack = null;
    EntityType extendsEntityType = null;

    assertAll(
        () -> expected.setId("id"),
        () -> expected.setPackage(pack),
        () -> expected.setLabel("entityLabel"),
        () -> expected.setDescription("entityDescription"),
        // TODO Attrs
        () -> expected.setAbstract(false),
        () -> expected.setExtends(extendsEntityType),
        () -> expected.setBackend(metaDataService.getDefaultBackend().getName()));
    // () -> assertEquals(expected, metadataV3Mapper.toEntityType(entityTypeRequest)));
  }
}
