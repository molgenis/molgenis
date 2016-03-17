package org.molgenis.data.mapper.algorithmgenerator.generator;

import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.mapper.algorithmgenerator.bean.Category;
import org.molgenis.data.mapper.algorithmgenerator.categorymapper.CategoryMapper;
import org.molgenis.data.mapper.algorithmgenerator.categorymapper.FrequencyCategoryMapper;
import org.molgenis.data.mapper.algorithmgenerator.categorymapper.LexicalCategoryMapper;
import org.molgenis.data.mapper.algorithmgenerator.rules.CategoryRule;
import org.molgenis.data.mapper.algorithmgenerator.rules.impl.MissingCategoryRule;
import org.molgenis.data.mapper.algorithmgenerator.rules.impl.NegativeCategoryRule;
import org.molgenis.data.mapper.algorithmgenerator.rules.impl.PositiveCategoryRule;

import com.google.common.collect.Lists;

public class OneToOneCategoryAlgorithmGenerator extends AbstractCategoryAlgorithmGenerator
{
	private final FrequencyCategoryMapper frequencyCategoryMapper;
	private final LexicalCategoryMapper lexicalCategoryMapper;

	public OneToOneCategoryAlgorithmGenerator(DataService dataService)
	{
		super(dataService);

		List<CategoryRule> rules = getRules(dataService);
		this.lexicalCategoryMapper = new LexicalCategoryMapper(rules);
		this.frequencyCategoryMapper = new FrequencyCategoryMapper(rules);
	}

	private List<CategoryRule> getRules(DataService dataService)
	{
		return Lists.newArrayList(new NegativeCategoryRule(), new PositiveCategoryRule(), new MissingCategoryRule());
	}

	@Override
	public boolean isSuitable(AttributeMetaData targetAttribute, List<AttributeMetaData> sourceAttributes)
	{
		return isXrefOrCategorialDataType(targetAttribute) && (sourceAttributes.stream().allMatch(this::isXrefOrCategorialDataType))
				&& sourceAttributes.size() == 1;
	}

	@Override
	public String generate(AttributeMetaData targetAttribute, List<AttributeMetaData> sourceAttributes,
			EntityMetaData targetEntityMetaData, EntityMetaData sourceEntityMetaData)
	{
		String mapAlgorithm = null;
		if (sourceAttributes.size() > 0)
		{
			AttributeMetaData firstSourceAttribute = sourceAttributes.get(0);

			List<Category> targetCategories = convertToCategory(targetAttribute);
			List<Category> sourceCategories = convertToCategory(firstSourceAttribute);

			// Check if both of target and source categories contain frequency units
			if (isFrequencyCategory(targetCategories) && isFrequencyCategory(sourceCategories))
			{
				mapAlgorithm = mapCategories(firstSourceAttribute, targetCategories, sourceCategories,
						frequencyCategoryMapper);
			}
			else
			{
				mapAlgorithm = mapCategories(firstSourceAttribute, targetCategories, sourceCategories,
						lexicalCategoryMapper);
			}
		}
		return mapAlgorithm;
	}

	public String mapCategories(AttributeMetaData sourceAttributeMetaData, List<Category> targetCategories,
			List<Category> sourceCategories, CategoryMapper categoryMapper)
	{
		StringBuilder stringBuilder = new StringBuilder();
		for (Category sourceCategory : sourceCategories)
		{
			Category bestTargetCategory = categoryMapper.findBestCategoryMatch(sourceCategory, targetCategories);

			if (bestTargetCategory != null)
			{
				if (stringBuilder.length() == 0)
				{
					stringBuilder.append("$('").append(sourceAttributeMetaData.getName()).append("')").append(".map({");
				}
				stringBuilder.append("\"").append(sourceCategory.getCode()).append("\":\"")
						.append(bestTargetCategory.getCode()).append("\",");
			}
		}

		if (stringBuilder.length() > 0)
		{
			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
			stringBuilder.append("}, null, null).value();");
		}

		return stringBuilder.toString();
	}

	boolean isFrequencyCategory(List<Category> categories)
	{
		return categories.stream().anyMatch(category -> category.getAmountWrapper() != null);
	}
}
