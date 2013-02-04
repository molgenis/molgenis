package org.molgenis.util.trityper.reader;

import java.nio.charset.Charset;

/**
 * 
 * @author harmjan
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value =
{ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" }, justification = "Not accessed by untrusted code")
public class SNP
{
	private byte chr;
	private int chrPos;
	private int id;
	private String name;
	private byte[] dosage;
	private byte[] alleles1;
	private byte[] alleles2;
	private Double HWEP;
	private Double MAF;
	private Double CR;
	private short[] genotypes;
	private double[] alleleFreq;
	private Byte minorAllele;
	private boolean passesQC;
	private byte[] alleles;
	public int nrCalled;
	private int[] genotypeFreq;

	public void setChr(byte chr)
	{
		this.chr = chr;
	}

	public void setChrPos(int chrPos)
	{
		this.chrPos = chrPos;
	}

	public byte getChr()
	{
		return this.chr;
	}

	public int getChrPos()
	{
		return this.chrPos;
	}

	public void setId(int snpId)
	{
		this.id = snpId;
	}

	public int getId()
	{
		return this.id;
	}

	public void setName(String snp)
	{
		this.name = snp;
	}

	public String getName()
	{
		return name;
	}

	public void setAlleles(byte[] allele1, byte[] allele2, Boolean[] indIncluded, Boolean[] indIsFemale)
	{
		this.alleles1 = allele1;
		this.alleles2 = allele2;
		genotypes = new short[allele1.length];
		alleles = new byte[3];

		short alleleItr = 0;
		int missingGenotypes = 0;
		genotypeFreq = new int[3];
		int nrTotal = 0;
		nrCalled = 0;
		for (int ind = 0; ind < alleles1.length; ind++)
		{
			Boolean inc = indIncluded[ind];
			if ((inc != null && inc) && (chr != 23 || indIsFemale[ind]))
			{
				nrTotal++;
			}
			if (allele1[ind] != 0 && allele2[ind] != 0 && (inc != null && inc))
			{

				// X-chromosomal SNPs get different treatment, as they are
				// heterozygous in all males
				if (chr != 23 || indIsFemale[ind])
				{
					nrCalled++;
				}

				short allelecode1 = -1;
				short allelecode2 = -1;

				for (short i = 0; i < 2; i++)
				{
					if (alleles[i] == allele1[ind])
					{
						allelecode1 = i;
					}
				}

				if (allelecode1 == -1)
				{
					alleles[alleleItr] = allele1[ind];
					allelecode1 = alleleItr;
					alleleItr++;
					if (alleleItr > 2)
					{
						System.out.println("ERROR!!!: Number of different alleles for SNP\t" + name
								+ "\t is more than two!");
						System.out.println("Allele 1:\t" + alleles[0] + " / " + new String(new byte[]
						{ alleles[0] }, Charset.forName("UTF-8")));
						System.out.println("Allele 2:\t" + alleles[1] + " / " + new String(new byte[]
						{ alleles[1] }, Charset.forName("UTF-8")));
						System.out.println("Allele 3:\t" + alleles[2] + " / " + new String(new byte[]
						{ alleles[2] }, Charset.forName("UTF-8")));
						break;
					}
				}

				for (short i = 0; i < 2; i++)
				{
					if (alleles[i] == allele2[ind])
					{
						allelecode2 = i;
					}
				}
				if (allelecode2 == -1)
				{
					alleles[alleleItr] = allele2[ind];
					allelecode2 = alleleItr;
					alleleItr++;
					if (alleleItr > 2)
					{
						System.out.println("ERROR!!!: Number of different alleles for SNP\t" + name
								+ "\t is more than two!");

						System.out.println("Allele 1:\t" + alleles[0] + " / " + new String(new byte[]
						{ alleles[0] }, Charset.forName("UTF-8")));

						System.out.println("Allele 2:\t" + alleles[1] + " / " + new String(new byte[]
						{ alleles[1] }, Charset.forName("UTF-8")));

						System.out.println("Allele 3:\t" + alleles[2] + " / " + new String(new byte[]
						{ alleles[2] }, Charset.forName("UTF-8")));
						break;
					}
				}

				short genotypeCode = (short) (allelecode1 + allelecode2);
				genotypes[ind] = genotypeCode;

				// X-chromosomal SNPs get different treatment, as they are
				// heterozygous in all males
				if (chr != 23 || indIsFemale[ind])
				{
					genotypeFreq[genotypeCode]++;
				}

			}
			else
			{
				if (inc != null && inc)
				{
					missingGenotypes++;
				}

				genotypes[ind] = -1;
			}
		}

		alleleFreq = new double[2];

		alleleFreq[0] = 2 * genotypeFreq[0] + genotypeFreq[1];
		alleleFreq[1] = 2 * genotypeFreq[2] + genotypeFreq[1];
		MAF = (alleleFreq[0]) / ((double) (nrCalled) * 2d);

		minorAllele = alleles[0];
		if (alleleFreq[0] > alleleFreq[1])
		{
			minorAllele = alleles[1];
			MAF = 1 - MAF;
		}

		// //Hardy Weinburg Test:
		// double[] genotypesExpected = new double[3];
		// genotypesExpected[0] = (alleleFreq[0] / 2) /
		// (genotypeFreq[0]+genotypeFreq[1]+genotypeFreq[2]) * (alleleFreq[0] /
		// 2);
		// genotypesExpected[1] = 2 * (genotypeFreq[0] + (genotypeFreq[1]/2d)) /
		// (genotypeFreq[0]+genotypeFreq[1]+genotypeFreq[2]) * (genotypeFreq[2]
		// + (genotypeFreq[1]/2d));
		// genotypesExpected[2] = (alleleFreq[1] / 2) /
		// (genotypeFreq[0]+genotypeFreq[1]+genotypeFreq[2]) * (alleleFreq[1] /
		// 2);

		this.HWEP = getExactHWEPValue(genotypeFreq[1], genotypeFreq[0], genotypeFreq[2]);

		passesQC = false;

		CR = (double) nrCalled / nrTotal;

		if ((genotypeFreq[0] > 0 && genotypeFreq[1] > 0) || (genotypeFreq[1] > 0 && genotypeFreq[2] > 0)
				|| (genotypeFreq[0] > 0 && genotypeFreq[2] > 0))
		{
			passesQC = true;
		}
	}

	public void setPassesQC(boolean b)
	{
		passesQC = b;
	}

	public boolean passesQC()
	{
		return passesQC;
	}

	private double getExactHWEPValue(int obs_hets, int obs_hom1, int obs_hom2)
	{
		// System.out.println("Starting exact HWE:\t" + obs_hets + "\t" +
		// obs_hom1 + "\t" + obs_hom2);

		int obs_homc = obs_hom1 < obs_hom2 ? obs_hom2 : obs_hom1;
		int obs_homr = obs_hom1 < obs_hom2 ? obs_hom1 : obs_hom2;

		int rare_copies = 2 * obs_homr + obs_hets;
		int genotypes = obs_hets + obs_homc + obs_homr;

		if (genotypes == 0) return -1;

		double[] het_probs = new double[rare_copies + 1];

		int i;
		for (i = 0; i <= rare_copies; i++)
			het_probs[i] = 0.0;

		/* start at midpoint */
		int mid = rare_copies * (2 * genotypes - rare_copies) / (2 * genotypes);

		/* check to ensure that midpoint and rare alleles have same parity */
		if (mid % 2 != rare_copies % 2) mid++;

		int curr_hets = mid;
		int curr_homr = (rare_copies - mid) / 2;
		int curr_homc = genotypes - curr_hets - curr_homr;

		het_probs[mid] = 1.0;
		double sum = het_probs[mid];
		for (curr_hets = mid; curr_hets > 1; curr_hets -= 2)
		{
			het_probs[curr_hets - 2] = het_probs[curr_hets] * curr_hets * (curr_hets - 1.0)
					/ (4.0 * (curr_homr + 1.0) * (curr_homc + 1.0));
			sum += het_probs[curr_hets - 2];
			/*
			 * 2 fewer heterozygotes for next iteration -> add one rare, one
			 * common homozygote
			 */
			curr_homr++;
			curr_homc++;
		}

		curr_hets = mid;
		curr_homr = (rare_copies - mid) / 2;
		curr_homc = genotypes - curr_hets - curr_homr;
		for (curr_hets = mid; curr_hets <= rare_copies - 2; curr_hets += 2)
		{
			het_probs[curr_hets + 2] = het_probs[curr_hets] * 4.0 * curr_homr * curr_homc
					/ ((curr_hets + 2.0) * (curr_hets + 1.0));
			sum += het_probs[curr_hets + 2];
			curr_homr--;
			curr_homc--;
		}

		for (i = 0; i <= rare_copies; i++)
			het_probs[i] /= sum;

		double p_hwe = 0.0;
		for (i = 0; i <= rare_copies; i++)
		{
			if (het_probs[i] <= het_probs[obs_hets])
			{
				p_hwe += het_probs[i];
			}
		}

		p_hwe = p_hwe > 1.0 ? 1.0 : p_hwe;

		return p_hwe;
	}

	/**
	 * @return the HWEP
	 */
	public Double getHWEP()
	{
		return HWEP;
	}

	/**
	 * @param HWEP
	 *            the HWEP to set
	 */
	public void setHWEP(Double HWEP)
	{
		this.HWEP = HWEP;
	}

	/**
	 * @return the MAF
	 */
	public Double getMAF()
	{
		return MAF;
	}

	/**
	 * @param MAF
	 *            the MAF to set
	 */
	public void setMAF(Double MAF)
	{
		this.MAF = MAF;
	}

	/**
	 * @return the CR
	 */
	public Double getCR()
	{
		return CR;
	}

	/**
	 * @param CR
	 *            the CR to set
	 */
	public void setCR(Double CR)
	{
		this.CR = CR;
	}

	public void setAlleles(byte[] allele1, byte[] allele2)
	{
		this.alleles1 = allele1;
		this.alleles2 = allele2;
	}

	public void clearGenotypes()
	{
		this.alleles1 = null;
		this.alleles2 = null;
		this.genotypes = null;
		this.alleleFreq = null;
		this.dosage = null;
	}

	public byte[] getAllele1()
	{
		return alleles1;
	}

	public byte[] getAllele2()
	{
		return alleles2;
	}

	public short[] getGenotypes()
	{
		return genotypes;
	}

	public void setDosage(byte[] dosageValues)
	{
		this.dosage = dosageValues;

	}

	public double[] getDosageValues()
	{
		if (dosage != null)
		{
			double[] dosagevalues = new double[dosage.length];
			for (int i = 0; i < dosage.length; i++)
			{
				dosagevalues[i] = ((double) (-Byte.MIN_VALUE + dosage[i])) / 100;
			}
			return dosagevalues;
		}
		else
		{
			return null;
		}
	}

	public boolean hasDosageInformation()
	{
		return (dosage != null);
	}

	public double[] selectGenotypes(int[] ids)
	{
		return selectGenotypes(ids, false);
	}

	public double[] selectGenotypes(int[] ids, boolean includeMissingGenotypes)
	{
		int numReq = ids.length;
		int i;
		int numAvail = 0;

		for (i = 0; i < numReq; i++)
		{
			int id = ids[i];
			// if we're including missing genotypes, or the genotypes for the
			// expression sample are unequal to -1
			if (includeMissingGenotypes || genotypes[ids[i]] != -1)
			{
				// if the expression sample has a genotype
				if (ids[i] != -1)
				{
					numAvail++;
				}
			}
		}

		double[] gtypes = new double[numAvail];
		int q = 0;
		for (i = 0; i < numReq; i++)
		{
			int id = ids[i];
			if (id != -1 && (includeMissingGenotypes || genotypes[id] != -1))
			{
				if (dosage != null)
				{
					double dosagevalue = ((double) (-Byte.MIN_VALUE + dosage[id])) / 100;
					gtypes[q] = dosagevalue;
				}
				else
				{
					gtypes[q] = genotypes[id];
				}
				q++;
			}
		}
		return gtypes;
	}

	public byte[] getAlleles()
	{
		return alleles;
	}

	public Byte getMinorAllele()
	{
		return minorAllele;
	}

	public double[] getAlleleFreq()
	{
		return alleleFreq;
	}

	/**
	 * @return the genotypeFreq
	 */
	public int[] getGenotypeFreq()
	{
		return genotypeFreq;
	}

	/**
	 * @param genotypeFreq
	 *            the genotypeFreq to set
	 */
	public void setGenotypeFreq(int[] genotypeFreq)
	{
		this.genotypeFreq = genotypeFreq;
	}

}
