package org.molgenis.data.annotation.impl;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.DECIMAL;
import static org.molgenis.data.vcf.VcfRepository.ALT;
import static org.molgenis.data.vcf.VcfRepository.ALT_META;
import static org.molgenis.data.vcf.VcfRepository.CHROM_META;
import static org.molgenis.data.vcf.VcfRepository.POS_META;
import static org.molgenis.data.vcf.VcfRepository.REF;
import static org.molgenis.data.vcf.VcfRepository.REF_META;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.elasticsearch.common.collect.Iterables;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.VariantAnnotator;
import org.molgenis.data.annotation.VcfUtils;
import org.molgenis.data.annotation.tabix.TabixRepository;
import org.molgenis.data.support.AnnotationServiceImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.server.MolgenisSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

/**
 * <p>
 * This class performs a system call to cross reference a chromosome and genomic location with a tabix indexed file. A
 * match can result in 1, 2 or 3 hits. These matches are reduced to one based on a reference and alternative nucleotide
 * base. The remaining hit will be used to parse two CADD scores.
 * </p>
 * 
 * <p>
 * <b>CADD returns:</b> CADD score Absolute, CADD score Scaled
 * </p>
 * 
 * @author mdehaan
 * 
 * */
@Component("caddService")
public class CaddServiceAnnotator extends VariantAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(CaddServiceAnnotator.class);

	private final AnnotationService annotatorService;
	private static final String NAME = "CADD";
	private final static DefaultEntityMetaData TABIX_METADATA = new DefaultEntityMetaData(NAME);
	{
		TABIX_METADATA.addAttributeMetaData(CHROM_META);
		TABIX_METADATA.addAttributeMetaData(POS_META);
		TABIX_METADATA.addAttributeMetaData(REF_META);
		TABIX_METADATA.addAttributeMetaData(ALT_META);
		TABIX_METADATA.addAttributeMetaData(new DefaultAttributeMetaData(CADD_ABS, DECIMAL));
		TABIX_METADATA.addAttributeMetaData(new DefaultAttributeMetaData(CADD_SCALED, DECIMAL));
		TABIX_METADATA.addAttribute("id").setIdAttribute(true).setVisible(false);
	}

	// the cadd service returns these two values
	// must be compatible with VCF format, ie no funny characters
	public static final String CADD_SCALED = VcfRepository.getInfoPrefix() + "CADDSCALED";
	public static final String CADD_ABS = VcfRepository.getInfoPrefix() + "CADDABS";

	final List<String> infoFields = Arrays
			.asList(new String[]
			{
					"##INFO=<ID="
							+ CADD_SCALED.substring(VcfRepository.getInfoPrefix().length())
							+ ",Number=1,Type=Float,Description=\"CADD scaled C score, ie. phred-like. See Kircher et al. 2014 (http://www.ncbi.nlm.nih.gov/pubmed/24487276) or CADD website (http://cadd.gs.washington.edu/) for more information.\">",
					"##INFO=<ID="
							+ CADD_ABS.substring(VcfRepository.getInfoPrefix().length())
							+ ",Number=1,Type=Float,Description=\"CADD absolute C score, ie. unscaled SVM output. Useful as  reference when the scaled score may be unexpected.\">" });

	public static final String CADD_FILE_LOCATION_PROPERTY = "cadd_location";

	private TabixRepository tabixRepository;

	@Autowired
	public CaddServiceAnnotator(MolgenisSettings molgenisSettings, AnnotationService annotatorService)
			throws IOException
	{
		this.annotatorService = annotatorService;
		tabixRepository = new TabixRepository(molgenisSettings.getProperty(CADD_FILE_LOCATION_PROPERTY), TABIX_METADATA);
	}

	public CaddServiceAnnotator(File caddTsvGzFile, File inputVcfFile, File outputVCFFile) throws Exception
	{
		this.annotatorService = new AnnotationServiceImpl();
		tabixRepository = new TabixRepository(caddTsvGzFile.getAbsolutePath(), TABIX_METADATA);

		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");

		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
		Iterator<Entity> vcfIter = vcfRepo.iterator();

		VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter, infoFields, CADD_SCALED);

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
		return tabixRepository.fileExists();
	}

	@Override
	public String getSimpleName()
	{
		return NAME;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	{
		Map<String, Object> resultMap = annotateEntityWithCADD(entity.getString(VcfRepository.CHROM),
				entity.getLong(VcfRepository.POS), entity.getString(VcfRepository.REF),
				entity.getString(VcfRepository.ALT));
		return Collections.<Entity> singletonList(getAnnotatedEntity(entity, resultMap));
	}

	private synchronized Map<String, Object> annotateEntityWithCADD(String chromosome, Long position, String reference,
			String alternative) throws IOException, InterruptedException
	{
		Iterable<Entity> caddLines = null;
		if (position != null)
		{
			try
			{
				Query query = tabixRepository.query().eq(VcfRepository.CHROM, chromosome).and()
						.eq(VcfRepository.POS, position.toString());
				caddLines = tabixRepository.findAll(query);
			}
			catch (Exception e)
			{
				LOG.error("Something went wrong (chromosome not in data?) when querying CADD tabix file for "
						+ chromosome + " POS: " + position + " REF: " + reference + " ALT: " + alternative
						+ "! skipping...");
			}
			for (Entity caddLine : caddLines)
			{
				if (caddLine.get(REF).equals(reference) && caddLine.get(ALT).equals(alternative))
				{
					return ImmutableMap.<String, Object> of(CADD_ABS, caddLine.get(CADD_ABS), CADD_SCALED,
							caddLine.get(CADD_SCALED));
				}
				// In some cases, the ref and alt are swapped. If this is the case, the initial if statement above will
				// fail, we can just check whether such a swapping has occured
				if (caddLine.get(ALT).equals(reference) && caddLine.get(REF).equals(alternative))
				{
					LOG.info("CADD scores found [swapped REF and ALT!] for CHROM: " + chromosome + " POS: " + position
							+ " REF: " + reference + " ALT: " + alternative + " LINE: " + caddLine);
					return ImmutableMap.<String, Object> of(CADD_ABS, caddLine.get(CADD_ABS), CADD_SCALED,
							caddLine.get(CADD_SCALED));
				}
			}
			if (Iterables.size(caddLines) > 3)
			{
				LOG.warn("More than 3 hits in the CADD file! for CHROM: " + chromosome + " POS: " + position + " REF: "
						+ reference + " ALT: " + alternative);
			}
		}
		return Collections.emptyMap();
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		DefaultAttributeMetaData cadd_abs = new DefaultAttributeMetaData(CADD_ABS, FieldTypeEnum.DECIMAL);
		cadd_abs.setDescription("\"Raw\" CADD scores come straight from the model, and are interpretable as the extent to which the annotation profile for a given variant suggests that that variant is likely to be \"observed\" (negative values) vs \"simulated\" (positive values). These values have no absolute unit of meaning and are incomparable across distinct annotation combinations, training sets, or model parameters. However, raw values do have relative meaning, with higher values indicating that a variant is more likely to be simulated (or \"not observed\") and therefore more likely to have deleterious effects.");
		DefaultAttributeMetaData cadd_scaled = new DefaultAttributeMetaData(CADD_SCALED, FieldTypeEnum.DECIMAL);
		cadd_scaled
				.setDescription("Since the raw scores do have relative meaning, one can take a specific group of variants, define the rank for each variant within that group, and then use that value as a \"normalized\" and now externally comparable unit of analysis. In our case, we scored and ranked all ~8.6 billion SNVs of the GRCh37/hg19 reference and then \"PHRED-scaled\" those values by expressing the rank in order of magnitude terms rather than the precise rank itself. For example, reference genome single nucleotide variants at the 10th-% of CADD scores are assigned to CADD-10, top 1% to CADD-20, top 0.1% to CADD-30, etc. The results of this transformation are the \"scaled\" CADD scores.");

		metadata.addAttributeMetaData(cadd_abs);
		metadata.addAttributeMetaData(cadd_scaled);

		return metadata;
	}

	@Override
	public String getDescription()
	{
		return "CADD is a tool for scoring the deleteriousness of single nucleotide variants as well as insertion/deletions variants in the human genome.\n"
				+ "\n"
				+ "While many variant annotation and scoring tools are around, most annotations tend to exploit a single information type (e.g. conservation) and/or are restricted in scope (e.g. to missense changes). Thus, a broadly applicable metric that objectively weights and integrates diverse information is needed. Combined Annotation Dependent Depletion (CADD) is a framework that integrates multiple annotations into one metric by contrasting variants that survived natural selection with simulated mutations.\n"
				+ "\n"
				+ "C-scores strongly correlate with allelic diversity, pathogenicity of both coding and non-coding variants, and experimentally measured regulatory effects, and also highly rank causal variants within individual genome sequences. Finally, C-scores of complex trait-associated variants from genome-wide association studies (GWAS) are significantly higher than matched controls and correlate with study sample size, likely reflecting the increased accuracy of larger GWAS.\n"
				+ "\n"
				+ "CADD can quantitatively prioritize functional, deleterious, and disease causal variants across a wide range of functional categories, effect sizes and genetic architectures and can be used prioritize causal variation in both research and clinical settings.";
	}

}
