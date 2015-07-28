package org.molgenis.omx.workflow;

public class WorkflowElementConnection
{
	private final Integer id;
	private final WorkflowElement inputElement;
	private final WorkflowElement outputElement;
	private final WorkflowFeature inputFeature;
	private final WorkflowFeature outputFeature;

	public WorkflowElementConnection(Integer id, WorkflowElement inputElement, WorkflowFeature inputFeature,
			WorkflowElement outputElement, WorkflowFeature outputFeature)
	{
		if (id == null) throw new IllegalArgumentException("Id is null");
		if (inputElement == null) throw new IllegalArgumentException("Input element is null");
		if (inputFeature == null) throw new IllegalArgumentException("Input feature is null");
		if (outputElement == null) throw new IllegalArgumentException("Output element is null");
		if (outputFeature == null) throw new IllegalArgumentException("Output feature is null");
		this.id = id;
		this.inputElement = inputElement;
		this.inputFeature = inputFeature;
		this.outputElement = outputElement;
		this.outputFeature = outputFeature;
	}

	public Integer getId()
	{
		return id;
	}

	public WorkflowElement getInputElement()
	{
		return inputElement;
	}

	public WorkflowFeature getInputFeature()
	{
		return inputFeature;
	}

	public WorkflowFeature getOutputFeature()
	{
		return outputFeature;
	}

	public WorkflowElement getOutputElement()
	{
		return outputElement;
	}

}
