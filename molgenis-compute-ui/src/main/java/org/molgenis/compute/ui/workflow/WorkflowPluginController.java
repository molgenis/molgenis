package org.molgenis.compute.ui.workflow;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.compute.ui.meta.UIWorkflowMetaData;
import org.molgenis.compute.ui.model.UIWorkflow;
import org.molgenis.compute5.ComputeProperties;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.collect.Lists;

@Controller
@RequestMapping(WorkflowPluginController.URI)
public class WorkflowPluginController extends MolgenisPluginController
{
	private static Logger logger = LoggerFactory.getLogger(WorkflowPluginController.class);

	public static final String ID = "workflow";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final DataService dataService;
	private final WorkflowImportService workflowImportService;
	private final MetaDataService metaDataService;
	private final WorkflowManageService workflowManageService;

	@Autowired
	public WorkflowPluginController(DataService dataService, MetaDataService metaDataService,
			WorkflowImportService workflowImportService, WorkflowManageService workflowManageService)
	{
		super(URI);
		this.dataService = dataService;
		this.workflowImportService = workflowImportService;
		this.metaDataService = metaDataService;
		this.workflowManageService = workflowManageService;
	}

	@RequestMapping(method = GET)
	public String init(@RequestParam(value = "q", required = false) String search, Model model)
	{
		Query q = new QueryImpl();
		if (StringUtils.isNotEmpty(search))
		{
			q.search(search);
			model.addAttribute("q", search);
		}

		model.addAttribute("workflows",
				Lists.newArrayList(dataService.findAll(UIWorkflowMetaData.INSTANCE.getName(), q, UIWorkflow.class)));

		return "view-workflows";
	}

	@RequestMapping(method = POST)
	public String createWorkflow(@Valid ImportWorkflowForm form, Model model)
	{
		ComputeProperties props = new ComputeProperties(new String[]
		{});
		props.path = form.getPath();
		props.workFlow = form.getWorkflowFileName();
		props.parameters = form.getParametersFileName();

		try
		{
			workflowImportService.importWorkflow(props);
		}
		catch (Exception e)
		{
			logger.info("Exception importing workflow '" + form.getWorkflowFileName() + "'", e);
			model.addAttribute("errorMessage", e.getMessage());
		}

		return init(null, model);
	}

	@RequestMapping(value = "{workflowName}", method = GET)
	public String viewWorkflow(@PathVariable("workflowName") String workflowName, Model model,
			HttpServletResponse response) throws IOException
	{
		UIWorkflow workflow = dataService.findOne(UIWorkflowMetaData.INSTANCE.getName(),
				new QueryImpl().eq(UIWorkflowMetaData.NAME, workflowName), UIWorkflow.class);

		if (workflow == null)
		{
			response.sendError(404);
			return null;
		}

		model.addAttribute("entities", Lists.newArrayList(metaDataService.getEntityMetaDatas()));
		model.addAttribute("workflow", workflow);

		return "view-workflow";
	}

	/**
	 * Called when the workflow form is updated
	 * 
	 * @param workflowName
	 * @param form
	 * @param model
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "{workflowName}", method = POST)
	public String updateWorkflow(@PathVariable("workflowName") String workflowName, @Valid UpdateWorkflowForm form,
			Model model, HttpServletResponse response) throws IOException
	{
		UIWorkflow workflow = dataService.findOne(UIWorkflowMetaData.INSTANCE.getName(),
				new QueryImpl().eq(UIWorkflowMetaData.NAME, workflowName), UIWorkflow.class);

		if (workflow == null)
		{
			response.sendError(404);
			return null;
		}

		try
		{
			workflowManageService.updateWorkflow(workflow.getIdentifier(), form.getName(), form.getDescription(),
					form.getTargetType(), form.isActive());
		}
		catch (Exception e)
		{
			model.addAttribute("errorMessage", e.getMessage());
		}

		model.addAttribute("entities", Lists.newArrayList(metaDataService.getEntityMetaDatas()));
		model.addAttribute("workflow",
				dataService.findOne(UIWorkflowMetaData.INSTANCE.getName(), workflow.getIdentifier(), UIWorkflow.class));

		return "view-workflow";
	}
}
