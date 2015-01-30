package org.molgenis.data.mapper;

import static org.molgenis.data.mapper.MappingServiceController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.algorithm.AlgorithmService;
import org.molgenis.data.algorithm.AlgorithmServiceImpl;
import org.molgenis.data.mapping.MappingService;
import org.molgenis.data.mapping.model.MappingProject;
import org.molgenis.data.support.DefaultAttributeMetaData;
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
	private static final String VIEW_ATTRIBUTE_MAPPING = "view-attribute-mappings";
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
	public String addMappingProject(@RequestParam("mapping-project-name") String identifier,
			@RequestParam("target-entity") String targetEntity)
	{
		mappingService.addMappingProject(identifier, getCurrentUser(), targetEntity);
		return "redirect:mappingproject/" + identifier;
	}

	@RequestMapping(value = "/editmappingproject", method = RequestMethod.POST)
	public String editMappingProject(@RequestParam("mapping-project-name") String identifier, Model model)
	{
		MappingProject mappingProject = mappingService.getMappingProject(identifier);
		mappingService.updateMappingProject(mappingProject);

		model = setModelAttributes(model);

		return VIEW_MAPPING_PROJECTS;
	}

	@RequestMapping("/mappingproject/{id}")
	public String getMappingProjectScreen(@PathVariable("id") String identifier, Model model)
	{
		model.addAttribute("mappingProject", mappingService.getMappingProject(identifier));
		return VIEW_ATTRIBUTE_MAPPING;
	}

	@RequestMapping("/mappingattributeshow")
	public String getMappingAttributeScreen(Model model)
	{
		return VIEW_EDIT_ATTRIBUTE_MAPPING;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/mappingattribute", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public MappingServiceResponse getMappingAttributeScreen(@RequestBody MappingServiceRequest mappingServiceRequest)
	{
		EntityMetaData targetEntityMetaData = dataService.getEntityMetaData(mappingServiceRequest
				.getTargetEntityIdentifier());

		EntityMetaData sourceEntityMetaData = dataService.getEntityMetaData(mappingServiceRequest
				.getSourceEntityIdentifier());

		AttributeMetaData targetAttribute = targetEntityMetaData != null ? targetEntityMetaData
				.getAttribute(mappingServiceRequest.getTargetAttributeIdentifier()) : null;

		// TODO : biobankconnect algorithm will be placed here!
		Iterable<AttributeMetaData> sourceAttributes = sourceEntityMetaData != null ? sourceEntityMetaData
				.getAtomicAttributes() : Collections.emptyList();

		return new MappingServiceResponse(mappingServiceRequest.getTargetEntityIdentifier(),
				mappingServiceRequest.getSourceEntityIdentifier(), targetAttribute, sourceAttributes);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/mappingattribute/testscript", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> testScrpit(@RequestBody MappingServiceRequest mappingServiceRequest)
	{
		Map<String, Object> results = new HashMap<String, Object>();

		EntityMetaData targetEntityMetaData = dataService.getEntityMetaData(mappingServiceRequest
				.getTargetEntityIdentifier());

		EntityMetaData sourceEntityMetaData = dataService.getEntityMetaData(mappingServiceRequest
				.getSourceEntityIdentifier());

		AttributeMetaData targetAttribute = targetEntityMetaData != null ? targetEntityMetaData
				.getAttribute(mappingServiceRequest.getTargetAttributeIdentifier()) : null;

		Iterable<AttributeMetaData> sourceAttributes = Lists.transform(
				AlgorithmServiceImpl.extractFeatureName(mappingServiceRequest.getAlgorithm()),
				new Function<String, AttributeMetaData>()
				{
					@Override
					public AttributeMetaData apply(final String attributeName)
					{
						return sourceEntityMetaData != null && sourceEntityMetaData.getAttribute(attributeName) != null ? sourceEntityMetaData
								.getAttribute(attributeName) : new DefaultAttributeMetaData(StringUtils.EMPTY);
					}
				});

		List<Object> calcualtedValues = algorithmService.applyAlgorithm(targetAttribute, sourceAttributes,
				mappingServiceRequest.getAlgorithm(),
				dataService.getRepositoryByEntityName(sourceEntityMetaData.getName()));

		results.put("result", calcualtedValues);

		return results;
	}

	@RequestMapping("/getsourcecolumn")
	@ResponseBody
	public List<String> getAttributesForNewSourceColumn(@RequestBody String newSourceEntityName)
	{
		List<String> mockAttributesForNewColumn = new ArrayList<String>(); // TODO use dataservice to return a
																			// List<Attributes>
		mockAttributesForNewColumn.add("sourceAttr1");
		mockAttributesForNewColumn.add("sourceAttr2");
		mockAttributesForNewColumn.add("sourceAttr3");
		mockAttributesForNewColumn.add("sourceAttr4");
		mockAttributesForNewColumn.add("sourceAttr5");
		return mockAttributesForNewColumn;
	}

	private Model setModelAttributes(Model model)
	{
		model.addAttribute("activeUser", getCurrentUser().getUsername());
		if (mappingService != null) model.addAttribute("mappingProjects", mappingService.getAllMappingProjects());

		Iterable<EntityMetaData> entitiesMeta = Iterables.transform(dataService.getEntityNames(),
				new Function<String, EntityMetaData>()
				{
					@Override
					public EntityMetaData apply(String entityName)
					{
						return dataService.getEntityMetaData(entityName);
					}
				});
		model.addAttribute("entitiesMeta", entitiesMeta);

		return model;
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
