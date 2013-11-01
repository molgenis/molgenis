package org.molgenis.omx.workflow;

import java.util.Collections;
import java.util.List;

import org.molgenis.omx.observ.Protocol;

public class Workflow
{
	private final Integer id;
	private final String name;
	private final List<WorkflowElement> workflowElements;

	public Workflow(Protocol protocol, List<WorkflowElement> workflowElements)
	{
		if (protocol == null) throw new IllegalArgumentException("Protocol is null");
		if (workflowElements == null) throw new IllegalArgumentException("WorkflowElements is null");
		this.id = protocol.getId();
		this.name = protocol.getName();
		this.workflowElements = workflowElements;
	}

	public Integer getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public List<WorkflowElement> getElements()
	{
		return Collections.unmodifiableList(workflowElements);
	}
}
