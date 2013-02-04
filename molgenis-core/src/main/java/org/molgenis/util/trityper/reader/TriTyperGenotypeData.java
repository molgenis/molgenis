package org.molgenis.util.trityper.reader;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @author harmjan
 */
public class TriTyperGenotypeData
{
	private String[] SNPs;
	private Boolean[] isFemale;
	private Boolean[] isCase;
	private Boolean[] isIncluded;
	private String[] individuals;

	private HashMap<String, Integer> individualToId;
	private HashMap<String, Integer> snpToSNPId;
	private String genotypeFileName;
	private String dosageFileName;

	private byte[] chr;
	private int[] chrPos;

	public TriTyperGenotypeData()
	{

	}

	public void load(String loc) throws IOException, Exception
	{
		if (!loc.endsWith("/"))
		{
			loc += "/";
		}

		checkFiles(loc);

		TextFile t = new TextFile(loc + "Individuals.txt", TextFile.R);
		String[] lineElems = t.readLineElems(TextFile.tab);
		ArrayList<String> alInd = new ArrayList<String>();
		int i = 0;
		individualToId = new HashMap<String, Integer>();
		while (lineElems != null)
		{
			String individual = lineElems[0];
			individualToId.put(individual, i);
			alInd.add(individual);
			i++;
			lineElems = t.readLineElems(TextFile.tab);
		}
		t.close();

		int numInds = individualToId.size();

		setIsFemale(new Boolean[numInds]);
		setIsCase(new Boolean[numInds]);
		setIsIncluded(new Boolean[numInds]);

		individuals = new String[numInds];

		for (i = 0; i < numInds; i++)
		{
			getIsFemale()[i] = null;
			getIsCase()[i] = null;
			getIsIncluded()[i] = null;
			individuals[i] = alInd.get(i);
		}
		alInd = null;

		t = new TextFile(loc + "PhenotypeInformation.txt", TextFile.R);

		int numMales = 0;
		int numFemales = 0;
		int numCases = 0;
		int numControls = 0;
		int numIncluded = 0;

		lineElems = t.readLineElems(TextFile.tab);
		alInd = new ArrayList<String>();
		while (lineElems != null)
		{
			String ind = lineElems[0];
			Integer indId = individualToId.get(ind);
			if (indId != null)
			{
				if (lineElems[1].equals("control"))
				{
					isCase[indId] = false;
					numControls++;
				}
				else if (lineElems[1].equals("case"))
				{
					isCase[indId] = true;
					numCases++;
				}
				else
				{
					System.err.println("Warning: case/control status unparseable for\t" + lineElems[1] + "\tfor\t"
							+ indId);
				}

				if (lineElems[2].equals("exclude"))
				{
					isIncluded[indId] = false;

				}
				else if (lineElems[2].equals("include"))
				{
					isIncluded[indId] = true;
					numIncluded++;
				}
				else
				{
					System.err.println("Warning: include/exclude status unparseable\t" + lineElems[2] + "\tfor\t"
							+ indId);
				}

				if (lineElems[3].equals("male"))
				{
					isFemale[indId] = false;
					numMales++;
				}
				else if (lineElems[3].equals("female"))
				{
					isFemale[indId] = true;
					numFemales++;
				}
				else
				{
					System.err.println("Warning: gender status unparseable\t" + lineElems[3] + "\tfor\t" + indId);
				}
			}
			lineElems = t.readLineElems(TextFile.tab);
		}
		t.close();

		System.out.println(numInds + " individuals detected, " + numMales + " males, " + numFemales + " females, "
				+ numCases + " cases, " + numControls + " controls, " + numIncluded + " included");

		t = new TextFile(loc + "SNPs.txt", TextFile.R);

		String line = t.readLine();
		alInd = new ArrayList<String>();
		int snpId = 0;
		ArrayList<String> tmpSNP = new ArrayList<String>();
		snpToSNPId = new HashMap<String, Integer>();

		while (line != null)
		{
			if (line.trim().length() > 0)
			{
				String snp = line;
				snpToSNPId.put(snp, snpId);
				tmpSNP.add(snp);
				snpId++;
			}
			line = t.readLine();
		}

		SNPs = new String[snpId];
		for (i = 0; i < tmpSNP.size(); i++)
		{
			SNPs[i] = tmpSNP.get(i);
		}

		t.close();

		System.out.println(SNPs.length + " snps loaded");

		t = new TextFile(loc + "SNPMappings.txt", TextFile.R);

		lineElems = t.readLineElems(TextFile.tab);
		alInd = new ArrayList<String>();

		chr = new byte[SNPs.length];
		chrPos = new int[SNPs.length];

		while (lineElems != null)
		{
			if (lineElems.length > 2)
			{
				String snpStr = lineElems[2];
				Integer snpNum = snpToSNPId.get(snpStr);
				if (snpNum != null)
				{
					chr[snpNum] = ChrAnnotation.parseChr(lineElems[0]);
					chrPos[snpNum] = Integer.parseInt(lineElems[1]);
				}

			}
			lineElems = t.readLineElems(TextFile.tab);

		}
		t.close();

		// open random access file
		setGenotypeFileName(loc + "GenotypeMatrix.dat");
		setDosageFileName(loc + "ImputedDosageMatrix.dat");

	}

	/**
	 * @return the isFemale
	 */
	public Boolean[] getIsFemale()
	{
		return isFemale;
	}

	/**
	 * @param isFemale
	 *            the isFemale to set
	 */
	public void setIsFemale(Boolean[] isFemale)
	{
		this.isFemale = isFemale;
	}

	/**
	 * @return the isCase
	 */
	public Boolean[] getIsCase()
	{
		return isCase;
	}

	/**
	 * @param isCase
	 *            the isCase to set
	 */
	public void setIsCase(Boolean[] isCase)
	{
		this.isCase = isCase;
	}

	/**
	 * @return the isIncluded
	 */
	public Boolean[] getIsIncluded()
	{
		return isIncluded;
	}

	/**
	 * @param isIncluded
	 *            the isIncluded to set
	 */
	public void setIsIncluded(Boolean[] isIncluded)
	{
		this.isIncluded = isIncluded;
	}

	/**
	 * @return the individualToId
	 */
	public HashMap<String, Integer> getIndividualToId()
	{
		return individualToId;
	}

	/**
	 * @param individualToId
	 *            the individualToId to set
	 */
	public void setIndividualToId(HashMap<String, Integer> individualToId)
	{
		this.individualToId = individualToId;
	}

	/**
	 * @return the snpToSNPObject
	 */
	public HashMap<String, Integer> getSnpToSNPId()
	{
		return snpToSNPId;
	}

	/**
	 * @param snpToSNPObject
	 *            the snpToSNPObject to set
	 */
	public void setSnpToSNPObject(HashMap<String, Integer> snpToSNPObject)
	{
		this.snpToSNPId = snpToSNPObject;
	}

	/**
	 * @return the genotypeFileName
	 */
	public String getGenotypeFileName()
	{
		return genotypeFileName;
	}

	/**
	 * @param genotypeFileName
	 *            the genotypeFileName to set
	 */
	public void setGenotypeFileName(String genotypeFileName)
	{
		this.genotypeFileName = genotypeFileName;
	}

	/**
	 * @return the dosageFileName
	 */
	public String getDosageFileName()
	{
		return dosageFileName;
	}

	/**
	 * @param dosageFileName
	 *            the dosageFileName to set
	 */
	public void setDosageFileName(String dosageFileName)
	{
		this.dosageFileName = dosageFileName;
	}

	/**
	 * @return the SNPs
	 */
	public String[] getSNPs()
	{
		return SNPs;
	}

	/**
	 * @param SNPs
	 *            the SNPs to set
	 */
	public void setSNPs(String[] SNPs)
	{
		this.SNPs = SNPs;
	}

	public String[] getIndividuals()
	{
		return individuals;
	}

	private void checkFiles(String loc) throws Exception
	{
		if (!Gpio.exists(loc))
		{
			throw new Exception("Error: Directory " + loc + " does not exist.");
		}
		else if (!Gpio.exists(loc + "PhenotypeInformation.txt"))
		{
			throw new Exception("Error: Required file " + loc + "PhenotypeInformation.txt does not exist.");
		}
		else if (!Gpio.exists(loc + "Individuals.txt"))
		{
			throw new Exception("Error: Required file " + loc + "Individuals.txt does not exist.");
		}
		else if (!Gpio.exists(loc + "SNPMappings.txt"))
		{
			throw new Exception("Error: Required file " + loc + "SNPMappings.txt does not exist.");
		}

		// SNPs.txt

		// GenotypeMatrix.dat
	}

	public Integer getIndividualId(String key)
	{
		return individualToId.get(key);
	}

	public Byte getChr(int s)
	{
		return chr[s];
	}

	public int getChrPos(int s)
	{
		return chrPos[s];
	}

	public SNP getSNPObject(int d)
	{
		SNP out = new SNP();
		out.setId(d);
		out.setChr(chr[d]);
		out.setChrPos(chrPos[d]);
		out.setName(SNPs[d]);
		return out;
	}

	public SNPLoader createSNPLoader() throws IOException
	{

		// BufferedRandomAccessFile dosageHandle = null;
		// BufferedRandomAccessFile genotypeHandle = new
		// BufferedRandomAccessFile(genotypeFileName, "r",
		// individuals.length*25);
		// if(Gpio.exists(dosageFileName)){
		// System.out.println("Dosage information is available at: "+dosageFileName);
		// dosageHandle = new BufferedRandomAccessFile(dosageFileName, "r",
		// individuals.length*25);
		// }

		RandomAccessFile dosageHandle = null;
		RandomAccessFile genotypeHandle = new RandomAccessFile(genotypeFileName, "r");
		if (Gpio.exists(dosageFileName))
		{
			dosageHandle = new RandomAccessFile(dosageFileName, "r");
		}

		SNPLoader s = new SNPLoader(genotypeHandle, dosageHandle, isIncluded, isFemale);
		s.setNumIndividuals(individuals.length);
		return s;
	}

}
