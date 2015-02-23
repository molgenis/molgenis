package org.molgenis.compute.ui.analysis;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.molgenis.compute.ui.model.*;
import org.molgenis.compute.ui.model.decorator.UIWorkflowDecorator;
import org.molgenis.data.*;
import org.molgenis.data.csv.CsvWriter;
import org.molgenis.data.support.QueryImpl;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hvbyelas on 1/7/15.
 */
public class AnalysisToFilesWriter
{
	List<UIWorkflowParameter> parameters = new ArrayList<UIWorkflowParameter>();

	private static Logger LOG = LoggerFactory.getLogger(AnalysisToFilesWriter.class);

	public static final String SEP = ",";

	public void writeToFiles(DataService dataService, Analysis analysis, String path)
	{
		UIWorkflow workflow = analysis.getWorkflow();

		try
		{
			//write parameters
			StringBuilder keysBuilder = new StringBuilder();

			int max = workflow.getParameters().get(0).getValues().size();

			StringBuilder[] valueBuilders =new StringBuilder[max];
			for (int i = 0; i < valueBuilders.length; i++)
			{
				valueBuilders[i] = new StringBuilder("");
			}

			parameters = workflow.getParameters();
			for (UIWorkflowParameter p : parameters)
			{
				keysBuilder.append(p.getKey()).append(SEP);
				for(int i = 0; i < valueBuilders.length; i++)
					valueBuilders[i].append(p.getValues().get(i).getValue()).append(SEP);
			}

			String keys = keysBuilder.toString().substring(0, keysBuilder.toString().length() - 1);

			List<String> values = new ArrayList<>();
			for (int i = 0; i < valueBuilders.length; i++)
			{
				String value = valueBuilders[i].toString()
						.substring(0, valueBuilders[i].toString().toString().length() - 1);
				values.add(value);
			}

			keys += System.getProperty("line.separator");

			for(String value : values)
			{
				keys += value + System.getProperty("line.separator");
			}

			FileUtils.writeStringToFile(new File(path + AnalysisPluginController.PARAMETERS_DEFAULT), keys);

			//write workflow
			String nodes = "step" + SEP + "protocol" + SEP + "dependencies\n";
			for (UIWorkflowNode node : workflow.getNodes())
			{
				nodes += node.getName() + SEP + node.getProtocol().getName() + SEP;

				List<UIWorkflowNode> previous = node.getPreviousNodes();
				for (UIWorkflowNode pre : previous)
				{
					nodes += pre.getName() + ";";
				}

				List<UIParameterMapping> parameterMappings = node.getParameterMappings();

				if (previous.size() > 0 && parameterMappings.size() == 0) nodes = nodes
						.substring(0, nodes.length() - 1);

				for (UIParameterMapping mapping : parameterMappings)
				{
					String from = mapping.getFrom();
					String to = mapping.getTo();

					nodes += from + "=" + to + ";";

				}
				if (parameterMappings.size() > 0) nodes = nodes.substring(0, nodes.length() - 1);

				nodes += System.getProperty("line.separator");
			}
			FileUtils.writeStringToFile(new File(path + AnalysisPluginController.WORKFLOW_DEFAULT), nodes);

			// worksheet writing
			CsvWriter csvWriter = new CsvWriter(new File(path + AnalysisPluginController.WORKSHEET), ',');
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
			List<UIWorkflowNode> WNodes = workflow.getNodes();

			List<String> writtenProtocols = new ArrayList<String>();
			for (UIWorkflowNode node : WNodes)
			{
				UIWorkflowProtocol protocol = node.getProtocol();
				String protocolName = protocol.getName();
				String template = protocol.getTemplate();

				if (!isWritten(writtenProtocols, protocolName))
				{
					FileUtils.writeStringToFile(new File(path + protocolName), template);
					writtenProtocols.add(protocolName);
				}
			}
		}
		catch (IOException e)
		{
			LOG.error("", e);
			throw new RuntimeException(e);
		}

	}

	private boolean isWritten(List<String> writtenProtocols, String protocolName)
	{
		return writtenProtocols.contains(protocolName);
	}

}
