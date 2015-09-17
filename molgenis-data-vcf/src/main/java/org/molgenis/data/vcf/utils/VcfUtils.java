package org.molgenis.data.vcf.utils;

import static org.molgenis.data.vcf.VcfRepository.ALT;
import static org.molgenis.data.vcf.VcfRepository.CHROM;
import static org.molgenis.data.vcf.VcfRepository.POS;
import static org.molgenis.data.vcf.VcfRepository.REF;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.collect.Iterables;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.datastructures.Sample;
import org.molgenis.data.vcf.datastructures.Trio;
import org.molgenis.vcf.meta.VcfMetaInfo;

import com.google.common.io.BaseEncoding;

public class VcfUtils
{
	public static final String TAB = "\t";

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

	public static String convertToVCF(Entity vcfEntity) throws MolgenisDataException
	{
		return convertToVCF(vcfEntity, Collections.emptyList());
	}

	/**
	 * Convert an vcfEntity to a VCF line Only output attributes that are in the attributesToInclude list, or all if
	 * attributesToInclude is empty
	 * 
	 * @param vcfEntity
	 * @param attributesToInclude
	 * @return
	 * @throws Exception
	 */
	public static String convertToVCF(Entity vcfEntity, List<String> attributesToInclude) throws MolgenisDataException
	{
		StringBuilder vcfRecord = new StringBuilder();

		List<String> vcfAttributes = Arrays.asList(new String[]
		{ VcfRepository.CHROM, VcfRepository.POS, VcfRepository.ID, VcfRepository.REF, VcfRepository.ALT,
				VcfRepository.QUAL, VcfRepository.FILTER });

		// fixed attributes: chrom pos id ref alt qual filter
		for (String vcfAttribute : vcfAttributes)
		{
			vcfRecord.append(((vcfEntity.getString(vcfAttribute) != null && !vcfEntity.getString(vcfAttribute).equals(
					"")) ? vcfEntity.getString(vcfAttribute) : ".")
					+ TAB);
		}

		List<String> infoFieldsSeen = new ArrayList<String>();
		boolean hasInfoFields = false;
		// flexible 'info' field, one column with potentially many data items
		for (AttributeMetaData attributeMetaData : vcfEntity.getEntityMetaData().getAttribute(VcfRepository.INFO)
				.getAttributeParts())
		{
			if (attributesToInclude.isEmpty() || attributesToInclude.contains(attributeMetaData.getName()))
			{
				infoFieldsSeen.add(attributeMetaData.getName());
				if (vcfEntity.getString(attributeMetaData.getName()) != null)
				{

					if (attributeMetaData.getDataType().getEnumType().equals(FieldTypeEnum.BOOL))
					{
						if (vcfEntity.getBoolean(attributeMetaData.getName()))
						{
							vcfRecord.append(attributeMetaData.getName() + ";");
							hasInfoFields = true;
						}
					}
					else
					{
						vcfRecord.append(attributeMetaData.getName() + "="
								+ vcfEntity.getString(attributeMetaData.getName()) + ";");
						hasInfoFields = true;
					}
				}
			}
		}
		if (!hasInfoFields)
		{
			vcfRecord.append(".");
		}

		// if we have SAMPLE data, add to output VCF
		Iterable<Entity> sampleEntities = vcfEntity.getEntities(VcfRepository.SAMPLES);
		if (sampleEntities != null && !Iterables.isEmpty(sampleEntities))
		{
			// add tab
			vcfRecord.append(TAB);
			boolean firstSample = true;

			for (Entity sample : sampleEntities)
			{
				StringBuilder formatColumn = new StringBuilder();
				StringBuilder sampleColumn = new StringBuilder();

				// write GT first if available
				for (String sampleAttribute : sample.getAttributeNames())
				{
					if (sampleAttribute.equals(VcfRepository.FORMAT_GT))
					{
						if (firstSample)
						{
							formatColumn.append(sampleAttribute);
							formatColumn.append(":");
						}
						if (sample.getString(sampleAttribute) != null)
						{
							sampleColumn.append(sample.getString(sampleAttribute));
							sampleColumn.append(":");
						}
						else
						{
							sampleColumn.append(".:");
						}
					}
				}

				for (String sampleAttribute : sample.getAttributeNames())
				{
					if (!sampleAttribute.equals(VcfRepository.FORMAT_GT))
					{
						// leave out autogenerated ID and NAME columns since this will greatly bloat the output file for
						// many samples
						// FIXME: chance to clash with existing ID and NAME columns in FORMAT ?? what happens then?
						if (!sampleAttribute.equals(VcfRepository.ID) && !sampleAttribute.equals(VcfRepository.NAME))
						{
							if (sample.getString(sampleAttribute) != null)
							{
								sampleColumn.append(sample.getString(sampleAttribute));
								sampleColumn.append(":");
							}
							else
							{
								sampleColumn.append(".:");
							}

							// get FORMAT fields, but only for the first time
							if (firstSample)
							{
								formatColumn.append(sampleAttribute);
								formatColumn.append(":");
							}

						}
					}
				}

				// add FORMAT data but only first time
				if (firstSample && formatColumn.length() > 0) // FIXME: do we expect this??
				{
					formatColumn.deleteCharAt(formatColumn.length() - 1); // delete trailing ':'
					vcfRecord.append(formatColumn.toString() + TAB);
					firstSample = false;
				}
				else if (firstSample)
				{
					throw new MolgenisDataException(
							"Weird situation: we are at sample 1 and want to print FORMAT info but there seems to be none?");
				}

				// now add SAMPLE data
				sampleColumn.deleteCharAt(sampleColumn.length() - 1);// delete trailing ':'
				vcfRecord.append(sampleColumn.toString() + TAB);
			}
			// after all samples, delete trailing '\t'
			vcfRecord.deleteCharAt(vcfRecord.length() - 1); // FIXME: need a check??
		}

		return vcfRecord.toString();
	}

	public static boolean checkPreviouslyAnnotatedAndAddMetadata(File inputVcfFile, PrintWriter outputVCFWriter,
			List<AttributeMetaData> infoFields) throws FileNotFoundException, MolgenisInvalidFormatException
	{
		return checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter, infoFields,
				Collections.emptyList());
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
	 * @throws Exception
	 */
	public static boolean checkPreviouslyAnnotatedAndAddMetadata(File inputVcfFile, PrintWriter outputVCFWriter,
			List<AttributeMetaData> infoFields, List<String> attributesToInclude)
			throws MolgenisInvalidFormatException, FileNotFoundException
	{
		String checkAnnotatedBeforeValue = attributesToInclude.isEmpty() ? (infoFields.isEmpty() ? null : infoFields
				.get(0).getName()) : attributesToInclude.get(0);
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
					System.out
							.println("\nThis file has already been annotated with '"
									+ checkAnnotatedBeforeValue
									+ "' data before it seems. Skipping any further annotation of variants that already contain this field.");
					annotatedBefore = true;
				}

				// read and print to output until we find the header
				outputVCFWriter.println(line);
				line = inputVcfFileScanner.nextLine();
				if (!line.startsWith(VcfRepository.PREFIX))
				{
					break;
				}
				System.out.print(".");
			}
			System.out.println("\nHeader line found:\n" + line);

			// check the header line
			if (!line.startsWith(VcfRepository.CHROM))
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
						outputVCFWriter.println(attributeMetaDataToInfoField(infoAttributeMetaData));
					}
				}
			}

			// print header
			outputVCFWriter.println(line);
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

	private static String attributeMetaDataToInfoField(AttributeMetaData infoAttributeMetaData)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("##INFO=<ID=");
		sb.append(infoAttributeMetaData.getName());
		sb.append(",Number=.");// FIXME: once we support list of primitives we can calculate based on combination of
								// type and nillable
		sb.append(",Type=");
		sb.append(toVcfDataType(infoAttributeMetaData.getDataType().getEnumType()));
		sb.append(",Description=\"");
		// http://samtools.github.io/hts-specs/VCFv4.1.pdf --> "The Description value must be surrounded by
		// double-quotes. Double-quote character can be escaped with backslash \ and backslash as \\."
		if (StringUtils.isBlank(infoAttributeMetaData.getDescription()))
		{
			((DefaultAttributeMetaData) infoAttributeMetaData)
					.setDescription(VcfRepository.DEFAULT_ATTRIBUTE_DESCRIPTION);
		}
		sb.append(infoAttributeMetaData.getDescription().replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " "));
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
			case IMAGE:
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
				// expecting: ##PEDIGREE=<Child=100400,Mother=100402,Father=100401>
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
							throw new MolgenisDataException("Expected Child, Mother or Father, but found: " + element
									+ " in line " + line);
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
