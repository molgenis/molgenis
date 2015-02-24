package org.molgenis.data.annotation.impl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.common.collect.Iterables;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.VariantAnnotator;
import org.molgenis.data.annotation.VcfUtils;
import org.molgenis.data.annotation.provider.CgdDataProvider;
import org.molgenis.data.annotation.provider.CgdDataProvider.generalizedInheritance;
import org.molgenis.data.support.AnnotationServiceImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.server.MolgenisSimpleSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Monogenic disease filter
 * 
 * */
@Component("monogenicDiseaseService")
public class MonogenicDiseaseCandidatesServiceAnnotator extends VariantAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(MonogenicDiseaseCandidatesServiceAnnotator.class);

	private final MolgenisSettings molgenisSettings;
	private final AnnotationService annotatorService;

	// the cadd service returns these two values
	// must be compatible with VCF format, ie no funny characters
	public static final String MONOGENICDISEASECANDIDATE = VcfRepository.getInfoPrefix() + "MONGENDISCAND";
	private static final String HOMREF = "HOMREF";
	private static final String HOMALT = "HOMALT";
	private static final String HET = "HET";

	private static final String NAME = "MONOGENICDISEASE";
	
	public enum outcome {
		EXCLUDED,
		INCLUDED_DOMINANT,
		INCLUDED_DOMINANT_HIGHIMPACT,
		INCLUDED_RECESSIVE,
		INCLUDED_RECESSIVE_HIGHIMPACT,
		INCLUDED_RECESSIVE_COMPOUND,
		INCLUDED_OTHER
	}

	final List<String> infoFields = Arrays
			.asList(new String[]
			{
					"##INFO=<ID="
							+ MONOGENICDISEASECANDIDATE.substring(VcfRepository.getInfoPrefix().length())
							+ ",Number=1,Type=String,Description=\"Possible outcomes: EXCLUDED, INCLUDED_DOMINANT, INCLUDED_DOMINANT_HIGHIMPACT, INCLUDED_RECESSIVE, INCLUDED_RECESSIVE_HIGHIMPACT, INCLUDED_RECESSIVE_COMPOUND, INCLUDED_OTHER\">",
								});
	
	@Autowired
	public MonogenicDiseaseCandidatesServiceAnnotator(MolgenisSettings molgenisSettings, AnnotationService annotatorService)
			throws IOException
	{
		this.molgenisSettings = molgenisSettings;
		this.annotatorService = annotatorService;
	}

	public MonogenicDiseaseCandidatesServiceAnnotator(File filterSettings, File inputVcfFile, File outputVCFFile) throws Exception
	{

		//TODO: filterSettings ??
		
		this.molgenisSettings = new MolgenisSimpleSettings();
		this.annotatorService = new AnnotationServiceImpl();

		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");

		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
		Iterator<Entity> vcfIter = vcfRepo.iterator();

		VcfUtils.checkInput(inputVcfFile, outputVCFWriter, infoFields, MONOGENICDISEASECANDIDATE.substring(VcfRepository.getInfoPrefix().length()));

		System.out.println("Now starting to process the data.");

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
		outputVCFWriter.close();
		vcfRepo.close();
		System.out.println("All done!");
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		annotatorService.addAnnotator(this);
	}

	@Override
	public boolean annotationDataExists()
	{
		return false;
	}

	@Override
	public String getSimpleName()
	{
		return NAME;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	{
		Map<String, Object> resultMap = annotateEntityWithMonogenicDiseaseCandidates(entity);
		return Collections.<Entity> singletonList(getAnnotatedEntity(entity, resultMap));
	}

	private synchronized Map<String, Object> annotateEntityWithMonogenicDiseaseCandidates(Entity entity) throws IOException
	{
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		// TODO: can be multiple!! even with canonical output...
		// ANN=G|intron_variant|MODIFIER|LOC101926913|LOC101926913|transcript|NR_110185.1|Noncoding|5/5|n.376+9526G>C||||||,G|non_coding_exon_variant|MODIFIER|LINC01124|LINC01124|transcript|NR_027433.1|Noncoding|1/1|n.590G>C||||||;
		
		/**
		 * Important variables to use in monogenic disease filter
		 */
		String[] annSplit = entity.getString(VcfRepository.getInfoPrefix() + "ANN").split("\\|", -1);
		double thGenMAF = entity.getDouble(ThousandGenomesServiceAnnotator.THGEN_MAF) != null ? entity.getDouble(ThousandGenomesServiceAnnotator.THGEN_MAF) : 0;
		double exacMAF = entity.getDouble(ExACServiceAnnotator.EXAC_MAF) != null ? entity.getDouble(ExACServiceAnnotator.EXAC_MAF) : 0;
		double gonlMAF = entity.getDouble(GoNLServiceAnnotator.GONL_MAF) != null ? entity.getDouble(GoNLServiceAnnotator.GONL_MAF) : 0;
		CgdDataProvider.generalizedInheritance cgdGenInh = entity.getString(ClinicalGenomicsDatabaseServiceAnnotator.CGD_GENERALIZED_INHERITANCE) != null ? generalizedInheritance.valueOf(entity.getString(ClinicalGenomicsDatabaseServiceAnnotator.CGD_GENERALIZED_INHERITANCE)) : null;
		String originalInheritance = entity.getString(ClinicalGenomicsDatabaseServiceAnnotator.CGD_INHERITANCE) != null ? entity.getString(ClinicalGenomicsDatabaseServiceAnnotator.CGD_INHERITANCE) : null;
		SnpEffServiceAnnotator.impact impact = SnpEffServiceAnnotator.impact.valueOf(annSplit[2]);
		String gene = annSplit[3];
		String condition = entity.getString(ClinicalGenomicsDatabaseServiceAnnotator.CGD_CONDITION);
		String hgvsVariant = annSplit[9];
		
		/**
		 * Read and check genotype data
		 */
		String alleles = null;
		String zygosity = null;
		if(entity.getEntities("Samples") != null && !Iterables.isEmpty(entity.getEntities("Samples")) && Iterables.size(entity.getEntities("Samples")) == 1)
		{
			for(Entity sample: entity.getEntities("SAMPLES"))
			{
				alleles = sample.getString("GT");
				break;
			}
		}
		else
		{
			throw new IOException("Expecting exactly 1 sample! bad data: " + entity.toString());
		}
		
		if(alleles.length() == 1)
		{
			throw new IOException("Hemizygous calls not yet supported (although would be easy)");
		}
		
		if(alleles.length() != 3)
		{
			throw new IOException("Genotype length not 3: " + alleles + " for record " + entity.toString());
		}
		
		char allele1 = alleles.charAt(0);
		char allele2 = alleles.charAt(2);
		
		if(!((allele1 == '0' || allele1 == '1') && (allele2 == '0' || allele2 == '1')))
		{
			throw new IOException("Allelic values other than 0 or 1 not yet supported, for " + alleles + " for record " + entity.toString());
		}
		
		if(allele1 == '0' && allele2 == '0')
		{
			zygosity = HOMREF;
		}
		else if(allele1 == '1' && allele2 == '1')
		{
			zygosity = HOMALT;
		}
		else
		{
			zygosity = HET;
		}
		
		/**
		 * Broad spectrum filters, already gets rid of >99% of variants
		 */
		
		//not in CGD, skip variant!
		if(cgdGenInh == null)
		{
			resultMap.put(MONOGENICDISEASECANDIDATE, outcome.EXCLUDED);
			return resultMap;
		}
		
		//common variant in one of the three big databases, skip it
		if(thGenMAF > 0.05 || exacMAF > 0.05 || gonlMAF > 0.05)
		{
			resultMap.put(MONOGENICDISEASECANDIDATE, outcome.EXCLUDED);
			return resultMap;
		}
		
		//skip any "low impact" variants
		if(impact.equals(SnpEffServiceAnnotator.impact.MODIFIER) || impact.equals(SnpEffServiceAnnotator.impact.LOW))
		{
			resultMap.put(MONOGENICDISEASECANDIDATE, outcome.EXCLUDED);
			return resultMap;
		}
		
		//skip any homozygous reference alleles
		if(zygosity.equals(HOMREF))
		{
			resultMap.put(MONOGENICDISEASECANDIDATE, outcome.EXCLUDED);
			return resultMap;
		}
		
		/**
		 * Sensitive filters
		 * We already know that zygosity is HET or HOMALT and MAF < 0.05
		 */
		
		//helper hashmap to find compound heterozygous pathogenicity
		Set<String> genesWithCandidates = new HashSet<String>();
		
		//dominant disease
		if(cgdGenInh.equals(CgdDataProvider.generalizedInheritance.DOMINANT) || cgdGenInh.equals(CgdDataProvider.generalizedInheritance.DOM_OR_REC))
		{
			
			// must be rare enough in EACH database
			if(thGenMAF < 0.0025 && exacMAF < 0.0025 && gonlMAF < 0.0025)
			{
				resultMap.put(MONOGENICDISEASECANDIDATE, impact.equals(SnpEffServiceAnnotator.impact.HIGH) ? outcome.INCLUDED_DOMINANT_HIGHIMPACT : outcome.INCLUDED_DOMINANT);
				return resultMap;
			}
			else
			{
				LOG.info("EXCLUDED candidate for dominant disease because not rare enough! 1KG: " + thGenMAF + ", ExAC: " + exacMAF + ", GoNL: " + gonlMAF +", for :" + entity.toString());
				resultMap.put(MONOGENICDISEASECANDIDATE, outcome.EXCLUDED);
				return resultMap;
			}
		}
		
		//recessive disease
		else if(cgdGenInh.equals(CgdDataProvider.generalizedInheritance.RECESSIVE) || cgdGenInh.equals(CgdDataProvider.generalizedInheritance.XLINKED))
		{
			//must be HOMALT for this
			if(zygosity.equals(HOMALT))
			{
				resultMap.put(MONOGENICDISEASECANDIDATE, impact.equals(SnpEffServiceAnnotator.impact.HIGH) ? outcome.INCLUDED_RECESSIVE_HIGHIMPACT : outcome.INCLUDED_RECESSIVE);
				return resultMap;
			}
			else
			{
				if(genesWithCandidates.contains(gene))
				{
					LOG.info("INCLUDED heterozygous variant for comp. het. recessive disease because we've seen at least 1 candidate before in gene '"+gene+", for " + entity.toString());
					resultMap.put(MONOGENICDISEASECANDIDATE, outcome.INCLUDED_RECESSIVE_COMPOUND);
					return resultMap;
				}
				else
				{
					resultMap.put(MONOGENICDISEASECANDIDATE, outcome.EXCLUDED); //exclude the 'first' variant in a compound series
					return resultMap;
				}
			}
		}
		
		//other
		else
		{
			LOG.info("INCLUDED variant with weird inheritance mode '"+originalInheritance+"', condition '"+condition+"', keeping variant " + entity.toString());
			resultMap.put(MONOGENICDISEASECANDIDATE, outcome.INCLUDED_OTHER);
			return resultMap;
		}

	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(MONOGENICDISEASECANDIDATE, FieldTypeEnum.INT)); //FIXME best type?
		return metadata;
	}

}
