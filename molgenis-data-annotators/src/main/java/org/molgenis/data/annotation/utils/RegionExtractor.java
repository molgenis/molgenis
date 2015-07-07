package org.molgenis.data.annotation.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import org.molgenis.data.annotator.tabix.TabixReader;

/**
 * For now, quickly made tool to get a region of data out from an annotator source. Typical question: from source A, I
 * want to pull out regions B filtered by C, grouped by common denominator D. For example: from ExAC (A), give me
 * Loss-of-Function variants (C) for specific genes (FD) from these exonic regions (B). We need to generalize this, just
 * like the rework of the regular entity-annotators. For now, we hard code it because its needed.
 *
 */
public class RegionExtractor
{

	public RegionExtractor(File input, File output, File vcfTabixRepo) throws IOException
	{

		TabixReader vcfTabixReader = new TabixReader(vcfTabixRepo.getAbsolutePath());

		Scanner s = new Scanner(input, "UTF-8");
		s.nextLine(); // skip header

		HashMap<String, List<String>> geneToVariants = new HashMap<String, List<String>>();

		String line = null;
		while (s.hasNext())
		{
			line = s.nextLine();
			//System.out.println("getting " + line);
			String[] split = line.split("\t", -1);
			String gene = split[0];
			String chr = split[1];
			String start = split[2];
			String stop = split[3];
			TabixReader.Iterator it = vcfTabixReader.query(chr + ":" + start + "-" + stop);
			String next;

			if (!geneToVariants.containsKey(gene))
			{
				List<String> variants = new ArrayList<String>();
				geneToVariants.put(gene, variants);
			}

			while (it != null && (next = it.next()) != null)
			{
				if (next.contains("\tPASS\t"))
				{
					if (next.contains("frameshift_variant"))
					{
						geneToVariants.get(gene).add("frameshift_variant\t" + next);
					}
					else if (next.contains("stop_gained"))
					{
						geneToVariants.get(gene).add("stop_gained\t" + next);
					}
					else if (next.contains("splice_donor_variant"))
					{
						geneToVariants.get(gene).add("splice_donor_variant\t" + next);
					}
					else if (next.contains("splice_acceptor_variant"))
					{
						geneToVariants.get(gene).add("splice_acceptor_variant\t" + next);
					}
				}

			}

		}

		s.close();

		PrintWriter pw = new PrintWriter(output, "UTF-8");

		for (Entry<String, List<String>> geneEntry : geneToVariants.entrySet())
		{
			for (String variant : geneEntry.getValue())
			{
				pw.println(geneEntry.getKey() + "\t" + variant);
			}
		}
		
		System.out.println("Gene\tNrOfLoF");
		for (String gene : geneToVariants.keySet())
		{
			System.out.println(gene + "\t" + geneToVariants.get(gene).size());
		}
		
		pw.flush();
		pw.close();

	}

	public static void main(String[] args) throws Exception
	{
		// Input:
		// <header>
		// AIP 11 67256807 67256926
		// AIP 11 67257787 67258124
		// AKT1 14 105258935 105260461
		// AKT1 14 105246425 105246553

		// get LoF:
		// 'frameshift_variant', 'stop_gained', 'splice_donor_variant', 'splice_acceptor_variant'

		// Output
		// AIP stop_gained 11 67257961 . C T 831 PASS AC=2;AC_AFR=0;AC_AMR=0; etc
		// AIP stop_gained 11 67257998 . G A 658.69 PASS AC=1;AC_AFR=0;AC_AMR=0; etc
		// AIP frameshift_variant 11 67257969 . CAG C 11623.47 PASS AC=17;AC_AFR=15;AC_AMR=0; etc
		// AKT1 stop_gained 14 105258948 . C T 1139.49 PASS AC=1;AC_AFR=0;AC_AMR=0; etc
		// etc

		if (args.length != 3)
		{
			throw new Exception("Expecting 3 arguments: input file, output file, vcf tabix repo");
		}

		File input = new File(args[0]);
		if (!input.exists())
		{
			throw new Exception("Input file not found at " + input.getAbsolutePath());
		}

		File output = new File(args[1]);
		if (output.exists())
		{
			System.out.println("WARNING: output file already exists at " + output.getAbsolutePath() + ", overwriting!");
		}

		File vcfTabixRepo = new File(args[2]);
		if (!vcfTabixRepo.exists())
		{
			throw new Exception("vcfTabixRepo not found at " + vcfTabixRepo.getAbsolutePath());
		}

		System.out.println("Starting..");

		new RegionExtractor(input, output, vcfTabixRepo);

		System.out.println("Done!");

	}

}
