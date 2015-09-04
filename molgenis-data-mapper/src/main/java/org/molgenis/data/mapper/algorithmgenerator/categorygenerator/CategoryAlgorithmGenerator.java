package org.molgenis.data.mapper.algorithmgenerator.categorygenerator;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.mapper.algorithmgenerator.bean.Category;
import org.molgenis.data.mapper.algorithmgenerator.categorymapper.CategoryMapperUtil;

public abstract class CategoryAlgorithmGenerator
{
	private final DataService dataService;

	public CategoryAlgorithmGenerator(DataService dataService)
	{
		this.dataService = dataService;
	}

	abstract boolean isSuitable(AttributeMetaData targetAttribute, List<AttributeMetaData> sourceAttributes);

	abstract String generate(AttributeMetaData targetAttribute, List<AttributeMetaData> sourceAttributes);

	public boolean isAttributeCategorical(AttributeMetaData attribute)
	{
		return attribute.getDataType().getEnumType() == FieldTypeEnum.CATEGORICAL;
	}

	public List<Category> convertToCategory(AttributeMetaData attributeMetaData)
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
