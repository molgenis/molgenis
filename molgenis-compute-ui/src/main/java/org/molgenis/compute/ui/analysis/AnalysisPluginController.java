package org.molgenis.compute.ui.analysis;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.apache.log4j.Logger;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(AnalysisPluginController.URI)
public class AnalysisPluginController extends MolgenisPluginController
{
	private static Logger logger = Logger.getLogger(AnalysisPluginController.class);

	public static final String ID = "analysis";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	public AnalysisPluginController()
	{
		super(URI);
	}

	@RequestMapping(method = GET)
	public String init(Model model, @RequestParam(value = "workflow", required = false) String workflowId,
			@RequestParam(value = "target", required = false) String targetId,
			@RequestParam(value = "q", required = false) String query)
	{
		model.addAttribute("workflowId", workflowId);
		model.addAttribute("targetId", targetId);
		model.addAttribute("q", query);
		return "view-analysis";
	}

	// TODO include query
	@RequestMapping(value = "/execute", method = POST)
	public String executeWorkflow(Model model, @RequestParam(value = "workflowId") String workflowId,
			@RequestParam(value = "targetId") String targetId)
	{
		logger.info("Executing workflow [" + workflowId + "] for target [" + targetId + "]");
		model.addAttribute("workflowId", workflowId);
		model.addAttribute("targetId", targetId);
		model.addAttribute("message", "Executing workflow [" + workflowId + "] for target [" + targetId + "]");
		return "view-analysis";
	}
}
