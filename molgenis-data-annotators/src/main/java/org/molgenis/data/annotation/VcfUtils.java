package org.molgenis.data.annotation;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.elasticsearch.common.collect.Iterables;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.vcf.VcfRepository;
import org.springframework.util.StringUtils;

public class VcfUtils
{

	/**
	 * Convert an entity to a VCF line
	 * 
	 * @param entity
	 * @return
	 */
	public static String convertToVCF(Entity entity)
	{
		StringBuilder vcfRecord = new StringBuilder();

		List<String> vcfAttributes = Arrays.asList(new String[]
		{ VariantAnnotator.CHROMOSOME, VariantAnnotator.POSITION, VcfRepository.ID, VariantAnnotator.REFERENCE,
				VariantAnnotator.ALTERNATIVE, VariantAnnotator.QUAL, VariantAnnotator.FILTER });

		// fixed attributes: chrom pos id ref alt qual filter
		for (String attribute : vcfAttributes)
		{
			vcfRecord.append(((entity.getString(attribute) != null && !entity.getString(attribute).equals("")) ? entity.getString(attribute) : ".") + "\t");
		//	vcfRecord.append(entity.getString(attribute) + "\t");
		}
		
		// flexible 'info' field, one column with potentially many data items
		for (AttributeMetaData attributeMetaData : entity.getEntityMetaData().getAttribute("INFO").getAttributeParts())
		{
			if(entity.getString(attributeMetaData.getName()) != null)
			{
				vcfRecord.append(attributeMetaData.getName().substring(VcfRepository.getInfoPrefix().length()) + "=" + entity.getString(attributeMetaData.getName())+ ";");
			}
		}
		
		//FIXME: dirty hack!! we now look outside INFO attribute for other 'INFO_' prepended fields, that come from the annotator...
		//when data API is done we should move the annotator column into the INFO compound attribute instead!!
		for (AttributeMetaData attributeMetaData : entity.getEntityMetaData().getAtomicAttributes())
		{
			if(attributeMetaData.getName().startsWith(VcfRepository.getInfoPrefix()) && entity.getString(attributeMetaData.getName()) != null)
			{
				vcfRecord.append(attributeMetaData.getName().substring(VcfRepository.getInfoPrefix().length()) + "=" + entity.getString(attributeMetaData.getName())+ ";");
			}
		}

		
		//if we have SAMPLE data, add to output VCF
		if(entity.getEntities("Samples") != null && !Iterables.isEmpty(entity.getEntities("Samples")))
		{
			//add tab
			vcfRecord.append("\t");
			
			StringBuilder formatColumn = new StringBuilder();
			StringBuilder sampleColumn = new StringBuilder();

			for(Entity sample: entity.getEntities("SAMPLES"))
			{
				boolean firstSample = true;
				for(String attribute : sample.getAttributeNames())
				{
					//get FORMAT fields, but only for the first time
					if(firstSample)
					{
						if(attribute.equals("ID") || attribute.equals("NAME"))
						{
							//FIXME ignore ID and NAME ? hacky?
						}
						else
						{
							if(sample.getString(attribute) != null)
							{
								formatColumn.append(attribute);
								formatColumn.append(":");
							}
							
						}
					}
					
					//get values for FORMAT for each SAMPLE
					if(attribute.equals("ID") || attribute.equals("NAME"))
					{
						//FIXME ignore ID and NAME ? hacky?
					}
					else
					{
					//	sampleColumn.append(sample.getString(attribute) != null ? sample.getString(attribute) : ".");
						if(sample.getString(attribute) != null)
						{
							sampleColumn.append(sample.getString(attribute));
							sampleColumn.append(":");
						}
						
					}

				}
				
				//add FORMAT data but only first time
				if(firstSample && formatColumn.length() > 0) //FIXME: do we expect this??
				{
					formatColumn.deleteCharAt(formatColumn.length()-1); //delete trailing ':'
					vcfRecord.append(formatColumn.toString() + "\t");
					firstSample = false;
				}
				
				//now add SAMPLE data
				sampleColumn.deleteCharAt(sampleColumn.length()-1);//delete trailing ':'
				vcfRecord.append(sampleColumn.toString() + "\t");
			}
			//after all samples, delete trailing '\t'
			vcfRecord.deleteCharAt(vcfRecord.length()-1); //FIXME: need a check??
		}
		
		return vcfRecord.toString();
	}

	/**
	 * Sensitive checks for previous annotations
	 * 
	 * @param inputVcfFile
	 * @param outputVCFFile
	 * @param inputVcfFileScanner
	 * @param outputVCFWriter
	 * @param infoFields
	 * @param checkAnnotatedBeforeValue
	 * @return
	 * @throws Exception
	 */
	public static boolean checkInput(File inputVcfFile, PrintWriter outputVCFWriter, List<String> infoFields,
			String checkAnnotatedBeforeValue) throws Exception
	{
		boolean annotatedBefore = false;

		System.out.println("Detecting VCF column header...");

		Scanner inputVcfFileScanner = new Scanner(inputVcfFile, "UTF-8");
		String line = inputVcfFileScanner.nextLine();

		// if first line does not start with ##, we dont trust this file as VCF
		if (line.startsWith("##"))
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
				if (!line.startsWith("##"))
				{
					break;
				}
				System.out.print(".");
			}
			System.out.println("\nHeader line found:\n" + line);

			// check the header line
			if (!line.startsWith("#CHROM"))
			{
				outputVCFWriter.close();
				inputVcfFileScanner.close();
				throw new Exception("Header does not start with #CHROM, are you sure it is a VCF file?");
			}

			// print INFO lines for stuff to be annotated
			if (!annotatedBefore)
			{
				for (String infoField : infoFields)
				{
					outputVCFWriter.println(infoField);
				}
			}

			// now print header
			outputVCFWriter.println(line);
		}
		else
		{
			outputVCFWriter.close();
			inputVcfFileScanner.close();
			throw new Exception("Did not find ## on the first line, are you sure it is a VCF file?");
		}

		inputVcfFileScanner.close();
		return annotatedBefore;
	}

}
