package org.molgenis.data.annotation.impl;

import java.util.*;
import java.io.*;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.LocusAnnotator;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.VariantAnnotator;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
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
	private static final String NAME = "dbNSFP-Variant";
	
	// the dbnsfp service is dependant on these four values,
	// without them no annotations can be returned
	private static final String CHROMOSOME = "chrom";
	private static final String POSITION = "pos";
	private static final String REFERENCE = "ref";
	private static final String ALTERNATIVE = "alt";

	// FIXME the prefix for chromosome files, change this into runtime property
	private static final String CHROMOSOME_FILE = "/Users/mdehaan/bin/tools/dbnsfp/dbNSFP2.3_variant.chr";

	// we want to know features, so take the first chromosome file and retrieve them from the header
	private static final String[] FEATURES = determineFeatures();

	@Autowired
	AnnotationService annotatorService;

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
	public Iterator<Entity> annotate(Iterator<Entity> source)
	{
		List<Entity> results = new ArrayList<Entity>();
		Map<String, List<String[]>> chromosomeMap = new HashMap<String, List<String[]>>();

		// Make a map with data pulled from every Entity
		while (source.hasNext())
		{
			Entity entity = source.next();

			List<String[]> listOfTriplets = new ArrayList<String[]>();

			// a triplet contains position, reference and alternative
			String[] triplets = new String[3];

			String chromosome = entity.getString(CHROMOSOME);
			
			triplets[0] = entity.getString(POSITION);
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
		}

		try
		{
			for (String chromosomeInMap : chromosomeMap.keySet())
			{
				FileReader reader = new FileReader(new File(CHROMOSOME_FILE + chromosomeInMap));
				BufferedReader bufferedReader = new BufferedReader(reader);

				List<String[]> charArraysForThisChromosome = chromosomeMap.get(chromosomeInMap);

				String line = "";

				while (bufferedReader.ready())
				{
					line = bufferedReader.readLine();
					String[] lineSplit = line.split("\t");

					charArrayReader: for (int i = 0; i < charArraysForThisChromosome.size(); i++)
					{
						Long position = Long.parseLong(charArraysForThisChromosome.get(i)[0]);

						if (lineSplit[1].equals(position))
						{
							String reference = charArraysForThisChromosome.get(i)[1];
							String alternative = charArraysForThisChromosome.get(i)[2];

							if (lineSplit[2].toUpperCase().equals(reference.toUpperCase())
									&& lineSplit[3].toUpperCase().equals(alternative.toUpperCase()))
							{
								int lineSplitIndex = 4; // skip the first four elements: chrom, pos, ref, alt

								// we have a match with a line
								HashMap<String, Object> resultMap = new HashMap<String, Object>();

								for (String feature : FEATURES)
								{
									if (feature != null)
									{
										resultMap.put(feature, lineSplit[lineSplitIndex]);
										lineSplitIndex = lineSplitIndex + 1;
									}
								}

								resultMap.put(CHROMOSOME, chromosomeInMap);
								resultMap.put(POSITION, position);
								resultMap.put(REFERENCE, reference);
								resultMap.put(ALTERNATIVE, alternative);

								results.add(new MapEntity(resultMap));
							}
							else
							{
								// TODO no match, next variant
								continue charArrayReader;
							}
						}
						else
						{
							// TODO no match, next variant
							continue charArrayReader;
						}
					}
				}

				bufferedReader.close();

			}
		}

		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}

		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		return results.iterator();
	}

	private static String[] determineFeatures()
	{
		String[] features = null;

		try
		{
			FileReader reader = new FileReader(new File(CHROMOSOME_FILE + "1"));
			BufferedReader bufferedReader = new BufferedReader(reader);

			String line = bufferedReader.readLine();
			features = line.split("\t");
			bufferedReader.close();
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		features[0] = null;
		features[1] = null;
		features[2] = null;
		features[3] = null;

		return features;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName());

		for (String attribute : FEATURES)
		{
			if (attribute != null)
			{
				// FIXME not all attributes are strings
				metadata.addAttributeMetaData(new DefaultAttributeMetaData(attribute, FieldTypeEnum.STRING));
			}
		}

		return metadata;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
