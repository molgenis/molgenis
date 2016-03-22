package org.molgenis.data.annotation.cmd;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.entity.impl.SnpEffAnnotator;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AttributesToXref
{
	public static void main(String[] args)
	{
		DefaultEntityMetaData metaData = new DefaultEntityMetaData("theEntity");
		AttributeMetaData attr1 = new DefaultAttributeMetaData("attr1", MolgenisFieldTypes.FieldTypeEnum.STRING);
		AttributeMetaData attr2 = new DefaultAttributeMetaData("attr2", MolgenisFieldTypes.FieldTypeEnum.STRING);
		AttributeMetaData attr3 = new DefaultAttributeMetaData("attr3", MolgenisFieldTypes.FieldTypeEnum.STRING);
		AttributeMetaData attr4 = new DefaultAttributeMetaData("effect", MolgenisFieldTypes.FieldTypeEnum.STRING)
				.setDescription("EFFECT annotations: 'id | ALT | ALT_GENE | GENE | EFFECT | TYPE'");
		metaData.addAttributeMetaData(attr1, EntityMetaData.AttributeRole.ROLE_ID);
		metaData.addAttributeMetaData(attr2);
		metaData.addAttributeMetaData(attr3);
		metaData.addAttributeMetaData(attr4);
		Entity theEntity = new MapEntity(metaData);
		theEntity.set(attr1.getName(), "1");
		theEntity.set(attr2.getName(), "2");
		theEntity.set(attr3.getName(), "3");
		theEntity.set(attr4.getName(), "4 | 5 | 6 | 7 | 8 | 9, 10 | 11 | 12 | 13 | 14 | 15");

		parse(metaData, "effect", Stream.of(theEntity));
	}

	public static List<Entity> parse(EntityMetaData entityMetaData, String attributeName, Stream<Entity> inputStream)
	{
		AttributeMetaData attributeToParse = entityMetaData.getAttribute(attributeName);
		HashMap<String, Map<Integer, AttributeMetaData>> metadataMap = parseDescription(
				attributeToParse.getDescription(),
				Lists.newArrayList((new SnpEffAnnotator().snpEff().getOutputMetaData().get(0).getAttributeParts())));

		String entityName = metadataMap.keySet().iterator().next();
		DefaultEntityMetaData xrefMetaData = new DefaultEntityMetaData(entityName);
		xrefMetaData.addAttributeMetaData(new DefaultAttributeMetaData("identifier").setAuto(true).setVisible(false),
				EntityMetaData.AttributeRole.ROLE_ID);
		xrefMetaData.addAllAttributeMetaData(Lists.newArrayList(metadataMap.get(entityName).values()));
		xrefMetaData.addAttributeMetaData(
				new DefaultAttributeMetaData(entityMetaData.getSimpleName(), MolgenisFieldTypes.FieldTypeEnum.MREF));
		List<Entity> results = new ArrayList<>();
		for (Entity inputEntity : inputStream.collect(Collectors.toList()))
		{
			results.addAll(parseValue(xrefMetaData, metadataMap.get(entityName),
					inputEntity.getString(attributeToParse.getName()), inputEntity));
		}
		return results;
	}

	private static HashMap<String, Map<Integer, AttributeMetaData>> parseDescription(String description,
			List<AttributeMetaData> annotatorAttributes)
	{
		String[] step1 = description.split(":");
		String entityName = StringUtils.deleteWhitespace(step1[0]);
		String[] attributeStrings = step1[1].split("\\|");
		Map<Integer, AttributeMetaData> attributeMap = new HashMap<>();
		Map<String, AttributeMetaData> annotatorAttributeMap = attributesToMap(annotatorAttributes);
		for (int i = 0; i < attributeStrings.length; i++)
		{
			String attribute = attributeStrings[i];
			MolgenisFieldTypes.FieldTypeEnum type = annotatorAttributeMap.containsKey(attribute)
					? annotatorAttributeMap.get(attribute).getDataType().getEnumType()
					: MolgenisFieldTypes.FieldTypeEnum.STRING;
			AttributeMetaData attr = new DefaultAttributeMetaData(StringUtils.deleteWhitespace(attribute), type)
					.setLabel(attribute);
			attributeMap.put(i, attr);
		}

		HashMap<String, Map<Integer, AttributeMetaData>> result = new HashMap<>();
		result.put(entityName, attributeMap);
		return result;
	}

	private static Map<String, AttributeMetaData> attributesToMap(List<AttributeMetaData> attributeMetaDataList)
	{
		Map<String, AttributeMetaData> attributeMap = new HashMap<>();
		for (AttributeMetaData attributeMetaData : attributeMetaDataList)
		{
			attributeMap.put(attributeMetaData.getName(), attributeMetaData);
		}
		return attributeMap;

	}

	private static List<Entity> parseValue(EntityMetaData metadata, Map<Integer, AttributeMetaData> attributesMap,
			String value, Entity originalEntity)
	{
		List<Entity> result = new ArrayList<>();
		String[] valuesPerEntity = value.split(",");

		for (Integer i = 0; i < valuesPerEntity.length; i++)
		{
			String[] values = valuesPerEntity[i].split("\\|");

			MapEntity singleResult = new MapEntity(metadata);
			for (Integer j = 0; j < values.length; j++)
			{
				String attributeName = attributesMap.get(j).getName();
				String attributeValue = values[j];
				singleResult.set(attributeName, attributeValue);
				singleResult.set(originalEntity.getEntityMetaData().getSimpleName(), originalEntity);

			}
			result.add(singleResult);
		}
		return result;
	}
}
