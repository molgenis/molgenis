package org.molgenis.data.annotation.impl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.VariantAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Status;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Type;
import org.molgenis.data.annotation.entity.impl.ExacAnnotator;
import org.molgenis.data.annotation.entity.impl.SnpEffAnnotator;
import org.molgenis.data.annotation.utils.AnnotatorUtils;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.datastructures.Sample;
import org.molgenis.data.vcf.datastructures.Trio;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * De novo variant annotator Uses trio data (child-mother-father), read from VCF pedigree data See:
 * http://samtools.github.io/hts-specs/VCFv4.2.pdf
 **/
@Component("deNovoService")
public class DeNovoAnnotator extends VariantAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(DeNovoAnnotator.class);

	private HashMap<String, Trio> pedigree;

	// helper lists for ease of use, derived from HashMap<String, Parents> pedigree
	private HashMap<String, Sample> motherToChild;
	private HashMap<String, Sample> fatherToChild;

	// need: matrix of gene x trio, with nr of denovo calls in each cell
	// <geneSymbol - <trioChildID - count>>
	LinkedHashMap<String, LinkedHashMap<String, Integer>> geneTrioCounts = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();

	public static final String DENOVO_LABEL = "DENOVO";
	public static final String DENOVO = VcfRepository.getInfoPrefix() + DENOVO_LABEL;

	private static final String NAME = "DENOVO";

	final List<String> infoFields = Arrays.asList(new String[]
	{ "##INFO=<ID=" + DENOVO.substring(VcfRepository.getInfoPrefix().length())
			+ ",Number=1,Type=String,Description=\"todo\">" });

	public DeNovoAnnotator()
	{
	};

	public DeNovoAnnotator(File deNovoFileLocation, File inputVcfFile, File outputVCFFile) throws Exception
	{
		// cast to a real logger to adjust to the log level
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LOG;
		root.setLevel(ch.qos.logback.classic.Level.ERROR);

		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");

		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
		Iterator<Entity> vcfIter = vcfRepo.iterator();

		VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter, getOutputMetaData(),
				DENOVO.substring(VcfRepository.getInfoPrefix().length()));

		pedigree = VcfUtils.getPedigree(inputVcfFile);
		motherToChild = new HashMap<String, Sample>();
		fatherToChild = new HashMap<String, Sample>();
		for (String key : pedigree.keySet())
		{
			Sample mom = pedigree.get(key).getMother();
			Sample dad = pedigree.get(key).getFather();
			motherToChild.put(mom.getId(), pedigree.get(key).getChild());
			fatherToChild.put(dad.getId(), pedigree.get(key).getChild());
		}

		for (String key : pedigree.keySet())
		{
			System.out.println(key + ", " + pedigree.get(key));
		}

		LOG.info("Now starting to process the data.");

		while (vcfIter.hasNext())
		{
			Entity record = vcfIter.next();

			List<Entity> annotatedRecord = annotateEntity(record);

			if (annotatedRecord.size() > 1)
			{
				outputVCFWriter.close();
				vcfRepo.close();
				throw new Exception("Multiple outputs for " + record.toString());
			}
			else if (annotatedRecord.size() == 0)
			{
				outputVCFWriter.println(VcfUtils.convertToVCF(record));
			}
			else
			{
				outputVCFWriter.println(VcfUtils.convertToVCF(annotatedRecord.get(0)));
			}
		}

		for (String child : geneTrioCounts.entrySet().iterator().next().getValue().keySet())
		{
			System.out.print("\t" + child);
		}
		System.out.print("\n");
		for (String gene : geneTrioCounts.keySet())
		{
			// System.out.println("GENE " + gene);
			StringBuilder line = new StringBuilder();
			line.append(gene);
			line.append("\t");
			for (String child : geneTrioCounts.get(gene).keySet())
			{
				// System.out.println("CHILD " + child);
				line.append(geneTrioCounts.get(gene).get(child));
				line.append("\t");
			}
			line.deleteCharAt(line.length() - 1);
			System.out.println(line.toString());
		}

		outputVCFWriter.close();
		vcfRepo.close();
		LOG.info("All done!");
	}

	@Override
	public String getSimpleName()
	{
		return NAME;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	{
		Map<String, Object> resultMap = annotateEntityWithDeNovo(entity);
		return Collections.singletonList(AnnotatorUtils.getAnnotatedEntity(this, entity, resultMap));
	}

	private synchronized Map<String, Object> annotateEntityWithDeNovo(Entity entity) throws IOException
	{
		Map<String, Object> resultMap = new HashMap<>();

		String chrom = entity.get(VcfRepository.CHROM).toString();
		if (chrom.equals("X") || chrom.equals("Y"))
		{
			LOG.info("Skipping allosomal variant: " + entity);
			resultMap.put(DENOVO, 0);
			return resultMap;
		}

		// only look at variants that PASS the filter
		String filter = entity.get(VcfRepository.FILTER).toString();
		if (!filter.equals("PASS"))
		{
			LOG.info("Skipping low quality variant: " + entity);
			resultMap.put(DENOVO, 0);
			return resultMap;
		}

		Double ABHet = entity.get("ABHet") != null ? Double.parseDouble(entity.get("ABHet").toString()) : 0;
		// only keep variants with overall high quality
		if (Double.parseDouble(entity.get(VcfRepository.QUAL).toString()) < 30)
		{
			LOG.info("Skipping low qual (<30) variant: " + entity);
			resultMap.put(DENOVO, 0);
		}
		else if (ABHet < 0.3 || ABHet > 0.5)
		{
			LOG.info("Skipping bad het AB variant: " + entity);
			resultMap.put(DENOVO, 0);
		}
		else if (Double.parseDouble(entity.get("ABHom").toString()) < 0.5) // only keep variants with overall high
																			// quality
		{
			LOG.info("Skipping bad hom AB variant: " + entity);
			resultMap.put(DENOVO, 0);
		}
		else if (Double.parseDouble(entity.get("SB").toString()) > 0.5)// Strand bias
		{
			LOG.info("Skipping bad SB variant: " + entity);
			resultMap.put(DENOVO, 0);
		}
		else
		{
			/**
			 * FIXME: enable or remove -> disable for now, but useful in some analyses.. if
			 * (alleleFrequencyFilter(entity, resultMap)) return resultMap;
			 */
			String geneSymbol = SnpEffAnnotator.getGeneNameFromEntity(entity);
			Iterable<Entity> samples = entity.getEntities("Samples");

			HashMap<String, Trio> childToParentGenotypes = processSamples(samples);
			HashMap<String, Trio> completeTrios = getCompleteTrios(childToParentGenotypes);

			int totalDenovoForVariant = 0;

			for (String child : completeTrios.keySet())
			{

				int denovoForTrio = findDeNovoVariants(completeTrios.get(child));
				totalDenovoForVariant += denovoForTrio;

				if (geneSymbol != null)
				{
					// matrix of gene x trio x denovo counts
					if (geneTrioCounts.containsKey(geneSymbol))
					{
						if (geneTrioCounts.get(geneSymbol).containsKey(child))
						{
							geneTrioCounts.get(geneSymbol).put(child,
									geneTrioCounts.get(geneSymbol).get(child) + denovoForTrio);
						}
						else
						{
							geneTrioCounts.get(geneSymbol).put(child, denovoForTrio);
						}
					}
					else
					{
						LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();
						counts.put(child, denovoForTrio);
						geneTrioCounts.put(geneSymbol, counts);
					}
				}

			}

			// also add total
			if (geneTrioCounts.get(geneSymbol).containsKey("TOTAL"))
			{
				geneTrioCounts.get(geneSymbol).put("TOTAL",
						geneTrioCounts.get(geneSymbol).get("TOTAL") + totalDenovoForVariant);
			}
			else
			{
				geneTrioCounts.get(geneSymbol).put("TOTAL", totalDenovoForVariant);
			}

			resultMap.put(DENOVO, totalDenovoForVariant);
		}
		return resultMap;
	}

	private HashMap<String, Trio> processSamples(Iterable<Entity> samples) throws IOException
	{
		HashMap<String, Trio> childToParentGenotypes = new HashMap<>();
		for (Entity sample : samples)
		{
			String sampleID = sample.get("NAME").toString()
					.substring(sample.get("NAME").toString().lastIndexOf("_") + 1);
			// found a child
			if (pedigree.keySet().contains(sampleID))
			{
				// child not previously added by finding a parent first, add here plus empty trio object
				if (!childToParentGenotypes.containsKey(sampleID))
				{
					Trio t = new Trio();
					t.setChild(new Sample(sampleID, sample));
					childToParentGenotypes.put(sampleID, t);
				}
				// child may have been added because the parent was found first, in that case there is only a key,
				// so make child object + genotype!
				else if (childToParentGenotypes.containsKey(sampleID) && sample != null)
				{
					// System.out.println("CHILD - UPDATING GENOTYPE");
					if (childToParentGenotypes.get(sampleID).getChild() != null)
					{
						throw new IOException("Child genotype for '" + sampleID + "' already known !");
					}
					Sample child = new Sample(sampleID, sample);
					childToParentGenotypes.get(sampleID).setChild(child);
				}
			}

			// found a mother
			else if (motherToChild.containsKey(sampleID))
			{
				// child not seen yet, look it up and add it here, but without genotype (!)
				if (!childToParentGenotypes.containsKey(motherToChild.get(sampleID).getId()))
				{
					Trio t = new Trio();
					t.setMother(new Sample(sampleID, sample));
					childToParentGenotypes.put(motherToChild.get(sampleID).getId(), t);
				}
				// child seen, check if mother was already known, we do not expect/want this to happen
				else
				{
					if (childToParentGenotypes.get(motherToChild.get(sampleID).getId()).getMother() != null)
					{
						throw new IOException("Mother '" + sampleID + "' already known for child '"
								+ motherToChild.get(sampleID) + "' !");
					}
					childToParentGenotypes.get(motherToChild.get(sampleID).getId()).setMother(
							new Sample(sampleID, sample));
				}
			}
			else if (fatherToChild.containsKey(sampleID))
			{
				// child not seen yet, look it up and add it here, but without genotype (!)
				if (!childToParentGenotypes.containsKey(fatherToChild.get(sampleID).getId()))
				{
					Trio t = new Trio();
					t.setFather(new Sample(sampleID, sample));
					childToParentGenotypes.put(fatherToChild.get(sampleID).getId(), t);
				}
				// child seen, check if father was already known, we do not expect/want this to happen
				else
				{
					if (childToParentGenotypes.get(fatherToChild.get(sampleID).getId()).getFather() != null)
					{
						throw new IOException("Father '" + sampleID + "' already known for child '"
								+ fatherToChild.get(sampleID) + "' !");
					}
					childToParentGenotypes.get(fatherToChild.get(sampleID).getId()).setFather(
							new Sample(sampleID, sample));
				}
			}
			else
			{
				LOG.warn("Sample ID '" + sampleID
						+ "' not in list of children, mothers or fathers !! ignoring for further analysis !!");
			}
		}
		LOG.info("Found " + childToParentGenotypes.size() + " trios..");
		return childToParentGenotypes;
	}

	private boolean alleleFrequencyFilter(Entity entity, Map<String, Object> resultMap)
	{
		double thousandGenomesMAF = entity.getDouble(ThousandGenomesServiceAnnotator.THGEN_MAF) != null ? entity
				.getDouble(ThousandGenomesServiceAnnotator.THGEN_MAF) : 0;
		double exacMAF = entity.getDouble(ExacAnnotator.EXAC_AF) != null ? entity.getDouble(ExacAnnotator.EXAC_AF) : 0;
		double gonlMAF = entity.getDouble(GoNLServiceAnnotator.GONL_MAF) != null ? entity
				.getDouble(GoNLServiceAnnotator.GONL_MAF) : 0;
		if (thousandGenomesMAF > 0.01 || exacMAF > 0.01 || gonlMAF > 0.01)
		{
			LOG.info("Skipping 'common' variant (>1% AF in GoNL/ExAC/1000G): " + entity);
			resultMap.put(DENOVO, 0);
			return true;
		}

		// impact
		String[] annSplit = entity.getString(VcfRepository.getInfoPrefix() + "ANN").split("\\|", -1);
		SnpEffAnnotator.Impact impact = Enum.valueOf(SnpEffAnnotator.Impact.class, annSplit[2]);
		if (impact.equals(SnpEffAnnotator.Impact.MODIFIER) || impact.equals(SnpEffAnnotator.Impact.LOW))
		{
			LOG.info("Skipping MODIFIER/LOW impact variant: " + entity);
			resultMap.put(DENOVO, 0);
			return true;
		}
		return false;
	}

	/**
	 * Filter trios for those that are complete (c+f+m)
	 * 
	 * @param trios
	 * @return
	 */
	public HashMap<String, Trio> getCompleteTrios(HashMap<String, Trio> trios)
	{
		HashMap<String, Trio> result = new HashMap<>();

		for (String key : trios.keySet())
		{
			if (trios.get(key).getChild() != null && trios.get(key).getMother() != null
					&& trios.get(key).getFather() != null)
			{
				result.put(key, trios.get(key));
			}
		}
		LOG.info("Of which " + result.size() + " complete, having child+mother+father..");
		return result;
	}

	/**
	 * Find de novo variants in complete trios Genotype may be missing
	 * 
	 * @param trio
	 * @return
	 */
	public int findDeNovoVariants(Trio trio)
	{

		/**
		 * Null checks
		 */
		if (trio.getMother().getGenotype().get("GT") == null)
		{
			LOG.warn("Maternal genotype null, skipping trio for child " + trio.getChild().getId());
			return 0;
		}
		if (trio.getFather().getGenotype().get("GT") == null)
		{
			LOG.warn("Paternal genotype null, skipping trio for child " + trio.getChild().getId());
			return 0;
		}
		if (trio.getChild().getGenotype().get("GT") == null)
		{
			LOG.warn("Child genotype null, skipping trio for child " + trio.getChild().getId());
			return 0;
		}

		/**
		 * Quality checks: genotype completeness
		 */
		String[] mat_all = trio.getMother().getGenotype().get("GT").toString().split("/", -1);
		if (mat_all.length != 2)
		{
			LOG.warn("Maternal genotype split by '/' does not have 2 elements, skipping trio for child "
					+ trio.getChild().getId());
			return 0;
		}

		String[] pat_all = trio.getFather().getGenotype().get("GT").toString().split("/", -1);
		if (pat_all.length != 2)
		{
			LOG.warn("Paternal genotype split by '/' does not have 2 elements, skipping trio for child "
					+ trio.getChild().getId());
			return 0;
		}

		String[] chi_all = trio.getChild().getGenotype().get("GT").toString().split("/", -1);
		if (chi_all.length != 2)
		{
			LOG.warn("Child genotype split by '/' does not have 2 elements, skipping trio for child "
					+ trio.getChild().getId());
			return 0;
		}

		/**
		 * Quality checks: read depth
		 */
		int minDepth = 20;

		int mat_dp = Integer.parseInt(trio.getMother().getGenotype().get("DP").toString());
		if (mat_dp < minDepth)
		{
			LOG.warn("Maternal genotype has less than " + minDepth + " reads (" + mat_dp
					+ "), skipping trio for child " + trio.getChild().getId());
			return 0;
		}

		int pat_dp = Integer.parseInt(trio.getFather().getGenotype().get("DP").toString());
		if (pat_dp < minDepth)
		{
			LOG.warn("Paternal genotype has less than " + minDepth + " reads (" + pat_dp
					+ "), skipping trio for child " + trio.getChild().getId());
			return 0;
		}

		int child_dp = Integer.parseInt(trio.getChild().getGenotype().get("DP").toString());
		if (child_dp < minDepth)
		{
			LOG.warn("Child genotype has less than " + minDepth + " reads (" + child_dp + "), skipping trio for child "
					+ trio.getChild().getId());
			return 0;
		}

		/**
		 * Quality checks: genotype quality
		 */
		double minQual = 30.00;

		double mat_gq = Double.parseDouble(trio.getMother().getGenotype().get("GQ").toString());
		if (mat_gq < minQual)
		{
			LOG.warn("Maternal genotype has less than " + minQual + " quality (" + mat_gq
					+ "), skipping trio for child " + trio.getChild().getId());
			return 0;
		}

		double pat_gq = Double.parseDouble(trio.getFather().getGenotype().get("GQ").toString());
		if (pat_gq < minQual)
		{
			LOG.warn("Paternal genotype has less than " + minQual + " quality (" + pat_gq
					+ "), skipping trio for child " + trio.getChild().getId());
			return 0;
		}

		double child_gq = Double.parseDouble(trio.getChild().getGenotype().get("GQ").toString());
		if (child_gq < minQual)
		{
			LOG.warn("Child genotype has less than " + minQual + " quality (" + child_gq
					+ "), skipping trio for child " + trio.getChild().getId());
			return 0;
		}

		/**
		 * Test if any combination of parent alleles can form the child alleles and return if possible
		 */
		for (String ma : mat_all)
		{
			for (String pa : pat_all)
			{
				// if child alleles in any combinations match, return 0
				if ((chi_all[0].equals(ma) && chi_all[1].equals(pa))
						|| (chi_all[0].equals(pa) && chi_all[1].equals(ma)))
				{
					return 0;
				}
			}
		}

		/**
		 * If none pass, we found a de novo variant
		 */
		LOG.info("De novo variant found for trio " + trio);
		return 1;
	}

	@Override
	public List<AttributeMetaData> getOutputMetaData()
	{
		List<AttributeMetaData> metadata = new ArrayList<>();
		metadata.add(new DefaultAttributeMetaData(DENOVO, FieldTypeEnum.STRING).setLabel(DENOVO_LABEL));
		return metadata;
	}

	@Override
	public List<AttributeMetaData> getInputMetaData()
	{
		List<AttributeMetaData> entityMetaData = super.getInputMetaData();
		entityMetaData.add(VcfRepository.FILTER_META);
		entityMetaData.add(VcfRepository.QUAL_META);
		entityMetaData.add(new DefaultAttributeMetaData(VcfRepository.getInfoPrefix() + "ANN", FieldTypeEnum.TEXT));
		entityMetaData.add(new DefaultAttributeMetaData("ABHet", FieldTypeEnum.STRING));
		entityMetaData.add(new DefaultAttributeMetaData("ABHom", FieldTypeEnum.STRING));
		entityMetaData.add(new DefaultAttributeMetaData("SB", FieldTypeEnum.STRING));
		entityMetaData.add(new DefaultAttributeMetaData(VcfRepository.SAMPLES, FieldTypeEnum.MREF));

		return entityMetaData;
	}

	@Override
	public AnnotatorInfo getInfo()
	{
		return AnnotatorInfo.create(Status.INDEV, Type.UNUSED, "unknown", "no description", getOutputMetaData());
	}
}
