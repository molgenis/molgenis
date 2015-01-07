package org.molgenis.compute.ui.analysis;

import org.apache.commons.io.FileUtils;
import org.molgenis.compute.ui.model.*;
import org.molgenis.data.csv.CsvWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hvbyelas on 1/7/15.
 */
public class DbValidator
{
	List<UIWorkflowProtocol> processedNodes = new ArrayList<UIWorkflowProtocol>();
	List<UIWorkflowParameter> parameters = new ArrayList<UIWorkflowParameter>();

	public static final String SEP = ",";

	public void createTestFromDB(Analysis analysis, String pathTest)
	{
		UIWorkflow workflow = analysis.getWorkflow();

		parameters = workflow.getParameters();
		try
		{
			String keys = "";
			String values = "";
			for (UIWorkflowParameter p : parameters)
			{
				keys += p.getKey() + SEP;
				values += p.getValue() + SEP;
			}

			keys = keys.substring(0,keys.length() - 1);
			values = values.substring(0,values.length() - 1);

			keys += System.getProperty("line.separator") + values + System.getProperty("line.separator");

			FileUtils.writeStringToFile(new File(pathTest + AnalysisPluginController.PARAMETERS_DEFAULT), keys);

			String nodes = "step"+ SEP + "protocol" + SEP + "dependencies\n";
			for(UIWorkflowNode node : workflow.getNodes())
			{
				 nodes += node.getName() + SEP + node.getProtocol().getName() + SEP;

				List<UIWorkflowNode> previous = node.getPreviousNodes();
				for(UIWorkflowNode pre : previous)
				{
					nodes += pre.getName() + ";";
				}

				List<UIParameterMapping> parameterMappings = node.getParameterMappings();

				if(previous.size() > 0 && parameterMappings.size() == 0)
					nodes = nodes.substring(0,nodes.length() - 1);

				 for(UIParameterMapping mapping : parameterMappings)
				 {
					 String from = mapping.getFrom();
					 String to = mapping.getTo();

					 nodes += from + "=" + to + ";";

				 }
				if(parameterMappings.size() > 0)
					nodes = nodes.substring(0,nodes.length() - 1);

				nodes += System.getProperty("line.separator");
			}
			FileUtils.writeStringToFile(new File(pathTest + AnalysisPluginController.WORKFLOW_DEFAULT), nodes);

		System.out.println("###" + parameters.size());
		int i = 0;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

}
