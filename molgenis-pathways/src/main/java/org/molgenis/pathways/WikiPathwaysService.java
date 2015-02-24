package org.molgenis.pathways;

import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.molgenis.wikipathways.client.WSPathwayInfo;
import org.molgenis.wikipathways.client.WSSearchResult;
import org.molgenis.wikipathways.client.WikiPathwaysPortType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;

/**
 * Interacts with WikiPathways. Caches results.
 */
@Component
public class WikiPathwaysService
{
	private final WikiPathwaysPortType wikiPathwaysProxy;
	private static final String COLORS = "colors";
	private static final String GRAPH_IDS = "graphIds";
	private static final String PATHWAY_ID = "pathwayId";

	private final LoadingCache<String, List<WSPathwayInfo>> allPathwaysCache = CacheBuilder.newBuilder()
			.maximumSize(Integer.MAX_VALUE).refreshAfterWrite(1, TimeUnit.DAYS)
			.build(new CacheLoader<String, List<WSPathwayInfo>>()
			{
				@Override
				public List<WSPathwayInfo> load(String organism) throws Exception
				{
					WSPathwayInfo[] listPathways = wikiPathwaysProxy.listPathways(organism);
					return Arrays.asList(listPathways);

				}
			});

	private final LoadingCache<String, String> uncoloredPathwayImageCache = CacheBuilder.newBuilder()
			.maximumSize(Integer.MAX_VALUE).refreshAfterWrite(1, TimeUnit.DAYS).build(new CacheLoader<String, String>()
			{
				@Override
				public synchronized String load(String pathwayId) throws Exception
				{
					return toSingleLineString(wikiPathwaysProxy.getPathwayAs("svg", pathwayId, 0));

				}
			});

	@Autowired
	public WikiPathwaysService(WikiPathwaysPortType wikiPathwaysProxy)
	{
		this.wikiPathwaysProxy = wikiPathwaysProxy;
	}

	/**
	 * Turns byte array containing pathway svg into a String. Reads all content into a single line.
	 * 
	 * @param source
	 *            byte array with the svg
	 * @return
	 */
	private static String toSingleLineString(byte[] source)
	{
		ByteArrayInputStream bis = new ByteArrayInputStream(source);
		Scanner scanner = new Scanner(bis, "UTF-8");
		scanner.useDelimiter("\\Z");
		String result = "";
		try
		{
			if (scanner.hasNext())
			{
				result = scanner.next();
			}
		}
		finally
		{
			scanner.close();
		}
		return result;
	}

	private final LoadingCache<String, List<WSSearchResult>> pathwaysByXrefCache = CacheBuilder.newBuilder()
			.maximumSize(Integer.MAX_VALUE).refreshAfterWrite(1, TimeUnit.DAYS)
			.build(new CacheLoader<String, List<WSSearchResult>>()
			{
				@Override
				public List<WSSearchResult> load(String gene) throws Exception
				{
					WSSearchResult[] listPathways = wikiPathwaysProxy.findPathwaysByXref(new String[]
					{ gene }, new String[]
					{ "H" }); // H for HGNC database
					return Arrays.asList(listPathways);
				}
			});

	private final LoadingCache<Map<String, Object>, String> coloredPathwayImageCache = CacheBuilder.newBuilder()
			.maximumSize(Integer.MAX_VALUE).refreshAfterWrite(1, TimeUnit.DAYS)
			.build(new CacheLoader<Map<String, Object>, String>()
			{
				@Override
				@SuppressWarnings(
				{ "unchecked" })
				public String load(Map<String, Object> coloredPathwayParameters) throws Exception
				{
					List<String> graphIds = (List<String>) coloredPathwayParameters.get(GRAPH_IDS);
					List<String> colors = (List<String>) coloredPathwayParameters.get(COLORS);
					return toSingleLineString(wikiPathwaysProxy.getColoredPathway(
							coloredPathwayParameters.get(PATHWAY_ID).toString(), "0", graphIds.toArray(new String[0]),
							colors.toArray(new String[0]), "svg"));
				}
			});

	/**
	 * Searches pathways.
	 * 
	 * @param searchTerm
	 *            string to search for
	 * @return Map with all matching pathway ids mapped to pathway name
	 * @throws RemoteException
	 */
	public Map<String, String> getFilteredPathways(String searchTerm, String species) throws RemoteException
	{
		WSSearchResult[] pathwaysByText = wikiPathwaysProxy.findPathwaysByText(searchTerm, species);

		Map<String, String> result = new HashMap<String, String>();
		for (WSSearchResult pathwayInfo : pathwaysByText)
		{
			result.put(pathwayInfo.getId(), pathwayInfo.getName() + " (" + pathwayInfo.getId() + ")");
		}
		return result;
	}

	/**
	 * Retrieves the GPML of the current version of a pathway
	 * 
	 * @param pathwayId
	 *            ID of the pathway in WikiPathways
	 * @return String containing the pathway GPML
	 * @throws ConverterException
	 * @throws RemoteException
	 */
	public String getCurrentPathwayGPML(String pathwayId) throws RemoteException
	{
		return wikiPathwaysProxy.getPathway(pathwayId, 0).getGpml();
	}

	/**
	 * Retrieves a colored pathway image
	 * 
	 * @param pathwayId
	 *            ID of the pathway from WikiPathways
	 * @param graphIds
	 *            List containing graphIds to color
	 * @param colors
	 *            List of colors in the same order of the graphIds
	 * @return String containing the pathway svg
	 * @throws ExecutionException
	 *             if loading of the cache fails
	 */
	public String getColoredPathwayImage(String pathwayId, List<String> graphIds, List<String> colors)
			throws ExecutionException
	{
		return coloredPathwayImageCache.get(ImmutableMap.<String, Object> of(PATHWAY_ID, pathwayId, GRAPH_IDS,
				graphIds, COLORS, colors));
	}

	/**
	 * Retrieves an uncolored pathway image
	 * 
	 * @param pathwayId
	 *            Id of the pathway from WikiPathways
	 * @return String containing pathway svg
	 * @throws ExecutionException
	 *             if loading of the cache fails
	 */
	public String getUncoloredPathwayImage(String pathwayId) throws ExecutionException
	{
		return uncoloredPathwayImageCache.get(pathwayId);
	}

	/**
	 * Retrieves pathways according to gene, filtered for a certain species.
	 * 
	 * @param gene
	 *            HGNC gene name
	 * @param species
	 *            String with species name, e.g. Homo sapiens
	 * @return Map mapping id to pathway name
	 * @throws ExecutionException
	 *             if loading of the cache fails
	 */
	public Map<String, String> getPathwaysForGene(String gene, String species) throws ExecutionException
	{
		Map<String, String> result = new HashMap<String, String>();
		for (WSSearchResult pathway : pathwaysByXrefCache.get(gene))
		{
			if (pathway.getSpecies().equals(species))
			{
				result.put(pathway.getId(), pathway.getName() + " (" + pathway.getId() + ")");
			}
		}
		return result;
	}

	/**
	 * Retrieves all pathways for a species.
	 * 
	 * @param species
	 *            String with species name, e.g. Homo sapiens
	 * @return Map mapping id to pathway name
	 * @throws ExecutionException
	 *             if loading of the cache fails
	 */
	public Map<String, String> getAllPathways(String species) throws ExecutionException
	{
		List<WSPathwayInfo> listOfPathways = allPathwaysCache.get(species);

		Map<String, String> result = new HashMap<String, String>();
		for (WSPathwayInfo pathwayInfo : listOfPathways)
		{
			result.put(pathwayInfo.getId(), pathwayInfo.getName() + " (" + pathwayInfo.getId() + ")");
		}
		return result;
	}

}
