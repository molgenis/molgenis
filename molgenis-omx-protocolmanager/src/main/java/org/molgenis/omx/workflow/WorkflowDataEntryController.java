package org.molgenis.omx.workflow;

import static org.molgenis.omx.workflow.WorkflowDataEntryController.URI;

import java.util.List;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

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

	@RequestMapping(value = "/workflow/{workflowId}/element/{workflowElementId}", method = RequestMethod.GET)
	public String getWorkflowElement(@PathVariable Integer workflowId, @PathVariable Integer workflowElementId,
			Model model) throws WorkflowException
	{
		model.addAttribute("workflowElement", workflowService.getWorkflowElement(workflowElementId));
		return "view-workflowdataentry-pane";
	}

	@RequestMapping(value = "/workflowelementdatarow/{rowId}", method = RequestMethod.POST, params = "_method=DELETE")
	@ResponseStatus(HttpStatus.OK)
	public void deleteWorkflowElementDataRow(@PathVariable Integer rowId) throws WorkflowException
	{
		workflowService.deleteWorkflowElementDataRow(rowId);
	}

	@RequestMapping(value = "/workflowelementdatarow", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void createWorkflowElementDataRowForConnections(@RequestParam Integer workflowElementId,
			@RequestParam("workflowElementDataRowIds[]") List<Integer> workflowElementDataRowIds)
			throws WorkflowException
	{
		workflowService.createWorkflowElementDataRowWithConnections(workflowElementId, workflowElementDataRowIds);
	}

	@RequestMapping(value = "/workflowelementdatarow/value", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void updateWorkflowElementDataRowValue(@RequestParam Integer workflowElementDataRowId,
			@RequestParam Integer featureId, @RequestParam String rawValue) throws WorkflowException
	{
		workflowService.createOrUpdateWorkflowElementDataRowValue(workflowElementDataRowId, featureId, rawValue);
	}
}
