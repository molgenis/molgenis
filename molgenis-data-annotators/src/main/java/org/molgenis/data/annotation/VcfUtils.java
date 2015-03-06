package org.molgenis.data.annotation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.xmlbeans.impl.piccolo.io.FileFormatException;
import org.elasticsearch.common.collect.Iterables;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.data.vcf.VcfRepository;

public class VcfUtils
{

	public static final String TAB = "\t";

	/**
	 * Convert an vcfEntity to a VCF line
	 * 
	 * @param vcfEntity
	 * @return
	 */
	public static String convertToVCF(Entity vcfEntity)
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
			// vcfRecord.append(vcfEntity.getString(vcfAttribute) + "\t");
		}

		List<String> infoFieldsSeen = new ArrayList<String>();
		// flexible 'info' field, one column with potentially many data items
		for (AttributeMetaData attributeMetaData : vcfEntity.getEntityMetaData().getAttribute(VcfRepository.INFO)
				.getAttributeParts())
		{
			infoFieldsSeen.add(attributeMetaData.getName());
			if (vcfEntity.getString(attributeMetaData.getName()) != null) // FIXME: This removes 'FLAG' fields? see
																			// http://samtools.github.io/hts-specs/VCFv4.2.pdf
			{
                if(attributeMetaData.getName().startsWith(VcfRepository.getInfoPrefix())) {
                    vcfRecord.append(attributeMetaData.getName().substring(VcfRepository.getInfoPrefix().length()) + "="
                            + vcfEntity.getString(attributeMetaData.getName()) + ";");
                }else{
                    vcfRecord.append(attributeMetaData.getName() + "="
                            + vcfEntity.getString(attributeMetaData.getName()) + ";");
                }
			}
		}

		for (AttributeMetaData attributeMetaData : vcfEntity.getEntityMetaData().getAtomicAttributes())
		{
			if (!infoFieldsSeen.contains(attributeMetaData.getName())
					&& attributeMetaData.getName().startsWith(VcfRepository.getInfoPrefix())
					&& vcfEntity.getString(attributeMetaData.getName()) != null)
			{
				vcfRecord.append(attributeMetaData.getName().substring(VcfRepository.getInfoPrefix().length()) + "="
						+ vcfEntity.getString(attributeMetaData.getName()) + ";");
			}
		}

		// if we have SAMPLE data, add to output VCF
		Iterable<Entity> sampleEntities = vcfEntity.getEntities(VcfRepository.SAMPLES);
		if (sampleEntities != null && !Iterables.isEmpty(sampleEntities))
		{
			// add tab
			vcfRecord.append(TAB);

			StringBuilder formatColumn = new StringBuilder();
			StringBuilder sampleColumn = new StringBuilder();

			for (Entity sample : sampleEntities)
			{
				boolean firstSample = true;
				for (String sampleAttribute : sample.getAttributeNames())
				{
					// get FORMAT fields, but only for the first time
					if (firstSample)
					{
						if (!sampleAttribute.equals(VcfRepository.ID) || !sampleAttribute.equals(VcfRepository.NAME)) if (sample
								.getString(sampleAttribute) != null)
						{
							formatColumn.append(sampleAttribute);
							formatColumn.append(":");
						}

					}

					// get values for FORMAT for each SAMPLE
					if (!sampleAttribute.equals(VcfRepository.ID) || !sampleAttribute.equals(VcfRepository.NAME))
					// sampleColumn.append(sample.getString(sampleAttribute) != null ? sample.getString(sampleAttribute)
					// : ".");
					if (sample.getString(sampleAttribute) != null)
					{
						sampleColumn.append(sample.getString(sampleAttribute));
						sampleColumn.append(":");
					}

				}

				// add FORMAT data but only first time
				if (firstSample && formatColumn.length() > 0) // FIXME: do we expect this??
				{
					formatColumn.deleteCharAt(formatColumn.length() - 1); // delete trailing ':'
					vcfRecord.append(formatColumn.toString() + TAB);
					firstSample = false;
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

	/**
	 * Checks for previous annotations
	 * 
	 * @param inputVcfFile
	 * @param outputVCFWriter
	 * @param infoFields
	 * @param checkAnnotatedBeforeValue
	 * @return
	 * @throws Exception
	 */
	public static boolean checkPreviouslyAnnotatedAndAddMetadata(File inputVcfFile, PrintWriter outputVCFWriter,
			List<String> infoFields, String checkAnnotatedBeforeValue) throws FileFormatException, FileNotFoundException
	{
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
				if (line.contains("##INFO=<ID=" + checkAnnotatedBeforeValue) && !annotatedBefore)
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
				throw new FileFormatException("Header does not start with #CHROM, are you sure it is a VCF file?");
			}

			// print INFO lines for stuff to be annotated
			if (!annotatedBefore)
			{
				for (String infoField : infoFields)
				{
					outputVCFWriter.println(infoField);
				}
			}

			// print header
			outputVCFWriter.println(line);
		}
		else
		{
			outputVCFWriter.close();
			inputVcfFileScanner.close();
			throw new FileFormatException("Did not find ## on the first line, are you sure it is a VCF file?");
		}

		inputVcfFileScanner.close();
		return annotatedBefore;
	}

}
