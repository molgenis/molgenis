package org.molgenis.data.annotation.impl.report;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.impl.MonogenicDiseaseCandidatesServiceAnnotator;
import org.molgenis.data.annotation.impl.PhenomizerServiceAnnotator;
import org.molgenis.data.annotation.entity.impl.CaddAnnotator;
import org.molgenis.data.vcf.VcfRepository;

public class ReportFactory
{

	private Repository vcfRepo;

	public ReportFactory(Repository vcfRepo)
	{
		this.vcfRepo = vcfRepo;
	}

	public ReportFactory(File inputVcfFile) throws IOException
	{
		this.vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
	}

	public Report createReport() throws IOException
	{
		Report report = new Report(true);

		Map<String, Integer> geneRanking = new HashMap<String, Integer>();

		Map<String, ArrayList<Entity>> allCandidates = new HashMap<String, ArrayList<Entity>>();

		/**
		 * First, get all potential candidates
		 */
		for (Entity entity : vcfRepo)
		{
			String candStatus = entity.getString(MonogenicDiseaseCandidatesServiceAnnotator.MONOGENICDISEASECANDIDATE);
			if (candStatus.startsWith("INCLUDED") || candStatus.startsWith("EXCLUDED_FIRST_OF_COMPOUND"))
			{
				String geneName = entity.getString("INFO_ANN").split("\\|")[3];

				if (allCandidates.containsKey(geneName))
				{
					allCandidates.get(geneName).add(entity);
				}
				else
				{
					ArrayList<Entity> newList = new ArrayList<Entity>();
					newList.add(entity);
					allCandidates.put(geneName, newList);
				}

			}
		}

		/**
		 * Then, remove candidates that are EXCLUDED_FIRST_OF_COMPOUND but have no further INCLUDED_RECESSIVE_COMPOUND
		 * within the same gene Assumes exactly 1x EXCLUDED_FIRST_OF_COMPOUND
		 */
		ArrayList<String> removeTheseGenes = new ArrayList<String>();
		for (String gene : allCandidates.keySet())
		{

			Entity variantFor_EXCLUDED_FIRST_OF_COMPOUND = null;
			ArrayList<Entity> variantsFor_INCLUDED_RECESSIVE_COMPOUND = new ArrayList<Entity>();

			for (Entity variant : allCandidates.get(gene))
			{
				String candStatus = variant
						.getString(MonogenicDiseaseCandidatesServiceAnnotator.MONOGENICDISEASECANDIDATE);
				if (candStatus.equals(MonogenicDiseaseCandidatesServiceAnnotator.outcome.EXCLUDED_FIRST_OF_COMPOUND
						.toString())
						|| candStatus
								.equals(MonogenicDiseaseCandidatesServiceAnnotator.outcome.EXCLUDED_FIRST_OF_COMPOUND_HIGHIMPACT
										.toString()))
				{
					variantFor_EXCLUDED_FIRST_OF_COMPOUND = variant;
				}
				else if (candStatus
						.equals(MonogenicDiseaseCandidatesServiceAnnotator.outcome.INCLUDED_RECESSIVE_COMPOUND
								.toString())
						|| candStatus
								.equals(MonogenicDiseaseCandidatesServiceAnnotator.outcome.INCLUDED_RECESSIVE_COMPOUND_HIGHIMPACT
										.toString()))
				{
					variantsFor_INCLUDED_RECESSIVE_COMPOUND.add(variant);
				}
			}

			if (variantFor_EXCLUDED_FIRST_OF_COMPOUND != null && variantsFor_INCLUDED_RECESSIVE_COMPOUND.size() == 0)
			{
				allCandidates.get(gene).remove(variantFor_EXCLUDED_FIRST_OF_COMPOUND);
				// flag 'empty' genes
				if (allCandidates.get(gene).size() == 0)
				{
					removeTheseGenes.add(gene);
				}
			}

		}
		// remove 'empty' genes
		for (String gene : removeTheseGenes)
		{
			allCandidates.remove(gene);
		}

		/**
		 * For each gene, calculate 'stars'
		 */
		for (String gene : allCandidates.keySet())
		{
			int stars = 0;

			// for gene-wide use, take just 1 variant
			Entity oneVariant = allCandidates.get(gene).get(0);

			/**
			 * Phenotype match
			 */
			Double phenomizerScore = oneVariant.getDouble(PhenomizerServiceAnnotator.PHENOMIZERPVAL) != null ? oneVariant
					.getDouble(PhenomizerServiceAnnotator.PHENOMIZERPVAL) : null;
			if (phenomizerScore != null)
			{
				stars++;
				// System.out.println(phenomizerScore);
				if (phenomizerScore < 0.05)
				{
					stars += 3;
				}
			}

			/**
			 * Pathogenicity estimates
			 */
			// TODO simple model for now!!! but need to calibrate!!!

			for (Entity variant : allCandidates.get(gene))
			{
				Double caddScore = variant.getDouble(CaddAnnotator.CADD_SCALED) != null ? variant
						.getDouble(CaddAnnotator.CADD_SCALED) : null;
				if (caddScore != null)
				{
					if (caddScore > 30)
					{
						stars += 3;
					}
					else if (caddScore > 20)
					{
						stars += 2;
					}
					else if (caddScore > 10)
					{
						stars += 1;
					}
				}
			}

			/**
			 * Impact
			 */
			// TODO

			for (Entity variant : allCandidates.get(gene))
			{
				String candStatus = variant
						.getString(MonogenicDiseaseCandidatesServiceAnnotator.MONOGENICDISEASECANDIDATE);

				if (candStatus.equals(MonogenicDiseaseCandidatesServiceAnnotator.outcome.INCLUDED_DOMINANT.toString()))
				{
					stars++;
				}
				else if (candStatus.equals(MonogenicDiseaseCandidatesServiceAnnotator.outcome.INCLUDED_RECESSIVE
						.toString()))
				{
					stars++;
				}

				else if (candStatus
						.equals(MonogenicDiseaseCandidatesServiceAnnotator.outcome.INCLUDED_DOMINANT_HIGHIMPACT
								.toString()))
				{
					stars += 2;
				}
				else if (candStatus
						.equals(MonogenicDiseaseCandidatesServiceAnnotator.outcome.INCLUDED_RECESSIVE_HIGHIMPACT
								.toString()))
				{
					stars += 2;
				}

				else if (candStatus
						.equals(MonogenicDiseaseCandidatesServiceAnnotator.outcome.INCLUDED_RECESSIVE_COMPOUND
								.toString()))
				{
					stars++;
				}
				else if (candStatus
						.equals(MonogenicDiseaseCandidatesServiceAnnotator.outcome.INCLUDED_RECESSIVE_COMPOUND_HIGHIMPACT
								.toString()))
				{
					stars += 2;
				}

				// some special cases: add a little extra weight on recessive compounds of which the FIRST one was high
				// impact
				else if (candStatus
						.equals(MonogenicDiseaseCandidatesServiceAnnotator.outcome.EXCLUDED_FIRST_OF_COMPOUND_HIGHIMPACT
								.toString()))
				{
					stars++;
				}
				// and also add a star for 'OTHER' inheritance
				else if (candStatus
						.equals(MonogenicDiseaseCandidatesServiceAnnotator.outcome.INCLUDED_OTHER.toString()))
				{
					stars++;
				}

			}

			geneRanking.put(gene, stars);
		}

		ValueComparator bvc = new ValueComparator(geneRanking);
		TreeMap<String, Integer> geneRankingSorted = new TreeMap<String, Integer>(bvc);
		geneRankingSorted.putAll(geneRanking);

		LinkedHashMap<String, Integer> monogenicDiseaseRiskGeneRanking = new LinkedHashMap<String, Integer>();

		for (String key : geneRankingSorted.keySet())
		{
			monogenicDiseaseRiskGeneRanking.put(key, geneRanking.get(key));
		}

		report.setMonogenicDiseaseRiskGeneRanking(monogenicDiseaseRiskGeneRanking);
		report.setMonogenicDiseaseRiskVariants(allCandidates);

		vcfRepo.close();

		return report;
	}
}

class ValueComparator implements Comparator<String>
{

	Map<String, Integer> base;

	public ValueComparator(Map<String, Integer> base)
	{
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with equals.
	public int compare(String a, String b)
	{
		if (base.get(a) >= base.get(b))
		{
			return -1;
		}
		else
		{
			return 1;
		} // returning 0 would merge keys
	}
}