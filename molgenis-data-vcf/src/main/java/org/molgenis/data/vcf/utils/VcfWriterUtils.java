package org.molgenis.data.vcf.utils;

import autovalue.shaded.com.google.common.common.collect.Lists;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.vcf.VcfRepository;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
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
import static org.molgenis.data.vcf.VcfRepository.NAME;
import static org.molgenis.data.vcf.VcfRepository.POS;
import static org.molgenis.data.vcf.VcfRepository.QUAL;
import static org.molgenis.data.vcf.VcfRepository.REF;
import static org.molgenis.data.vcf.VcfRepository.SAMPLES;

public class VcfWriterUtils
{
	public static final String VARIANT = "VARIANT";
	public static final String EFFECT = "EFFECT";
	private static final LinkedList<String> VCF_ATTRIBUTE_NAMES = new LinkedList<>(Arrays.asList(new String[]
	{ CHROM, POS, ID, REF, ALT, QUAL, FILTER }));

	public static final String ANNOTATION_FIELD_SEPARATOR = ";";
	private static final String PIPE_SEPARATOR = "|";
	public static final String SPACE_PIPE_SEPERATOR = " | ";

	public static void writeToVcf(Entity vcfEntity, List<AttributeMetaData> annotatorAttributes, BufferedWriter writer)
			throws MolgenisDataException, IOException
	{
		writeToVcf(vcfEntity, annotatorAttributes, Collections.emptyList(), writer);
	}

	/**
	 * Convert an vcfEntity to a VCF line Only output attributes that are in the attributesToInclude list, or all if
	 * attributesToInclude is empty
	 * 
	 * @param vcfEntity
	 * @param attributesToInclude
	 * @throws IOException,Exception
	 */
	public static void writeToVcf(Entity vcfEntity, List<AttributeMetaData> annotatorAttributes,
			List<String> attributesToInclude, BufferedWriter writer) throws MolgenisDataException, IOException
	{
		addStandardFieldsToVcf(vcfEntity, writer);
		writeInfoData(vcfEntity, writer, annotatorAttributes, attributesToInclude);

		// if we have SAMPLE data, add to output VCF
		Iterable<Entity> sampleEntities = vcfEntity.getEntities(SAMPLES);
		if (sampleEntities != null)
		{
			addSampleEntitiesToVcf(sampleEntities, writer);
		}
	}

	private static String parseRefAttributesToDataString(Entity vcfEntity, List<AttributeMetaData> annotatorAttributes,
			List<String> attributesToInclude)
	{
		Iterable<AttributeMetaData> attributes = vcfEntity.getEntityMetaData().getAttributes();
		String additionalInfoFields = "";
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
					// We are dealing with non standard Xref and Mref attributes
					// added by e.g. the SnpEff annotator,
					// which is NOT the SAMPLE_ENTITIES attribute
					additionalInfoFields = parseRefFieldsToInfoField(vcfEntity.getEntities(attributeName), attribute,
							additionalInfoFields, annotatorAttributes, attributesToInclude);
				}
			}

		}
		return additionalInfoFields;
	}

	public static void writeVcfHeader(File inputVcfFile, BufferedWriter outputVCFWriter,
			List<AttributeMetaData> annotatorAttributes) throws MolgenisInvalidFormatException, IOException
	{
		writeVcfHeader(inputVcfFile, outputVCFWriter, annotatorAttributes, Collections.emptyList());
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
	private static String parseRefFieldsToInfoField(Iterable<Entity> refEntities, AttributeMetaData attribute,
			String additionalInfoFields, List<AttributeMetaData> annotatorAttributes, List<String> attributesToInclude)
	{
		boolean secondValuePresent = false;
		for (Entity refEntity : refEntities)
		{
			Iterable<AttributeMetaData> refAttributes = refEntity.getEntityMetaData().getAttributes();
			if (!secondValuePresent)
			{
				additionalInfoFields = additionalInfoFields + attribute.getName() + "=";
				additionalInfoFields = addEntityValuesToAdditionalInfoField(additionalInfoFields, refEntity,
						refAttributes, annotatorAttributes, attributesToInclude);
			}
			else
			{
				additionalInfoFields = additionalInfoFields + ",";
				additionalInfoFields = addEntityValuesToAdditionalInfoField(additionalInfoFields, refEntity,
						refAttributes, annotatorAttributes, attributesToInclude);
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
				if (previousValuePresent) additionalInfoFields = additionalInfoFields + PIPE_SEPARATOR;
				String value = refEntity.getString(refAttribute.getName()) == null ? ""
						: refEntity.getString(refAttribute.getName());
				additionalInfoFields = additionalInfoFields + value;
				previousValuePresent = true;
			}
		}
		return additionalInfoFields;
	}

	/**
	 * Checks for previous annotations
	 * 
	 * @param inputVcfFile
	 * @param outputVCFWriter
	 * @param annotatorAttributes
	 * @param attributesToInclude
	 *            , the AttributeMetaData to write to the VCF file, if empty writes all attributes
	 * @return
	 * @throws MolgenisInvalidFormatException
	 * @throws IOException
	 */
	public static void writeVcfHeader(File inputVcfFile, BufferedWriter outputVCFWriter,
			List<AttributeMetaData> annotatorAttributes, List<String> attributesToInclude)
					throws MolgenisInvalidFormatException, IOException
	{
		System.out.println("Detecting VCF column header...");

		Scanner inputVcfFileScanner = new Scanner(inputVcfFile, "UTF-8");
		String line = inputVcfFileScanner.nextLine();

		Map<String, String> infoHeaderLinesMap = new LinkedHashMap<>();
		if (line.startsWith(VcfRepository.PREFIX))
		{
			line = processHeaderLines(outputVCFWriter, inputVcfFileScanner, line, infoHeaderLinesMap);
			System.out.println("\nHeader line found:\n" + line);

			checkHeaderLine(outputVCFWriter, inputVcfFileScanner, line);
			writeInfoHeaderLines(outputVCFWriter, annotatorAttributes, attributesToInclude, infoHeaderLinesMap);
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

	private static void writeColumnHeaders(BufferedWriter outputVCFWriter, String line) throws IOException
	{
		outputVCFWriter.write(line);
		outputVCFWriter.newLine();
	}

	private static void writeInfoHeaderLines(BufferedWriter outputVCFWriter,
			List<AttributeMetaData> annotatorAttributes, List<String> attributesToInclude,
			Map<String, String> infoHeaderLinesMap) throws IOException
	{
		Map<String, AttributeMetaData> annotatorAttributesMap = VcfUtils.getAttributesMapFromList(annotatorAttributes);
		writeNonCurrentAnnotatorInfoFields(outputVCFWriter, infoHeaderLinesMap, annotatorAttributesMap);
		writeCurrentAnnotatorInfoFields(outputVCFWriter, attributesToInclude, infoHeaderLinesMap,
				annotatorAttributesMap);
	}

	private static void writeCurrentAnnotatorInfoFields(BufferedWriter outputVCFWriter,
			List<String> attributesToInclude, Map<String, String> infoHeaderLinesMap,
			Map<String, AttributeMetaData> annotatorAttributes) throws IOException
	{
		for (AttributeMetaData annotatorInfoAttr : annotatorAttributes.values())
		{
			if (attributesToInclude.isEmpty() || attributesToInclude.contains(annotatorInfoAttr.getName())
					|| annotatorInfoAttr.getDataType().equals(XREF) || annotatorInfoAttr.getDataType().equals(MREF))
			{
				outputVCFWriter
						.write(createInfoStringFromAttribute(annotatorAttributes.get(annotatorInfoAttr.getName()),
								infoHeaderLinesMap.get(annotatorInfoAttr.getName()), attributesToInclude));
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

	/**
	 * Parses the info field from a MOLGENIS entity and writes it into VCF INFO column format
	 * 
	 * @param vcfEntity
	 * @param writer
	 * @param annotatorAttributes
	 * @param attributesToInclude
	 * @throws IOException
	 */
	private static void writeInfoData(Entity vcfEntity, BufferedWriter writer,
			List<AttributeMetaData> annotatorAttributes, List<String> attributesToInclude) throws IOException
	{
		boolean hasInfoFields = false;

		String refEntityAttributesInfoFields = parseRefAttributesToDataString(vcfEntity, annotatorAttributes,
				attributesToInclude);
		for (AttributeMetaData attributeMetaData : vcfEntity.getEntityMetaData().getAttribute(INFO).getAttributeParts())
		{
			String infoAttrName = attributeMetaData.getName();
			if (isOutputAttribute(attributeMetaData, annotatorAttributes, attributesToInclude))
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
			String currentInfoField, List<String> attributesToInclude)
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
			sb.append(VcfUtils.toVcfDataType(infoAttributeMetaData.getDataType().getEnumType()));
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
				String description;
				if (currentInfoField == null)
				{
					description = attributeName + " annotations: '";
				}
				else
				{
					description = currentInfoField.replace("'\">", SPACE_PIPE_SEPERATOR);
				}
				description = description + (refAttributesToString(atomicAttributes, attributesToInclude)
						.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " "));

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

	private static String refAttributesToString(Iterable<AttributeMetaData> atomicAttributes,
			List<String> attributesToInclude)
	{
		Iterable<AttributeMetaData> attributes = Iterables.filter(atomicAttributes, new Predicate<AttributeMetaData>()
		{
			@Override
			public boolean apply(AttributeMetaData attributeMetaData)
			{
				if (attributeMetaData.isVisible() && isOutputAttribute(attributeMetaData,
						Lists.newArrayList(atomicAttributes), attributesToInclude))
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		});
		return on(SPACE_PIPE_SEPERATOR).join(transform(attributes, AttributeMetaData::getName));
	}

	private static boolean isOutputAttribute(AttributeMetaData attribute, List<AttributeMetaData> annotatorAttributes,
			List<String> attributesToInclude)
	{
		List<AttributeMetaData> expandedAnnotatorAttributes = new ArrayList<>();
		for (AttributeMetaData annotatorAttr : annotatorAttributes)
		{
			if (annotatorAttr.getDataType().equals(XREF) || annotatorAttr.getDataType().equals(MREF))
				expandedAnnotatorAttributes
						.addAll(Lists.newArrayList(annotatorAttr.getRefEntity().getAtomicAttributes()));
			else expandedAnnotatorAttributes.add(annotatorAttr);
		}

		List<String> annotatorAttributeNames = expandedAnnotatorAttributes.stream().map(AttributeMetaData::getName)
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
