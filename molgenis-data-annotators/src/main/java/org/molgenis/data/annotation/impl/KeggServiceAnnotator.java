package org.molgenis.data.annotation.impl;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.LocusAnnotator;
import org.molgenis.data.annotation.impl.datastructures.*;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;

/**
 * @author jvelde
 */
@Component("KeggService")
public class KeggServiceAnnotator extends LocusAnnotator
{

	private static final String KEGG_PATHWAY_INFO = "keggPathwayInfo";
	private static final String HTTP_REST_KEGG_JP_LIST_PATHWAY_HSA = "http://rest.kegg.jp/list/pathway/hsa";
	private static final String KEGG_GENES_HSA = "keggGenesHsa";
	private static final String HTTP_REST_KEGG_JP_LIST_HSA = "http://rest.kegg.jp/list/hsa";
	private static final String KEGG_PATHWAYS_HSA = "keggPathwaysHsa";
	private static final String HTTP_REST_KEGG_JP_LINK_HSA_PATHWAY = "http://rest.kegg.jp/link/hsa/pathway";
	public static final String KEGG_GENE_ID = "KEGG_gene_id";
	public static final String KEGG_PATHWAYS_IDS = "KEGG_pathway_ids";
	public static final String KEGG_PATHWAYS_NAMES = "KEGG_pathway_names";

	@Autowired
	AnnotationService annotatorService;

	@Autowired
	DataService dataService;

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		annotatorService.addAnnotator(this);
	}

	@Override
	public String getName()
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
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName());
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(KEGG_GENE_ID,
				MolgenisFieldTypes.FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(KEGG_PATHWAYS_IDS,
				MolgenisFieldTypes.FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(KEGG_PATHWAYS_NAMES,
				MolgenisFieldTypes.FieldTypeEnum.STRING));
		return metadata;
	}

	/**
	 * // KEGG genes for homo sapiens // // stored in KeggID - KeggGene map, though KeggGene also has this identifier //
	 * // example: // hsa:4351 MPI, CDG1B, PMI, PMI1; mannose phosphate isomerase (EC:5.3.1.8); K01809
	 * mannose-6-phosphate isomerase // [EC:5.3.1.8]
	 **/
	public static Map<String, KeggGene> getKeggGenes() throws IOException
	{
		Map<String, KeggGene> res = new HashMap<String, KeggGene>();
		ArrayList<String> keggGenes = OmimHpoAnnotator.readLinesFromURL(HTTP_REST_KEGG_JP_LIST_HSA, KEGG_GENES_HSA);
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

		for (String pathwayId : pathwayGenes.keySet())
		{
			for (String geneId : pathwayGenes.get(pathwayId))
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
	public static Map<String, ArrayList<String>> getKeggPathwayGenes() throws IOException
	{
		ArrayList<String> keggGenesToPathway = OmimHpoAnnotator.readLinesFromURL(HTTP_REST_KEGG_JP_LINK_HSA_PATHWAY,
				KEGG_PATHWAYS_HSA);
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
	public static Map<String, String> getKeggPathwayInfo() throws IOException
	{
		ArrayList<String> keggPathwayInfo = OmimHpoAnnotator.readLinesFromURL(HTTP_REST_KEGG_JP_LIST_PATHWAY_HSA,
				KEGG_PATHWAY_INFO);

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
	public static Map<String, String> hgncToKeggGeneId(HashMap<String, HGNCLoc> hgncLocs,
			Map<String, KeggGene> keggGenes) throws Exception
	{
		Map<String, String> res = new HashMap<String, String>();

		for (String keggId : keggGenes.keySet())
		{
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
	public List<Entity> annotateEntity(Entity entity)
	{
		List<Entity> results = new ArrayList<Entity>();

		try
		{
			List<Locus> loci = new ArrayList<Locus>();

			String chromosome = entity.getString(CHROMOSOME);
			Long position = entity.getLong(POSITION);
			Locus locus = new Locus(chromosome, position);
			loci.add(locus);

			HashMap<String, HGNCLoc> hgncLocs = OmimHpoAnnotator.getHgncLocs();
			Map<String, ArrayList<String>> keggPathwayGenes = getKeggPathwayGenes();
			Map<String, String> pathwayInfo = getKeggPathwayInfo();
			Map<String, KeggGene> keggGenes = getKeggGenes();

			List<String> geneSymbols = OmimHpoAnnotator.locationToHGNC(hgncLocs, loci);

			Map<String, String> hgncToKeggGeneId = hgncToKeggGeneId(hgncLocs, keggGenes);
			Map<String, ArrayList<String>> keggGenePathways = getKeggGenePathways(keggPathwayGenes);

			for (int i = 0; i < loci.size(); i++)
			{
				locus = loci.get(i);
				String geneSymbol = geneSymbols.get(i);

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

					results.add(new MapEntity(resultMap));
				}
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		return results;
	}
}
