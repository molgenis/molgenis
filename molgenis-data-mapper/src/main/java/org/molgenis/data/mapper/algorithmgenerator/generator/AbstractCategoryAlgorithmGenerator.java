package org.molgenis.data.mapper.algorithmgenerator.generator;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.CATEGORICAL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.XREF;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.mapper.algorithmgenerator.bean.Category;
import org.molgenis.data.mapper.algorithmgenerator.categorymapper.CategoryMapperUtil;

import com.google.common.base.Preconditions;

public abstract class AbstractCategoryAlgorithmGenerator implements AlgorithmGenerator
{
	private final DataService dataService;

	public AbstractCategoryAlgorithmGenerator(DataService dataService)
	{
		this.dataService = Preconditions.checkNotNull(dataService);
	}

	boolean isXrefOrCategorialDataType(AttributeMetaData attribute)
	{
		FieldTypeEnum enumType = attribute.getDataType().getEnumType();
		return enumType == CATEGORICAL || enumType == XREF;
	}

	public List<Category> convertToCategory(AttributeMetaData attributeMetaData)
	{
		List<Category> categories = new ArrayList<Category>();
		EntityMetaData refEntity = attributeMetaData.getRefEntity();
		if (refEntity != null)
		{
			dataService.findAll(refEntity.getName()).forEach(entity -> {
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
