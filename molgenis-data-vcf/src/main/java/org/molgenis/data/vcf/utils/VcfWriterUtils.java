package org.molgenis.data.vcf.utils;

import com.google.common.collect.Lists;
import net.sf.samtools.util.BlockCompressedInputStream;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.model.VcfAttributes;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.google.common.base.Joiner.on;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterables.transform;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.support.EntityTypeUtils.isReferenceType;
import static org.molgenis.data.vcf.VcfRepository.DEFAULT_ATTRIBUTE_DESCRIPTION;
import static org.molgenis.data.vcf.model.VcfAttributes.*;

public class VcfWriterUtils
{
	public static final String VARIANT = "VARIANT";
	public static final String EFFECT = "EFFECT";
	private static final char ANNOTATION_FIELD_SEPARATOR = ';';
	private static final String SPACE_PIPE_SEPERATOR = " | ";

	private static final LinkedList<String> VCF_ATTRIBUTE_NAMES = new LinkedList<>(
			Arrays.asList(CHROM, POS, ID, REF, ALT, QUAL, FILTER));
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
			List<Attribute> addedAttributes) throws MolgenisInvalidFormatException, IOException
	{
		writeVcfHeader(inputVcfFile, outputVCFWriter, addedAttributes, Collections.emptyList());
	}

	/**
	 * Checks for previous annotations
	 *
	 * @param inputVcfFile
	 * @param outputVCFWriter
	 * @param addedAttributes
	 * @param attributesToInclude , the Attribute to write to the VCF file, if empty writes all attributes
	 * @return
	 * @throws MolgenisInvalidFormatException
	 * @throws IOException
	 */
	public static void writeVcfHeader(File inputVcfFile, BufferedWriter outputVCFWriter,
			List<Attribute> addedAttributes, List<String> attributesToInclude)
			throws MolgenisInvalidFormatException, IOException
	{
		System.out.println("Detecting VCF column header...");

		Scanner inputVcfFileScanner = createVcfFileScanner(inputVcfFile);
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

	private static Scanner createVcfFileScanner(File vcfFile) throws IOException
	{
		InputStream inputStream = new FileInputStream(vcfFile);
		if (vcfFile.getName().endsWith(".gz"))
		{
			inputStream = new BlockCompressedInputStream(inputStream);
		}
		return new Scanner(inputStream, UTF_8.name());
	}

	/**
	 * Overload of writeToVcf to support a simpler call with only Entity and Writer.
	 *
	 * @param vcfEntity
	 * @param writer
	 * @throws MolgenisDataException
	 * @throws IOException
	 */
	public static void writeToVcf(Entity vcfEntity, BufferedWriter writer) throws MolgenisDataException, IOException
	{
		writeToVcf(vcfEntity, new ArrayList<>(), new ArrayList<>(), writer);
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
	public static void writeToVcf(Entity vcfEntity, List<Attribute> addedAttributes, List<String> attributesToInclude,
			BufferedWriter writer) throws MolgenisDataException, IOException
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
			if (line.startsWith(VcfRepository.PREFIX + VcfAttributes.INFO))
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

	private static void writeInfoHeaders(BufferedWriter outputVCFWriter, List<Attribute> annotatorAttributes,
			List<String> attributesToInclude, Map<String, String> infoHeaderLinesMap) throws IOException
	{
		Map<String, Attribute> annotatorAttributesMap = VcfUtils.getAttributesMapFromList(annotatorAttributes);
		writeExistingInfoHeaders(outputVCFWriter, infoHeaderLinesMap, annotatorAttributesMap);
		writeAddedInfoHeaders(outputVCFWriter, attributesToInclude, annotatorAttributesMap, infoHeaderLinesMap);
	}

	private static void writeAddedInfoHeaders(BufferedWriter outputVCFWriter, List<String> attributesToInclude,
			Map<String, Attribute> annotatorAttributes, Map<String, String> infoHeaderLinesMap) throws IOException
	{
		for (Attribute annotatorInfoAttr : annotatorAttributes.values())
		{
			if (attributesToInclude.isEmpty() || attributesToInclude.contains(annotatorInfoAttr.getName())
					|| isReferenceType(annotatorInfoAttr))
			{
				outputVCFWriter.write(
						createInfoStringFromAttribute(annotatorAttributes.get(annotatorInfoAttr.getName()),
								attributesToInclude, infoHeaderLinesMap.get(annotatorInfoAttr.getName())));
				outputVCFWriter.newLine();
			}
		}
	}

	private static String createInfoStringFromAttribute(Attribute infoAttribute, List<String> attributesToInclude,
			String currentInfoField)
	{
		String attributeName = infoAttribute.getName();
		StringBuilder sb = new StringBuilder();

		sb.append("##INFO=<ID=");
		sb.append(attributeName);
		// FIXME: once we support list of primitives we can calculate based on combination of type and nillable
		sb.append(",Number=.");
		sb.append(",Type=");
		sb.append(VcfUtils.toVcfDataType(infoAttribute.getDataType()));
		sb.append(",Description=\"");

		// http://samtools.github.io/hts-specs/VCFv4.1.pdf --> "The Description
		// value must be surrounded by double-quotes. Double-quote character can be escaped with backslash \ and
		// backslash as \\."
		if (StringUtils.isBlank(infoAttribute.getDescription()))
		{
			if (isReferenceType(infoAttribute) && !attributeName.equals(SAMPLES))
			{
				String currentAttributesString =
						currentInfoField != null ? currentInfoField.substring((currentInfoField.indexOf("'") + 1),
								currentInfoField.lastIndexOf("'")) : "";
				writeRefAttributePartsToInfoDescription(infoAttribute, attributesToInclude, attributeName, sb,
						currentAttributesString);
			}
			else
			{
				sb.append(DEFAULT_ATTRIBUTE_DESCRIPTION);
			}
		}
		else
		{
			sb.append(infoAttribute.getDescription().replace("\"", "\\\"").replace("\n", " "));
		}
		sb.append("\">");
		return sb.toString();
	}

	private static void writeRefAttributePartsToInfoDescription(Attribute infoAttribute,
			List<String> attributesToInclude, String attributeName, StringBuilder sb, String existingAttributes)
	{
		Iterable<Attribute> atomicAttributes = infoAttribute.getRefEntity().getAtomicAttributes();
		sb.append(attributeName);
		sb.append(" annotations: '");
		if (!existingAttributes.isEmpty())
		{
			sb.append(existingAttributes);
			sb.append(" | ");
		}
		sb.append(refAttributesToString(atomicAttributes, attributesToInclude).replace("\\", "\\\\")
																			  .replace("\"", "\\\"")
																			  .replace("\n", " "));
		sb.append("'");
	}

	private static String refAttributesToString(Iterable<Attribute> atomicAttributes, List<String> attributesToInclude)
	{
		Iterable<Attribute> attributes = StreamSupport.stream(atomicAttributes.spliterator(), false)
													  .filter(attribute -> (attribute.isVisible() && isOutputAttribute(
															  attribute, Lists.newArrayList(atomicAttributes),
															  attributesToInclude)))
													  .collect(Collectors.toList());
		return on(SPACE_PIPE_SEPERATOR).join(transform(attributes, Attribute::getName));
	}

	private static void writeExistingInfoHeaders(BufferedWriter outputVCFWriter, Map<String, String> infoHeaderLinesMap,
			Map<String, Attribute> annotatorAttributes) throws IOException
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
			Object value = vcfEntity.get(attribute);
			String stringValue = ".";
			if (value != null) stringValue = value.toString();
			if (stringValue.isEmpty()) stringValue = ".";
			writer.write(stringValue);
			writer.write('\t');
		}
	}

	private static void writeInfoData(Entity vcfEntity, BufferedWriter writer, List<Attribute> annotatorAttributes,
			List<String> attributesToInclude) throws IOException
	{
		boolean hasInfoFields = false;

		List<Attribute> attributes = StreamSupport.stream(vcfEntity.getEntityType().getAllAttributes().spliterator(),
				false)
												  .filter(attr -> !(VCF_ATTRIBUTE_NAMES.contains(attr.getName()) || attr
														  .getName()
														  .equals(INFO)))
												  .filter(attr -> isOutputAttribute(attr, annotatorAttributes,
														  attributesToInclude))
												  .collect(Collectors.toList());

		List<String> infoFieldStrs = new ArrayList<>();
		for (Attribute attribute : attributes)
		{
			String infoFieldStr = getInfoFieldString(vcfEntity, attribute);
			if (infoFieldStr != null)
			{
				infoFieldStrs.add(infoFieldStr);
			}
		}
		hasInfoFields = !infoFieldStrs.isEmpty();
		writer.append(infoFieldStrs.stream().collect(joining(String.valueOf(ANNOTATION_FIELD_SEPARATOR))));

		String refEntityAttributesInfoFields = parseRefAttributesToDataString(vcfEntity, annotatorAttributes,
				attributesToInclude);
		if (!isNullOrEmpty(refEntityAttributesInfoFields))
		{
			if (hasInfoFields)
			{
				writer.append(ANNOTATION_FIELD_SEPARATOR);
			}
			writer.append(refEntityAttributesInfoFields);
			hasInfoFields = true;
		}
		if (!hasInfoFields)
		{
			writer.append('.');
		}
	}

	private static String parseRefAttributesToDataString(Entity vcfEntity, List<Attribute> annotatorAttributes,
			List<String> attributesToInclude)
	{
		Iterable<Attribute> attributes = vcfEntity.getEntityType().getAllAttributes();
		StringBuilder refEntityInfoFields = new StringBuilder();
		for (Attribute attribute : attributes)
		{
			String attributeName = attribute.getName();
			if (isReferenceType(attribute) && !attributeName.equals(SAMPLES))
			{
				// If the MREF field is empty, no effects were found, so we do not add an EFFECT field to this entity
				if (vcfEntity.get(attributeName) != null && isOutputAttribute(attribute, annotatorAttributes,
						attributesToInclude))
				{

					parseRefFieldsToInfoField(vcfEntity.getEntities(attributeName), attribute, refEntityInfoFields,
							annotatorAttributes, attributesToInclude);
				}
			}

		}
		if (refEntityInfoFields.length() > 0
				&& refEntityInfoFields.charAt(refEntityInfoFields.length() - 1) == ANNOTATION_FIELD_SEPARATOR)
		{
			refEntityInfoFields.setLength(refEntityInfoFields.length() - 1);
		}
		return refEntityInfoFields.toString();
	}

	private static String getInfoFieldString(Entity vcfEntity, Attribute attribute) throws IOException
	{
		String infoFieldValue = null;

		String infoAttrName = attribute.getName();
		if (attribute.getDataType() == BOOL)
		{
			Boolean infoAttrBoolValue = vcfEntity.getBoolean(infoAttrName);
			if (infoAttrBoolValue != null && infoAttrBoolValue)
			{
				infoFieldValue = infoAttrName;
			}
		}
		else if (!isReferenceType(attribute))
		{

			Object infoAttrStringValue = vcfEntity.get(infoAttrName);
			if (infoAttrStringValue != null)
			{
				infoFieldValue = infoAttrName + '=' + infoAttrStringValue.toString();
			}
		}

		return infoFieldValue;
	}

	/**
	 * Create a INFO field annotation and add values
	 */
	private static void parseRefFieldsToInfoField(Iterable<Entity> refEntities, Attribute attribute,
			StringBuilder refEntityInfoFields, List<Attribute> annotatorAttributes, List<String> attributesToInclude)
	{
		boolean secondValuePresent = false;
		for (Entity refEntity : refEntities)
		{
			Iterable<Attribute> refAttributes = refEntity.getEntityType().getAttributes();
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
			Iterable<Attribute> refAttributes, List<Attribute> annotatorAttributes, List<String> attributesToInclude)
	{
		boolean previousValuePresent = false;
		for (Attribute refAttribute : refAttributes)
		{
			if (refAttribute.isVisible() && !isReferenceType(refAttribute) && isOutputAttribute(refAttribute,
					annotatorAttributes, attributesToInclude))
			{
				if (previousValuePresent) refEntityInfoFields.append(PIPE_SEPARATOR);
				String value = refEntity.getString(refAttribute.getName()) == null ? "" : refEntity.getString(
						refAttribute.getName());
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
		if (sample.getEntityType().getAttribute(FORMAT_GT) != null)
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
		EntityType entityType = sample.getEntityType();
		for (Attribute sampleAttribute : entityType.getAttributes())
		{
			String sampleAttributeName = sampleAttribute.getName();
			if (!sampleAttributeName.equals(FORMAT_GT) && !sampleAttributeName.equals(VcfRepository.ORIGINAL_NAME))
			{
				// skip the field that were generated for the use of the entity within molgenis
				if (!sampleAttribute.equals(entityType.getIdAttribute()) && !sampleAttribute.equals(
						entityType.getLabelAttribute()))
				{
					if (sampleColumn.length() != 0) sampleColumn.append(":");
					Object sampleAttrValue = sample.get(sampleAttributeName);
					if (sampleAttrValue != null)
					{
						sampleColumn.append(sampleAttrValue.toString());
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
		if (sample.getEntityType().getAttribute(FORMAT_GT) != null)
		{
			formatColumn.append(FORMAT_GT);
		}
		EntityType entityType = sample.getEntityType();
		for (Attribute sampleAttribute : entityType.getAttributes())
		{
			String sampleAttributeName = sampleAttribute.getName();
			if (!sampleAttributeName.equals(FORMAT_GT) && !sampleAttributeName.equals(VcfRepository.ORIGINAL_NAME))
			{
				// skip the field that were generated for the use of the entity within molgenis
				if (!sampleAttribute.equals(entityType.getIdAttribute()) && !sampleAttribute.equals(
						entityType.getLabelAttribute()))
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

	private static boolean isOutputAttribute(Attribute attribute, List<Attribute> addedAttributes,
			List<String> attributesToInclude)
	{
		List<Attribute> expandedAddedAttributes = new ArrayList<>();
		for (Attribute annotatorAttr : addedAttributes)
		{
			if (isReferenceType(annotatorAttr))
				expandedAddedAttributes.addAll(Lists.newArrayList(annotatorAttr.getRefEntity().getAtomicAttributes()));
			else expandedAddedAttributes.add(annotatorAttr);
		}

		List<String> annotatorAttributeNames = expandedAddedAttributes.stream()
																	  .map(Attribute::getName)
																	  .collect(Collectors.toList());
		// always write all fields that were not added by this annotation run.
		// else write the field if it was specified or if nothing was sepcified at all.
		return (!annotatorAttributeNames.contains(attribute.getName()) || attributesToInclude.contains(
				attribute.getName()) || attributesToInclude.isEmpty()) && attribute.isVisible() && !attribute.getName()
																											 .equals(VcfAttributes.SAMPLES);
	}
}
