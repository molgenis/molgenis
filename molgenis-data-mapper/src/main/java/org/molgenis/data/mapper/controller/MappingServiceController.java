package org.molgenis.data.mapper.controller;

import static com.google.common.collect.Iterators.size;
import static org.molgenis.data.mapper.controller.MappingServiceController.URI;
import static org.molgenis.data.mapper.mapping.model.CategoryMapping.create;
import static org.molgenis.data.mapper.mapping.model.CategoryMapping.createEmpty;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.mapper.data.request.MappingServiceRequest;
import org.molgenis.data.mapper.mapping.model.AlgorithmResult;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.CategoryMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.mapping.model.MappingProject;
import org.molgenis.data.mapper.mapping.model.MappingTarget;
import org.molgenis.data.mapper.service.AlgorithmService;
import org.molgenis.data.mapper.service.MappingService;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.AggregateQueryImpl;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.util.ErrorMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Controller
@RequestMapping(URI)
public class MappingServiceController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(MappingServiceController.class);

	public static final String ID = "mappingservice";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String VIEW_MAPPING_PROJECTS = "view-mapping-projects";
	private static final String VIEW_ATTRIBUTE_MAPPING = "view-attribute-mapping";
	private static final String VIEW_SINGLE_MAPPING_PROJECT = "view-single-mapping-project";
	private static final String VIEW_CATEGORY_MAPPING_EDITOR = "view-advanced-mapping-editor";
	private static final String VIEW_ATTRIBUTE_MAPPING_FEEDBACK = "view-attribute-mapping-feedback";

	@Autowired
	private MolgenisUserService molgenisUserService;

	@Autowired
	private MappingService mappingService;

	@Autowired
	private AlgorithmService algorithmService;

	@Autowired
	private DataService dataService;

	@Autowired
	private OntologyTagService ontologyTagService;

	@Autowired
	private SemanticSearchService semanticSearchService;

	private ExecutorService executors;

	public MappingServiceController()
	{
		super(URI);
		executors = Executors.newSingleThreadExecutor();
	}

	/**
	 * Initializes the model with all mapping projects and all entities to the model.
	 * 
	 * @param model
	 *            the model to initialized
	 * @return view name of the mapping projects list
	 */
	@RequestMapping
	public String viewMappingProjects(Model model)
	{
		model.addAttribute("mappingProjects", mappingService.getAllMappingProjects());
		model.addAttribute("entityMetaDatas", getEntityMetaDatas());
		model.addAttribute("user", SecurityUtils.getCurrentUsername());
		model.addAttribute("admin", SecurityUtils.currentUserIsSu());
		return VIEW_MAPPING_PROJECTS;
	}

	/**
	 * Adds a new mapping project.
	 * 
	 * @param name
	 *            name of the mapping project
	 * @param targetEntity
	 *            name of the project's first {@link MappingTarget}'s target entity
	 * @return redirect URL for the newly created mapping project
	 */
	@RequestMapping(value = "/addMappingProject", method = RequestMethod.POST)
	public String addMappingProject(@RequestParam("mapping-project-name") String name,
			@RequestParam("target-entity") String targetEntity)
	{
		MappingProject newMappingProject = mappingService.addMappingProject(name, getCurrentUser(), targetEntity);
		return "redirect:/menu/main/mappingservice/mappingproject/" + newMappingProject.getIdentifier();
	}

	/**
	 * Removes a mapping project
	 * 
	 * @param mappingProjectId
	 *            the ID of the mapping project
	 * @return redirect url to the same page to force a refresh
	 */
	@RequestMapping(value = "/removeMappingProject", method = RequestMethod.POST)
	public String deleteMappingProject(@RequestParam(required = true) String mappingProjectId)
	{
		MappingProject project = mappingService.getMappingProject(mappingProjectId);
		if (hasWritePermission(project))
		{
			LOG.info("Deleting mappingProject " + project.getName());
			mappingService.deleteMappingProject(mappingProjectId);
		}
		return "redirect:/menu/main/mappingservice/";
	}

	/**
	 * Removes a attribute mapping
	 * 
	 * @param mappingProjectId
	 *            the ID of the mapping project
	 * @param target
	 *            the target entity
	 * @param source
	 *            the source entity
	 * @param attribute
	 *            the attribute that is mapped
	 * @return
	 */
	@RequestMapping(value = "/removeAttributeMapping", method = RequestMethod.POST)
	public String removeAttributeMapping(@RequestParam(required = true) String mappingProjectId,
			@RequestParam(required = true) String target, @RequestParam(required = true) String source,
			@RequestParam(required = true) String attribute)
	{
		MappingProject project = mappingService.getMappingProject(mappingProjectId);
		if (hasWritePermission(project))
		{
			project.getMappingTarget(target).getMappingForSource(source).deleteAttributeMapping(attribute);
			mappingService.updateMappingProject(project);
		}
		return "redirect:/menu/main/mappingservice/mappingproject/" + project.getIdentifier();
	}

	/**
	 * Adds a new {@link EntityMapping} to an existing {@link MappingTarget}
	 * 
	 * @param target
	 *            the name of the {@link MappingTarget}'s entity to add a source entity to
	 * @param source
	 *            the name of the source entity of the newly added {@link EntityMapping}
	 * @param mappingProjectId
	 *            the ID of the {@link MappingTarget}'s {@link MappingProject}
	 * @return redirect URL for the mapping project
	 */
	@RequestMapping(value = "/addEntityMapping", method = RequestMethod.POST)
	public String addEntityMapping(@RequestParam String mappingProjectId, String target, String source)
	{
		EntityMetaData sourceEntityMetaData = dataService.getEntityMetaData(source);
		EntityMetaData targetEntityMetaData = dataService.getEntityMetaData(target);

		Iterable<AttributeMetaData> attributes = targetEntityMetaData.getAtomicAttributes();

		MappingProject project = mappingService.getMappingProject(mappingProjectId);

		if (hasWritePermission(project))
		{
			EntityMapping mapping = project.getMappingTarget(target).addSource(sourceEntityMetaData);
			mappingService.updateMappingProject(project);
			executors.execute(() -> autoGenerateAlgorithms(mapping, target, sourceEntityMetaData, targetEntityMetaData,
					attributes, project));
		}

		return "redirect:/menu/main/mappingservice/mappingproject/" + mappingProjectId;
	}

	/**
	 * Removes entity mapping
	 * 
	 * @param mappingProjectId
	 *            ID of the mapping project to remove entity mapping from
	 * @param target
	 *            entity name of the mapping target
	 * @param source
	 *            entity name of the mapping source
	 * @return redirect url of the mapping project's page
	 */
	@RequestMapping(value = "/removeEntityMapping", method = RequestMethod.POST)
	public String removeEntityMapping(@RequestParam String mappingProjectId, String target, String source)
	{
		MappingProject project = mappingService.getMappingProject(mappingProjectId);
		if (hasWritePermission(project))
		{
			project.getMappingTarget(target).removeSource(source);
			mappingService.updateMappingProject(project);
		}
		return "redirect:/menu/main/mappingservice/mappingproject/" + mappingProjectId;
	}

	/**
	 * Adds a new {@link AttributeMapping} to an {@link EntityMapping}.
	 * 
	 * @param mappingProjectId
	 *            ID of the mapping project
	 * @param target
	 *            name of the target entity
	 * @param source
	 *            name of the source entity
	 * @param targetAttribute
	 *            name of the target attribute
	 * @param algorithm
	 *            the mapping algorithm
	 * @return redirect URL for the attributemapping
	 */
	@RequestMapping(value = "/saveattributemapping", method = RequestMethod.POST)
	public String saveAttributeMapping(@RequestParam(required = true) String mappingProjectId,
			@RequestParam(required = true) String target, @RequestParam(required = true) String source,
			@RequestParam(required = true) String targetAttribute, @RequestParam(required = true) String algorithm)
	{
		MappingProject mappingProject = mappingService.getMappingProject(mappingProjectId);
		if (hasWritePermission(mappingProject))
		{
			MappingTarget mappingTarget = mappingProject.getMappingTarget(target);
			EntityMapping mappingForSource = mappingTarget.getMappingForSource(source);
			if (algorithm.isEmpty())
			{
				mappingForSource.deleteAttributeMapping(targetAttribute);
			}
			else
			{
				AttributeMapping attributeMapping = mappingForSource.getAttributeMapping(targetAttribute);
				if (attributeMapping == null)
				{
					attributeMapping = mappingForSource.addAttributeMapping(targetAttribute);
				}
				attributeMapping.setAlgorithm(algorithm);
			}
			mappingService.updateMappingProject(mappingProject);
		}
		return "redirect:/menu/main/mappingservice/mappingproject/" + mappingProject.getIdentifier();
	}

	/**
	 * Displays a mapping project.
	 * 
	 * @param identifier
	 *            identifier of the {@link MappingProject}
	 * @param target
	 *            Name of the selected {@link MappingTarget}'s target entity
	 * @param model
	 *            the model
	 * @return View name of the
	 */
	@RequestMapping("/mappingproject/{id}")
	public String viewMappingProject(@PathVariable("id") String identifier,
			@RequestParam(value = "target", required = false) String target, Model model)
	{
		MappingProject project = mappingService.getMappingProject(identifier);
		if (target == null)
		{
			target = project.getMappingTargets().get(0).getName();
		}

		model.addAttribute("selectedTarget", target);
		model.addAttribute("mappingProject", project);
		model.addAttribute("entityMetaDatas", getNewSources(project.getMappingTarget(target)));
		model.addAttribute("hasWritePermission", hasWritePermission(project, false));
		model.addAttribute("attributeTagMap", getTagsForAttribute(target, project));

		return VIEW_SINGLE_MAPPING_PROJECT;
	}

	/**
	 * Creates the integrated entity for a mapping project's target
	 * 
	 * @param mappingProjectId
	 *            ID of the mapping project
	 * @param target
	 *            name of the target of the {@link EntityMapping}
	 * @param newEntityName
	 *            name of the new entity to create
	 * @return redirect URL to the data explorer displaying the newly generated entity
	 */
	@RequestMapping("/createIntegratedEntity")
	public String createIntegratedEntity(@RequestParam String mappingProjectId, @RequestParam String target,
			@RequestParam() String newEntityName, Model model)
	{
		try
		{
			MappingTarget mappingTarget = mappingService.getMappingProject(mappingProjectId).getMappingTarget(target);
			String name = mappingService.applyMappings(mappingTarget, newEntityName);
			return "redirect:/menu/main/dataexplorer?entity=" + name;
		}
		catch (RuntimeException ex)
		{
			model.addAttribute("heading", "Failed to create integrated entity.");
			model.addAttribute("message", ex.getMessage());
			model.addAttribute("href", "/menu/main/mappingservice/mappingproject/" + mappingProjectId);
			return "error-msg";
		}
	}

	/**
	 * Displays an {@link AttributeMapping}
	 * 
	 * @param mappingProjectId
	 *            ID of the {@link MappingProject}
	 * @param target
	 *            name of the target entity
	 * @param source
	 *            name of the source entity
	 * @param targetAttribute
	 *            name of the target attribute
	 */
	@RequestMapping("/attributeMapping")
	public String viewAttributeMapping(@RequestParam(required = true) String mappingProjectId,
			@RequestParam(required = true) String target, @RequestParam(required = true) String source,
			@RequestParam(required = true) String targetAttribute,
			@RequestParam(required = true) boolean showSuggestedAttributes, Model model)
	{
		MappingProject project = mappingService.getMappingProject(mappingProjectId);
		MappingTarget mappingTarget = project.getMappingTarget(target);
		EntityMapping entityMapping = mappingTarget.getMappingForSource(source);
		AttributeMapping attributeMapping = entityMapping.getAttributeMapping(targetAttribute);

		if (attributeMapping == null)
		{
			attributeMapping = entityMapping.addAttributeMapping(request.getTargetAttribute());
		}

		model.addAttribute("mappingProject", project);
		model.addAttribute("entityMapping", entityMapping);
		model.addAttribute("attributeMapping", attributeMapping);
		model.addAttribute("attributes",
				Lists.newArrayList(dataService.getEntityMetaData(request.getSource()).getAtomicAttributes()));
		model.addAttribute("hasWritePermission", hasWritePermission(project, false));

		return VIEW_ATTRIBUTE_MAPPING;
	}

	@RequestMapping(value = "/attributemappingfeedback", method = RequestMethod.POST)
	public String attributeMappingFeedback(@RequestParam(required = true) String mappingProjectId,
			@RequestParam(required = true) String target, @RequestParam(required = true) String source,
			@RequestParam(required = true) String targetAttribute, @RequestParam(required = true) String algorithm,
			Model model)
	{
		MappingProject project = mappingService.getMappingProject(mappingProjectId);

		MappingTarget mappingTarget = project.getMappingTarget(target);
		EntityMapping entityMapping = mappingTarget.getMappingForSource(source);

		AttributeMapping algorithmTest;

		if (entityMapping.getAttributeMapping(targetAttribute) == null)
		{
			algorithmTest = entityMapping.addAttributeMapping(targetAttribute);
			algorithmTest.setAlgorithm(algorithm);
		}
		else
		{
			algorithmTest = entityMapping.getAttributeMapping(targetAttribute);
			algorithmTest.setAlgorithm(algorithm);
		}

		try
		{
			Collection<String> sourceAttributeNames = algorithmService.getSourceAttributeNames(algorithm);
			if (!sourceAttributeNames.isEmpty())
			{
				model.addAttribute("sourceAttributeNames", sourceAttributeNames);
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		model.addAttribute("mappingProjectId", mappingProjectId);
		model.addAttribute("target", target);
		model.addAttribute("source", source);
		model.addAttribute("targetAttribute", dataService.getEntityMetaData(target).getAttribute(targetAttribute));

		FluentIterable<Entity> sourceEntities = FluentIterable.from(dataService.findAll(source)).limit(10);
		ImmutableList<AlgorithmResult> algorithmResults = sourceEntities.transform(
				sourceEntity -> {
					try
					{
						return AlgorithmResult.createSuccess(
								algorithmService.apply(algorithmTest, sourceEntity, sourceEntity.getEntityMetaData()),
								sourceEntity);
					}
					catch (Exception e)
					{
						return AlgorithmResult.createFailure(e, sourceEntity);
					}
				}).toList();
		model.addAttribute("feedbackRows", algorithmResults);

		long missing = algorithmResults.stream().filter(r -> r.isSuccess() && r.getValue() == null).count();
		long success = algorithmResults.stream().filter(AlgorithmResult::isSuccess).count() - missing;
		long error = algorithmResults.size() - success - missing;

		model.addAttribute("success", success);
		model.addAttribute("missing", missing);
		model.addAttribute("error", error);

		return VIEW_ATTRIBUTE_MAPPING_FEEDBACK;
	}

	/**
	 * Returns a view that allows the user to edit mappings involving xrefs / categoricals / strings
	 * 
	 * @param mappingProjectId
	 * @param target
	 * @param source
	 * @param targetAttribute
	 * @param sourceAttribute
	 * @param model
	 */
	@RequestMapping(value = "/advancedmappingeditor", method = RequestMethod.POST)
	public String advancedMappingEditor(@RequestParam(required = true) String mappingProjectId,
			@RequestParam(required = true) String target, @RequestParam(required = true) String source,
			@RequestParam(required = true) String targetAttribute,
			@RequestParam(required = true) String sourceAttribute, @RequestParam String algorithm, Model model)
	{
		MappingProject project = mappingService.getMappingProject(mappingProjectId);
		MappingTarget mappingTarget = project.getMappingTarget(target);
		EntityMapping entityMapping = mappingTarget.getMappingForSource(source);
		AttributeMapping attributeMapping = entityMapping.getAttributeMapping(targetAttribute);

		model.addAttribute("mappingProject", project);
		model.addAttribute("entityMapping", entityMapping);
		model.addAttribute("attributeMapping", attributeMapping);

		FieldType sourceAttributeDataType = dataService.getEntityMetaData(source).getAttribute(sourceAttribute)
				.getDataType();

		// values for target
		Iterable<Entity> targetAttributeEntities = dataService.findAll(dataService.getEntityMetaData(target)
				.getAttribute(targetAttribute).getRefEntity().getName());
		model.addAttribute("targetAttributeEntities", targetAttributeEntities);

		// ID attribute for the target ref entity
		String targetAttributeIdAttribute = dataService.getEntityMetaData(target).getAttribute(targetAttribute)
				.getRefEntity().getIdAttribute().getName();
		model.addAttribute("targetAttributeIdAttribute", targetAttributeIdAttribute);

		// Label attribute for the target ref entity
		String targetAttributeLabelAttribute = dataService.getEntityMetaData(target).getAttribute(targetAttribute)
				.getRefEntity().getLabelAttribute().getName();
		model.addAttribute("targetAttributeLabelAttribute", targetAttributeLabelAttribute);

		Iterable<Entity> sourceAttributeEntities = null;
		String sourceAttributeIdAttribute = null;
		String sourceAttributeLabelAttribute = null;

		if (sourceAttributeDataType instanceof XrefField || sourceAttributeDataType instanceof MrefField)
		{
			sourceAttributeEntities = dataService.findAll(dataService.getEntityMetaData(source)
					.getAttribute(sourceAttribute).getRefEntity().getName());

			sourceAttributeIdAttribute = dataService.getEntityMetaData(source).getAttribute(sourceAttribute)
					.getRefEntity().getIdAttribute().getName();

			sourceAttributeLabelAttribute = dataService.getEntityMetaData(source).getAttribute(sourceAttribute)
					.getRefEntity().getLabelAttribute().getName();
		}
		else
		{
			sourceAttributeEntities = dataService.findAll(dataService.getEntityMetaData(source).getName());
			sourceAttributeIdAttribute = dataService.getEntityMetaData(source).getIdAttribute().getName();
			sourceAttributeLabelAttribute = sourceAttribute;
		}

		model.addAttribute("sourceAttributeEntities", sourceAttributeEntities);
		model.addAttribute("numberOfSourceAttributes", size(sourceAttributeEntities.iterator()));
		model.addAttribute("sourceAttributeIdAttribute", sourceAttributeIdAttribute);
		model.addAttribute("sourceAttributeLabelAttribute", sourceAttributeLabelAttribute);

		// Check if the selected source attribute is aggregateable
		AttributeMetaData sourceAttributeAttributeMetaData = dataService.getEntityMetaData(source).getAttribute(
				sourceAttribute);
		if (sourceAttributeAttributeMetaData.isAggregateable())
		{
			AggregateResult aggregate = dataService.aggregate(source,
					new AggregateQueryImpl().attrX(sourceAttributeAttributeMetaData).query(new QueryImpl()));
			List<Long> aggregateCounts = new ArrayList<Long>();
			for (List<Long> count : aggregate.getMatrix())
			{
				aggregateCounts.add(count.get(0));
			}
			model.addAttribute("aggregates", aggregateCounts);
		}

		model.addAttribute("target", target);
		model.addAttribute("source", source);
		model.addAttribute("targetAttribute", dataService.getEntityMetaData(target).getAttribute(targetAttribute));
		model.addAttribute("sourceAttribute", dataService.getEntityMetaData(source).getAttribute(sourceAttribute));
		model.addAttribute("hasWritePermission", hasWritePermission(project, false));

		CategoryMapping<String, String> categoryMapping = null;
		if (algorithm == null)
		{
			algorithm = attributeMapping.getAlgorithm();
		}
		try
		{
			categoryMapping = create(algorithm);
		}
		catch (Exception ignore)
		{
		}

		if (categoryMapping == null)
		{
			categoryMapping = createEmpty(sourceAttribute);
		}
		model.addAttribute("categoryMapping", categoryMapping);

		return VIEW_CATEGORY_MAPPING_EDITOR;
	}

	@RequestMapping(value = "/savecategorymapping", method = RequestMethod.POST)
	public @ResponseBody void saveCategoryMapping(@RequestParam(required = true) String mappingProjectId,
			@RequestParam(required = true) String target, @RequestParam(required = true) String source,
			@RequestParam(required = true) String targetAttribute, @RequestParam(required = true) String algorithm)
	{
		MappingProject mappingProject = mappingService.getMappingProject(mappingProjectId);
		if (hasWritePermission(mappingProject))
		{
			MappingTarget mappingTarget = mappingProject.getMappingTarget(target);
			EntityMapping mappingForSource = mappingTarget.getMappingForSource(source);
			AttributeMapping attributeMapping = mappingForSource.getAttributeMapping(targetAttribute);
			if (attributeMapping == null)
			{
				attributeMapping = mappingForSource.addAttributeMapping(targetAttribute);
			}
			attributeMapping.setAlgorithm(algorithm);
			mappingService.updateMappingProject(mappingProject);
		}
	}

	/**
	 * Tests an algoritm by computing it for all entities in the source repository.
	 * 
	 * @param mappingServiceRequest
	 *            the {@link MappingServiceRequest} sent by the client
	 * @return Map with the results and size of the source
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/mappingattribute/testscript", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, Object> testScript(@RequestBody MappingServiceRequest mappingServiceRequest)
	{
		EntityMetaData targetEntityMetaData = dataService
				.getEntityMetaData(mappingServiceRequest.getTargetEntityName());
		AttributeMetaData targetAttribute = targetEntityMetaData != null ? targetEntityMetaData
				.getAttribute(mappingServiceRequest.getTargetAttributeName()) : null;
		Repository sourceRepo = dataService.getRepository(mappingServiceRequest.getSourceEntityName());
		List<Object> calculatedValues = algorithmService.applyAlgorithm(targetAttribute,
				mappingServiceRequest.getAlgorithm(), sourceRepo);
		return ImmutableMap.<String, Object> of("results", calculatedValues, "totalCount", Iterables.size(sourceRepo));
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		LOG.error(e.getMessage(), e);
		return new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage(
				"An error occurred. Please contact the administrator.<br />Message:" + e.getMessage()));
	}

	/**
	 * Generate algorithms based on semantic matches between attribute tags and descriptions
	 * 
	 * @param mapping
	 * @param target
	 * @param sourceEntityMetaData
	 * @param targetEntityMetaData
	 * @param attributes
	 * @param project
	 */
	private void autoGenerateAlgorithms(EntityMapping mapping, String target, EntityMetaData sourceEntityMetaData,
			EntityMetaData targetEntityMetaData, Iterable<AttributeMetaData> attributes, MappingProject project)
	{
		attributes.forEach(attribute -> algorithmService.autoGenerateAlgorithm(sourceEntityMetaData,
				targetEntityMetaData, mapping, attribute));
		mappingService.updateMappingProject(project);
	}

	/**
	 * Lists the entities that may be added as new sources to this mapping project's selected target
	 * 
	 * @param target
	 *            the selected target
	 * @return
	 */
	private List<EntityMetaData> getNewSources(MappingTarget target)
	{
		return StreamSupport.stream(dataService.getEntityNames().spliterator(), false)
				.filter((name) -> isValidSource(target, name)).map(dataService::getEntityMetaData)
				.collect(Collectors.toList());
	}

	private static boolean isValidSource(MappingTarget target, String name)
	{
		return !target.hasMappingFor(name);
	}

	private List<EntityMetaData> getEntityMetaDatas()
	{
		return Lists.newArrayList(Iterables.transform(dataService.getEntityNames(), dataService::getEntityMetaData));
	}

	private boolean hasWritePermission(MappingProject project)
	{
		return hasWritePermission(project, true);
	}

	private boolean hasWritePermission(MappingProject project, boolean logInfractions)
	{
		boolean result = SecurityUtils.currentUserIsSu()
				|| project.getOwner().getUsername().equals(SecurityUtils.getCurrentUsername());
		if (logInfractions && !result)
		{
			LOG.warn("User " + SecurityUtils.getCurrentUsername()
					+ " illegally tried to modify mapping project with id " + project.getIdentifier() + " owned by "
					+ project.getOwner().getUsername());
		}
		return result;
	}

	private MolgenisUser getCurrentUser()
	{
		return molgenisUserService.getUser(SecurityUtils.getCurrentUsername());
	}

	private Map<String, List<OntologyTerm>> getTagsForAttribute(String target, MappingProject project)
	{
		Map<String, List<OntologyTerm>> attributeTagMap = new HashMap<String, List<OntologyTerm>>();
		for (AttributeMetaData amd : project.getMappingTarget(target).getTarget().getAtomicAttributes())
		{
			List<OntologyTerm> ontologyTermsForAttribute = new ArrayList<OntologyTerm>(ontologyTagService
					.getTagsForAttribute(dataService.getEntityMetaData(target), amd).values());

			attributeTagMap.put(amd.getName(), ontologyTermsForAttribute);
		}

		return attributeTagMap;
	}
}
