package org.molgenis.data.vcf.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Scanner;

import org.springframework.util.StringUtils;

public class FixVcfAlleleNotation
{
	/**
	 * Fix N notation in VCFs produced by Ensembl VEP web service.
	 * 
	 * Example:
	 * 11	47354445	MYBPC3:c.3407_3409delACT	NAGT	N
	 * 
	 * Get location for UCSC:
	 * http://genome.ucsc.edu/cgi-bin/das/hg19/dna?segment=chr11:47354445,47354445
	 * Returns:
	 * "<DNA length="1">a</DNA>"
	 * 
	 * We need:
	 * "a"
	 * 
	 * And use it to turn the VCF record into:
	 * 11	47354445	MYBPC3:c.3407_3409delACT	AAGT	A
	 * 
	 * Both deletions and insertions suffer from this, e.g.:
	 * 6	51612724	PKHD1:c.9689delA	NT	N
	 * 6	51824680	PKHD1:c.5895dupA	N	NT
	 * 
	 * Caveat:
	 * Some original HGVS/cDNA notation inplies an insertion/deletion event involving the same bases, for example "c.652delAinsTA"
	 * This gets turned into "NA/NTA" and subsequently fixed into "TA/TTA". Note that the "A" is unnecessary here!
	 * This is not nice and some tools get confused. For example, CADD webservice doesn't understand until you change it into "T/TT".
	 * So we want either ref or alt to be 1 basepair, which is the right way to express variants in VCF format.
	 * This is why we do a check and fix it here.
	 * 
	 * 
	 * 
	 */
	public static void main(String[] args) throws Exception
	{
		File in = new File(args[0]);
		File out = new File(args[1]);
		
		PrintWriter pw = new PrintWriter(out);
		Scanner s = new Scanner(in);
		String line;
		while(s.hasNextLine())
		{
			//write out header untouched
			line = s.nextLine();
			if(line.startsWith("#")){
				pw.println(line);
				continue;
			}
			String[] split = line.split("\t");
			
			String chr = split[0];
			String pos = split[1];
			String ref = split[3];
			String alt = split[4];
			
			//first check if ref or alt need trimming. it does not matter that there may be an N.
			String[] trimmedRefAlt = trimRefAlt(ref, alt, "_").split("_");
			if(!ref.equals(trimmedRefAlt[0]) || !alt.equals(trimmedRefAlt[1]))
			{
				System.out.println("trimming ref/alt, from " + ref + "/" + alt + " to " + trimmedRefAlt[0] + "/" + trimmedRefAlt[1]);
				ref = trimmedRefAlt[0];
				alt =  trimmedRefAlt[1];
			}
			
			boolean queryUCSC = false;
			//if not both start with N, we expect neither to start with N (see example)
			if(!(ref.startsWith("N") && alt.startsWith("N")))
			{
				// could mean we DID fix the notation, so dont quit yet!
//				System.out.println("no reason to adjust variant " + chr + ":"+pos+" " + ref + "/" + alt + " because there is no N");
//				pw.println(line);
//				continue;
			}
			else if(ref.startsWith("N") && alt.startsWith("N"))
			{
				System.out.println("need to adjust variant " + chr + ":pos " + ref + "/" + alt + " because there is an N");
				int refNOccurence = StringUtils.countOccurrencesOf(ref, "N");
				int altNOccurence = StringUtils.countOccurrencesOf(alt, "N");
				if(refNOccurence != 1 || altNOccurence != 1)
				{
					s.close();
					pw.close();
					throw new Exception("expecting 'N' occurence == 1 for " + ref + " and " + alt);
				}
				queryUCSC = true;
			}
			//sanity check
			else
			{
				s.close();
				pw.close();
				throw new Exception("either ref "+ref+" or alt "+alt+" starts with N, not expected this");
			}
			
			String replacementRefBase = "if you see this, we did not get a replacement base while we needed one!";
			if(queryUCSC)
			{
				//get replacement base for N from UCSC
				URL ucsc = new URL("http://genome.ucsc.edu/cgi-bin/das/hg19/dna?segment=chr"+chr+":"+pos+","+pos);
				BufferedReader getUrlContent = new BufferedReader(new InputStreamReader(ucsc.openStream()));
				String urlLine;
				
				while ((urlLine = getUrlContent.readLine()) != null)
				{
					//the base ('g', 'c', 'a', 't') is on line of its own, so length == 1
					if(urlLine.length() == 1)
					{
						replacementRefBase = urlLine.toUpperCase();
						System.out.println("we found replacement base for N = " + replacementRefBase);
					}
				}
				getUrlContent.close();
				
				//wait a little bit
				Thread.sleep(100);
			}

			//print the fixed notation
			StringBuffer fixedLine = new StringBuffer();
			for(int i = 0; i < split.length; i ++)
			{
				if(i == 3 || i == 4)
				{
					String fixedNotation = i == 3 ? ref.replace("N", replacementRefBase) : alt.replace("N", replacementRefBase);
					fixedLine.append(fixedNotation + "\t");
				}
				else
				{
					fixedLine.append(split[i] + "\t");
				}
			}
			
			//remove trailing \t
			fixedLine.deleteCharAt(fixedLine.length()-1);
			
			//print & flush
			pw.println(fixedLine);
			pw.flush();
			
		}
		pw.flush();
		pw.close();
		
		s.close();
		
		System.out.println("Done!");

	}



	/**
	 * Helper method to try and retrieve CADD scores by first trimming ref or alt alleles.
	 * AT ATT -> A AT
	 * ATGTG ATG -> ATG A
	 * ATGTG ATGTGTGTG -> A ATGTG
	 * GATAT GAT -> GAT G
	 */
	public static String trimRefAlt(String ref, String alt, String sep)
	{

		char[] refRev = org.apache.commons.lang3.StringUtils.reverse(ref).toCharArray();
		char[] altRev = org.apache.commons.lang3.StringUtils.reverse(alt).toCharArray();

		int nrToDelete = 0;
		for(int i = 0; i < refRev.length; i++)
		{
			char refBase = refRev[i];
			char altBase = altRev[i];
			if(i == refRev.length-1 || i == altRev.length-1) //to stop deleting the last base, e.g. in AT/AAT or TA/TTA
			{
				break;
			}
			else if(refBase == altBase)
			{
				nrToDelete++;
			}
			else
			{
				break;
			}
		}
		String newRef = ref.substring(0, ref.length()-nrToDelete);
		String newAlt = alt.substring(0, alt.length()-nrToDelete);

		return newRef + sep + newAlt;
	}

}
