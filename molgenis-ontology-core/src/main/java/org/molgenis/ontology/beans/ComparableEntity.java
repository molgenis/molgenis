package org.molgenis.ontology.beans;

import java.math.BigDecimal;

import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.ontology.service.OntologyServiceImpl;

public class ComparableEntity extends MapEntity implements Comparable<ComparableEntity>
{
	private static final long serialVersionUID = 1L;

	public ComparableEntity(Entity entity, BigDecimal maxNgramScore, String maxScoreField)
	{
		for (String attributeName : entity.getAttributeNames())
		{
			set(attributeName, entity.get(attributeName));
		}
		set(OntologyServiceImpl.SCORE, maxNgramScore.doubleValue());
		set(OntologyServiceImpl.MAX_SCORE_FIELD, maxScoreField);
	}

	@Override
	public int compareTo(ComparableEntity other)
	{
		return (getDecimal().compareTo(other.getDecimal())) * (-1);
	}

	public BigDecimal getDecimal()
	{
		return new BigDecimal(Double.parseDouble(getString(OntologyServiceImpl.SCORE)));
	}
}
