package org.molgenis.data.annotation.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.vcf.utils.FixVcfAlleleNotation;

import static org.molgenis.data.vcf.utils.FixVcfAlleleNotation.trimRefAlt;

/**
 * Helper methods to deal with CADD web service output
 *
 */
public class CaddWebserviceOutputUtils
{
	/**
	 * Some usage examples
	 * @param args
	 */
	public static void main(String[] args)
	{
		System.out.println("Trimming AT/ATT to " + trimRefAlt("AT", "ATT", "/"));
		System.out.println("Trimming TA/TTA to " + trimRefAlt("TA", "TTA", "/"));
		System.out.println("Trimming ATGTG/ATG to " + trimRefAlt("ATGTG", "ATG", "/"));
		System.out.println("Trimming ATGTG/ATGTGTGTG to " + trimRefAlt("ATGTG", "ATGTGTGTG", "/"));
		System.out.println("Trimming GATAT/GAT to " + trimRefAlt("GATAT", "GAT", "/"));
		System.out.println("Trimming A/T to " + trimRefAlt("A", "T", "/"));
		System.out.println("Trimming AT/TA to " + trimRefAlt("AT", "TA", "/"));
	}
	
	/**
	 * Load web service output in a map
	 * "chr_pos_ref_alt" to CADD RAW and PHRED score (e.g. "2.178447_17.37")
	 * @param caddFile
	 * @return
	 * @throws FileNotFoundException
	 */
	public static HashMap<String, String> load(File caddFile) throws FileNotFoundException
	{
		Scanner cadd = new Scanner(caddFile);
		
		HashMap<String, String> caddScores = new HashMap<String, String>();
		
		String line = null;
		while(cadd.hasNextLine())
		{
			line = cadd.nextLine();
			if(line.startsWith("#"))
			{
				continue;
			}
			String[] split = line.split("\t", -1);
			caddScores.put(split[0] + "_" + split[1] + "_" + split[2] + "_" + split[3], split[4] + "_" + split[5]);
		}
		cadd.close();
		return caddScores;
	}

	
}
