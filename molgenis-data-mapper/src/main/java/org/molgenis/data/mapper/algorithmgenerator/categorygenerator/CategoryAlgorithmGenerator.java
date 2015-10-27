package org.molgenis.data.mapper.algorithmgenerator.categorygenerator;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.mapper.algorithmgenerator.bean.Category;
import org.molgenis.data.mapper.algorithmgenerator.categorymapper.CategoryMapperUtil;

import com.google.common.base.Preconditions;

public abstract class CategoryAlgorithmGenerator
{
	private final DataService dataService;

	public CategoryAlgorithmGenerator(DataService dataService)
	{
		this.dataService = Preconditions.checkNotNull(dataService);
	}

	public abstract boolean isSuitable(AttributeMetaData targetAttribute, List<AttributeMetaData> sourceAttributes);

	public abstract String generate(AttributeMetaData targetAttribute, List<AttributeMetaData> sourceAttributes);

	public List<Category> convertToCategory(AttributeMetaData attributeMetaData)
	{
		List<Category> categories = new ArrayList<Category>();
		EntityMetaData refEntity = attributeMetaData.getRefEntity();
		if (refEntity != null)
		{
			for (Entity entity : dataService.findAll(refEntity.getName()))
			{
				String code = entity.getString(refEntity.getIdAttribute().getName());
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
