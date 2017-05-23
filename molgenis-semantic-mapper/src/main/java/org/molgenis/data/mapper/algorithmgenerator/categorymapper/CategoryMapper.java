package org.molgenis.data.mapper.algorithmgenerator.categorymapper;

import org.molgenis.data.mapper.algorithmgenerator.bean.Category;
import org.molgenis.data.mapper.algorithmgenerator.rules.CategoryMatchQuality;
import org.molgenis.data.mapper.algorithmgenerator.rules.CategoryRule;

import java.util.List;

public abstract class CategoryMapper
{
	protected final List<CategoryRule> rules;

	public CategoryMapper(List<CategoryRule> rules)
	{
		this.rules = rules;
	}

	public abstract Category findBestCategoryMatch(Category sourceCategory, List<Category> targetCategories);

	public abstract CategoryMatchQuality<?> applyCustomRules(Category sourceCategory, Category targetCategory);
}
