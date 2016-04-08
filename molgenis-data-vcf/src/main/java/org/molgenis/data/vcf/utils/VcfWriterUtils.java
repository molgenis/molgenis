package org.molgenis.data.vcf.utils;

import autovalue.shaded.com.google.common.common.collect.Lists;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.vcf.VcfRepository;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

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
import static org.molgenis.data.vcf.VcfRepository.POS;
import static org.molgenis.data.vcf.VcfRepository.QUAL;
import static org.molgenis.data.vcf.VcfRepository.REF;
import static org.molgenis.data.vcf.VcfRepository.SAMPLES;

public class VcfWriterUtils
{
	public static final String VARIANT = "VARIANT";
	public static final String EFFECT = "EFFECT";
	public static final char ANNOTATION_FIELD_SEPARATOR = ';';
	public static final String SPACE_PIPE_SEPERATOR = " | ";

	private static final LinkedList<String> VCF_ATTRIBUTE_NAMES = new LinkedList<>(Arrays.asList(new String[]
	{ CHROM, POS, ID, REF, ALT, QUAL, FILTER }));
	private static final char PIPE_SEPARATOR = '|';

	/**
	 * Convert an vcfEntity to a VCF line Only output attributes that are in the attributesToInclude list, or all if
	 * attributesToInclude is empty
	 *
	 * @param inputVcfFile
	 * @param outputVCFWriter
	 * @param addedAttributes
	 * @throws IOException,MolgenisInvalidFormatException
	 */
	public static void writeVcfHeader(File inputVcfFile, BufferedWriter outputVCFWriter,
			List<AttributeMetaData> addedAttributes) throws MolgenisInvalidFormatException, IOException
	{
		writeVcfHeader(inputVcfFile, outputVCFWriter, addedAttributes, Collections.emptyList());
	}

	/**
	 * Checks for previous annotations
	 *
	 * @param inputVcfFile
	 * @param outputVCFWriter
	 * @param addedAttributes
	 * @param attributesToInclude
	 *            , the AttributeMetaData to write to the VCF file, if empty writes all attributes
	 * @return
	 * @throws MolgenisInvalidFormatException
	 * @throws IOException
	 */
	public static void writeVcfHeader(File inputVcfFile, BufferedWriter outputVCFWriter,
									  List<AttributeMetaData> addedAttributes, List<String> attributesToInclude)
			throws MolgenisInvalidFormatException, IOException
	{
		System.out.println("Detecting VCF column header...");

		Scanner inputVcfFileScanner = new Scanner(inputVcfFile, "UTF-8");
		String line = inputVcfFileScanner.nextLine();

		Map<String, String> infoHeaderLinesMap = new LinkedHashMap<>();
		if (line.startsWith(VcfRepository.PREFIX))
		{
			line = processHeaders(outputVCFWriter, inputVcfFileScanner, line, infoHeaderLinesMap);
			System.out.println("\nHeader line found:\n" + line);

			checkColumnHeaders(outputVCFWriter, inputVcfFileScanner, line);
			writeInfoHeaders(outputVCFWriter, addedAttributes, attributesToInclude, infoHeaderLinesMap);
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

	/**
	 * Convert an vcfEntity to a VCF line Only output attributes that are in the attributesToInclude list, or all if
	 * attributesToInclude is empty
	 *
	 * @param vcfEntity
	 * @param addedAttributes
	 * @param attributesToInclude
	 * @param writer
	 * @throws IOException,Exception
	 */
	public static void writeToVcf(Entity vcfEntity, List<AttributeMetaData> addedAttributes,
			List<String> attributesToInclude, BufferedWriter writer) throws MolgenisDataException, IOException
	{
		addStandardFieldsToVcf(vcfEntity, writer);
		writeInfoData(vcfEntity, writer, addedAttributes, attributesToInclude);

		// if we have SAMPLE data, add to output VCF
		Iterable<Entity> sampleEntities = vcfEntity.getEntities(SAMPLES);
		if (sampleEntities != null)
		{
			addSampleEntitiesToVcf(sampleEntities, writer);
		}
	}

	// ****************
	// * Parse header *
	// ****************

	private static String processHeaders(BufferedWriter outputVCFWriter, Scanner inputVcfFileScanner, String line,
			Map<String, String> infoHeaderLinesMap) throws IOException
	{
		while (inputVcfFileScanner.hasNextLine())
		{
			if (line.startsWith(VcfRepository.PREFIX + VcfRepository.INFO))
			{
				infoHeaderLinesMap.put(VcfUtils.getIdFromInfoField(line), line);
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

	private static void checkColumnHeaders(BufferedWriter outputVCFWriter, Scanner inputVcfFileScanner, String line)
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

	// ****************
	// * Write header *
	// ****************

	private static void writeColumnHeaders(BufferedWriter outputVCFWriter, String line) throws IOException
	{
		outputVCFWriter.write(line);
		outputVCFWriter.newLine();
	}

	private static void writeInfoHeaders(BufferedWriter outputVCFWriter, List<AttributeMetaData> annotatorAttributes,
			List<String> attributesToInclude, Map<String, String> infoHeaderLinesMap) throws IOException
	{
		Map<String, AttributeMetaData> annotatorAttributesMap = VcfUtils.getAttributesMapFromList(annotatorAttributes);
		writeExistingInfoHeaders(outputVCFWriter, infoHeaderLinesMap, annotatorAttributesMap);
		writeAddedInfoHeaders(outputVCFWriter, attributesToInclude, annotatorAttributesMap);
	}

	private static void writeAddedInfoHeaders(BufferedWriter outputVCFWriter, List<String> attributesToInclude,
			Map<String, AttributeMetaData> annotatorAttributes) throws IOException
	{
		for (AttributeMetaData annotatorInfoAttr : annotatorAttributes.values())
		{
			if (attributesToInclude.isEmpty() || attributesToInclude.contains(annotatorInfoAttr.getName())
					|| annotatorInfoAttr.getDataType().equals(XREF) || annotatorInfoAttr.getDataType().equals(MREF))
			{
				outputVCFWriter.write(createInfoStringFromAttribute(
						annotatorAttributes.get(annotatorInfoAttr.getName()), attributesToInclude));
				outputVCFWriter.newLine();
			}
		}
	}

	private static String createInfoStringFromAttribute(AttributeMetaData infoAttributeMetaData,
			List<String> attributesToInclude)
	{
		String attributeName = infoAttributeMetaData.getName();
		StringBuilder sb = new StringBuilder();

		sb.append("##INFO=<ID=");
		sb.append(attributeName);
		// FIXME: once we support list of primitives we can calculate based on combination of type and nillable
		sb.append(",Number=.");
		sb.append(",Type=");
		sb.append(VcfUtils.toVcfDataType(infoAttributeMetaData.getDataType().getEnumType()));
		sb.append(",Description=\"");

		// http://samtools.github.io/hts-specs/VCFv4.1.pdf --> "The Description
		// value must be surrounded by double-quotes. Double-quote character can be escaped with backslash \ and
		// backslash as \\."
		if (StringUtils.isBlank(infoAttributeMetaData.getDescription()))
		{
			if ((infoAttributeMetaData.getDataType().equals(MREF) || infoAttributeMetaData.getDataType().equals(XREF))
					&& !attributeName.equals(SAMPLES))
			{
				writeAttributesToInfoDescription(infoAttributeMetaData, attributesToInclude, attributeName, sb);
			}
			else
			{
				sb.append(VcfRepository.DEFAULT_ATTRIBUTE_DESCRIPTION);
			}
		}
		else
		{
			sb.append(infoAttributeMetaData.getDescription());
		}
		sb.append("\">");
		return sb.toString();
	}

	private static void writeAttributesToInfoDescription(AttributeMetaData infoAttributeMetaData,
			List<String> attributesToInclude, String attributeName, StringBuilder sb)
	{
		Iterable<AttributeMetaData> atomicAttributes = infoAttributeMetaData.getRefEntity().getAtomicAttributes();
		sb.append(attributeName);
		sb.append(" annotations: '");
		sb.append(refAttributesToString(atomicAttributes, attributesToInclude).replace("\\", "\\\\")
				.replace("\"", "\\\"").replace("\n", " "));
		sb.append("'");
	}

	private static String refAttributesToString(Iterable<AttributeMetaData> atomicAttributes,
			List<String> attributesToInclude)
	{
		Iterable<AttributeMetaData> attributes = Iterables.filter(atomicAttributes, new Predicate<AttributeMetaData>()
		{
			@Override
			public boolean apply(AttributeMetaData attributeMetaData)
			{
				return (attributeMetaData.isVisible() && isOutputAttribute(attributeMetaData,
						Lists.newArrayList(atomicAttributes), attributesToInclude));
			}
		});
		return on(SPACE_PIPE_SEPERATOR).join(transform(attributes, AttributeMetaData::getName));
	}

	private static void writeExistingInfoHeaders(BufferedWriter outputVCFWriter, Map<String, String> infoHeaderLinesMap,
			Map<String, AttributeMetaData> annotatorAttributes) throws IOException
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

	// ***************
	// * Write data *
	// ***************

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

	private static void writeInfoData(Entity vcfEntity, BufferedWriter writer,
			List<AttributeMetaData> annotatorAttributes, List<String> attributesToInclude) throws IOException
	{
		boolean hasInfoFields = false;

		for (AttributeMetaData attributeMetaData : vcfEntity.getEntityMetaData().getAttribute(INFO).getAttributeParts())
		{
			if (isOutputAttribute(attributeMetaData, annotatorAttributes, attributesToInclude))
			{
				hasInfoFields = writeSingleInfoField(vcfEntity, writer, hasInfoFields, attributeMetaData);
			}
		}

		String refEntityAttributesInfoFields = parseRefAttributesToDataString(vcfEntity, annotatorAttributes,
				attributesToInclude);
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

	private static String parseRefAttributesToDataString(Entity vcfEntity, List<AttributeMetaData> annotatorAttributes,
			List<String> attributesToInclude)
	{
		Iterable<AttributeMetaData> attributes = vcfEntity.getEntityMetaData().getAttributes();
		StringBuilder refEntityInfoFields = new StringBuilder();
		for (AttributeMetaData attribute : attributes)
		{
			String attributeName = attribute.getName();
			if ((attribute.getDataType().equals(MREF) || attribute.getDataType().equals(XREF))
					&& !attributeName.equals(SAMPLES))
			{
				// If the MREF field is empty, no effects were found, so we do not add an EFFECT field to this entity
				if (vcfEntity.get(attributeName) != null
						&& isOutputAttribute(attribute, annotatorAttributes, attributesToInclude))
				{
					parseRefFieldsToInfoField(vcfEntity.getEntities(attributeName), attribute, refEntityInfoFields,
							annotatorAttributes, attributesToInclude);
				}
			}

		}
		return refEntityInfoFields.toString();
	}

	private static boolean writeSingleInfoField(Entity vcfEntity, BufferedWriter writer, boolean hasInfoFields,
			AttributeMetaData attributeMetaData) throws IOException
	{
		String infoAttrName = attributeMetaData.getName();
		if (attributeMetaData.getDataType().getEnumType() == FieldTypeEnum.BOOL)
		{
			Boolean infoAttrBoolValue = vcfEntity.getBoolean(infoAttrName);
			if (infoAttrBoolValue != null && infoAttrBoolValue)
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
		return hasInfoFields;
	}

	/**
	 * Create a INFO field annotation and add values
	 */
	private static void parseRefFieldsToInfoField(Iterable<Entity> refEntities, AttributeMetaData attribute,
			StringBuilder refEntityInfoFields, List<AttributeMetaData> annotatorAttributes,
			List<String> attributesToInclude)
	{
		boolean secondValuePresent = false;
		for (Entity refEntity : refEntities)
		{
			Iterable<AttributeMetaData> refAttributes = refEntity.getEntityMetaData().getAttributes();
			if (!secondValuePresent)
			{
				refEntityInfoFields.append(attribute.getName());
				refEntityInfoFields.append("=");
				addEntityValuesToRefEntityInfoField(refEntityInfoFields, refEntity, refAttributes, annotatorAttributes,
						attributesToInclude);
			}
			else
			{
				refEntityInfoFields.append(",");
				addEntityValuesToRefEntityInfoField(refEntityInfoFields, refEntity, refAttributes, annotatorAttributes,
						attributesToInclude);
			}
			secondValuePresent = true;
		}
		refEntityInfoFields.append(ANNOTATION_FIELD_SEPARATOR);
	}

	/**
	 * Add the values of each EFFECT entity to the info field
	 */
	private static void addEntityValuesToRefEntityInfoField(StringBuilder refEntityInfoFields, Entity refEntity,
			Iterable<AttributeMetaData> refAttributes, List<AttributeMetaData> annotatorAttributes,
			List<String> attributesToInclude)
	{
		boolean previousValuePresent = false;
		for (AttributeMetaData refAttribute : refAttributes)
		{
			if (refAttribute.isVisible() && (refAttribute.getDataType() != XREF)
					&& !refAttribute.getDataType().equals(MREF)
					&& isOutputAttribute(refAttribute, annotatorAttributes, attributesToInclude))
			{
				if (previousValuePresent) refEntityInfoFields.append(PIPE_SEPARATOR);
				String value = refEntity.getString(refAttribute.getName()) == null ? ""
						: refEntity.getString(refAttribute.getName());
				refEntityInfoFields.append(value);
				previousValuePresent = true;
			}
		}
	}

	// *****************
	// * Write samples *
	// *****************

	private static void addSampleEntitiesToVcf(Iterable<Entity> sampleEntities, BufferedWriter writer)
			throws IOException
	{
		boolean first = true;
		for (Entity sample : sampleEntities)
		{
			writer.append('\t');
			if (first)
			{
				writeFormatString(writer, sample);
			}
			writeSampleData(writer, sample);
			first = false;
		}
	}

	private static void writeSampleData(BufferedWriter writer, Entity sample) throws IOException
	{
		StringBuilder sampleColumn = new StringBuilder();
		if (sample.getEntityMetaData().getAttribute(FORMAT_GT) != null)
		{
			String sampleAttrValue = sample.getString(FORMAT_GT);
			if (sampleAttrValue != null)
			{
				sampleColumn.append(sampleAttrValue);
			}
			else
			{
				sampleColumn.append(".");
			}

		}

		EntityMetaData entityMetadata = sample.getEntityMetaData();
		for (AttributeMetaData sampleAttribute : entityMetadata.getAttributes())
		{
			String sampleAttributeName = sampleAttribute.getName();
			if (!sampleAttributeName.equals(FORMAT_GT))
			{
				// skip the field that were generated for the use of the entity within molgenis
				if (!sampleAttribute.equals(entityMetadata.getIdAttribute())
						&& !sampleAttribute.equals(entityMetadata.getLabelAttribute()))
				{
					if (sampleColumn.length() != 0) sampleColumn.append(":");
					String sampleAttrValue = sample.getString(sampleAttributeName);
					if (sampleAttrValue != null)
					{
						sampleColumn.append(sampleAttrValue);
					}
					else
					{
						sampleColumn.append(".");
					}
				}
			}
		}
		writer.write(sampleColumn.toString());
	}

	private static void writeFormatString(BufferedWriter writer, Entity sample) throws IOException
	{
		StringBuilder formatColumn = new StringBuilder();
		// write GT first if available
		if (sample.getEntityMetaData().getAttribute(FORMAT_GT) != null)
		{
			formatColumn.append(FORMAT_GT);
		}
		EntityMetaData entityMetadata = sample.getEntityMetaData();
		for (AttributeMetaData sampleAttribute : entityMetadata.getAttributes())
		{
			String sampleAttributeName = sampleAttribute.getName();
			if (!sampleAttributeName.equals(FORMAT_GT))
			{
				// skip the field that were generated for the use of the entity within molgenis
				if (!sampleAttribute.equals(entityMetadata.getIdAttribute())
						&& !sampleAttribute.equals(entityMetadata.getLabelAttribute()))
				{
					if (formatColumn.length() != 0) formatColumn.append(':');
					formatColumn.append(sampleAttributeName);
				}
			}
		}
		if (formatColumn.length() > 0)
		{
			formatColumn.append('\t');
			writer.write(formatColumn.toString());
		}
		else
		{
			throw new MolgenisDataException("Missing FORMAT information while trying to print first sample");
		}
	}

	// *********************
	// * Utility functions *
	// *********************

	private static boolean isOutputAttribute(AttributeMetaData attribute, List<AttributeMetaData> addedAttributes,
			List<String> attributesToInclude)
	{
		List<AttributeMetaData> expandedAddedAttributes = new ArrayList<>();
		for (AttributeMetaData annotatorAttr : addedAttributes)
		{
			if (annotatorAttr.getDataType().equals(XREF) || annotatorAttr.getDataType().equals(MREF))
				expandedAddedAttributes.addAll(Lists.newArrayList(annotatorAttr.getRefEntity().getAtomicAttributes()));
			else expandedAddedAttributes.add(annotatorAttr);
		}

		List<String> annotatorAttributeNames = expandedAddedAttributes.stream().map(AttributeMetaData::getName)
				.collect(Collectors.toList());
		if (!annotatorAttributeNames.contains(attribute.getName()))
		{
			// always write all fields that were not added by this annotation run.
			return true;
		}
		// else write the field if it was specified or if nothing was sepcified at all.
		else return attributesToInclude.contains(attribute.getName()) || attributesToInclude.isEmpty();
	}
}
