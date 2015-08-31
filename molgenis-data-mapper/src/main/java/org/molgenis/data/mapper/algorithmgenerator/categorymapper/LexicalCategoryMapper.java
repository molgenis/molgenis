package org.molgenis.data.mapper.algorithmgenerator.categorymapper;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.mapper.algorithmgenerator.bean.Category;
import org.molgenis.data.semanticsearch.string.NGramDistanceAlgorithm;

public class LexicalCategoryMapper implements CategoryMapper
{
	public Category findBestCategoryMatch(Category sourceCategory, List<Category> targetCategories)
	{
		String sourceCategoryLabel = sourceCategory.getLabel().toLowerCase();
		Category bestCategory = null;
		double bestNGramScore = -1;
		for (Category targetCategory : targetCategories)
		{
			String targetCategoryLabel = targetCategory.getLabel();

			if (StringUtils.equalsIgnoreCase(sourceCategoryLabel, targetCategoryLabel))
			{
				return targetCategory;
			}

			double ngramScore = NGramDistanceAlgorithm.stringMatching(sourceCategoryLabel, targetCategoryLabel);
			if (bestNGramScore == -1 || bestNGramScore < ngramScore)
			{
				bestNGramScore = ngramScore;
				bestCategory = targetCategory;
			}
		}
		return bestCategory;
	}

	public boolean applyCustomRules(Category sourceCategory, Category targetCategory)
	{
		// TODO Auto-generated method stub
		return false;
	}
}
