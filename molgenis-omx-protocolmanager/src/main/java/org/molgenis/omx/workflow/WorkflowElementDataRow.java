package org.molgenis.omx.workflow;

import static org.molgenis.framework.db.QueryRule.Operator.AND;
import static org.molgenis.framework.db.QueryRule.Operator.EQUALS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
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

	public WorkflowElementDataRow(ObservationSet observationSet, Database database) throws DatabaseException
	{
		if (observationSet == null) throw new IllegalArgumentException("ObservationSet is null");
		if (database == null) throw new IllegalArgumentException("Database is null");
		this.id = observationSet.getId();
		this.valueMap = createValueMap(observationSet, database);
		this.incomingElementDataRowConnections = createElementDataRowConnections(observationSet,
				ObservationSetFlow.DESTINATION, database);
		this.outgoingElementDataRowConnections = createElementDataRowConnections(observationSet,
				ObservationSetFlow.SOURCE, database);
		this.completed = determineIsCompleted(observationSet, database);
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

	private boolean determineIsCompleted(ObservationSet observationSet, Database database)
	{
		boolean isCompleted = true;
		List<ObservableFeature> features = observationSet.getPartOfDataSet().getProtocolUsed().getRequiredFeatures();

		// List<ObservableFeature> features = observationSet.getPartOfDataSet().getProtocolUsed().getFeatures();
		for (ObservableFeature feature : features)
		{
			try
			{
				List<ObservedValue> observedValues = database.find(ObservedValue.class, new QueryRule(
						ObservedValue.OBSERVATIONSET, EQUALS, observationSet), new QueryRule(AND), new QueryRule(
						ObservedValue.FEATURE, EQUALS, feature));
				if (observedValues == null || observedValues.isEmpty())
				{
					isCompleted = false;
					break;
				}
			}
			catch (DatabaseException e)
			{
				throw new RuntimeException(e);
			}
		}
		return isCompleted;
	}

	private Map<Integer, Object> createValueMap(ObservationSet observationSet, Database database)
	{
		Map<Integer, Object> valueMap = new HashMap<Integer, Object>();
		try
		{
			List<ObservedValue> observedValues = database.find(ObservedValue.class, new QueryRule(
					ObservedValue.OBSERVATIONSET, EQUALS, observationSet));

			if (observedValues != null)
			{
				ValueConverter valueConverter = new ValueConverter(database);
				for (ObservedValue observedValue : observedValues)
				{
					Object value = valueConverter.toCell(observedValue.getValue()).getValue();
					valueMap.put(observedValue.getFeature().getId(), value);
				}
			}
		}
		catch (ValueConverterException e)
		{
			throw new RuntimeException(e);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
		return valueMap;
	}

	private List<WorkflowElementDataRowConnection> createElementDataRowConnections(ObservationSet observationSet,
			String direction, final Database database) throws DatabaseException
	{
		List<ObservationSetFlow> observationSetFlows = database.find(ObservationSetFlow.class, new QueryRule(direction,
				EQUALS, observationSet));

		return observationSetFlows != null ? Lists.transform(observationSetFlows,
				new Function<ObservationSetFlow, WorkflowElementDataRowConnection>()
				{
					@Override
					public WorkflowElementDataRowConnection apply(ObservationSetFlow observationSetFlow)
					{
						try
						{
							return new WorkflowElementDataRowConnection(observationSetFlow, database);
						}
						catch (DatabaseException e)
						{
							throw new RuntimeException(e);
						}
					}
				}) : null;
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