package org.molgenis.omx.workflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.converters.ValueConverter;
import org.molgenis.omx.converters.ValueConverterException;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class WorkflowElementDataRow
{
	private final Integer id;
	private final Map<Integer, Object> valueMap;
	private final List<WorkflowElementDataRowConnection> incomingElementDataRowConnections;
	private final List<WorkflowElementDataRowConnection> outgoingElementDataRowConnections;
	private final boolean completed;

	public WorkflowElementDataRow(ObservationSet observationSet, DataService dataService)
	{
		if (observationSet == null) throw new IllegalArgumentException("ObservationSet is null");
		if (dataService == null) throw new IllegalArgumentException("Database is null");
		this.id = observationSet.getId();
		this.valueMap = createValueMap(observationSet, dataService);
		this.incomingElementDataRowConnections = createElementDataRowConnections(observationSet,
				ObservationSetFlow.DESTINATION, dataService);
		this.outgoingElementDataRowConnections = createElementDataRowConnections(observationSet,
				ObservationSetFlow.SOURCE, dataService);
		this.completed = determineIsCompleted(observationSet, dataService);
	}

	public Integer getId()
	{
		return id;
	}

	public Map<Integer, Object> getValues()
	{
		return valueMap;
	}

	public Object getValue(Integer workflowFeatureId)
	{
		return valueMap.get(workflowFeatureId);
	}

	public List<WorkflowElementDataRowConnection> getIncomingElementDataRowConnections()
	{
		return incomingElementDataRowConnections;
	}

	public List<WorkflowElementDataRowConnection> getOutgoingElementDataRowConnections()
	{
		return outgoingElementDataRowConnections;
	}

	public boolean isCompleted()
	{
		return completed;
	}

	private boolean determineIsCompleted(ObservationSet observationSet, DataService dataService)
	{
		boolean isCompleted = true;
		List<ObservableFeature> features = observationSet.getPartOfDataSet().getProtocolUsed().getRequiredFeatures();

		// List<ObservableFeature> features = observationSet.getPartOfDataSet().getProtocolUsed().getFeatures();
		for (ObservableFeature feature : features)
		{

			long count = dataService
					.count(ObservedValue.ENTITY_NAME, new QueryImpl().eq(ObservedValue.OBSERVATIONSET, observationSet)
							.eq(ObservedValue.FEATURE, feature));

			if (count > 0)
			{
				isCompleted = false;
				break;
			}

		}
		return isCompleted;
	}

	private Map<Integer, Object> createValueMap(ObservationSet observationSet, DataService dataService)
	{
		Map<Integer, Object> valueMap = new HashMap<Integer, Object>();
		try
		{
			List<ObservedValue> observedValues = dataService.findAllAsList(ObservedValue.ENTITY_NAME,
					new QueryImpl().eq(ObservedValue.OBSERVATIONSET, observationSet));

			ValueConverter valueConverter = new ValueConverter(dataService);
			for (ObservedValue observedValue : observedValues)
			{
				Object value = valueConverter.toCell(observedValue.getValue()).getValue();
				valueMap.put(observedValue.getFeature().getId(), value);
			}
		}
		catch (ValueConverterException e)
		{
			throw new RuntimeException(e);
		}

		return valueMap;
	}

	private List<WorkflowElementDataRowConnection> createElementDataRowConnections(ObservationSet observationSet,
			String direction, final DataService dataService)
	{
		List<ObservationSetFlow> observationSetFlows = dataService.findAllAsList(ObservationSetFlow.ENTITY_NAME,
				new QueryImpl().eq(direction, observationSet));

		return Lists.transform(observationSetFlows,
				new Function<ObservationSetFlow, WorkflowElementDataRowConnection>()
				{
					@Override
					public WorkflowElementDataRowConnection apply(ObservationSetFlow observationSetFlow)
					{
						return new WorkflowElementDataRowConnection(observationSetFlow, dataService);
					}
				});
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		WorkflowElementDataRow other = (WorkflowElementDataRow) obj;
		if (id == null)
		{
			if (other.id != null) return false;
		}
		else if (!id.equals(other.id)) return false;
		return true;
	}
}