package org.molgenis.omx.workflow;

import static org.molgenis.omx.workflow.WorkflowDataEntryController.URI;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(URI)
public class WorkflowDataEntryController extends MolgenisPluginController
{
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + "workflowdataentry";

	private final WorkflowService workflowService;

	@Autowired
	public WorkflowDataEntryController(WorkflowService workflowService)
	{
		super(URI);
		if (workflowService == null) throw new IllegalArgumentException("WorkflowService is null");
		this.workflowService = workflowService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model)
	{
		model.addAttribute("workflows", workflowService.getWorkflows());
		return "view-workflowdataentry";
	}

	@RequestMapping(value = "/workflow/{workflowId}", method = RequestMethod.GET)
	@ResponseBody
	public Workflow getWorkflow(@PathVariable Integer workflowId) throws WorkflowException
	{
		return workflowService.getWorkflow(workflowId);
	}

	@RequestMapping(value = "/workflow/{workflowId}/step/{workflowStepId}", method = RequestMethod.GET)
	public String getWorkflowApplicationStep(@PathVariable Integer workflowId, @PathVariable Integer workflowStepId,
			Model model) throws WorkflowException
	{
		model.addAttribute("workflowStep", workflowService.getWorkflowStep(workflowStepId));
		model.addAttribute("workflowStepData", workflowService.getWorkflowStepData(workflowStepId));
		return "view-workflowdataentry-pane";
	}
}
