package org.molgenis.semanticmapper.algorithmgenerator.generator;

import com.google.common.base.Preconditions;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.semanticmapper.algorithmgenerator.bean.Category;
import org.molgenis.semanticmapper.algorithmgenerator.categorymapper.CategoryMapperUtil;

import java.util.ArrayList;
import java.util.List;

import static org.molgenis.data.support.EntityTypeUtils.isSingleReferenceType;

abstract class AbstractCategoryAlgorithmGenerator implements AlgorithmGenerator
{
	private final DataService dataService;

	AbstractCategoryAlgorithmGenerator(DataService dataService)
	{
		this.dataService = Preconditions.checkNotNull(dataService);
	}

	boolean isXrefOrCategorialDataType(Attribute attribute)
	{
		return isSingleReferenceType(attribute);
	}

	List<Category> convertToCategory(Attribute attribute)
	{
		List<Category> categories = new ArrayList<>();
		EntityType refEntity = attribute.getRefEntity();

		if (refEntity != null)
		{
			dataService.findAll(refEntity.getId()).forEach(entity ->
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
