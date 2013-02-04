package org.molgenis.util.plink.datatypes;

public class Biallele
{
	private final char allele1;
	private final char allele2;

	public Biallele(char allele1, char allele2)
	{
		this.allele1 = allele1;
		this.allele2 = allele2;
	}

	public Biallele(String allele1, String allele2) throws Exception
	{
		if (allele1.length() != 1 || allele2.length() != 1) throw new Exception(
				"Inputs must have 1 character each (1 per allele)");
		this.allele1 = allele1.charAt(0);
		this.allele2 = allele2.charAt(0);
	}

	public Biallele(String allele) throws Exception
	{
		if (allele.length() != 2) throw new Exception("Input must have 2 characters (allele 1 & 2)");
		this.allele1 = allele.charAt(0);
		this.allele2 = allele.charAt(1);
	}

	public char getAllele1()
	{
		return allele1;
	}

	public char getAllele2()
	{
		return allele2;
	}

	public static Biallele create(char allele1, char allele2)
	{
		switch (allele1)
		{
			case 'A':
				switch (allele2)
				{
					case 'A':
						return Biallele_AA;
					case 'C':
						return Biallele_AC;
				}
				break;
			case 'C':
				switch (allele2)
				{
					case 'A':
						return Biallele_CA;
					case 'C':
						return Biallele_CC;
				}
				break;
			case 'G':
				switch (allele2)
				{
					case 'G':
						return Biallele_GG;
					case 'T':
						return Biallele_GT;
				}
				break;
			case 'T':
				switch (allele2)
				{
					case 'G':
						return Biallele_TG;
					case 'T':
						return Biallele_TT;
				}
				break;
			case '0':
				if (allele2 == '0') return Biallele_00;
		}
		return new Biallele(allele1, allele2);
	}

	@Override
	public String toString()
	{
		return allele1 + " " + allele2;
	}

	private static final Biallele Biallele_AA = new Biallele('A', 'A');
	private static final Biallele Biallele_AC = new Biallele('A', 'C');
	private static final Biallele Biallele_CA = new Biallele('C', 'A');
	private static final Biallele Biallele_CC = new Biallele('C', 'C');
	private static final Biallele Biallele_GG = new Biallele('G', 'G');
	private static final Biallele Biallele_GT = new Biallele('G', 'T');
	private static final Biallele Biallele_TG = new Biallele('T', 'G');
	private static final Biallele Biallele_TT = new Biallele('T', 'T');
	private static final Biallele Biallele_00 = new Biallele('0', '0');
}
