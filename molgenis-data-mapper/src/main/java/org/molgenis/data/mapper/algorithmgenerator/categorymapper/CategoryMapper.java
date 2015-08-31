package org.molgenis.data.mapper.algorithmgenerator.categorymapper;

import java.util.List;

import org.molgenis.data.mapper.algorithmgenerator.bean.Category;

public interface CategoryMapper
{
	public Category findBestCategoryMatch(Category sourceCategory, List<Category> targetCategories);

	boolean applyCustomRules(Category sourceCategory, Category targetCategory);
}
