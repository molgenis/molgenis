package org.molgenis.data.annotation.entity.impl;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.STRING;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.TEXT;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.annotation.AbstractRepositoryAnnotator;
import org.molgenis.data.annotation.CmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Status;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Type;
import org.molgenis.data.annotation.impl.cmdlineannotatorsettingsconfigurer.SingleFileLocationCmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.annotation.utils.JarRunner;
import org.molgenis.data.annotation.utils.JarRunnerImpl;
import org.molgenis.data.annotator.websettings.SnpEffAnnotatorSettings;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.Iterators;

/**
 * SnpEff annotator
 * <p>
 * SnpEff is a genetic variant annotation and effect prediction toolbox. It annotates and predicts the effects of
 * variants on genes (such as amino acid changes). see http://snpeff.sourceforge.net/
 * <p>
 * For this annotator to work SnpEff.jar must be present on the filesystem at the location defined by the
 * RuntimeProperty 'snpeff_jar_location'
 * <p>
 * <p>
 * new ANN field replacing EFF:
 * <p>
 * ANN=A|missense_variant|MODERATE|NEXN|NEXN|transcript|NM_144573.3|Coding|8/13|c.733G>A|p.Gly245Arg|1030/3389|733/2028|
 * 245/675||
 * <p>
 * <p>
 * -lof doesnt seem to work? would be great... http://snpeff.sourceforge.net/snpEff_lof_nmd.pdfs
 */
@Configuration
public class SnpEffAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(SnpEffAnnotator.class);
	public static final String NAME = "snpEff";

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

	public enum Impact
	{
		MODIFIER, LOW, MODERATE, HIGH
	}

	@Autowired
	private JarRunner jarRunner;

	@Autowired
	private Entity snpEffAnnotatorSettings;

	@Bean
	public RepositoryAnnotator snpEff()
	{
		return new SnpEffRepositoryAnnotator(snpEffAnnotatorSettings, jarRunner);
	}

	@Bean
	JarRunner jarRunner()
	{
		return new JarRunnerImpl();
	}

	/**
	 * Helper function to get gene name from entity
	 *
	 * @param entity
	 * @return
	 */
	public static String getGeneNameFromEntity(Entity entity)
	{
		String geneSymbol = null;
		if (entity.getString(GENE_NAME) != null)
		{
			geneSymbol = entity.getString(GENE_NAME);
		}
		if (geneSymbol == null)
		{
			String annField = entity.getString("ANN");
			if (annField != null)
			{
				// if the entity is annotated with the snpEff annotator the split is already done
				String[] split = annField.split("\\|", -1);
				// TODO: ask Joeri to explain this line
				if (split.length > 10)
				{
					// 3 is 'gene name'
					// TODO check if it should not be index 4 -> 'gene id'
					if (split[3].length() != 0)
					{
						geneSymbol = split[3];
					}
					else
					{
						// will happen a lot for whole genome sequencing data
						LOG.info("No gene symbol in ANN field for " + entity.toString());
					}

				}
			}
		}
		return geneSymbol;
	}

	public static class SnpEffRepositoryAnnotator extends AbstractRepositoryAnnotator
	{
		private static final String CHARSET = "UTF-8";
		private String snpEffPath;
		private final Entity pluginSettings;
		private final AnnotatorInfo info = AnnotatorInfo.create(Status.READY, Type.EFFECT_PREDICTION, NAME,
				"Genetic variant annotation and effect prediction toolbox. It annotates and predicts the effects of variants on genes (such as amino acid changes). ",
				getOutputMetaData());
		private final JarRunner jarRunner;

		public SnpEffRepositoryAnnotator(Entity pluginSettings, JarRunner jarRunner)
		{
			this.pluginSettings = pluginSettings;
			this.jarRunner = jarRunner;
		}

		@Override
		public AnnotatorInfo getInfo()
		{
			return info;
		}

		@Override
		public Iterator<Entity> annotate(Iterable<Entity> source)
		{
			File inputVcf = null;
			try
			{
				inputVcf = getInputVcfTempFile(source);
			}
			catch (IOException e)
			{
				throw new MolgenisDataException("Exception running SnpEff", e);

			}
			return annotateRepository(source, inputVcf);
		}

		public Iterator<Entity> annotateRepository(Iterable<Entity> source, final File inputVcf)
		{
			try
			{

				Iterator<Entity> it = source.iterator();
				if (!it.hasNext()) return Iterators.emptyIterator();

				List<String> params = Arrays.asList("-Xmx2g", getSnpEffPath(), "hg19", "-noStats", "-noLog", "-lof",
						"-canon", "-ud", "0", "-spliceSiteSize", "5");
				File outputVcf = jarRunner.runJar(NAME, params, inputVcf);
				// When vcf reader/writer can handle samples and SnpEff annotations just return a VcfRepository (with
				// inputVcf as input)
				// iterator here

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(outputVcf.getAbsolutePath()), CHARSET));

				return new Iterator<Entity>()
				{
					@Override
					public boolean hasNext()
					{
						boolean next = it.hasNext();
						if (!next)
						{
							IOUtils.closeQuietly(reader);
						}

						return next;
					}

					@Override
					public Entity next()
					{
						Entity entity = it.next();
						DefaultEntityMetaData meta = new DefaultEntityMetaData(entity.getEntityMetaData());
						info.getOutputAttributes().forEach(meta::addAttributeMetaData);
						Entity copy = new MapEntity(entity, meta);
						try
						{
							String line = readLine(reader);
							parseOutputLineToEntity(line, copy);
						}
						catch (IOException e)
						{
							throw new UncheckedIOException(e);
						}

						return copy;
					}

				};
			}
			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}
			catch (InterruptedException e)
			{
				throw new MolgenisDataException("Exception running SnpEff", e);
			}
		}

		private String readLine(BufferedReader reader) throws IOException
		{
			String line = reader.readLine();
			while ((line != null) && line.startsWith("#"))
			{
				line = reader.readLine();
			}

			return line;
		}

		public File getInputVcfTempFile(Iterable<Entity> source) throws IOException
		{
			File vcf = File.createTempFile(NAME, ".vcf");
			try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(vcf), CHARSET)))
			{

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
			}

			return vcf;
		}

		// FIXME: can be multiple? even when using canonical!
		// e.g.
		// ANN=G|intron_variant|MODIFIER|LOC101926913|LOC101926913|transcript|NR_110185.1|Noncoding|5/5|n.376+9526G>C||||||,G|non_coding_exon_variant|MODIFIER|LINC01124|LINC01124|transcript|NR_027433.1|Noncoding|1/1|n.590G>C||||||;
		public void parseOutputLineToEntity(String line, Entity entity)
		{
			String lof = "";
			String nmd = "";
			String[] fields = line.split("\t");
			String[] ann_field = fields[7].split(";");
			String[] annotation = ann_field[0].split(Pattern.quote("|"), -1);

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
		}

		@Override
		public List<AttributeMetaData> getOutputMetaData()
		{
			List<AttributeMetaData> attributes = new ArrayList<>();

			DefaultAttributeMetaData annotation = new DefaultAttributeMetaData(ANNOTATION, STRING);
			annotation.setDescription(
					"Annotated using Sequence Ontology terms. Multiple effects can be concatenated using ‘&’ (source:http://snpeff.sourceforge.net)");
			attributes.add(annotation);

			DefaultAttributeMetaData putative_impact = new DefaultAttributeMetaData(PUTATIVE_IMPACT, STRING);
			putative_impact.setDescription(
					" A simple estimation of putative impact / deleteriousness : {HIGH, MODERATE, LOW, MODIFIER}(source:http://snpeff.sourceforge.net)");
			attributes.add(putative_impact);

			DefaultAttributeMetaData gene_name = new DefaultAttributeMetaData(GENE_NAME, STRING);
			gene_name.setDescription(
					"Common gene name (HGNC). Optional: use closest gene when the variant is “intergenic”(source:http://snpeff.sourceforge.net)");
			attributes.add(gene_name);

			DefaultAttributeMetaData gene_id = new DefaultAttributeMetaData(GENE_ID, STRING);
			gene_id.setDescription("Gene ID");
			attributes.add(gene_id);

			DefaultAttributeMetaData feature_type = new DefaultAttributeMetaData(FEATURE_TYPE, STRING);
			feature_type.setDescription(
					"Which type of feature is in the next field (e.g. transcript, motif, miRNA, etc.). It is preferred to use Sequence Ontology (SO) terms, but ‘custom’ (user defined) are allowed. ANN=A|stop_gained|HIGH|||transcript|... Tissue specific features may include cell type / tissue information separated by semicolon e.g.: ANN=A|histone_binding_site|LOW|||H3K4me3:HeLa-S3|...\n"
							+ "Feature ID: Depending on the annotation, this may be: Transcript ID (preferably using version number), Motif ID, miRNA, ChipSeq peak, Histone mark, etc. Note: Some features may not have ID (e.g. histone marks from custom Chip-Seq experiments may not have a unique ID). (source:http://snpeff.sourceforge.net)");
			attributes.add(feature_type);

			DefaultAttributeMetaData feature_id = new DefaultAttributeMetaData(FEATURE_ID, STRING);
			feature_id.setDescription(
					"Depending on the annotation, this may be: Transcript ID (preferably using version number), Motif ID, miRNA, ChipSeq peak, Histone mark, etc. Note: Some features may not have ID (e.g. histone marks from custom Chip-Seq experiments may not have a unique ID).(source:http://snpeff.sourceforge.net)");
			attributes.add(feature_id);

			DefaultAttributeMetaData transcript_biotype = new DefaultAttributeMetaData(TRANSCRIPT_BIOTYPE, STRING);
			transcript_biotype.setDescription(
					"The bare minimum is at least a description on whether the transcript is {“Coding”, “Noncoding”}. Whenever possible, use ENSEMBL biotypes.(source:http://snpeff.sourceforge.net)");
			attributes.add(transcript_biotype);

			DefaultAttributeMetaData rank_total = new DefaultAttributeMetaData(RANK_TOTAL, STRING);
			rank_total.setDescription(
					"Exon or Intron rank / total number of exons or introns(source:http://snpeff.sourceforge.net)");
			attributes.add(rank_total);

			DefaultAttributeMetaData HGVS_c = new DefaultAttributeMetaData(HGVS_C, TEXT);
			HGVS_c.setDescription("Variant using HGVS notation (DNA level)(source:http://snpeff.sourceforge.net)");
			attributes.add(HGVS_c);

			DefaultAttributeMetaData HGVS_p = new DefaultAttributeMetaData(HGVS_P, STRING);
			HGVS_p.setDescription(
					"If variant is coding, this field describes the variant using HGVS notation (Protein level). Since transcript ID is already mentioned in ‘feature ID’, it may be omitted here.(source:http://snpeff.sourceforge.net)");
			attributes.add(HGVS_p);

			DefaultAttributeMetaData cDNA_position = new DefaultAttributeMetaData(C_DNA_POSITION, STRING);
			cDNA_position.setDescription(
					"Position in cDNA and trancript’s cDNA length (one based)(source:http://snpeff.sourceforge.net)");
			attributes.add(cDNA_position);

			DefaultAttributeMetaData CDS_position = new DefaultAttributeMetaData(CDS_POSITION, STRING);
			CDS_position.setDescription(
					"Position and number of coding bases (one based includes START and STOP codons)(source:http://snpeff.sourceforge.net)");
			attributes.add(CDS_position);

			DefaultAttributeMetaData Protein_position = new DefaultAttributeMetaData(PROTEIN_POSITION, STRING);
			Protein_position.setDescription("Position and number of AA (one based, including START, but not STOP)");
			attributes.add(Protein_position);

			DefaultAttributeMetaData Distance_to_feature = new DefaultAttributeMetaData(DISTANCE_TO_FEATURE, STRING);
			Distance_to_feature.setDescription(
					"All items in this field are options, so the field could be empty. Up/Downstream: Distance to first / last codon Intergenic: Distance to closest gene Distance to closest Intron boundary in exon (+/- up/downstream). If same, use positive number. Distance to closest exon boundary in Intron (+/- up/downstream) Distance to first base in MOTIF Distance to first base in miRNA Distance to exon-intron boundary in splice_site or splice _region ChipSeq peak: Distance to summit (or peak center) Histone mark / Histone state: Distance to summit (or peak center)(source:http://snpeff.sourceforge.net)");
			attributes.add(Distance_to_feature);

			DefaultAttributeMetaData Errors = new DefaultAttributeMetaData(ERRORS, STRING);
			Errors.setDescription(
					"Add errors, warnings oErrors, Warnings or Information messages: Add errors, warnings or r informative message that can affect annotation accuracy. It can be added using either ‘codes’ (as shown in column 1, e.g. W1) or ‘message types’ (as shown in column 2, e.g. WARNING_REF_DOES_NOT_MATCH_GENOME). All these errors, warnings or information messages messages are optional.(source:http://snpeff.sourceforge.net)");
			attributes.add(Errors);

			DefaultAttributeMetaData lof = new DefaultAttributeMetaData(LOF, STRING);
			lof.setDescription(
					"snpEff can estimate if a variant is deemed to have a loss of function on the protein.(source:http://snpeff.sourceforge.net)");
			attributes.add(lof);

			DefaultAttributeMetaData nmd = new DefaultAttributeMetaData(NMD, STRING);
			nmd.setDescription(
					"Nonsense mediate decay assessment. Some mutations may cause mRNA to be degraded thus not translated into a protein. NMD analysis marks mutations that are estimated to trigger nonsense mediated decay.(source:http://snpeff.sourceforge.net)");
			attributes.add(nmd);

			DefaultAttributeMetaData compoundAttributeMetaData = new DefaultAttributeMetaData(this.getFullName(),
					MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
			compoundAttributeMetaData.setLabel(this.getSimpleName());

			for (AttributeMetaData attributeMetaData : attributes)
			{
				compoundAttributeMetaData.addAttributePart(attributeMetaData);
			}

			return Collections.singletonList(compoundAttributeMetaData);
		}

		@Override
		public List<AttributeMetaData> getInputMetaData()
		{
			List<AttributeMetaData> attributes = new ArrayList<>();
			attributes.add(VcfRepository.CHROM_META);
			attributes.add(VcfRepository.POS_META);
			attributes.add(VcfRepository.REF_META);
			attributes.add(VcfRepository.ALT_META);

			return attributes;
		}

		@Override
		public String getSimpleName()
		{
			return NAME;
		}

		@Override
		protected boolean annotationDataExists()
		{
			return getSnpEffPath() != null;
		}

		private String getSnpEffPath()
		{
			if ((pluginSettings != null) && (snpEffPath == null)) {
				snpEffPath = RunAsSystemProxy
						.runAsSystem(() -> pluginSettings.getString(SnpEffAnnotatorSettings.Meta.SNPEFF_JAR_LOCATION));

				if (snpEffPath != null)
				{
					File snpEffFile = new File(snpEffPath);
					if (snpEffFile.exists() && snpEffFile.isFile())
					{
						LOG.info("SnpEff found at: " + snpEffFile.getAbsolutePath());
					}
					else
					{
						LOG.debug("SnpEff not found at: " + snpEffFile.getAbsolutePath());
						snpEffPath = null;
					}
				}
			}

			return snpEffPath;
		}

		@Override
		public CmdLineAnnotatorSettingsConfigurer getCmdLineAnnotatorSettingsConfigurer()
		{
			return new SingleFileLocationCmdLineAnnotatorSettingsConfigurer(
					SnpEffAnnotatorSettings.Meta.SNPEFF_JAR_LOCATION, pluginSettings);
		}
	}

}
