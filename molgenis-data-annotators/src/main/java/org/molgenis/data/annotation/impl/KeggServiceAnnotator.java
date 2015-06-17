package org.molgenis.data.annotation.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.LocusAnnotator;
import org.molgenis.data.annotation.impl.datastructures.HGNCLocations;
import org.molgenis.data.annotation.impl.datastructures.KeggGene;
import org.molgenis.data.annotation.impl.datastructures.Locus;
import org.molgenis.data.annotation.mini.AnnotatorInfo;
import org.molgenis.data.annotation.mini.AnnotatorInfo.Status;
import org.molgenis.data.annotation.mini.AnnotatorInfo.Type;
import org.molgenis.data.annotation.provider.HgncLocationsProvider;
import org.molgenis.data.annotation.provider.KeggDataProvider;
import org.molgenis.data.annotation.utils.AnnotatorUtils;
import org.molgenis.data.annotation.utils.HgncLocationsUtils;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @author jvelde
 */
@Component("KeggService")
public class KeggServiceAnnotator extends LocusAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(KeggServiceAnnotator.class);

	private final AnnotationService annotatorService;
	private final HgncLocationsProvider hgncLocationsProvider;
	private final KeggDataProvider keggDataProvider;

	public static final String KEGG_GENE_ID = "KEGG_gene_id";
	public static final String KEGG_PATHWAYS_IDS = "KEGG_pathway_ids";
	public static final String KEGG_PATHWAYS_NAMES = "KEGG_pathway_names";

	Map<String, ArrayList<String>> keggPathwayGenes = new HashMap<>();
	Map<String, KeggGene> keggGenes = new HashMap<>();
	Map<String, String> pathwayInfo = new HashMap<>();
	Map<String, HGNCLocations> hgncLocations = new HashMap<>();
	Map<String, String> hgncToKeggGeneId = new HashMap<>();

	@Autowired
	public KeggServiceAnnotator(AnnotationService annotatorService, HgncLocationsProvider hgncLocationsProvider,
			KeggDataProvider keggDataProvider) throws IOException
	{
		this.annotatorService = annotatorService;
		this.hgncLocationsProvider = hgncLocationsProvider;
		this.keggDataProvider = keggDataProvider;
	}

	@Override
	public String getSimpleName()
	{
		return "KEGG";
	}

	@Override
	public boolean annotationDataExists()
	{
		return true;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException
	{
		getAnnotationDataFromSources();
		List<Entity> results = new ArrayList<>();
		String chromosome = entity.getString(VcfRepository.CHROM);
		Long position = entity.getLong(VcfRepository.POS);
		Locus locus = new Locus(chromosome, position);
		List<String> geneSymbols = HgncLocationsUtils.locationToHgcn(hgncLocations, locus);

		// TODO: cache this

		Map<String, ArrayList<String>> keggGenePathways = getKeggGenePathways(keggPathwayGenes);

		try
		{
			for (String geneSymbol : geneSymbols)
			{
				if (geneSymbol != null)
				{
					HashMap<String, Object> resultMap = new HashMap<>();

					resultMap.put(VcfRepository.CHROM, locus.getChrom());
					resultMap.put(VcfRepository.POS, locus.getPos());

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

					results.add(AnnotatorUtils.getAnnotatedEntity(this, entity, resultMap));
				}
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		return results;
	}

	private void getAnnotationDataFromSources() throws IOException
	{
		if (this.keggPathwayGenes.isEmpty())
		{
			LOG.info("keggPathwayGenes empty, started fetching the data");
			this.keggPathwayGenes = getKeggPathwayGenes();
			LOG.info("finished fetching the keggPathwayGenes data");
		}
		if (this.keggGenes.isEmpty())
		{
			LOG.info("keggGenes empty, started fetching the data");
			this.keggGenes = getKeggGenes();
			LOG.info("finished fetching the keggGenes data");
		}
		if (this.pathwayInfo.isEmpty())
		{
			LOG.info("pathwayInfo empty, started fetching the data");
			this.pathwayInfo = getKeggPathwayInfo();
			LOG.info("finished fetching the pathwayInfo data");
		}
		if (this.hgncLocations.isEmpty())
		{
			LOG.info("hgncLocations empty, started fetching the data");
			this.hgncLocations = hgncLocationsProvider.getHgncLocations();
			LOG.info("finished fetching the hgncLocations data");
		}
		if (this.hgncToKeggGeneId.isEmpty())
		{
			LOG.info("hgncToKeggGeneId empty, started fetching the data");
			this.hgncToKeggGeneId = hgncToKeggGeneId();
			LOG.info("finished fetching the hgncToKeggGeneId data");
		}
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
	 * // KEGG genes for homo sapiens // // stored in KeggID - KeggGene map, though KeggGene also has this identifier //
	 * // example: // hsa:4351 MPI, CDG1B, PMI, PMI1; mannose phosphate isomerase (EC:5.3.1.8); K01809
	 * mannose-6-phosphate isomerase // [EC:5.3.1.8]
	 */
	private Map<String, KeggGene> getKeggGenes() throws IOException
	{
		Map<String, KeggGene> res = new HashMap<>();
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
	 * // KEGG pathway and gene info // // stored in map of pathwayId - List of KeggIDs // // example: // path:hsa00010
	 * hsa:92483 // path:hsa00010 hsa:92579 // path:hsa00020 hsa:1431 // path:hsa00020 hsa:1737
	 */
	private Map<String, ArrayList<String>> getKeggPathwayGenes() throws IOException
	{
		List<String> keggGenesToPathway = IOUtils.readLines(keggDataProvider.getKeggPathwayHsaReader());
		Map<String, ArrayList<String>> res = new HashMap<>();
		for (String s : keggGenesToPathway)
		{
			String[] split = s.split("\t");

			if (res.containsKey(split[0]))
			{
				res.get(split[0]).add(split[1]);
			}
			else
			{
				res.put(split[0], new ArrayList<>(Arrays.asList(split[1])));
			}
		}
		return res;
	}

	/**
	 * // KEGG pathway and gene info // // stored in map of pathwayId - info // // example: // path:hsa00010 Glycolysis
	 * / Gluconeogenesis - Homo sapiens (human)
	 */
	private Map<String, String> getKeggPathwayInfo() throws IOException
	{
		List<String> keggPathwayInfo = IOUtils.readLines(keggDataProvider.getKeggPathwayReader());

		Map<String, String> res = new HashMap<>();

		for (String s : keggPathwayInfo)
		{
			String[] split = s.split("\t");
			res.put(split[0], split[1]);
		}

		return res;

	}

	/**
	 * // map HGNC to a KeggGene identifier
	 */
	private Map<String, String> hgncToKeggGeneId() throws IOException
	{
		Map<String, String> res = new HashMap<>();

		Map<String, HGNCLocations> hgncLocs = hgncLocationsProvider.getHgncLocations();

		for (Map.Entry<String, KeggGene> entry : keggGenes.entrySet())
		{
			String keggId = entry.getKey();
			KeggGene keggGene = keggGenes.get(keggId);

			for (String symbol : keggGene.getSymbols())
			{
				if (hgncLocs.containsKey(symbol))
				{
					res.put(symbol, keggId);
					break;
				}
			}
		}
		return res;
	}

	@Override
	public List<AttributeMetaData> getOutputMetaData()
	{
		List<AttributeMetaData> metadata = new ArrayList<>();
		metadata.add(new DefaultAttributeMetaData(KEGG_GENE_ID, MolgenisFieldTypes.FieldTypeEnum.STRING));
		metadata.add(new DefaultAttributeMetaData(KEGG_PATHWAYS_IDS, MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.add(new DefaultAttributeMetaData(KEGG_PATHWAYS_NAMES, MolgenisFieldTypes.FieldTypeEnum.TEXT));
		return metadata;
	}

	@Override
	public AnnotatorInfo getInfo()
	{
		return AnnotatorInfo.create(Status.INDEV, Type.UNUSED, "unknown", "no description", getOutputMetaData());
	}
}
