package org.molgenis.omx.workflow;

import java.util.List;

import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class WorkflowStep
{
	private final Integer id;
	private final String name;
	private final List<WorkflowFeature> features;
	private final List<WorkflowFeature> inputFeatures;
	private final List<WorkflowStep> inputWorkflowSteps;

	public WorkflowStep(Protocol protocol)
	{
		this(protocol, null);
	}

	public WorkflowStep(Protocol protocol, List<ProtocolFlow> protocolFlows)
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
					return new WorkflowFeature(protocolFlow.getFeature());
				}
			});
			this.inputWorkflowSteps = Lists.transform(protocolFlows, new Function<ProtocolFlow, WorkflowStep>()
			{
				@Override
				public WorkflowStep apply(ProtocolFlow protocolFlow)
				{
					List<ProtocolFlow> protocolViews = null;
					return new WorkflowStep(protocolFlow.getSource(), protocolViews);
				}
			});
		}
		else
		{
			this.inputFeatures = null;
			this.inputWorkflowSteps = null;

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

	public List<WorkflowStep> getInputWorkflowSteps()
	{
		return inputWorkflowSteps;
	}
}
