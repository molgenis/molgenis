package org.molgenis.data.mapper.algorithmgenerator.generator;

import com.google.common.base.Preconditions;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.mapper.algorithmgenerator.bean.Category;
import org.molgenis.data.mapper.algorithmgenerator.categorymapper.CategoryMapperUtil;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import java.util.ArrayList;
import java.util.List;

import static org.molgenis.data.support.EntityTypeUtils.isSingleReferenceType;

public abstract class AbstractCategoryAlgorithmGenerator implements AlgorithmGenerator
{
	private final DataService dataService;

	public AbstractCategoryAlgorithmGenerator(DataService dataService)
	{
		this.dataService = Preconditions.checkNotNull(dataService);
	}

	boolean isXrefOrCategorialDataType(Attribute attribute)
	{
		return isSingleReferenceType(attribute);
	}

	public List<Category> convertToCategory(Attribute attribute)
	{
		List<Category> categories = new ArrayList<>();
		EntityType refEntity = attribute.getRefEntity();

		if (refEntity != null)
		{
			dataService.findAll(refEntity.getFullyQualifiedName()).forEach(entity ->
			{
				String code = DataConverter.toString(entity.get(refEntity.getIdAttribute().getName()));
				String label = DataConverter.toString(entity.get(refEntity.getLabelAttribute().getName()));
				Category category = Category.create(code, label, CategoryMapperUtil.convertDescriptionToAmount(label));
				if (!categories.contains(category))
				{
					categories.add(category);
				}
			});
		}
		return categories;
	}
}
