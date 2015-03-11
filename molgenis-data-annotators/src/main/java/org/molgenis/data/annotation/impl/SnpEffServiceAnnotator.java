package org.molgenis.data.annotation.impl;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.support.AnnotationServiceImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.server.MolgenisSimpleSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 
 * new ANN field replacing EFF:
 * 
 * ANN=A|missense_variant|MODERATE|NEXN|NEXN|transcript|NM_144573.3|Coding|8/13|c.733G>A|p.Gly245Arg|1030/3389|733/2028|
 * 245/675||
 * 
 * 
 * -lof doesnt seem to work? would be great... http://snpeff.sourceforge.net/snpEff_lof_nmd.pdf
 * 
 * 
 * */
@Component("SnpEffServiceAnnotator")
public class SnpEffServiceAnnotator implements RepositoryAnnotator, ApplicationListener<ContextRefreshedEvent>
{
	private static final Logger LOG = LoggerFactory.getLogger(SnpEffServiceAnnotator.class);
	public static final String SNPEFF_JAR_LOCATION_PROPERTY = "snpeff_jar_location";

	private final MolgenisSettings molgenisSettings;
	private final AnnotationService annotatorService;
	public static String snpEffPath = "";

	private static final String NAME = "SnpEff";
	public static final String ANNOTATION = "Annotation";
	public static final String PUTATIVE_IMPACT = "Putative_impact";
	public static final String GENE_NAME = "Gene_Name";
	public static final String GENE_ID = "Gene_ID";
	public static final String FEATURE_TYPE = "Feature_type";
	public static final String FEATURE_ID = "Feature_ID";
	public static final String TRANSCRIPT_BIOTYPE = "Transcript_biotype";
	public static final String RANK_TOTAL = "Rank_total";
	public static final String HGVS_C = "HGVS_c";
	public static final String HGVS_P = "HGVS_p";
	public static final String C_DNA_POSITION = "cDNA_position";
	public static final String CDS_POSITION = "CDS_position";
	public static final String PROTEIN_POSITION = "Protein_position";
	public static final String DISTANCE_TO_FEATURE = "Distance_to_feature";
	public static final String ERRORS = "Errors";
	public static final String LOF = "LOF";
	public static final String NMD = "NMD";
	private DataService dataService = null;

	public enum impact
	{
		MODIFIER, LOW, MODERATE, HIGH
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		annotatorService.addAnnotator(this);
	}

	@Autowired
	public SnpEffServiceAnnotator(MolgenisSettings molgenisSettings, AnnotationService annotatorService,
			DataService dataService) throws IOException
	{
		this.molgenisSettings = molgenisSettings;
		this.annotatorService = annotatorService;
		this.dataService = dataService;
	}

	public SnpEffServiceAnnotator(File snpEffLocation, File inputVcfFile, File outputVCFFile) throws Exception
	{
		this.molgenisSettings = new MolgenisSimpleSettings();
		molgenisSettings.setProperty(SNPEFF_JAR_LOCATION_PROPERTY, snpEffLocation.getAbsolutePath());
		this.annotatorService = new AnnotationServiceImpl();

		checkSnpEffPath();
		runSnpEff(inputVcfFile, outputVCFFile);

		System.out.println("All done!");
	}

	@Override
	public String getSimpleName()
	{
		return NAME;
	}

	@Override
	public String getFullName()
	{
		return getSimpleName();
	}

	@Override
	public String getDescription()
	{
		return "SnpEff is a variant annotation and effect prediction tool. It annotates and predicts the effects of genetic variants (such as amino acid changes).(source:http://snpeff.sourceforge.net)";
	}

	private boolean checkSnpEffPath()
	{
		boolean result = false;
		snpEffPath = molgenisSettings.getProperty(SNPEFF_JAR_LOCATION_PROPERTY);
		if (snpEffPath != null)
		{
			File snpEffpath = new File(snpEffPath);
			if (snpEffpath.exists() && snpEffpath.isFile())
			{
				LOG.info("SnpEff found at: " + snpEffpath.getAbsolutePath());
				result = true;
			}
			else
			{
				LOG.error("SnpEff not found at: " + snpEffpath.getAbsolutePath());
			}
		}
		return result;
	}

	@Override
	public Iterator<Entity> annotate(Iterable<Entity> source)
	{
		String inputTempFileName = UUID.randomUUID().toString();
		String outputTempFileName = UUID.randomUUID().toString();
		File inputTempFile;
		File outputTempFile;
		List<Entity> results = new ArrayList<>();// FIXME: everything to a List is not very nice!

		try
		{
			outputTempFile = File.createTempFile(outputTempFileName, ".vcf");
			inputTempFile = getInputTempFile(source, inputTempFileName);

			runSnpEff(inputTempFile, outputTempFile);

			Reader reader = new InputStreamReader(new FileInputStream(outputTempFile.getAbsolutePath()), "utf-8");
			BufferedReader br = new BufferedReader(reader);

			String line;
			Iterator<Entity> entityIterator = source.iterator();
			while ((line = br.readLine()) != null)
			{
				if (!line.startsWith("##"))
				{
					Entity entity = entityIterator.next();
					Entity resultEntity = parseOutputLineToEntity(line, entity.getEntityMetaData().getName());
					results.add(resultEntity);
				}
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException("Could not read or create an intermediate file during annotation", e);
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException("Exception during annotation", e);
		}
		return results.iterator();// FIXME: not nice!
	}

	public void runSnpEff(File tempInput, File tempOutput) throws IOException, InterruptedException
	{
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "-Xmx2g", snpEffPath, "hg19", "-noStats", "-lof",
				"-canon", "-ud", "0", "-spliceSiteSize", "5", tempInput.getAbsolutePath());
		pb.redirectOutput(tempOutput);
		// Error logging to standard logging.
		pb.redirectError(ProcessBuilder.Redirect.INHERIT);

		Process p = pb.start();
		p.waitFor();
	}

	public File getInputTempFile(Iterable<Entity> source, String tempInputFileName) throws IOException
	{
		File tempInput;
		tempInput = File.createTempFile(tempInputFileName, ".vcf");

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempInput), "UTF-8"));
		for (Entity entity : source)
		{
			StringBuilder builder = new StringBuilder();
			builder.append(entity.getString(VcfRepository.CHROM));
			builder.append("\t");
			builder.append(entity.getString(VcfRepository.POS));
			builder.append("\t.\t");
			builder.append(entity.getString(VcfRepository.REF));
			builder.append("\t");
			builder.append(entity.getString(VcfRepository.ALT));
			builder.append("\n");
			bw.write(builder.toString());
		}
		bw.close();
		return tempInput;
	}

	// FIXME: can be multiple? even when using canonical!
	// e.g.
	// ANN=G|intron_variant|MODIFIER|LOC101926913|LOC101926913|transcript|NR_110185.1|Noncoding|5/5|n.376+9526G>C||||||,G|non_coding_exon_variant|MODIFIER|LINC01124|LINC01124|transcript|NR_027433.1|Noncoding|1/1|n.590G>C||||||;
	public Entity parseOutputLineToEntity(String line, String entityName)
	{
		String lof = "";
		String nmd = "";
		String[] fields = line.split("\t");
		String[] ann_field = fields[7].split(";");
		String[] annotation = ann_field[0].split(Pattern.quote("|"), -1);
		QueryRule chromRule = new QueryRule(VcfRepository.CHROM, QueryRule.Operator.EQUALS, fields[0]);
		Query query = new QueryImpl(chromRule).and().eq(VcfRepository.POS, fields[1]);
		Entity entity = dataService.findOne(entityName, query);
		if (ann_field.length > 1)
		{
			if (ann_field[1].startsWith("LOF="))
			{
				lof = ann_field[1];
			}
			else if (ann_field[1].startsWith("NMD="))
			{
				nmd = ann_field[1];
			}
		}
		if (ann_field.length > 2)
		{
			if (ann_field[2].startsWith("LOF="))
			{
				lof = ann_field[2];
			}
			else if (ann_field[2].startsWith("NMD="))
			{
				nmd = ann_field[2];
			}
		}

		entity.set(ANNOTATION, annotation[1]);
		entity.set(PUTATIVE_IMPACT, annotation[2]);
		entity.set(GENE_NAME, annotation[3]);
		entity.set(GENE_ID, annotation[4]);
		entity.set(FEATURE_TYPE, annotation[5]);
		entity.set(FEATURE_ID, annotation[6]);
		entity.set(TRANSCRIPT_BIOTYPE, annotation[7]);
		entity.set(RANK_TOTAL, annotation[8]);
		entity.set(HGVS_C, annotation[9]);
		entity.set(HGVS_P, annotation[10]);
		entity.set(C_DNA_POSITION, annotation[11]);
		entity.set(CDS_POSITION, annotation[12]);
		entity.set(PROTEIN_POSITION, annotation[13]);
		entity.set(DISTANCE_TO_FEATURE, annotation[14]);
		entity.set(ERRORS, annotation[15]);
		entity.set(LOF, lof.replace("LOF=", ""));
		entity.set(NMD, nmd.replace("NMD=", ""));

		return entity;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);

		DefaultAttributeMetaData annotation = new DefaultAttributeMetaData(ANNOTATION,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		annotation
				.setDescription("Annotated using Sequence Ontology terms. Multiple effects can be concatenated using ‘&’ (source:http://snpeff.sourceforge.net)");
		metadata.addAttributeMetaData(annotation);

		DefaultAttributeMetaData putative_impact = new DefaultAttributeMetaData(PUTATIVE_IMPACT,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		putative_impact
				.setDescription(" A simple estimation of putative impact / deleteriousness : {HIGH, MODERATE, LOW, MODIFIER}(source:http://snpeff.sourceforge.net)");
		metadata.addAttributeMetaData(putative_impact);

		DefaultAttributeMetaData gene_name = new DefaultAttributeMetaData(GENE_NAME,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		gene_name
				.setDescription("Common gene name (HGNC). Optional: use closest gene when the variant is “intergenic”(source:http://snpeff.sourceforge.net)");
		metadata.addAttributeMetaData(gene_name);

		DefaultAttributeMetaData gene_id = new DefaultAttributeMetaData(GENE_ID,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		gene_id.setDescription("Gene ID");
		metadata.addAttributeMetaData(gene_id);

		DefaultAttributeMetaData feature_type = new DefaultAttributeMetaData(FEATURE_TYPE,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		feature_type
				.setDescription("Which type of feature is in the next field (e.g. transcript, motif, miRNA, etc.). It is preferred to use Sequence Ontology (SO) terms, but ‘custom’ (user defined) are allowed. ANN=A|stop_gained|HIGH|||transcript|... Tissue specific features may include cell type / tissue information separated by semicolon e.g.: ANN=A|histone_binding_site|LOW|||H3K4me3:HeLa-S3|...\n"
						+ "Feature ID: Depending on the annotation, this may be: Transcript ID (preferably using version number), Motif ID, miRNA, ChipSeq peak, Histone mark, etc. Note: Some features may not have ID (e.g. histone marks from custom Chip-Seq experiments may not have a unique ID). (source:http://snpeff.sourceforge.net)");
		metadata.addAttributeMetaData(feature_type);

		DefaultAttributeMetaData feature_id = new DefaultAttributeMetaData(FEATURE_ID,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		feature_id
				.setDescription("Depending on the annotation, this may be: Transcript ID (preferably using version number), Motif ID, miRNA, ChipSeq peak, Histone mark, etc. Note: Some features may not have ID (e.g. histone marks from custom Chip-Seq experiments may not have a unique ID).(source:http://snpeff.sourceforge.net)");
		metadata.addAttributeMetaData(feature_id);

		DefaultAttributeMetaData transcript_biotype = new DefaultAttributeMetaData(TRANSCRIPT_BIOTYPE,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		transcript_biotype
				.setDescription("The bare minimum is at least a description on whether the transcript is {“Coding”, “Noncoding”}. Whenever possible, use ENSEMBL biotypes.(source:http://snpeff.sourceforge.net)");
		metadata.addAttributeMetaData(transcript_biotype);

		DefaultAttributeMetaData rank_total = new DefaultAttributeMetaData(RANK_TOTAL,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		rank_total
				.setDescription("Exon or Intron rank / total number of exons or introns(source:http://snpeff.sourceforge.net)");
		metadata.addAttributeMetaData(rank_total);

		DefaultAttributeMetaData HGVS_c = new DefaultAttributeMetaData(HGVS_C, MolgenisFieldTypes.FieldTypeEnum.STRING);
		HGVS_c.setDescription("Variant using HGVS notation (DNA level)(source:http://snpeff.sourceforge.net)");
		metadata.addAttributeMetaData(HGVS_c);

		DefaultAttributeMetaData HGVS_p = new DefaultAttributeMetaData(HGVS_P, MolgenisFieldTypes.FieldTypeEnum.STRING);
		HGVS_p.setDescription("If variant is coding, this field describes the variant using HGVS notation (Protein level). Since transcript ID is already mentioned in ‘feature ID’, it may be omitted here.(source:http://snpeff.sourceforge.net)");
		metadata.addAttributeMetaData(HGVS_p);

		DefaultAttributeMetaData cDNA_position = new DefaultAttributeMetaData(C_DNA_POSITION,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		cDNA_position
				.setDescription("Position in cDNA and trancript’s cDNA length (one based)(source:http://snpeff.sourceforge.net)");
		metadata.addAttributeMetaData(cDNA_position);

		DefaultAttributeMetaData CDS_position = new DefaultAttributeMetaData(CDS_POSITION,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		CDS_position
				.setDescription("Position and number of coding bases (one based includes START and STOP codons)(source:http://snpeff.sourceforge.net)");
		metadata.addAttributeMetaData(CDS_position);

		DefaultAttributeMetaData Protein_position = new DefaultAttributeMetaData(PROTEIN_POSITION,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		Protein_position.setDescription("Position and number of AA (one based, including START, but not STOP)");
		metadata.addAttributeMetaData(Protein_position);

		DefaultAttributeMetaData Distance_to_feature = new DefaultAttributeMetaData(DISTANCE_TO_FEATURE,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		Distance_to_feature
				.setDescription("All items in this field are options, so the field could be empty. Up/Downstream: Distance to first / last codon Intergenic: Distance to closest gene Distance to closest Intron boundary in exon (+/- up/downstream). If same, use positive number. Distance to closest exon boundary in Intron (+/- up/downstream) Distance to first base in MOTIF Distance to first base in miRNA Distance to exon-intron boundary in splice_site or splice _region ChipSeq peak: Distance to summit (or peak center) Histone mark / Histone state: Distance to summit (or peak center)(source:http://snpeff.sourceforge.net)");
		metadata.addAttributeMetaData(Distance_to_feature);

		DefaultAttributeMetaData Errors = new DefaultAttributeMetaData(ERRORS, MolgenisFieldTypes.FieldTypeEnum.STRING);
		Errors.setDescription("Add errors, warnings oErrors, Warnings or Information messages: Add errors, warnings or r informative message that can affect annotation accuracy. It can be added using either ‘codes’ (as shown in column 1, e.g. W1) or ‘message types’ (as shown in column 2, e.g. WARNING_REF_DOES_NOT_MATCH_GENOME). All these errors, warnings or information messages messages are optional.(source:http://snpeff.sourceforge.net)");
		metadata.addAttributeMetaData(Errors);

		DefaultAttributeMetaData lof = new DefaultAttributeMetaData(LOF, MolgenisFieldTypes.FieldTypeEnum.STRING);
		lof.setDescription("snpEff can estimate if a variant is deemed to have a loss of function on the protein.(source:http://snpeff.sourceforge.net)");
		metadata.addAttributeMetaData(lof);

		DefaultAttributeMetaData nmd = new DefaultAttributeMetaData(NMD, MolgenisFieldTypes.FieldTypeEnum.STRING);
		nmd.setDescription("Nonsense mediate decay assessment. Some mutations may cause mRNA to be degraded thus not translated into a protein. NMD analysis marks mutations that are estimated to trigger nonsense mediated decay.(source:http://snpeff.sourceforge.net)");
		metadata.addAttributeMetaData(nmd);

		return metadata;
	}

	@Override
	public EntityMetaData getInputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		metadata.addAttributeMetaData(VcfRepository.CHROM_META);
		metadata.addAttributeMetaData(VcfRepository.POS_META);
		metadata.addAttributeMetaData(VcfRepository.REF_META);
		metadata.addAttributeMetaData(VcfRepository.ALT_META);

		return metadata;
	}

	@Override
	public String canAnnotate(EntityMetaData repoMetaData)
	{
		Iterable<AttributeMetaData> annotatorAttributes = getInputMetaData().getAttributes();
		for (AttributeMetaData annotatorAttribute : annotatorAttributes)
		{
			// one of the needed attributes not present? we can not annotate
			if (repoMetaData.getAttribute(annotatorAttribute.getName()) == null)
			{
				return "missing required attribute";
			}

			// one of the needed attributes not of the correct type? we can not annotate
			if (!repoMetaData.getAttribute(annotatorAttribute.getName()).getDataType()
					.equals(annotatorAttribute.getDataType()))
			{
				return "a required attribute has the wrong datatype";
			}

			// Are the runtime property files not available, or is a webservice down? we can not annotate
			if (!checkSnpEffPath())
			{
				return "SnpEff not found";
			}
		}

		return "true";
	}

}
