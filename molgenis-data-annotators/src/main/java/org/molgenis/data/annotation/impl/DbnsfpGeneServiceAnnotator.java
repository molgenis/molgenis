package org.molgenis.data.annotation.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.LocusAnnotator;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.impl.datastructures.HGNCLoc;
import org.molgenis.data.annotation.impl.datastructures.Locus;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * <p>
 * This class uses a location to retrieve a gene from the ... class. This gene can then be used to approach the dbNSFP
 * gene file. It extracts relevant gene annotations from this file. This can be run seperately from the dbNSFP variant
 * service.
 * </p>
 * 
 * <p>
 * <b>dbNSFP gene returns:</b>
 * 
 * Gene_name Ensembl_gene chr Gene_old_names Gene_other_names Uniprot_acc Uniprot_id Entrez_gene_id CCDS_id Refseq_id
 * ucsc_id MIM_id Gene_full_name Pathway(Uniprot) Pathway(ConsensusPathDB) Function_description Disease_description
 * MIM_phenotype_id MIM_disease Trait_association(GWAS) GO_Slim_biological_process GO_Slim_cellular_component
 * GO_Slim_molecular_function Expression(egenetics) Expression(GNF/Atlas) Interactions(IntAct) Interactions(BioGRID)
 * Interactions(ConsensusPathDB) P(HI) P(rec) Known_rec_info Essential_gene
 * </p>
 * 
 * @author mdehaan
 * 
 * @version dbNSFP version 2.3 downloaded January 26, 2014
 * 
 * */
@Component("dbnsfpGeneService")
public class DbnsfpGeneServiceAnnotator extends LocusAnnotator
{
	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	AnnotationService annotatorService;

	private static final String NAME = "dbNSFP-Gene";
	public static final String GENE_FILE_LOCATION_PROPERTY = "dbnsfp_gene_location";

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

	private String getFileLocation()
	{
		return molgenisSettings.getProperty(GENE_FILE_LOCATION_PROPERTY);
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		String geneFile = getFileLocation();
		String[] features = determineFeatures(geneFile);

		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName());

		for (String attribute : features)
		{
			if (attribute != null)
			{
				// FIXME not all attributes are strings
				metadata.addAttributeMetaData(new DefaultAttributeMetaData(attribute, FieldTypeEnum.TEXT));
			}
		}

		return metadata;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity)
	{
		List<Entity> results = new ArrayList<Entity>();
		BufferedReader bufferedReader = null;
		String geneFile = getFileLocation();
		String[] features = determineFeatures(geneFile);

		try
		{
			FileReader reader = new FileReader(new File(geneFile));
			bufferedReader = new BufferedReader(reader);

			HashMap<String, HGNCLoc> hgncLocs = OmimHpoAnnotator.getHgncLocs();

			Long position = entity.getLong(POSITION);
			String chromosome = entity.getString(CHROMOSOME);

			List<Locus> locus = new ArrayList<Locus>(Arrays.asList(new Locus(chromosome, position)));
			List<String> geneSymbols = OmimHpoAnnotator.locationToHGNC(hgncLocs, locus);

			if (geneSymbols != null)
			{
				while (bufferedReader.ready())
				{
					String line = bufferedReader.readLine();
					String[] lineSplit = line.split("\t");

					for (String gene : geneSymbols)
					{
						if (lineSplit[0].equals(gene))
						{
							int lineSplitIndex = 0;
							HashMap<String, Object> resultMap = new HashMap<String, Object>();

							for (String feature : features)
							{
								if (feature != null)
								{
									resultMap.put(feature, lineSplit[lineSplitIndex]);
									lineSplitIndex = lineSplitIndex + 1;
								}

							}

							resultMap.put(CHROMOSOME, chromosome);
							resultMap.put(POSITION, position);

							results.add(new MapEntity(resultMap));
						}
					}
				}
			}
		}

		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			try
			{
				bufferedReader.close();
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		return results;
	}

	public String[] determineFeatures(String geneFile)
	{
		String[] features = null;
		BufferedReader bufferedReader = null;
		try
		{
			FileReader reader = new FileReader(new File(geneFile));
			bufferedReader = new BufferedReader(reader);

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
		finally
		{
			try
			{
				bufferedReader.close();
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		// remove the # from the first feature in the header
		features[0] = features[0].replace("#", "");

		return features;
	}
}
