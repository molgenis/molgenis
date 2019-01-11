package org.molgenis.data.importer.emx;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.validation.meta.AttributeValidator.ValidationMode.ADD_SKIP_ENTITY_VALIDATION;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.i18n.model.L10nStringFactory;
import org.molgenis.data.i18n.model.LanguageFactory;
import org.molgenis.data.importer.EntitiesValidationReport;
import org.molgenis.data.importer.MyEntitiesValidationReport;
import org.molgenis.data.importer.ParsedMetaData;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.meta.model.TagFactory;
import org.molgenis.data.validation.meta.AttributeValidator;
import org.molgenis.data.validation.meta.EntityTypeValidator;
import org.molgenis.data.validation.meta.TagValidator;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EmxMetadataParserTest extends AbstractMockitoTest {
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

  // strict mode is non-trivial for this test class due to the nature of the code under test
  @SuppressWarnings("deprecation")
  EmxMetadataParserTest() {
    super(Strictness.WARN);
  }

  @BeforeMethod
  public void setUpBeforeMethod() {
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
  public void testParse() {
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
    assertEquals(parsedMetaData, expectedParsedMetaData);
  }

  @Test
  public void testValidate() {
    initMetaFactories();

    MetaDataService metaDataService = createMetaDataService();
    when(dataService.getMeta()).thenReturn(metaDataService);

    RepositoryCollection repositoryCollection = createEmxRepositoryCollection();
    MyEntitiesValidationReport expectedEntitiesValidationReport = new MyEntitiesValidationReport();
    expectedEntitiesValidationReport.addEntity("IdOnlyEntityType", true);

    EntitiesValidationReport entitiesValidationReport =
        emxMetadataParser.validate(repositoryCollection);

    assertEquals(entitiesValidationReport, expectedEntitiesValidationReport);
    // 6: name, entity, dataType, idAttribute, labelAttribute, visible
    verify(attributeValidator, times(6))
        .validate(any(Attribute.class), eq(ADD_SKIP_ENTITY_VALIDATION));
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
