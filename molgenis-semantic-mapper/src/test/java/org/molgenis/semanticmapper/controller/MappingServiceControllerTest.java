package org.molgenis.semanticmapper.controller;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.meta.AttributeType.DATE;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.semantic.Relation.isAssociatedWith;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;
import static org.molgenis.semanticmapper.controller.MappingServiceController.URI;
import static org.molgenis.semanticsearch.explain.bean.ExplainedAttributeDto.create;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.molgenis.core.ui.jobs.JobsController;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.semantic.Relation;
import org.molgenis.jobs.JobExecutor;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.semanticmapper.job.MappingJobExecution;
import org.molgenis.semanticmapper.job.MappingJobExecutionFactory;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping;
import org.molgenis.semanticmapper.mapping.model.EntityMapping;
import org.molgenis.semanticmapper.mapping.model.MappingProject;
import org.molgenis.semanticmapper.mapping.model.MappingTarget;
import org.molgenis.semanticmapper.service.AlgorithmService;
import org.molgenis.semanticmapper.service.MappingService;
import org.molgenis.semanticsearch.explain.bean.AttributeSearchResults;
import org.molgenis.semanticsearch.explain.bean.ExplainedAttribute;
import org.molgenis.semanticsearch.semantic.Hit;
import org.molgenis.semanticsearch.semantic.Hits;
import org.molgenis.semanticsearch.service.OntologyTagService;
import org.molgenis.semanticsearch.service.SemanticSearchService;
import org.molgenis.web.converter.GsonConfig;
import org.molgenis.web.menu.MenuReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;

@MockitoSettings(strictness = Strictness.LENIENT)
@WebAppConfiguration
@ContextConfiguration(classes = GsonConfig.class)
class MappingServiceControllerTest extends AbstractMolgenisSpringTest {
  @Autowired private EntityTypeFactory entityTypeFactory;

  @Autowired private AttributeFactory attrMetaFactory;

  @Mock private MappingJobExecutionFactory mappingJobExecutionFactory;

  @Mock private MappingService mappingService;

  @Mock private AlgorithmService algorithmService;

  @Mock private DataService dataService;

  @Mock private OntologyTagService ontologyTagService;

  @Mock private SemanticSearchService semanticSearchService;

  @Autowired private GsonHttpMessageConverter gsonHttpMessageConverter;

  @Mock private MenuReaderService menuReaderService;

  @Mock private Model model;

  @Mock private EntityType target1;
  @Mock private EntityType target2;
  @Mock private MetaDataService metaDataService;
  @Mock private Package system;
  @Mock private Package base;
  @Mock private JobExecutor jobExecutor;
  @Mock private MappingJobExecution mappingJobExecution;
  @Mock private EntityType mappingJobExecutionMetadata;
  @Mock private JobsController jobsController;

  private MappingServiceController controller;

  private EntityType lifeLines;
  private EntityType hop;
  private MappingProject mappingProject;
  private static final String ID = "mappingservice";
  private Attribute ageAttr;
  private Attribute dobAttr;
  private Attribute heightAttr;

  private MockMvc mockMvc;
  private SecurityContext previousContext;

  @AfterEach
  void tearDownAfterClass() {
    SecurityContextHolder.setContext(previousContext);
  }

  @BeforeEach
  void beforeTest() {
    previousContext = SecurityContextHolder.getContext();
    SecurityContext testContext = SecurityContextHolder.createEmptyContext();
    TestingAuthenticationToken authentication = new TestingAuthenticationToken("user", null);
    authentication.setAuthenticated(true);
    testContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(testContext);

    hop = entityTypeFactory.create("HOP");
    ageAttr = attrMetaFactory.create().setName("age").setDataType(INT);
    hop.addAttribute(ageAttr);
    dobAttr = attrMetaFactory.create().setName("dob").setDataType(DATE);
    hop.addAttribute(dobAttr);
    hop.setAbstract(true);

    lifeLines = entityTypeFactory.create("LifeLines");

    mappingProject = new MappingProject("hop hop hop", 3);
    mappingProject.setIdentifier("asdf");
    MappingTarget mappingTarget = mappingProject.addTarget(hop);
    EntityMapping entityMapping = mappingTarget.addSource(lifeLines);
    AttributeMapping attributeMapping = entityMapping.addAttributeMapping("age");
    attributeMapping.setAlgorithm("$('dob').age()");

    when(dataService.getMeta()).thenReturn(metaDataService);

    controller =
        new MappingServiceController(
            algorithmService,
            mappingService,
            dataService,
            ontologyTagService,
            semanticSearchService,
            menuReaderService,
            mappingJobExecutionFactory,
            jobExecutor,
            jobsController);

    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setMessageConverters(gsonHttpMessageConverter, new StringHttpMessageConverter())
            .build();
  }

  @Test
  void itShouldUpdateExistingAttributeMappingWhenSaving() throws Exception {
    when(mappingService.getMappingProject("asdf")).thenReturn(mappingProject);
    when(menuReaderService.findMenuItemPath(ID)).thenReturn("/menu/main/mappingservice");

    mockMvc
        .perform(
            post(URI + "/saveattributemapping")
                .param("mappingProjectId", "asdf")
                .param("target", "HOP")
                .param("source", "LifeLines")
                .param("targetAttribute", "age")
                .param("algorithm", "$('length').value()")
                .param("algorithmState", "CURATED"))
        .andExpect(redirectedUrl("/menu/main/mappingservice/mappingproject/asdf"));
    MappingProject expected = new MappingProject("hop hop hop", 3);
    expected.setIdentifier("asdf");
    MappingTarget mappingTarget = expected.addTarget(hop);
    EntityMapping entityMapping = mappingTarget.addSource(lifeLines);
    AttributeMapping ageMapping = entityMapping.addAttributeMapping("age");
    ageMapping.setAlgorithm("$('length').value()");
    ageMapping.setAlgorithmState(AttributeMapping.AlgorithmState.CURATED);

    verify(mappingService).updateMappingProject(expected);
  }

  @Test
  void itShouldCreateNewAttributeMappingWhenSavingIfNonePresent() throws Exception {
    when(mappingService.getMappingProject("asdf")).thenReturn(mappingProject);
    when(menuReaderService.findMenuItemPath(ID)).thenReturn("/menu/main/mappingservice");

    heightAttr = attrMetaFactory.create().setName("height").setDataType(INT);
    hop.addAttribute(heightAttr);

    mockMvc
        .perform(
            post(URI + "/saveattributemapping")
                .param("mappingProjectId", "asdf")
                .param("target", "HOP")
                .param("source", "LifeLines")
                .param("targetAttribute", "height")
                .param("algorithm", "$('length').value()")
                .param("algorithmState", "CURATED"))
        .andExpect(redirectedUrl("/menu/main/mappingservice/mappingproject/asdf"));

    MappingProject expected = new MappingProject("hop hop hop", 3);
    expected.setIdentifier("asdf");
    MappingTarget mappingTarget = expected.addTarget(hop);
    EntityMapping entityMapping = mappingTarget.addSource(lifeLines);
    AttributeMapping ageMapping = entityMapping.addAttributeMapping("age");
    ageMapping.setAlgorithm("$('dob').age()");
    AttributeMapping heightMapping = entityMapping.addAttributeMapping("height");
    heightMapping.setAlgorithm("$('length').value()");
    heightMapping.setAlgorithmState(AttributeMapping.AlgorithmState.CURATED);

    verify(mappingService).updateMappingProject(expected);
  }

  @Test
  void itShouldRemoveEmptyAttributeMappingsWhenSaving() throws Exception {
    when(mappingService.getMappingProject("asdf")).thenReturn(mappingProject);
    when(menuReaderService.findMenuItemPath(ID)).thenReturn("/menu/main/mappingservice");

    mockMvc
        .perform(
            post(URI + "/saveattributemapping")
                .param("mappingProjectId", "asdf")
                .param("target", "HOP")
                .param("source", "LifeLines")
                .param("targetAttribute", "age")
                .param("algorithm", "")
                .param("algorithmState", "CURATED"))
        .andExpect(redirectedUrl("/menu/main/mappingservice/mappingproject/asdf"));

    MappingProject expected = new MappingProject("hop hop hop", 3);
    expected.setIdentifier("asdf");
    MappingTarget mappingTarget = expected.addTarget(hop);
    mappingTarget.addSource(lifeLines);
    verify(mappingService).updateMappingProject(expected);
  }

  @Test
  void getFirstAttributeMappingInfo_age() throws Exception {
    when(mappingService.getMappingProject("asdf")).thenReturn(mappingProject);
    MvcResult result =
        mockMvc
            .perform(
                post(URI + "/firstattributemapping")
                    .param("mappingProjectId", "asdf")
                    .param("target", "HOP")
                    .param("source", "LifeLines")
                    .param("skipAlgorithmStates[]", "CURATED", "DISCUSS")
                    .accept(MediaType.APPLICATION_JSON))
            .andReturn();

    String actual = result.getResponse().getContentAsString();

    assertEquals(
        "{\"mappingProjectId\":\"asdf\",\"target\":\"HOP\",\"source\":\"LifeLines\",\"targetAttribute\":\"age\"}",
        actual);
  }

  @Test
  void getFirstAttributeMappingInfo_dob() throws Exception {
    when(mappingService.getMappingProject("asdf")).thenReturn(mappingProject);
    mappingProject
        .getMappingTarget("HOP")
        .getMappingForSource("LifeLines")
        .getAttributeMapping("age")
        .setAlgorithmState(AttributeMapping.AlgorithmState.CURATED);

    MvcResult result2 =
        mockMvc
            .perform(
                post(URI + "/firstattributemapping")
                    .param("mappingProjectId", "asdf")
                    .param("target", "HOP")
                    .param("source", "LifeLines")
                    .param("skipAlgorithmStates[]", "CURATED", "DISCUSS")
                    .accept(MediaType.APPLICATION_JSON))
            .andReturn();

    String actual2 = result2.getResponse().getContentAsString();

    assertEquals(
        "{\"mappingProjectId\":\"asdf\",\"target\":\"HOP\",\"source\":\"LifeLines\",\"targetAttribute\":\"dob\"}",
        actual2);
  }

  @Test
  void testIsNewEntityReturnsTrueIfEntityIsNew() throws Exception {
    MockHttpServletResponse response =
        mockMvc
            .perform(
                get(URI + "/isNewEntity")
                    .param("targetEntityTypeId", "blah")
                    .accept(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse();
    assertEquals(
        "true",
        response.getContentAsString(),
        "When checking for a new entity type, the result should be the String \"true\"");
    assertEquals(APPLICATION_JSON_UTF8_VALUE, response.getContentType());
    verify(dataService).hasEntityType("blah");
  }

  @Test
  void testIsNewEntityReturnsFalseIfEntityExists() throws Exception {
    when(dataService.hasEntityType("it_emx_test_TypeTest")).thenReturn(true);
    when(dataService.getEntityType("it_emx_test_TypeTest")).thenReturn(mock(EntityType.class));
    MockHttpServletResponse response =
        mockMvc
            .perform(
                get(URI + "/isNewEntity")
                    .param("targetEntityTypeId", "it_emx_test_TypeTest")
                    .accept(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse();
    assertEquals(APPLICATION_JSON_UTF8_VALUE, response.getContentType());
    assertEquals(
        "false",
        response.getContentAsString(),
        "When checking for an existing entity type, the result should be the String \"false\"");
  }

  @Test
  void getFirstAttributeMappingInfo_none() throws Exception {
    when(mappingService.getMappingProject("asdf")).thenReturn(mappingProject);
    mappingProject
        .getMappingTarget("HOP")
        .getMappingForSource("LifeLines")
        .getAttributeMapping("age")
        .setAlgorithmState(AttributeMapping.AlgorithmState.DISCUSS);

    MappingTarget mappingTarget = mappingProject.getMappingTarget("HOP");
    EntityMapping entityMapping = mappingTarget.getMappingForSource("LifeLines");
    AttributeMapping attributeMapping = entityMapping.addAttributeMapping("dob");
    attributeMapping.setAlgorithm("$('dob').age()");
    attributeMapping.setAlgorithmState(AttributeMapping.AlgorithmState.DISCUSS);

    MvcResult result3 =
        mockMvc
            .perform(
                post(URI + "/firstattributemapping")
                    .param("mappingProjectId", "asdf")
                    .param("target", "HOP")
                    .param("source", "LifeLines")
                    .param("skipAlgorithmStates[]", "CURATED", "DISCUSS")
                    .accept(MediaType.APPLICATION_JSON))
            .andReturn();

    String actual3 = result3.getResponse().getContentAsString();

    assertEquals("", actual3);
  }

  @Test
  void testScheduleMappingJobUnknownMappingProjectId() throws Exception {
    mockMvc
        .perform(
            post(URI + "/map")
                .param("mappingProjectId", "mappingProjectId")
                .param("targetEntityTypeId", "targetEntityTypeId")
                .param("label", "label")
                .param("package", "package")
                .accept("text/plain"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType("text/plain"))
        .andExpect(content().string("No mapping project found with ID mappingProjectId"));
  }

  @Test
  void testScheduleMappingJobUnknownPackage() throws Exception {
    when(mappingService.getMappingProject("mappingProjectId")).thenReturn(mappingProject);

    mockMvc
        .perform(
            post(URI + "/map")
                .param("mappingProjectId", "mappingProjectId")
                .param("targetEntityTypeId", "targetEntityTypeId")
                .param("label", "label")
                .param("package", "sys")
                .accept("text/plain"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType("text/plain"))
        .andExpect(content().string("No package found with ID sys"));
  }

  @Test
  void testScheduleMappingJobSystemPackage() throws Exception {
    when(mappingService.getMappingProject("mappingProjectId")).thenReturn(mappingProject);
    Package systemPackage = mock(Package.class);
    when(systemPackage.getId()).thenReturn("sys");
    when(metaDataService.getPackage("sys")).thenReturn(Optional.of(systemPackage));

    mockMvc
        .perform(
            post(URI + "/map")
                .param("mappingProjectId", "mappingProjectId")
                .param("targetEntityTypeId", "targetEntityTypeId")
                .param("label", "label")
                .param("package", "sys")
                .accept("text/plain"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType("text/plain"))
        .andExpect(content().string("Package [sys] is a system package."));
  }

  @Test
  void testMap() throws Exception {
    when(mappingService.getMappingProject("mappingProjectId")).thenReturn(mappingProject);
    Package aPackage = mock(Package.class);
    when(aPackage.getId()).thenReturn("base");
    when(aPackage.getRootPackage()).thenReturn(null);
    when(metaDataService.getPackage("base")).thenReturn(Optional.of(aPackage));

    when(mappingJobExecutionFactory.create()).thenReturn(mappingJobExecution);
    when(mappingJobExecution.getEntityType()).thenReturn(mappingJobExecutionMetadata);
    when(mappingJobExecution.getIdValue()).thenReturn("abcde");
    when(mappingJobExecutionMetadata.getId()).thenReturn("MappingJobExecution");

    mockMvc
        .perform(
            post(URI + "/map")
                .param("mappingProjectId", "mappingProjectId")
                .param("targetEntityTypeId", "targetEntityTypeId")
                .param("label", "label")
                .param("package", "base")
                .accept("text/plain"))
        .andExpect(status().isCreated())
        .andExpect(content().contentType("text/plain"))
        .andExpect(content().string("/api/v2/MappingJobExecution/abcde"));

    verify(jobExecutor).submit(mappingJobExecution);
    verify(mappingJobExecution).setMappingProjectId("mappingProjectId");
    verify(mappingJobExecution).setLabel("label");
    verify(mappingJobExecution).setAddSourceAttribute(null);
    verify(mappingJobExecution).setTargetEntityTypeId("targetEntityTypeId");
    verify(mappingJobExecution).setPackageId("base");
    verify(mappingJobExecution).getEntityType();
    verify(mappingJobExecution).getIdValue();
    Mockito.verifyNoMoreInteractions(mappingJobExecution);
  }

  @Test
  void testCreateIntegratedEntity() throws Exception {
    when(mappingJobExecution.getEntityType()).thenReturn(mappingJobExecutionMetadata);
    when(mappingJobExecution.getIdValue()).thenReturn("mappingJobExecution");
    when(mappingJobExecutionMetadata.getId()).thenReturn("mappingJobExecutionMetadata");
    when(mappingJobExecutionFactory.create()).thenReturn(mappingJobExecution);
    when(jobsController.createJobExecutionViewHref(
            "/api/v2/mappingJobExecutionMetadata/mappingJobExecution", 1000))
        .thenReturn("/jobs/viewJob/?jobHref=jobHref&refreshTimeoutMillis=1000");

    mockMvc
        .perform(
            post(URI + "/createIntegratedEntity")
                .param("mappingProjectId", "mappingProjectId")
                .param("targetEntityTypeId", "targetEntityTypeId")
                .param("label", "label")
                .param("package", "base")
                .accept("text/plain"))
        .andExpect(status().isFound())
        .andExpect(
            header()
                .string("Location", "/jobs/viewJob/?jobHref=jobHref&refreshTimeoutMillis=1000"));

    verify(jobExecutor).submit(mappingJobExecution);
    verify(mappingJobExecution).setMappingProjectId("mappingProjectId");
    verify(mappingJobExecution).setLabel("label");
    verify(mappingJobExecution).setAddSourceAttribute(null);
    verify(mappingJobExecution).setTargetEntityTypeId("targetEntityTypeId");
    verify(mappingJobExecution).setPackageId("base");
    verify(mappingJobExecution).getIdValue();
    verify(mappingJobExecution).getEntityType();
    Mockito.verifyNoMoreInteractions(mappingJobExecution);
  }

  @Test
  void testViewMappingProject() {
    when(mappingService.getMappingProject("hop hop hop")).thenReturn(mappingProject);
    when(dataService.getEntityTypeIds()).thenReturn(Stream.of("LifeLines", "entity1", "entity2"));
    when(mappingService.getCompatibleEntityTypes(hop))
        .thenReturn(Stream.of(lifeLines, target1, target2));

    when(dataService.getEntityType("HOP")).thenReturn(hop);
    OntologyTerm ontologyTermAge = OntologyTerm.create("iri1", "label1");
    OntologyTerm ontologyTermDateOfBirth = OntologyTerm.create("iri2", "label2");
    when(ontologyTagService.getTagsForAttribute(hop, ageAttr))
        .thenReturn(ImmutableMultimap.of(isAssociatedWith, ontologyTermAge));
    when(ontologyTagService.getTagsForAttribute(hop, dobAttr))
        .thenReturn(ImmutableMultimap.of(isAssociatedWith, ontologyTermDateOfBirth));

    when(dataService.getMeta()).thenReturn(metaDataService);
    when(metaDataService.getPackages()).thenReturn(asList(system, base));
    when(system.getId()).thenReturn(PACKAGE_SYSTEM); // system package, not a valid choice
    when(base.getId()).thenReturn("base");
    when(dataService.getEntityType("entity1")).thenReturn(target1);
    when(dataService.getEntityType("entity2")).thenReturn(target2);

    String view = controller.viewMappingProject("hop hop hop", model);

    assertEquals("view-single-mapping-project", view);
    verify(model).addAttribute("entityTypes", asList(target1, target2));
    verify(model).addAttribute("packages", singletonList(base));
    verify(model).addAttribute("compatibleTargetEntities", asList(lifeLines, target1, target2));
    verify(model).addAttribute("selectedTarget", "HOP");
    verify(model).addAttribute("mappingProject", mappingProject);
    verify(model)
        .addAttribute(
            "attributeTagMap",
            ImmutableMap.of(
                "dob",
                singletonList(ontologyTermDateOfBirth),
                "age",
                singletonList(ontologyTermAge)));
  }

  @Test
  void testAutoGenerateAlgorithms() {
    EntityType test = entityTypeFactory.create("TEST");
    Attribute idAttr = attrMetaFactory.create().setName("id").setDataType(INT);
    test.addAttribute(idAttr);
    Attribute computedAttr =
        attrMetaFactory
            .create()
            .setName("computed")
            .setDataType(INT)
            .setExpression("Very expressive");
    test.addAttribute(computedAttr);

    controller.autoGenerateAlgorithms(null, test, test, null);

    verify(algorithmService).autoGenerateAlgorithm(test, test, null);
    verifyNoMoreInteractions(algorithmService);
  }

  @Test
  void testGetSemanticSearchAttributeMappingRelevantAttributes() {
    Map<String, String> requestBody =
        ImmutableMap.of("mappingProjectId", "id0", "target", "target0", "source", "source0");
    MappingProject mappingProject = mock(MappingProject.class);
    when(mappingService.getMappingProject("id0")).thenReturn(mappingProject);
    MappingTarget mappingTarget = mock(MappingTarget.class);
    EntityMapping entityMapping = mock(EntityMapping.class);
    EntityType targetEntityType = mock(EntityType.class);
    when(entityMapping.getTargetEntityType()).thenReturn(targetEntityType);
    EntityType sourceEntityType = mock(EntityType.class);
    Attribute stringAttribute =
        when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    when(stringAttribute.getName()).thenReturn("stringAttribute");
    Attribute compoundAttribute =
        when(mock(Attribute.class).getDataType()).thenReturn(COMPOUND).getMock();
    when(compoundAttribute.getName()).thenReturn("compoundAttribute");
    when(sourceEntityType.getAtomicAttributes())
        .thenReturn(asList(stringAttribute, compoundAttribute));
    when(entityMapping.getSourceEntityType()).thenReturn(sourceEntityType);
    when(mappingTarget.getMappingForSource("source0")).thenReturn(entityMapping);
    when(mappingProject.getMappingTarget("target0")).thenReturn(mappingTarget);
    Multimap<Relation, OntologyTerm> multiMap = ArrayListMultimap.create();
    when(ontologyTagService.getTagsForAttribute(targetEntityType, null)).thenReturn(multiMap);
    when(semanticSearchService.findAttributes(sourceEntityType, targetEntityType, null, emptySet()))
        .thenReturn(
            AttributeSearchResults.create(
                stringAttribute,
                Hits.create(
                    Hit.create(
                        ExplainedAttribute.create(stringAttribute, emptySet(), false), 1f))));
    assertEquals(
        singletonList(create(stringAttribute, emptySet(), false)),
        controller.getSemanticSearchAttributeMapping(requestBody));
  }

  @Test
  void testGetSemanticSearchAttributeMappingNoRelevantAttributes() {
    Map<String, String> requestBody =
        ImmutableMap.of("mappingProjectId", "id0", "target", "target0", "source", "source0");
    MappingProject mappingProject = mock(MappingProject.class);
    when(mappingService.getMappingProject("id0")).thenReturn(mappingProject);
    MappingTarget mappingTarget = mock(MappingTarget.class);
    EntityMapping entityMapping = mock(EntityMapping.class);
    EntityType targetEntityType = mock(EntityType.class);
    when(entityMapping.getTargetEntityType()).thenReturn(targetEntityType);
    EntityType sourceEntityType = mock(EntityType.class);
    Attribute stringAttribute =
        when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    when(stringAttribute.getName()).thenReturn("stringAttribute");
    Attribute compoundAttribute =
        when(mock(Attribute.class).getDataType()).thenReturn(COMPOUND).getMock();
    when(compoundAttribute.getName()).thenReturn("compoundAttribute");
    when(sourceEntityType.getAtomicAttributes())
        .thenReturn(asList(stringAttribute, compoundAttribute));
    when(entityMapping.getSourceEntityType()).thenReturn(sourceEntityType);
    when(mappingTarget.getMappingForSource("source0")).thenReturn(entityMapping);
    when(mappingProject.getMappingTarget("target0")).thenReturn(mappingTarget);
    Multimap<Relation, OntologyTerm> multiMap = ArrayListMultimap.create();
    when(ontologyTagService.getTagsForAttribute(targetEntityType, null)).thenReturn(multiMap);
    when(semanticSearchService.findAttributes(sourceEntityType, targetEntityType, null, emptySet()))
        .thenReturn(AttributeSearchResults.create(stringAttribute, Hits.create(emptyList())));
    assertEquals(
        singletonList(create(stringAttribute, emptySet(), false)),
        controller.getSemanticSearchAttributeMapping(requestBody));
  }

  @Test
  void testAddEntityMapping() {
    String mappingProjectId = "MyMappingProjectId";
    MappingProject mappingProject = mock(MappingProject.class);
    when(mappingService.getMappingProject(mappingProjectId)).thenReturn(mappingProject);

    String targetEntityTypeId = "MyTargetEntityTypeId";
    String sourceEntityTypeId = "MySourceEntityTypeId";
    EntityType targetEntityType = mock(EntityType.class);
    EntityType sourceEntityType = mock(EntityType.class);
    doReturn(targetEntityType).when(dataService).getEntityType(targetEntityTypeId);
    doReturn(sourceEntityType).when(dataService).getEntityType(sourceEntityTypeId);

    MappingTarget mappingTarget = mock(MappingTarget.class);
    EntityMapping entityMapping = mock(EntityMapping.class);
    when(mappingTarget.addSource(sourceEntityType)).thenReturn(entityMapping);
    when(mappingProject.getMappingTarget(targetEntityTypeId)).thenReturn(mappingTarget);

    controller.addEntityMapping(mappingProjectId, targetEntityTypeId, sourceEntityTypeId, null);

    verify(mappingTarget).addSource(sourceEntityType);
    verify(algorithmService)
        .autoGenerateAlgorithm(sourceEntityType, targetEntityType, entityMapping);
    verify(mappingService, times(2)).updateMappingProject(mappingProject);
  }

  @Test
  void testAddEntityMappingCopyAlgorithms() {
    String mappingProjectId = "MyMappingProjectId";
    MappingProject mappingProject = mock(MappingProject.class);
    when(mappingService.getMappingProject(mappingProjectId)).thenReturn(mappingProject);

    String targetEntityTypeId = "MyTargetEntityTypeId";
    String sourceEntityTypeId = "MySourceEntityTypeId";
    String algorithmSourceEntityTypeId = "MyAlgorithmSourceEntityTypeId";
    EntityType targetEntityType = mock(EntityType.class);
    EntityType sourceEntityType = mock(EntityType.class);
    EntityType algorithmSourceEntityType = mock(EntityType.class);
    doReturn(targetEntityType).when(dataService).getEntityType(targetEntityTypeId);
    doReturn(sourceEntityType).when(dataService).getEntityType(sourceEntityTypeId);
    doReturn(algorithmSourceEntityType)
        .when(dataService)
        .getEntityType(algorithmSourceEntityTypeId);

    MappingTarget mappingTarget = mock(MappingTarget.class);
    EntityMapping entityMapping = mock(EntityMapping.class);
    when(mappingTarget.addSource(sourceEntityType)).thenReturn(entityMapping);
    EntityMapping sourceEntityMapping = mock(EntityMapping.class);
    when(mappingTarget.getMappingForSource(algorithmSourceEntityTypeId))
        .thenReturn(sourceEntityMapping);
    when(mappingProject.getMappingTarget(targetEntityTypeId)).thenReturn(mappingTarget);

    controller.addEntityMapping(
        mappingProjectId, targetEntityTypeId, sourceEntityTypeId, algorithmSourceEntityTypeId);

    verify(mappingTarget).addSource(sourceEntityType);
    verify(algorithmService).copyAlgorithms(sourceEntityMapping, entityMapping);
    verify(mappingService, times(2)).updateMappingProject(mappingProject);
  }
}
