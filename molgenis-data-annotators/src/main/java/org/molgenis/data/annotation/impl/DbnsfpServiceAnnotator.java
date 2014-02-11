package org.molgenis.data.annotation.impl;

import java.util.*;
import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.RepositoryAnnotator;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

/**
 * <p>
 * This class...
 * </p>
 * 
 * <p>
 * <b>dbNSFP returns:</b>
 * 
 * chr	pos(1-coor)	ref	alt	aaref	aaalt	hg18_pos(1-coor)	genename	Uniprot_acc	Uniprot_id	Uniprot_aapos	Interpro_domain	cds_strand	refcodon	SLR_test_statistic 	codonpos	fold-degenerate	Ancestral_allele	Ensembl_geneid	Ensembl_transcriptid	aapos	aapos_SIFT	aapos_FATHMM	SIFT_score	SIFT_score_converted	SIFT_pred	Polyphen2_HDIV_score	
 * Polyphen2_HDIV_pred	Polyphen2_HVAR_score	Polyphen2_HVAR_pred	LRT_score	LRT_score_converted	LRT_pred	MutationTaster_score	MutationTaster_score_converted	MutationTaster_pred	MutationAssessor_score	MutationAssessor_score_converted	MutationAssessor_pred	FATHMM_score	FATHMM_score_converted	FATHMM_pred	RadialSVM_score	RadialSVM_score_converted	
 * RadialSVM_pred	LR_score	LR_pred	Reliability_index	GERP++_NR	GERP++_RS	phyloP	29way_pi	29way_logOdds	LRT_Omega	UniSNP_ids	1000Gp1_AC	1000Gp1_AF	1000Gp1_AFR_AC	1000Gp1_AFR_AF	1000Gp1_EUR_AC	1000Gp1_EUR_AF	1000Gp1_AMR_AC	1000Gp1_AMR_AF	1000Gp1_ASN_AC	1000Gp1_ASN_AF	ESP6500_AA_AF	ESP6500_EA_AF
 * </p>
 * 
 * @author mdehaan
 * 
 * */
public class DbnsfpServiceAnnotator implements RepositoryAnnotator
{
	// the dbnsfp service is dependant on these four values,
	// without them no annotations can be returned
	private static final String CHROMOSOME = "chrom";
	private static final String POSITION = "pos";
	private static final String REFERENCE = "ref";
	private static final String ALTERNATIVE = "alt";
	
	// the prefix for chromosome files
	private static final String CHROMOSOME_FILE = "/Users/bin/tools/dbfnsp/dbNSFP2.3_variant.chr";
	
	/**
	 * <p>
	 * This method...
	 * </p>
	 * 
	 * @param Iterator<Entity>
	 * @return Iterator<Entity>
	 * 
	 * */
	@Override
	public Iterator<Entity> annotate(Iterator<Entity> source)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * <p>
	 * This method returns the output that this annotation tool will produce
	 * </p>
	 * 
	 * */
	@Override
	public EntityMetaData getOutputMetaData()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * <p>
	 * This method returns the attributes needed to use this annotation tool.
	 * </p>
	 * 
	 * */
	@Override
	public EntityMetaData getInputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName());

		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CHROMOSOME, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(POSITION, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(REFERENCE, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(ALTERNATIVE, FieldTypeEnum.STRING));
		
		return metadata;
		
	}

	@Override
	public Boolean canAnnotate()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName()
	{
		return "dbNSFP";
	}

	public static void main(String[] args) throws Exception
	{
		String infile = "";
		String outfile = "";
		boolean hg18 = false;
		boolean allChr = true;
		boolean searchAllChr = false;
		boolean hasGene = false;
		boolean hasEnsemble = false;
		boolean hasUniprot = false;
		boolean hasEnspSnp = false;
		boolean hasSnp = false;
		boolean hasEntrez = false;
		boolean hasUniprotSnp = false;
		boolean allColumns = true;
		boolean isVcf = false;
		int[] columns = new int[0];
		boolean preserveVcf = false;
		String[] chromosomes =
		{ "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
				"20", "21", "22", "X", "Y" };
		
		
		for (int i = 0; i < args.length; i += 2)
		{
			if (args[i].equalsIgnoreCase("-c"))
			{
				StringTokenizer t = new StringTokenizer(args[i + 1], ",");
				int nt = t.countTokens();
				allChr = false;
				chromosomes = new String[nt];
				for (int j = 0; j < nt; j++)
					chromosomes[j] = t.nextToken();
			}
			if (args[i].equalsIgnoreCase("-i")) infile = args[i + 1];
			if (args[i].equalsIgnoreCase("-o")) outfile = args[i + 1];
			if (args[i].equalsIgnoreCase("-w"))
			{
				StringTokenizer t = new StringTokenizer(args[i + 1], ",");
				int nt = t.countTokens();
				allColumns = false;
				int nc = 0;
				for (int j = 0; j < nt; j++)
				{
					String tmp = t.nextToken();
					StringTokenizer t2 = new StringTokenizer(tmp, "-");
					if (t2.countTokens() == 1) nc++;
					else
					{
						int begin = Integer.parseInt(t2.nextToken());
						int end = Integer.parseInt(t2.nextToken());
						nc += end - begin + 1;
					}
				}
				columns = new int[nc];
				t = new StringTokenizer(args[i + 1], ",");
				nt = t.countTokens();
				nc = 0;
				for (int j = 0; j < nt; j++)
				{
					String tmp = t.nextToken();
					StringTokenizer t2 = new StringTokenizer(tmp, "-");
					if (t2.countTokens() == 1)
					{
						columns[nc] = Integer.parseInt(t2.nextToken());
						nc++;
					}
					else
					{
						int begin = Integer.parseInt(t2.nextToken());
						int end = Integer.parseInt(t2.nextToken());
						for (int k = begin; k <= end; k++)
						{
							columns[nc] = k;
							nc++;
						}
					}
				}
			}
			if (args[i].equalsIgnoreCase("-p")) preserveVcf = true;
		}
	
		PrintWriter out = new PrintWriter(new FileWriter(outfile));
		PrintWriter out2 = new PrintWriter(new FileWriter(outfile + ".err"));

		// read input file
		HashMap snps = new HashMap();
		HashMap chrsx = new HashMap();
		HashMap genes = new HashMap();
		HashMap poss = new HashMap();
		HashMap genequery = new HashMap();
		HashMap uniprotsnp = new HashMap();
		HashMap enspsnp = new HashMap();
		BufferedReader in = new BufferedReader(new FileReader(infile));
		if (infile.substring(infile.length() - 4).equalsIgnoreCase(".vcf")) isVcf = true;
		String title = "";
		while (in.ready())
		{
			String line = in.readLine();
			if (line.isEmpty()) continue;
			if (line.charAt(0) == '#')
			{
				title = line;
				continue;
			}
			StringTokenizer t = new StringTokenizer(line);
			int nt = t.countTokens();
			if (isVcf)
			{
				t = new StringTokenizer(line, "\t");
				String chr = t.nextToken().toUpperCase();
				if (chr.length() > 3 && chr.substring(0, 3).equalsIgnoreCase("CHR")) chr = chr.substring(3);
				String pos = t.nextToken();
				String id = t.nextToken();
				String ref = t.nextToken().toUpperCase();
				String alt = t.nextToken().toUpperCase();
				if (preserveVcf) snps.put(chr + "_" + pos + "_" + ref + "_" + alt, compress(line));
				else snps.put(chr + "_" + pos + "_" + ref + "_" + alt, " ");
				if (allChr) chrsx.put(chr, "");
				hasSnp = true;
			}
			else if (nt == 6)
			{
				String chr = t.nextToken().toUpperCase();
				String pos = t.nextToken();
				String ref = t.nextToken().toUpperCase();
				String alt = t.nextToken().toUpperCase();
				String aaref = t.nextToken().toUpperCase();
				String aaalt = t.nextToken().toUpperCase();
				snps.put(chr + "_" + pos + "_" + ref + "_" + alt + "_" + aaref + "_" + aaalt, " ");
				if (allChr) chrsx.put(chr, "");
				hasSnp = true;
			}
			else if (nt == 4)
			{
				String chr = t.nextToken().toUpperCase();
				String pos = t.nextToken();
				String ref = t.nextToken().toUpperCase();
				String alt = t.nextToken().toUpperCase();
				snps.put(chr + "_" + pos + "_" + ref + "_" + alt, " ");
				if (allChr) chrsx.put(chr, "");
				hasSnp = true;
			}
			else if (nt == 1)
			{
				String tmp = t.nextToken().toUpperCase();
				if (tmp.length() > 7 && tmp.substring(0, 7).equalsIgnoreCase("Uniprot"))
				{
					StringTokenizer t2 = new StringTokenizer(tmp, ":");
					int nt2 = t2.countTokens();
					if (nt2 == 2)
					{
						genequery.put(tmp, tmp);
						hasGene = true;
					}
					else if (nt2 == 3)
					{
						snps.put(tmp, " ");
						StringTokenizer t3 = new StringTokenizer(tmp, ":");
						t3.nextToken();
						uniprotsnp.put(t3.nextToken(), "");
						hasSnp = true;
						hasUniprotSnp = true;
					}
					hasUniprot = true;
				}
				else if (tmp.length() > 7 && tmp.substring(0, 7).equalsIgnoreCase("Ensembl"))
				{
					StringTokenizer t2 = new StringTokenizer(tmp, ":");
					int nt2 = t2.countTokens();
					if (nt2 == 2)
					{
						genequery.put(tmp, tmp);
						hasGene = true;
						hasEnsemble = true;
					}
					else if (nt2 == 3)
					{
						snps.put(tmp, " ");
						StringTokenizer t3 = new StringTokenizer(tmp, ":");
						t3.nextToken();
						enspsnp.put(t3.nextToken(), "");
						hasSnp = true;
						hasEnspSnp = true;
					}

				}
				else if (tmp.length() > 6 && tmp.substring(0, 6).equalsIgnoreCase("Entrez"))
				{
					hasEntrez = true;
					genequery.put(tmp, tmp);
					hasGene = true;
				}
				else
				{
					genequery.put(tmp, tmp);
					hasGene = true;
				}
			}
			else if (nt == 2)
			{
				String chr = t.nextToken().toUpperCase();
				String pos = t.nextToken();
				poss.put(chr + "_" + pos, "");
				if (allChr) chrsx.put(chr, "");
				hasSnp = true;
			}
		}
		in.close();

		// search dbNSFP_gene
		int ngenecol = 0;
		HashMap dbNSFPgene = new HashMap(33000);
		String genetitle = "";
		if (true)
		{
			String infile2 = "dbNSFP2.3_gene";
			in = new BufferedReader(new FileReader(infile2));
			String line = in.readLine();
			int indx = line.indexOf("chr");
			genetitle = line.substring(indx + 4);
			while (in.ready())
			{
				line = in.readLine();
				StringTokenizer t = new StringTokenizer(line, "\t");
				int nt = t.countTokens();
				ngenecol = nt;
				String[] work = new String[nt];
				for (int i = 0; i < nt; i++)
					work[i] = t.nextToken();
				String genename = work[0].toUpperCase();
				String chr = work[2];
				if (hasGene)
				{
					if (genequery.containsKey(genename))
					{
						if (allChr) chrsx.put(chr, "");
						genes.put(genename, genename);
						genequery.remove(genename);
					}

				}
				if (hasEnsemble)
				{
					t = new StringTokenizer(work[1], ";");
					nt = t.countTokens();
					for (int i = 0; i < nt; i++)
					{
						String ensg = t.nextToken().toUpperCase();
						if (genequery.containsKey("ENSEMBL:" + ensg))
						{
							if (allChr) chrsx.put(chr, "");
							genes.put("ENSEMBL:" + ensg, "ENSEMBL:" + ensg);
							genequery.remove("ENSEMBL:" + ensg);
						}
					}
				}
				if (hasUniprot)
				{
					t = new StringTokenizer(work[5], ";");
					nt = t.countTokens();
					for (int i = 0; i < nt; i++)
					{
						String tmp = t.nextToken().toUpperCase();
						if (genequery.containsKey("UNIPROT:" + tmp))
						{
							if (allChr) chrsx.put(chr, "");
							genes.put("UNIPROT:" + tmp, "UNIPROT:" + tmp);
							genequery.remove("UNIPROT:" + tmp);
						}
						if (hasUniprotSnp)
						{
							if (uniprotsnp.containsKey(tmp))
							{
								if (allChr) chrsx.put(chr, "");
								uniprotsnp.remove(tmp);
							}
						}
					}
					t = new StringTokenizer(work[6], ";");
					nt = t.countTokens();
					for (int i = 0; i < nt; i++)
					{
						String tmp = t.nextToken().toUpperCase();
						if (genequery.containsKey("UNIPROT:" + tmp))
						{
							if (allChr) chrsx.put(chr, "");
							genes.put("UNIPROT:" + tmp, "UNIPROT:" + tmp);
							genequery.remove("UNIPROT:" + tmp);
						}
						if (hasUniprotSnp)
						{
							if (uniprotsnp.containsKey(tmp))
							{
								if (allChr) chrsx.put(chr, "");
								uniprotsnp.remove(tmp);
							}
						}
					}
				}
				if (hasEntrez)
				{
					t = new StringTokenizer(work[7], ";");
					nt = t.countTokens();
					for (int i = 0; i < nt; i++)
					{
						String tmp = t.nextToken().toUpperCase();
						if (genequery.containsKey("ENTREZ:" + tmp))
						{
							if (allChr) chrsx.put(chr, "");
							genes.put(genename, "ENTREZ:" + tmp);
							genequery.remove("ENTREZ:" + tmp);
						}
					}
				}
				StringBuilder sb2 = new StringBuilder(work[3]);
				for (int i = 4; i < work.length; i++)
					sb2.append("\t" + work[i]);
				String tmp = sb2.toString();
				dbNSFPgene.put(genename, compress(tmp));

			}
			in.close();
			if (genequery.size() > 0)
			{
				genes.putAll(genequery);
				searchAllChr = true;
			}
			if (uniprotsnp.size() > 0) searchAllChr = true;
			if (enspsnp.size() > 0) searchAllChr = true;
		}

		int nchr = chromosomes.length;
		if (allChr && !searchAllChr)
		{
			nchr = chrsx.size();
			chromosomes = (String[]) chrsx.keySet().toArray(new String[nchr]);
		}
		Arrays.sort(chromosomes);

		// begin search

		for (int ii = 0; ii < nchr; ii++)
		{
			String chr = chromosomes[ii];
			System.out.println("Searching chr" + chr);
			String infile2 = "dbNSFP2.3_variant.chr" + chr;
			File f = new File(infile2);
			if (!f.isFile()) continue;
			in = new BufferedReader(new FileReader(infile2));
			// int count=0;
			if (ii == 0)
			{
				if (isVcf && preserveVcf) out.print(title + "\t");
				if (allColumns) out.println(in.readLine() + "\t" + genetitle);
				else
				{
					String line = in.readLine() + "\t" + genetitle;
					StringTokenizer t = new StringTokenizer(line, "\t");
					int nt = t.countTokens();
					String[] tmp = new String[nt];
					for (int i = 0; i < nt; i++)
						tmp[i] = t.nextToken();
					out.print(tmp[columns[0] - 1]);
					for (int i = 1; i < columns.length; i++)
					{
						out.print("\t" + tmp[columns[i] - 1]);
					}
					out.println();
				}
			}
			else in.readLine();
			boolean ready = true;
			while (ready)
			{
				String line = "";
				try
				{
					line = in.readLine();
				}
				catch (java.io.IOException e)
				{
					// System.out.println(e);
				}
				if (line == null)
				{

					ready = false;
					line = "";
					continue;
				}
				// count++;

				// TODO EXTRACT VALUES FOR SPECIFIC VARIANT HERE
				StringTokenizer t = new StringTokenizer(line, "\t");
				String chr0 = t.nextToken().toUpperCase();
				String pos = t.nextToken();
				String ref = t.nextToken();
				String alt = t.nextToken();
				String aaref = t.nextToken();
				String aaalt = t.nextToken();
				String hg18pos = t.nextToken();
				String genename = "";
				String uniprotacc = "";
				String uniprotid = "";
				String uniprotaapos = "";
				String ensg = "";
				String enst = "";
				String enspaapos1 = "";
				String enspaapos2 = "";
				String coor = chr0 + "_" + pos;
				if (hg18) coor = chr0 + "_" + hg18pos;
				genename = t.nextToken().toUpperCase();
				if (hasUniprot || hasEnsemble)
				{

					uniprotacc = t.nextToken().toUpperCase();
					uniprotid = t.nextToken().toUpperCase();
					uniprotaapos = t.nextToken();
				}
				if (hasEnsemble)
				{
					for (int i = 0; i < 7; i++)
						t.nextToken();
					ensg = t.nextToken().toUpperCase();
					enst = t.nextToken().toUpperCase();
				}
				if (hasEnspSnp)
				{
					t.nextToken();
					enspaapos1 = t.nextToken();
					enspaapos2 = t.nextToken();
				}
				boolean found = false;
				if (hasGene)
				{
					ArrayList names = new ArrayList();
					StringTokenizer t3 = new StringTokenizer(genename, "; ");
					int nt3 = t3.countTokens();
					for (int i = 0; i < nt3; i++)
						names.add(t3.nextToken());
					if (hasUniprot)
					{
						StringTokenizer t2 = new StringTokenizer(uniprotid, ";");
						int nt2 = t2.countTokens();
						for (int i = 0; i < nt2; i++)
						{
							uniprotid = t2.nextToken();
							names.add("UNIPROT:" + uniprotid);
						}
						t2 = new StringTokenizer(uniprotacc, ";");
						nt2 = t2.countTokens();
						for (int i = 0; i < nt2; i++)
						{
							uniprotacc = t2.nextToken();
							names.add("UNIPROT:" + uniprotacc);
						}

					}
					if (hasEnsemble)
					{
						StringTokenizer t2 = new StringTokenizer(ensg, ";");
						int nc1 = t2.countTokens();
						for (int i = 0; i < nc1; i++)
							names.add("ENSEMBL:" + t2.nextToken());
						t2 = new StringTokenizer(enst, ";");
						nc1 = t2.countTokens();
						for (int i = 0; i < nc1; i++)
							names.add("ENSEMBL:" + t2.nextToken());
					}
					for (int i = 0; i < names.size(); i++)
					{
						String tmp = (String) names.get(i);
						if (genes.containsKey(tmp))
						{

							genes.put(tmp, "");
							found = true;
						}
					}

				}
				String extra = "";
				if (hasSnp)
				{
					String snp = coor + "_" + ref + "_" + alt;
					String snp2 = coor + "_" + ref + "_" + alt + "_" + aaref + "_" + aaalt;

					if (snps.containsKey(snp))
					{
						found = true;
						extra = (String) snps.get(snp);
						snps.put(snp, "");
					}
					if (snps.containsKey(snp2))
					{
						found = true;
						extra = (String) snps.get(snp2);
						snps.put(snp2, "");
					}
					if (poss.containsKey(coor))
					{
						found = true;
						poss.put(coor, " ");
					}
					if (hasUniprotSnp)
					{
						StringTokenizer t2 = new StringTokenizer(uniprotid, ";");
						StringTokenizer t3 = new StringTokenizer(uniprotacc, ";");
						StringTokenizer t4 = new StringTokenizer(uniprotaapos, ";");
						int nt2 = t2.countTokens();
						for (int i = 0; i < nt2; i++)
						{
							uniprotid = t2.nextToken();
							uniprotacc = t3.nextToken();
							uniprotaapos = t4.nextToken();
							String uniprot3 = "UNIPROT:" + uniprotid + ":" + aaref + uniprotaapos + aaalt;
							if (snps.containsKey(uniprot3))
							{
								found = true;
								extra = (String) snps.get(snp);
								snps.put(uniprot3, "");
							}
							String uniprot4 = "UNIPROT:" + uniprotacc + ":" + aaref + uniprotaapos + aaalt;
							if (snps.containsKey(uniprot4))
							{
								found = true;
								extra = (String) snps.get(snp);
								snps.put(uniprot4, "");
							}
						}
					}
					if (hasEnspSnp)
					{
						StringTokenizer t2 = new StringTokenizer(enspaapos1, ";");
						StringTokenizer t3 = new StringTokenizer(enspaapos2, ";");
						int nt2 = t2.countTokens();
						for (int i = 0; i < nt2; i++)
						{
							String key = "ENSEMBL:" + t2.nextToken();
							if (snps.containsKey(key))
							{
								found = true;
								extra = (String) snps.get(snp);
								snps.put(key, "");
							}

						}
						if (!found)
						{
							int nt3 = t3.countTokens();
							for (int i = 0; i < nt3; i++)
							{
								String key = "ENSEMBL:" + t3.nextToken();
								if (snps.containsKey(key))
								{
									found = true;
									extra = (String) snps.get(snp);
									snps.put(key, "");
								}

							}
						}

					}
				}

				if (found)
				{
					if (isVcf && preserveVcf) out.print(decompress(extra) + "\t");
					if (dbNSFPgene.containsKey(genename))
					{
						String tmp = (String) dbNSFPgene.get(genename);
						tmp = decompress(tmp);
						if (allColumns) out.println(line + "\t" + tmp);
						else
						{
							String linex = line + "\t" + tmp;
							StringTokenizer tx = new StringTokenizer(linex, "\t");
							int nt = tx.countTokens();
							String[] tmpx = new String[nt];
							for (int i = 0; i < nt; i++)
								tmpx[i] = tx.nextToken();
							out.print(tmpx[columns[0] - 1]);
							for (int i = 1; i < columns.length; i++)
							{
								out.print("\t" + tmpx[columns[i] - 1]);
							}
							out.println();
						}
					}
					else
					{
						String tmp = ".";
						for (int i = 1; i < ngenecol - 3; i++)
							tmp = tmp + "\t.";
						if (allColumns) out.println(line + "\t" + tmp);
						else
						{
							String linex = line + "\t" + tmp;
							StringTokenizer tx = new StringTokenizer(linex, "\t");
							int nt = tx.countTokens();
							String[] tmpx = new String[nt];
							for (int i = 0; i < nt; i++)
								tmpx[i] = tx.nextToken();
							out.print(tmpx[columns[0] - 1]);
							for (int i = 1; i < columns.length; i++)
							{
								out.print("\t" + tmpx[columns[i] - 1]);
							}
							out.println();
						}
					}
				}
			}
			in.close();
		}
		out.close();

		// report unfound queries
		System.out.println();

		int nquery = snps.size();
		String[] keys = (String[]) snps.keySet().toArray(new String[nquery]);
		int found = 0;
		int unfound = 0;
		for (int i = 0; i < nquery; i++)
		{
			String tmp = (String) snps.get(keys[i]);
			if (!tmp.isEmpty())
			{
				String tmp2 = keys[i].replace("_", "\t");
				out2.println(tmp2);
				unfound++;
			}
			else found++;
		}
		if (found > 0) System.out.println(found + " SNP(s) are found. Written to " + outfile);
		if (unfound > 0)
		{
			System.out.println(unfound + " SNP(s) are not found. Written to " + outfile + ".err");
			out2.println("Total " + unfound + " SNP(s) are not found.");
		}
		found = 0;
		unfound = 0;

		nquery = poss.size();
		keys = (String[]) poss.keySet().toArray(new String[nquery]);
		for (int i = 0; i < nquery; i++)
		{
			String tmp = (String) poss.get(keys[i]);
			if (tmp.isEmpty())
			{
				String tmp2 = keys[i].replace("_", "\t");
				out2.println(tmp2);
				unfound++;
			}
			else found++;
		}
		if (found > 0) System.out.println(found + " position(s) are found. Written to " + outfile);
		if (unfound > 0)
		{
			System.out.println(unfound + " position(s) are not found. Written to " + outfile + ".err");
			out2.println("Total " + unfound + " position(s) are not found.");
		}
		found = 0;
		unfound = 0;

		nquery = genes.size();
		keys = (String[]) genes.keySet().toArray(new String[nquery]);
		for (int i = 0; i < nquery; i++)
		{
			String tmp = (String) genes.get(keys[i]);
			if (!tmp.isEmpty())
			{
				out2.println(tmp);
				unfound++;
			}
			else found++;
		}
		if (found > 0) System.out.println(found + " gene(s) are found. Written to " + outfile);
		if (unfound > 0)
		{
			System.out.println(unfound + " gene(s) are not found. Written to " + outfile + ".err");
			out2.println("Total " + unfound + " gene(s) are not found.");
		}
		out2.close();

	}

	private static boolean isInt(String s)
	{
		try
		{
			Integer.parseInt(s);
		}
		catch (NumberFormatException nfe)
		{
			return false;
		}
		return true;
	}

	private static String compress(String str) throws IOException
	{
		if (str == null || str.length() == 0)
		{
			return str;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		gzip.write(str.getBytes());
		gzip.close();
		String outStr = out.toString("ISO-8859-1");
		return outStr;
	}

	private static String decompress(String str) throws IOException
	{
		if (str == null || str.length() == 0)
		{
			return str;
		}
		GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(str.getBytes("ISO-8859-1")));
		BufferedReader bf = new BufferedReader(new InputStreamReader(gis, "ISO-8859-1"));
		String outStr = "";
		String line;
		while ((line = bf.readLine()) != null)
		{
			outStr += line;
		}
		return outStr;
	}

	
	
}
