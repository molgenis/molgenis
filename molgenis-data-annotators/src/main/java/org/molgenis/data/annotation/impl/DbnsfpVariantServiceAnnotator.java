package org.molgenis.data.annotation.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.VariantAnnotator;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * <p>
 * This class uses a chromosome to approach a dbNSFP chromosome variant file, it then checks all locations recorded for
 * a chromosome in the data set, for every line, and uses a matching line to add annotation to the data set variant.
 * </p>
 * 
 * <p>
 * <b>dbNSFP variant returns:</b>
 * 
 * chr pos(1-coor) ref alt aaref aaalt hg18_pos(1-coor) genename Uniprot_acc Uniprot_id Uniprot_aapos Interpro_domain
 * cds_strand refcodon SLR_test_statistic codonpos fold-degenerate Ancestral_allele Ensembl_geneid Ensembl_transcriptid
 * aapos aapos_SIFT aapos_FATHMM SIFT_score SIFT_score_converted SIFT_pred Polyphen2_HDIV_score Polyphen2_HDIV_pred
 * Polyphen2_HVAR_score Polyphen2_HVAR_pred LRT_score LRT_score_converted LRT_pred MutationTaster_score
 * MutationTaster_score_converted MutationTaster_pred MutationAssessor_score MutationAssessor_score_converted
 * MutationAssessor_pred FATHMM_score FATHMM_score_converted FATHMM_pred RadialSVM_score RadialSVM_score_converted
 * RadialSVM_pred LR_score LR_pred Reliability_index GERP++_NR GERP++_RS phyloP 29way_pi 29way_logOdds LRT_Omega
 * UniSNP_ids 1000Gp1_AC 1000Gp1_AF 1000Gp1_AFR_AC 1000Gp1_AFR_AF 1000Gp1_EUR_AC 1000Gp1_EUR_AF 1000Gp1_AMR_AC
 * 1000Gp1_AMR_AF 1000Gp1_ASN_AC 1000Gp1_ASN_AF ESP6500_AA_AF ESP6500_EA_AF
 * </p>
 * 
 * @author mdehaan
 * 
 * @version dbNSFP version 2.3 downloaded January 26, 2014
 * 
 * */
@Component("dbnsfpVariantService")
public class DbnsfpVariantServiceAnnotator extends VariantAnnotator
{
	private final MolgenisSettings molgenisSettings;
	private final AnnotationService annotatorService;

	private static final String NAME = "dbNSFP-Variant";
	public static final String CHROMOSOME_FILE_LOCATION_PROPERTY = "dbsnfp_variant_location";

	static final String CHR = "chr";
	static final String POS_1_COOR = "pos(1-coor)";
	static final String REF = "ref";
	static final String ALT = "alt";
	static final String AAREF = "aaref";
	static final String AAALT = "aaalt";
	static final String HG18_POS_1_COOR = "hg18_pos(1-coor)";
	static final String GENENAME = "genename";
	static final String UNIPROT_ACC = "Uniprot_acc";
	static final String UNIPROT_ID = "Uniprot_id";
	static final String UNIPROT_AAPOS = "Uniprot_aapos";
	static final String INTERPRO_DOMAIN = "Interpro_domain";
	static final String CDS_STRAND = "cds_strand";
	static final String REFCODON = "refcodon";
	static final String SLR_TEST_STATISTIC = "SLR_test_statistic ";
	static final String CODONPOS = "codonpos";
	static final String FOLD_DEGENERATE = "fold-degenerate";
	static final String ANCESTRAL_ALLELE = "Ancestral_allele";
	static final String ENSEMBL_GENEID = "Ensembl_geneid";
	static final String ENSEMBL_TRANSCRIPTID = "Ensembl_transcriptid";
	static final String AAPOS = "aapos";
	static final String AAPOS_SIFT = "aapos_SIFT";
	static final String AAPOS_FATHMM = "aapos_FATHMM";
	static final String SIFT_SCORE = "SIFT_score";
	static final String SIFT_SCORE_CONVERTED = "SIFT_score_converted";
	static final String SIFT_PRED = "SIFT_pred";
	static final String POLYPHEN2_HDIV_SCORE = "Polyphen2_HDIV_score";
	static final String POLYPHEN2_HDIV_PRED = "Polyphen2_HDIV_pred";
	static final String POLYPHEN2_HVAR_SCORE = "Polyphen2_HVAR_score";
	static final String POLYPHEN2_HVAR_PRED = "Polyphen2_HVAR_pred";
	static final String LRT_SCORE = "LRT_score";
	static final String LRT_SCORE_CONVERTED = "LRT_score_converted";
	static final String LRT_PRED = "LRT_pred";
	static final String MUTATIONTASTER_SCORE = "MutationTaster_score";
	static final String MUTATIONTASTER_SCORE_CONVERTED = "MutationTaster_score_converted";
	static final String MUTATIONTASTER_PRED = "MutationTaster_pred";
	static final String MUTATIONASSESSOR_SCORE = "MutationAssessor_score";
	static final String MUTATIONASSESSOR_SCORE_CONVERTED = "MutationAssessor_score_converted";
	static final String MUTATIONASSESSOR_PRED = "MutationAssessor_pred";
	static final String FATHMM_SCORE = "FATHMM_score";
	static final String FATHMM_SCORE_CONVERTED = "FATHMM_score_converted";
	static final String FATHMM_PRED = "FATHMM_pred";
	static final String RADIALSVM_SCORE = "RadialSVM_score";
	static final String RADIALSVM_SCORE_CONVERTED = "RadialSVM_score_converted";
	static final String RADIALSVM_PRED = "RadialSVM_pred";
	static final String LR_SCORE = "LR_score";
	static final String LR_PRED = "LR_pred";
	static final String RELIABILITY_INDEX = "Reliability_index";
	static final String GERP_NR = "GERP++_NR";
	static final String GERP_RS = "GERP++_RS";
	static final String PHYLOP = "phyloP";
	static final String TWONINE_WAY_PI = "29way_pi";
	static final String TWONINE_WAY_LOGODDS = "29way_logOdds";
	static final String LRT_OMEGA = "LRT_Omega";
	static final String UNISNP_IDS = "UniSNP_ids";
	static final String THOUSAND_GP1_AC = "1000Gp1_AC";
	static final String THOUSAND_GP1_AF = "1000Gp1_AF";
	static final String THOUSAND_GP1_AFR_AC = "1000Gp1_AFR_AC";
	static final String THOUSAND_GP1_AFR_AF = "1000Gp1_AFR_AF";
	static final String THOUSAND_GP1_EUR_AC = "1000Gp1_EUR_AC";
	static final String THOUSAND_GP1_EUR_AF = "1000Gp1_EUR_AF";
	static final String THOUSAND_GP1_AMR_AC = "1000Gp1_AMR_AC";
	static final String THOUSAND_GP1_AMR_AF = "1000Gp1_AMR_AF";
	static final String THOUSAND_GP1_ASN_AC = "1000Gp1_ASN_AC";
	static final String THOUSAND_GP1_ASN_AF = "1000Gp1_ASN_AF";
	static final String ESP6500_AA_AF = "ESP6500_AA_AF";
	static final String ESP6500_EA_AF = "ESP6500_EA_AF";

	@Autowired
	public DbnsfpVariantServiceAnnotator(MolgenisSettings molgenisSettings, AnnotationService annotatorService)
	{
		this.molgenisSettings = molgenisSettings;
		this.annotatorService = annotatorService;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		annotatorService.addAnnotator(this);
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public boolean annotationDataExists()
	{
		return new File(molgenisSettings.getProperty(CHROMOSOME_FILE_LOCATION_PROPERTY)).exists();
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException
	{
		List<Entity> results = new ArrayList<Entity>();
		Map<String, List<String[]>> chromosomeMap = new HashMap<String, List<String[]>>();

		List<String[]> listOfTriplets = new ArrayList<String[]>();

		// a triplet contains position, reference and alternative
		String[] triplets = new String[3];

		String chromosome = entity.getString(CHROMOSOME);

		triplets[0] = entity.getLong(POSITION).toString();
		triplets[1] = entity.getString(REFERENCE);
		triplets[2] = entity.getString(ALTERNATIVE);

		listOfTriplets.add(triplets);

		// Now a chromosome + a triplet belong to one Entity
		// Entities with the same chromosome are added to one key
		if (chromosomeMap.containsKey(chromosome))
		{
			chromosomeMap.get(chromosome).addAll(listOfTriplets);
		}
		else
		{
			chromosomeMap.put(chromosome, listOfTriplets);
		}

		for (Entry<String, List<String[]>> entry : chromosomeMap.entrySet())
		{
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(
					molgenisSettings.getProperty(CHROMOSOME_FILE_LOCATION_PROPERTY)), Charset.forName("UTF-8")));

			try
			{
				List<String[]> charArraysForThisChromosome = entry.getValue();

				String line = "";

				fileReader: while (bufferedReader.ready())
				{
					line = bufferedReader.readLine();
					if (line.startsWith("#"))
					{
						continue fileReader;
					}

					String[] lineSplit = line.split("\t");

					charArrayReader: for (int i = 0; i < charArraysForThisChromosome.size(); i++)
					{
						Long position = Long.parseLong(charArraysForThisChromosome.get(i)[0]);

						if (lineSplit[1].equals(position.toString()))
						{
							String reference = charArraysForThisChromosome.get(i)[1];
							String alternative = charArraysForThisChromosome.get(i)[2];

							if (lineSplit[2].toUpperCase().equals(reference.toUpperCase())
									&& lineSplit[3].toUpperCase().equals(alternative.toUpperCase()))
							{
								int lineSplitIndex = 0;

								HashMap<String, Object> resultMap = new HashMap<String, Object>();
								Iterable<AttributeMetaData> featureList = getOutputMetaData().getAtomicAttributes();

								for (AttributeMetaData feature : featureList)
								{
									if (feature != null)
									{
										resultMap.put(feature.getName(), lineSplit[lineSplitIndex]);
										lineSplitIndex = lineSplitIndex + 1;
									}
								}

								resultMap.put(CHROMOSOME, entry.getKey());
								resultMap.put(POSITION, position);
								resultMap.put(REFERENCE, reference);
								resultMap.put(ALTERNATIVE, alternative);

								results.add(getAnnotatedEntity(entity, resultMap));
							}
							else
							{
								continue charArrayReader;
							}
						}
						else
						{
							continue charArrayReader;
						}
					}
				}
			}
			catch (FileNotFoundException e)
			{
				throw new RuntimeException(e);
			}
			finally
			{
				bufferedReader.close();
			}
		}

		return results;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);

		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CHR, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(POS_1_COOR, FieldTypeEnum.LONG));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(REF, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(ALT, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(AAREF, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(AAALT, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(HG18_POS_1_COOR, FieldTypeEnum.LONG));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(GENENAME, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(UNIPROT_ACC, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(UNIPROT_ID, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(UNIPROT_AAPOS, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(INTERPRO_DOMAIN, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CDS_STRAND, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(REFCODON, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(SLR_TEST_STATISTIC, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CODONPOS, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(FOLD_DEGENERATE, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(ANCESTRAL_ALLELE, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(ENSEMBL_GENEID, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(ENSEMBL_TRANSCRIPTID, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(AAPOS, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(AAPOS_SIFT, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(AAPOS_FATHMM, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(SIFT_SCORE, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(SIFT_SCORE_CONVERTED, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(SIFT_PRED, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(POLYPHEN2_HDIV_SCORE, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(POLYPHEN2_HDIV_PRED, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(POLYPHEN2_HVAR_SCORE, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(POLYPHEN2_HVAR_PRED, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(LRT_SCORE, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(LRT_SCORE_CONVERTED, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(LRT_PRED, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(MUTATIONTASTER_SCORE, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(MUTATIONTASTER_SCORE_CONVERTED, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(MUTATIONTASTER_PRED, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(MUTATIONASSESSOR_SCORE, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(MUTATIONASSESSOR_SCORE_CONVERTED,
				FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(MUTATIONASSESSOR_PRED, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(FATHMM_SCORE, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(FATHMM_SCORE_CONVERTED, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(FATHMM_PRED, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(RADIALSVM_SCORE, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(RADIALSVM_SCORE_CONVERTED, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(RADIALSVM_PRED, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(LR_SCORE, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(LR_PRED, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(RELIABILITY_INDEX, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(GERP_NR, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(GERP_RS, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(PHYLOP, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(TWONINE_WAY_PI, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(TWONINE_WAY_LOGODDS, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(LRT_OMEGA, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(UNISNP_IDS, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(THOUSAND_GP1_AC, FieldTypeEnum.INT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(THOUSAND_GP1_AF, FieldTypeEnum.DECIMAL));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(THOUSAND_GP1_AFR_AC, FieldTypeEnum.INT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(THOUSAND_GP1_AFR_AF, FieldTypeEnum.DECIMAL));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(THOUSAND_GP1_EUR_AC, FieldTypeEnum.INT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(THOUSAND_GP1_EUR_AF, FieldTypeEnum.DECIMAL));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(THOUSAND_GP1_AMR_AC, FieldTypeEnum.INT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(THOUSAND_GP1_AMR_AF, FieldTypeEnum.DECIMAL));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(THOUSAND_GP1_ASN_AC, FieldTypeEnum.INT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(THOUSAND_GP1_ASN_AF, FieldTypeEnum.DECIMAL));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(ESP6500_AA_AF, FieldTypeEnum.DECIMAL));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(ESP6500_EA_AF, FieldTypeEnum.DECIMAL));

		return metadata;
	}
}
