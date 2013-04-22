package org.molgenis.compute.db.importer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.molgenis.compute.design.ComputeParameter;
import org.molgenis.compute.design.ComputeProtocol;
import org.molgenis.compute.design.ComputeStep;
import org.molgenis.compute.design.ComputeWorkflow;
import org.molgenis.compute5.model.Input;
import org.molgenis.compute5.model.Output;
import org.molgenis.compute5.model.Protocol;
import org.molgenis.compute5.model.Step;
import org.molgenis.compute5.model.Workflow;
import org.molgenis.compute5.parsers.WorkflowCsvParser;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;

public class WorkflowImporter
{
	private final Database database;

	public WorkflowImporter(Database database)
	{
		this.database = database;
	}

	public void importWorkflowFile(String workflowCsvFile, String name) throws IOException, DatabaseException
	{
		ComputeWorkflow computeWorkflow = createComputeWorkflow(name);

		Workflow workflow = WorkflowCsvParser.parse(workflowCsvFile);
		for (Step step : workflow.getSteps())
		{
			Protocol protocol = step.getProtocol();
			ComputeProtocol computeProtocol = new ComputeProtocol();
			computeProtocol.setName(protocol.getName());
			computeProtocol.setTemplate(protocol.getTemplate());
			computeProtocol.setCores(protocol.getCores());
			computeProtocol.setDescription(protocol.getDescription());

			List<ComputeParameter> computeParamaters = new ArrayList<ComputeParameter>();

			for (Input input : protocol.getInputs())
			{
				ComputeParameter param = new ComputeParameter();
				param.setName(input.getName());
				param.setType("in");
				param.setDescription(input.getDescription());
				param.setDataType(input.getType());
				database.add(param);
				computeParamaters.add(param);
			}

			for (Output output : protocol.getOutputs())
			{
				ComputeParameter param = new ComputeParameter();
				param.setName(output.getName());
				param.setType("out");
				param.setDescription(output.getDescription());
				param.setDataType(output.getType());
				param.setValue(output.getValue());
				database.add(param);
				computeParamaters.add(param);
			}

			computeProtocol.setParameters(computeParamaters);
			database.add(computeProtocol);

			ComputeStep computeStep = new ComputeStep();
			computeStep.setComputeProtocol(computeProtocol);
			computeStep.setComputeWorkflow(computeWorkflow);
			computeStep.setName(step.getName());
			database.add(computeStep);
		}

		// Set the previous steps
		for (Step step : workflow.getSteps())
		{
			if ((step.getPreviousSteps() != null) && !step.getPreviousSteps().isEmpty())
			{
				ComputeStep computeStep = ComputeStep.findByName(database, step.getName());
				for (String previous : step.getPreviousSteps())
				{
					ComputeStep previousStep = ComputeStep.findByName(database, previous);
					computeStep.getPreviousSteps().add(previousStep);
				}
				database.update(computeStep);
			}
		}
	}

	private ComputeWorkflow createComputeWorkflow(String name) throws DatabaseException
	{
		ComputeWorkflow computeWorkFlow = new ComputeWorkflow();
		computeWorkFlow.setName(name);
		database.add(computeWorkFlow);

		return computeWorkFlow;
	}

}
