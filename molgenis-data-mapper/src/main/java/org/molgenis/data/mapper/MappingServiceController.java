package org.molgenis.data.mapper;

import static org.molgenis.data.mapper.MappingServiceController.URI;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.mapping.EntityMapping;
import org.molgenis.data.mapping.MappingProject;
import org.molgenis.data.mapping.MappingService;
import org.molgenis.data.repository.MappingProjectRepository;
import org.molgenis.data.repository.impl.MappingProjectRepositoryImpl;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(URI)
public class MappingServiceController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(MappingServiceController.class);

	public static final String ID = "mappingservice";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String VIEW_MAPPING_PROJECTS = "view-mapping-projects";
	private static final String VIEW_ATTRIBUTE_MAPPING = "view-attribute-mappings";

	@Autowired
	private MolgenisUserService molgenisUserService;

	@Autowired
	private MappingService mappingService;
	
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
	public String addMappingProject(HttpServletRequest request, Model model)
	{
		mappingService.addMappingProject(request.getParameter("mapping-project-name").toString(), getCurrentUser());

		model = setModelAttributes(model);
		return VIEW_MAPPING_PROJECTS;
	}

	@RequestMapping("/mappingproject/{id}")
	public String getAttributeMappingScreen(Model model)
	{
		// TODO Put attribute mapping information based on mapping project ID into the model
		return VIEW_ATTRIBUTE_MAPPING;
	}

	private Model setModelAttributes(Model model)
	{
		model.addAttribute("activeUser", getCurrentUser().getUsername());

		if (mappingService == null) model.addAttribute("mappingProjects", null);
		else model.addAttribute("mappingProjects", mappingService.getAllMappingProjects());

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
