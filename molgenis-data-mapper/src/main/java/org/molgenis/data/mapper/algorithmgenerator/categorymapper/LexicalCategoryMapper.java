package org.molgenis.data.mapper.algorithmgenerator.categorymapper;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.mapper.algorithmgenerator.bean.Category;
import org.molgenis.data.mapper.algorithmgenerator.rules.CategoryRule;
import org.molgenis.data.semanticsearch.string.NGramDistanceAlgorithm;

public class LexicalCategoryMapper extends CategoryMapper
{
	private final static double DEFAULT_THRESHOLD = 0.5f;

	public LexicalCategoryMapper(List<CategoryRule> rules)
	{
		super(rules);
	}

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

		if (bestNGramScore < DEFAULT_THRESHOLD)
		{
			Optional<Category> findFirst = targetCategories.stream()
					.filter(targetCategory -> applyCustomRules(sourceCategory, targetCategory)).findFirst();

			if (findFirst.isPresent())
			{
				bestCategory = findFirst.get();
			}
		}

		return bestCategory;
	}

	public boolean applyCustomRules(Category sourceCategory, Category targetCategory)
	{
		return rules.stream().anyMatch(rule -> rule.isRuleApplied(targetCategory, sourceCategory));
	}
}