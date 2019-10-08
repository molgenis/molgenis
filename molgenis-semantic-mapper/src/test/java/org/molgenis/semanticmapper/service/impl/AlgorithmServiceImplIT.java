package org.molgenis.semanticmapper.service.impl;

import static com.google.common.collect.Lists.newArrayList;
import static java.time.ZoneId.systemDefault;
import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.DATE;
import static org.molgenis.data.meta.AttributeType.DATE_TIME;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.LONG;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;

import com.google.common.collect.LinkedHashMultimap;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.stream.Stream;
import javax.script.ScriptException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.security.config.UserTestConfig;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.js.magma.JsMagmaScriptEvaluator;
import org.molgenis.js.nashorn.NashornScriptEngine;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.semanticmapper.algorithmgenerator.service.AlgorithmGeneratorService;
import org.molgenis.semanticmapper.algorithmgenerator.service.impl.AlgorithmGeneratorServiceImpl;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping;
import org.molgenis.semanticmapper.mapping.model.EntityMapping;
import org.molgenis.semanticmapper.mapping.model.MappingProject;
import org.molgenis.semanticmapper.service.AlgorithmService;
import org.molgenis.semanticmapper.service.UnitResolver;
import org.molgenis.semanticsearch.explain.bean.AttributeSearchResults;
import org.molgenis.semanticsearch.explain.bean.EntityTypeSearchResults;
import org.molgenis.semanticsearch.explain.bean.ExplainedAttribute;
import org.molgenis.semanticsearch.explain.bean.ExplainedQueryString;
import org.molgenis.semanticsearch.repository.TagRepository;
import org.molgenis.semanticsearch.semantic.Hit;
import org.molgenis.semanticsearch.semantic.Hits;
import org.molgenis.semanticsearch.service.OntologyTagService;
import org.molgenis.semanticsearch.service.SemanticSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = AlgorithmServiceImplIT.Config.class)
class AlgorithmServiceImplIT extends AbstractMolgenisSpringTest {
  @Autowired private EntityTypeFactory entityTypeFactory;

  @Autowired private AttributeFactory attrMetaFactory;

  @Autowired private AlgorithmService algorithmService;

  @Autowired private EntityManager entityManager;

  @Autowired private OntologyTagService ontologyTagService;

  @Autowired private SemanticSearchService semanticSearchService;

  @Autowired private AlgorithmTemplateService algorithmTemplateService;

  @BeforeEach
  void setUpBeforeMethod() {
    when(algorithmTemplateService.find(any())).thenReturn(Stream.empty());
  }

  @Test
  void testGetSourceAttributeNames() {
    assertEquals(singletonList("id"), algorithmService.getSourceAttributeNames("$('id')"));
  }

  @Test
  void testGetSourceAttributeNamesNoQuotes() {
    assertEquals(singletonList("id"), algorithmService.getSourceAttributeNames("$(id)"));
  }

  @Test
  void testDeepReference() {
    assertEquals(
        singletonList("gender"), algorithmService.getSourceAttributeNames("$(gender.label)"));
  }

  static Object[][] testApplyProvider() {
    String april20DMY = "2017-04-20";
    LocalDate april20 = LocalDate.parse(april20DMY);
    Instant april20defaultTimezone = april20.atStartOfDay(systemDefault()).toInstant();
    Instant april20UTC = april20.atStartOfDay(UTC).toInstant();
    return new Object[][] {
      {INT, 25, "$('source').value()", INT, 25, "map INT value"},
      {LONG, 529387981723498L, "$('source').value()", LONG, 529387981723498L, "map LONG value"},
      {BOOL, false, "$('source').value()", BOOL, false, "map BOOL value false"},
      {BOOL, true, "$('source').value()", BOOL, true, "map BOOL value true"},
      {DATE, april20, "$('source').value()", DATE, april20, "map DATE to DATE"},
      {
        DATE,
        april20,
        "$('source').value()",
        DATE_TIME,
        april20defaultTimezone,
        "DATE should map to DATE_TIME containing start of day in default timezone"
      },
      {
        STRING,
        april20DMY,
        "new Date($('source').value())",
        DATE_TIME,
        april20UTC,
        "ymd STRING to DATE_TIME parsed by JavaScript returns start of day in UTC (BEWARE!)"
      },
      {
        STRING,
        april20DMY,
        "var date = $('source').value().split('-'); new Date(date[0], date[1]-1, date[2])",
        DATE_TIME,
        april20defaultTimezone,
        "ymd STRING to DATE_TIME parsed manually returns start of day in default timezone"
      },
      {
        STRING,
        april20DMY,
        "var date = $('source').value().split('-'); new Date(date[0], date[1]-1, date[2])",
        DATE,
        april20,
        "ymd STRING to DATE parsed manually returns correct day"
      },
      {
        STRING,
        april20DMY,
        "new Date($('source').value())",
        DATE,
        april20UTC.atZone(ZoneId.systemDefault()).toLocalDate(),
        "ymd STRING to DATE parsed by JavaScript returns wrong day, depending on default timezone (BEWARE!)"
      },
      {
        STRING,
        april20.toString(),
        "var date = $('source').value().split('-'); new Date(date[0], date[1]-1, date[2])",
        DATE_TIME,
        april20defaultTimezone,
        "ymd STRING to DATE_TIME parsed manually returns start of day in system default timezone"
      },
      {
        DATE_TIME,
        april20defaultTimezone,
        "$('source').value()",
        DATE,
        april20,
        "DATE_TIME to DATE returns day in default timeZone"
      },
      {
        DATE_TIME,
        april20UTC,
        "$('source').value()",
        LONG,
        april20UTC.toEpochMilli(),
        "DATE_TIME to LONG returns epoch millis"
      },
      {
        DATE,
        april20,
        "$('source').value()",
        LONG,
        april20defaultTimezone.toEpochMilli(),
        "DATE to LONG returns epoch millis at start of day in default timezone"
      }
    };
  }

  @ParameterizedTest
  @MethodSource("testApplyProvider")
  void testApply(
      AttributeType sourceAttributeType,
      Object sourceAttributeValue,
      String algorithm,
      AttributeType targetAttributeType,
      Object expected,
      String message) {
    String idAttrName = "id";
    EntityType entityType = entityTypeFactory.create("LL");
    entityType.addAttribute(attrMetaFactory.create().setName(idAttrName).setDataType(INT), ROLE_ID);
    entityType.addAttribute(
        attrMetaFactory.create().setName("source").setDataType(sourceAttributeType));
    Entity source = new DynamicEntity(entityType);
    source.set(idAttrName, 1);
    source.set("source", sourceAttributeValue);

    Attribute targetAttribute = attrMetaFactory.create().setName("target");
    targetAttribute.setDataType(targetAttributeType);
    AttributeMapping attributeMapping = new AttributeMapping(targetAttribute);
    attributeMapping.setAlgorithm(algorithm);
    Object result = algorithmService.apply(attributeMapping, source, entityType, 3);
    assertEquals(result, expected, message);
  }

  @Test
  void testGetAgeScript() {
    String idAttrName = "id";
    EntityType entityType = entityTypeFactory.create("LL");
    entityType.addAttribute(attrMetaFactory.create().setName(idAttrName).setDataType(INT), ROLE_ID);
    entityType.addAttribute(attrMetaFactory.create().setName("dob").setDataType(DATE));
    Entity source = new DynamicEntity(entityType);
    source.set(idAttrName, 1);
    source.set("dob", LocalDate.of(1973, Month.AUGUST, 28));

    Attribute targetAttribute = attrMetaFactory.create().setName("age");
    targetAttribute.setDataType(INT);
    AttributeMapping attributeMapping = new AttributeMapping(targetAttribute);
    attributeMapping.setAlgorithm(
        "Math.floor((new Date(2015, 2, 12) - $('dob').value())/(365.2425 * 24 * 60 * 60 * 1000))");
    Object result = algorithmService.apply(attributeMapping, source, entityType, 3);
    assertEquals(41, result);
  }

  @Test
  void testGetXrefScript() {
    // xref entities
    EntityType entityTypeXref = entityTypeFactory.create("xrefEntity1");
    entityTypeXref.addAttribute(attrMetaFactory.create().setName("id").setDataType(INT), ROLE_ID);
    entityTypeXref.addAttribute(attrMetaFactory.create().setName("field1"));
    Entity xref1a = new DynamicEntity(entityTypeXref);
    xref1a.set("id", 1);
    xref1a.set("field1", "Test");

    EntityType entityTypeXref2 = entityTypeFactory.create("xrefEntity2");
    entityTypeXref2.addAttribute(attrMetaFactory.create().setName("id").setDataType(INT), ROLE_ID);
    entityTypeXref2.addAttribute(attrMetaFactory.create().setName("field2"));
    Entity xref2a = new DynamicEntity(entityTypeXref2);
    xref2a.set("id", 2);
    xref2a.set("field2", "Test");

    // source Entity
    EntityType entityTypeSource = entityTypeFactory.create("Source");
    entityTypeSource.addAttribute(attrMetaFactory.create().setName("id").setDataType(INT), ROLE_ID);
    entityTypeSource.addAttribute(attrMetaFactory.create().setName("xref").setDataType(XREF));
    Entity source = new DynamicEntity(entityTypeSource);
    source.set("id", 1);
    source.set("xref", xref2a);

    Attribute targetAttribute = attrMetaFactory.create().setName("field1");
    targetAttribute.setDataType(XREF);
    targetAttribute.setRefEntity(entityTypeXref);

    AttributeMapping attributeMapping = new AttributeMapping(targetAttribute);
    attributeMapping.setAlgorithm("$('xref').map({'1':'2', '2':'1'}).value();");

    when(entityManager.getReference(entityTypeXref, 1)).thenReturn(xref1a);

    Entity result = (Entity) algorithmService.apply(attributeMapping, source, entityTypeSource, 3);
    assertEquals(xref2a.get("field2"), result.get("field1"));
  }

  @Test
  void testAttrXref() {
    EntityType referenceEntityType = entityTypeFactory.create("reference");
    referenceEntityType.addAttribute(attrMetaFactory.create().setName("id"), ROLE_ID);
    referenceEntityType.addAttribute(attrMetaFactory.create().setName("label"));

    Entity referenceEntity = new DynamicEntity(referenceEntityType);
    referenceEntity.set("id", "1");
    referenceEntity.set("label", "label 1");

    EntityType sourceEntityType = entityTypeFactory.create("source");
    sourceEntityType.addAttribute(attrMetaFactory.create().setName("id"), ROLE_ID);
    sourceEntityType.addAttribute(
        attrMetaFactory.create().setName("source_xref").setDataType(XREF));

    Entity sourceEntity = new DynamicEntity(sourceEntityType);
    sourceEntity.set("id", "1");
    sourceEntity.set("source_xref", referenceEntity);

    Attribute targetAttribute = attrMetaFactory.create().setName("target_label");
    AttributeMapping attributeMapping = new AttributeMapping(targetAttribute);
    attributeMapping.setAlgorithm("$('source_xref').attr('label').value()");

    Object result = algorithmService.apply(attributeMapping, sourceEntity, sourceEntityType, 3);
    assertEquals("label 1", result);
  }

  @Test
  void testAttrMref() {
    EntityType referenceEntityType = entityTypeFactory.create("reference");
    referenceEntityType.addAttribute(attrMetaFactory.create().setName("id"), ROLE_ID);
    referenceEntityType.addAttribute(attrMetaFactory.create().setName("label"));

    Entity referenceEntity1 = new DynamicEntity(referenceEntityType);
    referenceEntity1.set("id", "1");
    referenceEntity1.set("label", "label 1");

    Entity referenceEntity2 = new DynamicEntity(referenceEntityType);
    referenceEntity2.set("id", "2");
    referenceEntity2.set("label", "label 2");

    EntityType sourceEntityType = entityTypeFactory.create("source");
    sourceEntityType.addAttribute(attrMetaFactory.create().setName("id"), ROLE_ID);
    sourceEntityType.addAttribute(
        attrMetaFactory.create().setName("source_mref").setDataType(MREF));

    Entity sourceEntity = new DynamicEntity(sourceEntityType);
    sourceEntity.set("id", "1");
    sourceEntity.set("source_mref", newArrayList(referenceEntity1, referenceEntity2));

    Attribute targetAttribute = attrMetaFactory.create().setName("target_label");
    AttributeMapping attributeMapping = new AttributeMapping(targetAttribute);
    attributeMapping.setAlgorithm(
        "$('source_mref').map(function(mref){ return mref.attr('label').value()}).value()");

    Object result = algorithmService.apply(attributeMapping, sourceEntity, sourceEntityType, 3);
    assertEquals("[label 1, label 2]", result);
  }

  @Test
  void testApplyMref() {
    String refEntityName = "refEntity";
    String refEntityIdAttrName = "id";
    String refEntityLabelAttrName = "label";

    String refEntityId0 = "id0";
    String refEntityId1 = "id1";

    String sourceEntityName = "source";
    String sourceEntityAttrName = "mref-source";
    String targetEntityAttrName = "mref-target";

    // ref entities
    EntityType refEntityType = entityTypeFactory.create(refEntityName);
    refEntityType.addAttribute(attrMetaFactory.create().setName(refEntityIdAttrName), ROLE_ID);
    refEntityType.addAttribute(
        attrMetaFactory.create().setName(refEntityLabelAttrName).setDataType(STRING), ROLE_LABEL);

    Entity refEntity0 = new DynamicEntity(refEntityType);
    refEntity0.set(refEntityIdAttrName, refEntityId0);
    refEntity0.set(refEntityLabelAttrName, "label0");

    Entity refEntity1 = new DynamicEntity(refEntityType);
    refEntity1.set(refEntityIdAttrName, refEntityId1);
    refEntity1.set(refEntityLabelAttrName, "label1");

    // mapping
    Attribute targetAttribute = attrMetaFactory.create().setName(targetEntityAttrName);
    targetAttribute.setDataType(MREF).setNillable(false).setRefEntity(refEntityType);
    AttributeMapping attributeMapping = new AttributeMapping(targetAttribute);
    attributeMapping.setAlgorithm("$('" + sourceEntityAttrName + "').value()");

    when(entityManager.getReference(refEntityType, refEntityId0)).thenReturn(refEntity0);
    when(entityManager.getReference(refEntityType, refEntityId1)).thenReturn(refEntity1);

    // source Entity
    EntityType entityTypeSource = entityTypeFactory.create(sourceEntityName);
    entityTypeSource.addAttribute(
        attrMetaFactory.create().setName(refEntityIdAttrName).setDataType(INT).setAuto(true),
        ROLE_ID);
    entityTypeSource.addAttribute(
        attrMetaFactory
            .create()
            .setName(sourceEntityAttrName)
            .setDataType(MREF)
            .setNillable(false)
            .setRefEntity(refEntityType));
    Entity source = new DynamicEntity(entityTypeSource);
    source.set(sourceEntityAttrName, asList(refEntity0, refEntity1));

    Object result = algorithmService.apply(attributeMapping, source, entityTypeSource, 3);
    assertEquals(asList(refEntity0, refEntity1), result);
  }

  @Test
  void testApplyMrefNillable() {
    String refEntityName = "refEntity";
    String refEntityIdAttrName = "id";
    String refEntityLabelAttrName = "label";

    String sourceEntityName = "source";
    String sourceEntityAttrName = "mref-source";
    String targetEntityAttrName = "mref-target";

    // ref entities
    EntityType refEntityType = entityTypeFactory.create(refEntityName);
    refEntityType.addAttribute(attrMetaFactory.create().setName(refEntityIdAttrName), ROLE_ID);
    refEntityType.addAttribute(
        attrMetaFactory.create().setName(refEntityLabelAttrName), ROLE_LABEL);

    // mapping
    Attribute targetAttribute = attrMetaFactory.create().setName(targetEntityAttrName);
    targetAttribute.setDataType(MREF).setNillable(true).setRefEntity(refEntityType);
    AttributeMapping attributeMapping = new AttributeMapping(targetAttribute);
    attributeMapping.setAlgorithm("$('" + sourceEntityAttrName + "').value()");

    // source Entity
    EntityType entityTypeSource = entityTypeFactory.create(sourceEntityName);
    entityTypeSource.addAttribute(
        attrMetaFactory.create().setName(refEntityIdAttrName).setDataType(INT).setAuto(true),
        ROLE_ID);
    entityTypeSource.addAttribute(
        attrMetaFactory
            .create()
            .setName(sourceEntityAttrName)
            .setDataType(MREF)
            .setNillable(true)
            .setRefEntity(refEntityType));

    Entity source = new DynamicEntity(entityTypeSource);
    source.set(sourceEntityAttrName, emptyList());

    Object result = algorithmService.apply(attributeMapping, source, entityTypeSource, 3);
    assertEquals(emptyList(), result);
  }

  @Test
  void testCreateAttributeMappingIfOnlyOneMatch() {
    EntityType targetEntityType = entityTypeFactory.create("target");
    Attribute targetAttribute = attrMetaFactory.create().setName("targetHeight");
    targetAttribute.setDescription("height");
    targetEntityType.addAttribute(targetAttribute);

    EntityType sourceEntityType = entityTypeFactory.create("source");
    Attribute sourceAttribute = attrMetaFactory.create().setName("sourceHeight");
    sourceAttribute.setDescription("height");
    sourceEntityType.addAttribute(sourceAttribute);

    MappingProject project = new MappingProject("project", 3);
    project.addTarget(targetEntityType);

    EntityMapping mapping = project.getMappingTarget("target").addSource(sourceEntityType);

    Hits<ExplainedAttribute> matches =
        Hits.create(
            Hit.create(
                ExplainedAttribute.create(
                    sourceAttribute,
                    singleton(ExplainedQueryString.create("height", "height", "height", 100)),
                    false),
                1f));

    LinkedHashMultimap<Relation, OntologyTerm> ontologyTermTags = LinkedHashMultimap.create();

    when(semanticSearchService.findAttributes(sourceEntityType, targetEntityType))
        .thenReturn(
            EntityTypeSearchResults.create(
                targetEntityType,
                singletonList(AttributeSearchResults.create(targetAttribute, matches))));

    when(ontologyTagService.getTagsForAttribute(targetEntityType, targetAttribute))
        .thenReturn(ontologyTermTags);

    algorithmService.autoGenerateAlgorithm(sourceEntityType, targetEntityType, mapping);

    assertEquals(
        "$('sourceHeight').value();", mapping.getAttributeMapping("targetHeight").getAlgorithm());
  }

  @Test
  void testWhenSourceDoesNotMatchThenNoMappingGetsCreated() {
    EntityType targetEntityType = entityTypeFactory.create("target");
    Attribute targetAttribute = attrMetaFactory.create().setName("targetHeight");
    targetAttribute.setDescription("height");
    targetEntityType.addAttribute(targetAttribute);

    EntityType sourceEntityType = entityTypeFactory.create("source");
    Attribute sourceAttribute = attrMetaFactory.create().setName("sourceHeight");
    sourceAttribute.setDescription("weight");
    sourceEntityType.addAttribute(sourceAttribute);

    MappingProject project = new MappingProject("project", 3);
    project.addTarget(targetEntityType);

    EntityMapping mapping = project.getMappingTarget("target").addSource(sourceEntityType);

    when(semanticSearchService.findAttributes(sourceEntityType, targetEntityType))
        .thenReturn(EntityTypeSearchResults.create(targetEntityType, emptyList()));

    when(ontologyTagService.getTagsForAttribute(targetEntityType, targetAttribute))
        .thenReturn(LinkedHashMultimap.create());

    algorithmService.autoGenerateAlgorithm(sourceEntityType, targetEntityType, mapping);

    org.junit.jupiter.api.Assertions.assertNull(mapping.getAttributeMapping("targetHeight"));
  }

  @Test
  void testWhenSourceHasMultipleMatchesThenFirstMappingGetsCreated() {
    EntityType targetEntityType = entityTypeFactory.create("target");
    Attribute targetAttribute = attrMetaFactory.create().setName("targetHeight");
    targetAttribute.setDescription("height");
    targetEntityType.addAttribute(targetAttribute);

    EntityType sourceEntityType = entityTypeFactory.create("source");
    Attribute sourceAttribute1 = attrMetaFactory.create().setName("sourceHeight1");
    sourceAttribute1.setDescription("height");
    Attribute sourceAttribute2 = attrMetaFactory.create().setName("sourceHeight2");
    sourceAttribute2.setDescription("height");

    sourceEntityType.addAttributes(asList(sourceAttribute1, sourceAttribute2));

    MappingProject project = new MappingProject("project", 3);
    project.addTarget(targetEntityType);

    EntityMapping mapping = project.getMappingTarget("target").addSource(sourceEntityType);

    Hits<ExplainedAttribute> mappings =
        Hits.create(
            Hit.create(ExplainedAttribute.create(sourceAttribute1, emptySet(), false), 1f),
            Hit.create(ExplainedAttribute.create(sourceAttribute2, emptySet(), false), 1f));

    LinkedHashMultimap<Relation, OntologyTerm> ontologyTermTags = LinkedHashMultimap.create();

    when(semanticSearchService.findAttributes(sourceEntityType, targetEntityType))
        .thenReturn(
            EntityTypeSearchResults.create(
                targetEntityType,
                singletonList(AttributeSearchResults.create(targetAttribute, mappings))));

    when(ontologyTagService.getTagsForAttribute(targetEntityType, targetAttribute))
        .thenReturn(ontologyTermTags);

    algorithmService.autoGenerateAlgorithm(sourceEntityType, targetEntityType, mapping);

    assertEquals(
        sourceAttribute1, mapping.getAttributeMapping("targetHeight").getSourceAttributes().get(0));
  }

  @Configuration
  @Import(UserTestConfig.class)
  static class Config {
    @Autowired private DataService dataService;

    @Bean
    SemanticSearchService semanticSearchService() {
      return mock(SemanticSearchService.class);
    }

    @Bean
    UnitResolver unitResolver() {
      return new UnitResolverImpl(ontologyService());
    }

    @Bean
    EntityManager entityManager() {
      return mock(EntityManager.class);
    }

    @Bean
    JsMagmaScriptEvaluator jsScriptEvaluator() throws ScriptException, IOException {
      return new JsMagmaScriptEvaluator(new NashornScriptEngine());
    }

    @Bean
    AlgorithmService algorithmService() throws ScriptException, IOException {
      return new AlgorithmServiceImpl(
          semanticSearchService(),
          algorithmGeneratorService(),
          entityManager(),
          jsScriptEvaluator());
    }

    @Bean
    AlgorithmTemplateService algorithmTemplateService() {
      return mock(AlgorithmTemplateServiceImpl.class);
    }

    @Bean
    OntologyService ontologyService() {
      return mock(OntologyService.class);
    }

    @Bean
    TagRepository tagRepository() {
      return mock(TagRepository.class);
    }

    @Bean
    IdGenerator idGenerator() {
      return mock(IdGenerator.class);
    }

    @Bean
    OntologyTagService ontologyTagService() {
      return mock(OntologyTagService.class);
    }

    @Bean
    AlgorithmGeneratorService algorithmGeneratorService() {
      return new AlgorithmGeneratorServiceImpl(
          dataService, unitResolver(), algorithmTemplateService());
    }
  }
}
