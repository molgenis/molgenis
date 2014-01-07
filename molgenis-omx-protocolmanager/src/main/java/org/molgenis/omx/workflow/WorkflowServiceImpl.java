package org.molgenis.omx.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

@Service
public class WorkflowServiceImpl implements WorkflowService
{
	private final DataService dataService;

	@Autowired
	public WorkflowServiceImpl(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		this.dataService = dataService;
	}

	// TODO make transactional
	@Override
	public Workflow getWorkflow(Integer workflowId) throws WorkflowException
	{
		Protocol protocol = dataService.findOne(Protocol.ENTITY_NAME, workflowId);
		if (protocol == null) throw new WorkflowException("Unknown workflow [" + workflowId + "]");

		return createWorkflow(protocol);
	}

	// TODO make transactional
	@Override
	public List<Workflow> getWorkflows()
	{
		List<Protocol> workflows = dataService.findAllAsList(Protocol.ENTITY_NAME,
				new QueryImpl().eq(Protocol.ROOT, true));
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
		Protocol protocol = dataService.findOne(Protocol.ENTITY_NAME, workflowElementId);
		if (protocol == null) throw new WorkflowException("Unknown workflow element [" + workflowElementId + "]");

		return new WorkflowElement(protocol, dataService);

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
							return new WorkflowElement(protocol, dataService);
						}
						catch (WorkflowException e)
						{
							throw new RuntimeException(e);
						}
					}
				}));
	}

	@Override
	@Transactional
	public void deleteWorkflowElementDataRow(Integer workflowElementDataRowId) throws WorkflowException
	{
		ObservationSet observationSet = dataService.findOne(ObservationSet.ENTITY_NAME, workflowElementDataRowId);
		if (observationSet == null)
		{
			throw new WorkflowException("Unknown workflow element data row [" + workflowElementDataRowId + "]");
		}

		List<ObservationSetFlow> sourceObservationSetFlows = dataService.findAllAsList(ObservationSetFlow.ENTITY_NAME,
				new QueryImpl().eq(ObservationSetFlow.SOURCE, observationSet));
		if (!sourceObservationSetFlows.isEmpty())
		{
			// TODO decide if we want to do a recursive delete
			dataService.delete(ObservationSetFlow.ENTITY_NAME, sourceObservationSetFlows);
		}

		List<ObservationSetFlow> destinationObservationSetFlows = dataService.findAllAsList(
				ObservationSetFlow.ENTITY_NAME, new QueryImpl().eq(ObservationSetFlow.DESTINATION, observationSet));
		if (!destinationObservationSetFlows.isEmpty())
		{
			dataService.delete(ObservationSetFlow.ENTITY_NAME, destinationObservationSetFlows);
		}

		List<ObservedValue> observedValues = dataService.findAllAsList(ObservedValue.ENTITY_NAME,
				new QueryImpl().eq(ObservedValue.OBSERVATIONSET, observationSet));
		if (!observedValues.isEmpty())
		{
			// TODO delete values
			// List<Value> values = new ArrayList<Value>();
			// for (ObservedValue observedValue : observedValues)
			// values.add(observedValue.getValue());

			dataService.delete(ObservedValue.ENTITY_NAME, observedValues);
			// database.remove(values);
		}
		dataService.delete(ObservationSet.ENTITY_NAME, observationSet);

	}

	@Transactional
	@Override
	public void createWorkflowElementDataRowWithConnections(Integer workflowElementId,
			List<Integer> workflowElementDataRowIds)
	{

		// get data set for the given workflow element
		DataSet dataSet = getDataSetForWorkFlowElement(workflowElementId);

		// create new observation set
		String observationSetIdentifier = UUID.randomUUID().toString();

		ObservationSet destinationObservationSet = new ObservationSet();
		destinationObservationSet.setIdentifier(observationSetIdentifier);
		destinationObservationSet.setPartOfDataSet(dataSet);
		dataService.add(ObservationSet.ENTITY_NAME, destinationObservationSet);

		if (workflowElementDataRowIds != null && !workflowElementDataRowIds.isEmpty())
		{
			// create observation set connections to new observation set
			List<ObservationSetFlow> observationSetFlows = new ArrayList<ObservationSetFlow>();
			for (Integer workflowElementDataRowId : workflowElementDataRowIds)
			{
				ObservationSet sourceObservationSet = dataService.findOne(ObservationSet.ENTITY_NAME,
						workflowElementDataRowId);
				ObservationSetFlow observationSetFlow = new ObservationSetFlow();
				observationSetFlow.setSource(sourceObservationSet);
				observationSetFlow.setDestination(destinationObservationSet);
				observationSetFlows.add(observationSetFlow);

			}
			dataService.add(ObservationSetFlow.ENTITY_NAME, observationSetFlows);

			// create values for output features
			Protocol protocol = dataService.findOne(Protocol.ENTITY_NAME, workflowElementId);
			List<ProtocolFlow> protocolFlows = dataService.findAllAsList(ProtocolFlow.ENTITY_NAME,
					new QueryImpl().eq(ProtocolFlow.DESTINATION, protocol));

			for (ProtocolFlow protocolFlow : protocolFlows)
			{
				ObservableFeature inputFeature = protocolFlow.getInputFeature();
				ObservableFeature outputFeature = protocolFlow.getOutputFeature();

				List<Value> outputValues = new ArrayList<Value>();
				for (ObservationSetFlow observationSetFlow : observationSetFlows)
				{
					List<ObservedValue> inputObservedValues = dataService.findAllAsList(
							ObservedValue.ENTITY_NAME,
							new QueryImpl().eq(ObservedValue.OBSERVATIONSET, observationSetFlow.getSource()).and()
									.eq(ObservedValue.FEATURE, inputFeature));

					if (inputObservedValues.isEmpty()) throw new RuntimeException("missing value");
					else if (inputObservedValues.size() > 1) throw new RuntimeException("expected exactly one value");
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
							if (!(value instanceof XrefValue)) throw new RuntimeException(new WorkflowException(
									"Value must be of type XrefValue instead of " + value.getClass().getSimpleName()));
							return ((XrefValue) value).getValue();

						}
					}));
					dataService.add(MrefValue.ENTITY_NAME, value);
					outputObservedValue.setValue(value);
				}
				else
				{
					outputObservedValue.setValue(outputValues.get(0));
				}
				dataService.add(ObservedValue.ENTITY_NAME, outputObservedValue);

			}
		}

	}

	@Transactional
	@Override
	public void createOrUpdateWorkflowElementDataRowValue(Integer workflowElementDataRowId, Integer featureId,
			String rawValue)
	{
		ObservationSet observationSet = dataService.findOne(ObservationSet.ENTITY_NAME, workflowElementDataRowId);
		ObservableFeature observableFeature = dataService.findOne(ObservableFeature.ENTITY_NAME, featureId);

		List<ObservedValue> observedValues = dataService.findAllAsList(
				ObservedValue.ENTITY_NAME,
				new QueryImpl().eq(ObservedValue.OBSERVATIONSET, observationSet).and()
						.eq(ObservedValue.FEATURE, observableFeature));

		String colName = "key";
		Entity entity = new MapEntity(colName, rawValue);

		if (observedValues.isEmpty())
		{
			if (observableFeature.getDataType().equalsIgnoreCase(FieldTypeEnum.XREF.toString())
					|| observableFeature.getDataType().equalsIgnoreCase(FieldTypeEnum.MREF.toString())
					|| observableFeature.getDataType().equalsIgnoreCase(FieldTypeEnum.INT.toString())
					|| observableFeature.getDataType().equalsIgnoreCase(FieldTypeEnum.DATE.toString())
					|| observableFeature.getDataType().equalsIgnoreCase(FieldTypeEnum.BOOL.toString()))
			{
				String characteristicIdentifier = UUID.randomUUID().toString();

				Characteristic characteristic = new Characteristic();
				characteristic.setIdentifier(characteristicIdentifier);
				characteristic.setName(rawValue);
				dataService.add(Characteristic.ENTITY_NAME, characteristic);

				entity.set(colName, characteristicIdentifier);
			}

			Value value;
			try
			{
				value = new ValueConverter(dataService).fromEntity(entity, colName, observableFeature);
			}
			catch (ValueConverterException e)
			{
				throw new RuntimeException(e);
			}
			dataService.add(Value.ENTITY_NAME, value);

			ObservedValue observedValue = new ObservedValue();
			observedValue.setObservationSet(observationSet);
			observedValue.setFeature(observableFeature);
			observedValue.setValue(value);
			dataService.add(ObservedValue.ENTITY_NAME, observedValue);
		}
		else if (observedValues.size() > 1) throw new RuntimeException(
				"expected exactly one value for a row/column combination");
		else
		{
			Value value = observedValues.get(0).getValue();
			if (observableFeature.getDataType().equalsIgnoreCase(FieldTypeEnum.XREF.toString()))
			{
				Characteristic characteristic = ((XrefValue) value).getValue();
				characteristic.setName(rawValue);
				dataService.update(Characteristic.ENTITY_NAME, characteristic);
			}
			else
			{
				try
				{
					new ValueConverter(dataService).updateFromEntity(entity, colName, observableFeature, value);
				}
				catch (ValueConverterException e)
				{
					throw new RuntimeException(e);
				}
				dataService.update(Value.ENTITY_NAME, value);
			}
		}

	}

	private DataSet getDataSetForWorkFlowElement(Integer workflowElementId)
	{
		// get data set for the given workflow element
		Protocol protocol = dataService.findOne(Protocol.ENTITY_NAME, workflowElementId);
		List<DataSet> dataSets = dataService.findAllAsList(DataSet.ENTITY_NAME,
				new QueryImpl().eq(DataSet.PROTOCOLUSED, protocol));

		if (dataSets.size() != 1) throw new RuntimeException("Workflow element must have exactly one data set");
		return dataSets.get(0);
	}
}
