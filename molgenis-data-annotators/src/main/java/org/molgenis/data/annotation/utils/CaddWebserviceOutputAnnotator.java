package org.molgenis.data.annotation.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;

import static org.molgenis.data.vcf.utils.FixVcfAlleleNotation.trimRefAlt;

/** 
 * Annotator that adds the output of the CADD webservice to a VCF file.
 * TODO: rewrite into a proper annotator!
 *  
 * Many indels in your VCF are not present in the static CADD files for SNV, 1000G, etc.
 * The webservice can calculate CADD scores for these variants to get more complete data.
 * This is what you get back when you use the webservice of CADD:
 * 
 * ## CADD v1.3 (c) University of Washington and Hudson-Alpha Institute for Biotechnology 2013-2015. All rights reserved.
 * #CHROM	POS	REF	ALT	RawScore	PHRED
 * 1	3102852	G	A	0.458176	7.103
 * 1	3102888	G	T	-0.088829	1.815
 * 1	3103004	G	A	1.598097	13.84
 * 1	3319479	G	A	0.717654	8.942
 * 
 * This information can be added to a VCF based on chrom, pos, ref, alt.
 *
 */
public class CaddWebserviceOutputAnnotator
{

	File vcfToAnnotate;
	File caddWebserviceOutput;
	HashMap<String, String> caddScores;
	PrintWriter pw;
	
	public CaddWebserviceOutputAnnotator(File vcfToAnnotate, File caddWebserviceOutput, File outputFile) throws FileNotFoundException
	{
		if (!vcfToAnnotate.isFile())
		{
			throw new FileNotFoundException("VCF file " + vcfToAnnotate.getAbsolutePath()
					+ " does not exist or is directory");
		}
		if (!caddWebserviceOutput.isFile())
		{
			throw new FileNotFoundException("CADD webservice output file " + caddWebserviceOutput.getAbsolutePath()
					+ " does not exist or is directory");
		}
		if (outputFile.isFile())
		{
			System.out.println("Warning: output file " + outputFile.getAbsolutePath()
					+ " already exists, overwriting content!");
		}
		this.vcfToAnnotate = vcfToAnnotate;
		this.caddWebserviceOutput = caddWebserviceOutput;
		this.pw = new PrintWriter(outputFile);
		caddScores = CaddWebserviceOutputUtils.load(caddWebserviceOutput);
	}
	
	public void annotate() throws Exception
	{
		Scanner vcfOutputScanner = new Scanner(vcfToAnnotate);
		boolean printCaddHeaderPrinted = false;
		while(vcfOutputScanner.hasNextLine())
		{
			//e.g.  '2	47630249	GA	G	1.373305	10.52'
			String line = vcfOutputScanner.nextLine();
	        if(line.startsWith("#"))
	        {
	        	pw.println(line);
	        	if(!printCaddHeaderPrinted)
	        	{
	        		pw.println("##INFO=<ID=CADD,Number=.,Type=Float,Description=\"na\">");
	        		pw.println("##INFO=<ID=CADD_SCALED,Number=.,Type=Float,Description=\"na\">");
	        		printCaddHeaderPrinted = true;
	        	}
	            continue;
	        }
	        String[] split = line.split("\t", -1);
	        
	        //splitting into: CHROM POS (id) REF ALT
	        //FIXME: obviously, this will fail for multiple alternative alleles etc.
	        String chrPosRefAlt = split[0] + "_" + split[1] + "_" + split[3] + "_" + split[4];
	        
	        String caddRawAndPhred = null;
	        if(caddScores.containsKey(chrPosRefAlt))
	        {
	        	caddRawAndPhred = caddScores.get(chrPosRefAlt);
	        }
	        else
	        {
	        	//if not found, try trimming alleles and then matching
	        	String trimmedRefAlt = trimRefAlt(split[3], split[4], "_");
	        	chrPosRefAlt = split[0] + "_" + split[1] + "_" + trimmedRefAlt;
	        	if(caddScores.containsKey(chrPosRefAlt))
		        {
		        	caddRawAndPhred = caddScores.get(chrPosRefAlt);
		        }
	        	else
	        	{
	        		System.out.println("WARNING: CADD score missing for " + split[0] + " " + split[1] + " " + split[3] + " " + split[4] + " ! (even when using trimmed key '"+chrPosRefAlt+"')");
	        	}
	        }
	        
	        if(caddRawAndPhred != null)
	        {
	        	String[] caddScore = caddRawAndPhred.split("_");
	        	String caddRaw = caddScore[0];
	        	String caddScaled = caddScore[1];
	        	
	        	StringBuffer lineWithCadd = new StringBuffer();
	        	for(int i = 0; i < split.length; i ++)
	        	{
	        		if(i == 7)
	        		{
	        			lineWithCadd.append("CADD=" + caddRaw + ";CADD_SCALED=" + caddScaled + ";" + split[i] + "\t");
	        		}
	        		else
	        		{
	        			lineWithCadd.append(split[i] + "\t");
	        		}
	        		
	        	}
	        	lineWithCadd.deleteCharAt(lineWithCadd.length()-1);
	        	pw.println(lineWithCadd.toString());
	        }
	        else
	        {
	        	pw.println(line);
	        }
	        
	        
	        pw.flush();
		}
		vcfOutputScanner.close();
		pw.close();
		
		System.out.println("Done!");
	}

	public static void main(String[] args) throws Exception
	{
		File vcfToAnnotate = new File(args[0]);
		File caddWebserviceOutput = new File(args[1]);
		File outputFile = new File(args[2]);
		CaddWebserviceOutputAnnotator cwoa = new CaddWebserviceOutputAnnotator(vcfToAnnotate, caddWebserviceOutput, outputFile);
		cwoa.annotate();
	}

}
