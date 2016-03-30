package org.molgenis.data.vcf.utils;

import autovalue.shaded.com.google.common.common.collect.Lists;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.google.common.io.BaseEncoding;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.datastructures.Sample;
import org.molgenis.data.vcf.datastructures.Trio;
import org.molgenis.vcf.meta.VcfMetaInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.base.Joiner.on;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterables.transform;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.vcf.VcfRepository.ALT;
import static org.molgenis.data.vcf.VcfRepository.CHROM;
import static org.molgenis.data.vcf.VcfRepository.FILTER;
import static org.molgenis.data.vcf.VcfRepository.FORMAT_GT;
import static org.molgenis.data.vcf.VcfRepository.ID;
import static org.molgenis.data.vcf.VcfRepository.INFO;
import static org.molgenis.data.vcf.VcfRepository.NAME;
import static org.molgenis.data.vcf.VcfRepository.POS;
import static org.molgenis.data.vcf.VcfRepository.QUAL;
import static org.molgenis.data.vcf.VcfRepository.REF;
import static org.molgenis.data.vcf.VcfRepository.SAMPLES;

public class VcfWriterUtils
{

	public static final String VARIANT = "VARIANT";
	public static final String EFFECT = "EFFECT";
	private static List<String> VCF_ATTRIBUTE_NAMES = Arrays.asList(new String[]
	{ CHROM, POS, ID, REF, ALT, QUAL, FILTER });

	public static final String ANNOTATION_FIELD_SEPARATOR = ";";
	private static final String PIPE_SEPARATOR = "|";
	public static final String SPACE_PIPE_SEPERATOR = " | ";

	/**
	 * Creates a internal molgenis id from a vcf entity
	 * 
	 * @param vcfEntity
	 * @return the id
	 */
	public static String createId(Entity vcfEntity)
	{
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(StringUtils.strip(vcfEntity.get(CHROM).toString()));
		strBuilder.append("_");
		strBuilder.append(StringUtils.strip(vcfEntity.get(POS).toString()));
		strBuilder.append("_");
		strBuilder.append(StringUtils.strip(vcfEntity.get(REF).toString()));
		strBuilder.append("_");
		strBuilder.append(StringUtils.strip(vcfEntity.get(ALT).toString()));
		String idStr = strBuilder.toString();

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
		String id = BaseEncoding.base64Url().omitPadding().encode(md5Hash);

		return id;
	}

	public static void writeToVcf(Entity vcfEntity, BufferedWriter writer) throws MolgenisDataException, IOException
	{
		writeToVcf(vcfEntity, Collections.emptyList(), writer);
	}

	/**
	 * Convert an vcfEntity to a VCF line Only output attributes that are in the attributesToInclude list, or all if
	 * attributesToInclude is empty
	 * 
	 * @param vcfEntity
	 * @param attributesToInclude
	 * @throws IOException,Exception
	 */
	public static void writeToVcf(Entity vcfEntity, List<String> attributesToInclude, BufferedWriter writer)
			throws MolgenisDataException, IOException
	{
		addStandardFieldsToVcf(vcfEntity, writer);
		writeInfoData(vcfEntity, writer, attributesToInclude);

		// if we have SAMPLE data, add to output VCF
		Iterable<Entity> sampleEntities = vcfEntity.getEntities(SAMPLES);
		if (sampleEntities != null)
		{
			addSampleEntitiesToVcf(sampleEntities, writer);
		}
	}

	private static String parseRefAttributesToHeaderString(Entity vcfEntity)
	{
		Iterable<AttributeMetaData> attributes = vcfEntity.getEntityMetaData().getAttributes();
		String additionalInfoFields = "";
		for (AttributeMetaData attribute : attributes)
		{
			String attributeName = attribute.getName();
			if ((attribute.getDataType().equals(MREF) || attribute.getDataType().equals(XREF))
					&& !VCF_ATTRIBUTE_NAMES.contains(attributeName) && !attributeName.equals(SAMPLES))
			{
				// If the MREF field is empty, no effects were found, so we do not add an EFFECT field to this entity
				if (vcfEntity.get(attributeName) != null)
				{
					// We are dealing with non standard Xref and Mref attributes
					// added by e.g. the SnpEff annotator,
					// which is NOT the SAMPLE_ENTITIES attribute
					additionalInfoFields = parseNonStandardRefFieldsToInfoField(vcfEntity.getEntities(attributeName),
							attribute, additionalInfoFields);
				}
			}

		}
		return additionalInfoFields;
	}

	public static Iterator<Entity> reverseXrefMrefRelation(Iterator<Entity> annotatedRecords)
	{
		return new Iterator<Entity>()
		{
			PeekingIterator<Entity> effects = Iterators.peekingIterator(annotatedRecords);

			DefaultEntityMetaData resultEMD;

			private EntityMetaData getResultEMD(EntityMetaData effectsEMD, EntityMetaData variantEMD)
			{
				if (resultEMD == null)
				{
					resultEMD = new DefaultEntityMetaData(variantEMD);
					resultEMD.addAttribute(EFFECT).setDataType(MREF).setRefEntity(effectsEMD);
				}
				return resultEMD;
			}

			@Override
			public boolean hasNext()
			{
				return effects.hasNext();
			}

			private Entity newVariant(Entity variant, List<Entity> effectsForVariant)
			{
				Entity newVariant = createEntityStructure(variant, effectsForVariant);

				return newVariant;
			}

			private Entity createEntityStructure(Entity variant, List<Entity> effectsForVariant)
			{
				EntityMetaData effectEMD = effectsForVariant.get(0).getEntityMetaData();
				Entity newVariant = new MapEntity(getResultEMD(effectEMD, variant.getEntityMetaData()));
				newVariant.set(variant);

				if (effectsForVariant.size() > 1)
				{
					newVariant.set(EFFECT, effectsForVariant);
				}
				else
				{
					// is this an empty effect entity?
					Entity entity = effectsForVariant.get(0);
					boolean isEmpty = true;
					for (AttributeMetaData attr : effectEMD.getAtomicAttributes())
					{
						if (attr.getName().equals("id") || attr.getName().equals(VARIANT))
						{
							continue;
						}
						else if (entity.get(attr.getName()) != null)
						{
							isEmpty = false;
							break;
						}
					}

					if (!isEmpty) newVariant.set(EFFECT, effectsForVariant);
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
					if (variant == null || variant.getIdValue().toString() == peekedId)
					{
						Entity effect = effects.next();
						variant = effect.getEntity(VARIANT);
						effectsForVariant.add(effect);
					}
					else
					{
						Entity newVariant = newVariant(variant, effectsForVariant);
						return newVariant;
					}
				}
				return newVariant(variant, effectsForVariant);
			}
		};
	}

	public static void writeVcfHeader(File inputVcfFile, BufferedWriter outputVCFWriter,
			List<AttributeMetaData> infoFields) throws MolgenisInvalidFormatException, IOException
	{
		writeVcfHeader(inputVcfFile, outputVCFWriter, infoFields, Collections.emptyList());
	}

	/**
	 * Add standard columns to VCF. Return a String containing fields and values from non-standard columns.
	 * 
	 * @param vcfEntity
	 * @param writer
	 * @return
	 * @throws IOException
	 */
	private static void addStandardFieldsToVcf(Entity vcfEntity, BufferedWriter writer) throws IOException
	{
		for (String attribute : VCF_ATTRIBUTE_NAMES)
		{
			String value = vcfEntity.getString(attribute);
			if (value != null && !value.isEmpty())
			{
				writer.write(value);
			}
			else
			{
				writer.write('.');
			}
			writer.write('\t');
		}
	}

	/**
	 * Create a INFO field annotation and add values
	 * 
	 * @param refEntities
	 * @param attribute
	 * @param additionalInfoFields
	 */
	private static String parseNonStandardRefFieldsToInfoField(Iterable<Entity> refEntities,
			AttributeMetaData attribute, String additionalInfoFields)
	{
		boolean secondValuePresent = false;
		for (Entity refEntity : refEntities)
		{
			Iterable<AttributeMetaData> refAttributes = refEntity.getEntityMetaData().getAttributes();
			if (!secondValuePresent)
			{
				additionalInfoFields = additionalInfoFields + attribute.getName() + "=";
				additionalInfoFields = addEntityValuesToAdditionalInfoField(additionalInfoFields, refEntity,
						refAttributes);
			}
			else
			{
				additionalInfoFields = additionalInfoFields + ",";
				additionalInfoFields = addEntityValuesToAdditionalInfoField(additionalInfoFields, refEntity,
						refAttributes);
			}
			secondValuePresent = true;
		}
		additionalInfoFields = additionalInfoFields + ANNOTATION_FIELD_SEPARATOR;
		return additionalInfoFields;
	}

	/**
	 * Add the values of each EFFECT entity to the info field
	 *
	 * @param additionalInfoFields
	 * @param refEntity
	 * @param refAttributes
	 * @return
	 */
	private static String addEntityValuesToAdditionalInfoField(String additionalInfoFields, Entity refEntity,
			Iterable<AttributeMetaData> refAttributes)
	{
		boolean secondValuePresent = false;
		AttributeMetaData idAttribute = refEntity.getEntityMetaData().getIdAttribute();
		for (AttributeMetaData refAttribute : refAttributes)
		{
			if (!refAttribute.isSameAs(idAttribute) && (refAttribute.getDataType() != XREF))
				if (!refAttribute.isSameAs(idAttribute) && !refAttribute.getDataType().equals(MREF)
						&& !refAttribute.getDataType().equals(XREF))
			{
				if (secondValuePresent) additionalInfoFields = additionalInfoFields + PIPE_SEPARATOR;
				String value = refEntity.getString(refAttribute.getName()) == null ? ""
						: refEntity.getString(refAttribute.getName());
				additionalInfoFields = additionalInfoFields + value;
				secondValuePresent = true;

			}
		}
		return additionalInfoFields;
	}

	/**
	 * Checks for previous annotations
	 * 
	 * @param inputVcfFile
	 * @param outputVCFWriter
	 * @param infoFields
	 * @param attributesToInclude
	 *            , the AttributeMetaData to write to the VCF file, if empty writes all attributes
	 * @return
	 * @throws MolgenisInvalidFormatException
	 * @throws IOException
	 */
	public static void writeVcfHeader(File inputVcfFile, BufferedWriter outputVCFWriter,
			List<AttributeMetaData> infoFields, List<String> attributesToInclude)
					throws MolgenisInvalidFormatException, IOException
	{
		System.out.println("Detecting VCF column header...");

		Scanner inputVcfFileScanner = new Scanner(inputVcfFile, "UTF-8");
		String line = inputVcfFileScanner.nextLine();

		// if first line does not start with ##, we don't trust this file as VCF
		Map<String, String> infoHeaderLinesMap = new LinkedHashMap<>();
		if (line.startsWith(VcfRepository.PREFIX))
		{
			line = processHeaderLines(outputVCFWriter, inputVcfFileScanner, line, infoHeaderLinesMap);
			System.out.println("\nHeader line found:\n" + line);

			// check the header line
			checkHeaderLine(outputVCFWriter, inputVcfFileScanner, line);
			writeInfoHeaderLines(outputVCFWriter, infoFields, attributesToInclude, infoHeaderLinesMap);
			writeColumnHeaders(outputVCFWriter, line);
		}
		else
		{
			outputVCFWriter.close();
			inputVcfFileScanner.close();
			throw new MolgenisInvalidFormatException(
					"Did not find ## on the first line, are you sure it is a VCF file?");
		}

		inputVcfFileScanner.close();
	}

	private static String processHeaderLines(BufferedWriter outputVCFWriter, Scanner inputVcfFileScanner, String line,
			Map<String, String> infoHeaderLinesMap) throws IOException
	{
		while (inputVcfFileScanner.hasNextLine())
		{
			if (line.startsWith(VcfRepository.PREFIX + VcfRepository.INFO))
			{
				infoHeaderLinesMap.put(getIdFromInfoField(line), line);
			}
			else if (line.startsWith(VcfRepository.PREFIX))
			{
				outputVCFWriter.write(line);
				outputVCFWriter.newLine();
			}
			else
			{
				break;
			}
			line = inputVcfFileScanner.nextLine();

			System.out.print(".");
		}
		return line;
	}

	private static void checkHeaderLine(BufferedWriter outputVCFWriter, Scanner inputVcfFileScanner, String line)
			throws IOException, MolgenisInvalidFormatException
	{
		if (!line.startsWith(CHROM))
		{
			outputVCFWriter.close();
			inputVcfFileScanner.close();
			throw new MolgenisInvalidFormatException(
					"Header does not start with #CHROM, are you sure it is a VCF file?");
		}
	}

	private static String getIdFromInfoField(String line)
	{
		int idStartIndex = line.indexOf("ID=") + 3;
		int idEndIndex = line.indexOf(",");
		return line.substring(idStartIndex, idEndIndex);
	}

	private static void writeColumnHeaders(BufferedWriter outputVCFWriter, String line) throws IOException
	{
		outputVCFWriter.write(line);
		outputVCFWriter.newLine();
	}

	private static void writeInfoHeaderLines(BufferedWriter outputVCFWriter, List<AttributeMetaData> infoFields,
			List<String> attributesToInclude, Map<String, String> infoHeaderLinesMap) throws IOException
	{
		Map<String, AttributeMetaData> annotatorAttributes = getAttributesMapFromList(infoFields);
		writeNonCurrentAnnotatorInfoFields(outputVCFWriter, infoHeaderLinesMap, annotatorAttributes);
		writeCurrentAnnotatorInfoFields(outputVCFWriter, attributesToInclude, infoHeaderLinesMap, annotatorAttributes);
	}

	private static void writeCurrentAnnotatorInfoFields(BufferedWriter outputVCFWriter,
			List<String> attributesToInclude, Map<String, String> infoHeaderLinesMap,
			Map<String, AttributeMetaData> annotatorAttributes) throws IOException
	{
		for (String annotatorInfoAttr : annotatorAttributes.keySet())
		{
			if (attributesToInclude.isEmpty() || attributesToInclude.contains(annotatorInfoAttr))
			{
				outputVCFWriter.write(createInfoStringFromAttribute(annotatorAttributes.get(annotatorInfoAttr),
						infoHeaderLinesMap.get(annotatorInfoAttr)));
				outputVCFWriter.newLine();
			}
		}
	}

	private static void writeNonCurrentAnnotatorInfoFields(BufferedWriter outputVCFWriter,
			Map<String, String> infoHeaderLinesMap, Map<String, AttributeMetaData> annotatorAttributes)
					throws IOException
	{
		for (String infoHeaderFieldKey : infoHeaderLinesMap.keySet())
		{
			if (!annotatorAttributes.containsKey(infoHeaderFieldKey))
			{
				outputVCFWriter.write(infoHeaderLinesMap.get(infoHeaderFieldKey));
				outputVCFWriter.newLine();
			}
		}
	}

	public static List<AttributeMetaData> getAtomicAttributesFromList(Iterable<AttributeMetaData> outputAttrs)
	{
		List<AttributeMetaData> result = new ArrayList<>();
		for (AttributeMetaData attributeMetaData : outputAttrs)
		{
			if (attributeMetaData.getDataType().getEnumType().equals(MolgenisFieldTypes.FieldTypeEnum.COMPOUND))
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
		for (AttributeMetaData attr : attributes)
		{
			attributeMap.put(attr.getName(), attr);
		}
		return attributeMap;
	}

	/**
	 * Parses the info field from a MOLGENIS entity and writes it into VCF INFO column format
	 * 
	 * @param vcfEntity
	 * @param writer
	 * @param attributesToInclude
	 * @throws IOException
	 */
	private static void writeInfoData(Entity vcfEntity, BufferedWriter writer, List<String> attributesToInclude)
			throws IOException
	{
		boolean hasInfoFields = false;

		String refEntityAttributesInfoFields = parseRefAttributesToHeaderString(vcfEntity);
		// flexible 'info' field, one column with potentially many data items
		for (AttributeMetaData attributeMetaData : vcfEntity.getEntityMetaData().getAttribute(INFO).getAttributeParts())
		{
			String infoAttrName = attributeMetaData.getName();
			if ((attributesToInclude.isEmpty() || attributesToInclude.contains(infoAttrName)))
			{
				if (attributeMetaData.getDataType().getEnumType() == FieldTypeEnum.BOOL)
				{
					Boolean infoAttrBoolValue = vcfEntity.getBoolean(infoAttrName);
					if (infoAttrBoolValue != null && infoAttrBoolValue.booleanValue() == true)
					{
						writer.append(infoAttrName);
						writer.append(ANNOTATION_FIELD_SEPARATOR);
						hasInfoFields = true;
					}
				}
				else
				{
					String infoAttrStringValue = vcfEntity.getString(infoAttrName);
					if (infoAttrStringValue != null)
					{
						writer.append(infoAttrName);
						writer.append('=');
						writer.append(infoAttrStringValue);
						writer.append(ANNOTATION_FIELD_SEPARATOR);
						hasInfoFields = true;
					}
				}
			}
		}
		if (!isNullOrEmpty(refEntityAttributesInfoFields))
		{
			writer.append(refEntityAttributesInfoFields);
			hasInfoFields = true;
		}

		if (!hasInfoFields)
		{
			writer.append('.');
		}
	}

	/**
	 * Adds sample entities to a vcf
	 * 
	 * @param sampleEntities
	 * @param writer
	 * @throws IOException
	 */
	private static void addSampleEntitiesToVcf(Iterable<Entity> sampleEntities, BufferedWriter writer)
			throws IOException
	{
		boolean firstSample = true;
		for (Iterator<Entity> it = sampleEntities.iterator(); it.hasNext();)
		{
			if (firstSample)
			{
				writer.append('\t');
			}

			Entity sample = it.next();

			StringBuilder formatColumn = new StringBuilder();
			StringBuilder sampleColumn = new StringBuilder();

			// write GT first if available
			if (sample.getEntityMetaData().getAttribute(FORMAT_GT) != null)
			{
				if (firstSample)
				{
					formatColumn.append(FORMAT_GT);
					formatColumn.append(':');
				}
				String sampleAttrValue = sample.getString(FORMAT_GT);
				if (sampleAttrValue != null)
				{
					sampleColumn.append(sampleAttrValue);
					sampleColumn.append(':');
				}
				else
				{
					sampleColumn.append(".:");
				}
			}

			for (AttributeMetaData sampleAttr : sample.getEntityMetaData().getAttributes())
			{
				String sampleAttribute = sampleAttr.getName();
				if (!sampleAttribute.equals(FORMAT_GT))
				{
					// leave out autogenerated ID and NAME columns since this
					// will greatly bloat the output file for many samples
					// FIXME: chance to clash with existing ID and NAME columns
					// in FORMAT ?? what happens then?
					if (!sampleAttribute.equals(ID) && !sampleAttribute.equals(NAME))
					{
						String sampleAttrValue = sample.getString(sampleAttribute);
						if (sampleAttrValue != null)
						{
							sampleColumn.append(sampleAttrValue);
							sampleColumn.append(':');
						}
						else
						{
							sampleColumn.append(".:");
						}

						// get FORMAT fields, but only for the first time
						if (firstSample)
						{
							formatColumn.append(sampleAttribute);
							formatColumn.append(':');
						}
					}
				}
			}

			// add FORMAT data but only first time
			if (firstSample && formatColumn.length() > 0)
			{
				// delete trailing ':'
				formatColumn.deleteCharAt(formatColumn.length() - 1);
				writer.write(formatColumn.toString());
				writer.write('\t');
				firstSample = false;
			}
			else if (firstSample)
			{
				throw new MolgenisDataException(
						"Weird situation: we are at sample 1 and want to print FORMAT info but there seems to be none?");
			}

			// now add SAMPLE data
			// delete trailing ":"
			sampleColumn.deleteCharAt(sampleColumn.length() - 1);
			writer.write(sampleColumn.toString());

			if (it.hasNext())
			{
				writer.write('\t');
			}
		}
	}

	private static String createInfoStringFromAttribute(AttributeMetaData infoAttributeMetaData,
			String currentInfoField)
	{
		String attributeName = infoAttributeMetaData.getName();
		StringBuilder sb = new StringBuilder();
		if (currentInfoField == null)
		{
			sb.append("##INFO=<ID=");
			sb.append(attributeName);
			sb.append(",Number=.");// FIXME: once we support list of primitives we
			// can calculate based on combination of
			// type and nillable
			sb.append(",Type=");
			sb.append(toVcfDataType(infoAttributeMetaData.getDataType().getEnumType()));
			sb.append(",Description=\"");
		}
		// http://samtools.github.io/hts-specs/VCFv4.1.pdf --> "The Description
		// value must be surrounded by
		// double-quotes. Double-quote character can be escaped with backslash \
		// and backslash as \\."
		if (StringUtils.isBlank(infoAttributeMetaData.getDescription()))
		{
			if ((infoAttributeMetaData.getDataType().equals(MREF) || infoAttributeMetaData.getDataType().equals(XREF))
					&& !attributeName.equals(SAMPLES))
			{
				Iterable<AttributeMetaData> atomicAttributes = infoAttributeMetaData.getRefEntity()
						.getAtomicAttributes();
				EntityMetaData emd = infoAttributeMetaData.getRefEntity();
				AttributeMetaData idAttribute = emd.getIdAttribute();
				String description;
				if (currentInfoField == null)
				{
					description = attributeName + " annotations: '";
				}
				else
				{
					description = currentInfoField.replace("'\">", SPACE_PIPE_SEPERATOR);
				}
				description = description + (filterRefAttributes(atomicAttributes, idAttribute).replace("\\", "\\\\")
						.replace("\"", "\\\"").replace("\n", " "));

				description = description + "'";
				((DefaultAttributeMetaData) infoAttributeMetaData).setDescription(description);
			}
			else
			{
				((DefaultAttributeMetaData) infoAttributeMetaData)
						.setDescription(VcfRepository.DEFAULT_ATTRIBUTE_DESCRIPTION);
			}
		}
		sb.append(infoAttributeMetaData.getDescription());
		sb.append("\">");
		return sb.toString();
	}

	private static String filterRefAttributes(Iterable<AttributeMetaData> atomicAttributes,
			final AttributeMetaData idAttribute)
	{
		return on(SPACE_PIPE_SEPERATOR)
				.join(Iterables.filter(transform(atomicAttributes, AttributeMetaData::getName), new Predicate<Object>()
				{
					@Override
					public boolean apply(Object attributeMetaData)
					{
						if (idAttribute == null || !attributeMetaData.equals(idAttribute.getName()))
						{
							return true;
						}
						else
						{
							return false;
						}
					}
				}));
	}

	private static String toVcfDataType(MolgenisFieldTypes.FieldTypeEnum dataType)
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

	public static List<Entity> parseData(EntityMetaData entityMetaData, String attributeName, Stream<Entity> inputStream)
	{
		return parseData(entityMetaData, attributeName, inputStream, Collections.emptyList());
	}

	public static List<Entity> parseData(EntityMetaData entityMetaData, String attributeName, Stream<Entity> inputStream,
			List<AttributeMetaData> annotatorAttributes)
	{
		AttributeMetaData attributeToParse = entityMetaData.getAttribute(attributeName);
		HashMap<String, Map<Integer, AttributeMetaData>> metadataMap = parseDescription(
				attributeToParse.getDescription(), annotatorAttributes);

		String entityName = metadataMap.keySet().iterator().next();
		DefaultEntityMetaData xrefMetaData = new DefaultEntityMetaData(entityName);
		xrefMetaData.addAttributeMetaData(new DefaultAttributeMetaData("identifier").setAuto(true).setVisible(false),
				EntityMetaData.AttributeRole.ROLE_ID);
		xrefMetaData.addAllAttributeMetaData(
				com.google.common.collect.Lists.newArrayList(metadataMap.get(entityName).values()));
		xrefMetaData
				.addAttributeMetaData(new DefaultAttributeMetaData("Variant", MolgenisFieldTypes.FieldTypeEnum.MREF));
		List<Entity> results = new ArrayList<>();
		for (Entity inputEntity : inputStream.collect(Collectors.toList()))
		{
			DefaultEntityMetaData newEntityMetadata = removeRefFieldFromInfoMetadata(attributeToParse, inputEntity);
			Entity originalEntity = new MapEntity(inputEntity, newEntityMetadata);

			results.addAll(parseValue(xrefMetaData, metadataMap.get(entityName),
					inputEntity.getString(attributeToParse.getName()), originalEntity));
		}
		return results;
	}

	private static DefaultEntityMetaData removeRefFieldFromInfoMetadata(AttributeMetaData attributeToParse,
			Entity inputEntity)
	{
		DefaultEntityMetaData newMeta = (DefaultEntityMetaData) inputEntity.getEntityMetaData();
		DefaultAttributeMetaData newInfoMetadata = (DefaultAttributeMetaData) newMeta.getAttribute(VcfRepository.INFO);
		newInfoMetadata.setAttributesMetaData(StreamSupport
				.stream(newMeta.getAttribute(VcfRepository.INFO).getAttributeParts().spliterator(), false)
				.filter(attr -> !attr.getName().equals(attributeToParse.getName())).collect(Collectors.toList()));
		newMeta.removeAttributeMetaData(VcfRepository.INFO_META);
		newMeta.addAttributeMetaData(newInfoMetadata);
		return newMeta;
	}

	private static HashMap<String, Map<Integer, AttributeMetaData>> parseDescription(String description,
			List<AttributeMetaData> annotatorAttributes)
	{
		String[] step1 = description.split(":");
		String entityName = org.apache.commons.lang.StringUtils.deleteWhitespace(step1[0]);
		String value = step1[1].replaceAll("^\\s'|'$", "");

		String[] attributeStrings = value.split("\\|");
		Map<Integer, AttributeMetaData> attributeMap = new HashMap<>();
		Map<String, AttributeMetaData> annotatorAttributeMap = attributesToMap(annotatorAttributes);
		for (int i = 0; i < attributeStrings.length; i++)
		{
			String attribute = attributeStrings[i];
			MolgenisFieldTypes.FieldTypeEnum type = annotatorAttributeMap.containsKey(attribute)
					? annotatorAttributeMap.get(attribute).getDataType().getEnumType()
					: MolgenisFieldTypes.FieldTypeEnum.STRING;
			AttributeMetaData attr = new DefaultAttributeMetaData(
					org.apache.commons.lang.StringUtils.deleteWhitespace(attribute), type).setLabel(attribute);
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
	 * @param inputVcfFile
	 * @return
	 * @throws FileNotFoundException
	 */
	public static HashMap<String, Trio> getPedigree(File inputVcfFile) throws FileNotFoundException
	{
		HashMap<String, Trio> result = new HashMap<String, Trio>();

		Scanner inputVcfFileScanner = new Scanner(inputVcfFile, "UTF-8");
		String line = inputVcfFileScanner.nextLine();

		// if first line does not start with ##, we don't trust this file as VCF
		if (line.startsWith(VcfRepository.PREFIX))
		{
			while (inputVcfFileScanner.hasNextLine())
			{
				// detect pedigree line
				// expecting:
				// ##PEDIGREE=<Child=100400,Mother=100402,Father=100401>
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
							inputVcfFileScanner.close();
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
						inputVcfFileScanner.close();
						throw new MolgenisDataException("Missing Child, Mother or Father ID in line " + line);
					}
				}

				line = inputVcfFileScanner.nextLine();
				if (!line.startsWith(VcfRepository.PREFIX))
				{
					break;
				}
			}
		}

		inputVcfFileScanner.close();
		return result;
	}
}
