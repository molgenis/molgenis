package org.molgenis.data.annotation.impl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.VariantAnnotator;
import org.molgenis.data.annotation.cmd.AnnotatorInfo;
import org.molgenis.data.annotation.utils.AnnotatorUtils;
import org.molgenis.data.annotation.utils.TabixReader;
import org.molgenis.data.support.AnnotationServiceImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.server.MolgenisSimpleSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

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

	private final MolgenisSettings molgenisSettings;
	private final AnnotationService annotatorService;
	
	@Override
	public AnnotatorInfo.status getStatus(){
		return AnnotatorInfo.status.BETA;
	}

	@Override
	public AnnotatorInfo.type getType(){
		return AnnotatorInfo.type.PATHOGENICITY_ESTIMATE;
	}
	
	@Override
	public String getCode()
	{
		return "cadd";
	}

	// the cadd service returns these two values
	// must be compatible with VCF format, ie no funny characters
	public static final String CADD_SCALED_LABEL = "CADDSCALED";
	public static final String CADD_ABS_LABEL = "CADDABS";
    public static final String CADD_SCALED = VcfRepository.getInfoPrefix()+"CADDSCALED";
    public static final String CADD_ABS = VcfRepository.getInfoPrefix()+"CADDABS";

	private static final String NAME = "CADD";

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

    private volatile TabixReader tabixReader;

	@Autowired
	public CaddServiceAnnotator(MolgenisSettings molgenisSettings, AnnotationService annotatorService)
			throws IOException
	{
		this.molgenisSettings = molgenisSettings;
		this.annotatorService = annotatorService;
	}

	public CaddServiceAnnotator(File caddTsvGzFile, File inputVcfFile, File outputVCFFile) throws Exception
	{

		this.molgenisSettings = new MolgenisSimpleSettings();
		molgenisSettings.setProperty(CADD_FILE_LOCATION_PROPERTY, caddTsvGzFile.getAbsolutePath());

		this.annotatorService = new AnnotationServiceImpl();

		tabixReader = new TabixReader(molgenisSettings.getProperty(CADD_FILE_LOCATION_PROPERTY));
		checkTabixReader();

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
		return new File(molgenisSettings.getProperty(CADD_FILE_LOCATION_PROPERTY)).exists();
	}

	@Override
	public String getSimpleName()
	{
		return NAME;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	{
		checkTabixReader();
        Map<String, Object> resultMap = annotateEntityWithCADD(entity.getString(VcfRepository.CHROM), entity.getLong(VcfRepository.POS),
				entity.getString(VcfRepository.REF), entity.getString(VcfRepository.ALT));
		return Collections.<Entity> singletonList(AnnotatorUtils.getAnnotatedEntity(this, entity, resultMap));
	}

	private synchronized Map<String, Object> annotateEntityWithCADD(String chromosome, Long position, String reference, String alternative) throws IOException, InterruptedException
	{
		TabixReader.Iterator tabixIterator = null;
		try
		{
			tabixIterator = tabixReader.query(chromosome + ":" + position + "-" + position);
		}
		catch(Exception e)
		{
			LOG.error("Something went wrong (chromosome not in data?) when querying CADD tabix file for " + chromosome + " POS: " + position + " REF: " + reference
					+ " ALT: " + alternative + "! skipping...");
		}

        Double caddAbs = null;
        Double caddScaled = null;

        // TabixReaderIterator does not have a hasNext();
        boolean done = tabixIterator == null;
        int i = 0;

        Map<String, Object> resultMap = new HashMap<String, Object>();

      //get line(s) from data, we expect 0 (no hit), 1 (specialized files such as 1000G) or 3 (whole genome file), so 0 to 3 hits
        while (!done)
        {
            String line = null;

            try
    		{
    			line = tabixIterator.next();
    		}
    		catch(net.sf.samtools.SAMFormatException sfx)
    		{
    			LOG.error("Bad GZIP file for CHROM: " + chromosome + " POS: " + position + " REF: " + reference
    					+ " ALT: " + alternative + " LINE: " + line);
    			throw sfx;
    		}
            catch(NullPointerException npe)
    		{
    			LOG.info("No data for CHROM: " + chromosome + " POS: " + position + " REF: " + reference + " ALT: " + alternative + " LINE: " + line);
    		}

            if (line != null)
            {
                String[] split = null;
                i++;
                split = line.split("\t");
                if (split.length != 6)
                {
                    LOG.error("bad CADD output for CHROM: " + chromosome + " POS: " + position + " REF: " + reference
                            + " ALT: " + alternative + " LINE: " + line);
                    continue;
                }
                if (split[2].equals(reference) && split[3].equals(alternative))
                {
					LOG.info("CADD scores found for CHROM: " + chromosome + " POS: " + position + " REF: " + reference
							+ " ALT: " + alternative + " LINE: " + line);
                    caddAbs = Double.parseDouble(split[4]);
                    caddScaled = Double.parseDouble(split[5]);
                    done = true;
                }
                // In some cases, the ref and alt are swapped. If this is the case, the initial if statement above will
                // fail, we can just check whether such a swapping has occured
                else if (split[3].equals(reference) && split[2].equals(alternative))
                {
                    LOG.info("CADD scores found [swapped REF and ALT!] for CHROM: " + chromosome + " POS: " + position
                            + " REF: " + reference + " ALT: " + alternative + " LINE: " + line);
                    caddAbs = Double.parseDouble(split[4]);
                    caddScaled = Double.parseDouble(split[5]);
                    done = true;
                }
                else
                {
                    if (i > 3)
                    {
                        LOG.warn("More than 3 hits in the CADD file! for CHROM: " + chromosome + " POS: " + position
                                + " REF: " + reference + " ALT: " + alternative);
                    }
                }
            }
            else //case: line == null
            {
                LOG.warn("No hit found in CADD file for CHROM: " + chromosome + " POS: " + position + " REF: " + reference + " ALT: " + alternative);
                done = true;
            }
        }

        resultMap.put(CADD_ABS, caddAbs);
        resultMap.put(CADD_SCALED, caddScaled);
        return resultMap;
	}

	/**
	 * Makes sure the tabixReader exists.
	 */
	private void checkTabixReader() throws IOException
	{
		if (tabixReader == null)
		{
			synchronized (this)
			{
				if (tabixReader == null)
				{
					tabixReader = new TabixReader(molgenisSettings.getProperty(CADD_FILE_LOCATION_PROPERTY));
				}
			}
		}
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		DefaultAttributeMetaData cadd_abs = new DefaultAttributeMetaData(CADD_ABS, FieldTypeEnum.DECIMAL).setLabel(CADD_ABS_LABEL).setDescription("\"Raw\" CADD scores come straight from the model, and are interpretable as the extent to which the annotation profile for a given variant suggests that that variant is likely to be \"observed\" (negative values) vs \"simulated\" (positive values). These values have no absolute unit of meaning and are incomparable across distinct annotation combinations, training sets, or model parameters. However, raw values do have relative meaning, with higher values indicating that a variant is more likely to be simulated (or \"not observed\") and therefore more likely to have deleterious effects.(source: http://cadd.gs.washington.edu/info)");
		DefaultAttributeMetaData cadd_scaled = new DefaultAttributeMetaData(CADD_SCALED, FieldTypeEnum.DECIMAL).setLabel(CADD_SCALED_LABEL).setDescription("Since the raw scores do have relative meaning, one can take a specific group of variants, define the rank for each variant within that group, and then use that value as a \"normalized\" and now externally comparable unit of analysis. In our case, we scored and ranked all ~8.6 billion SNVs of the GRCh37/hg19 reference and then \"PHRED-scaled\" those values by expressing the rank in order of magnitude terms rather than the precise rank itself. For example, reference genome single nucleotide variants at the 10th-% of CADD scores are assigned to CADD-10, top 1% to CADD-20, top 0.1% to CADD-30, etc. The results of this transformation are the \"scaled\" CADD scores.(source: http://cadd.gs.washington.edu/info)");

		metadata.addAttributeMetaData(cadd_abs);
		metadata.addAttributeMetaData(cadd_scaled);

		return metadata;
	}

    @Override
    public String getDescription(){
        return "CADD is a tool for scoring the deleteriousness of single nucleotide variants as well as insertion/deletions variants in the human genome.\n" +
                "\n" +
                "While many variant annotation and scoring utils are around, most annotations tend to exploit a single information type (e.g. conservation) and/or are restricted in scope (e.g. to missense changes). Thus, a broadly applicable metric that objectively weights and integrates diverse information is needed. Combined Annotation Dependent Depletion (CADD) is a framework that integrates multiple annotations into one metric by contrasting variants that survived natural selection with simulated mutations.\n" +
                "\n" +
                "C-scores strongly correlate with allelic diversity, pathogenicity of both coding and non-coding variants, and experimentally measured regulatory effects, and also highly rank causal variants within individual genome sequences. Finally, C-scores of complex trait-associated variants from genome-wide association studies (GWAS) are significantly higher than matched controls and correlate with study sample size, likely reflecting the increased accuracy of larger GWAS.\n" +
                "\n" +
                "CADD can quantitatively prioritize functional, deleterious, and disease causal variants across a wide range of functional categories, effect sizes and genetic architectures and can be used prioritize causal variation in both research and clinical settings. (source: http://cadd.gs.washington.edu/info)";
    }

}
