package org.molgenis.data.mapper.algorithmgenerator.categorymapper;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.mapper.algorithmgenerator.bean.Category;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LexicalCategoryMapperTest
{
	LexicalCategoryMapper lexicalCategoryMapper = new LexicalCategoryMapper();

	@Test
	public void findBestCategoryMatch()
	{
		List<Category> targetCategories = new ArrayList<Category>();
		Category maleCategory = Category.create(0, "MALE");
		Category femaleCategory = Category.create(1, "FEMALE");
		targetCategories.add(maleCategory);
		targetCategories.add(femaleCategory);

		Assert.assertEquals(lexicalCategoryMapper.findBestCategoryMatch(Category.create(1, "male"), targetCategories),
				maleCategory);

		Assert.assertEquals(
				lexicalCategoryMapper.findBestCategoryMatch(Category.create(2, "female"), targetCategories),
				femaleCategory);
	}
}