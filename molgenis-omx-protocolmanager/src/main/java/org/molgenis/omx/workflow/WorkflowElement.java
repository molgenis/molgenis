package org.molgenis.omx.workflow;

import java.util.List;

import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class WorkflowElement
{
	private final Integer id;
	private final String name;
	private final List<WorkflowFeature> features;
	private final List<WorkflowFeature> inputFeatures;
	private final List<WorkflowFeature> outputFeatures;
	private final List<WorkflowElement> inputWorkflowElements;

	public WorkflowElement(Protocol protocol)
	{
		this(protocol, null);
	}

	public WorkflowElement(Protocol protocol, List<ProtocolFlow> protocolFlows)
	{
		if (protocol == null) throw new IllegalArgumentException("Protocol is null");
		this.id = protocol.getId();
		this.name = protocol.getName();
		this.features = Lists.transform(protocol.getFeatures(), new Function<ObservableFeature, WorkflowFeature>()
		{
			@Override
			public WorkflowFeature apply(ObservableFeature feature)
			{
				return new WorkflowFeature(feature);
			}
		});

		if (protocolFlows != null)
		{
			this.inputFeatures = Lists.transform(protocolFlows, new Function<ProtocolFlow, WorkflowFeature>()
			{
				@Override
				public WorkflowFeature apply(ProtocolFlow protocolFlow)
				{
					return new WorkflowFeature(protocolFlow.getInputFeature());
				}
			});
			this.outputFeatures = Lists.transform(protocolFlows, new Function<ProtocolFlow, WorkflowFeature>()
			{
				@Override
				public WorkflowFeature apply(ProtocolFlow protocolFlow)
				{
					return new WorkflowFeature(protocolFlow.getOutputFeature());
				}
			});
			this.inputWorkflowElements = Lists.transform(protocolFlows, new Function<ProtocolFlow, WorkflowElement>()
			{
				@Override
				public WorkflowElement apply(ProtocolFlow protocolFlow)
				{
					List<ProtocolFlow> protocolViews = null;
					return new WorkflowElement(protocolFlow.getSource(), protocolViews);
				}
			});
		}
		else
		{
			this.inputFeatures = null;
			this.outputFeatures = null;
			this.inputWorkflowElements = null;
		}
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

	public List<WorkflowFeature> getInputFeatures()
	{
		return inputFeatures;
	}

	public List<WorkflowFeature> getOutputFeatures()
	{
		return outputFeatures;
	}

	public List<WorkflowElement> getInputWorkflowElements()
	{
		return inputWorkflowElements;
	}
}
