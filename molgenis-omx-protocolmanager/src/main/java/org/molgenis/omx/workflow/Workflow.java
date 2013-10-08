package org.molgenis.omx.workflow;

import java.util.Collections;
import java.util.List;

import org.molgenis.omx.observ.Protocol;

public class Workflow
{
	private final Integer id;
	private final String name;
	private final List<WorkflowStep> workflowSteps;

	public Workflow(Protocol protocol, List<WorkflowStep> workflowSteps)
	{
		if (protocol == null) throw new IllegalArgumentException("Protocol is null");
		if (workflowSteps == null) throw new IllegalArgumentException("WorkflowSteps is null");
		this.id = protocol.getId();
		this.name = protocol.getName();
		this.workflowSteps = workflowSteps;
	}

	public Integer getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public List<WorkflowStep> getSteps()
	{
		return Collections.unmodifiableList(workflowSteps);
	}
}
