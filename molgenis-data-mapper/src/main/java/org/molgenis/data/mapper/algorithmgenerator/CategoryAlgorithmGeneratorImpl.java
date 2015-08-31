package org.molgenis.data.mapper.algorithmgenerator;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.mapper.algorithmgenerator.bean.Category;
import org.molgenis.data.mapper.algorithmgenerator.categorymapper.CategoryMapper;
import org.molgenis.data.mapper.algorithmgenerator.categorymapper.CategoryMapperUtil;
import org.molgenis.data.mapper.algorithmgenerator.categorymapper.FrequencyCategoryMapper;
import org.molgenis.data.mapper.algorithmgenerator.categorymapper.LexicalCategoryMapper;

public class CategoryAlgorithmGeneratorImpl implements CategoryAlgorithmGenerator
{
	private final DataService dataService;
	private final FrequencyCategoryMapper frequencyCategoryMapper;
	private final LexicalCategoryMapper lexicalCategoryMapper;

	public CategoryAlgorithmGeneratorImpl(DataService dataService)
	{
		this.dataService = dataService;
		lexicalCategoryMapper = new LexicalCategoryMapper();
		frequencyCategoryMapper = new FrequencyCategoryMapper();
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
			mapAlgorithm = mapCategories(sourceAttributeMetaData, targetCategories, sourceCategories,
					frequencyCategoryMapper);
		}
		else
		{
			mapAlgorithm = mapCategories(sourceAttributeMetaData, targetCategories, sourceCategories,
					lexicalCategoryMapper);
		}
		return mapAlgorithm;
	}

	String mapCategories(AttributeMetaData sourceAttributeMetaData, List<Category> targetCategories,
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
				Category category = Category.create(code, label, CategoryMapperUtil.convertDescriptionToAmount(label));
				if (!categories.contains(category))
				{
					categories.add(category);
				}
			}
		}
		return categories;
	}
}
