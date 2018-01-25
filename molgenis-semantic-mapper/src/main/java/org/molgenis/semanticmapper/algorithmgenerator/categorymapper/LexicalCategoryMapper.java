package org.molgenis.semanticmapper.algorithmgenerator.categorymapper;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.semanticmapper.algorithmgenerator.bean.Category;
import org.molgenis.semanticmapper.algorithmgenerator.rules.CategoryMatchQuality;
import org.molgenis.semanticmapper.algorithmgenerator.rules.CategoryRule;
import org.molgenis.semanticsearch.string.NGramDistanceAlgorithm;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class LexicalCategoryMapper extends CategoryMapper
{
	private final static double DEFAULT_THRESHOLD = 50.0f;

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
			Optional<?> findFirst = targetCategories.stream()
													.map(targetCategory -> applyCustomRules(sourceCategory,
															targetCategory))
													.filter(Objects::nonNull)
													.sorted()
													.findFirst();

			if (findFirst.isPresent() && findFirst.get() instanceof CategoryMatchQuality)
			{
				bestCategory = ((CategoryMatchQuality<?>) findFirst.get()).getTargetCategory();
			}
		}

		return bestCategory;
	}

	public CategoryMatchQuality<?> applyCustomRules(Category sourceCategory, Category targetCategory)
	{
		return rules.stream()
					.map(rule -> rule.createCategoryMatchQuality(targetCategory, sourceCategory))
					.filter(CategoryMatchQuality::isRuleApplied)
					.sorted()
					.findFirst()
					.orElse(null);
	}
}