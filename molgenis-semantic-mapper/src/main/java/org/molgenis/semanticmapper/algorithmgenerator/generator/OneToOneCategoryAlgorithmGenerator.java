package org.molgenis.semanticmapper.algorithmgenerator.generator;

import com.google.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.semanticmapper.algorithmgenerator.bean.Category;
import org.molgenis.semanticmapper.algorithmgenerator.categorymapper.CategoryMapper;
import org.molgenis.semanticmapper.algorithmgenerator.categorymapper.FrequencyCategoryMapper;
import org.molgenis.semanticmapper.algorithmgenerator.categorymapper.LexicalCategoryMapper;
import org.molgenis.semanticmapper.algorithmgenerator.rules.CategoryRule;
import org.molgenis.semanticmapper.algorithmgenerator.rules.impl.MissingCategoryRule;
import org.molgenis.semanticmapper.algorithmgenerator.rules.impl.NegativeCategoryRule;
import org.molgenis.semanticmapper.algorithmgenerator.rules.impl.PositiveCategoryRule;

import java.util.List;

public class OneToOneCategoryAlgorithmGenerator extends AbstractCategoryAlgorithmGenerator
{
	private final FrequencyCategoryMapper frequencyCategoryMapper;
	private final LexicalCategoryMapper lexicalCategoryMapper;

	public OneToOneCategoryAlgorithmGenerator(DataService dataService)
	{
		super(dataService);

		List<CategoryRule> rules = getRules();
		this.lexicalCategoryMapper = new LexicalCategoryMapper(rules);
		this.frequencyCategoryMapper = new FrequencyCategoryMapper(rules);
	}

	private List<CategoryRule> getRules()
	{
		return Lists.newArrayList(new NegativeCategoryRule(), new PositiveCategoryRule(), new MissingCategoryRule());
	}

	@Override
	public boolean isSuitable(Attribute targetAttribute, List<Attribute> sourceAttributes)
	{
		return isXrefOrCategorialDataType(targetAttribute) && (sourceAttributes.stream()
																			   .allMatch(
																					   this::isXrefOrCategorialDataType))
				&& sourceAttributes.size() == 1;
	}

	@Override
	public String generate(Attribute targetAttribute, List<Attribute> sourceAttributes, EntityType targetEntityType,
			EntityType sourceEntityType)
	{
		String mapAlgorithm = null;
		if (!sourceAttributes.isEmpty())
		{
			Attribute firstSourceAttribute = sourceAttributes.get(0);

			List<Category> targetCategories = convertToCategory(targetAttribute);
			List<Category> sourceCategories = convertToCategory(firstSourceAttribute);

			// Check if both of target and source categories contain frequency units
			if (isFrequencyCategory(targetCategories) && isFrequencyCategory(sourceCategories))
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

	private String mapCategories(Attribute sourceAttribute, List<Category> targetCategories,
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
					stringBuilder.append("$('").append(sourceAttribute.getName()).append("')").append(".map({");
				}
				stringBuilder.append("\"")
							 .append(sourceCategory.getCode())
							 .append("\":\"")
							 .append(bestTargetCategory.getCode())
							 .append("\",");
			}
		}

		if (stringBuilder.length() > 0)
		{
			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
			stringBuilder.append("}, null, null).value();");
		}

		return stringBuilder.toString();
	}

	boolean isFrequencyCategory(List<Category> categories)
	{
		return categories.stream().anyMatch(category -> category.getAmountWrapper() != null);
	}
}
