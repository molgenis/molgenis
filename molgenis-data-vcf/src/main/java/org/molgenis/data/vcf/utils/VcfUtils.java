package org.molgenis.data.vcf.utils;

import autovalue.shaded.com.google.common.common.collect.Lists;
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
import java.util.List;
import java.util.Scanner;

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

public class VcfUtils
{

	private static List<String> VCF_ATTRIBUTE_NAMES = Arrays.asList(new String[]
	{ CHROM, POS, ID, REF, ALT, QUAL, FILTER });

	public static final String ANNOTATION_FIELD_SEPARATOR = ";";
	private static final String PIPE_SEPARATOR = "|";

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
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	public static void writeToVcf(Entity vcfEntity, List<String> attributesToInclude, BufferedWriter writer)
			throws MolgenisDataException, IOException
	{
		// Add fixed attributes: chrom pos id ref alt qual filter to output VCF
		String additionalInfoFields = addStandardFieldsToVcf(vcfEntity, writer);

		// Parse info field and add to output VCF
		addInfoFieldToVcf(vcfEntity, writer, attributesToInclude, additionalInfoFields);

		// if we have SAMPLE data, add to output VCF
		Iterable<Entity> sampleEntities = vcfEntity.getEntities(SAMPLES);
		if (sampleEntities != null)
		{
			addSampleEntitiesToVcf(sampleEntities, writer);
		}
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
					resultEMD.addAttribute("EFFECT").setDataType(MREF).setRefEntity(effectsEMD);
				}
				return resultEMD;
			}

			@Override
			public boolean hasNext()
			{
				return effects.hasNext() ? true : false;
			}

			@Override
			public Entity next()
			{
				Entity variant = null;
				String peekedId;
				List<Entity> effectsForVariant = Lists.newArrayList();
				while (effects.hasNext())
				{
					peekedId = effects.peek().getEntity("VARIANT").getIdValue().toString();
					if (variant == null || variant.getIdValue().toString() == peekedId)
					{
						Entity effect = effects.next();
						variant = effect.getEntity("VARIANT");
						effectsForVariant.add(effect);
					}
					else
					{
						Entity newVariant = new MapEntity(getResultEMD(effectsForVariant.get(0).getEntityMetaData(),
								variant.getEntityMetaData()));
						newVariant.set(variant);
						newVariant.set("EFFECT", effectsForVariant);
						effectsForVariant = Lists.newArrayList();
						return newVariant;
					}
				}
				Entity newVariant = new MapEntity(
						getResultEMD(effectsForVariant.get(0).getEntityMetaData(), variant.getEntityMetaData()));
				newVariant.set(variant);
				newVariant.set("EFFECT", effectsForVariant);
				return newVariant;
			}
		};
	}

	public static boolean checkPreviouslyAnnotatedAndAddMetadata(File inputVcfFile, BufferedWriter outputVCFWriter,
			List<AttributeMetaData> infoFields) throws MolgenisInvalidFormatException, IOException
	{
		return checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter, infoFields,
				Collections.emptyList());
	}

	/**
	 * Add standard columns to VCF. Return a String containing fields and values from non-standard columns.
	 * 
	 * @param vcfEntity
	 * @param writer
	 * @return
	 * @throws IOException
	 */
	private static String addStandardFieldsToVcf(Entity vcfEntity, BufferedWriter writer) throws IOException
	{
		Iterable<AttributeMetaData> attributes = vcfEntity.getEntityMetaData().getAttributes();
		String additionalInfoFields = "";

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
		for (AttributeMetaData attribute : attributes)
		{
			if (!VCF_ATTRIBUTE_NAMES.contains(attribute.getName()))
			{
				if ((attribute.getDataType().equals(MREF) || attribute.getDataType().equals(XREF))
						&& !attribute.getName().equals(SAMPLES))
				{
					// We are dealing with non standard Xref and Mref attributes
					// added by e.g. the SnpEff annotator,
					// which is NOT the SAMPLE_ENTITIES attribute
					additionalInfoFields = parseNonStandardMrefFieldsToInfoField(
							vcfEntity.getEntities(attribute.getName()), attribute, additionalInfoFields);

				}
			}

		}
		return additionalInfoFields;
	}

	/**
	 * Take non standard fields added by annotators like SnpEff, and parse there values into VCF info field format
	 * 
	 * @param refEntities
	 * @param attribute
	 * @param additionalInfoFields
	 */
	private static String parseNonStandardMrefFieldsToInfoField(Iterable<Entity> refEntities,
			AttributeMetaData attribute, String additionalInfoFields)
	{
		boolean secondValuePresent = false;
		for (Entity entity : refEntities)
		{
			Iterable<AttributeMetaData> refAttributes = entity.getEntityMetaData().getAttributes();
			if (!secondValuePresent)
			{
				additionalInfoFields = additionalInfoFields + attribute.getName() + "=";
				additionalInfoFields = addEntityValuesToAdditionalInfoField(additionalInfoFields, entity,
						refAttributes);
			}
			else
			{
				additionalInfoFields = additionalInfoFields + ",";
				additionalInfoFields = addEntityValuesToAdditionalInfoField(additionalInfoFields, entity,
						refAttributes);
			}
			secondValuePresent = true;
		}
		additionalInfoFields = additionalInfoFields + ANNOTATION_FIELD_SEPARATOR;
		return additionalInfoFields;
	}

	private static String addEntityValuesToAdditionalInfoField(String additionalInfoFields, Entity entity,
			Iterable<AttributeMetaData> refAttributes)
	{
		boolean secondValuePresent = false;
		AttributeMetaData idAttribute = entity.getEntityMetaData().getIdAttribute();
		for (AttributeMetaData refAttribute : refAttributes)
		{
			if (!refAttribute.isSameAs(idAttribute))
			{
				if (secondValuePresent) additionalInfoFields = additionalInfoFields + PIPE_SEPARATOR;
				additionalInfoFields = additionalInfoFields + entity.get(refAttribute.getName());
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
	public static boolean checkPreviouslyAnnotatedAndAddMetadata(File inputVcfFile, BufferedWriter outputVCFWriter,
			List<AttributeMetaData> infoFields, List<String> attributesToInclude)
					throws MolgenisInvalidFormatException, IOException
	{
		String checkAnnotatedBeforeValue = attributesToInclude.isEmpty()
				? (infoFields.isEmpty() ? null : infoFields.get(0).getName()) : attributesToInclude.get(0);
		boolean annotatedBefore = false;

		System.out.println("Detecting VCF column header...");

		Scanner inputVcfFileScanner = new Scanner(inputVcfFile, "UTF-8");
		String line = inputVcfFileScanner.nextLine();

		// if first line does not start with ##, we don't trust this file as VCF
		if (line.startsWith(VcfRepository.PREFIX))
		{
			while (inputVcfFileScanner.hasNextLine())
			{
				// detect existing annotations of the same info field
				if ((checkAnnotatedBeforeValue != null) && line.contains("##INFO=<ID=" + checkAnnotatedBeforeValue)
						&& !annotatedBefore)
				{
					System.out.println("\nThis file has already been annotated with '" + checkAnnotatedBeforeValue
							+ "' data before it seems. Skipping any further annotation of variants that already contain this field.");
					annotatedBefore = true;
				}

				// read and print to output until we find the header
				outputVCFWriter.write(line);
				outputVCFWriter.newLine();
				line = inputVcfFileScanner.nextLine();
				if (!line.startsWith(VcfRepository.PREFIX))
				{
					break;
				}
				System.out.print(".");
			}
			System.out.println("\nHeader line found:\n" + line);

			// check the header line
			if (!line.startsWith(CHROM))
			{
				outputVCFWriter.close();
				inputVcfFileScanner.close();
				throw new MolgenisInvalidFormatException(
						"Header does not start with #CHROM, are you sure it is a VCF file?");
			}

			// print INFO lines for stuff to be annotated
			if (!annotatedBefore)
			{
				for (AttributeMetaData infoAttributeMetaData : getAtomicAttributesFromList(infoFields))
				{
					if (attributesToInclude.isEmpty() || attributesToInclude.contains(infoAttributeMetaData.getName()))
					{
						outputVCFWriter.write(attributeMetaDataToInfoField(infoAttributeMetaData));
						outputVCFWriter.newLine();
					}
				}
			}

			// print header
			outputVCFWriter.write(line);
			outputVCFWriter.newLine();
		}
		else
		{
			outputVCFWriter.close();
			inputVcfFileScanner.close();
			throw new MolgenisInvalidFormatException(
					"Did not find ## on the first line, are you sure it is a VCF file?");
		}

		inputVcfFileScanner.close();
		return annotatedBefore;
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

	/**
	 * Parses the info field from a MOLGENIS entity and writes it into VCF INFO column format
	 * 
	 * @param vcfEntity
	 * @param writer
	 * @param attributesToInclude
	 * @throws IOException
	 */
	private static void addInfoFieldToVcf(Entity vcfEntity, BufferedWriter writer, List<String> attributesToInclude,
			String additionInfoFieldValues) throws IOException
	{
		boolean hasInfoFields = false;

		// flexible 'info' field, one column with potentially many data items
		for (AttributeMetaData attributeMetaData : vcfEntity.getEntityMetaData().getAttribute(INFO).getAttributeParts())
		{
			String infoAttrName = attributeMetaData.getName();
			if (attributesToInclude.isEmpty() || attributesToInclude.contains(infoAttrName))
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

		// Previous method parsed values from XREF or MREF columns that can be
		// added by certain annotators. E.g. SnpEff
		// Add these values to the INFO column
		if (!isNullOrEmpty(additionInfoFieldValues))
		{
			writer.append(additionInfoFieldValues);
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
					// will greatly bloat the output file for
					// many samples
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
			if (firstSample && formatColumn.length() > 0) // FIXME: do we expect
															// this??
			{
				formatColumn.deleteCharAt(formatColumn.length() - 1); // delete
																		// trailing
																		// ':'
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
			sampleColumn.deleteCharAt(sampleColumn.length() - 1);// delete
																	// trailing
																	// ':'
			writer.write(sampleColumn.toString());

			if (it.hasNext())
			{
				writer.write('\t');
			}
		}
	}

	private static String attributeMetaDataToInfoField(AttributeMetaData infoAttributeMetaData)
	{
		String attributeName = infoAttributeMetaData.getName();
		StringBuilder sb = new StringBuilder();
		sb.append("##INFO=<ID=");
		sb.append(attributeName);
		sb.append(",Number=.");// FIXME: once we support list of primitives we
								// can calculate based on combination of
								// type and nillable
		sb.append(",Type=");
		sb.append(toVcfDataType(infoAttributeMetaData.getDataType().getEnumType()));
		sb.append(",Description=\"");
		// http://samtools.github.io/hts-specs/VCFv4.1.pdf --> "The Description
		// value must be surrounded by
		// double-quotes. Double-quote character can be escaped with backslash \
		// and backslash as \\."
		if (StringUtils.isBlank(infoAttributeMetaData.getDescription()))
		{
			if ((infoAttributeMetaData.getDataType().equals(MREF) || infoAttributeMetaData.getDataType().equals(XREF))
					&& !attributeName.equals(SAMPLES))
			{
				String description = attributeName + " annotations: '";
				description = description
						+ on(" | ").join(transform(infoAttributeMetaData.getRefEntity().getAtomicAttributes(),
								AttributeMetaData::getName));
				description = description + "'";
				((DefaultAttributeMetaData) infoAttributeMetaData).setDescription(description);
			}
			else
			{
				((DefaultAttributeMetaData) infoAttributeMetaData)
						.setDescription(VcfRepository.DEFAULT_ATTRIBUTE_DESCRIPTION);
			}
		}
		sb.append(
				infoAttributeMetaData.getDescription().replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " "));
		sb.append("\">");
		return sb.toString();
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
