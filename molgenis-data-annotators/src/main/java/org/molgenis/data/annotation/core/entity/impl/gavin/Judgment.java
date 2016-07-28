package org.molgenis.data.annotation.core.entity.impl.gavin;

/**
 * Judgment result of the gavin method
 */
public class Judgment
{
	public enum Classification
	{
		Benign, Pathogenic, VOUS
	}

	public enum Method
	{
		calibrated, genomewide
	}

	String reason;
	Classification classification;
	Method method;
	String gene;

	public Judgment(Classification classification, Method method, String gene, String reason)
	{
		this.reason = reason;
		this.classification = classification;
		this.method = method;
		this.gene = gene;
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

	public String getGene()
	{
		return gene;
	}

	@Override
	public String toString()
	{
		return "Judgment [reason=" + reason + ", classification=" + classification + ", method=" + method + "]";
	}

}