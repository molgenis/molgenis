package org.molgenis.data.vcf.utils;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.PeekingIterator;
import com.google.common.io.BaseEncoding;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.AttributeType;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.datastructures.Sample;
import org.molgenis.data.vcf.datastructures.Trio;
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

import static org.molgenis.AttributeType.*;
import static org.molgenis.data.vcf.model.VcfAttributes.*;
import static org.molgenis.data.vcf.utils.VcfWriterUtils.VARIANT;

@Component
public class VcfUtils
{
	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attributeFactory;

	/**
	 * Creates a internal molgenis id from a vcf entity
	 *
	 * @param vcfEntity
	 * @return the id
	 */
	public static String createId(Entity vcfEntity)
	{
		String idStr = StringUtils.strip(vcfEntity.get(CHROM).toString()) + "_" + StringUtils
				.strip(vcfEntity.get(POS).toString()) + "_" + StringUtils.strip(vcfEntity.get(REF).toString()) + "_"
				+ StringUtils.strip(vcfEntity.get(ALT).toString()) + "_" + StringUtils
				.strip(vcfEntity.get(ID).toString()) + "_" + StringUtils
				.strip(vcfEntity.get(QUAL) != null ? vcfEntity.get(QUAL).toString() : "") + "_" + StringUtils
				.strip(vcfEntity.get(FILTER) != null ? vcfEntity.get(FILTER).toString() : "");

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

	public static List<Attribute> getAtomicAttributesFromList(Iterable<Attribute> outputAttrs)
	{
		List<Attribute> result = new ArrayList<>();
		for (Attribute attribute : outputAttrs)
		{
			if (attribute.getDataType() == COMPOUND)
			{
				result.addAll(getAtomicAttributesFromList(attribute.getChildren()));
			}
			else
			{
				result.add(attribute);
			}
		}
		return result;
	}

	public static Map<String, Attribute> getAttributesMapFromList(Iterable<Attribute> outputAttrs)
	{
		Map<String, Attribute> attributeMap = new LinkedHashMap<>();
		List<Attribute> attributes = getAtomicAttributesFromList(outputAttrs);
		for (Attribute attribute : attributes)
		{
			attributeMap.put(attribute.getName(), attribute);
		}
		return attributeMap;
	}

	protected static String toVcfDataType(AttributeType dataType)
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
			case ONE_TO_MANY:
				return VcfMetaInfo.Type.STRING.toString();
			case COMPOUND:
			case FILE:
				throw new RuntimeException("invalid vcf data type " + dataType);
			default:
				throw new RuntimeException("unsupported vcf data type " + dataType);
		}
	}

	public Iterator<Entity> reverseXrefMrefRelation(Iterator<Entity> annotatedRecords)
	{
		return new Iterator<Entity>()
		{
			final PeekingIterator<Entity> effects = Iterators.peekingIterator(annotatedRecords);

			EntityType resultEMD;
			EntityType effectsEMD;

			private void createResultEntityType(Entity effect, EntityType variantEMD)
			{
				if (resultEMD == null || effectsEMD == null)
				{
					effectsEMD = effect.getEntityType();
					resultEMD = entityTypeFactory.create(variantEMD);
					resultEMD.addAttribute(attributeFactory.create().setName(VcfWriterUtils.EFFECT).setDataType(MREF)
							.setRefEntity(effectsEMD));
				}
			}

			@Override
			public boolean hasNext()
			{
				return effects.hasNext();
			}

			private Entity createEntityStructure(Entity variant, List<Entity> effectsForVariant)
			{
				createResultEntityType(effectsForVariant.get(0), variant.getEntityType());
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
					for (Attribute attr : effectsEMD.getAtomicAttributes())
					{
						if (attr.getName().equals(effectsEMD.getIdAttribute().getName()) || attr.getName()
								.equals(VARIANT))
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
					peekedId = effects.peek().getEntity(VARIANT).getIdValue().toString();
					if (variant == null || variant.getIdValue().toString().equals(peekedId))
					{
						Entity effect = effects.next();
						variant = effect.getEntity(VARIANT);
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

	public Stream<Entity> createEntityStructureForVcf(EntityType entityType, String attributeName,
			Stream<Entity> inputStream, List<Attribute> annotatorAttributes, EntityType variantEntityType)
	{
		Attribute attributeToParse = entityType.getAttribute(attributeName);
		String description = getDescription(attributeToParse);
		String[] step1 = description.split(":");
		String entityName = StringUtils.deleteWhitespace(step1[0]);
		String value = step1[1].replaceAll("^\\s'|'$", "");

		Map<Integer, Attribute> effectFieldAttributeMap = parseDescription(value, annotatorAttributes);
		EntityType effectsEntityType = getEffectsEntityType(effectFieldAttributeMap, entityName, annotatorAttributes);

		return inputStream.flatMap(
				entity -> createEntityStructureForSingleEntity(attributeToParse, effectFieldAttributeMap, effectsEntityType, entity,
						variantEntityType));
	}

	private Stream<Entity> createEntityStructureForSingleEntity(Attribute attributeToParse,
			Map<Integer, Attribute> effectFieldAttributeMap, EntityType effectsEntityType, Entity vcfInputEntity, EntityType variantEntityType)
	{
		List<Entity> results = new ArrayList<>();
		Entity variantEntity = new DynamicEntity(variantEntityType);

		for (String attr : variantEntity.getAttributeNames())
		{
			if (vcfInputEntity.getEntityType().getAttribute(attr) != null)
			{
				variantEntity.set(attr, vcfInputEntity.get(attr));
			}
		}

		List<Entity> result = createEffectsEntitiesForSingleVariant(effectsEntityType, effectFieldAttributeMap, vcfInputEntity.getString(attributeToParse.getName()),
				variantEntity).collect(Collectors.toList());
		results.addAll(result);
		return results.stream();
	}

	//Get the description of the field that needs to be parsed to determine the attributes of the Entity based on the attribute
	//for example, this line:
	//##INFO=<ID=EFFECT,Number=.,Type=String,Description="EFFECT annotations: 'Alt_Allele | Gene_Name | Annotation | Putative_impact | Gene_ID | Feature_type | Feature_ID | Transcript_biotype | Rank_total | HGVS_c | HGVS_p | cDNA_position | CDS_position | Protein_position | Distance_to_feature | Errors'">
	public String getDescription(Attribute attributeToParse)
	{
		String description = attributeToParse.getDescription();
		if (description.indexOf(':') == -1)
		{
			throw new RuntimeException(
					"Unable to create entitystructure, missing semicolon in description of [" + attributeToParse
							.getName() + "]");
		}
		return description;
	}

	private EntityType getEffectsEntityType(Map<Integer, Attribute> metadataMap, String entityName,
			List<Attribute> annotatorAttributes)
	{
		EntityType effectsEntityType = entityTypeFactory.create().setName(entityName);
		effectsEntityType.addAttribute(attributeFactory.create().setName("identifier").setAuto(true).setVisible(false),
				EntityType.AttributeRole.ROLE_ID);
		effectsEntityType.addAttributes(com.google.common.collect.Lists.newArrayList(metadataMap.values()));

		//if annotator attributes not present add them
		//check if needed for annotators running on an already annotated file
		for (Attribute attr : annotatorAttributes)
		{
			if (effectsEntityType.getAttribute(attr.getName()) == null)
			{
				effectsEntityType.addAttribute(attr);
			}
		}

		effectsEntityType.addAttribute(attributeFactory.create().setName(VARIANT).setDataType(XREF));

		return effectsEntityType;
	}

	public EntityType removeRefFieldFromInfoMetadata(Attribute attributeToParse, EntityType inputEntityType)
	{
		EntityType newMeta = entityTypeFactory.create();
		newMeta.set(inputEntityType);
		newMeta.removeAttribute(attributeToParse);
		return newMeta;
	}

	private Map<Integer, Attribute> parseDescription(String description, List<Attribute> annotatorAttributes)
	{
		String value = description.replaceAll("^\\s'|'$", "");

		String[] attributeStrings = value.split("\\|");
		Map<Integer, Attribute> attributeMap = new HashMap<>();
		Map<String, Attribute> annotatorAttributeMap = getAttributesMapFromList(annotatorAttributes);
		for (int i = 0; i < attributeStrings.length; i++)
		{
			String attribute = attributeStrings[i];
			AttributeType type = annotatorAttributeMap.containsKey(attribute) ? annotatorAttributeMap.get(attribute)
					.getDataType() : STRING;
			Attribute attr = attributeFactory.create().setName(StringUtils.deleteWhitespace(attribute))
					.setDataType(type).setLabel(attribute);
			attributeMap.put(i, attr);
		}
		return attributeMap;
	}

	private static Stream<Entity> createEffectsEntitiesForSingleVariant(EntityType effectsEntityType, Map<Integer, Attribute> effectFieldAttributeMap, String descriptionFieldsString,
			Entity variantEntity)
	{
		List<Entity> listOfEffectsEntities = new ArrayList<>();
		if (descriptionFieldsString == null) return listOfEffectsEntities.stream();
		String[] descriptionFieldValues = descriptionFieldsString.split(",");

		for (String descriptionFieldValue : descriptionFieldValues)
		{
			String[] descriptionFieldPartValues = descriptionFieldValue.split("\\|");

			DynamicEntity singleEffectsEntity = new DynamicEntity(effectsEntityType);
			for (Integer descriptionFieldIndex = 0; descriptionFieldIndex < descriptionFieldPartValues.length; descriptionFieldIndex++)
			{
				String attributeName = effectFieldAttributeMap.get(descriptionFieldIndex).getName().replaceAll("^\'|\'$", "");
				String attributeValue = descriptionFieldPartValues[descriptionFieldIndex];
				singleEffectsEntity.set(attributeName, attributeValue);
			}
			singleEffectsEntity.set(VARIANT, variantEntity);
			listOfEffectsEntities.add(singleEffectsEntity);
		}
		return listOfEffectsEntities.stream();
	}

	/**
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
