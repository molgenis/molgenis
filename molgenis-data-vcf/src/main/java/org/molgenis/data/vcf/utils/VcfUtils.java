package org.molgenis.data.vcf.utils;

import autovalue.shaded.com.google.common.common.collect.Lists;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.google.common.io.BaseEncoding;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.datastructures.Sample;
import org.molgenis.data.vcf.datastructures.Trio;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.vcf.meta.VcfMetaInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.molgenis.MolgenisFieldTypes.AttributeType.COMPOUND;
import static org.molgenis.data.vcf.model.VcfAttributes.*;

@Component
public class VcfUtils
{
	@Autowired
	private static EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	private static AttributeMetaDataFactory attributeMetaDataFactory;
	/**
	 * Creates a internal molgenis id from a vcf entity
	 *
	 * @param vcfEntity
	 * @return the id
	 */
	public static String createId(Entity vcfEntity)
	{
		String idStr = StringUtils.strip(vcfEntity.get(CHROM).toString()) +
				"_" +
				StringUtils.strip(vcfEntity.get(POS).toString()) +
				"_" +
				StringUtils.strip(vcfEntity.get(REF).toString()) +
				"_" +
				StringUtils.strip(vcfEntity.get(ALT).toString()) +
				"_" +
				StringUtils.strip(vcfEntity.get(ID).toString()) +
				"_" +
				StringUtils.strip(vcfEntity.get(QUAL).toString()) +
				"_" +
				StringUtils.strip(vcfEntity.get(FILTER).toString());

		// use MD5 hash to prevent ids that are too long
		MessageDigest messageDigest;
		try
		{
			messageDigest = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
		byte[] md5Hash = messageDigest.digest(idStr.getBytes(Charset.forName("UTF-8")));

		// convert MD5 hash to string ids that can be safely used in URLs

		return BaseEncoding.base64Url().omitPadding().encode(md5Hash);
	}

	public static String getIdFromInfoField(String line)
	{
		int idStartIndex = line.indexOf("ID=") + 3;
		int idEndIndex = line.indexOf(',');
		return line.substring(idStartIndex, idEndIndex);
	}

	public static List<AttributeMetaData> getAtomicAttributesFromList(Iterable<AttributeMetaData> outputAttrs)
	{
		List<AttributeMetaData> result = new ArrayList<>();
		for (AttributeMetaData attributeMetaData : outputAttrs)
		{
			if (attributeMetaData.getDataType() == COMPOUND)
			{
				result.addAll(getAtomicAttributesFromList(attributeMetaData.getAttributeParts()));
			}
			else
			{
				result.add(attributeMetaData);
			}
		}
		return result;
	}

	public static Map<String, AttributeMetaData> getAttributesMapFromList(Iterable<AttributeMetaData> outputAttrs)
	{
		Map<String, AttributeMetaData> attributeMap = new LinkedHashMap<>();
		List<AttributeMetaData> attributes = getAtomicAttributesFromList(outputAttrs);
		for (AttributeMetaData attributeMetaData : attributes)
		{
			attributeMap.put(attributeMetaData.getName(), attributeMetaData);
		}
		return attributeMap;
	}

	protected static String toVcfDataType(MolgenisFieldTypes.AttributeType dataType)
	{
		switch (dataType)
		{
			case BOOL:
				return VcfMetaInfo.Type.FLAG.toString();
			case LONG:
			case DECIMAL:
				return VcfMetaInfo.Type.FLOAT.toString();
			case INT:
				return VcfMetaInfo.Type.INTEGER.toString();
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case STRING:
			case TEXT:
			case DATE:
			case DATE_TIME:
			case CATEGORICAL:
			case XREF:
			case CATEGORICAL_MREF:
			case MREF:
				return VcfMetaInfo.Type.STRING.toString();
			case COMPOUND:
			case FILE:
				throw new RuntimeException("invalid vcf data type " + dataType);
			default:
				throw new RuntimeException("unsupported vcf data type " + dataType);
		}
	}

	public static Iterator<Entity> reverseXrefMrefRelation(Iterator<Entity> annotatedRecords)
	{
		return new Iterator<Entity>()
		{
			final PeekingIterator<Entity> effects = Iterators.peekingIterator(annotatedRecords);

			EntityMetaData resultEMD;
			EntityMetaData effectsEMD;

			private void createResultEntityMetaData(Entity effect, EntityMetaData variantEMD)
			{
				if (resultEMD == null || effectsEMD == null)
				{
					effectsEMD = effect.getEntityMetaData();
					resultEMD = entityMetaDataFactory.create(variantEMD);
					resultEMD.addAttribute(attributeMetaDataFactory.create().setName(VcfWriterUtils.EFFECT).setDataType(
							MolgenisFieldTypes.AttributeType.MREF).setRefEntity(effectsEMD));
				}
			}

			@Override
			public boolean hasNext()
			{
				return effects.hasNext();
			}

			private Entity createEntityStructure(Entity variant, List<Entity> effectsForVariant)
			{
				createResultEntityMetaData(effectsForVariant.get(0), variant.getEntityMetaData());
				Entity newVariant = new DynamicEntity(resultEMD);
				newVariant.set(variant);

				if (effectsForVariant.size() > 1)
				{
					newVariant.set(VcfWriterUtils.EFFECT, effectsForVariant);
				}
				else
				{
					// is this an empty effect entity?
					Entity entity = effectsForVariant.get(0);
					boolean isEmpty = true;
					for (AttributeMetaData attr : effectsEMD.getAtomicAttributes())
					{
						if (attr.getName().equals(effectsEMD.getIdAttribute().getName())
								|| attr.getName().equals(VcfWriterUtils.VARIANT))
						{
						}
						else if (entity.get(attr.getName()) != null)
						{
							isEmpty = false;
							break;
						}
					}

					if (!isEmpty) newVariant.set(VcfWriterUtils.EFFECT, effectsForVariant);
				}
				return newVariant;
			}

			@Override
			public Entity next()
			{
				Entity variant = null;
				String peekedId;
				List<Entity> effectsForVariant = Lists.newArrayList();
				while (effects.hasNext())
				{
					peekedId = effects.peek().getEntity(VcfWriterUtils.VARIANT).getIdValue().toString();
					if (variant == null || variant.getIdValue().toString().equals(peekedId))
					{
						Entity effect = effects.next();
						variant = effect.getEntity(VcfWriterUtils.VARIANT);
						effectsForVariant.add(effect);
					}
					else
					{
						return createEntityStructure(variant, effectsForVariant);
					}
				}
				return createEntityStructure(variant, effectsForVariant);
			}
		};
	}

	public static List<Entity> createEntityStructureForVcf(EntityMetaData entityMetaData, String attributeName,
			Stream<Entity> inputStream)
	{
		return createEntityStructureForVcf(entityMetaData, attributeName, inputStream, Collections.emptyList());
	}

	private static List<Entity> createEntityStructureForVcf(EntityMetaData entityMetaData, String attributeName,
			Stream<Entity> inputStream, List<AttributeMetaData> annotatorAttributes)
	{
		AttributeMetaData attributeToParse = entityMetaData.getAttribute(attributeName);
		String description = attributeToParse.getDescription();
		if (description.indexOf(':') == -1)
		{
			throw new RuntimeException(
					"Unable to create entitystructure, missing semicolon in description of [" + attributeName + "]");
		}

		String[] step1 = description.split(":");
		String entityName = org.apache.commons.lang.StringUtils.deleteWhitespace(step1[0]);
		String value = step1[1].replaceAll("^\\s'|'$", "");

		Map<Integer, AttributeMetaData> metadataMap = parseDescription(value, annotatorAttributes);
		EntityMetaData xrefMetaData = getXrefEntityMetaData(metadataMap, entityName);

		List<Entity> results = new ArrayList<>();
		for (Entity inputEntity : inputStream.collect(Collectors.toList()))
		{
			EntityMetaData newEntityMetadata = removeRefFieldFromInfoMetadata(attributeToParse, inputEntity);
			Entity originalEntity = new DynamicEntity(newEntityMetadata);
			originalEntity.set(inputEntity);

			results.addAll(parseValue(xrefMetaData, metadataMap, inputEntity.getString(attributeToParse.getName()),
					originalEntity));
		}
		return results;
	}

	private static EntityMetaData getXrefEntityMetaData(Map<Integer, AttributeMetaData> metadataMap,
			String entityName)
	{
		EntityMetaData xrefMetaData = entityMetaDataFactory.create().setName(entityName);
		xrefMetaData.addAttribute(attributeMetaDataFactory.create().setName("identifier").setAuto(true).setVisible(false),
				EntityMetaData.AttributeRole.ROLE_ID);
		xrefMetaData.addAttributes(com.google.common.collect.Lists.newArrayList(metadataMap.values()));
		xrefMetaData
				.addAttribute(attributeMetaDataFactory.create().setName("Variant").setDataType(MolgenisFieldTypes.AttributeType.MREF));
		return xrefMetaData;
	}

	private static EntityMetaData removeRefFieldFromInfoMetadata(AttributeMetaData attributeToParse,
			Entity inputEntity)
	{
		EntityMetaData newMeta = inputEntity.getEntityMetaData();
		AttributeMetaData newInfoMetadata = newMeta.getAttribute(VcfAttributes.INFO);
		newInfoMetadata.setAttributeParts(StreamSupport
				.stream(newMeta.getAttribute(VcfAttributes.INFO).getAttributeParts().spliterator(), false)
				.filter(attr -> !attr.getName().equals(attributeToParse.getName())).collect(Collectors.toList()));
		newMeta.removeAttribute(newMeta.getAttribute(VcfAttributes.INFO));
		newMeta.addAttribute(newInfoMetadata);
		return newMeta;
	}

	private static Map<Integer, AttributeMetaData> parseDescription(String description,
			List<AttributeMetaData> annotatorAttributes)
	{
		String value = description.replaceAll("^\\s'|'$", "");

		String[] attributeStrings = value.split("\\|");
		Map<Integer, AttributeMetaData> attributeMap = new HashMap<>();
		Map<String, AttributeMetaData> annotatorAttributeMap = getAttributesMapFromList(annotatorAttributes);
		for (int i = 0; i < attributeStrings.length; i++)
		{
			String attribute = attributeStrings[i];
			MolgenisFieldTypes.AttributeType type = annotatorAttributeMap.containsKey(attribute)
					? annotatorAttributeMap.get(attribute).getDataType()
					: MolgenisFieldTypes.AttributeType.STRING;
			AttributeMetaData attr = attributeMetaDataFactory.create().setName(
					org.apache.commons.lang.StringUtils.deleteWhitespace(attribute)).setDataType(type).setLabel(attribute);
			attributeMap.put(i, attr);
		}
		return attributeMap;
	}

	private static List<Entity> parseValue(EntityMetaData metadata, Map<Integer, AttributeMetaData> attributesMap,
			String value, Entity originalEntity)
	{
		List<Entity> result = new ArrayList<>();
		String[] valuesPerEntity = value.split(",");

		for (String aValuesPerEntity : valuesPerEntity)
		{
			String[] values = aValuesPerEntity.split("\\|");

			DynamicEntity singleResult = new DynamicEntity(metadata);
			for (Integer j = 0; j < values.length; j++)
			{
				String attributeName = attributesMap.get(j).getName().replaceAll("^\'|\'$", "");
				String attributeValue = values[j];
				singleResult.set(attributeName, attributeValue);
				singleResult.set("Variant", originalEntity);

			}
			result.add(singleResult);
		}
		return result;
	}

	/**
	 *
	 * Get pedigree data from VCF Now only support child, father, mother No fancy data structure either Output:
	 * result.put(childID, Arrays.asList(new String[]{motherID, fatherID}));
	 *
	 * @param inputVcfFileScanner
	 * @return
	 * @throws FileNotFoundException
	 */
	public static HashMap<String, Trio> getPedigree(Scanner inputVcfFileScanner)
	{
		HashMap<String, Trio> result = new HashMap<>();

		while (inputVcfFileScanner.hasNextLine())
		{
			String line = inputVcfFileScanner.nextLine();

			// quit when we don't see header lines anymore
			if (!line.startsWith(VcfRepository.PREFIX))
			{
				break;
			}

			// detect pedigree line
			// expecting e.g. ##PEDIGREE=<Child=100400,Mother=100402,Father=100401>
			if (line.startsWith("##PEDIGREE"))
			{
				System.out.println("Pedigree data line: " + line);
				String childID = null;
				String motherID = null;
				String fatherID = null;

				String lineStripped = line.replace("##PEDIGREE=<", "").replace(">", "");
				String[] lineSplit = lineStripped.split(",", -1);
				for (String element : lineSplit)
				{
					if (element.startsWith("Child"))
					{
						childID = element.replace("Child=", "");
					}
					else if (element.startsWith("Mother"))
					{
						motherID = element.replace("Mother=", "");
					}
					else if (element.startsWith("Father"))
					{
						fatherID = element.replace("Father=", "");
					}
					else
					{
						throw new MolgenisDataException(
								"Expected Child, Mother or Father, but found: " + element + " in line " + line);
					}
				}

				if (childID != null && motherID != null && fatherID != null)
				{
					// good
					result.put(childID, new Trio(new Sample(childID), new Sample(motherID), new Sample(fatherID)));
				}
				else
				{
					throw new MolgenisDataException("Missing Child, Mother or Father ID in line " + line);
				}
			}
		}
		return result;
	}

}
