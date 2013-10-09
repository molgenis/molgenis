package org.molgenis.omx.workflow;

import static org.molgenis.framework.db.QueryRule.Operator.EQUALS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.omx.converters.ValueConverter;
import org.molgenis.omx.converters.ValueConverterException;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.utils.ProtocolUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

@Service
public class WorkflowServiceImpl implements WorkflowService
{
	private final Database database;

	@Autowired
	public WorkflowServiceImpl(Database database)
	{
		if (database == null) throw new IllegalArgumentException("Database is null");
		this.database = database;
	}

	@Override
	public Workflow getWorkflow(Integer workflowId) throws WorkflowException
	{
		Protocol protocol;
		try
		{
			protocol = Protocol.findById(database, workflowId);
			if (protocol == null) throw new WorkflowException("Unknown workflow [" + workflowId + "]");
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
		return createWorkflow(protocol);
	}

	@Override
	public List<Workflow> getWorkflows()
	{
		List<Protocol> workflows;
		try
		{
			workflows = database.find(Protocol.class, new QueryRule(Protocol.ROOT, EQUALS, true));
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
		return Lists.transform(workflows, new Function<Protocol, Workflow>()
		{

			@Override
			public Workflow apply(Protocol protocol)
			{
				return createWorkflow(protocol);
			}
		});
	}

	@Override
	public WorkflowElement getWorkflowElement(Integer workflowStepId) throws WorkflowException
	{
		Protocol protocol;
		try
		{
			protocol = Protocol.findById(database, workflowStepId);
			if (protocol == null) throw new WorkflowException("Unknown workflow step [" + workflowStepId + "]");
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
		return createWorkflowStep(protocol);

	}

	@Override
	public WorkflowElementData getWorkflowElementData(Integer workflowElementId) throws WorkflowException
	{
		WorkflowElement workflowStep = getWorkflowElement(workflowElementId);

		Protocol protocol;
		try
		{
			protocol = Protocol.findById(database, workflowElementId);
			if (protocol == null) throw new WorkflowException("Unknown workflow step [" + workflowElementId + "]");
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}

		ValueConverter valueConverter = new ValueConverter(database);
		List<? extends ObservationSet> observationSets;
		try
		{
			List<DataSet> dataSets = database.find(DataSet.class, new QueryRule(DataSet.PROTOCOLUSED, Operator.EQUALS,
					protocol));
			if (dataSets == null || dataSets.size() != 1) throw new RuntimeException(
					"Workflow step must have exactly one data set");
			DataSet dataSet = dataSets.get(0);

			observationSets = ObservationSet.find(database, new QueryRule(ObservationSet.PARTOFDATASET,
					Operator.EQUALS, dataSet));
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}

		Map<Integer, Integer> featureIndex = new HashMap<Integer, Integer>();
		int i = 0;
		List<ObservableFeature> features = protocol.getFeatures();
		for (ObservableFeature workflowFeature : features)
		{
			featureIndex.put(workflowFeature.getId(), i++);
		}

		List<DataRow> dataMatrix = null;

		List<WorkflowFeature> inputFeatures = workflowStep.getInputFeatures();
		if (inputFeatures != null && !inputFeatures.isEmpty())
		{
			// case: input features
			// create rows with linked rows

			WorkflowFeature workflowFeature = inputFeatures.get(0);

			dataMatrix = new ArrayList<DataRow>();
			try
			{
				for (WorkflowElement inputStep : workflowStep.getInputWorkflowSteps())
				{
					Protocol inputprotocol = Protocol.findById(database, inputStep.getId());

					List<DataSet> inputDataSets = database.find(DataSet.class, new QueryRule(DataSet.PROTOCOLUSED,
							Operator.EQUALS, inputprotocol));
					if (inputDataSets == null || inputDataSets.size() != 1) throw new RuntimeException(
							"Workflow step must have exactly one data set");
					DataSet inputDataSet = inputDataSets.get(0);

					List<ObservationSet> inputObservationSets = database.find(ObservationSet.class, new QueryRule(
							ObservationSet.PARTOFDATASET, Operator.EQUALS, inputDataSet));

					for (ObservationSet inputObservationSet : inputObservationSets)
					{
						// get values for this row
						List<ObservedValue> values = database.find(ObservedValue.class, new QueryRule(
								ObservedValue.FEATURE, Operator.EQUALS, workflowFeature.getId()), new QueryRule(
								Operator.AND), new QueryRule(ObservedValue.OBSERVATIONSET, Operator.EQUALS,
								inputObservationSet));

						ObservedValue value = values.get(0); // FIXME support multiple values
						// get linked rows for this row
						List<DataRow> linkedDataRows = new ArrayList<DataRow>();
						List<ObservationSetFlow> observationSetFlows = database.find(ObservationSetFlow.class,
								new QueryRule(ObservationSetFlow.SOURCE, Operator.EQUALS, inputObservationSet));
						for (ObservationSetFlow observationSetFlow : observationSetFlows)
						{
							ObservationSet destinationRow = observationSetFlow.getDestination();
							linkedDataRows.add(createDataRow(destinationRow, features, featureIndex, valueConverter));
						}
						try
						{
							dataMatrix.add(new DataRow(Arrays.<Object> asList(valueConverter.toCell(value.getValue())
									.getValue()), linkedDataRows));
						}
						catch (ValueConverterException e)
						{
							throw new RuntimeException(e);
						}
					}
				}
			}
			catch (DatabaseException e)
			{
				throw new RuntimeException(e);
			}
		}
		else
		{
			// case: no input features
			// create rows without linked rows
			if (!features.isEmpty())
			{
				dataMatrix = new ArrayList<DataRow>(observationSets.size());
				for (ObservationSet observationSet : observationSets)
				{
					dataMatrix.add(createDataRow(observationSet, features, featureIndex, valueConverter));
				}
			}
			else
			{
				dataMatrix = Collections.emptyList();
			}
		}

		return new WorkflowElementData(dataMatrix);
	}

	private DataRow createDataRow(ObservationSet observationSet, List<ObservableFeature> features,
			Map<Integer, Integer> featureIndex, ValueConverter valueConverter)
	{
		int nrFeatures = features.size();
		List<Object> row = new ArrayList<Object>(nrFeatures);
		for (int j = 0; j < nrFeatures; ++j)
			row.add(null);
		List<ObservedValue> values;
		try
		{
			values = database.find(ObservedValue.class, new QueryRule(ObservedValue.FEATURE, Operator.IN, features),
					new QueryRule(Operator.AND), new QueryRule(ObservedValue.OBSERVATIONSET, Operator.EQUALS,
							observationSet));
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
		for (ObservedValue value : values)
		{
			try
			{
				row.set(featureIndex.get(value.getFeature().getId()), valueConverter.toCell(value.getValue())
						.getValue());
			}
			catch (ValueConverterException e)
			{
				throw new RuntimeException(e);
			}
		}
		return new DataRow(row);
	}

	private Workflow createWorkflow(Protocol protocol)
	{
		return new Workflow(protocol, Lists.transform(ProtocolUtils.getProtocolDescendants(protocol, false),
				new Function<Protocol, WorkflowElement>()
				{
					@Override
					public WorkflowElement apply(Protocol protocol)
					{
						return createWorkflowStep(protocol);
					}
				}));
	}

	private WorkflowElement createWorkflowStep(Protocol protocol)
	{
		List<ProtocolFlow> protocolFlows;
		try
		{
			protocolFlows = database.find(ProtocolFlow.class, new QueryRule(ProtocolFlow.DESTINATION_IDENTIFIER,
					Operator.EQUALS, protocol.getIdentifier()));
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
		return new WorkflowElement(protocol, protocolFlows);
	}
}
