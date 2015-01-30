package org.molgenis.data.mapper;

import static org.molgenis.data.mapper.MappingServiceController.URI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.mapping.MappingService;
import org.molgenis.data.mapping.model.MappingProject;
import org.molgenis.data.meta.MetaDataService;
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

@Controller
@RequestMapping(URI)
public class MappingServiceController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(MappingServiceController.class);

	public static final String ID = "mappingservice";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String VIEW_MAPPING_PROJECTS = "view-mapping-projects";
	private static final String VIEW_ATTRIBUTE_MAPPING = "view-single-mapping-project";

	@Autowired
	private MolgenisUserService molgenisUserService;

	@Autowired
	private MappingService mappingService;

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
		// FIXME need to wright complete URL else it will use /plugin as root and the molgenis header and footer wont be
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
		
		// Fill the model
		model.addAttribute("selectedTarget", selectedTarget);
		model.addAttribute("selectedTargetAttributes", targetAttributes.get(selectedTarget));
		model.addAttribute("targetAttributes", targetAttributes);
		model.addAttribute("mappingProject", selectedMappingProject);

		return VIEW_ATTRIBUTE_MAPPING;
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
