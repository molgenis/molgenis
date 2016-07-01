package org.molgenis.data.annotation.entity.impl.gavin;

/**
 * Judgment result of the gavin method
 */
public class Judgment
{
	public enum Classification
	{
		Benign, Pathognic, VOUS
	}

	public enum Method
	{
		calibrated, genomewide
	}

	String reason;
	Classification classification;
	Method method;

	public Judgment(Classification classification, Method method, String reason)
	{
		this.reason = reason;
		this.classification = classification;
		this.method = method;
	}

	public String getReason()
	{
		return reason;
	}

	public Classification getClassification()
	{
		return classification;
	}

	public Method getConfidence()
	{
		return method;
	}

	@Override
	public String toString()
	{
		return "Judgment [reason=" + reason + ", classification=" + classification + ", method=" + method + "]";
	}

}