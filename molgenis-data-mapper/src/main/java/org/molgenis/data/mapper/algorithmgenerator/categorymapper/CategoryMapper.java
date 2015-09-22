package org.molgenis.data.mapper.algorithmgenerator.categorymapper;

import java.util.List;

import org.molgenis.data.mapper.algorithmgenerator.bean.Category;
import org.molgenis.data.mapper.algorithmgenerator.rules.CategoryRule;

public abstract class CategoryMapper
{
	protected final List<CategoryRule> rules;

	public CategoryMapper(List<CategoryRule> rules)
	{
		this.rules = rules;
	}

	public abstract Category findBestCategoryMatch(Category sourceCategory, List<Category> targetCategories);

	public abstract boolean applyCustomRules(Category sourceCategory, Category targetCategory);
}
