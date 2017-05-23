package org.molgenis.data.mapper.algorithmgenerator.rules;

import org.molgenis.data.mapper.algorithmgenerator.bean.Category;

public interface CategoryRule
{
	CategoryMatchQuality<?> createCategoryMatchQuality(Category targetCategory, Category sourceCategory);
}
