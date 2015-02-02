package org.molgenis.ontology.beans;

import java.math.BigDecimal;

import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.ontology.repository.OntologyTermQueryRepository;

public class ComparableEntity extends MapEntity implements Comparable<ComparableEntity>
{
	private static final long serialVersionUID = 1L;
	public static final String SCORE = "Score";
	public static final String MAX_SCORE_FIELD = "maxScoreField";
	public static final String COMBINED_SCORE = "Combined_Score";

	public ComparableEntity(Entity entity, BigDecimal maxNgramScore, String maxScoreField)
	{
		for (String attributeName : entity.getAttributeNames())
		{
			set(attributeName, entity.get(attributeName));
		}
		set(SCORE, maxNgramScore.doubleValue());
		set(COMBINED_SCORE, maxNgramScore.doubleValue());
		set(MAX_SCORE_FIELD, maxScoreField);
	}

	@Override
	public int compareTo(ComparableEntity other)
	{
		int compareTo = other.getCombinedScore().compareTo(getCombinedScore());

		if (compareTo == 0)
		{
			int otherStringLength = other.getString(OntologyTermQueryRepository.SYNONYMS).length();
			int stringLength = getString(OntologyTermQueryRepository.SYNONYMS).length();

			if (otherStringLength > stringLength) return -1;
			else if (otherStringLength == stringLength) return 0;
			else if (otherStringLength < stringLength) return 1;
		}

		return compareTo;
	}

	public BigDecimal getCombinedScore()
	{
		return new BigDecimal(Double.parseDouble(getString(COMBINED_SCORE)));
	}

	public BigDecimal getScore()
	{
		return new BigDecimal(Double.parseDouble(getString(SCORE)));
	}
}
