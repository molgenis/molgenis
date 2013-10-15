package org.molgenis.omx.workflow;

import static org.molgenis.framework.db.QueryRule.Operator.EQUALS;
import static org.molgenis.framework.db.QueryRule.Operator.AND;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
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

	public void createWorkflowElementDataRowConnections(Integer workflowElementId,
			List<Integer> workflowElementDataRowIds)
	{
		try
		{
			// get data set for the given workflow element
			List<DataSet> dataSets = database.find(DataSet.class, new QueryRule(DataSet.PROTOCOLUSED, EQUALS,
					workflowElementId));
			if (dataSets == null || dataSets.size() != 1) throw new RuntimeException(
					"Workflow step must have exactly one data set");
			DataSet dataSet = dataSets.get(0);

			String observationSetIdentifier = UUID.randomUUID().toString();

			ObservationSet destinationObservationSet = new ObservationSet();
			destinationObservationSet.setIdentifier(observationSetIdentifier);
			destinationObservationSet.setPartOfDataSet(dataSet);
			database.add(destinationObservationSet);

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
			
			// create input values
			List<ProtocolFlow> protocolFlows = database.find(ProtocolFlow.class, new QueryRule(
					ProtocolFlow.DESTINATION, EQUALS, workflowElementId));
			if (protocolFlows != null)
			{
				for (ProtocolFlow protocolFlow : protocolFlows)
				{
					ObservableFeature inputFeature = protocolFlow.getInputFeature();
					ObservableFeature outputFeature = protocolFlow.getOutputFeature();
					
					for(ObservationSetFlow observationSetFlow : observationSetFlows) {
						database.find(ObservedValue.class, new QueryRule(ObservedValue.OBSERVATIONSET, EQUALS, observationSetFlow.getSource()), new QueryRule(AND), new QueryRule(ObservedValue.FEATURE, EQUALS, observationSetFlow.getSource()))
					}
					
				}
			}
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}

	}
}
