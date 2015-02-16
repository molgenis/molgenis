package org.molgenis.compute.ui.analysis;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.molgenis.compute.ui.IdGenerator;
import org.molgenis.compute.ui.clusterexecutor.ClusterManager;
import org.molgenis.compute.ui.meta.AnalysisJobMetaData;
import org.molgenis.compute.ui.meta.AnalysisMetaData;
import org.molgenis.compute.ui.meta.UIBackendMetaData;
import org.molgenis.compute.ui.meta.UIWorkflowMetaData;
import org.molgenis.compute.ui.model.Analysis;
import org.molgenis.compute.ui.model.AnalysisJob;
import org.molgenis.compute.ui.model.UIBackend;
import org.molgenis.compute.ui.model.UIWorkflow;
import org.molgenis.compute.ui.model.UIWorkflowNode;
import org.molgenis.compute.ui.model.decorator.UIWorkflowDecorator;
import org.molgenis.compute5.CommandLineRunContainer;
import org.molgenis.compute5.ComputeCommandLine;
import org.molgenis.compute5.ComputeProperties;
import org.molgenis.compute5.GeneratedScript;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.QueryRule;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.dataexplorer.event.DataExplorerRegisterRefCellClickEventHandler;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.security.core.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

@Controller
@RequestMapping(AnalysisPluginController.URI)
public class AnalysisPluginController extends MolgenisPluginController implements
		DataExplorerRegisterRefCellClickEventHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(AnalysisPluginController.class);

	public static final String ID = "analysis";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private static final String CREATE_MAPPING = "/create";
	public static final String URI_CREATE = URI + CREATE_MAPPING;

	// FIXME do not use files, use entities from database
	public static final String WORKFLOW_DEFAULT = "workflow.csv";
	public static final String PARAMETERS_DEFAULT = "parameters.csv";
	public static final String WORKSHEET = "worksheet.csv";

	private final DataService dataService;

	@Autowired
	private ClusterManager clusterManager;

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
			model.addAttribute("analysis", analysis);
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
		model.addAttribute("analysis", analysis);

		Iterable<UIWorkflow> workflows = dataService.findAll(UIWorkflowMetaData.INSTANCE.getName(), UIWorkflow.class);
		model.addAttribute("workflows", workflows);

		return "view-analysis";
	}

	@Transactional
	@RequestMapping(value = CREATE_MAPPING, method = POST)
	@ResponseBody
	public Map<String, Object> createAnalysis(Model model,
			@Valid @RequestBody CreateAnalysisRequest createAnalysisRequest)
	{
		String workflowId = createAnalysisRequest.getWorkflowId();

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
		String analysisName = generateAnalysisName(creationDate);
		final Analysis analysis = new Analysis(analysisId, analysisName);
		analysis.setBackend(backend);
		analysis.setCreationDate(creationDate);
		analysis.setWorkflow(workflow);
		analysis.setUser(SecurityUtils.getCurrentUsername());
		dataService.add(AnalysisMetaData.INSTANCE.getName(), analysis);

		String targetEntityName = createAnalysisRequest.getTargetEntityName();
		if (targetEntityName != null && !targetEntityName.isEmpty())
		{
			// get requested targets
			Iterable<Entity> targets;
			List<QueryRule> queryRules = createAnalysisRequest.getQ();
			if (queryRules != null)
			{
				targets = dataService.findAll(targetEntityName, new QueryImpl(queryRules));
			}
			else
			{
				targets = dataService.findAll(targetEntityName);
			}

			// set analysis on requested targets
			final String analysisAttrName = UIWorkflowDecorator.ANALYSIS_ATTRIBUTE.getName();
			dataService.update(targetEntityName, Iterables.transform(targets, new Function<Entity, Entity>()
			{
				@Override
				public Entity apply(Entity entity)
				{
					Iterable<Analysis> targetAnalysis = entity.getEntities(analysisAttrName, Analysis.class);
					entity.set(analysisAttrName, Iterables.concat(targetAnalysis, Arrays.asList(analysis)));
					return entity;
				}
			}));
		}

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("href", "/menu/main/analysis/view/" + analysisId); // for data explorer
		response.put(AnalysisMetaData.IDENTIFIER, analysisId);
		return response;
	}

	@Transactional
	@RequestMapping(value = "/clone/{analysisId}", method = POST)
	@ResponseBody
	public Map<String, String> cloneAnalysis(@PathVariable(value = "analysisId") String analysisId)
	{
		final Analysis analysis = dataService.findOne(AnalysisMetaData.INSTANCE.getName(), analysisId, Analysis.class);
		if (analysis == null) throw new UnknownEntityException("Unknown Analysis [" + analysisId + "]");

		Date clonedCreationDate = new Date();
		String clonedAnalysisId = IdGenerator.generateId();
		String clonedAnalysisName = generateAnalysisName(clonedCreationDate);

		final Analysis clonedAnalysis = new Analysis(clonedAnalysisId, clonedAnalysisName);
		clonedAnalysis.setBackend(analysis.getBackend());
		clonedAnalysis.setCreationDate(clonedCreationDate);
		clonedAnalysis.setDescription(analysis.getDescription());
		// do not set jobs, submitScript
		clonedAnalysis.setWorkflow(analysis.getWorkflow());
		clonedAnalysis.setUser(analysis.getUser());
		dataService.add(AnalysisMetaData.INSTANCE.getName(), clonedAnalysis);

		String targetEntityName = analysis.getWorkflow().getTargetType();
		final String analysisAttrName = UIWorkflowDecorator.ANALYSIS_ATTRIBUTE.getName();

		// add cloned analysis to targets
		Iterable<Entity> targets = dataService
				.findAll(targetEntityName, new QueryImpl().eq(analysisAttrName, analysis));
		dataService.update(targetEntityName, Iterables.transform(targets, new Function<Entity, Entity>()
		{
			@Override
			public Entity apply(Entity entity)
			{
				Iterable<Analysis> targetAnalysis = entity.getEntities(analysisAttrName, Analysis.class);
				entity.set(analysisAttrName, Iterables.concat(targetAnalysis, Arrays.asList(clonedAnalysis)));
				return entity;
			}
		}));

		return Collections.singletonMap(AnalysisMetaData.IDENTIFIER, clonedAnalysisId);
	}

	@Transactional
	@RequestMapping(value = "/run/{analysisId}", method = POST)
	@ResponseStatus(HttpStatus.OK)
	public void runAnalysis(@PathVariable(value = "analysisId") String analysisId)
	{
		Analysis analysis = dataService.findOne(AnalysisMetaData.INSTANCE.getName(), analysisId, Analysis.class);
		if (analysis == null) throw new UnknownEntityException("Unknown Analysis [" + analysisId + "]");
		LOG.info("Running analysis [" + analysisId + "]");

		String runID = analysis.getName();

		String path = ".tmp" + File.separator + runID + File.separator;

		new AnalysisToFilesWriter().writeToFiles(dataService, analysis, path);

		UIBackend backend = analysis.getBackend();
		String backendHost = backend.getHost();
		String schedulerType = backend.getSchedulerType().toString().toLowerCase();
		String[] args =
		{ "--generate", "--workflow", path + WORKFLOW_DEFAULT, "--parameters", path + PARAMETERS_DEFAULT,
				"--parameters", path + WORKSHEET, "-b", schedulerType, "--runid", runID, "--weave", "--url",
				backendHost, "--path", "", "-rundir", path + "rundir" };

		ComputeProperties properties = new ComputeProperties(args);
		properties.execute = false;
		properties.runDir = path + "rundir";
		CommandLineRunContainer container = null;
		try
		{
			container = new ComputeCommandLine().execute(properties);
		}
		catch (Exception e)
		{
			LOG.error("", e);
			throw new RuntimeException(e);
		}

		UIWorkflow uiWorkflow = analysis.getWorkflow();
		List<GeneratedScript> generatedScripts = container.getTasks();
		for (GeneratedScript generatedScript : generatedScripts)
		{
			UIWorkflowNode node = findNode(uiWorkflow, generatedScript.getStepName());

			AnalysisJob job = new AnalysisJob(IdGenerator.generateId());
			job.setName(generatedScript.getName());
			job.setGeneratedScript(generatedScript.getScript());
			job.setWorkflowNode(node);
			job.setAnalysis(analysis);
			dataService.add(AnalysisJobMetaData.INSTANCE.getName(), job);
		}

		// update analysis
		analysis.setSubmitScript(container.getSumbitScript());

		dataService.update(AnalysisMetaData.INSTANCE.getName(), analysis);

		clusterManager.executeAnalysis(analysis);

	}

	@Transactional
	@RequestMapping(value = "/rerun/{analysisId}", method = POST)
	@ResponseStatus(HttpStatus.OK)
	public void reRunAnalysis(@PathVariable(value = "analysisId") String analysisId)
	{
		System.out.println("In ReRun");

		Analysis analysis = dataService.findOne(AnalysisMetaData.INSTANCE.getName(), analysisId, Analysis.class);
		if (analysis == null) throw new UnknownEntityException("Unknown Analysis [" + analysisId + "]");
		LOG.info("Running analysis [" + analysisId + "]");

		//TODO: continue here
	}

	@Transactional
	@RequestMapping(value = "/pause/{analysisId}", method = POST)
	@ResponseStatus(HttpStatus.OK)
	public void pauseAnalysis(@PathVariable(value = "analysisId") String analysisId)
	{
		// TODO implement pause analysis
		LOG.info("TODO implement pause analysis");
		throw new RuntimeException("'Pause analysis' not implemented");
	}

	@Transactional
	@RequestMapping(value = "/continue/{analysisId}", method = POST)
	@ResponseStatus(HttpStatus.OK)
	public void continueAnalysis(@PathVariable(value = "analysisId") String analysisId)
	{
		// TODO implement continue analysis
		LOG.info("TODO implement continue analysis");
		throw new RuntimeException("'Continue analysis' not implemented");
	}

	@Transactional
	@RequestMapping(value = "/stop/{analysisId}", method = POST)
	@ResponseStatus(HttpStatus.OK)
	public void stopAnalysis(@PathVariable(value = "analysisId") String analysisId)
	{
		Analysis analysis = dataService.findOne(AnalysisMetaData.INSTANCE.getName(), analysisId, Analysis.class);
		clusterManager.cancelRunJobs(analysis);
	}

	@RequestMapping("/{analysisId}/progress.js")
	public String getProgressScript(@PathVariable(value = "analysisId") String analysisId, Model model)
	{
		Analysis analysis = dataService.findOne(AnalysisMetaData.INSTANCE.getName(), analysisId, Analysis.class);

		Iterable<AnalysisJob> analysisJobs = dataService.findAll(AnalysisJobMetaData.INSTANCE.getName(),
				new QueryImpl().eq(AnalysisJobMetaData.ANALYSIS, analysis), AnalysisJob.class);

		model.addAttribute("analysis", analysis);
		model.addAttribute("jobCount", new AnalysisJobCount(analysisJobs));
		return "progress";
	}

	private UIWorkflowNode findNode(UIWorkflow uiWorkflow, String stepName)
	{
		for (UIWorkflowNode node : uiWorkflow.getNodes())
		{
			String name = node.getName();
			if (stepName.equalsIgnoreCase(name)) return node;
		}
		return null;
	}

	private String generateAnalysisName(Date creationDate)
	{
		return "Analysis (" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(creationDate) + ')';
	}

	private static class CreateAnalysisRequest
	{
		private String workflowId;

		private String targetEntityName;

		private List<QueryRule> q;

		public String getWorkflowId()
		{
			return workflowId;
		}

		public String getTargetEntityName()
		{
			return targetEntityName;
		}

		public List<QueryRule> getQ()
		{
			return q;
		}
	}

	@Override
	public String getRefRedirectUrlTemplate()
	{
		// FIXME do not hardcode menu reference
		return "/menu/main/analysis/view/{{id}}";
	}
}
