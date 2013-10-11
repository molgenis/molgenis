package org.molgenis.omx.workflow;

import static org.molgenis.framework.db.QueryRule.Operator.EQUALS;

import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
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
		return new WorkflowElement(protocol, database);

	}

	private Workflow createWorkflow(Protocol protocol)
	{
		return new Workflow(protocol, Lists.transform(ProtocolUtils.getProtocolDescendants(protocol, false),
				new Function<Protocol, WorkflowElement>()
				{
					@Override
					public WorkflowElement apply(Protocol protocol)
					{
						try
						{
							return new WorkflowElement(protocol, database);
						}
						catch (WorkflowException e)
						{
							throw new RuntimeException(e);
						}
					}
				}));
	}

	// private WorkflowElement createWorkflowElement(Protocol protocol) throws WorkflowException
	// {
	// List<ProtocolFlow> protocolFlows;
	// try
	// {
	// protocolFlows = database.find(ProtocolFlow.class, new QueryRule(ProtocolFlow.DESTINATION_IDENTIFIER,
	// Operator.EQUALS, protocol.getIdentifier()));
	// }
	// catch (DatabaseException e)
	// {
	// throw new RuntimeException(e);
	// }
	// return new WorkflowElement(protocol, database);
	// }

	// private WorkflowElementData createWorkflowElementData(Protocol protocol) throws WorkflowException
	// {
	// try
	// {
	// protocol = Protocol.findById(database, protocol.getId());
	// if (protocol == null) throw new WorkflowException("Unknown workflow step [" + protocol.getId() + "]");
	// }
	// catch (DatabaseException e)
	// {
	// throw new RuntimeException(e);
	// }
	//
	// ValueConverter valueConverter = new ValueConverter(database);
	// List<? extends ObservationSet> observationSets;
	// try
	// {
	// List<DataSet> dataSets = database.find(DataSet.class, new QueryRule(DataSet.PROTOCOLUSED, Operator.EQUALS,
	// protocol));
	// if (dataSets == null || dataSets.size() != 1) throw new RuntimeException(
	// "Workflow step must have exactly one data set");
	// DataSet dataSet = dataSets.get(0);
	//
	// observationSets = ObservationSet.find(database, new QueryRule(ObservationSet.PARTOFDATASET,
	// Operator.EQUALS, dataSet));
	// }
	// catch (DatabaseException e)
	// {
	// throw new RuntimeException(e);
	// }
	//
	// List<WorkflowElementDataRow> dataMatrix = new ArrayList<WorkflowElementDataRow>();
	// try
	// {
	// for (ObservationSet observationSet : observationSets)
	// {
	// // get connections for this row
	// List<ObservationSetFlow> observationSetFlows = database.find(ObservationSetFlow.class, new QueryRule(
	// ObservationSetFlow.DESTINATION, Operator.EQUALS, observationSet));
	//
	// dataMatrix.add(new WorkflowElementDataRow(observationSet, observationSetFlows, database));
	// }
	// }
	// catch (DatabaseException e)
	// {
	// throw new RuntimeException(e);
	// }
	//
	// return new WorkflowElementData(dataMatrix);
	// }

	// @Override
	// public WorkflowElementData getWorkflowElementData(Integer workflowElementId) throws WorkflowException
	// {
	// WorkflowElement workflowElement = getWorkflowElement(workflowElementId);

	// Map<Integer, Integer> featureIndex = new HashMap<Integer, Integer>();
	// int i = 0;
	// List<ObservableFeature> features = protocol.getFeatures();
	// for (ObservableFeature workflowFeature : features)
	// {
	// featureIndex.put(workflowFeature.getId(), i++);
	// }
	//
	// List<WorkflowDataRow> dataMatrix = null;
	//
	// List<WorkflowElementConnection> elementConnections = workflowElement.getElementConnections();
	// if (elementConnections != null && !elementConnections.isEmpty())
	// {
	// Protocol inputprotocol = Protocol.findById(database, inputStep.getId());
	//
	// List<DataSet> inputDataSets = database.find(DataSet.class, new QueryRule(DataSet.PROTOCOLUSED,
	// Operator.EQUALS, inputprotocol));
	// if (inputDataSets == null || inputDataSets.size() != 1) throw new RuntimeException(
	// "Workflow step must have exactly one data set");
	// DataSet inputDataSet = inputDataSets.get(0);
	//
	// List<ObservationSet> inputObservationSets = database.find(ObservationSet.class, new QueryRule(
	// ObservationSet.PARTOFDATASET, Operator.EQUALS, inputDataSet));
	//
	// for (ObservationSet inputObservationSet : inputObservationSets)
	// {
	// // get values for this row
	// List<ObservedValue> values = database.find(ObservedValue.class, new QueryRule(ObservedValue.FEATURE,
	// Operator.EQUALS, workflowFeature.getId()), new QueryRule(Operator.AND), new QueryRule(
	// ObservedValue.OBSERVATIONSET, Operator.EQUALS, inputObservationSet));
	//
	// ObservedValue value = values.get(0); // FIXME support multiple values
	// // get linked rows for this row
	// List<WorkflowDataRow> linkedDataRows = new ArrayList<WorkflowDataRow>();
	// List<ObservationSetFlow> observationSetFlows = database.find(ObservationSetFlow.class, new QueryRule(
	// ObservationSetFlow.SOURCE, Operator.EQUALS, inputObservationSet));
	// for (ObservationSetFlow observationSetFlow : observationSetFlows)
	// {
	// ObservationSet destinationRow = observationSetFlow.getDestination();
	// linkedDataRows.add(createDataRow(destinationRow, features, featureIndex, valueConverter));
	// }
	// try
	// {
	// dataMatrix.add(new WorkflowDataRow(Arrays.<Object> asList(valueConverter.toCell(value.getValue())
	// .getValue())));
	// }
	// catch (ValueConverterException e)
	// {
	// throw new RuntimeException(e);
	// }
	// }
	//
	// // case: input features
	// // create rows with linked rows
	//
	// WorkflowFeature workflowFeature = elementConnections.get(0);
	//
	// dataMatrix = new ArrayList<WorkflowDataRow>();
	// try
	// {
	// for (WorkflowElement inputStep : workflowElement.getInputWorkflowElements())
	// {
	// Protocol inputprotocol = Protocol.findById(database, inputStep.getId());
	//
	// List<DataSet> inputDataSets = database.find(DataSet.class, new QueryRule(DataSet.PROTOCOLUSED,
	// Operator.EQUALS, inputprotocol));
	// if (inputDataSets == null || inputDataSets.size() != 1) throw new RuntimeException(
	// "Workflow step must have exactly one data set");
	// DataSet inputDataSet = inputDataSets.get(0);
	//
	// List<ObservationSet> inputObservationSets = database.find(ObservationSet.class, new QueryRule(
	// ObservationSet.PARTOFDATASET, Operator.EQUALS, inputDataSet));
	//
	// for (ObservationSet inputObservationSet : inputObservationSets)
	// {
	// // get values for this row
	// List<ObservedValue> values = database.find(ObservedValue.class, new QueryRule(
	// ObservedValue.FEATURE, Operator.EQUALS, workflowFeature.getId()), new QueryRule(
	// Operator.AND), new QueryRule(ObservedValue.OBSERVATIONSET, Operator.EQUALS,
	// inputObservationSet));
	//
	// ObservedValue value = values.get(0); // FIXME support multiple values
	// // get linked rows for this row
	// List<WorkflowDataRow> linkedDataRows = new ArrayList<WorkflowDataRow>();
	// List<ObservationSetFlow> observationSetFlows = database.find(ObservationSetFlow.class,
	// new QueryRule(ObservationSetFlow.SOURCE, Operator.EQUALS, inputObservationSet));
	// for (ObservationSetFlow observationSetFlow : observationSetFlows)
	// {
	// ObservationSet destinationRow = observationSetFlow.getDestination();
	// linkedDataRows.add(createDataRow(destinationRow, features, featureIndex, valueConverter));
	// }
	// try
	// {
	// dataMatrix.add(new WorkflowDataRow(Arrays.<Object> asList(valueConverter.toCell(
	// value.getValue()).getValue())));
	// }
	// catch (ValueConverterException e)
	// {
	// throw new RuntimeException(e);
	// }
	// }
	// }
	// }
	// catch (DatabaseException e)
	// {
	// throw new RuntimeException(e);
	// }
	// }
	// else
	// {
	// // case: no input features
	// // create rows without linked rows
	// if (!features.isEmpty())
	// {
	// dataMatrix = new ArrayList<WorkflowDataRow>(observationSets.size());
	// for (ObservationSet observationSet : observationSets)
	// {
	// dataMatrix.add(createDataRow(observationSet, features, featureIndex, valueConverter));
	// }
	// }
	// else
	// {
	// dataMatrix = Collections.emptyList();
	// }
	// }

	// return new WorkflowElementData(dataMatrix);
	// }

	// private WorkflowDataRow createDataRow(ObservationSet observationSet, List<ObservableFeature> features,
	// Map<Integer, Integer> featureIndex, ValueConverter valueConverter)
	// {
	// int nrFeatures = features.size();
	// List<Object> row = new ArrayList<Object>(nrFeatures);
	// for (int j = 0; j < nrFeatures; ++j)
	// row.add(null);
	// List<ObservedValue> values;
	// try
	// {
	// values = database.find(ObservedValue.class, new QueryRule(ObservedValue.FEATURE, Operator.IN, features),
	// new QueryRule(Operator.AND), new QueryRule(ObservedValue.OBSERVATIONSET, Operator.EQUALS,
	// observationSet));
	// }
	// catch (DatabaseException e)
	// {
	// throw new RuntimeException(e);
	// }
	// for (ObservedValue value : values)
	// {
	// try
	// {
	// row.set(featureIndex.get(value.getFeature().getId()), valueConverter.toCell(value.getValue())
	// .getValue());
	// }
	// catch (ValueConverterException e)
	// {
	// throw new RuntimeException(e);
	// }
	// }
	// return new WorkflowDataRow(row);
	// }
}
