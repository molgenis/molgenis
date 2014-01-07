package org.molgenis.omx.workflow;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
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

	public WorkflowElement(final Protocol protocol, DataService dataService) throws WorkflowException
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
		this.elementConnections = createElementConnections(protocol, dataService);
		this.workflowElementData = createWorkflowElementData(protocol, dataService);
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

	private List<WorkflowElementConnection> createElementConnections(Protocol protocol, final DataService dataService)
	{
		List<ProtocolFlow> protocolFlows = dataService.findAllAsList(ProtocolFlow.ENTITY_NAME,
				new QueryImpl().eq(ProtocolFlow.DESTINATION, protocol));

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
							inputElement = new WorkflowElement(protocolFlow.getSource(), dataService);
							outputElement = new WorkflowElement(protocolFlow.getDestination(), dataService);
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

	private WorkflowElementData createWorkflowElementData(Protocol protocol, DataService dataService)
			throws WorkflowException
	{
		;
		List<DataSet> dataSets = dataService.findAllAsList(DataSet.ENTITY_NAME,
				new QueryImpl().eq(DataSet.PROTOCOLUSED, protocol));

		if (dataSets.size() != 1) throw new RuntimeException("Workflow step must have exactly one data set");
		DataSet dataSet = dataSets.get(0);

		List<? extends ObservationSet> observationSets = dataService.findAllAsList(ObservationSet.ENTITY_NAME,
				new QueryImpl().eq(ObservationSet.PARTOFDATASET, dataSet));

		List<WorkflowElementDataRow> dataMatrix = new ArrayList<WorkflowElementDataRow>();

		for (ObservationSet observationSet : observationSets)
			dataMatrix.add(new WorkflowElementDataRow(observationSet, dataService));

		return new WorkflowElementData(dataMatrix);
	}
}
