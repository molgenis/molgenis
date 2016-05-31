package org.molgenis.data.vcf.utils;

import org.molgenis.data.Entity;
import org.molgenis.data.vcf.VcfRepository;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class VcfDiff
{

	/**
	 * Input: VCF files A and B
	 * 
	 * Output:
	 * OnlyInA.vcf
	 * OnlyInB.vcf
	 * InBothContentA.vcf
	 * InBothContentB.vcf
	 * 
	 * FIXME: does not match when there are multiple alternative alleles! but for now better than nothing.
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException
	{
		File vcfA = new File(args[0]);
		File vcfB = new File(args[1]);
		File outputDir = vcfA.getParentFile();
		new VcfDiff(vcfA, vcfB, outputDir);
	}
	
	//chrom_pos_ref_alt as key
	HashMap<String, Entity> vcfARecords = new HashMap<String, Entity>();
	ArrayList<String> intersectKeys = new ArrayList<String>();
	
	public VcfDiff(File vcfA, File vcfB, File outputDir) throws IOException
	{
		readVcfA(vcfA);
		compareToVcfB(vcfB, outputDir);
		System.out.println("Done!");
	}
	
	public void compareToVcfB(File vcfB, File outputDir) throws IOException
	{
		BufferedWriter onlyInA = new BufferedWriter(new PrintWriter(new File(outputDir, "onlyInA.vcf")));
		BufferedWriter onlyInB = new BufferedWriter(new PrintWriter(new File(outputDir, "onlyInB.vcf")));
		BufferedWriter inBothContentA = new BufferedWriter(new PrintWriter(new File(outputDir, "inBothContentA.vcf")));
		BufferedWriter inBothContentB = new BufferedWriter(new PrintWriter(new File(outputDir, "inBothContentB.vcf")));
		
		VcfRepository vcfRepo = new VcfRepository(vcfB, "vcfB");
		java.util.Iterator<Entity> vcfRepoIter = vcfRepo.iterator();
		while (vcfRepoIter.hasNext())
		{
			Entity record = vcfRepoIter.next();
			String key = record.getString("#CHROM") + "_" + record.getString("POS") + "_" + record.getString("REF") + "_" + record.getString("ALT");
			if(vcfARecords.containsKey(key))
			{
				//intersect
				intersectKeys.add(key);
				VcfWriterUtils.writeToVcf(vcfARecords.get(key), inBothContentA);
				inBothContentA.write('\n');
				VcfWriterUtils.writeToVcf(record, inBothContentB);
				inBothContentB.write('\n');
			}
			else
			{
				//only in B
				VcfWriterUtils.writeToVcf(record, onlyInB);
				onlyInB.write('\n');
			}

		}
		vcfRepo.close();
		
		//only in A
		for(String key : vcfARecords.keySet())
		{
			if(!intersectKeys.contains(key))
			{
				VcfWriterUtils.writeToVcf(vcfARecords.get(key), onlyInA);
				onlyInA.write('\n');
			}
		}
		
		onlyInA.flush();
		onlyInA.close();
		onlyInB.flush();
		onlyInB.close();
		inBothContentA.flush();
		inBothContentA.close();
		inBothContentB.flush();
		inBothContentB.close();
		
	}
	
	public void readVcfA(File vcfA) throws IOException
	{
		VcfRepository vcfRepo = new VcfRepository(vcfA, "vcfA");
		java.util.Iterator<Entity> vcfRepoIter = vcfRepo.iterator();
		while (vcfRepoIter.hasNext())
		{
			Entity record = vcfRepoIter.next();
			String key = record.getString("#CHROM") + "_" + record.getString("POS") + "_" + record.getString("REF") + "_" + record.getString("ALT");
			if(vcfARecords.containsKey(key))
			{
				System.out.println("WARNING: key " + key + " is duplicate !");
			}
			vcfARecords.put(key, record);
		}
		vcfRepo.close();
	}

}
