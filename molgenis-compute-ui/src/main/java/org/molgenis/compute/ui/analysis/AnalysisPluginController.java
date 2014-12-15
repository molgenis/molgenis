package org.molgenis.compute.ui.analysis;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.common.collect.Iterables;
import org.molgenis.compute.ui.IdGenerator;
import org.molgenis.compute.ui.meta.AnalysisJobMetaData;
import org.molgenis.compute.ui.meta.AnalysisMetaData;
import org.molgenis.compute.ui.meta.UIBackendMetaData;
import org.molgenis.compute.ui.meta.UIWorkflowMetaData;
import org.molgenis.compute.ui.model.*;
import org.molgenis.compute5.CommandLineRunContainer;
import org.molgenis.compute5.ComputeCommandLine;
import org.molgenis.compute5.ComputeProperties;
import org.molgenis.compute5.GeneratedScript;
import org.molgenis.data.*;
import org.molgenis.data.csv.CsvWriter;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
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

	private String runID = "testEmpty12";
	private String path = ".tmp"+ File.separator + runID + File.separator;
	private String pathProtocols = path + "protocols" + File.separator;

	private static final String WORKFLOW_DEFAULT = "workflow.csv";
	private static final String PARAMETERS_DEFAULT = "parameters.csv";
	private static final String WORKSHEET = "worksheet.csv";

	private String extension = ".sh";

	private List<String> writtenProtocols = null;

	private UIWorkflow uiWorkflow = null;
	private final DataService dataService;

	@Autowired
	public AnalysisPluginController(DataService dataService)
	{
		super(URI);
		this.dataService = dataService;
	}

	@RequestMapping(method = GET)
	public String init()
	{
		return "view-analysis";
	}

	@RequestMapping(value = "/create", method = GET)
	public String create(Model model, @RequestParam(value = "workflow", required = false) String workflowId,
			@RequestParam(value = "target", required = false) String targetId,
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

		Iterable<UIWorkflow> workflows = dataService.findAll(UIWorkflowMetaData.INSTANCE.getName(), UIWorkflow.class);

		String analysisId = IdGenerator.generateId();
		String analysisName = "Analysis-" + creationDate.getTime();
		Analysis analysis = new Analysis(analysisId, analysisName);
		analysis.setBackend(backend);
		analysis.setCreationDate(creationDate);
		analysis.setWorkflow(workflow);
		dataService.add(AnalysisMetaData.INSTANCE.getName(), analysis);

		model.addAttribute("analysis", analysis);
		model.addAttribute("workflows", workflows);
		model.addAttribute("targetId", targetId);
		model.addAttribute("q", query);
		return "view-analysis-create";
	}

	// TODO include query
	@RequestMapping(value = "/execute", method = POST)
	public String executeWorkflow(Model model, @RequestParam(value = "workflowId") String workflowId,
			@RequestParam(value = "targetId") String targetId)
	{
		logger.info("Executing workflow [" + workflowId + "] for target [" + targetId + "]");

		try
		{
			uiWorkflow = dataService.findOne(UIWorkflowMetaData.INSTANCE.getName(),
					new QueryImpl().eq(UIWorkflowMetaData.IDENTIFIER, workflowId), UIWorkflow.class);

			String workflowFile = uiWorkflow.getWorkflowFile();
			String parametersFile = uiWorkflow.getParametersFile();


			FileUtils.writeStringToFile(new File(path + WORKFLOW_DEFAULT), workflowFile);
			FileUtils.writeStringToFile(new File(path + PARAMETERS_DEFAULT), parametersFile);

			CsvWriter csvWriter = new CsvWriter(new File(path + WORKSHEET),',');

			EntityMetaData metaData = dataService.getEntityMetaData(targetId);
			Iterable<AttributeMetaData> attributeMetaDatas = metaData.getAtomicAttributes();
			ArrayList<String> strMeta = new ArrayList<String>();
			for(AttributeMetaData attributeMetaData : attributeMetaDatas)
			{
				String str = attributeMetaData.getName();
				strMeta.add(str);
			}

			csvWriter.writeAttributeNames(strMeta);

			Iterable<Entity> entities = dataService.findAll(targetId);
			for(Entity entity : entities)
			{
				csvWriter.add(entity);
			}
			csvWriter.close();

			List<UIWorkflowNode> nodes = uiWorkflow.getNodes();

			writtenProtocols = new ArrayList<String>();
			for(UIWorkflowNode node: nodes)
			{
				String name = node.getName();
				UIWorkflowProtocol protocol = node.getProtocol();
				String protocolName = protocol.getName();
				String template = protocol.getTemplate();

				if(!isWritten(protocolName))
				{
//					FileUtils.writeStringToFile(new File(pathProtocols + protocolName + extension), template);
					FileUtils.writeStringToFile(new File(path + protocolName), template);
					writtenProtocols.add(protocolName);
				}
			}

			//generate jobs
			String[] args = {"--generate",
					"--workflow", path + WORKFLOW_DEFAULT,
					"--parameters",	path + PARAMETERS_DEFAULT,
					"--parameters", path + WORKSHEET,
					"-b", "slurm",
					"--runid", runID,
					"--weave", "--url","umcg.hpc.rug.nl",
					"--path", "",
					"-rundir", path + "rundir"};

			ComputeProperties properties = new ComputeProperties(args);
			properties.execute = false;
			properties.runDir = path + "rundir";
			CommandLineRunContainer container = new ComputeCommandLine().execute(properties);

			Analysis analysis = new Analysis(IdGenerator.generateId(), runID);
			analysis.setCreationDate(new Date());
			analysis.setWorkflow(uiWorkflow);
			analysis.setSubmitScsript(container.getSumbitScript());

			List<GeneratedScript> generatedScripts = container.getTasks();
			List<AnalysisJob> jobs = new ArrayList<AnalysisJob>();
			for(GeneratedScript generatedScript : generatedScripts)
			{
				AnalysisJob job = new AnalysisJob(IdGenerator.generateId());
			    job.setName(generatedScript.getName());
				job.setGeneratedScript(generatedScript.getScript());

				UIWorkflowNode node = findNode(generatedScript.getStepName());
				job.setWorkflowNode(node);
				jobs.add(job);
				dataService.add(AnalysisJobMetaData.INSTANCE.getName(), job);
			}

			analysis.setJobs(jobs);
			dataService.add(AnalysisMetaData.INSTANCE.getName(), analysis);


		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		//here analysis submission to be done

		model.addAttribute("workflowId", workflowId);
		model.addAttribute("targetId", targetId);
		model.addAttribute("message", "Executing workflow [" + workflowId + "] for target [" + targetId + "]");
		return "view-analysis";
	}

	private UIWorkflowNode findNode(String stepName)
	{
		for (UIWorkflowNode node : uiWorkflow.getNodes())
		{
			String name = node.getName();
			if(stepName.equalsIgnoreCase(name))
				return node;
		}
		return null;
	}

	private boolean isWritten(String protocolName)
	{
		return writtenProtocols.contains(protocolName);
	}
}
