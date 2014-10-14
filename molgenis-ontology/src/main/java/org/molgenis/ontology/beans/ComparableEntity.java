package org.molgenis.ontology.beans;

import java.math.BigDecimal;

import org.molgenis.data.Entity;

public class ComparableEntity implements Comparable<ComparableEntity>
{
	private final String maxScoreField;
	private final Entity entity;
	private final BigDecimal similarityScore;

	public ComparableEntity(Entity entity, BigDecimal similarityScore, String maxScoreField)
	{
		this.entity = entity;
		this.similarityScore = similarityScore;
		this.maxScoreField = maxScoreField;
	}

	public String getMaxScoreField()
	{
		return maxScoreField;
	}

	public BigDecimal getSimilarityScore()
	{
		return similarityScore;
	}

	public Entity getEntity()
	{
		return entity;
	}

	@Override
	public int compareTo(ComparableEntity other)
	{
		return similarityScore.compareTo(other.getSimilarityScore()) * (-1);
	}
}