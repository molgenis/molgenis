package org.molgenis.semanticmapper.algorithmgenerator.rules.quality;

public abstract class Quality<T> implements Comparable<Quality<T>>
{
	public abstract T getQualityIndicator();
}
