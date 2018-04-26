package org.molgenis.semanticmapper.algorithmgenerator.rules;

import org.molgenis.semanticmapper.algorithmgenerator.bean.Category;

public interface CategoryRule
{
	CategoryMatchQuality<?> createCategoryMatchQuality(Category targetCategory, Category sourceCategory);
}
