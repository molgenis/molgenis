package org.molgenis.data.mapper;

import static org.molgenis.data.mapper.MappingServiceController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.algorithm.AlgorithmService;
import org.molgenis.data.mapping.MappingService;
import org.molgenis.data.mapping.model.AttributeMapping;
import org.molgenis.data.mapping.model.EntityMapping;
import org.molgenis.data.mapping.model.MappingProject;
import org.molgenis.data.mapping.model.MappingTarget;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.util.ErrorMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
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

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

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

	@Autowired
	private MolgenisUserService molgenisUserService;

	@Autowired
	private MappingService mappingService;

	@Autowired
	private AlgorithmService algorithmService;

	@Autowired
	private DataService dataService;

	@Autowired
	private PermissionSystemService permissionSystemService;

	public MappingServiceController()
	{
		super(URI);
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
		// FIXME need to write complete URL else it will use /plugin as root and the molgenis header and footer wont be
		// loaded
		return "redirect:/menu/main/mappingservice/mappingproject/" + newMappingProject.getIdentifier();
	}

	@RequestMapping(value = "/removeMappingProject", method = RequestMethod.POST)
	public String deleteMappingProject(@RequestParam(required = true) String mappingProjectId)
	{
		MappingProject project = mappingService.getMappingProject(mappingProjectId);
		if (mayChange(project))
		{
			LOG.info("Deleting mappingProject " + project.getName());
			mappingService.deleteMappingProject(mappingProjectId);
		}
		return "redirect:/menu/main/mappingservice/";
	}

	private boolean mayChange(MappingProject project)
	{
		return mayChange(project, true);
	}

	private boolean mayChange(MappingProject project, boolean logInfractions)
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
		MappingProject project = mappingService.getMappingProject(mappingProjectId);
		if (mayChange(project))
		{
			project.getMappingTarget(target).addSource(dataService.getEntityMetaData(source));
			mappingService.updateMappingProject(project);
		}
		return "redirect:/menu/main/mappingservice/mappingproject/" + mappingProjectId;
	}

	@RequestMapping(value = "/removeEntityMapping", method = RequestMethod.POST)
	public String removeEntityMapping(@RequestParam String mappingProjectId, String target, String source)
	{
		MappingProject project = mappingService.getMappingProject(mappingProjectId);
		if (mayChange(project))
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
	 * @param sourceAttribute
	 *            name of the source attribute
	 * @return redirect URL for the attributemapping
	 */
	@RequestMapping(value = "/saveattributemapping", method = RequestMethod.POST)
	public String saveAttributeMapping(@RequestParam(required = true) String mappingProjectId,
			@RequestParam(required = true) String target, @RequestParam(required = true) String source,
			@RequestParam(required = true) String targetAttribute,
			@RequestParam(required = true) String sourceAttribute, @RequestParam(required = true) String algorithm)
	{
		MappingProject mappingProject = mappingService.getMappingProject(mappingProjectId);
		if (mayChange(mappingProject))
		{
			MappingTarget mappingTarget = mappingProject.getMappingTarget(target);
			EntityMapping mappingForSource = mappingTarget.getMappingForSource(source);
			AttributeMapping attributeMapping = mappingForSource.getAttributeMapping(targetAttribute);
			if (attributeMapping == null)
			{
				attributeMapping = mappingForSource.addAttributeMapping(targetAttribute);
			}
			EntityMetaData sourceEmd = dataService.getEntityMetaData(source);
			attributeMapping.setSource(sourceEmd.getAttribute(sourceAttribute));
			attributeMapping.setAlgorithm(algorithm);
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
		// Fill the model
		model.addAttribute("selectedTarget", target);
		model.addAttribute("mappingProject", project);
		model.addAttribute("entityMetaDatas", getNewSources(project.getMappingTarget(target)));
		model.addAttribute("mayChange", mayChange(project, false));

		return VIEW_SINGLE_MAPPING_PROJECT;
	}

	@RequestMapping("/createintegratedentity")
	public String createIntegratedEntity(@RequestParam String mappingProjectId, @RequestParam String target,
			@RequestParam() String newEntityName)
	{
		MappingTarget mappingTarget = mappingService.getMappingProject(mappingProjectId).getMappingTarget(target);
		String name = mappingService.applyMappings(mappingTarget, newEntityName);
		permissionSystemService.giveUserEntityAndMenuPermissions(SecurityContextHolder.getContext(),
				Collections.singletonList(name));
		return "redirect:/menu/main/dataexplorer?entity=" + name;
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

	/**
	 * Displays an {@link AttributeMapping}
	 * 
	 * @param mappingProjectId
	 *            ID of the {@link MappingProject}
	 * @param target
	 *            name of the target entity
	 * @param source
	 *            name of the source entity
	 * @param attribute
	 *            name of the target attribute
	 * @param model
	 *            the model
	 * @return name of the attributemapping view
	 */
	@RequestMapping("/attributeMapping")
	public String viewAttributeMapping(@RequestParam String mappingProjectId, @RequestParam String target,
			@RequestParam String source, @RequestParam String attribute, Model model)
	{
		MappingProject project = mappingService.getMappingProject(mappingProjectId);
		MappingTarget mappingTarget = project.getMappingTarget(target);
		EntityMapping entityMapping = mappingTarget.getMappingForSource(source);
		AttributeMapping attributeMapping = entityMapping.getAttributeMapping(attribute);
		if (attributeMapping == null)
		{
			attributeMapping = entityMapping.addAttributeMapping(attribute);
		}
		model.addAttribute("mappingProject", project);
		model.addAttribute("entityMapping", entityMapping);
		model.addAttribute("attributeMapping", attributeMapping);
		model.addAttribute("mayChange", mayChange(project, false));
		return VIEW_ATTRIBUTE_MAPPING;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/mappingattribute/testscript", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, Object> testScript(@RequestBody MappingServiceRequest mappingServiceRequest)
	{
		Map<String, Object> results = new HashMap<String, Object>();

		EntityMetaData targetEntityMetaData = dataService
				.getEntityMetaData(mappingServiceRequest.getTargetEntityName());

		EntityMetaData sourceEntityMetaData = dataService
				.getEntityMetaData(mappingServiceRequest.getSourceEntityName());

		AttributeMetaData targetAttribute = targetEntityMetaData != null ? targetEntityMetaData
				.getAttribute(mappingServiceRequest.getTargetAttributeName()) : null;

		Iterable<AttributeMetaData> sourceAttributes = extractFeatureIdentifiersAsAttributeMetaData(
				mappingServiceRequest, sourceEntityMetaData);

		Repository sourceRepo = dataService.getRepositoryByEntityName(sourceEntityMetaData.getName());
		List<Object> calculatedValues = algorithmService.applyAlgorithm(targetAttribute, sourceAttributes,
				mappingServiceRequest.getAlgorithm(), sourceRepo);

		results.put("results", calculatedValues);
		results.put("totalCount", Iterables.size(sourceRepo));

		return results;
	}

	private Iterable<AttributeMetaData> extractFeatureIdentifiersAsAttributeMetaData(
			MappingServiceRequest mappingServiceRequest, EntityMetaData sourceEntityMetaData)
	{
		return Iterables.filter(Iterables.transform(
				algorithmService.getSourceAttributeNames(mappingServiceRequest.getAlgorithm()),
				new Function<String, AttributeMetaData>()
				{
					@Override
					public AttributeMetaData apply(final String attributeName)
					{
						return sourceEntityMetaData != null && sourceEntityMetaData.getAttribute(attributeName) != null ? sourceEntityMetaData
								.getAttribute(attributeName) : null;
					}
				}), Predicates.notNull());
	}

	private List<EntityMetaData> getEntityMetaDatas()
	{
		return Lists.newArrayList(Iterables.transform(dataService.getEntityNames(), dataService::getEntityMetaData));
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

	private MolgenisUser getCurrentUser()
	{
		return molgenisUserService.getUser(SecurityUtils.getCurrentUsername());
	}
}
