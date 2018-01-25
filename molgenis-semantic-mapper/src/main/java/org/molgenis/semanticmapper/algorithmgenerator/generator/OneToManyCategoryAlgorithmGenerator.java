package org.molgenis.semanticmapper.algorithmgenerator.generator;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.semanticmapper.algorithmgenerator.bean.AmountWrapper;
import org.molgenis.semanticmapper.algorithmgenerator.bean.Category;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class OneToManyCategoryAlgorithmGenerator extends AbstractCategoryAlgorithmGenerator
{
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##.#",
			DecimalFormatSymbols.getInstance(Locale.ENGLISH));

	private final OneToOneCategoryAlgorithmGenerator oneToOneCategoryAlgorithmGenerator;

	public OneToManyCategoryAlgorithmGenerator(DataService dataService)
	{
		super(dataService);
		oneToOneCategoryAlgorithmGenerator = new OneToOneCategoryAlgorithmGenerator(dataService);
	}

	@Override
	public boolean isSuitable(Attribute targetAttribute, List<Attribute> sourceAttributes)
	{
		return isXrefOrCategorialDataType(targetAttribute) && (sourceAttributes.stream()
																			   .allMatch(
																					   this::isXrefOrCategorialDataType))
				&& sourceAttributes.size() > 1;
	}

	@Override
	public String generate(Attribute targetAttribute, List<Attribute> sourceAttributes, EntityType targetEntityType,
			EntityType sourceEntityType)
	{
		// if the target attribute and all the source attributes contain frequency related categories
		StringBuilder stringBuilder = new StringBuilder();

		if (suitableForGeneratingWeightedMap(targetAttribute, sourceAttributes))
		{
			stringBuilder.append(createAlgorithmNullCheckIfStatement(sourceAttributes))
						 .append(createAlgorithmElseBlock(targetAttribute, sourceAttributes));
		}
		else
		{
			for (Attribute sourceAttribute : sourceAttributes)
			{
				stringBuilder.append(
						oneToOneCategoryAlgorithmGenerator.generate(targetAttribute, Arrays.asList(sourceAttribute),
								targetEntityType, sourceEntityType));
			}
		}

		return stringBuilder.toString();
	}

	String createAlgorithmElseBlock(Attribute targetAttribute, List<Attribute> sourceAttributes)
	{
		StringBuilder stringBuilder = new StringBuilder();
		if (!sourceAttributes.isEmpty())
		{
			stringBuilder.append("else{\n").append("\tSUM_WEIGHT = new newValue(0);\n");
			for (Attribute sourceAttribute : sourceAttributes)
			{
				String generateWeightedMap = generateWeightedMap(sourceAttribute);

				if (StringUtils.isNotEmpty(generateWeightedMap))
				{

					stringBuilder.append("\tSUM_WEIGHT.plus(").append(generateWeightedMap).append(");\n");
				}
			}
			stringBuilder.append("\tSUM_WEIGHT").append(groupCategoryValues(targetAttribute)).append("\n}");
		}
		return stringBuilder.toString();
	}

	String createAlgorithmNullCheckIfStatement(List<Attribute> sourceAttributes)
	{
		StringBuilder stringBuilder = new StringBuilder();
		if (!sourceAttributes.isEmpty())
		{
			stringBuilder.append("var SUM_WEIGHT;\n").append("if(");
			sourceAttributes.stream()
							.forEach(attribute -> stringBuilder.append("$('")
															   .append(attribute.getName())
															   .append("').isNull().value() && "));
			stringBuilder.delete(stringBuilder.length() - 4, stringBuilder.length());
			stringBuilder.append("){\n").append("\tSUM_WEIGHT = new newValue();\n").append("\tSUM_WEIGHT.value();\n}");
		}
		return stringBuilder.toString();
	}

	boolean suitableForGeneratingWeightedMap(Attribute targetAttribute, List<Attribute> sourceAttributes)
	{
		boolean isTargetSuitable = oneToOneCategoryAlgorithmGenerator.isFrequencyCategory(
				convertToCategory(targetAttribute));
		boolean areSourcesSuitable = sourceAttributes.stream()
													 .map(this::convertToCategory)
													 .allMatch(oneToOneCategoryAlgorithmGenerator::isFrequencyCategory);
		return isTargetSuitable && areSourcesSuitable;
	}

	public String generateWeightedMap(Attribute attribute)
	{
		StringBuilder stringBuilder = new StringBuilder();

		for (Category sourceCategory : convertToCategory(attribute))
		{
			AmountWrapper amountWrapper = sourceCategory.getAmountWrapper();
			if (amountWrapper != null)
			{
				if (stringBuilder.length() == 0)
				{
					stringBuilder.append("$('").append(attribute.getName()).append("').map({");
				}

				double estimatedValue = amountWrapper.getAmount().getEstimatedValue();

				stringBuilder.append("\"")
							 .append(sourceCategory.getCode())
							 .append("\":")
							 .append(DECIMAL_FORMAT.format(estimatedValue))
							 .append(",");
			}
		}

		if (stringBuilder.length() > 0)
		{
			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
			stringBuilder.append("}, null, null).value()");
		}

		return stringBuilder.toString();
	}

	public String groupCategoryValues(Attribute attribute)
	{
		StringBuilder stringBuilder = new StringBuilder();

		List<Category> sortedCategories = convertToCategory(attribute).stream()
																	  .filter(category -> category.getAmountWrapper()
																			  != null)
																	  .collect(Collectors.toList());

		sortedCategories.sort(Comparator.comparingDouble(o -> o.getAmountWrapper().getAmount().getEstimatedValue()));

		List<Integer> sortedRangValues = getRangedValues(sortedCategories);

		Map<String, Category> categoryRangeBoundMap = createCategoryRangeBoundMap(sortedCategories);

		if (categoryRangeBoundMap.size() > 0)
		{
			if (stringBuilder.length() == 0)
			{
				stringBuilder.append(".group([");
			}

			sortedRangValues.stream().forEach(value -> stringBuilder.append(value).append(','));
			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
			stringBuilder.append("]).map({");

			for (Entry<String, Category> entry : categoryRangeBoundMap.entrySet())
			{
				stringBuilder.append("\"")
							 .append(entry.getKey())
							 .append("\":\"")
							 .append(entry.getValue().getCode())
							 .append("\",");
			}
			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
			stringBuilder.append("}, null, null).value();");
		}

		return stringBuilder.toString();
	}

	private List<Integer> getRangedValues(List<Category> sortedCategories)
	{
		List<Integer> sortedRangValues = new ArrayList<>();
		for (Category targetCategory : sortedCategories)
		{
			int minValue = parseAmountMinimumValue(targetCategory);
			int maxValue = parseAmountMaximumValue(targetCategory);

			if (!sortedRangValues.contains(minValue))
			{
				sortedRangValues.add(minValue);
			}

			if (!sortedRangValues.contains(maxValue))
			{
				sortedRangValues.add(maxValue);
			}
		}
		return sortedRangValues;
	}

	private Map<String, Category> createCategoryRangeBoundMap(List<Category> sortedCategories)
	{
		Map<String, Category> categoryRangeBoundMap = new LinkedHashMap<>();

		for (int categoryIndex = 0; categoryIndex < sortedCategories.size(); categoryIndex++)
		{
			Category category = sortedCategories.get(categoryIndex);

			int minValue = parseAmountMinimumValue(category);
			int maxValue = parseAmountMaximumValue(category);

			if (categoryIndex == 0)
			{
				categoryRangeBoundMap.put("-" + minValue, category);
			}

			// The category contains ranged values
			if (minValue != maxValue)
			{
				categoryRangeBoundMap.put(minValue + "-" + maxValue, category);
			}
			else
			{
				if (categoryIndex < sortedCategories.size() - 1)
				{
					Category nextCategory = sortedCategories.get(categoryIndex + 1);
					int upperBound = parseAmountMinimumValue(nextCategory);
					if (upperBound != maxValue)
					{
						categoryRangeBoundMap.put(maxValue + "-" + upperBound, category);
					}
				}

				if (categoryIndex > 0)
				{
					Category previousCategory = sortedCategories.get(categoryIndex - 1);
					int lowerBound = parseAmountMaximumValue(previousCategory);
					if (lowerBound != minValue)
					{
						categoryRangeBoundMap.put(lowerBound + "-" + minValue, category);
					}
				}
			}
			if (categoryIndex == sortedCategories.size() - 1)
			{
				categoryRangeBoundMap.put(maxValue + "+", category);
			}
		}
		return categoryRangeBoundMap;
	}

	int parseAmountMinimumValue(Category category)
	{
		return (int) Double.parseDouble(
				DECIMAL_FORMAT.format(category.getAmountWrapper().getAmount().getMinimumValue()));
	}

	int parseAmountMaximumValue(Category category)
	{
		return (int) Double.parseDouble(
				DECIMAL_FORMAT.format(category.getAmountWrapper().getAmount().getMaximumValue()));
	}
}
