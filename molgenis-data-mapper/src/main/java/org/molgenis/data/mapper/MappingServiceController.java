package org.molgenis.data.mapper;

import static org.molgenis.data.mapper.MappingServiceController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.algorithm.AlgorithmService;
import org.molgenis.data.algorithm.AlgorithmServiceImpl;
import org.molgenis.data.mapping.MappingService;
import org.molgenis.data.mapping.model.EntityMapping;
import org.molgenis.data.mapping.model.MappingProject;
import org.molgenis.framework.ui.MolgenisPluginController;
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

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

@Controller
@RequestMapping(URI)
public class MappingServiceController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(MappingServiceController.class);

	public static final String ID = "mappingservice";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String VIEW_MAPPING_PROJECTS = "view-mapping-projects";
	private static final String VIEW_ATTRIBUTE_MAPPING = "view-single-mapping-project";
	private static final String VIEW_EDIT_ATTRIBUTE_MAPPING = "attribute-mapping-editor";

	@Autowired
	private MolgenisUserService molgenisUserService;

	@Autowired
	private MappingService mappingService;

	@Autowired
	private AlgorithmService algorithmService;

	@Autowired
	private DataService dataService;

	public MappingServiceController()
	{
		super(URI);
	}

	@RequestMapping
	public String init(Model model)
	{
		model = setModelAttributes(model);
		return VIEW_MAPPING_PROJECTS;
	}

	@RequestMapping(value = "/addmappingproject", method = RequestMethod.POST)
	public String addMappingProject(@RequestParam("mapping-project-name") String name,
			@RequestParam("target-entity") String targetEntity)
	{
		MappingProject newMappingProject = mappingService.addMappingProject(name, getCurrentUser(), targetEntity);
		// FIXME need to write complete URL else it will use /plugin as root and the molgenis header and footer wont be
		// loaded
		return "redirect:/menu/main/mappingservice/mappingproject/" + newMappingProject.getIdentifier();
	}

	@RequestMapping(value = "/editmappingproject", method = RequestMethod.POST)
	public String editMappingProject(@RequestParam("mapping-project-name") String identifier, Model model)
	{
		MappingProject mappingProject = mappingService.getMappingProject(identifier);
		mappingService.updateMappingProject(mappingProject);

		model = setModelAttributes(model);

		return VIEW_MAPPING_PROJECTS;
	}
	
	@RequestMapping(value = "/addentitymapping", method = RequestMethod.POST)
	public String addEntityMapping(@RequestParam("target") String target, @RequestParam("source") String source,
			@RequestParam("mappingProjectId") String mappingProjectIdentifier)
	{
		MappingProject project = mappingService.getMappingProject(mappingProjectIdentifier);
		project.getTargets().get(target).addSource(dataService.getEntityMetaData(source));
		mappingService.updateMappingProject(project);
		
		return "redirect:/menu/main/mappingservice/mappingproject/" + mappingProjectIdentifier;
	}

	@RequestMapping("/mappingproject/{id}")
	public String getMappingProjectScreen(@PathVariable("id") String identifier, Model model)
	{
		MappingProject selectedMappingProject = mappingService.getMappingProject(identifier);
		Map<String, Iterable<AttributeMetaData>> targetAttributes = new HashMap<String, Iterable<AttributeMetaData>>();
		for (String target : selectedMappingProject.getTargets().keySet())
		{
			// Get the entity metadata for every target
			targetAttributes.put(target, selectedMappingProject.getTargets().get(target).getTarget().getAttributes());
		}
		String selectedTarget = targetAttributes.keySet().toArray()[0].toString();

		// FIXME entityMapping might not be the correct term for a map with sources as keys
		Map<String, EntityMapping> entityMappings = selectedMappingProject.getTargets().get(selectedTarget)
				.getEntityMappings();

		// Fill the model
		model.addAttribute("selectedTarget", selectedTarget);
		model.addAttribute("selectedTargetAttributes", targetAttributes.get(selectedTarget));
		model.addAttribute("targetAttributes", targetAttributes);
		model.addAttribute("mappingProject", selectedMappingProject);
		model.addAttribute("entityMetaDatas", getEntityMetaDatas());
		model.addAttribute("entityMappings", entityMappings);

		return VIEW_ATTRIBUTE_MAPPING;
	}

	@RequestMapping(value = "/get-new-attributes", method = RequestMethod.POST)
	public @ResponseBody Iterable<AttributeMetaData> getAttributesForNewEntity(@RequestBody String newEntityName)
	{
		return dataService.getEntityMetaData(newEntityName).getAttributes();
	}

	@RequestMapping("/editattributemapping")
	public String getMappingAttributeScreen(@RequestBody MappingServiceRequest mappingServiceRequest, Model model)
	{
		// add to model: Target Entity, targetAttribute, Source Entity, source attributes (all?)
		EntityMetaData targetEntityMetaData = dataService
				.getEntityMetaData(mappingServiceRequest.getTargetEntityName());

		EntityMetaData sourceEntityMetaData = dataService
				.getEntityMetaData(mappingServiceRequest.getSourceEntityName());

		AttributeMetaData targetAttribute = targetEntityMetaData != null ? targetEntityMetaData
				.getAttribute(mappingServiceRequest.getTargetAttributeName()) : null;

		// TODO : biobankconnect algorithm will be placed here!
		Iterable<AttributeMetaData> sourceAttributes = sourceEntityMetaData != null ? sourceEntityMetaData
				.getAtomicAttributes() : Collections.emptyList();

		model.addAllAttributes(ImmutableMap.of("targetEntityMetaData", targetEntityMetaData, "sourceEntityMetaData",
				sourceEntityMetaData, "targetAttribute", targetAttribute, "sourceAttributes", sourceAttributes));

		return VIEW_EDIT_ATTRIBUTE_MAPPING;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/mappingattribute/testscript", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, Object> testScrpit(@RequestBody MappingServiceRequest mappingServiceRequest)
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
				AlgorithmServiceImpl.extractFeatureName(mappingServiceRequest.getAlgorithm()),
				new Function<String, AttributeMetaData>()
				{
					public AttributeMetaData apply(final String attributeName)
					{
						return sourceEntityMetaData != null && sourceEntityMetaData.getAttribute(attributeName) != null ? sourceEntityMetaData
								.getAttribute(attributeName) : null;
					}
				}), Predicates.notNull());
	}

	private Model setModelAttributes(Model model)
	{
		if (mappingService != null) model.addAttribute("mappingProjects", mappingService.getAllMappingProjects());
		model.addAttribute("entityMetaDatas", getEntityMetaDatas());

		return model;
	}

	private Iterable<EntityMetaData> getEntityMetaDatas()
	{
		return Iterables.transform(dataService.getEntityNames(), new Function<String, EntityMetaData>()
		{
			@Override
			public EntityMetaData apply(String entityName)
			{
				return dataService.getEntityMetaData(entityName);
			}
		});
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
