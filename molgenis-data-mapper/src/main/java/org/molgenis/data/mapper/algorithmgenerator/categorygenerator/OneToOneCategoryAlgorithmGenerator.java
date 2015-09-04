package org.molgenis.data.mapper.algorithmgenerator.categorygenerator;

import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.mapper.algorithmgenerator.bean.Category;
import org.molgenis.data.mapper.algorithmgenerator.categorymapper.CategoryMapper;
import org.molgenis.data.mapper.algorithmgenerator.categorymapper.FrequencyCategoryMapper;
import org.molgenis.data.mapper.algorithmgenerator.categorymapper.LexicalCategoryMapper;

public class OneToOneCategoryAlgorithmGenerator extends CategoryAlgorithmGenerator
{
	private final FrequencyCategoryMapper frequencyCategoryMapper;
	private final LexicalCategoryMapper lexicalCategoryMapper;

	public OneToOneCategoryAlgorithmGenerator(DataService dataService)
	{
		super(dataService);
		lexicalCategoryMapper = new LexicalCategoryMapper();
		frequencyCategoryMapper = new FrequencyCategoryMapper();
	}

	@Override
	public boolean isSuitable(AttributeMetaData targetAttribute, List<AttributeMetaData> sourceAttributes)
	{
		return sourceAttributes.size() == 1 && isAttributeCategorical(targetAttribute)
				&& isAttributeCategorical(sourceAttributes.get(0));
	}

	@Override
	public String generate(AttributeMetaData targetAttribute, List<AttributeMetaData> sourceAttributes)
	{
		String mapAlgorithm = null;
		if (sourceAttributes.size() > 0)
		{
			AttributeMetaData firstSourceAttribute = sourceAttributes.get(0);

			List<Category> targetCategories = convertToCategory(targetAttribute);
			List<Category> sourceCategories = convertToCategory(firstSourceAttribute);

			// Check if both of target and source categories contain frequency units
			if (targetCategories.stream().anyMatch(category -> category.getAmountWrapper() != null)
					&& sourceCategories.stream().anyMatch(category -> category.getAmountWrapper() != null))
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
}
