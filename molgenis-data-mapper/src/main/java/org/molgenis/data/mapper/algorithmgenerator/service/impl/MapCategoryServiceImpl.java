package org.molgenis.data.mapper.algorithmgenerator.service.impl;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.CATEGORICAL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.XREF;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.mapper.algorithmgenerator.categorygenerator.CategoryAlgorithmGenerator;
import org.molgenis.data.mapper.algorithmgenerator.categorygenerator.OneToManyCategoryAlgorithmGenerator;
import org.molgenis.data.mapper.algorithmgenerator.categorygenerator.OneToOneCategoryAlgorithmGenerator;
import org.molgenis.data.mapper.algorithmgenerator.service.MapCategoryService;

public class MapCategoryServiceImpl implements MapCategoryService
{
	private final List<CategoryAlgorithmGenerator> categoryAlgorithmGenerators;

	public MapCategoryServiceImpl(DataService dataService)
	{
		this.categoryAlgorithmGenerators = Arrays.asList(new OneToOneCategoryAlgorithmGenerator(dataService),
				new OneToManyCategoryAlgorithmGenerator(dataService));
	}

	public String generate(AttributeMetaData targetAttribute, List<AttributeMetaData> sourceAttributes)
	{
		if (isValid(targetAttribute, sourceAttributes))
		{
			for (CategoryAlgorithmGenerator generator : categoryAlgorithmGenerators)
			{
				if (generator.isSuitable(targetAttribute, sourceAttributes))
				{
					return generator.generate(targetAttribute, sourceAttributes);
				}
			}
		}
		return StringUtils.EMPTY;
	}

	public boolean isValid(AttributeMetaData targetAttribute, List<AttributeMetaData> sourceAttributes)
	{
		return isReferenceDataType(targetAttribute) && (sourceAttributes.stream().allMatch(this::isReferenceDataType));
	}

	boolean isReferenceDataType(AttributeMetaData attribute)
	{
		FieldTypeEnum enumType = attribute.getDataType().getEnumType();
		return enumType == CATEGORICAL || enumType == XREF;
	}
}
