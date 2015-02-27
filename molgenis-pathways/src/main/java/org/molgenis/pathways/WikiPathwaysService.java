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

import org.molgenis.pathways.WikiPathwaysController.Impact;
import org.molgenis.wikipathways.client.WSPathwayInfo;
import org.molgenis.wikipathways.client.WSSearchResult;
import org.molgenis.wikipathways.client.WikiPathwaysPortType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.auto.value.AutoValue;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Interacts with WikiPathways. Caches results.
 */
@Component
public class WikiPathwaysService
{
	private final WikiPathwaysPortType wikiPathwaysProxy;

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

	@AutoValue
	public static abstract class ColoredPathwayParameters
	{
		public abstract String getPathwayId();

		public abstract Map<String, Impact> getImpactPerGraphId();

		public String[] getGraphIdArray()
		{
			return getImpactPerGraphId().keySet().toArray(new String[0]);
		}

		public String[] getColorArray()
		{
			return getImpactPerGraphId().values().stream().map(Impact::getColor).toArray((i) -> new String[i]);
		}

		public static ColoredPathwayParameters create(String pathwayId, Map<String, Impact> impactPerGraphId)
		{
			return new AutoValue_WikiPathwaysService_ColoredPathwayParameters(pathwayId, impactPerGraphId);
		}
	}

	private final LoadingCache<ColoredPathwayParameters, String> coloredPathwayImageCache = CacheBuilder.newBuilder()
			.maximumSize(Integer.MAX_VALUE).refreshAfterWrite(1, TimeUnit.DAYS)
			.build(new CacheLoader<ColoredPathwayParameters, String>()
			{
				@Override
				public String load(ColoredPathwayParameters coloredPathwayParameters) throws Exception
				{
					System.out.println(coloredPathwayParameters);
					return toSingleLineString(wikiPathwaysProxy.getColoredPathway(
							coloredPathwayParameters.getPathwayId(), "0", coloredPathwayParameters.getGraphIdArray(),
							coloredPathwayParameters.getColorArray(), "svg"));
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
	public String getPathwayGPML(String pathwayId) throws RemoteException
	{
		return wikiPathwaysProxy.getPathway(pathwayId, 0).getGpml();
	}

	/**
	 * Retrieves a colored pathway image
	 * 
	 * @param pathwayId
	 *            ID of the pathway from WikiPathways
	 * @param highestImpactPerGraphId
	 * @param graphIds
	 *            List containing graphIds to color
	 * @param colors
	 *            List of colors in the same order of the graphIds
	 * @return String containing the pathway svg
	 * @throws ExecutionException
	 *             if loading of the cache fails
	 */
	public String getColoredPathwayImage(String pathwayId, Map<String, Impact> impactPerGraphId)
			throws ExecutionException
	{
		return coloredPathwayImageCache.get(ColoredPathwayParameters.create(pathwayId, impactPerGraphId));
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
