package org.molgenis.data.mapper.algorithmgenerator;

import java.util.HashSet;
import java.util.Set;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.mapper.algorithmgenerator.bean.Category;

public class CategoryAlgorithmGeneratorImpl implements AlgorithmGenerator
{
	private final DataService dataService;

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

		Set<Category> targetCategories = convertToCategory(targetAttributeMetaData);

		Set<Category> sourceCategories = convertToCategory(sourceAttributeMetaData);

		return null;
	}

	Set<Category> convertToCategory(AttributeMetaData attributeMetaData)
	{
		Set<Category> categories = new HashSet<Category>();
		EntityMetaData refEntity = attributeMetaData.getRefEntity();

		for (Entity entity : dataService.findAll(refEntity.getName()))
		{
			Integer code = entity.getInt(refEntity.getIdAttribute().getName());
			String label = entity.getString(refEntity.getLabelAttribute().getName());
			Category category = Category.create(code, label);
			if (!categories.contains(category))
			{
				categories.add(category);
			}
		}
		return categories;
	}
}
