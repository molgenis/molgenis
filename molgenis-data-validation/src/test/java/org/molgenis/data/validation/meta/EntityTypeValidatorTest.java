package org.molgenis.data.validation.meta;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.test.AbstractMockitoTest;

class EntityTypeValidatorTest extends AbstractMockitoTest {
  private EntityTypeValidator entityTypeValidator;
  @Mock private DataService dataService;
  @Mock private EntityType entityType;
  @Mock private EntityType parent;
  @Mock private Attribute idAttr;
  @Mock private Attribute labelAttr;
  @Mock private Attribute lookupAttr1;
  @Mock private Attribute lookupAttr2;
  @Mock private Package aPackage;
  @Mock private SystemEntityTypeRegistry systemEntityTypeRegistry;
  @Mock private MetaDataService metaDataService;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUpBeforeMethod() {
    entityTypeValidator = new EntityTypeValidator(dataService, systemEntityTypeRegistry);
  }

  @Test
  void testValidateEntityIdIsReservedKeyword() {
    when(entityType.getId()).thenReturn("logout");
    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () -> EntityTypeValidator.validateEntityId(entityType));
    assertThat(exception.getMessage())
        .containsPattern("Name \\[logout\\] is not allowed because it is a reserved keyword.");
  }

  @Test
  void testValidateEntityIdValid() {
    when(entityType.getId()).thenReturn("entity");
    EntityTypeValidator.validateEntityId(entityType);
  }

  @Test
  void testValidateLabelIsEmpty() {
    when(entityType.getId()).thenReturn("entity");
    when(entityType.getLabel()).thenReturn("");
    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () -> EntityTypeValidator.validateEntityLabel(entityType));
    assertThat(exception.getMessage()).containsPattern("Label of EntityType \\[entity\\] is empty");
  }

  @Test
  void testValidateLabelIsWhiteSpace() {
    when(entityType.getId()).thenReturn("entity");
    when(entityType.getLabel()).thenReturn("  ");
    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () -> EntityTypeValidator.validateEntityLabel(entityType));
    assertThat(exception.getMessage())
        .containsPattern("Label of EntityType \\[entity\\] contains only white space");
  }

  @Test
  void testValidateLabelIsValid() {
    when(entityType.getLabel()).thenReturn(" Label ");
    EntityTypeValidator.validateEntityLabel(entityType);
  }

  @Test
  void testValidatePackageNonSystem() {
    when(entityType.getPackage()).thenReturn(aPackage);
    when(aPackage.getId()).thenReturn("nosys");

    entityTypeValidator.validatePackage(entityType);
  }

  @Test
  void testValidatePackageNull() {
    when(entityType.getPackage()).thenReturn(null);

    entityTypeValidator.validatePackage(entityType);
  }

  @Test
  void testValidateSystemPackageInvalid() {
    when(entityType.getId()).thenReturn("entity");
    when(entityType.getPackage()).thenReturn(aPackage);
    when(aPackage.getId()).thenReturn(PACKAGE_SYSTEM);
    when(systemEntityTypeRegistry.hasSystemEntityType("entity")).thenReturn(false);

    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () -> entityTypeValidator.validatePackage(entityType));
    assertThat(exception.getMessage())
        .containsPattern("Adding entity \\[entity\\] to system package \\[sys\\] is not allowed");
  }

  @Test
  void testValidateSystemPackageValid() {
    when(entityType.getId()).thenReturn("entity");
    when(entityType.getPackage()).thenReturn(aPackage);
    when(aPackage.getId()).thenReturn(PACKAGE_SYSTEM);
    when(systemEntityTypeRegistry.hasSystemEntityType("entity")).thenReturn(true);

    entityTypeValidator.validatePackage(entityType);
  }

  @Test
  void testValidateOwnAttributesNoAttributes() {
    when(entityType.getId()).thenReturn("entity");
    when(entityType.getAllAttributes()).thenReturn(emptyList());

    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () -> EntityTypeValidator.validateOwnAttributes(entityType));
    assertThat(exception.getMessage())
        .containsPattern(
            "EntityType \\[entity\\] does not contain any attributes. "
                + "Did you use the correct package\\+entity name combination in both the entities as well as the attributes sheet\\?");
  }

  @Test
  void testValidateOwnAttributesAttributesWithSameName() {
    when(entityType.getId()).thenReturn("entity");
    when(entityType.getAllAttributes()).thenReturn(newArrayList(idAttr, labelAttr));
    when(idAttr.getName()).thenReturn("id");
    when(labelAttr.getName()).thenReturn("id");

    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () -> EntityTypeValidator.validateOwnAttributes(entityType));
    assertThat(exception.getMessage())
        .containsPattern("EntityType \\[entity\\] contains multiple attributes with name \\[id\\]");
  }

  @Test
  void testValidateOwnAttributesNonExistingParent() {
    when(entityType.getId()).thenReturn("entity");
    when(entityType.getAllAttributes()).thenReturn(newArrayList(idAttr, labelAttr));
    when(idAttr.getName()).thenReturn("id");
    when(labelAttr.getName()).thenReturn("label");
    Attribute parent = mock(Attribute.class);
    when(parent.getIdentifier()).thenReturn("non-existing-parent");
    when(parent.getName()).thenReturn("non-existing-parent");
    when(labelAttr.getParent()).thenReturn(parent);

    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () -> EntityTypeValidator.validateOwnAttributes(entityType));
    assertThat(exception.getMessage())
        .containsPattern(
            "Attribute \\[label\\] of EntityType \\[entity\\] has a non-existing parent attribute \\[non-existing-parent\\]");
  }

  @Test
  void testValidateOwnIdAttributeNull() {
    when(entityType.getId()).thenReturn("entity");
    when(entityType.getOwnIdAttribute()).thenReturn(null);

    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () ->
                EntityTypeValidator.validateOwnIdAttribute(
                    entityType, ImmutableMap.of("id", idAttr, "label", labelAttr)));
    assertThat(exception.getMessage())
        .containsPattern("EntityType \\[entity\\] is missing required ID attribute");
  }

  @Test
  void testValidateOwnIdAttributeNotListedInAllAttributes() {
    when(entityType.getId()).thenReturn("entity");
    when(entityType.getOwnIdAttribute()).thenReturn(idAttr);
    String idAttributeIdentifier = "abcde";
    String labelAttributeIdentifier = "defgh";
    when(idAttr.getIdentifier()).thenReturn(idAttributeIdentifier);
    when(idAttr.getName()).thenReturn("id");

    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () ->
                EntityTypeValidator.validateOwnIdAttribute(
                    entityType, ImmutableMap.of(labelAttributeIdentifier, labelAttr)));
    assertThat(exception.getMessage())
        .containsPattern(
            "EntityType \\[entity\\] ID attribute \\[id\\] is not part of the entity attributes");
  }

  @Test
  void testValidateOwnIdAttributeInvalidType() {
    when(entityType.getId()).thenReturn("entity");
    when(entityType.getOwnIdAttribute()).thenReturn(idAttr);
    String idAttributeIdentifier = "abcde";
    when(idAttr.getIdentifier()).thenReturn(idAttributeIdentifier);
    when(idAttr.getName()).thenReturn("id");
    when(idAttr.getDataType()).thenReturn(COMPOUND);

    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () ->
                EntityTypeValidator.validateOwnIdAttribute(
                    entityType, ImmutableMap.of(idAttributeIdentifier, idAttr)));
    assertThat(exception.getMessage())
        .containsPattern(
            "EntityType \\[entity\\] ID attribute \\[id\\] type \\[COMPOUND\\] is not allowed");
  }

  @Test
  void testValidateOwnIdAttributeNotUnique() {
    when(entityType.getId()).thenReturn("entity");
    when(entityType.getOwnIdAttribute()).thenReturn(idAttr);
    String idAttributeIdentifier = "abcde";
    when(idAttr.getIdentifier()).thenReturn(idAttributeIdentifier);
    when(idAttr.getName()).thenReturn("id");
    when(idAttr.getDataType()).thenReturn(STRING);
    when(idAttr.isUnique()).thenReturn(false);

    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () ->
                EntityTypeValidator.validateOwnIdAttribute(
                    entityType, ImmutableMap.of(idAttributeIdentifier, idAttr)));
    assertThat(exception.getMessage())
        .containsPattern("EntityType \\[entity\\] ID attribute \\[id\\] is not a unique attribute");
  }

  @Test
  void testValidateOwnIdAttributeNillable() {
    when(entityType.getId()).thenReturn("entity");
    when(entityType.getOwnIdAttribute()).thenReturn(idAttr);
    String idAttributeIdentifier = "abcde";
    when(idAttr.getIdentifier()).thenReturn(idAttributeIdentifier);
    when(idAttr.getName()).thenReturn("id");
    when(idAttr.getDataType()).thenReturn(STRING);
    when(idAttr.isUnique()).thenReturn(true);
    when(idAttr.isNillable()).thenReturn(true);

    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () ->
                EntityTypeValidator.validateOwnIdAttribute(
                    entityType, ImmutableMap.of(idAttributeIdentifier, idAttr)));
    assertThat(exception.getMessage())
        .containsPattern("EntityType \\[entity\\] ID attribute \\[id\\] cannot be nillable");
  }

  @Test
  void testValidateOwnIdAttributeNullAbstract() {
    when(entityType.getOwnIdAttribute()).thenReturn(null);
    when(entityType.isAbstract()).thenReturn(true);

    EntityTypeValidator.validateOwnIdAttribute(entityType, emptyMap());
  }

  @Test
  void testValidateOwnIdAttributeNullParentIdNull() {
    when(entityType.getId()).thenReturn("entity");
    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () -> EntityTypeValidator.validateOwnIdAttribute(entityType, emptyMap()));
    assertThat(exception.getMessage())
        .containsPattern("EntityType \\[entity\\] is missing required ID attribute");
  }

  @Test
  void testValidateOwnIdAttributeParentHasIdAttribute() {
    when(entityType.getOwnIdAttribute()).thenReturn(null);
    when(entityType.getIdAttribute()).thenReturn(idAttr);
    EntityTypeValidator.validateOwnIdAttribute(entityType, emptyMap());
  }

  @Test
  void testValidateOwnLabelAttributeNullIdAttributeVisible() {
    EntityTypeValidator.validateOwnLabelAttribute(entityType, emptyMap());
  }

  @Test
  void testValidateLabelAttributeHidden() {
    when(entityType.getLabelAttribute()).thenReturn(labelAttr);
    when(labelAttr.isVisible()).thenReturn(false);
    when(entityType.getId()).thenReturn("entity");
    when(labelAttr.getName()).thenReturn("labelAttr");
    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () -> EntityTypeValidator.validateLabelAttribute(entityType));
    assertThat(exception.getMessage())
        .containsPattern(
            "Label attribute \\[labelAttr\\] of EntityType \\[entity\\] must be visible.");
  }

  @Test
  void testValidateLabelAttributeNullIdAttributeInvisible() {
    when(entityType.getIdAttribute()).thenReturn(idAttr);
    when(idAttr.isVisible()).thenReturn(false);
    when(entityType.getId()).thenReturn("entity");
    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () -> EntityTypeValidator.validateLabelAttribute(entityType));
    assertThat(exception.getMessage())
        .containsPattern(
            "EntityType \\[entity\\] must define a label attribute because the identifier is hidden");
  }

  @Test
  void testValidateOwnLabelAttributeNotInAttributeMap() {
    when(entityType.getOwnLabelAttribute()).thenReturn(labelAttr);
    when(entityType.getId()).thenReturn("entity");
    when(labelAttr.getName()).thenReturn("label");
    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () -> EntityTypeValidator.validateOwnLabelAttribute(entityType, emptyMap()));
    assertThat(exception.getMessage())
        .containsPattern(
            "Label attribute \\[label\\] is not one of the attributes of entity \\[entity\\]");
  }

  @Test
  void testValidateOwnLabelAttributeValid() {
    when(entityType.getOwnLabelAttribute()).thenReturn(labelAttr);
    String labelAttributeId = "bcdef";
    when(labelAttr.getIdentifier()).thenReturn(labelAttributeId);
    EntityTypeValidator.validateOwnLabelAttribute(
        entityType, ImmutableMap.of(labelAttributeId, labelAttr));
  }

  @Test
  void testValidateOwnLabelAttributeNillable() {
    when(entityType.getOwnLabelAttribute()).thenReturn(labelAttr);
    when(entityType.getId()).thenReturn("entity");
    when(labelAttr.isNillable()).thenReturn(true);
    when(labelAttr.getIdentifier()).thenReturn("label");
    when(labelAttr.getName()).thenReturn("label");
    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () ->
                EntityTypeValidator.validateOwnLabelAttribute(
                    entityType, ImmutableMap.of("label", labelAttr)));
    assertThat(exception.getMessage())
        .containsPattern(
            "Label attribute \\[label\\] of entity type \\[entity\\] cannot be nillable");
  }

  @Test
  void testValidateLabelAttributeIdHidden() {
    when(entityType.getLabelAttribute()).thenReturn(null);
    when(entityType.getId()).thenReturn("package_name");
    when(entityType.getIdAttribute()).thenReturn(idAttr);
    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () -> EntityTypeValidator.validateLabelAttribute(entityType));
    assertThat(exception.getMessage())
        .containsPattern(
            "EntityType \\[package_name\\] must define a label attribute because the identifier is hidden");
  }

  @Test
  void testValidateOwnLabelAttributeAbstractEntity() {
    when(entityType.getOwnLabelAttribute()).thenReturn(null);
    EntityTypeValidator.validateOwnLabelAttribute(entityType, newHashMap());
  }

  @Test
  void testValidateOwnLookupAttributesEmpty() {
    when(entityType.getOwnLookupAttributes()).thenReturn(emptyList());
    EntityTypeValidator.validateOwnLookupAttributes(entityType, emptyMap());
  }

  @Test
  void testValidateOwnLookupAttributesNotInAttributeMap() {
    when(entityType.getId()).thenReturn("entity");
    when(entityType.getOwnLookupAttributes()).thenReturn(singletonList(lookupAttr1));
    when(lookupAttr1.getName()).thenReturn("lookup1");
    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () -> EntityTypeValidator.validateOwnLookupAttributes(entityType, emptyMap()));
    assertThat(exception.getMessage())
        .containsPattern(
            "Lookup attribute \\[lookup1\\] is not one of the attributes of entity \\[entity\\]");
  }

  @Test
  void testValidateOwnLookupAttributesInvisible() {
    when(entityType.getOwnLookupAttributes())
        .thenReturn(ImmutableList.of(lookupAttr1, lookupAttr2));
    String lookupAttr1Id = "abcde";
    String lookupAttr2Id = "defgh";
    when(lookupAttr1.getIdentifier()).thenReturn(lookupAttr1Id);
    when(lookupAttr2.getIdentifier()).thenReturn(lookupAttr2Id);
    when(lookupAttr1.isVisible()).thenReturn(true);
    when(lookupAttr2.isVisible()).thenReturn(false);
    when(entityType.getId()).thenReturn("entity");
    when(lookupAttr2.getName()).thenReturn("lookup2");
    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () ->
                EntityTypeValidator.validateOwnLookupAttributes(
                    entityType,
                    ImmutableMap.of(lookupAttr1Id, lookupAttr1, lookupAttr2Id, lookupAttr2)));
    assertThat(exception.getMessage())
        .containsPattern(
            "Lookup attribute \\[lookup2\\] of entity type \\[entity\\] must be visible");
  }

  @Test
  void testValidateOwnLookupAttributesValid() {
    when(entityType.getOwnLookupAttributes())
        .thenReturn(ImmutableList.of(lookupAttr1, lookupAttr2));
    String lookupAttr1Id = "abcde";
    String lookupAttr2Id = "defgh";
    when(lookupAttr1.getIdentifier()).thenReturn(lookupAttr1Id);
    when(lookupAttr2.getIdentifier()).thenReturn(lookupAttr2Id);
    when(lookupAttr1.isVisible()).thenReturn(true);
    when(lookupAttr2.isVisible()).thenReturn(true);
    EntityTypeValidator.validateOwnLookupAttributes(
        entityType, ImmutableMap.of(lookupAttr1Id, lookupAttr1, lookupAttr2Id, lookupAttr2));
  }

  @Test
  void testValidateExtendsNonAbstract() {
    when(entityType.getExtends()).thenReturn(parent);
    when(parent.isAbstract()).thenReturn(false);
    when(entityType.getId()).thenReturn("entity");
    when(parent.getId()).thenReturn("parent");
    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () -> EntityTypeValidator.validateExtends(entityType));
    assertThat(exception.getMessage())
        .containsPattern(
            "EntityType \\[parent\\] is not abstract; EntityType \\[entity\\] can't extend it");
  }

  @Test
  void testValidateExtendsValid() {
    when(entityType.getExtends()).thenReturn(parent);
    when(parent.isAbstract()).thenReturn(true);
    EntityTypeValidator.validateExtends(entityType);
  }

  @Test
  void testValidateBackendInvalid() {
    when(entityType.getBackend()).thenReturn("BackendName");
    when(dataService.getMeta()).thenReturn(metaDataService);
    when(metaDataService.hasBackend("BackendName")).thenReturn(false);
    Exception exception =
        assertThrows(
            MolgenisValidationException.class,
            () -> entityTypeValidator.validateBackend(entityType));
    assertThat(exception.getMessage()).containsPattern("Unknown backend \\[BackendName\\]");
  }

  @Test
  void testValidateBackendValid() {
    when(entityType.getBackend()).thenReturn("BackendName");
    when(dataService.getMeta()).thenReturn(metaDataService);
    when(metaDataService.hasBackend("BackendName")).thenReturn(true);
    entityTypeValidator.validateBackend(entityType);
  }

  @Test
  void testValidateValid() {
    when(entityType.getId()).thenReturn("entity");
    when(entityType.getLabel()).thenReturn("Label");
    when(entityType.getPackage()).thenReturn(null);
    when(entityType.getExtends()).thenReturn(null);
    when(idAttr.getIdentifier()).thenReturn("abcde");
    when(idAttr.getDataType()).thenReturn(STRING);
    when(idAttr.isUnique()).thenReturn(true);
    when(idAttr.isVisible()).thenReturn(true);
    when(entityType.getAllAttributes()).thenReturn(singletonList(idAttr));
    when(entityType.getOwnAllAttributes()).thenReturn(singletonList(idAttr));
    when(entityType.getOwnIdAttribute()).thenReturn(idAttr);
    when(entityType.getIdAttribute()).thenReturn(idAttr);
    when(entityType.getBackend()).thenReturn("PostgreSQL");
    when(dataService.getMeta()).thenReturn(metaDataService);
    when(metaDataService.hasBackend("PostgreSQL")).thenReturn(true);
    entityTypeValidator.validate(entityType);
  }
}
