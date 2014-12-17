package org.molgenis.compute.ui.analysis;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.molgenis.compute.ui.IdGenerator;
import org.molgenis.compute.ui.meta.AnalysisJobMetaData;
import org.molgenis.compute.ui.meta.AnalysisMetaData;
import org.molgenis.compute.ui.meta.UIBackendMetaData;
import org.molgenis.compute.ui.meta.UIWorkflowMetaData;
import org.molgenis.compute.ui.model.Analysis;
import org.molgenis.compute.ui.model.AnalysisJob;
import org.molgenis.compute.ui.model.UIBackend;
import org.molgenis.compute.ui.model.UIWorkflow;
import org.molgenis.compute.ui.model.UIWorkflowNode;
import org.molgenis.compute.ui.model.UIWorkflowProtocol;
import org.molgenis.compute.ui.model.decorator.UIWorkflowDecorator;
import org.molgenis.compute5.CommandLineRunContainer;
import org.molgenis.compute5.ComputeCommandLine;
import org.molgenis.compute5.ComputeProperties;
import org.molgenis.compute5.GeneratedScript;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.QueryRule;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.csv.CsvWriter;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
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
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

@Controller
@RequestMapping(AnalysisPluginController.URI)
public class AnalysisPluginController extends MolgenisPluginController
{
	private static Logger logger = Logger.getLogger(AnalysisPluginController.class);

	public static final String ID = "analysis";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private static final String CREATE_MAPPING = "/create";
	public static final String URI_CREATE = URI + CREATE_MAPPING;

	// FIXME do not use files, use entities from database
	private static final String WORKFLOW_DEFAULT = "workflow.csv";
	private static final String PARAMETERS_DEFAULT = "parameters.csv";
	private static final String WORKSHEET = "worksheet.csv";

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
				entity.set(analysisAttrName, Iterables.concat(targetAnalysis, Arrays.asList(analysis)));
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
		logger.info("Running analysis [" + analysisId + "]");

		String runID = analysisId;
		String path = ".tmp" + File.separator + runID + File.separator;

		try
		{
			UIWorkflow uiWorkflow = analysis.getWorkflow();

			String workflowFile = uiWorkflow.getWorkflowFile();
			String parametersFile = uiWorkflow.getParametersFile();

			FileUtils.writeStringToFile(new File(path + WORKFLOW_DEFAULT), workflowFile);
			FileUtils.writeStringToFile(new File(path + PARAMETERS_DEFAULT), parametersFile);

			CsvWriter csvWriter = new CsvWriter(new File(path + WORKSHEET), ',');
			try
			{
				String targetEntityName = analysis.getWorkflow().getTargetType();
				final String analysisAttrName = UIWorkflowDecorator.ANALYSIS_ATTRIBUTE.getName();
				Iterable<Entity> targets = dataService.findAll(targetEntityName,
						new QueryImpl().eq(analysisAttrName, analysis));
				if (targets == null || Iterables.isEmpty(targets))
				{
					throw new UnknownEntityException("Expected at least one analysis target");
				}

				EntityMetaData metaData = dataService.getEntityMetaData(targetEntityName);
				csvWriter.writeAttributeNames(Iterables.transform(
						Iterables.filter(metaData.getAtomicAttributes(), new Predicate<AttributeMetaData>()
						{
							@Override
							public boolean apply(AttributeMetaData attribute)
							{
								// exclude analysis attribute
								return !attribute.getName().equals(analysisAttrName);
							}
						}), new Function<AttributeMetaData, String>()
						{
							@Override
							public String apply(AttributeMetaData attribute)
							{
								return attribute.getName();
							}
						}));

				for (Entity entity : targets)
				{
					csvWriter.add(entity);
				}
			}
			finally
			{
				csvWriter.close();
			}
			List<UIWorkflowNode> nodes = uiWorkflow.getNodes();

			List<String> writtenProtocols = new ArrayList<String>();
			for (UIWorkflowNode node : nodes)
			{
				UIWorkflowProtocol protocol = node.getProtocol();
				String protocolName = protocol.getName();
				String template = protocol.getTemplate();

				if (!isWritten(writtenProtocols, protocolName))
				{
					// FileUtils.writeStringToFile(new File(pathProtocols + protocolName + extension), template);
					FileUtils.writeStringToFile(new File(path + protocolName), template);
					writtenProtocols.add(protocolName);
				}
			}

			// generate jobs
			String[] args =
			{ "--generate", "--workflow", path + WORKFLOW_DEFAULT, "--parameters", path + PARAMETERS_DEFAULT,
					"--parameters", path + WORKSHEET, "-b", "slurm", "--runid", runID, "--weave", "--url",
					"umcg.hpc.rug.nl", "--path", "", "-rundir", path + "rundir" };

			ComputeProperties properties = new ComputeProperties(args);
			properties.execute = false;
			properties.runDir = path + "rundir";
			CommandLineRunContainer container = new ComputeCommandLine().execute(properties);

			List<GeneratedScript> generatedScripts = container.getTasks();
			List<AnalysisJob> jobs = new ArrayList<AnalysisJob>();
			for (GeneratedScript generatedScript : generatedScripts)
			{
				AnalysisJob job = new AnalysisJob(IdGenerator.generateId());
				job.setName(generatedScript.getName());
				job.setGeneratedScript(generatedScript.getScript());

				UIWorkflowNode node = findNode(uiWorkflow, generatedScript.getStepName());
				job.setWorkflowNode(node);
				jobs.add(job);
				dataService.add(AnalysisJobMetaData.INSTANCE.getName(), job);
			}

			// update analysis
			analysis.setSubmitScript(container.getSumbitScript());
			analysis.setJobs(jobs);
			dataService.update(AnalysisMetaData.INSTANCE.getName(), analysis);

		}
		catch (IOException e)
		{
			logger.error("", e);
			throw new RuntimeException(e);
		}
		catch (Exception e)
		{
			logger.error("", e);
			throw new RuntimeException(e);
		}
	}

	@Transactional
	@RequestMapping(value = "/pause/{analysisId}", method = POST)
	@ResponseStatus(HttpStatus.OK)
	public void pauseAnalysis(@PathVariable(value = "analysisId") String analysisId)
	{
		// TODO implement pause analysis
		logger.info("TODO implement pause analysis");
		throw new RuntimeException("'Pause analysis' not implemented");
	}

	@Transactional
	@RequestMapping(value = "/stop/{analysisId}", method = POST)
	@ResponseStatus(HttpStatus.OK)
	public void stopAnalysis(@PathVariable(value = "analysisId") String analysisId)
	{
		// TODO implement stop analysis
		logger.info("TODO implement stop analysis");
		throw new RuntimeException("'Stop analysis' not implemented");
	}

	@RequestMapping("/{analysisId}/progress.js")
	public String getProgressScript(@PathVariable(value = "analysisId") String analysisId, Model model)
	{
		Analysis analysis = dataService.findOne(AnalysisMetaData.INSTANCE.getName(), analysisId, Analysis.class);
		model.addAttribute("analysis", analysis);

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

	private boolean isWritten(List<String> writtenProtocols, String protocolName)
	{
		return writtenProtocols.contains(protocolName);
	}

	private String generateAnalysisName(Date creationDate)
	{
		return "Analysis-" + creationDate.getTime();
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
}
