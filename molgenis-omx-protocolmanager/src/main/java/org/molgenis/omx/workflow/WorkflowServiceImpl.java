package org.molgenis.omx.workflow;

import static org.molgenis.framework.db.QueryRule.Operator.AND;
import static org.molgenis.framework.db.QueryRule.Operator.EQUALS;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.omx.converters.ValueConverter;
import org.molgenis.omx.converters.ValueConverterException;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.value.MrefValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.observ.value.XrefValue;
import org.molgenis.omx.utils.ProtocolUtils;
import org.molgenis.util.tuple.KeyValueTuple;
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

	// TODO make transactional
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

	// TODO make transactional
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
	public WorkflowElement getWorkflowElement(Integer workflowElementId) throws WorkflowException
	{
		Protocol protocol;
		try
		{
			protocol = Protocol.findById(database, workflowElementId);
			if (protocol == null) throw new WorkflowException("Unknown workflow element [" + workflowElementId + "]");
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

	// TODO make transactional
	@Override
	public void deleteWorkflowElementDataRow(Integer workflowElementDataRowId) throws WorkflowException
	{
		try
		{
			ObservationSet observationSet = ObservationSet.findById(database, workflowElementDataRowId);
			if (observationSet == null)
			{
				throw new WorkflowException("Unknown workflow element data row [" + workflowElementDataRowId + "]");
			}

			List<ObservationSetFlow> sourceObservationSetFlows = database.find(ObservationSetFlow.class, new QueryRule(
					ObservationSetFlow.SOURCE, EQUALS, observationSet));
			if (sourceObservationSetFlows != null && !sourceObservationSetFlows.isEmpty())
			{
				// TODO decide if we want to do a recursive delete
				database.remove(sourceObservationSetFlows);
			}

			List<ObservationSetFlow> destinationObservationSetFlows = database.find(ObservationSetFlow.class,
					new QueryRule(ObservationSetFlow.DESTINATION, EQUALS, observationSet));
			if (destinationObservationSetFlows != null && !destinationObservationSetFlows.isEmpty())
			{
				database.remove(destinationObservationSetFlows);
			}

			List<ObservedValue> observedValues = database.find(ObservedValue.class, new QueryRule(
					ObservedValue.OBSERVATIONSET, EQUALS, observationSet));
			if (observedValues != null && !observedValues.isEmpty())
			{
				// TODO delete values
				// List<Value> values = new ArrayList<Value>();
				// for (ObservedValue observedValue : observedValues)
				// values.add(observedValue.getValue());

				database.remove(observedValues);
				// database.remove(values);
			}
			database.remove(observationSet);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
	}

	// TODO make transactional
	@Override
	public void createWorkflowElementDataRowWithConnections(Integer workflowElementId,
			List<Integer> workflowElementDataRowIds)
	{
		try
		{
			// get data set for the given workflow element
			DataSet dataSet = getDataSetForWorkFlowElement(workflowElementId);

			// create new observation set
			String observationSetIdentifier = UUID.randomUUID().toString();

			ObservationSet destinationObservationSet = new ObservationSet();
			destinationObservationSet.setIdentifier(observationSetIdentifier);
			destinationObservationSet.setPartOfDataSet(dataSet);
			database.add(destinationObservationSet);

			if (workflowElementDataRowIds != null && !workflowElementDataRowIds.isEmpty())
			{
				// create observation set connections to new observation set
				List<ObservationSetFlow> observationSetFlows = new ArrayList<ObservationSetFlow>();
				for (Integer workflowElementDataRowId : workflowElementDataRowIds)
				{
					ObservationSet sourceObservationSet = ObservationSet.findById(database, workflowElementDataRowId);
					ObservationSetFlow observationSetFlow = new ObservationSetFlow();
					observationSetFlow.setSource(sourceObservationSet);
					observationSetFlow.setDestination(destinationObservationSet);
					observationSetFlows.add(observationSetFlow);

				}
				database.add(observationSetFlows);

				// create values for output features
				List<ProtocolFlow> protocolFlows = database.find(ProtocolFlow.class, new QueryRule(
						ProtocolFlow.DESTINATION, EQUALS, workflowElementId));
				if (protocolFlows != null)
				{
					for (ProtocolFlow protocolFlow : protocolFlows)
					{
						ObservableFeature inputFeature = protocolFlow.getInputFeature();
						ObservableFeature outputFeature = protocolFlow.getOutputFeature();

						List<Value> outputValues = new ArrayList<Value>();
						for (ObservationSetFlow observationSetFlow : observationSetFlows)
						{
							List<ObservedValue> inputObservedValues = database.find(ObservedValue.class, new QueryRule(
									ObservedValue.OBSERVATIONSET, EQUALS, observationSetFlow.getSource()),
									new QueryRule(AND), new QueryRule(ObservedValue.FEATURE, EQUALS, inputFeature));
							if (inputObservedValues == null || inputObservedValues.isEmpty()) throw new RuntimeException(
									"missing value");
							else if (inputObservedValues.size() > 1) throw new RuntimeException(
									"expected exactly one value");
							outputValues.add(inputObservedValues.get(0).getValue());
						}

						ObservedValue outputObservedValue = new ObservedValue();
						outputObservedValue.setObservationSet(destinationObservationSet);
						outputObservedValue.setFeature(outputFeature);

						if (outputValues.isEmpty()) throw new RuntimeException("TODO check if this is a valid case");
						else if (outputValues.size() > 1)
						{
							MrefValue value = new MrefValue();
							value.setValue(Lists.transform(outputValues, new Function<Value, Characteristic>()
							{
								@Override
								public Characteristic apply(Value value)
								{
									if (!(value instanceof XrefValue)) throw new RuntimeException(
											new WorkflowException("Value must be of type XrefValue instead of "
													+ value.getClass().getSimpleName()));
									return ((XrefValue) value).getValue();

								}
							}));
							database.add(value);
							outputObservedValue.setValue(value);
						}
						else
						{
							outputObservedValue.setValue(outputValues.get(0));
						}
						database.add(outputObservedValue);
					}
				}
			}
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
	}

	// TODO make transactional
	@Override
	public void createOrUpdateWorkflowElementDataRowValue(Integer workflowElementDataRowId, Integer featureId,
			String rawValue)
	{
		try
		{
			ObservationSet observationSet = ObservationSet.findById(database, workflowElementDataRowId);
			ObservableFeature observableFeature = ObservableFeature.findById(database, featureId);
			List<ObservedValue> observedValues = database.find(ObservedValue.class, new QueryRule(
					ObservedValue.OBSERVATIONSET, EQUALS, observationSet), new QueryRule(AND), new QueryRule(
					ObservedValue.FEATURE, EQUALS, observableFeature));
			if (observedValues.size() > 1) throw new RuntimeException(
					"expected exactly one value for a row/column combination");

			String colName = "key";
			KeyValueTuple tuple = new KeyValueTuple();
			tuple.set(colName, rawValue);

			if (observedValues == null || observedValues.isEmpty())
			{
				if (observableFeature.getDataType().equalsIgnoreCase(FieldTypeEnum.XREF.toString())
						|| observableFeature.getDataType().equalsIgnoreCase(FieldTypeEnum.MREF.toString()))
				{
					String characteristicIdentifier = UUID.randomUUID().toString();

					Characteristic characteristic = new Characteristic();
					characteristic.setIdentifier(characteristicIdentifier);
					characteristic.setName(rawValue);
					database.add(characteristic);

					tuple.set(colName, characteristicIdentifier);
				}

				Value value;
				try
				{
					value = new ValueConverter(database).fromTuple(tuple, colName, observableFeature);
				}
				catch (ValueConverterException e)
				{
					throw new RuntimeException(e);
				}

				ObservedValue observedValue = new ObservedValue();
				observedValue.setObservationSet(observationSet);
				observedValue.setFeature(observableFeature);
				observedValue.setValue(value);
				database.add(observedValue);
			}
			else
			{
				Value value = observedValues.get(0).getValue();
				if (observableFeature.getDataType().equalsIgnoreCase(FieldTypeEnum.XREF.toString()))
				{
					Characteristic characteristic = ((XrefValue) value).getValue();
					characteristic.setName(rawValue);
					database.update(characteristic);
				}
				else
				{
					try
					{
						new ValueConverter(database).updateFromTuple(tuple, colName, observableFeature, value);
					}
					catch (ValueConverterException e)
					{
						throw new RuntimeException(e);
					}
					database.update(value);
				}
			}

		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}

	}

	private DataSet getDataSetForWorkFlowElement(Integer workflowElementId) throws DatabaseException
	{
		// get data set for the given workflow element
		List<DataSet> dataSets = database.find(DataSet.class, new QueryRule(DataSet.PROTOCOLUSED, EQUALS,
				workflowElementId));
		if (dataSets == null || dataSets.size() != 1) throw new RuntimeException(
				"Workflow element must have exactly one data set");
		return dataSets.get(0);
	}
}
