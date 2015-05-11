package org.molgenis.data.annotation.impl;

import org.elasticsearch.common.collect.Iterables;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.VariantAnnotator;
import org.molgenis.data.annotation.cmd.AnnotatorInfo;
import org.molgenis.data.annotation.provider.CgdDataProvider;
import org.molgenis.data.annotation.provider.CgdDataProvider.generalizedInheritance;
import org.molgenis.data.annotation.utils.AnnotatorUtils;
import org.molgenis.data.support.AnnotationServiceImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.molgenis.framework.server.MolgenisSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

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

/**
 * Monogenic disease filter
 * 
 * */
@Component("monogenicDiseaseService")
public class MonogenicDiseaseCandidatesServiceAnnotator extends VariantAnnotator
{
	@Override
	public AnnotatorInfo.status getStatus(){
		return AnnotatorInfo.status.BETA;
	}

	@Override
	public AnnotatorInfo.type getType(){
		return AnnotatorInfo.type.AUTOMATED_PROTOCOL;
	}
	
	@Override
	public String getCode()
	{
		return "monogenic";
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(MonogenicDiseaseCandidatesServiceAnnotator.class);
	public static final String ANNOTATIONFIELD = VcfRepository.getInfoPrefix() + "ANN";

	private final AnnotationService annotatorService;
	public static final String MONOGENICDISEASECANDIDATE_LABEL = "MONGENDISCAND";
	private static final String HOMREF_LABEL = "HOMREF";
	private static final String HOMALT_LABEL = "HOMALT";
	private static final String HET_LABEL = "HET";
	private static final String HOMREF = VcfRepository.getInfoPrefix() + HOMREF_LABEL;
	private static final String HOMALT = VcfRepository.getInfoPrefix() + HOMALT_LABEL;
	private static final String HET = VcfRepository.getInfoPrefix() + HET_LABEL;
	public static final String MONOGENICDISEASECANDIDATE = VcfRepository.getInfoPrefix()
			+ MONOGENICDISEASECANDIDATE_LABEL;

	private static final String NAME = "MONOGENICDISEASE";

	// helper hashmap to find compound heterozygous pathogenicity
	Set<String> genesWithCandidates;

	public enum outcome
	{
		EXCLUDED, EXCLUDED_FIRST_OF_COMPOUND, EXCLUDED_FIRST_OF_COMPOUND_HIGHIMPACT, INCLUDED_DOMINANT, INCLUDED_DOMINANT_HIGHIMPACT, INCLUDED_RECESSIVE, INCLUDED_RECESSIVE_HIGHIMPACT, INCLUDED_RECESSIVE_COMPOUND, INCLUDED_RECESSIVE_COMPOUND_HIGHIMPACT, INCLUDED_OTHER
	}

	final List<String> infoFields = Arrays
			.asList(new String[]
                    {"##INFO=<ID="
                            + MONOGENICDISEASECANDIDATE.substring(VcfRepository.getInfoPrefix().length())
                            + ",Number=1,Type=String,Description=\"Possible outcomes: EXCLUDED, INCLUDED_DOMINANT, INCLUDED_DOMINANT_HIGHIMPACT, INCLUDED_RECESSIVE, INCLUDED_RECESSIVE_HIGHIMPACT, INCLUDED_RECESSIVE_COMPOUND, INCLUDED_OTHER\">",});

	@Autowired
	public MonogenicDiseaseCandidatesServiceAnnotator(MolgenisSettings molgenisSettings,
			AnnotationService annotatorService) throws IOException
	{
		this.annotatorService = annotatorService;
	}

	public MonogenicDiseaseCandidatesServiceAnnotator(File filterSettings, File inputVcfFile, File outputVCFFile)
			throws Exception
	{
		// TODO: filterSettings in input file??
		// or... symptoms then invoke
		// http://compbio.charite.de/phenomizer/phenomizer/PhenomizerServiceURI?mobilequery=true&terms=HP:0001300,HP:0007325&numres=100

		genesWithCandidates = new HashSet<String>();

		this.annotatorService = new AnnotationServiceImpl();

		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");

		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
		Iterator<Entity> vcfIter = vcfRepo.iterator();

		VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter, infoFields,
				MONOGENICDISEASECANDIDATE.substring(VcfRepository.getInfoPrefix().length()));

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
	public String getSimpleName()
	{
		return NAME;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	{
		Map<String, Object> resultMap = annotateEntityWithMonogenicDiseaseCandidates(entity);
		return Collections.singletonList(AnnotatorUtils.getAnnotatedEntity(this, entity, resultMap));
	}

	private synchronized Map<String, Object> annotateEntityWithMonogenicDiseaseCandidates(Entity entity)
			throws IOException
	{
		Map<String, Object> resultMap = new HashMap<String, Object>();

		/**
		 * Important variables to use in monogenic disease filter
		 */
		String[] annSplit = entity.getString(ANNOTATIONFIELD).split("\\|", -1);
		if (annSplit.length < 4) throw new MolgenisDataException(
				"Annotation field does not contain the expected input (less items than expected)");
		double thousandGenomesMAF = entity.getDouble(ThousandGenomesServiceAnnotator.THGEN_MAF) != null ? entity
				.getDouble(ThousandGenomesServiceAnnotator.THGEN_MAF) : 0;
		double exacMAF = entity.getDouble(ExACServiceAnnotator.EXAC_MAF) != null ? entity
				.getDouble(ExACServiceAnnotator.EXAC_MAF) : 0;
		double gonlMAF = entity.getDouble(GoNLServiceAnnotator.GONL_MAF) != null ? entity
				.getDouble(GoNLServiceAnnotator.GONL_MAF) : 0;
		CgdDataProvider.generalizedInheritance cgdGenInh = entity
				.getString(ClinicalGenomicsDatabaseServiceAnnotator.GENERALIZED_INHERITANCE) != null ? generalizedInheritance
				.valueOf(entity.getString(ClinicalGenomicsDatabaseServiceAnnotator.GENERALIZED_INHERITANCE)) : null;
		String originalInheritance = entity.getString(ClinicalGenomicsDatabaseServiceAnnotator.INHERITANCE) != null ? entity
				.getString(ClinicalGenomicsDatabaseServiceAnnotator.INHERITANCE) : null;
		SnpEffServiceAnnotator.impact impact = SnpEffServiceAnnotator.impact.valueOf(annSplit[2]);
		String gene = annSplit[3];
		String condition = entity.getString(ClinicalGenomicsDatabaseServiceAnnotator.CONDITION);

		// TODO: can be multiple!! even with canonical output...
		String zygosity = checkGenotypeData(entity);
		boolean filter = isApplyBroadSpectrumFilter(thousandGenomesMAF, exacMAF, gonlMAF, cgdGenInh, impact, zygosity);

		if (filter)
		{
			resultMap.put(MONOGENICDISEASECANDIDATE, outcome.EXCLUDED);
		}
		else
		{

			/**
			 * Sensitive filters We already know that zygosity is HET or HOMALT and MAF < 0.05
			 */

			// dominant disorders, including those may may also be recessive, and X-linked, since CGD does not
			// distinguish
			// dominant or recessive for those..
			if (cgdGenInh.equals(CgdDataProvider.generalizedInheritance.DOMINANT)
					|| cgdGenInh.equals(CgdDataProvider.generalizedInheritance.DOM_OR_REC)
					|| cgdGenInh.equals(CgdDataProvider.generalizedInheritance.XLINKED))
			{
				// must be rare enough in EACH database
				annotateForDominantDisorders(resultMap, thousandGenomesMAF, exacMAF, gonlMAF, cgdGenInh, impact);
			}

			// recessive disorders, including those may may also be dominant, and X-linked, since CGD does not
			// distinguish
			// dominant or recessive for those..
			else if (cgdGenInh.equals(CgdDataProvider.generalizedInheritance.RECESSIVE)
					|| cgdGenInh.equals(CgdDataProvider.generalizedInheritance.DOM_OR_REC)
					|| cgdGenInh.equals(CgdDataProvider.generalizedInheritance.XLINKED))
			{
				annotateForRecessiveDisorders(entity, resultMap, impact, gene, zygosity);
			}
			else
			{
				LOG.info("INCLUDED variant with untypical inheritance mode '" + originalInheritance + "', condition '"
						+ condition + "', keeping variant " + entity.toString());
				resultMap.put(MONOGENICDISEASECANDIDATE, outcome.INCLUDED_OTHER);
			}
		}
		return resultMap;
	}

	private void annotateForRecessiveDisorders(Entity entity, Map<String, Object> resultMap,
			SnpEffServiceAnnotator.impact impact, String gene, String zygosity) throws IOException
	{
		// must be HOMALT for this
		if (zygosity.equals(HOMALT))
		{
			resultMap
					.put(MONOGENICDISEASECANDIDATE,
                            impact.equals(SnpEffServiceAnnotator.impact.HIGH) ? outcome.INCLUDED_RECESSIVE_HIGHIMPACT : outcome.INCLUDED_RECESSIVE);
		}
		// only option left: HET, but check just in case
		else if (zygosity.equals(HET))
		{
			if (genesWithCandidates.contains(gene))
			{
				LOG.info("INCLUDED heterozygous variant for comp. het. recessive disease because we've seen at least 1 candidate before in gene '"
						+ gene + ", for " + entity.toString());
				resultMap
						.put(MONOGENICDISEASECANDIDATE,
                                impact.equals(SnpEffServiceAnnotator.impact.HIGH) ? outcome.INCLUDED_RECESSIVE_COMPOUND_HIGHIMPACT : outcome.INCLUDED_RECESSIVE_COMPOUND);
			}
			else
			{
				genesWithCandidates.add(gene);
				resultMap
						.put(MONOGENICDISEASECANDIDATE,
                                impact.equals(SnpEffServiceAnnotator.impact.HIGH) ? outcome.EXCLUDED_FIRST_OF_COMPOUND_HIGHIMPACT : outcome.EXCLUDED_FIRST_OF_COMPOUND); // exclude
			}
		}
		else
		{
			throw new IOException("Zygosity HOMREF, something went wrong in prefilter!");
		}
	}

	private void annotateForDominantDisorders(Map<String, Object> resultMap, double thousandGenomesMAF, double exacMAF,
			double gonlMAF, generalizedInheritance cgdGenInh, SnpEffServiceAnnotator.impact impact)
	{
		if (thousandGenomesMAF < 0.0025 && exacMAF < 0.0025 && gonlMAF < 0.0025)
		{
			resultMap
					.put(MONOGENICDISEASECANDIDATE,
                            impact.equals(SnpEffServiceAnnotator.impact.HIGH) ? outcome.INCLUDED_DOMINANT_HIGHIMPACT : outcome.INCLUDED_DOMINANT);
		}
		// if purely dominant, exclude at this point!
		else if (cgdGenInh.equals(generalizedInheritance.DOMINANT))
		{
			resultMap.put(MONOGENICDISEASECANDIDATE, outcome.EXCLUDED);
		}
	}

	private boolean isApplyBroadSpectrumFilter(double thousandGenomesMAF, double exacMAF, double gonlMAF,
			generalizedInheritance cgdGenInh, SnpEffServiceAnnotator.impact impact, String zygosity)
	{
		/**
		 * Broad spectrum filters, already gets rid of >99% of variants
		 */
		boolean filter = false;
		// not in CGD, skip variant!
		if (cgdGenInh == null) filter = true;
		// common variant in one of the three big databases, skip it
		else if (thousandGenomesMAF > 0.05 || exacMAF > 0.05 || gonlMAF > 0.05) filter = true;
		// skip any "low impact" variants
		else if (impact.equals(SnpEffServiceAnnotator.impact.MODIFIER)
				|| impact.equals(SnpEffServiceAnnotator.impact.LOW)) filter = true;
		// skip any homozygous reference alleles
		else if (zygosity.equals(HOMREF)) filter = true;
		return filter;
	}

	private String checkGenotypeData(Entity entity) throws IOException
	{
		/**
		 * Read and check genotype data
		 */
		String alleles = null;
		String zygosity;
		if (entity.getEntities(VcfRepository.SAMPLES) != null
				&& !Iterables.isEmpty(entity.getEntities(VcfRepository.SAMPLES))
				&& Iterables.size(entity.getEntities(VcfRepository.SAMPLES)) == 1)
		{
			for (Entity sample : entity.getEntities(VcfRepository.SAMPLES))
			{
				alleles = sample.getString("GT");
				break;
			}
		}
		else
		{
			throw new IOException("Expecting exactly 1 sample! bad data: " + entity.toString());
		}

		if (alleles.length() == 1) throw new IOException("Hemizygous calls not yet supported");
		else if (alleles.length() != 3) throw new IOException("Genotype length not 3: " + alleles + " for record "
				+ entity.toString());

		char allele1 = alleles.charAt(0);
		char allele2 = alleles.charAt(2);

		if (!((allele1 == '0' || allele1 == '1') && (allele2 == '0' || allele2 == '1')))
		{
			throw new IOException("Allelic values other than 0 or 1 not yet supported, for " + alleles + " for record "
					+ entity.toString());
		}

		if (allele1 == '0' && allele2 == '0')
		{
			zygosity = HOMREF;
		}
		else if (allele1 == '1' && allele2 == '1')
		{
			zygosity = HOMALT;
		}
		else
		{
			zygosity = HET;
		}
		return zygosity;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(MONOGENICDISEASECANDIDATE, FieldTypeEnum.STRING)
				.setLabel(MONOGENICDISEASECANDIDATE_LABEL));
		return metadata;
	}

	@Override
	public EntityMetaData getInputMetaData()
	{
		DefaultEntityMetaData entityMetaData = (DefaultEntityMetaData) super.getInputMetaData();
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(ANNOTATIONFIELD, FieldTypeEnum.TEXT));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(ThousandGenomesServiceAnnotator.THGEN_MAF,
				FieldTypeEnum.DECIMAL));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(ExACServiceAnnotator.EXAC_MAF,
				FieldTypeEnum.DECIMAL));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(GoNLServiceAnnotator.GONL_MAF,
				FieldTypeEnum.DECIMAL));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(
				ClinicalGenomicsDatabaseServiceAnnotator.GENERALIZED_INHERITANCE, FieldTypeEnum.TEXT));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(
				ClinicalGenomicsDatabaseServiceAnnotator.INHERITANCE, FieldTypeEnum.TEXT));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(VcfRepository.SAMPLES, FieldTypeEnum.MREF));
		return entityMetaData;
	}

}
