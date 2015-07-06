package org.molgenis.data.annotation.utils;

import org.molgenis.data.annotator.tabix.TabixReader;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

public class MergeDenovoFromPhaseByTransmissionWithVCF
{

	public MergeDenovoFromPhaseByTransmissionWithVCF(File mvf, File vcf, File out) throws Exception
	{
		String vcfHeader = "#CHROM	POS	ID	REF	ALT	QUAL	FILTER";
		String infoHeader = "ABHet	ABHom	AC	AF	AN	BaseQRankSum	DB	DP	DS	Dels	FS	HRun	HaplotypeScore	InbreedingCoeff	MQ	MQ0	MQRankSum	OND	QD	ReadPosRankSum	SB";
		String mvfHeader = "CHROM	POS	AC	FAMILY	TP	MOTHER_GT	MOTHER_DP	MOTHER_AD	MOTHER_PL	FATHER_GT	FATHER_DP	FATHER_AD	FATHER_PL	CHILD_GT	CHILD_DP	CHILD_AD	CHILD_PL";
		
		
		Scanner mvfReader = new Scanner(mvf);
		
		//skip header
		String mvfLine = mvfReader.nextLine();
		
		TabixReader tabixReader = new TabixReader(vcf.getAbsolutePath());

		PrintWriter writer = new PrintWriter(out, "UTF-8");
		writer.println(vcfHeader + "\t" + infoHeader + "\t" + mvfHeader);
		
		while_:
		while (mvfReader.hasNextLine())
		{
			mvfLine = mvfReader.nextLine();
			
			
			/**
			 * FILTER
			 */
			if(mvfLine.contains("./."))
			{
				//skip lines with missing genotypes!
				continue while_;
			}
			
			
			/**
			 * FILTER
			 */
			if(mvfLine.startsWith("X"))
			{
				//skip lines for chrom X (for now)
				continue while_;
			}

			
			String[] split = mvfLine.split("\t", -1);
			String chr = split[0];
			String pos = split[1];

			TabixReader.Iterator tabixIterator = null;
			try
			{
				tabixIterator = tabixReader.query(chr + ":" + pos + "-" + pos);
			}
			catch (Exception e)
			{
				throw new Exception("Something went wrong (chromosome not in data?) when querying CADD tabix file for "
						+ chr + " POS: " + pos);
			}

			String vcfLine = null;

			try
			{
				vcfLine = tabixIterator.next();
			}
			catch (net.sf.samtools.SAMFormatException sfx)
			{
				throw new Exception("Bad GZIP file for CHROM: " + chr + " POS: " + pos + " LINE: " + mvfLine);
			}
			catch (NullPointerException npe)
			{
				throw new Exception("No data for CHROM: " + chr + " POS: " + pos + " LINE: " + mvfLine);
			}
			
			
			String vcfLineSplit[] = vcfLine.split("\t", -1);
			String vcfStuff = vcfLineSplit[0] + "\t" + vcfLineSplit[1] + "\t" + vcfLineSplit[2] + "\t" + vcfLineSplit[3] + "\t" + vcfLineSplit[4] + "\t" + vcfLineSplit[5] + "\t" + vcfLineSplit[6];
			
			boolean lineIsFilteredOut = false;
			String vcfInfoLineSplit[] = vcfLineSplit[7].split(";",-1);
			StringBuffer infoStuff = new StringBuffer();
			for(String infoFieldFromHeader : infoHeader.split("\t"))
			{
				boolean found = false;
				for(String infoFieldFromVCF : vcfInfoLineSplit)
				{
					String infoFieldFromVCFSplit[] = infoFieldFromVCF.split("=",-1);
					if(infoFieldFromVCFSplit[0].equals(infoFieldFromHeader))
					{
						found = true;
						if(infoFieldFromHeader.equals("DS") || infoFieldFromHeader.equals("DB"))
						{
							//its a flag..
							infoStuff.append(infoFieldFromHeader);
						}
						else
						{
							/**
							 * FILTER
							 */
							// "The higher the output value, the more likely there is to be bias. More bias is indicative of false positive calls."
							// https://www.broadinstitute.org/gatk/gatkdocs/org_broadinstitute_gatk_tools_walkers_annotator_FisherStrand.php
							if(infoFieldFromHeader.equals("FS"))
							{
								double FS = Double.parseDouble(infoFieldFromVCFSplit[1]);
								if(FS > 20)
								{
									System.out.println("FS > 20 ("+FS+") for " + mvfLine);
									continue while_;
								}
								
							}
							
							
							infoStuff.append(infoFieldFromVCFSplit[1]);
						}
						infoStuff.append("\t");
						
					}
				}
				
				if(!found)
				{
					infoStuff.append("\t");
				}
			}
			
			
			
			/**
			 * FILTER
			 */
			// "the greater the TP, the more confident the call"
			// http://gatkforums.broadinstitute.org/discussion/2470/phasebytransmission-help
			String[] mvfLineSplit = mvfLine.split("\t", -1);
			double TP = Double.parseDouble(mvfLineSplit[4]);
			if(TP < 20)
			{
				System.out.println("TP < 20 ("+TP+") for " + mvfLine);
				continue while_;
			}
			
			
			/**
			 * FILTER
			 */
			// sequencing depth, 20 is a nice cutoff
			double CHILD_DP = Double.parseDouble(mvfLineSplit[14]);
			if(CHILD_DP < 20)
			{
				System.out.println("CHILD_DP < 20 ("+CHILD_DP+") for " + mvfLine);
				continue while_;
			}
			

			writer.println(vcfStuff + "\t" + infoStuff + mvfLine);
			writer.flush();
			
		}

		mvfReader.close();
		writer.close();
	}

	public static void main(String[] args) throws Exception
	{
		if (args.length != 3)
		{
			throw new Exception(
					"Usage: java -Xmx4g -jar MergeDenovoFromPhaseByTransmissionWithVCF.jar [mvf.txt] [tabix indexed GZ VCF file] [output file].\n");
		}

		File mvf = new File(args[0]);
		if (!mvf.exists())
		{
			throw new Exception("Mendelian violations file not found at " + mvf);
		}

		File vcf = new File(args[1]);
		if (!vcf.exists())
		{
			throw new Exception("VCF file not found at " + vcf);
		}

		File output = new File(args[2]);
		if (output.exists())
		{
			System.out.println("WARNING: output file already exists at " + output);
		}

		new MergeDenovoFromPhaseByTransmissionWithVCF(mvf, vcf, output);

	}

}
