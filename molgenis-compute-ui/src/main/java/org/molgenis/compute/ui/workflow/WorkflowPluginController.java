package org.molgenis.compute.ui.workflow;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.molgenis.compute.ui.meta.UIWorkflowMetaData;
import org.molgenis.compute.ui.model.UIWorkflow;
import org.molgenis.compute5.ComputeProperties;
import org.molgenis.data.DataService;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.Lists;

@Controller
@RequestMapping(WorkflowPluginController.URI)
public class WorkflowPluginController extends MolgenisPluginController
{
	public static final String ID = "workflow";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private static Logger logger = Logger.getLogger(WorkflowPluginController.class);
	private final DataService dataService;
	private final WorkflowImportService workflowImportService;

	@Autowired
	public WorkflowPluginController(DataService dataService, WorkflowImportService workflowImportService)
	{
		super(URI);
		this.dataService = dataService;
		this.workflowImportService = workflowImportService;
	}

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		model.addAttribute("workflows",
				Lists.newArrayList(dataService.findAll(UIWorkflowMetaData.INSTANCE.getName(), UIWorkflow.class)));

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

		return init(model);
	}
}
