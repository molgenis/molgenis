package org.molgenis.util.trityper.reader;

import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * 
 * @author harmjan
 */
public class TriTyperReader
{
	private static final Logger logger = Logger.getLogger(TriTyperReader.class);

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args)
	{
		// TODO code application logic here
		TriTyperGenotypeData data = new TriTyperGenotypeData();
		try
		{
			data.load("/Users/joeri/Desktop/BrainsetTriTyper");
			String[] snps = data.getSNPs();
			SNPLoader loader = data.createSNPLoader();

			String[] individuals = data.getIndividuals();

			for (int i = 0; i < snps.length; i++)
			{
				SNP snpObject = data.getSNPObject(i);
				loader.loadGenotypes(snpObject);
				double[] dosage = null;
				if (loader.hasDosageInformation())
				{
					loader.loadDosage(snpObject);
					dosage = snpObject.getDosageValues();
				}

				if (snpObject.passesQC())
				{
					System.out.println("SNP Passes QC!");

					short[] genotypes = snpObject.getGenotypes();
					byte[] alleles1 = snpObject.getAllele1();
					byte[] alleles2 = snpObject.getAllele2();

					for (int g = 0; g < genotypes.length; g++)
					{
						String allele1Str = BaseAnnot.toString(alleles1[g]);
						String allele2Str = BaseAnnot.toString(alleles2[g]);
						// allele1Str+allele2Str geeft nu het eigenlijke
						// genotype van individu g.

						System.out.println("al1: " + allele1Str + ", al2: " + allele2Str);

						if (loader.hasDosageInformation())
						{
							System.out.println(individuals[g] + "\t" + genotypes[g] + "\t" + dosage[g]);
						}
						else
						{
							System.out.println(individuals[g] + "\t" + genotypes[g]);
						}

					}
				}

			}

		}
		catch (IOException ex)
		{
			logger.error(ex);
		}
		catch (Exception ex)
		{
			logger.error(ex);
		}
	}
}
