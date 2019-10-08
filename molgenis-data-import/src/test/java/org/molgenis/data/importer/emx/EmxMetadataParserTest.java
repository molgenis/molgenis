package org.molgenis.data.importer.emx;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.molgenis.data.importer.emx.EmxMetadataParser.DEFAULT_NAMESPACE;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES_RANGE_MAX;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES_RANGE_MIN;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES_ABSTRACT;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES_BACKEND;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES_DESCRIPTION;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES_EXTENDS;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES_LABEL;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES_NAME;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES_PACKAGE;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES_TAGS;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_PACKAGE_NAME;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_PACKAGE_PARENT;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL_MREF;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.meta.AttributeType.FILE;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.TagMetadata.TAG;
import static org.molgenis.data.validation.meta.AttributeValidator.ValidationMode.ADD_SKIP_ENTITY_VALIDATION;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Range;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.UnknownTagException;
import org.molgenis.data.i18n.model.L10nString;
import org.molgenis.data.i18n.model.L10nStringFactory;
import org.molgenis.data.i18n.model.LanguageFactory;
import org.molgenis.data.importer.EntitiesValidationReport;
import org.molgenis.data.importer.MyEntitiesValidationReport;
import org.molgenis.data.importer.ParsedMetaData;
import org.molgenis.data.importer.emx.exception.InconsistentPackageStructureException;
import org.molgenis.data.importer.emx.exception.InvalidDataTypeException;
import org.molgenis.data.importer.emx.exception.InvalidEmxAttributeException;
import org.molgenis.data.importer.emx.exception.InvalidRangeException;
import org.molgenis.data.importer.emx.exception.InvalidValueException;
import org.molgenis.data.importer.emx.exception.MissingEmxAttributeAttributeValueException;
import org.molgenis.data.importer.emx.exception.NillableReferenceAggregatableException;
import org.molgenis.data.importer.emx.exception.UnknownEmxAttributeException;
import org.molgenis.data.importer.emx.exception.UnknownEntityValueException;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.meta.model.TagFactory;
import org.molgenis.data.validation.meta.AttributeValidator;
import org.molgenis.data.validation.meta.EntityTypeValidator;
import org.molgenis.data.validation.meta.TagValidator;
import org.molgenis.test.AbstractMockitoTest;

@MockitoSettings(strictness = Strictness.LENIENT)
class EmxMetadataParserTest extends AbstractMockitoTest {
  @Mock private DataService dataService;
  @Mock private PackageFactory packageFactory;
  @Mock private AttributeFactory attrMetaFactory;
  @Mock private EntityTypeFactory entityTypeFactory;
  @Mock private TagFactory tagFactory;
  @Mock private LanguageFactory languageFactory;
  @Mock private L10nStringFactory l10nStringFactory;
  @Mock private EntityTypeValidator entityTypeValidator;
  @Mock private AttributeValidator attributeValidator;
  @Mock private TagValidator tagValidator;
  @Mock private EntityTypeDependencyResolver entityTypeDependencyResolver;

  private EmxMetadataParser emxMetadataParser;

  @BeforeEach
  void setUpBeforeMethod() {
    emxMetadataParser =
        new EmxMetadataParser(
            dataService,
            packageFactory,
            attrMetaFactory,
            entityTypeFactory,
            tagFactory,
            languageFactory,
            l10nStringFactory,
            entityTypeValidator,
            attributeValidator,
            tagValidator,
            entityTypeDependencyResolver);
  }

  @Test
  void testParse() {
    initMetaFactories();

    MetaDataService metaDataService = createMetaDataService();
    when(dataService.getMeta()).thenReturn(metaDataService);

    RepositoryCollection repositoryCollection = createEmxRepositoryCollection();
    ParsedMetaData expectedParsedMetaData =
        new ParsedMetaData(
            ImmutableList.of(),
            ImmutableMap.of(),
            ImmutableMap.of(),
            ImmutableMap.of(),
            ImmutableMap.of());
    ParsedMetaData parsedMetaData = emxMetadataParser.parse(repositoryCollection, null);
    assertEquals(expectedParsedMetaData, parsedMetaData);
  }

  @Test
  void testValidate() {
    initMetaFactories();

    MetaDataService metaDataService = createMetaDataService();
    when(dataService.getMeta()).thenReturn(metaDataService);

    RepositoryCollection repositoryCollection = createEmxRepositoryCollection();
    MyEntitiesValidationReport expectedEntitiesValidationReport = new MyEntitiesValidationReport();
    expectedEntitiesValidationReport.addEntity("IdOnlyEntityType", true);

    EntitiesValidationReport entitiesValidationReport =
        emxMetadataParser.validate(repositoryCollection);

    assertEquals(expectedEntitiesValidationReport, entitiesValidationReport);
    // 6: name, entity, dataType, idAttribute, labelAttribute, visible
    verify(attributeValidator, times(6))
        .validate(any(Attribute.class), eq(ADD_SKIP_ENTITY_VALIDATION));
  }

  @Test
  void testCheckRequiredAttributeAttributes() {
    emxMetadataParser.checkRequiredAttributeAttributes(0, "name", "entity");
  }

  @Test
  void testCheckRequiredAttributeAttributesNoName() {
    assertThrows(
        MissingEmxAttributeAttributeValueException.class,
        () -> emxMetadataParser.checkRequiredAttributeAttributes(0, "", "entity"));
  }

  @Test
  void testCheckRequiredAttributeAttributesNoEntity() {
    assertThrows(
        MissingEmxAttributeAttributeValueException.class,
        () -> emxMetadataParser.checkRequiredAttributeAttributes(0, "name", ""));
  }

  @Test
  void testCheckReferenceDatatypeRules() {
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(XREF);
    emxMetadataParser.checkReferenceDatatypeRules(0, "entityName", "emxName", attr, "refEntity");
  }

  @Test
  void testCheckReferenceDatatypeRulesNillableAggregatableRef() {
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(MREF);
    when(attr.isAggregatable()).thenReturn(true);
    when(attr.isNillable()).thenReturn(true);
    assertThrows(
        NillableReferenceAggregatableException.class,
        () ->
            emxMetadataParser.checkReferenceDatatypeRules(
                0, "entityName", "emxName", attr, "refEntity"));
  }

  @Test
  void testCheckReferenceDatatypeRulesNoRefEntity() {
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(CATEGORICAL_MREF);
    when(attr.getName()).thenReturn("name");
    assertThrows(
        MissingEmxAttributeAttributeValueException.class,
        () -> emxMetadataParser.checkReferenceDatatypeRules(0, "entityName", "emxName", attr, ""));
  }

  @Test
  void testCheckReferenceDatatypeRulesNoRefEntityFile() {
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(FILE);
    when(attr.getName()).thenReturn("name");
    emxMetadataParser.checkReferenceDatatypeRules(0, "entityName", "emxName", attr, "");
  }

  @Test
  void validatePartOfAttribute() {
    initMetaFactories();

    Entity emxAttrEntity = mock(Entity.class);
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(COMPOUND);
    EmxAttribute emxAttr = mock(EmxAttribute.class);
    when(emxAttr.getAttr()).thenReturn(attr);
    IntermediateParseResults intermediateParseResults =
        new IntermediateParseResults(entityTypeFactory);
    emxMetadataParser.validatePartOfAttribute(
        0, "entityTypeId", "attributeName", "partOfAttribute", emxAttr);
  }

  @Test
  void setIdAttrAuto() {
    EmxAttribute emxAttr = mock(EmxAttribute.class);
    Attribute attr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    emxMetadataParser.setIdAttr(0, emxAttr, attr, "auto");
    verify(attr).setAuto(true);
    verify(emxAttr).setIdAttr(true);
  }

  @Test
  void setIdAttrTrue() {
    EmxAttribute emxAttr = mock(EmxAttribute.class);
    Attribute attr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    emxMetadataParser.setIdAttr(0, emxAttr, attr, "true");
    verify(attr, times(0)).setAuto(true);
    verify(emxAttr).setIdAttr(true);
  }

  @Test
  void setIdAttrFalse() {
    EmxAttribute emxAttr = mock(EmxAttribute.class);
    Attribute attr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    emxMetadataParser.setIdAttr(0, emxAttr, attr, "false");
    verify(attr, times(0)).setAuto(true);
    verify(emxAttr, times(0)).setIdAttr(true);
  }

  @Test
  void setIdAttrInvalid() {
    initMetaFactories();

    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(COMPOUND);
    EmxAttribute emxAttr = mock(EmxAttribute.class);
    when(emxAttr.getAttr()).thenReturn(attr);
    assertThrows(
        InvalidValueException.class, () -> emxMetadataParser.setIdAttr(0, emxAttr, attr, "test"));
  }

  @Test
  void validateAutoAttrValueInvalid() {
    initMetaFactories();

    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(COMPOUND);
    when(attr.isAuto()).thenReturn(true);
    when(attr.getName()).thenReturn("auto_attr");
    EmxAttribute emxAttr = mock(EmxAttribute.class);
    when(emxAttr.getAttr()).thenReturn(attr);
    assertThrows(
        InvalidDataTypeException.class,
        () -> emxMetadataParser.validateAutoAttrValue(0, attr, "emxEntityName"));
  }

  @Test
  void validateAutoAttrValue() {
    initMetaFactories();

    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(STRING);
    when(attr.isAuto()).thenReturn(true);
    when(attr.getName()).thenReturn("auto_attr");
    EmxAttribute emxAttr = mock(EmxAttribute.class);
    when(emxAttr.getAttr()).thenReturn(attr);
    emxMetadataParser.validateAutoAttrValue(0, attr, "emxEntityName");
    // No exception expected
  }

  @Test
  void setRangeInvalidMin() {
    initMetaFactories();

    Entity emxAttrEntity = mock(Entity.class);
    when(emxAttrEntity.getString(EMX_ATTRIBUTES_RANGE_MIN)).thenReturn("test");
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(COMPOUND);
    EmxAttribute emxAttr = mock(EmxAttribute.class);
    when(emxAttr.getAttr()).thenReturn(attr);
    assertThrows(
        InvalidRangeException.class,
        () -> emxMetadataParser.setRange(emxAttrEntity, "emxEntityName", "emxAttrName", attr, 0));
  }

  @Test
  void setRangeInvalidMax() {
    initMetaFactories();

    Entity emxAttrEntity = mock(Entity.class);
    when(emxAttrEntity.getString(EMX_ATTRIBUTES_RANGE_MIN)).thenReturn("1");
    when(emxAttrEntity.getString(EMX_ATTRIBUTES_RANGE_MAX)).thenReturn("test");
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(COMPOUND);
    EmxAttribute emxAttr = mock(EmxAttribute.class);
    when(emxAttr.getAttr()).thenReturn(attr);
    assertThrows(
        InvalidRangeException.class,
        () -> emxMetadataParser.setRange(emxAttrEntity, "emxEntityName", "emxAttrName", attr, 0));
  }

  @Test
  void setRangeValid() {
    initMetaFactories();

    Entity emxAttrEntity = mock(Entity.class);
    when(emxAttrEntity.getString(EMX_ATTRIBUTES_RANGE_MIN)).thenReturn("1");
    when(emxAttrEntity.getString(EMX_ATTRIBUTES_RANGE_MAX)).thenReturn("2");
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(COMPOUND);
    EmxAttribute emxAttr = mock(EmxAttribute.class);
    when(emxAttr.getAttr()).thenReturn(attr);
    emxMetadataParser.setRange(emxAttrEntity, "emxEntityName", null, attr, 0);
    verify(attr).setRange(new Range(1L, 2L));
  }

  @Test
  void setEnumOptions() {
    initMetaFactories();

    Entity emxAttrEntity = mock(Entity.class);
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(COMPOUND);
    EmxAttribute emxAttr = mock(EmxAttribute.class);
    when(emxAttr.getAttr()).thenReturn(attr);
    when(emxAttrEntity.get("enumOptions")).thenReturn("test1,test2");
    emxMetadataParser.setEnumOptions(emxAttrEntity, "emxEntityName", attr, 0);
    verify(attr).setEnumOptions(Arrays.asList("test1", "test2"));
  }

  @Test
  void setEnumOptionsNoOptions() {
    initMetaFactories();

    Entity emxAttrEntity = mock(Entity.class);
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(COMPOUND);
    when(attr.getName()).thenReturn("attr");
    EmxAttribute emxAttr = mock(EmxAttribute.class);
    when(emxAttr.getAttr()).thenReturn(attr);
    assertThrows(
        MissingEmxAttributeAttributeValueException.class,
        () -> emxMetadataParser.setEnumOptions(emxAttrEntity, "emxEntityName", attr, 0));
  }

  @Test
  void setLabelAttrInvalid() {
    initMetaFactories();

    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(COMPOUND);
    EmxAttribute emxAttr = mock(EmxAttribute.class);
    when(emxAttr.getAttr()).thenReturn(attr);
    IntermediateParseResults intermediateParseResults =
        new IntermediateParseResults(entityTypeFactory);
    assertThrows(
        InvalidValueException.class,
        () -> emxMetadataParser.setLabelAttr(0, "emxEntityName", emxAttr, attr, "test"));
  }

  @Test
  void setLookupAttrInvalid() {
    initMetaFactories();

    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(COMPOUND);
    EmxAttribute emxAttr = mock(EmxAttribute.class);
    when(emxAttr.getAttr()).thenReturn(attr);
    IntermediateParseResults intermediateParseResults =
        new IntermediateParseResults(entityTypeFactory);
    assertThrows(
        InvalidValueException.class,
        () -> emxMetadataParser.setLookupAttr(0, "emxEntityName", emxAttr, attr, "test"));
  }

  @Test
  void setAttrVisible() {
    initMetaFactories();

    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(COMPOUND);
    EmxAttribute emxAttr = mock(EmxAttribute.class);
    when(emxAttr.getAttr()).thenReturn(attr);
    emxMetadataParser.setAttrVisible(0, attr, "test");
    verify(attr).setVisibleExpression("test");
  }

  @Test
  void setAttrVisibleEmptyString() {
    initMetaFactories();

    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(COMPOUND);
    EmxAttribute emxAttr = mock(EmxAttribute.class);
    when(emxAttr.getAttr()).thenReturn(attr);
    emxMetadataParser.setAttrVisible(0, attr, "");
    verify(attr, times(0)).setVisibleExpression("");
  }

  @Test
  void setAttrNullable() {
    initMetaFactories();

    Attribute attr = mock(Attribute.class);
    when(attr.getString("nillable")).thenReturn("test");
    emxMetadataParser.setAttrNullable(0, attr, "test");
    verify(attr).setNullableExpression("test");
  }

  @Test
  void setAttrNullableEmptyString() {
    initMetaFactories();

    Attribute attr = mock(Attribute.class);
    when(attr.getString("nillable")).thenReturn("");
    emxMetadataParser.setAttrNullable(0, attr, "");
    verify(attr, times(0)).setNullableExpression("");
  }

  @Test
  void validateEmxIdAttrValue() {
    initMetaFactories();

    Entity emxAttrEntity = mock(Entity.class);
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(COMPOUND);
    EmxAttribute emxAttr = mock(EmxAttribute.class);
    when(emxAttr.getAttr()).thenReturn(attr);
    IntermediateParseResults intermediateParseResults =
        new IntermediateParseResults(entityTypeFactory);
    emxMetadataParser.validateEmxIdAttrValue(0, attr, "AUTO");
  }

  @Test
  void validateEmxIdAttrValueInvalid() {
    initMetaFactories();

    Entity emxAttrEntity = mock(Entity.class);
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(COMPOUND);
    EmxAttribute emxAttr = mock(EmxAttribute.class);
    when(emxAttr.getAttr()).thenReturn(attr);
    IntermediateParseResults intermediateParseResults =
        new IntermediateParseResults(entityTypeFactory);
    assertThrows(
        InvalidValueException.class,
        () -> emxMetadataParser.validateEmxIdAttrValue(0, attr, "test"));
  }

  @Test
  void checkAttrSheetHeaders() {
    Attribute attr = mock(Attribute.class);
    when(attr.getName()).thenReturn("error");
    assertThrows(
        UnknownEmxAttributeException.class, () -> emxMetadataParser.checkAttrSheetHeaders(attr));
  }

  @Test
  void checkAttrSheetHeadersCorrectValue() {
    Attribute attr = mock(Attribute.class);
    when(attr.getName()).thenReturn("entity");
    emxMetadataParser.checkAttrSheetHeaders(attr);
    // No exception expected
  }

  @Test
  void checkAttrSheetHeadersCorrecti18nValue() {
    Attribute attr = mock(Attribute.class);
    when(attr.getName()).thenReturn("label-nl");
    emxMetadataParser.checkAttrSheetHeaders(attr);
    // No exception expected
  }

  @Test
  void checkEntitySheetHeaders() {
    EntityType entityType = mock(EntityType.class);
    Attribute attr = mock(Attribute.class);
    when(attr.getName()).thenReturn("error");
    when(entityType.getAtomicAttributes()).thenReturn(Collections.singletonList(attr));
    Repository<Entity> entitiesRepo = mock(Repository.class);
    when(entitiesRepo.getEntityType()).thenReturn(entityType);

    assertThrows(
        InvalidEmxAttributeException.class,
        () -> emxMetadataParser.checkEntitySheetHeaders(entitiesRepo));
  }

  @Test
  void processAttrRefEntityNameDataService() {
    initMetaFactories();

    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(COMPOUND);
    when(attr.getName()).thenReturn("name");

    IntermediateParseResults intermediateParseResults =
        new IntermediateParseResults(entityTypeFactory);

    intermediateParseResults.addEntityType("IdOnlyEntityType");

    emxMetadataParser.processAttrRefEntityName(
        intermediateParseResults, 0, "IdOnlyEntityType", null, attr);

    verifyNoMoreInteractions(dataService);
  }

  @Test
  void processAttrRefEntityName() {
    initMetaFactories();

    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(COMPOUND);
    when(attr.getName()).thenReturn("name");

    IntermediateParseResults intermediateParseResults =
        new IntermediateParseResults(entityTypeFactory);
    when(dataService.hasEntityType("refEntity")).thenReturn(true);

    emxMetadataParser.processAttrRefEntityName(
        intermediateParseResults, 0, "refEntity", null, attr);

    verify(dataService).getEntityType("refEntity");
  }

  @Test
  void processAttrRefEntityNameInvalidRefEntity() {
    initMetaFactories();

    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(COMPOUND);
    when(attr.getName()).thenReturn("name");
    EmxAttribute emxAttr = mock(EmxAttribute.class);
    when(emxAttr.getAttr()).thenReturn(attr);
    IntermediateParseResults intermediateParseResults =
        new IntermediateParseResults(entityTypeFactory);
    when(dataService.hasEntityType("refEntity")).thenReturn(false);
    assertThrows(
        UnknownEntityValueException.class,
        () ->
            emxMetadataParser.processAttrRefEntityName(
                intermediateParseResults, 0, "refEntity", null, attr));
  }

  @Test
  void putEntitiesInDefaultPackage() {
    initMetaFactories();

    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(COMPOUND);
    EmxAttribute emxAttr = mock(EmxAttribute.class);
    when(emxAttr.getAttr()).thenReturn(attr);

    Package pack = mock(Package.class);
    when(dataService.findOneById(PackageMetadata.PACKAGE, "default", Package.class))
        .thenReturn(pack);
    IntermediateParseResults intermediateParseResults =
        new IntermediateParseResults(entityTypeFactory);

    EntityType entityType = mock(EntityType.class);
    when(entityType.setLabel(any())).thenReturn(entityType);
    when(entityType.setPackage(any())).thenReturn(entityType);

    when(entityTypeFactory.create("test1")).thenReturn(entityType);

    intermediateParseResults.addEntityType("test1");
    emxMetadataParser.putEntitiesInDefaultPackage(intermediateParseResults, "default");
    verify(entityType).setPackage(pack);
  }

  @Test
  void getPackage() {
    initMetaFactories();

    Entity emxAttrEntity = mock(Entity.class);
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(COMPOUND);
    EmxAttribute emxAttr = mock(EmxAttribute.class);
    when(emxAttr.getAttr()).thenReturn(attr);
    IntermediateParseResults intermediateParseResults =
        new IntermediateParseResults(entityTypeFactory);

    emxMetadataParser.getPackage(intermediateParseResults, "pack");
    verify(dataService).findOneById(PackageMetadata.PACKAGE, "pack", Package.class);
  }

  @Test
  void getEntityTypeFromDataService() {
    initMetaFactories();

    Repository repo1 = mock(Repository.class);
    Repository repo2 = mock(Repository.class);
    EntityType entityType1 = mock(EntityType.class);
    EntityType entityType2 = mock(EntityType.class);
    when(repo1.getEntityType()).thenReturn(entityType1);
    when(repo2.getEntityType()).thenReturn(entityType2);
    doReturn(repo1).when(dataService).getRepository("entityType1");
    doReturn(repo2).when(dataService).getRepository("entityType2");

    Map<String, EntityType> expected = new HashMap<>();
    expected.put("entityType1", entityType1);
    expected.put("entityType2", entityType2);

    assertEquals(
        expected,
        EmxMetadataParser.getEntityTypeFromDataService(
            dataService, asList("entityType1", "entityType2")));
  }

  @Test
  void parseSingleEntityType() {
    initMetaFactories();

    Entity entity = mock(Entity.class);
    when(entity.getString(EMX_ENTITIES_NAME)).thenReturn("testType");
    when(entity.getString(EMX_ENTITIES_PACKAGE)).thenReturn("");
    when(entity.getString(EMX_ENTITIES_LABEL)).thenReturn("label");
    when(entity.getString(EMX_ENTITIES_DESCRIPTION)).thenReturn("desc");
    when(entity.getString(EMX_ENTITIES_ABSTRACT)).thenReturn("false");
    when(entity.getString(EMX_ENTITIES_EXTENDS)).thenReturn("parentEntity");
    when(entity.getString(EMX_ENTITIES_BACKEND)).thenReturn("paper");
    when(entity.getString(EMX_ENTITIES_TAGS)).thenReturn("tag1,tag2");

    EntityType entityType = createEmxAttributeRepositoryEntityType();
    when(entityTypeFactory.create("testType"))
        .thenAnswer(
            invocation -> {
              when(entityType.setLabel("testType")).thenReturn(entityType);
              when(entityType.setPackage(null)).thenReturn(entityType);
              return entityType;
            });

    Tag tag1 = mock(Tag.class);
    Tag tag2 = mock(Tag.class);

    EntityType parent = mock(EntityType.class);
    MetaDataService metaDataService = mock(MetaDataService.class);
    when(metaDataService.hasBackend("paper")).thenReturn(true);
    when(metaDataService.getEntityType("parentEntity")).thenReturn(Optional.of(parent));
    when(dataService.getMeta()).thenReturn(metaDataService);

    IntermediateParseResults intermediateParseResults =
        new IntermediateParseResults(entityTypeFactory);
    intermediateParseResults.addTag("tag1", tag1);
    intermediateParseResults.addTag("tag2", tag2);

    emxMetadataParser.parseSingleEntityType(intermediateParseResults, 0, entity);
    verify(entityType).setLabel("label");
    verify(entityType).setDescription("desc");
    verify(entityType).setAbstract(false);
    verify(entityType).setExtends(parent);
    verify(entityType).setBackend("paper");
    verify(entityType).setTags(Arrays.asList(tag1, tag2));
  }

  @Test
  void parseSingleEntityTypeUnknownTag() {
    initMetaFactories();

    Entity entity = mock(Entity.class);
    when(entity.getString(EMX_ENTITIES_NAME)).thenReturn("testType");
    when(entity.getString(EMX_ENTITIES_PACKAGE)).thenReturn("");
    when(entity.getString(EMX_ENTITIES_LABEL)).thenReturn("label");
    when(entity.getString(EMX_ENTITIES_DESCRIPTION)).thenReturn("desc");
    when(entity.getString(EMX_ENTITIES_ABSTRACT)).thenReturn("false");
    when(entity.getString(EMX_ENTITIES_EXTENDS)).thenReturn("parentEntity");
    when(entity.getString(EMX_ENTITIES_BACKEND)).thenReturn("paper");
    when(entity.getString(EMX_ENTITIES_TAGS)).thenReturn("tag1,tag2");

    EntityType entityType = createEmxAttributeRepositoryEntityType();
    when(entityTypeFactory.create("testType"))
        .thenAnswer(
            invocation -> {
              when(entityType.setLabel("testType")).thenReturn(entityType);
              when(entityType.setPackage(null)).thenReturn(entityType);
              return entityType;
            });

    EntityType parent = mock(EntityType.class);
    MetaDataService metaDataService = mock(MetaDataService.class);
    when(metaDataService.hasBackend("paper")).thenReturn(true);
    when(metaDataService.getEntityType("parentEntity")).thenReturn(Optional.of(parent));
    when(dataService.getMeta()).thenReturn(metaDataService);

    IntermediateParseResults intermediateParseResults =
        new IntermediateParseResults(entityTypeFactory);

    assertThrows(
        UnknownTagException.class,
        () -> emxMetadataParser.parseSingleEntityType(intermediateParseResults, 0, entity));
  }

  @Test
  void parseSinglePackage() {
    initMetaFactories();
    Package pack = mock(Package.class);
    when(packageFactory.create("pack")).thenReturn(pack);
    Entity entity = mock(Entity.class);
    IntermediateParseResults intermediateParseResults =
        new IntermediateParseResults(entityTypeFactory);
    when(entity.getString(EMX_PACKAGE_NAME)).thenReturn("pack");
    emxMetadataParser.parseSinglePackage(intermediateParseResults, 0, entity);
    assertTrue(intermediateParseResults.hasPackage("pack"));
  }

  @Test
  void parseSinglePackageWithParent() {
    initMetaFactories();
    Package pack = mock(Package.class);
    when(packageFactory.create("parent_pack")).thenReturn(pack);
    Entity entity = mock(Entity.class);
    IntermediateParseResults intermediateParseResults =
        new IntermediateParseResults(entityTypeFactory);
    when(entity.getString(EMX_PACKAGE_NAME)).thenReturn("parent_pack");
    when(entity.getString(EMX_PACKAGE_PARENT)).thenReturn("parent");
    emxMetadataParser.parseSinglePackage(intermediateParseResults, 0, entity);
    assertTrue(intermediateParseResults.hasPackage("parent_pack"));
  }

  @Test
  void parseSinglePackageWithParentInconsistent() {
    initMetaFactories();
    Package pack = mock(Package.class);
    when(packageFactory.create("pack")).thenReturn(pack);
    Entity entity = mock(Entity.class);
    IntermediateParseResults intermediateParseResults =
        new IntermediateParseResults(entityTypeFactory);
    when(entity.getString(EMX_PACKAGE_NAME)).thenReturn("pack");
    when(entity.getString(EMX_PACKAGE_PARENT)).thenReturn("parent");
    assertThrows(
        InconsistentPackageStructureException.class,
        () -> emxMetadataParser.parseSinglePackage(intermediateParseResults, 0, entity));
  }

  @Test
  void parseBooleanTrue() {
    assertTrue(EmxMetadataParser.parseBoolean("true", 0, "column"));
  }

  @Test
  void parseBooleanFalse() {
    assertFalse(EmxMetadataParser.parseBoolean("false", 0, "column"));
  }

  @Test
  void parseBooleanInvalid() {
    assertThrows(
        InvalidValueException.class, () -> EmxMetadataParser.parseBoolean("test", 0, "column"));
  }

  @Test
  void setI18nEntityTypeLabel() {
    Entity entity = mock(Entity.class);
    when(entity.getString("label-fr")).thenReturn("label");
    EntityType entityType = mock(EntityType.class);
    emxMetadataParser.setI18nEntityTypeLabelAndDesc(entity, entityType, "label-fr");
    verify(entityType).setLabel("fr", "label");
  }

  @Test
  void setI18nEntityTypeDesc() {
    Entity entity = mock(Entity.class);
    when(entity.getString("description-xx")).thenReturn("qwerty");
    EntityType entityType = mock(EntityType.class);
    emxMetadataParser.setI18nEntityTypeLabelAndDesc(entity, entityType, "description-xx");
    verify(entityType).setDescription("xx", "qwerty");
  }

  @Test
  void setI18nEntityLabel() {
    Entity emxAttrEntity = mock(Entity.class);
    when(emxAttrEntity.getString("label-de")).thenReturn("test");
    Attribute attr = mock(Attribute.class);
    emxMetadataParser.setI18nAttributeLabelAndDesc(emxAttrEntity, attr, "label-de");
    verify(attr).setLabel("de", "test");
  }

  @Test
  void setI18nEntityDesc() {
    Entity emxAttrEntity = mock(Entity.class);
    when(emxAttrEntity.getString("description-nl")).thenReturn("omschrijving");
    Attribute attr = mock(Attribute.class);
    emxMetadataParser.setI18nAttributeLabelAndDesc(emxAttrEntity, attr, "description-nl");
    verify(attr).setDescription("nl", "omschrijving");
  }

  @Test
  void toL10nStringDefaultNamespace() {
    L10nString l10nString = mock(L10nString.class);
    when(l10nStringFactory.create()).thenReturn(l10nString);

    Entity emxEntity = mock(Entity.class);
    when(emxEntity.getString("msgid")).thenReturn("identifier");
    when(emxEntity.getString("description")).thenReturn("desc");
    when(emxEntity.getString("namespace")).thenReturn(null);
    when(emxEntity.getString("nl")).thenReturn("nederlands");
    when(emxEntity.getString("en")).thenReturn("english");
    when(emxEntity.getString("de")).thenReturn("deutch");

    L10nString actual = emxMetadataParser.toL10nString(emxEntity);
    verify(l10nString).setMessageID("identifier");
    verify(l10nString).setNamespace(DEFAULT_NAMESPACE);
    verify(l10nString).setDescription("desc");
    verify(l10nString).set("nl", "nederlands");
    verify(l10nString).set("en", "english");
    verify(l10nString).set("de", "deutch");
  }

  @Test
  void toL10nString() {
    L10nString l10nString = mock(L10nString.class);
    when(l10nStringFactory.create()).thenReturn(l10nString);

    Entity emxEntity = mock(Entity.class);
    when(emxEntity.getString("msgid")).thenReturn("identifier");
    when(emxEntity.getString("description")).thenReturn("desc");
    when(emxEntity.getString("namespace")).thenReturn("dataexplorer");
    when(emxEntity.getString("nl")).thenReturn("nederlands");

    L10nString actual = emxMetadataParser.toL10nString(emxEntity);
    verify(l10nString).setMessageID("identifier");
    verify(l10nString).setNamespace("dataexplorer");
    verify(l10nString).setDescription("desc");
    verify(l10nString).set("nl", "nederlands");
  }

  @Test
  void parseTagsSheet() {
    Tag tag = mock(Tag.class);
    when(tagFactory.create("tag1")).thenReturn(tag);

    Repository<Entity> repository = mock(Repository.class);
    Entity emxEntity = mock(Entity.class);
    when(emxEntity.getString("identifier")).thenReturn("tag1");
    when(emxEntity.getString("objectIRI")).thenReturn("iri");
    when(emxEntity.getString("label")).thenReturn("the label");
    when(emxEntity.getString("relationLabel")).thenReturn("relation");
    when(emxEntity.getString("codeSystem")).thenReturn("code");
    when(emxEntity.getString("relationIRI")).thenReturn("relation iri");

    when(repository.iterator()).thenReturn(Collections.singletonList(emxEntity).iterator());
    IntermediateParseResults results = emxMetadataParser.parseTagsSheet(repository);
    assertTrue(results.hasTag("tag1"));
    verify(tag).setLabel("the label");
    verify(tag).setObjectIri("iri");
    verify(tag).setRelationIri("relation iri");
    verify(tag).setRelationLabel("relation");
    verify(tag).setCodeSystem("code");
  }

  @Test
  void testToTags() {
    Tag tag1 = mock(Tag.class);
    Tag tag2 = mock(Tag.class);
    EntityTypeFactory entityTypeFactory = mock(EntityTypeFactory.class);
    IntermediateParseResults intermediateParseResults =
        new IntermediateParseResults(entityTypeFactory);
    intermediateParseResults.addTag("tag2", tag2);
    when(dataService.findOneById(TAG, "tag1", Tag.class)).thenReturn(tag1);
    assertEquals(
        asList(tag1, tag2),
        emxMetadataParser.toTags(intermediateParseResults, asList("tag1", "tag2")));
  }

  @Test
  void testToTagsUnknownTag() {
    Tag tag2 = mock(Tag.class);
    EntityTypeFactory entityTypeFactory = mock(EntityTypeFactory.class);
    IntermediateParseResults intermediateParseResults =
        new IntermediateParseResults(entityTypeFactory);
    intermediateParseResults.addTag("tag2", tag2);
    assertThrows(
        UnknownTagException.class,
        () -> emxMetadataParser.toTags(intermediateParseResults, Arrays.asList("tag1", "tag2")));
  }

  private void initMetaFactories() {
    initAttrMetaFactory();
    initEntityTypeFactory();
  }

  private void initEntityTypeFactory() {
    when(entityTypeFactory.create("IdOnlyEntityType"))
        .thenAnswer(
            invocation -> {
              EntityType entityType = createEmxAttributeRepositoryEntityType();
              when(entityType.setLabel("IdOnlyEntityType")).thenReturn(entityType);
              when(entityType.setPackage(null)).thenReturn(entityType);
              return entityType;
            });
  }

  private void initAttrMetaFactory() {
    when(attrMetaFactory.create())
        .thenAnswer(
            invocation -> {
              Attribute attribute = mock(Attribute.class);
              when(attribute.setName(any())).thenReturn(attribute);
              // not 100% correct, but doesn't matter as long as it isn't a reference type
              when(attribute.getDataType()).thenReturn(STRING);
              return attribute;
            });
  }

  private RepositoryCollection createEmxRepositoryCollection() {
    Repository<Entity> attributeRepository = createEmxAttributeRepository();
    RepositoryCollection repositoryCollection = mock(RepositoryCollection.class);
    when(repositoryCollection.getRepository("attributes")).thenReturn(attributeRepository);
    return repositoryCollection;
  }

  private Repository<Entity> createEmxAttributeRepository() {
    EntityType attributeRepositoryEntityType = createEmxAttributeRepositoryEntityType();
    List<Entity> attributeEntities = createEmxAttributeRepositoryAttributes();

    @SuppressWarnings("unchecked")
    Repository<Entity> attributeRepository = mock(Repository.class);
    when(attributeRepository.getEntityType()).thenReturn(attributeRepositoryEntityType);
    when(attributeRepository.iterator()).thenAnswer(invocation -> attributeEntities.iterator());
    return attributeRepository;
  }

  private EntityType createEmxAttributeRepositoryEntityType() {
    Attribute idAttribute = createEmxAttributeRepositoryEntityTypeAttribute("id");
    Attribute nameAttribute = createEmxAttributeRepositoryEntityTypeAttribute("name");
    Attribute entityAttribute = createEmxAttributeRepositoryEntityTypeAttribute("entity");
    Attribute dataTypeAttribute = createEmxAttributeRepositoryEntityTypeAttribute("dataType");
    Attribute idAttributeAttribute = createEmxAttributeRepositoryEntityTypeAttribute("idAttribute");
    Attribute labelAttributeAttribute =
        createEmxAttributeRepositoryEntityTypeAttribute("labelAttribute");
    Attribute visibleAttribute = createEmxAttributeRepositoryEntityTypeAttribute("visible");

    EntityType attributeRepositoryEntityType = mock(EntityType.class);
    doReturn(idAttribute).when(attributeRepositoryEntityType).getAttribute("id");
    doReturn(nameAttribute).when(attributeRepositoryEntityType).getAttribute("name");
    doReturn(entityAttribute).when(attributeRepositoryEntityType).getAttribute("entity");
    doReturn(dataTypeAttribute).when(attributeRepositoryEntityType).getAttribute("dataType");
    doReturn(idAttributeAttribute).when(attributeRepositoryEntityType).getAttribute("idAttribute");
    doReturn(labelAttributeAttribute)
        .when(attributeRepositoryEntityType)
        .getAttribute("labelAttribute");
    doReturn(visibleAttribute).when(attributeRepositoryEntityType).getAttribute("visible");

    List<Attribute> attributes =
        asList(
            nameAttribute,
            entityAttribute,
            dataTypeAttribute,
            idAttributeAttribute,
            labelAttributeAttribute,
            visibleAttribute);
    when(attributeRepositoryEntityType.getAtomicAttributes()).thenReturn(attributes);
    when(attributeRepositoryEntityType.getAllAttributes()).thenReturn(attributes);
    when(attributeRepositoryEntityType.getOwnAtomicAttributes()).thenReturn(attributes);
    return attributeRepositoryEntityType;
  }

  private Attribute createEmxAttributeRepositoryEntityTypeAttribute(String name) {
    Attribute attribute = mock(Attribute.class);
    when(attribute.getDataType()).thenReturn(STRING);
    when(attribute.getName()).thenReturn(name);
    return attribute;
  }

  private List<Entity> createEmxAttributeRepositoryAttributes() {
    Entity attributeEntity = mock(Attribute.class);
    doReturn("id").when(attributeEntity).getString("name");
    doReturn("IdOnlyEntityType").when(attributeEntity).getString("entity");
    doReturn("string").when(attributeEntity).getString("dataType");
    doReturn(true).when(attributeEntity).getBoolean("idAttribute");
    doReturn(true).when(attributeEntity).getBoolean("labelAttribute");
    doReturn(true).when(attributeEntity).getBoolean("visible");
    return singletonList(attributeEntity);
  }

  private MetaDataService createMetaDataService() {
    RepositoryCollection defaultBackendRepositoryCollection = mock(RepositoryCollection.class);
    when(defaultBackendRepositoryCollection.getName()).thenReturn("MyDefault");
    MetaDataService metaDataService = mock(MetaDataService.class);
    when(metaDataService.getDefaultBackend()).thenReturn(defaultBackendRepositoryCollection);
    return metaDataService;
  }
}
