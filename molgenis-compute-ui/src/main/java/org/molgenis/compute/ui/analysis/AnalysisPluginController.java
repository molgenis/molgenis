package org.molgenis.compute.ui.analysis;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.Date;

import org.apache.log4j.Logger;
import org.molgenis.compute.ui.IdGenerator;
import org.molgenis.compute.ui.meta.AnalysisMetaData;
import org.molgenis.compute.ui.meta.AnalysisTargetMetaData;
import org.molgenis.compute.ui.meta.UIBackendMetaData;
import org.molgenis.compute.ui.meta.UIWorkflowMetaData;
import org.molgenis.compute.ui.model.Analysis;
import org.molgenis.compute.ui.model.AnalysisTarget;
import org.molgenis.compute.ui.model.UIBackend;
import org.molgenis.compute.ui.model.UIWorkflow;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

@Controller
@RequestMapping(AnalysisPluginController.URI)
public class AnalysisPluginController extends MolgenisPluginController
{
	private static Logger logger = Logger.getLogger(AnalysisPluginController.class);

	public static final String ID = "analysis";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final DataService dataService;

	@Autowired
	public AnalysisPluginController(DataService dataService)
	{
		super(URI);
		this.dataService = dataService;
	}

	@RequestMapping(method = GET)
	public String init(Model model, @RequestParam(value = "analysis", required = false) String analysisId)
	{
		if (analysisId != null)
		{
			Analysis analysis = dataService.findOne(AnalysisMetaData.INSTANCE.getName(), analysisId, Analysis.class);
			if (analysis == null) throw new UnknownEntityException("Unknown Analysis [" + analysisId + "]");

			long nrTargets = dataService.count(AnalysisTargetMetaData.INSTANCE.getName(),
					new QueryImpl().eq(AnalysisTargetMetaData.ANALYSIS, analysis));

			model.addAttribute("analysis", analysis);
			model.addAttribute("hasAnalysisTargets", nrTargets > 0);
		}

		Iterable<UIWorkflow> workflows = dataService.findAll(UIWorkflowMetaData.INSTANCE.getName(), UIWorkflow.class);
		model.addAttribute("workflows", workflows);

		return "view-analysis";
	}

	@RequestMapping(value = "/view/{analysisId}", method = GET)
	public String viewAnalysis(Model model, @PathVariable(value = "analysisId") String analysisId)
	{
		Analysis analysis = dataService.findOne(AnalysisMetaData.INSTANCE.getName(), analysisId, Analysis.class);
		if (analysis == null) throw new UnknownEntityException("Unknown Analysis [" + analysisId + "]");

		long nrTargets = dataService.count(AnalysisTargetMetaData.INSTANCE.getName(),
				new QueryImpl().eq(AnalysisTargetMetaData.ANALYSIS, analysis));

		model.addAttribute("analysis", analysis);
		model.addAttribute("hasAnalysisTargets", nrTargets > 0);

		Iterable<UIWorkflow> workflows = dataService.findAll(UIWorkflowMetaData.INSTANCE.getName(), UIWorkflow.class);
		model.addAttribute("workflows", workflows);

		return "view-analysis";
	}

	@RequestMapping(value = "/create", method = GET)
	public String create(Model model, @RequestParam(value = "workflow", required = false) String workflowId,
			@RequestParam(value = "target", required = false) String targetEntityName,
			@RequestParam(value = "q", required = false) String query)
	{
		// TODO discuss how to select backend
		Iterable<UIBackend> backends = dataService.findAll(UIBackendMetaData.INSTANCE.getName(), UIBackend.class);
		if (Iterables.isEmpty(backends))
		{
			throw new RuntimeException("Database does not contain any backend");
		}
		UIBackend backend = backends.iterator().next();
		Date creationDate = new Date();

		UIWorkflow workflow;
		if (workflowId != null && !workflowId.isEmpty())
		{
			workflow = dataService.findOne(UIWorkflowMetaData.INSTANCE.getName(), workflowId, UIWorkflow.class);
			if (workflow == null)
			{
				throw new UnknownEntityException("Unknown " + UIWorkflow.class.getSimpleName() + " [" + workflowId
						+ "]");
			}

		}
		else
		{
			// TODO discuss how to select initial workflow if not specified
			Iterable<UIWorkflow> workflows = dataService.findAll(UIWorkflowMetaData.INSTANCE.getName(),
					UIWorkflow.class);
			if (Iterables.isEmpty(backends))
			{
				throw new RuntimeException("Database does not contain any workflows");
			}
			workflow = workflows.iterator().next();

		}

		String analysisId = IdGenerator.generateId();
		String analysisName = "Analysis-" + creationDate.getTime();
		final Analysis analysis = new Analysis(analysisId, analysisName);
		analysis.setBackend(backend);
		analysis.setCreationDate(creationDate);
		analysis.setWorkflow(workflow);
		dataService.add(AnalysisMetaData.INSTANCE.getName(), analysis);

		if (targetEntityName != null && !targetEntityName.isEmpty())
		{
			Iterable<Entity> targets;
			if (query != null)
			{
				targets = dataService.findAll(targetEntityName);
			}
			else
			{
				// FIXME apply query
				targets = dataService.findAll(targetEntityName);
			}

			dataService.add(AnalysisTargetMetaData.INSTANCE.getName(),
					Iterables.transform(targets, new Function<Entity, AnalysisTarget>()
					{
						@Override
						public AnalysisTarget apply(Entity entity)
						{
							return new AnalysisTarget(IdGenerator.generateId(), entity.getIdValue().toString(),
									analysis);
						}
					}));
		}

		return "rediect:" + AnalysisPluginController.URI + "?analysis=" + analysisId;
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
