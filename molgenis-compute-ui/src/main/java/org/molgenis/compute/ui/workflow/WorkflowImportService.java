package org.molgenis.compute.ui.workflow;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.molgenis.compute.ui.ComputeUiException;
import org.molgenis.compute.ui.IdGenerator;
import org.molgenis.compute.ui.meta.*;
import org.molgenis.compute.ui.model.*;
import org.molgenis.compute5.ComputeProperties;
import org.molgenis.compute5.model.Input;
import org.molgenis.compute5.model.Output;
import org.molgenis.compute5.model.Step;
import org.molgenis.compute5.model.Workflow;
import org.molgenis.compute5.parsers.WorkflowCsvParser;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.QueryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Component
public class WorkflowImportService
{
	private static Logger logger = LoggerFactory.getLogger(WorkflowImportService.class);

	private final DataService dataService;
	private final SearchService searchService;

	@Autowired
	public WorkflowImportService(DataService dataService, SearchService searchService)
	{
		this.dataService = dataService;
		this.searchService = searchService;
	}

	@Transactional
	@PreAuthorize("hasAnyRole('ROLE_SYSTEM, ROLE_SU')")
	public void importWorkflow(ComputeProperties computeProperties) throws IOException
	{
		try
		{
			doImportWorkflow(computeProperties);
		}
		catch (Exception e)
		{
			try
			{
				rebuildIndices();
			}
			catch (Exception e1)
			{
				logger.error("Exception rebuilding indices after importWorkflow", e1);
			}

			throw e;
		}
	}

	@Transactional
	public void doImportWorkflow(ComputeProperties computeProperties) throws IOException
	{
		String baseDir = computeProperties.path;
		if (!new File(baseDir).exists()) throw new IOException("Directory '" + baseDir + "' does not exist.");

		String workflowFileName = Paths.get(baseDir, computeProperties.workFlow).toString();
		if (!new File(workflowFileName).exists()) throw new IOException("Workflow file '" + workflowFileName
				+ "' does not exist.");

		String workflowName = Paths.get(baseDir).getFileName().toString();
		if (workflowExists(workflowName)) throw new ComputeUiException("Workflow '" + workflowName
				+ "' already exists.");

		File parameterFile = new File(baseDir, computeProperties.parameters[0]);
		if (!parameterFile.exists()) throw new IOException("Parameters file '" + computeProperties.parameters[0]
				+ "' does not exist.");

		logger.info("Importing pipeline '" + workflowName + "'");

		Workflow workflow = new WorkflowCsvParser().parse(workflowFileName, computeProperties);

		Map<String, UIWorkflowNode> nodesByName = Maps.newLinkedHashMap();
		Map<String, UIParameter> parametersByName = Maps.newLinkedHashMap();

		for (Step step : workflow.getSteps())
		{
			UIWorkflowProtocol protocol = dataService.findOne(UIWorkflowProtocolMetaData.INSTANCE.getName(),
					new QueryImpl().eq(UIWorkflowProtocolMetaData.NAME, step.getProtocol().getName()),
					UIWorkflowProtocol.class);

			if (protocol == null)
			{
				protocol = new UIWorkflowProtocol(IdGenerator.generateId(), step.getProtocol().getName(), step
						.getProtocol().getTemplate());

				List<UIParameter> parameters = Lists.newArrayList();

				for (Input input : step.getProtocol().getInputs())
				{
					UIParameter parameter = new UIParameter(IdGenerator.generateId(), input.getName());
					parameter.setType(ParameterType.INPUT);
					parameter.setDataType(input.getType());
					parameters.add(parameter);
					parametersByName.put(parameter.getName(), parameter);
				}

				for (Output output : step.getProtocol().getOutputs())
				{
					UIParameter parameter = new UIParameter(IdGenerator.generateId(), output.getName());
					parameter.setType(ParameterType.OUTPUT);
					parameter.setDataType(output.getType());
					parameters.add(parameter);
					parametersByName.put(parameter.getName(), parameter);
				}

				dataService.add(UIParameterMetaData.INSTANCE.getName(), parameters);
				protocol.setParameters(parameters);

				dataService.add(UIWorkflowProtocolMetaData.INSTANCE.getName(), protocol);
			}

			UIWorkflowNode node = new UIWorkflowNode(IdGenerator.generateId(), step.getName(), protocol);
			dataService.add(UIWorkflowNodeMetaData.INSTANCE.getName(), node);

			nodesByName.put(step.getName(), node);
		}

		// Set previous nodes and parametermappings
		for (Step step : workflow.getSteps())
		{
			if (!step.getPreviousSteps().isEmpty())
			{
				UIWorkflowNode node = nodesByName.get(step.getName());
				for (String prevStepName : step.getPreviousSteps())
				{
					node.addPreviousNode(nodesByName.get(prevStepName));
				}

				// Parameter mapping, parameter can come from input/output parameters, parameters.csv or can be a
				// worksheet attribute name
				List<UIParameterMapping> uiParameterMappings = Lists.newArrayList();
				for (Map.Entry<String, String> mapping : step.getParametersMapping().entrySet())
				{
					uiParameterMappings.add(new UIParameterMapping(IdGenerator.generateId(), mapping.getKey(), mapping
							.getValue()));
				}

				dataService.add(UIParameterMappingMetaData.INSTANCE.getName(), uiParameterMappings);
				node.setParameterMappings(uiParameterMappings);
			}
		}

		dataService.update(UIWorkflowNodeMetaData.INSTANCE.getName(), nodesByName.values());

		List<UIWorkflowParameter> uiWorkflowParameters = parseParametersFile(parameterFile);
		dataService.add(UIWorkflowParameterMetaData.INSTANCE.getName(), uiWorkflowParameters);

		UIWorkflow uiWorkflow = new UIWorkflow(IdGenerator.generateId(), workflowName);
		uiWorkflow.setNodes(Lists.newArrayList(nodesByName.values()));

		uiWorkflow.setParameters(uiWorkflowParameters);

		dataService.add(UIWorkflowMetaData.INSTANCE.getName(), uiWorkflow);

		logger.info("Import pipeline '" + workflowName + "' done.");
	}

	private boolean workflowExists(String name)
	{
		return dataService.findOne(UIWorkflowMetaData.INSTANCE.getName(),
				new QueryImpl().eq(UIWorkflowMetaData.NAME, name)) != null;
	}

	private List<UIWorkflowParameter> parseParametersFile(File f)
	{
		if (!f.getName().toLowerCase().endsWith(".csv")) throw new ComputeUiException(
				"Parameters file must be a csv file.");

		List<UIWorkflowParameter> params = Lists.newArrayList();
		CsvRepository csv = new CsvRepository(f, null);

		HashMap<String, List<String>> tmp = new HashMap<String, List<String>>();

		for(Entity e : csv)
		{
			for (AttributeMetaData attr : csv.getEntityMetaData().getAttributes())
			{
				String name = attr.getName();
				String value = e.getString(attr.getName());

				if(!tmp.containsKey(name))
				{
					List<String> values = new ArrayList<String>();
					values.add(value);
					tmp.put(name, values);
				}
				else
				{
					List<String> values = tmp.get(name);
					values.add(value);
				}
			}
		}

		Iterator iter = tmp.entrySet().iterator();
		while (iter.hasNext())
		{
			Map.Entry entry = (Map.Entry) iter.next();
			String name = (String) entry.getKey();
			List<String> values = (List<String>) entry.getValue();

			List<UIWorkflowParameterValue> uiValues = new ArrayList<UIWorkflowParameterValue>();
			for(String value: values)
			{
				UIWorkflowParameterValue uiParameterValue = new UIWorkflowParameterValue(IdGenerator.generateId(),
						value);
				uiValues.add(uiParameterValue);
			}
			dataService.add(UIWorkflowParameterValueMetaData.INSTANCE.getName(), uiValues);
			params.add(new UIWorkflowParameter(IdGenerator.generateId(), name, uiValues));
		}
		return params;
	}

	private void rebuildIndices()
	{
		List<EntityMetaData> metasToReIndex = Arrays.<EntityMetaData> asList(UIParameterMetaData.INSTANCE,
				UIWorkflowProtocolMetaData.INSTANCE, UIWorkflowNodeMetaData.INSTANCE,
				UIWorkflowParameterMetaData.INSTANCE, UIWorkflowMetaData.INSTANCE);

		for (EntityMetaData meta : metasToReIndex)
		{
			searchService.rebuildIndex(dataService.findAll(meta.getName()), meta);
		}
	}
}
