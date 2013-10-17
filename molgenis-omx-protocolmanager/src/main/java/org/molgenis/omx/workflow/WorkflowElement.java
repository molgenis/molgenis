package org.molgenis.omx.workflow;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.Protocol;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class WorkflowElement
{
	private final Integer id;
	private final String name;
	private transient final List<WorkflowFeature> features;
	private transient final List<WorkflowElementConnection> elementConnections;
	private transient final WorkflowElementData workflowElementData;

	public WorkflowElement(final Protocol protocol, Database database) throws WorkflowException
	{
		if (protocol == null) throw new IllegalArgumentException("Protocol is null");
		this.id = protocol.getId();
		this.name = protocol.getName();
		this.features = Lists.transform(protocol.getFeatures(), new Function<ObservableFeature, WorkflowFeature>()
		{
			@Override
			public WorkflowFeature apply(ObservableFeature feature)
			{
				return new WorkflowFeature(feature, protocol);
			}
		});
		this.elementConnections = createElementConnections(protocol, database);
		this.workflowElementData = createWorkflowElementData(protocol, database);
	}

	public Integer getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public List<WorkflowFeature> getFeatures()
	{
		return features;
	}

	public List<WorkflowElementConnection> getElementConnections()
	{
		return elementConnections;
	}

	public WorkflowElementData getWorkflowElementData()
	{
		return workflowElementData;
	}

	private List<WorkflowElementConnection> createElementConnections(Protocol protocol, final Database database)
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

		return protocolFlows != null ? Lists.transform(protocolFlows,
				new Function<ProtocolFlow, WorkflowElementConnection>()
				{
					@Override
					public WorkflowElementConnection apply(ProtocolFlow protocolFlow)
					{
						WorkflowElement inputElement;
						WorkflowElement outputElement;
						try
						{
							inputElement = new WorkflowElement(protocolFlow.getSource(), database);
							outputElement = new WorkflowElement(protocolFlow.getDestination(), database);
						}
						catch (WorkflowException e)
						{
							throw new RuntimeException(e);
						}

						WorkflowFeature inputFeature = new WorkflowFeature(protocolFlow.getInputFeature(), protocolFlow
								.getSource());
						WorkflowFeature outputFeature = new WorkflowFeature(protocolFlow.getOutputFeature(),
								protocolFlow.getDestination());
						return new WorkflowElementConnection(protocolFlow.getId(), inputElement, inputFeature,
								outputElement, outputFeature);
					}
				}) : null;
	}

	private WorkflowElementData createWorkflowElementData(Protocol protocol, Database database)
			throws WorkflowException
	{
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

		List<WorkflowElementDataRow> dataMatrix = new ArrayList<WorkflowElementDataRow>();
		try
		{
			for (ObservationSet observationSet : observationSets)
				dataMatrix.add(new WorkflowElementDataRow(observationSet, database));
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}

		return new WorkflowElementData(dataMatrix);
	}
}
