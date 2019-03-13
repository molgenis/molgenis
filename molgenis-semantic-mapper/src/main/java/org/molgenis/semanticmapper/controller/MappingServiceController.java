package org.molgenis.semanticmapper.controller;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Streams.stream;
import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.util.PackageUtils.isSystemPackage;
import static org.molgenis.data.validation.meta.NameValidator.validateEntityName;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSu;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;
import static org.molgenis.semanticmapper.controller.MappingServiceController.URI;
import static org.molgenis.semanticmapper.mapping.model.CategoryMapping.create;
import static org.molgenis.semanticmapper.mapping.model.CategoryMapping.createEmpty;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.core.ui.data.importer.wizard.ImportWizardController;
import org.molgenis.core.ui.jobs.JobsController;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.support.AggregateQueryImpl;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.util.EntityTypeUtils;
import org.molgenis.dataexplorer.controller.DataExplorerController;
import org.molgenis.jobs.JobExecutionUriUtils;
import org.molgenis.jobs.JobExecutor;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.security.core.runas.RunAsSystemAspect;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.semanticmapper.data.request.GenerateAlgorithmRequest;
import org.molgenis.semanticmapper.data.request.MappingServiceRequest;
import org.molgenis.semanticmapper.job.MappingJobExecution;
import org.molgenis.semanticmapper.job.MappingJobExecutionFactory;
import org.molgenis.semanticmapper.mapping.model.AlgorithmResult;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping.AlgorithmState;
import org.molgenis.semanticmapper.mapping.model.CategoryMapping;
import org.molgenis.semanticmapper.mapping.model.EntityMapping;
import org.molgenis.semanticmapper.mapping.model.MappingProject;
import org.molgenis.semanticmapper.mapping.model.MappingTarget;
import org.molgenis.semanticmapper.service.AlgorithmService;
import org.molgenis.semanticmapper.service.MappingService;
import org.molgenis.semanticmapper.service.impl.AlgorithmEvaluation;
import org.molgenis.semanticsearch.explain.bean.AttributeSearchResults;
import org.molgenis.semanticsearch.explain.bean.ExplainedAttribute;
import org.molgenis.semanticsearch.explain.bean.ExplainedAttributeDto;
import org.molgenis.semanticsearch.semantic.Hit;
import org.molgenis.semanticsearch.service.OntologyTagService;
import org.molgenis.semanticsearch.service.SemanticSearchService;
import org.molgenis.web.PluginController;
import org.molgenis.web.menu.MenuReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(URI)
public class MappingServiceController extends PluginController {
  private static final Logger LOG = LoggerFactory.getLogger(MappingServiceController.class);

  public static final String ID = "mappingservice";
  public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;
  private static final String VIEW_MAPPING_PROJECTS = "view-mapping-projects";
  private static final String VIEW_ATTRIBUTE_MAPPING = "view-attribute-mapping";
  private static final String VIEW_SINGLE_MAPPING_PROJECT = "view-single-mapping-project";
  private static final String VIEW_CATEGORY_MAPPING_EDITOR = "view-advanced-mapping-editor";
  private static final String VIEW_ATTRIBUTE_MAPPING_FEEDBACK = "view-attribute-mapping-feedback";

  private final MappingService mappingService;
  private final AlgorithmService algorithmService;
  private final DataService dataService;
  private final OntologyTagService ontologyTagService;
  private final SemanticSearchService semanticSearchService;
  private final MenuReaderService menuReaderService;
  private final MappingJobExecutionFactory mappingJobExecutionFactory;
  private final UserAccountService userAccountService;
  private final JobExecutor jobExecutor;
  private final JobsController jobsController;

  public MappingServiceController(
      AlgorithmService algorithmService,
      MappingService mappingService,
      DataService dataService,
      OntologyTagService ontologyTagService,
      SemanticSearchService semanticSearchService,
      MenuReaderService menuReaderService,
      MappingJobExecutionFactory mappingJobExecutionFactory,
      UserAccountService userAccountService,
      JobExecutor jobExecutor,
      JobsController jobsController) {
    super(URI);
    this.algorithmService = algorithmService;
    this.mappingService = mappingService;
    this.dataService = dataService;
    this.ontologyTagService = ontologyTagService;
    this.semanticSearchService = semanticSearchService;
    this.menuReaderService = menuReaderService;
    this.mappingJobExecutionFactory = mappingJobExecutionFactory;
    this.userAccountService = userAccountService;
    this.jobExecutor = jobExecutor;
    this.jobsController = jobsController;
  }

  /**
   * Initializes the model with all mapping projects and all entities to the model.
   *
   * @param model the model to initialized
   * @return view name of the mapping projects list
   */
  @GetMapping
  public String viewMappingProjects(Model model) {
    model.addAttribute("mappingProjects", mappingService.getAllMappingProjects());
    model.addAttribute("entityTypes", getWritableEntityTypes());
    model.addAttribute("user", getCurrentUsername());
    model.addAttribute("admin", currentUserIsSu());
    model.addAttribute(
        "importerUri", menuReaderService.findMenuItemPath(ImportWizardController.ID));
    return VIEW_MAPPING_PROJECTS;
  }

  /**
   * Adds a new mapping project.
   *
   * @param name name of the mapping project
   * @param targetEntity name of the project's first {@link MappingTarget}'s target entity
   * @return redirect URL for the newly created mapping project
   */
  @PostMapping("/addMappingProject")
  public String addMappingProject(
      @RequestParam("mapping-project-name") String name,
      @RequestParam("target-entity") String targetEntity,
      @RequestParam("depth") int depth) {
    MappingProject newMappingProject = mappingService.addMappingProject(name, targetEntity, depth);
    return "redirect:"
        + getMappingServiceMenuUrl()
        + "/mappingproject/"
        + newMappingProject.getIdentifier();
  }

  /**
   * Removes a mapping project
   *
   * @param mappingProjectId the ID of the mapping project
   * @return redirect url to the same page to force a refresh
   */
  @PostMapping("/removeMappingProject")
  public String deleteMappingProject(@RequestParam() String mappingProjectId) {
    MappingProject project = mappingService.getMappingProject(mappingProjectId);
    LOG.info("Deleting mappingProject {}", project.getName());
    mappingService.deleteMappingProject(mappingProjectId);
    return "redirect:" + getMappingServiceMenuUrl();
  }

  /**
   * Removes a attribute mapping
   *
   * @param mappingProjectId the ID of the mapping project
   * @param target the target entity
   * @param source the source entity
   * @param attribute the attribute that is mapped
   */
  @PostMapping("/removeAttributeMapping")
  public String removeAttributeMapping(
      @RequestParam() String mappingProjectId,
      @RequestParam() String target,
      @RequestParam() String source,
      @RequestParam() String attribute) {
    MappingProject project = mappingService.getMappingProject(mappingProjectId);
    project.getMappingTarget(target).getMappingForSource(source).deleteAttributeMapping(attribute);
    mappingService.updateMappingProject(project);
    return "redirect:" + getMappingServiceMenuUrl() + "/mappingproject/" + project.getIdentifier();
  }

  /**
   * Adds a new {@link EntityMapping} to an existing {@link MappingTarget}
   *
   * @param target the name of the {@link MappingTarget}'s entity to add a source entity to
   * @param source the name of the source entity of the newly added {@link EntityMapping}
   * @param mappingProjectId the ID of the {@link MappingTarget}'s {@link MappingProject}
   * @return redirect URL for the mapping project
   */
  @PostMapping("/addEntityMapping")
  public String addEntityMapping(
      @RequestParam String mappingProjectId, String target, String source) {
    EntityType sourceEntityType = dataService.getEntityType(source);
    EntityType targetEntityType = dataService.getEntityType(target);

    MappingProject project = mappingService.getMappingProject(mappingProjectId);
    EntityMapping mapping = project.getMappingTarget(target).addSource(sourceEntityType);
    mappingService.updateMappingProject(project);
    autoGenerateAlgorithms(mapping, sourceEntityType, targetEntityType, project);

    return "redirect:" + getMappingServiceMenuUrl() + "/mappingproject/" + mappingProjectId;
  }

  /**
   * Removes entity mapping
   *
   * @param mappingProjectId ID of the mapping project to remove entity mapping from
   * @param target entity name of the mapping target
   * @param source entity name of the mapping source
   * @return redirect url of the mapping project's page
   */
  @PostMapping("/removeEntityMapping")
  public String removeEntityMapping(
      @RequestParam String mappingProjectId, String target, String source) {
    MappingProject project = mappingService.getMappingProject(mappingProjectId);
    project.getMappingTarget(target).removeSource(source);
    mappingService.updateMappingProject(project);
    return "redirect:" + getMappingServiceMenuUrl() + "/mappingproject/" + mappingProjectId;
  }

  @PostMapping("/validateAttrMapping")
  @ResponseBody
  public AttributeMappingValidationReport validateAttributeMapping(
      @Valid @RequestBody MappingServiceRequest mappingServiceRequest) {
    String targetEntityName = mappingServiceRequest.getTargetEntityName();
    EntityType targetEntityType = dataService.getEntityType(targetEntityName);

    String targetAttributeName = mappingServiceRequest.getTargetAttributeName();
    Attribute targetAttr = targetEntityType.getAttribute(targetAttributeName);
    if (targetAttr == null) {
      throw new UnknownAttributeException(targetEntityType, targetAttributeName);
    }

    String algorithm = mappingServiceRequest.getAlgorithm();
    Long offset = mappingServiceRequest.getOffset();
    Long num = mappingServiceRequest.getNum();
    int depth = mappingServiceRequest.getDepth();

    Query<Entity> query = new QueryImpl<>().offset(offset.intValue()).pageSize(num.intValue());
    String sourceEntityName = mappingServiceRequest.getSourceEntityName();
    Iterable<Entity> sourceEntities = () -> dataService.findAll(sourceEntityName, query).iterator();

    long total = dataService.count(sourceEntityName, new QueryImpl<>());
    long nrSuccess = 0;
    long nrErrors = 0;

    Map<String, String> errorMessages = new LinkedHashMap<>();
    for (AlgorithmEvaluation evaluation :
        algorithmService.applyAlgorithm(targetAttr, algorithm, sourceEntities, depth)) {
      if (evaluation.hasError()) {
        errorMessages.put(
            evaluation.getEntity().getIdValue().toString(), evaluation.getErrorMessage());
        ++nrErrors;
      } else {
        ++nrSuccess;
      }
    }

    return new AttributeMappingValidationReport(total, nrSuccess, nrErrors, errorMessages);
  }

  private static class AttributeMappingValidationReport {
    private final Long total;
    private final Long nrSuccess;
    private final Long nrErrors;
    private final Map<String, String> errorMessages;

    public AttributeMappingValidationReport(
        Long total, Long nrSuccess, Long nrErrors, Map<String, String> errorMessages) {
      this.total = total;
      this.nrSuccess = nrSuccess;
      this.nrErrors = nrErrors;
      this.errorMessages = errorMessages;
    }

    @SuppressWarnings("unused")
    public Long getTotal() {
      return total;
    }

    @SuppressWarnings("unused")
    public Long getNrSuccess() {
      return nrSuccess;
    }

    @SuppressWarnings("unused")
    public Long getNrErrors() {
      return nrErrors;
    }

    @SuppressWarnings("unused")
    public Map<String, String> getErrorMessages() {
      return errorMessages;
    }
  }

  /**
   * Adds a new {@link AttributeMapping} to an {@link EntityMapping}.
   *
   * @param mappingProjectId ID of the mapping project
   * @param target name of the target entity
   * @param source name of the source entity
   * @param targetAttribute name of the target attribute
   * @param algorithm the mapping algorithm
   * @return redirect URL for the attributemapping
   */
  @PostMapping("/saveattributemapping")
  public String saveAttributeMapping(
      @RequestParam() String mappingProjectId,
      @RequestParam() String target,
      @RequestParam() String source,
      @RequestParam() String targetAttribute,
      @RequestParam() String algorithm,
      @RequestParam() AlgorithmState algorithmState) {
    MappingProject mappingProject = mappingService.getMappingProject(mappingProjectId);
    MappingTarget mappingTarget = mappingProject.getMappingTarget(target);
    EntityMapping mappingForSource = mappingTarget.getMappingForSource(source);
    if (algorithm.isEmpty()) {
      mappingForSource.deleteAttributeMapping(targetAttribute);
    } else {
      AttributeMapping attributeMapping = mappingForSource.getAttributeMapping(targetAttribute);
      if (attributeMapping == null) {
        attributeMapping = mappingForSource.addAttributeMapping(targetAttribute);
      }
      attributeMapping.setAlgorithm(algorithm);
      attributeMapping.setAlgorithmState(algorithmState);
    }
    mappingService.updateMappingProject(mappingProject);
    return "redirect:"
        + getMappingServiceMenuUrl()
        + "/mappingproject/"
        + mappingProject.getIdentifier();
  }

  /**
   * Find the firstattributeMapping skip the the algorithmStates that are given in the {@link
   * AttributeMapping} to an {@link EntityMapping}.
   *
   * @param mappingProjectId ID of the mapping project
   * @param target name of the target entity
   * @param skipAlgorithmStates the mapping algorithm states that should skip
   */
  @PostMapping("/firstattributemapping")
  @ResponseBody
  public FirstAttributeMappingInfo getFirstAttributeMappingInfo(
      @RequestParam() String mappingProjectId,
      @RequestParam() String target,
      @RequestParam(value = "skipAlgorithmStates[]") List<AlgorithmState> skipAlgorithmStates,
      Model model) {
    MappingProject mappingProject = mappingService.getMappingProject(mappingProjectId);
    MappingTarget mappingTarget = mappingProject.getMappingTarget(target);
    List<String> sourceNames =
        mappingTarget
            .getEntityMappings()
            .stream()
            .map(i -> i.getSourceEntityType().getId())
            .collect(toList());

    EntityType targetEntityMeta = mappingTarget.getTarget();
    for (Attribute attribute : targetEntityMeta.getAtomicAttributes()) {
      if (attribute.equals(targetEntityMeta.getIdAttribute())) {
        continue;
      }

      for (String source : sourceNames) {
        EntityMapping entityMapping = mappingTarget.getMappingForSource(source);
        AttributeMapping attributeMapping = entityMapping.getAttributeMapping(attribute.getName());

        if (null != attributeMapping) {
          AlgorithmState algorithmState = attributeMapping.getAlgorithmState();
          if (null != skipAlgorithmStates && skipAlgorithmStates.contains(algorithmState)) {
            continue;
          }
        }

        return FirstAttributeMappingInfo.create(
            mappingProjectId, target, source, attribute.getName());
      }
    }

    return null;
  }

  /**
   * Displays a mapping project.
   *
   * @param identifier identifier of the {@link MappingProject}
   * @param model the model
   * @return View name for a single mapping project
   */
  @GetMapping("/mappingproject/{id}")
  public String viewMappingProject(@PathVariable("id") String identifier, Model model) {
    MappingProject project = mappingService.getMappingProject(identifier);
    MappingTarget mappingTarget = project.getMappingTargets().get(0);
    String target = mappingTarget.getName();
    model.addAttribute("entityTypes", getNewSources(mappingTarget));
    model.addAttribute(
        "compatibleTargetEntities",
        mappingService.getCompatibleEntityTypes(mappingTarget.getTarget()).collect(toList()));
    model.addAttribute("selectedTarget", target);
    model.addAttribute("mappingProject", project);
    model.addAttribute("attributeTagMap", getTagsForAttribute(target, project));
    model.addAttribute(
        "packages",
        dataService
            .getMeta()
            .getPackages()
            .stream()
            .filter(p -> !isSystemPackage(p))
            .collect(toList()));
    return VIEW_SINGLE_MAPPING_PROJECT;
  }

  @PostMapping("/mappingproject/clone")
  public String cloneMappingProject(@RequestParam("mappingProjectId") String mappingProjectId) {
    mappingService.cloneMappingProject(mappingProjectId);
    return "redirect:" + getMappingServiceMenuUrl();
  }

  @PostMapping("/mappingproject/setDepth")
  public String cloneMappingProject(
      @RequestParam("mappingProjectId") String mappingProjectId, @RequestParam("depth") int depth) {
    MappingProject mappingProject = mappingService.getMappingProject(mappingProjectId);
    mappingProject.setDepth(depth);
    mappingService.updateMappingProject(mappingProject);
    return "redirect:" + getMappingServiceMenuUrl();
  }

  /**
   * This controller will first of all check if the user-defined search terms exist. If so, the
   * searchTerms will be used directly in the SemanticSearchService. If the searchTerms are not
   * defined by users, it will use the ontologyTermTags in the SemantiSearchService. If neither of
   * the searchTerms and the OntologyTermTags exist, it will use the information from the
   * targetAttribute in the SemanticSearchService
   *
   * <p>If string terms are sent to the SemanticSearchService, they will be first of all converted
   * to the ontologyTerms using findTag method
   */
  @PostMapping(value = "/attributeMapping/semanticsearch", consumes = APPLICATION_JSON_VALUE)
  @ResponseBody
  public List<ExplainedAttributeDto> getSemanticSearchAttributeMapping(
      @RequestBody Map<String, String> requestBody) {
    String mappingProjectId = requestBody.get("mappingProjectId");
    String target = requestBody.get("target");
    String source = requestBody.get("source");
    String targetAttributeName = requestBody.get("targetAttribute");
    String searchTermsString = requestBody.get("searchTerms");
    Set<String> searchTerms = new HashSet<>();

    if (StringUtils.isNotBlank(searchTermsString)) {
      searchTerms.addAll(
          Sets.newHashSet(searchTermsString.toLowerCase().split("\\s+or\\s+"))
              .stream()
              .filter(StringUtils::isNotBlank)
              .map(String::trim)
              .collect(Collectors.toSet()));
    }

    MappingProject project = mappingService.getMappingProject(mappingProjectId);
    MappingTarget mappingTarget = project.getMappingTarget(target);
    EntityMapping entityMapping = mappingTarget.getMappingForSource(source);

    Attribute targetAttribute =
        entityMapping.getTargetEntityType().getAttribute(targetAttributeName);

    AttributeSearchResults attributeSearchResults =
        semanticSearchService.findAttributes(
            entityMapping.getSourceEntityType(),
            entityMapping.getTargetEntityType(),
            targetAttribute,
            searchTerms);

    // If no relevant attributes are found, return all source attributes
    if (attributeSearchResults.getHits().iterator().hasNext()) {
      return stream(entityMapping.getSourceEntityType().getAtomicAttributes())
          .filter(attribute -> attribute.getDataType() != COMPOUND)
          .map(ExplainedAttributeDto::create)
          .collect(toList());
    }
    return stream(attributeSearchResults.getHits())
        .map(Hit::getResult)
        .map(this::toExplainedAttributeDto)
        .collect(toList());
  }

  private ExplainedAttributeDto toExplainedAttributeDto(ExplainedAttribute attributeSearchHit) {
    return ExplainedAttributeDto.create(
        attributeSearchHit.getAttribute(),
        attributeSearchHit.getExplainedQueryStrings(),
        attributeSearchHit.isHighQuality());
  }

  @PostMapping(value = "/attributemapping/algorithm", consumes = APPLICATION_JSON_VALUE)
  @ResponseBody
  public String getSuggestedAlgorithm(
      @RequestBody GenerateAlgorithmRequest generateAlgorithmRequest) {
    EntityType targetEntityType =
        dataService.getEntityType(generateAlgorithmRequest.getTargetEntityTypeId());

    EntityType sourceEntityType =
        dataService.getEntityType(generateAlgorithmRequest.getSourceEntityTypeId());

    Attribute targetAttribute =
        targetEntityType.getAttribute(generateAlgorithmRequest.getTargetAttributeName());

    List<Attribute> sourceAttributes =
        generateAlgorithmRequest
            .getSourceAttributes()
            .stream()
            .map(sourceEntityType::getAttribute)
            .collect(toList());

    return algorithmService.generateAlgorithm(
        targetAttribute, targetEntityType, sourceAttributes, sourceEntityType);
  }

  /**
   * Checks if an entityTypeID already exists. Used to validate ID field in the "New dataset" tab
   * when creating a new integrated dataset.
   */
  @GetMapping("/isNewEntity")
  @ResponseBody
  public boolean isNewEntity(@RequestParam String targetEntityTypeId) {
    return !dataService.hasEntityType(targetEntityTypeId);
  }

  /**
   * Creates the integrated entity for a mapping project's target
   *
   * @param mappingProjectId ID of the mapping project
   * @param targetEntityTypeId ID of the target entity to create or update
   * @param label label of the target entity to create
   * @param packageId ID of the package to put the newly created entity in
   * @return redirect URL to the data explorer displaying the newly generated entity
   */
  @PostMapping("/createIntegratedEntity")
  public String createIntegratedEntity(
      @RequestParam String mappingProjectId,
      @RequestParam String targetEntityTypeId,
      @RequestParam(required = false) String label,
      @RequestParam(required = false, name = "package") String packageId,
      @RequestParam(required = false) Boolean addSourceAttribute) {
    if (label != null && label.trim().isEmpty()) {
      label = null;
    }

    String mappingJobExecutionHref =
        submitMappingJob(mappingProjectId, targetEntityTypeId, addSourceAttribute, packageId, label)
            .getBody();
    return format(
        "redirect:{0}", jobsController.createJobExecutionViewHref(mappingJobExecutionHref, 1000));
  }

  /**
   * Schedules a {@link MappingJobExecution}.
   *
   * @param mappingProjectId ID of the mapping project
   * @param targetEntityTypeId ID of the target entity to create or update
   * @param label label of the target entity to create
   * @param rawPackageId ID of the package to put the newly created entity in
   * @return the href of the created MappingJobExecution
   */
  @PostMapping(value = "/map", produces = TEXT_PLAIN_VALUE)
  public ResponseEntity<String> scheduleMappingJob(
      @RequestParam String mappingProjectId,
      @RequestParam String targetEntityTypeId,
      @RequestParam(required = false) String label,
      @RequestParam(required = false, name = "package") String rawPackageId,
      @RequestParam(required = false) Boolean addSourceAttribute)
      throws URISyntaxException {
    mappingProjectId = mappingProjectId.trim();
    targetEntityTypeId = targetEntityTypeId.trim();
    label = trim(label);
    String packageId = trim(rawPackageId);

    try {
      validateEntityName(targetEntityTypeId);
      if (mappingService.getMappingProject(mappingProjectId) == null) {
        throw new MolgenisDataException("No mapping project found with ID " + mappingProjectId);
      }
      if (packageId != null) {
        Package aPackage =
            dataService
                .getMeta()
                .getPackage(packageId)
                .orElseThrow(
                    () -> new MolgenisDataException("No package found with ID " + packageId));
        if (isSystemPackage(aPackage)) {
          throw new MolgenisDataException(format("Package [{0}] is a system package.", packageId));
        }
      }
    } catch (MolgenisDataException mde) {
      return ResponseEntity.badRequest().contentType(TEXT_PLAIN).body(mde.getMessage());
    }

    String jobHref =
        submitMappingJob(mappingProjectId, targetEntityTypeId, addSourceAttribute, packageId, label)
            .getBody();
    return created(new URI(jobHref)).contentType(TEXT_PLAIN).body(jobHref);
  }

  /**
   * Schedules a mappingJob for the current user.
   *
   * @param mappingProjectId ID for the mapping project to run
   * @param targetEntityTypeId ID for the integrated dataset
   * @param addSourceAttribute indication if a source attribute should be added to the target {@link
   *     EntityType}
   * @return the HREF for the scheduled {@link MappingJobExecution}
   */
  @PostMapping(value = "/submit", produces = TEXT_PLAIN_VALUE)
  public ResponseEntity<String> submitMappingJob(
      @RequestParam String mappingProjectId,
      @RequestParam String targetEntityTypeId,
      @RequestParam(required = false) Boolean addSourceAttribute,
      @RequestParam(required = false, name = "package") String packageId,
      @RequestParam(required = false) String label) {
    MappingJobExecution mappingJobExecution = mappingJobExecutionFactory.create();
    User currentUser = userAccountService.getCurrentUser();
    mappingJobExecution.setMappingProjectId(mappingProjectId);
    mappingJobExecution.setTargetEntityTypeId(targetEntityTypeId);
    mappingJobExecution.setAddSourceAttribute(addSourceAttribute);
    mappingJobExecution.setPackageId(packageId);
    mappingJobExecution.setLabel(label);

    jobExecutor.submit(mappingJobExecution);

    return ok(JobExecutionUriUtils.getUriPath(mappingJobExecution));
  }

  /**
   * Displays an {@link AttributeMapping}
   *
   * @param mappingProjectId ID of the {@link MappingProject}
   * @param target name of the target entity
   * @param source name of the source entity
   * @param targetAttribute name of the target attribute
   */
  @GetMapping("/attributeMapping")
  public String viewAttributeMapping(
      @RequestParam() String mappingProjectId,
      @RequestParam() String target,
      @RequestParam() String source,
      @RequestParam() String targetAttribute,
      Model model) {
    MappingProject project = mappingService.getMappingProject(mappingProjectId);
    MappingTarget mappingTarget = project.getMappingTarget(target);
    EntityMapping entityMapping = mappingTarget.getMappingForSource(source);
    AttributeMapping attributeMapping = entityMapping.getAttributeMapping(targetAttribute);

    if (attributeMapping == null) {
      attributeMapping = entityMapping.addAttributeMapping(targetAttribute);
    }

    if (attributeMapping.getTargetAttribute().hasRefEntity()) {
      EntityType refEntityType = attributeMapping.getTargetAttribute().getRefEntity();
      Iterable<Entity> refEntities = () -> dataService.findAll(refEntityType.getId()).iterator();
      model.addAttribute("categories", refEntities);
    }

    Multimap<Relation, OntologyTerm> tagsForAttribute =
        ontologyTagService.getTagsForAttribute(
            entityMapping.getTargetEntityType(), attributeMapping.getTargetAttribute());

    model.addAttribute("tags", tagsForAttribute.values());
    model.addAttribute(
        "dataExplorerUri", menuReaderService.findMenuItemPath(DataExplorerController.ID));
    model.addAttribute("mappingProject", project);
    model.addAttribute("entityMapping", entityMapping);
    model.addAttribute(
        "sourceAttributesSize",
        Iterables.size(entityMapping.getSourceEntityType().getAtomicAttributes()));
    model.addAttribute("attributeMapping", attributeMapping);
    model.addAttribute(
        "attributes", newArrayList(dataService.getEntityType(source).getAtomicAttributes()));

    return VIEW_ATTRIBUTE_MAPPING;
  }

  @PostMapping("/attributemappingfeedback")
  public String attributeMappingFeedback(
      @RequestParam() String mappingProjectId,
      @RequestParam() String target,
      @RequestParam() String source,
      @RequestParam() String targetAttribute,
      @RequestParam() String algorithm,
      Model model) {
    MappingProject project = mappingService.getMappingProject(mappingProjectId);

    MappingTarget mappingTarget = project.getMappingTarget(target);
    EntityMapping entityMapping = mappingTarget.getMappingForSource(source);

    AttributeMapping algorithmTest;

    if (entityMapping.getAttributeMapping(targetAttribute) == null) {
      algorithmTest = entityMapping.addAttributeMapping(targetAttribute);
      algorithmTest.setAlgorithm(algorithm);
    } else {
      algorithmTest = entityMapping.getAttributeMapping(targetAttribute);
      algorithmTest.setAlgorithm(algorithm);
    }

    try {
      Collection<String> sourceAttributeNames = algorithmService.getSourceAttributeNames(algorithm);
      if (!sourceAttributeNames.isEmpty()) {
        List<Attribute> sourceAttributes =
            sourceAttributeNames
                .stream()
                .map(
                    attributeName -> {
                      EntityType sourceEntityType = entityMapping.getSourceEntityType();
                      return sourceEntityType.getAttribute(attributeName);
                    })
                .filter(Objects::nonNull)
                .collect(toList());

        model.addAttribute("sourceAttributes", sourceAttributes);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    model.addAttribute("mappingProjectId", mappingProjectId);
    model.addAttribute("depth", project.getDepth());
    model.addAttribute("target", target);
    model.addAttribute("source", source);
    model.addAttribute(
        "targetAttribute", dataService.getEntityType(target).getAttribute(targetAttribute));

    Stream<Entity> sourceEntities = dataService.findAll(source).limit(10);

    List<AlgorithmResult> algorithmResults =
        sourceEntities
            .map(
                sourceEntity -> {
                  try {
                    return AlgorithmResult.createSuccess(
                        algorithmService.apply(
                            algorithmTest,
                            sourceEntity,
                            sourceEntity.getEntityType(),
                            project.getDepth()),
                        sourceEntity);
                  } catch (Exception e) {
                    return AlgorithmResult.createFailure(e, sourceEntity);
                  }
                })
            .collect(toList());
    model.addAttribute("feedbackRows", algorithmResults);

    long missing =
        algorithmResults.stream().filter(r -> r.isSuccess() && r.getValue() == null).count();
    long success = algorithmResults.stream().filter(AlgorithmResult::isSuccess).count() - missing;
    long error = algorithmResults.size() - success - missing;

    model.addAttribute("success", success);
    model.addAttribute("missing", missing);
    model.addAttribute("error", error);
    model.addAttribute(
        "dataexplorerUri", menuReaderService.findMenuItemPath(DataExplorerController.ID));
    return VIEW_ATTRIBUTE_MAPPING_FEEDBACK;
  }

  /**
   * Returns a view that allows the user to edit mappings involving xrefs / categoricals / strings
   */
  @PostMapping("/advancedmappingeditor")
  public String advancedMappingEditor(
      @RequestParam() String mappingProjectId,
      @RequestParam() String target,
      @RequestParam() String source,
      @RequestParam() String targetAttribute,
      @RequestParam() String sourceAttribute,
      @RequestParam String algorithm,
      Model model) {
    MappingProject project = mappingService.getMappingProject(mappingProjectId);
    MappingTarget mappingTarget = project.getMappingTarget(target);
    EntityMapping entityMapping = mappingTarget.getMappingForSource(source);
    AttributeMapping attributeMapping = entityMapping.getAttributeMapping(targetAttribute);

    model.addAttribute("mappingProject", project);
    model.addAttribute("entityMapping", entityMapping);
    model.addAttribute("attributeMapping", attributeMapping);

    // set variables for the target column in the mapping editor
    Attribute targetAttr = dataService.getEntityType(target).getAttribute(targetAttribute);

    Stream<Entity> targetAttributeEntities;
    String targetAttributeIdAttribute = null;
    String targetAttributeLabelAttribute = null;

    if (EntityTypeUtils.isReferenceType(targetAttr)) {
      targetAttributeEntities =
          dataService.findAll(
              dataService
                  .getEntityType(target)
                  .getAttribute(targetAttribute)
                  .getRefEntity()
                  .getId());

      targetAttributeIdAttribute =
          dataService
              .getEntityType(target)
              .getAttribute(targetAttribute)
              .getRefEntity()
              .getIdAttribute()
              .getName();

      targetAttributeLabelAttribute =
          dataService
              .getEntityType(target)
              .getAttribute(targetAttribute)
              .getRefEntity()
              .getLabelAttribute()
              .getName();
    } else {
      targetAttributeEntities = dataService.findAll(dataService.getEntityType(target).getId());
      targetAttributeIdAttribute = dataService.getEntityType(target).getIdAttribute().getName();
      targetAttributeLabelAttribute =
          dataService.getEntityType(target).getLabelAttribute().getName();
    }

    model.addAttribute(
        "targetAttributeEntities", (Iterable<Entity>) targetAttributeEntities::iterator);
    model.addAttribute("targetAttributeIdAttribute", targetAttributeIdAttribute);
    model.addAttribute("targetAttributeLabelAttribute", targetAttributeLabelAttribute);

    // set variables for the source column in the mapping editor
    Attribute sourceAttr = dataService.getEntityType(source).getAttribute(sourceAttribute);

    Stream<Entity> sourceAttributeEntities;
    String sourceAttributeIdAttribute = null;
    String sourceAttributeLabelAttribute = null;

    if (EntityTypeUtils.isReferenceType(sourceAttr)) {
      sourceAttributeEntities =
          dataService.findAll(
              dataService
                  .getEntityType(source)
                  .getAttribute(sourceAttribute)
                  .getRefEntity()
                  .getId());

      sourceAttributeIdAttribute =
          dataService
              .getEntityType(source)
              .getAttribute(sourceAttribute)
              .getRefEntity()
              .getIdAttribute()
              .getName();

      sourceAttributeLabelAttribute =
          dataService
              .getEntityType(source)
              .getAttribute(sourceAttribute)
              .getRefEntity()
              .getLabelAttribute()
              .getName();
    } else {
      sourceAttributeEntities = dataService.findAll(dataService.getEntityType(source).getId());
      sourceAttributeIdAttribute = dataService.getEntityType(source).getIdAttribute().getName();
      sourceAttributeLabelAttribute =
          dataService.getEntityType(source).getLabelAttribute().getName();
    }

    List<Entity> sourceAttributeEntityList = sourceAttributeEntities.collect(toList());

    model.addAttribute("sourceAttributeEntities", sourceAttributeEntityList);
    model.addAttribute("numberOfSourceAttributes", sourceAttributeEntityList.size());
    model.addAttribute("sourceAttributeIdAttribute", sourceAttributeIdAttribute);
    model.addAttribute("sourceAttributeLabelAttribute", sourceAttributeLabelAttribute);

    // Check if the selected source attribute is isAggregatable
    Attribute sourceAttributeAttribute =
        dataService.getEntityType(source).getAttribute(sourceAttribute);
    if (sourceAttributeAttribute.isAggregatable()) {
      AggregateResult aggregate =
          dataService.aggregate(
              source,
              new AggregateQueryImpl().attrX(sourceAttributeAttribute).query(new QueryImpl<>()));
      List<Long> aggregateCounts = new ArrayList<>();
      for (List<Long> count : aggregate.getMatrix()) {
        aggregateCounts.add(count.get(0));
      }
      model.addAttribute("aggregates", aggregateCounts);
    }

    model.addAttribute("target", target);
    model.addAttribute("source", source);
    model.addAttribute(
        "targetAttribute", dataService.getEntityType(target).getAttribute(targetAttribute));
    model.addAttribute(
        "sourceAttribute", dataService.getEntityType(source).getAttribute(sourceAttribute));

    CategoryMapping<String, String> categoryMapping = null;
    if (algorithm == null) {
      algorithm = attributeMapping.getAlgorithm();
    }
    try {
      categoryMapping = create(algorithm);
    } catch (Exception ignore) {
    }

    if (categoryMapping == null) {
      categoryMapping = createEmpty(sourceAttribute);
    }
    model.addAttribute("categoryMapping", categoryMapping);

    return VIEW_CATEGORY_MAPPING_EDITOR;
  }

  @PostMapping("/savecategorymapping")
  @ResponseBody
  public void saveCategoryMapping(
      @RequestParam() String mappingProjectId,
      @RequestParam() String target,
      @RequestParam() String source,
      @RequestParam() String targetAttribute,
      @RequestParam() String algorithm) {
    MappingProject mappingProject = mappingService.getMappingProject(mappingProjectId);
    MappingTarget mappingTarget = mappingProject.getMappingTarget(target);
    EntityMapping mappingForSource = mappingTarget.getMappingForSource(source);
    AttributeMapping attributeMapping = mappingForSource.getAttributeMapping(targetAttribute);
    if (attributeMapping == null) {
      attributeMapping = mappingForSource.addAttributeMapping(targetAttribute);
    }
    attributeMapping.setAlgorithm(algorithm);
    attributeMapping.setAlgorithmState(AlgorithmState.CURATED);
    mappingService.updateMappingProject(mappingProject);
  }

  /**
   * Tests an algoritm by computing it for all entities in the source repository.
   *
   * @param mappingServiceRequest the {@link MappingServiceRequest} sent by the client
   * @return Map with the results and size of the source
   */
  @PostMapping(
      value = "/mappingattribute/testscript",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE)
  @ResponseBody
  public Map<String, Object> testScript(@RequestBody MappingServiceRequest mappingServiceRequest) {
    EntityType targetEntityType =
        dataService.hasEntityType(mappingServiceRequest.getTargetEntityName())
            ? dataService.getEntityType(mappingServiceRequest.getTargetEntityName())
            : null;
    Attribute targetAttribute =
        targetEntityType != null
            ? targetEntityType.getAttribute(mappingServiceRequest.getTargetAttributeName())
            : null;
    Repository<Entity> sourceRepo =
        dataService.getRepository(mappingServiceRequest.getSourceEntityName());

    Iterable<AlgorithmEvaluation> algorithmEvaluations =
        algorithmService.applyAlgorithm(
            targetAttribute,
            mappingServiceRequest.getAlgorithm(),
            sourceRepo,
            mappingServiceRequest.getDepth());

    List<Object> calculatedValues =
        newArrayList(Iterables.transform(algorithmEvaluations, AlgorithmEvaluation::getValue));

    return ImmutableMap.of("results", calculatedValues, "totalCount", Iterables.size(sourceRepo));
  }

  /**
   * Generate algorithms based on semantic matches between attribute tags and descriptions
   *
   * <p>package-private for testablity
   */
  void autoGenerateAlgorithms(
      EntityMapping mapping,
      EntityType sourceEntityType,
      EntityType targetEntityType,
      MappingProject project) {
    algorithmService.autoGenerateAlgorithm(sourceEntityType, targetEntityType, mapping);
    mappingService.updateMappingProject(project);
  }

  /**
   * Lists the entities that may be added as new sources to this mapping project's selected target
   *
   * @param target the selected target
   */
  private List<EntityType> getNewSources(MappingTarget target) {
    return dataService
        .getEntityTypeIds()
        .filter(name -> isValidSource(target, name))
        .map(dataService::getEntityType)
        .collect(toList());
  }

  private static boolean isValidSource(MappingTarget target, String name) {
    return !target.hasMappingFor(name);
  }

  private List<EntityType> getEntityTypes() {
    return dataService.getEntityTypeIds().map(dataService::getEntityType).collect(toList());
  }

  private List<EntityType> getWritableEntityTypes() {
    return getEntityTypes()
        .stream()
        .filter(emd -> !emd.isAbstract())
        .filter(
            emd -> dataService.getCapabilities(emd.getId()).contains(RepositoryCapability.WRITABLE))
        .collect(toList());
  }

  private Map<String, List<OntologyTerm>> getTagsForAttribute(
      String target, MappingProject project) {
    Map<String, List<OntologyTerm>> attributeTagMap = new HashMap<>();
    for (Attribute amd : project.getMappingTarget(target).getTarget().getAtomicAttributes()) {
      EntityType targetMetaData =
          RunAsSystemAspect.runAsSystem(() -> dataService.getEntityType(target));
      attributeTagMap.put(
          amd.getName(),
          newArrayList(ontologyTagService.getTagsForAttribute(targetMetaData, amd).values()));
    }

    return attributeTagMap;
  }

  private String getMappingServiceMenuUrl() {
    return menuReaderService.findMenuItemPath(ID);
  }
}
