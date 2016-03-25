package org.molgenis.data.annotation.entity.impl;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.STRING;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.TEXT;
import static org.molgenis.data.annotation.entity.impl.SnpEffRunner.LOF;
import static org.molgenis.data.annotation.entity.impl.SnpEffRunner.NMD;
import static org.molgenis.data.support.VcfEffectsMetaData.ANNOTATION;
import static org.molgenis.data.support.VcfEffectsMetaData.CDS_POSITION;
import static org.molgenis.data.support.VcfEffectsMetaData.C_DNA_POSITION;
import static org.molgenis.data.support.VcfEffectsMetaData.DISTANCE_TO_FEATURE;
import static org.molgenis.data.support.VcfEffectsMetaData.ERRORS;
import static org.molgenis.data.support.VcfEffectsMetaData.FEATURE_ID;
import static org.molgenis.data.support.VcfEffectsMetaData.FEATURE_TYPE;
import static org.molgenis.data.support.VcfEffectsMetaData.GENE_ID;
import static org.molgenis.data.support.VcfEffectsMetaData.GENE_NAME;
import static org.molgenis.data.support.VcfEffectsMetaData.HGVS_C;
import static org.molgenis.data.support.VcfEffectsMetaData.HGVS_P;
import static org.molgenis.data.support.VcfEffectsMetaData.PROTEIN_POSITION;
import static org.molgenis.data.support.VcfEffectsMetaData.PUTATIVE_IMPACT;
import static org.molgenis.data.support.VcfEffectsMetaData.RANK_TOTAL;
import static org.molgenis.data.support.VcfEffectsMetaData.TRANSCRIPT_BIOTYPE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AbstractExternalRepositoryAnnotator;
import org.molgenis.data.annotation.CmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Status;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Type;
import org.molgenis.data.annotation.impl.cmdlineannotatorsettingsconfigurer.SingleFileLocationCmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.annotator.websettings.SnpEffAnnotatorSettings;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.VcfEffectsMetaData;
import org.molgenis.data.vcf.VcfRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.w3c.dom.Attr;

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

	public enum Impact
	{
		MODIFIER, LOW, MODERATE, HIGH
	}

	@Autowired
	private SnpEffRunner snpEffRunner;

	@Autowired
	private Entity snpEffAnnotatorSettings;

	@Bean
	public RepositoryAnnotator snpEff()
	{
		return new SnpEffRepositoryAnnotator(snpEffRunner, snpEffAnnotatorSettings);
	}

	public static class SnpEffRepositoryAnnotator extends AbstractExternalRepositoryAnnotator
	{
		private final AnnotatorInfo info = AnnotatorInfo.create(Status.READY, Type.EFFECT_PREDICTION, NAME,
				"Genetic variant annotation and effect prediction toolbox. "
						+ "It annotates and predicts the effects of variants on genes (such as amino acid changes). "
						+ "This annotator creates a new table with SnpEff output to be able to store mutli-allelic and multigenic results. "
						+ "Results are NOT added to your existing dataset. "
						+ "SnpEff results can found in the <your_dataset_name>_EFFECTS. ",
				getOutputMetaData());
		private SnpEffRunner snpEffRunner;
		private Entity snpEffAnnotatorSettings;

		public SnpEffRepositoryAnnotator(SnpEffRunner snpEffRunner, Entity snpEffAnnotatorSettings)
		{
			this.snpEffRunner = snpEffRunner;
			this.snpEffAnnotatorSettings = snpEffAnnotatorSettings;
		}

		@Override
		public AnnotatorInfo getInfo()
		{
			return info;
		}

		@Override
		public Iterator<Entity> annotate(Iterable<Entity> source)
		{
			return snpEffRunner.getSnpEffects(source);
		}

		@Override
		public String canAnnotate(EntityMetaData repoMetaData)
		{
			return super.canAnnotate(repoMetaData);
		}

		@Override
		public EntityMetaData getOutputMetaData(EntityMetaData sourceEMD)
		{
			return new VcfEffectsMetaData(sourceEMD);
		}

		@Override
		public LinkedList<AttributeMetaData> getOrderedAttributeList(EntityMetaData sourceEMD) {
			return VcfEffectsMetaData.getOrderedAttributeList(sourceEMD);
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
		public List<AttributeMetaData> getRequiredAttributes()
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
		public boolean annotationDataExists()
		{
			return snpEffRunner.getSnpEffPath() != null;
		}

		@Override
		public CmdLineAnnotatorSettingsConfigurer getCmdLineAnnotatorSettingsConfigurer()
		{
			return new SingleFileLocationCmdLineAnnotatorSettingsConfigurer(
					SnpEffAnnotatorSettings.Meta.SNPEFF_JAR_LOCATION, snpEffAnnotatorSettings);
		}
	}

}
