package org.molgenis.data.mapper.algorithmgenerator;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.mapper.algorithmgenerator.bean.Category;
import org.molgenis.data.mapper.algorithmgenerator.categorymapper.FrequencyCategoryMapper;

public class CategoryAlgorithmGeneratorImpl implements CategoryAlgorithmGenerator
{
	private final DataService dataService;

	private final FrequencyCategoryMapper frequencyCategoryMapper = new FrequencyCategoryMapper();

	public CategoryAlgorithmGeneratorImpl(DataService dataService)
	{
		this.dataService = dataService;
	}

	public boolean isSuitable(AttributeMetaData targetAttributeMetaData, AttributeMetaData sourceAttributeMetaData)
	{
		return targetAttributeMetaData.getDataType().getEnumType() == FieldTypeEnum.CATEGORICAL;
	}

	public String generate(AttributeMetaData targetAttributeMetaData, AttributeMetaData sourceAttributeMetaData)
	{
		List<Category> targetCategories = convertToCategory(targetAttributeMetaData);
		List<Category> sourceCategories = convertToCategory(sourceAttributeMetaData);

		String mapAlgorithm;

		// Check if both of target and source categories contain frequency units
		if (targetCategories.stream().anyMatch(category -> category.getAmountWrapper() != null)
				&& sourceCategories.stream().anyMatch(category -> category.getAmountWrapper() != null))
		{
			mapAlgorithm = mapFrequencyCategories(sourceAttributeMetaData, targetCategories, sourceCategories);
		}
		else
		{
			mapAlgorithm = mapLexicalCategories(sourceAttributeMetaData, targetCategories, sourceCategories);
		}
		return mapAlgorithm;
	}

	String mapLexicalCategories(AttributeMetaData sourceAttributeMetaData, List<Category> targetCategories,
			List<Category> sourceCategories)
	{
		return null;
	}

	String mapFrequencyCategories(AttributeMetaData sourceAttributeMetaData, List<Category> targetCategories,
			List<Category> sourceCategories)
	{
		StringBuilder stringBuilder = new StringBuilder();
		for (Category sourceCategory : sourceCategories)
		{
			Category bestTargetCategory = findBestCategoryMatch(sourceCategory, targetCategories);

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

	Category findBestCategoryMatch(Category sourceCategory, List<Category> targetCategories)
	{
		Category bestTargetCategory = null;
		double smallestFactor = -1;
		for (Category targetCategory : targetCategories)
		{
			Double convertFactor = frequencyCategoryMapper.convert(sourceCategory.getAmountWrapper(),
					targetCategory.getAmountWrapper());

			if (convertFactor == null) continue;

			if (smallestFactor == -1)
			{
				smallestFactor = convertFactor;
				bestTargetCategory = targetCategory;
			}
			else if (smallestFactor > convertFactor)
			{
				smallestFactor = convertFactor;
				bestTargetCategory = targetCategory;
			}
		}
		return bestTargetCategory;
	}

	List<Category> convertToCategory(AttributeMetaData attributeMetaData)
	{
		List<Category> categories = new ArrayList<Category>();
		EntityMetaData refEntity = attributeMetaData.getRefEntity();
		if (refEntity != null)
		{
			for (Entity entity : dataService.findAll(refEntity.getName()))
			{
				Integer code = entity.getInt(refEntity.getIdAttribute().getName());
				String label = entity.getString(refEntity.getLabelAttribute().getName());
				Category category = Category.create(code, label,
						frequencyCategoryMapper.convertDescriptionToAmount(label));
				if (!categories.contains(category))
				{
					categories.add(category);
				}
			}
		}
		return categories;
	}
}
