package org.molgenis.util.plink.readers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.molgenis.util.plink.datatypes.Biallele;
import org.molgenis.util.plink.datatypes.BimEntry;
import org.molgenis.util.plink.datatypes.FamEntry;
import org.molgenis.util.plink.drivers.BedFileDriver;
import org.molgenis.util.plink.drivers.BimFileDriver;
import org.molgenis.util.plink.drivers.FamFileDriver;

/**
 * Plink binary reader/converter. See:
 * https://github.com/molgenis/molgenis/blob/standalone_tools
 * /src/plinkbintocsv/PlinkbinToCsv.java
 * 
 * @author joeri
 * 
 */
public class BedBimFamReader
{

	private BedFileDriver bedfd;
	private BimFileDriver bimfd;
	private FamFileDriver famfd;

	private long nrOfIndividuals;
	private long nrOfSnps;
	private long nrOfGenotypes;
	private int paddingPerSnp;
	private List<String> individualNames;
	private List<String> snpNames;
	private HashMap<String, Biallele> snpCoding;

	public BedBimFamReader(File bed, File bim, File fam) throws Exception
	{
		bedfd = new BedFileDriver(bed);
		bimfd = new BimFileDriver(bim);
		famfd = new FamFileDriver(fam);

		nrOfIndividuals = famfd.getNrOfElements();
		nrOfSnps = bimfd.getNrOfElements();
		nrOfGenotypes = nrOfIndividuals * nrOfSnps;
		paddingPerSnp = (int) ((bedfd.getNrOfElements() - nrOfGenotypes) / nrOfSnps);
	}

	private void setIndividuals() throws Exception
	{
		List<String> individualNames = new ArrayList<String>();
		List<FamEntry> famEntries = famfd.getAllEntries();
		if (famEntries.size() != nrOfIndividuals)
		{
			throw new Exception("Problem with FAM file: scanned number of elements (" + nrOfIndividuals
					+ ") does not match number of parsed elements (" + famEntries.size() + ")");
		}
		for (FamEntry fe : famEntries)
		{
			if (individualNames.contains(fe.getIndividual()))
			{
				throw new Exception("Problem with FAM file: Individual '" + fe.getIndividual() + "' is not unique!");
			}
			individualNames.add(fe.getIndividual());
		}
		this.individualNames = individualNames;
	}

	private void setSnps() throws Exception
	{
		nrOfSnps = bimfd.getNrOfElements();
		List<String> snpNames = new ArrayList<String>();
		List<BimEntry> bimEntries = bimfd.getAllEntries();
		if (bimEntries.size() != nrOfSnps)
		{
			throw new Exception(
					"Problem with BIM file: scanned number of elements does not match number of parsed elements");
		}
		snpCoding = new HashMap<String, Biallele>();
		for (BimEntry be : bimEntries)
		{
			if (snpCoding.containsKey(be.getSNP()))
			{
				throw new Exception("Problem with BIM file: SNP '" + be.getSNP() + "' is not unique!");
			}
			snpCoding.put(be.getSNP(), be.getBiallele());
			snpNames.add(be.getSNP());
		}
		this.snpNames = snpNames;
	}

	public void extractGenotypes(File writeTo) throws Exception
	{
		setIndividuals();
		setSnps();

		Writer genotypesOut = new OutputStreamWriter(new FileOutputStream(writeTo), "UTF-8");

		// /header: all individual names
		for (String indvName : individualNames)
		{
			genotypesOut.write("\t" + indvName);
		}
		genotypesOut.write("\n");

		// elements: snp name + genotypes
		int snpCounter = 0;
		for (int genotypeStart = 0; genotypeStart < nrOfGenotypes; genotypeStart += nrOfIndividuals)
		{
			System.out.println((int) ((genotypeStart / (double) nrOfGenotypes) * 100) + "% of genotypes done");

			String snpName = snpNames.get(snpCounter);
			String[] allIndividualsForThisSNP = bedfd.getElements(genotypeStart, genotypeStart + nrOfIndividuals,
					paddingPerSnp, snpCounter);
			String a1 = Character.toString(snpCoding.get(snpName).getAllele1());
			String a2 = Character.toString(snpCoding.get(snpName).getAllele2());
			String hom1 = a1 + a1;
			String hom2 = a2 + a2;
			String hetr = a1 + a2;

			StringBuilder lineOfGenotypesBuilder = new StringBuilder(snpName);
			for (String s : allIndividualsForThisSNP)
			{
				lineOfGenotypesBuilder.append('\t').append(bedfd.convertGenoCoding(s, hom1, hom2, hetr, ""));
			}
			lineOfGenotypesBuilder.append('\n');
			genotypesOut.write(lineOfGenotypesBuilder.toString());

			snpCounter++;
		}

		genotypesOut.close();

	}

	public static void main(String[] args) throws Exception
	{
		File bed = new File(Biallele.class.getResource("../testfiles/test.bed").getFile());

		File bim = new File(Biallele.class.getResource("../testfiles/test.bim").getFile());

		File fam = new File(Biallele.class.getResource("../testfiles/test.fam").getFile());

		BedBimFamReader bbfr = new BedBimFamReader(bed, bim, fam);

		File out = new File("geno_tmp.txt");

		System.out.println("going to write to: " + out.getAbsolutePath());

		bbfr.extractGenotypes(out);
	}
}
