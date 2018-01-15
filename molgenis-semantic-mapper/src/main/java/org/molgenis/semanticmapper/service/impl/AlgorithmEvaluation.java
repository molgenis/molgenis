package org.molgenis.semanticmapper.service.impl;

import org.molgenis.data.Entity;

public class AlgorithmEvaluation
{
	private final Entity entity;
	private Object value;
	private String errorMessage;

	public AlgorithmEvaluation(Entity entity)
	{
		this.entity = entity;
	}

	public Entity getEntity()
	{
		return entity;
	}

	public boolean hasValue()
	{
		return errorMessage == null;
	}

	public Object getValue()
	{
		return value;
	}

	public void setValue(Object value)
	{
		this.value = value;
	}

	public AlgorithmEvaluation value(Object value)
	{
		this.value = value;
		return this;
	}

	public boolean hasError()
	{
		return errorMessage != null;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	public AlgorithmEvaluation errorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
		return this;
	}
}
