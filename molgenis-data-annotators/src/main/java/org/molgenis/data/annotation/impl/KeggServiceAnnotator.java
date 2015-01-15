package org.molgenis.data.annotation.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.HgncLocationsUtils;
import org.molgenis.data.annotation.LocusAnnotator;
import org.molgenis.data.annotation.impl.datastructures.HGNCLocations;
import org.molgenis.data.annotation.impl.datastructures.KeggGene;
import org.molgenis.data.annotation.impl.datastructures.Locus;
import org.molgenis.data.annotation.provider.HgncLocationsProvider;
import org.molgenis.data.annotation.provider.KeggDataProvider;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @author jvelde
 */
@Component("KeggService")
public class KeggServiceAnnotator extends LocusAnnotator
{
	private final AnnotationService annotatorService;
	private final HgncLocationsProvider hgncLocationsProvider;
	private final KeggDataProvider keggDataProvider;

	public static final String KEGG_GENE_ID = "KEGG_gene_id";
	public static final String KEGG_PATHWAYS_IDS = "KEGG_pathway_ids";
	public static final String KEGG_PATHWAYS_NAMES = "KEGG_pathway_names";

	@Autowired
	public KeggServiceAnnotator(AnnotationService annotatorService, HgncLocationsProvider hgncLocationsProvider,
			KeggDataProvider keggDataProvider) throws IOException
	{
		this.annotatorService = annotatorService;
		this.hgncLocationsProvider = hgncLocationsProvider;
		this.keggDataProvider = keggDataProvider;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		annotatorService.addAnnotator(this);
	}

	@Override
	public String getSimpleName()
	{
		return "KEGG";
	}

	@Override
	public boolean annotationDataExists()
	{
		boolean dataExists = true;

		// TODO Check if the webservices are up

		return dataExists;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException
	{
		List<Entity> results = new ArrayList<Entity>();

		String chromosome = entity.getString(CHROMOSOME);
		Long position = entity.getLong(POSITION);

		Locus locus = new Locus(chromosome, position);

		Map<String, ArrayList<String>> keggPathwayGenes = getKeggPathwayGenes();
		Map<String, String> pathwayInfo = getKeggPathwayInfo();

		List<String> geneSymbols = HgncLocationsUtils.locationToHgcn(hgncLocationsProvider.getHgncLocations(), locus);

		Map<String, String> hgncToKeggGeneId = hgncToKeggGeneId();
		Map<String, ArrayList<String>> keggGenePathways = getKeggGenePathways(keggPathwayGenes);

		try
		{
			for (String geneSymbol : geneSymbols)
			{
				if (geneSymbol != null)
				{
					HashMap<String, Object> resultMap = new HashMap<String, Object>();

					resultMap.put(CHROMOSOME, locus.getChrom());
					resultMap.put(POSITION, locus.getPos());

					String keggGeneId = hgncToKeggGeneId.get(geneSymbol);

					resultMap.put(KEGG_GENE_ID, keggGeneId);

					if (keggGenePathways.get(keggGeneId) != null)
					{

						StringBuilder sb = new StringBuilder();
						for (String pathwayId : keggGenePathways.get(keggGeneId))
						{
							sb.append(pathwayId + ", ");
						}
						sb.delete(sb.length() - 2, sb.length());
						resultMap.put(KEGG_PATHWAYS_IDS, sb.toString());

						sb = new StringBuilder();
						for (String pathwayId : keggGenePathways.get(keggGeneId))
						{
							sb.append(pathwayInfo.get(pathwayId) + ", ");
						}

						sb.delete(sb.length() - 2, sb.length());
						resultMap.put(KEGG_PATHWAYS_NAMES, sb.toString());
					}

					else
					{
						// no genes for this pathway, do nothing
					}

					results.add(getAnnotatedEntity(entity, resultMap));
				}
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		return results;
	}

	/**
	 * // KEGG genes for homo sapiens // // stored in KeggID - KeggGene map, though KeggGene also has this identifier //
	 * // example: // hsa:4351 MPI, CDG1B, PMI, PMI1; mannose phosphate isomerase (EC:5.3.1.8); K01809
	 * mannose-6-phosphate isomerase // [EC:5.3.1.8]
	 **/
	private Map<String, KeggGene> getKeggGenes() throws IOException
	{
		Map<String, KeggGene> res = new HashMap<String, KeggGene>();
		List<String> keggGenes = IOUtils.readLines(keggDataProvider.getKeggHsaReader());
		for (String s : keggGenes)
		{
			String[] line = s.split("\t");
			String[] allSymbolsAndProteins = line[1].split("; ");

			String id = line[0];
			List<String> symbols = new ArrayList<String>(Arrays.asList(allSymbolsAndProteins[0].split(", ")));
			List<String> proteins = new ArrayList<String>(Arrays.asList(allSymbolsAndProteins).subList(1,
					allSymbolsAndProteins.length));

			KeggGene kg = new KeggGene(id, symbols, proteins);
			res.put(id, kg);

		}
		return res;
	}

	/**
	 * Convert map of (pathway -> genes) to (gene -> pathways)
	 * 
	 * @param pathwayGenes
	 * @return
	 */
	public static Map<String, ArrayList<String>> getKeggGenePathways(Map<String, ArrayList<String>> pathwayGenes)
	{
		Map<String, ArrayList<String>> res = new HashMap<String, ArrayList<String>>();

		for (Map.Entry<String, ArrayList<String>> entry : pathwayGenes.entrySet())
		{
			String pathwayId = entry.getKey();
			for (String geneId : entry.getValue())
			{
				if (res.containsKey(geneId))
				{
					res.get(geneId).add(pathwayId);
				}
				else
				{
					ArrayList<String> list = new ArrayList<String>();
					list.add(pathwayId);
					res.put(geneId, list);
				}
			}
		}
		return res;
	}

	/**
	 * // KEGG pathway and gene info // // stored in map of pathwayId - List of KeggIDs // // example: // path:hsa00010
	 * hsa:92483 // path:hsa00010 hsa:92579 // path:hsa00020 hsa:1431 // path:hsa00020 hsa:1737
	 **/
	private Map<String, ArrayList<String>> getKeggPathwayGenes() throws IOException
	{
		List<String> keggGenesToPathway = IOUtils.readLines(keggDataProvider.getKeggPathwayHsaReader());
		Map<String, ArrayList<String>> res = new HashMap<String, ArrayList<String>>();
		for (String s : keggGenesToPathway)
		{
			String[] split = s.split("\t");

			if (res.containsKey(split[0]))
			{
				res.get(split[0]).add(split[1]);
			}
			else
			{
				res.put(split[0], new ArrayList<String>(Arrays.asList(split[1])));
			}
		}
		return res;
	}

	/**
	 * // KEGG pathway and gene info // // stored in map of pathwayId - info // // example: // path:hsa00010 Glycolysis
	 * / Gluconeogenesis - Homo sapiens (human)
	 **/
	private Map<String, String> getKeggPathwayInfo() throws IOException
	{
		List<String> keggPathwayInfo = IOUtils.readLines(keggDataProvider.getKeggPathwayReader());

		Map<String, String> res = new HashMap<String, String>();

		for (String s : keggPathwayInfo)
		{
			String[] split = s.split("\t");
			res.put(split[0], split[1]);
		}

		return res;

	}

	/**
	 * // map HGNC to a KeggGene identifier
	 **/
	private Map<String, String> hgncToKeggGeneId() throws IOException
	{
		Map<String, KeggGene> keggGenes = getKeggGenes();
		Map<String, String> res = new HashMap<String, String>();

		Map<String, HGNCLocations> hgncLocs = hgncLocationsProvider.getHgncLocations();

		for (Map.Entry<String, KeggGene> entry : keggGenes.entrySet())
		{
			String keggId = entry.getKey();
			KeggGene k = keggGenes.get(keggId);

			for (String symbol : k.getSymbols())
			{
				if (hgncLocs.containsKey(symbol))
				{
					res.put(symbol, keggId);
					break;
				}
			}
		}

		Integer mapped = 0;
		for (String hgnc : hgncLocs.keySet())
		{
			if (res.containsKey(hgnc))
			{
				mapped++;
			}
		}

		return res;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(KEGG_GENE_ID,
				MolgenisFieldTypes.FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(KEGG_PATHWAYS_IDS,
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(KEGG_PATHWAYS_NAMES,
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		return metadata;
	}
}
