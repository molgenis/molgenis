package org.molgenis.pathways.service;

import com.google.common.cache.LoadingCache;
import org.molgenis.pathways.model.Impact;
import org.molgenis.pathways.model.Pathway;
import org.molgenis.wikipathways.client.WikiPathwaysPortType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Interacts with WikiPathways. Caches results.
 */
@Component
public class WikiPathwaysService
{
	private static final String[] HGNC_CODE = new String[] { "H" };

	private final WikiPathwaysPortType wikiPathwaysProxy;
	private final LoadingCache<String, Set<Pathway>> allPathwaysCache;
	private final LoadingCache<PathwaysPerGeneParameters, Set<Pathway>> pathwaysPerGeneCache;
	private final LoadingCache<ColoredPathwayParameters, String> coloredPathwayImageCache;
	private final LoadingCache<String, String> uncoloredPathwayImageCache;

	/**
	 * Creates a new WikiPathwaysService. creates all the caches with their proper loaders.
	 *
	 * @param wikiPathwaysProxy {@link WikiPathwaysPortType} proxy for the REST api that the caches use to load their data from
	 */
	@Autowired
	public WikiPathwaysService(WikiPathwaysPortType wikiPathwaysProxy)
	{
		this.wikiPathwaysProxy = wikiPathwaysProxy;
		this.allPathwaysCache = CacheFactory.loadingPathwayCache(wikiPathwaysProxy::listPathways,
				(organism, pathway) -> true, Pathway::create);
		this.pathwaysPerGeneCache = CacheFactory.loadingPathwayCache(
				params -> wikiPathwaysProxy.findPathwaysByXref(params.getGeneArray(), HGNC_CODE),
				(params, pathway) -> pathway.getSpecies().equals(params.getSpecies()), Pathway::create);
		this.uncoloredPathwayImageCache = CacheFactory.loadingCache(
				pathwayId -> toSingleLineString(wikiPathwaysProxy.getPathwayAs("svg", pathwayId, 0)));
		this.coloredPathwayImageCache = CacheFactory.loadingCache(params -> toSingleLineString(
				wikiPathwaysProxy.getColoredPathway(params.getPathwayId(), "0", params.getGraphIdArray(),
						params.getColorArray(), "svg")));
	}

	/**
	 * Turns byte array containing pathway svg into a String. Reads all content into a single line.
	 *
	 * @param source byte array with the svg
	 * @return String containing the svg
	 */
	private static String toSingleLineString(byte[] source)
	{
		StringBuilder result = new StringBuilder();
		ByteArrayInputStream bis = new ByteArrayInputStream(source);
		Scanner scanner = new Scanner(bis, "UTF-8");
		scanner.useDelimiter("\\n");
		try
		{
			while (scanner.hasNext())
			{
				result.append(scanner.next());
				result.append(" ");
			}
		}
		finally
		{
			scanner.close();
		}
		return result.toString();
	}

	/**
	 * Searches pathways.
	 *
	 * @param searchTerm string to search for
	 * @return Map with all matching pathway ids mapped to pathway name
	 * @throws RemoteException
	 */
	public Collection<Pathway> getFilteredPathways(String searchTerm, String species) throws RemoteException
	{
		return Arrays.stream(wikiPathwaysProxy.findPathwaysByText(searchTerm, species))
					 .map(Pathway::create)
					 .collect(Collectors.toList());
	}

	/**
	 * Retrieves the GPML of the current version of a pathway
	 *
	 * @param pathwayId ID of the pathway in WikiPathways
	 * @return String containing the pathway GPML
	 * @throws RemoteException
	 */
	public String getPathwayGPML(String pathwayId) throws RemoteException
	{
		return wikiPathwaysProxy.getPathway(pathwayId, 0).getGpml();
	}

	/**
	 * Retrieves a colored pathway image
	 *
	 * @param pathwayId        ID of the pathway from WikiPathways
	 * @param impactPerGraphId
	 * @return String containing the pathway svg
	 * @throws ExecutionException if loading of the cache fails
	 */
	public String getColoredPathwayImage(String pathwayId, Map<String, Impact> impactPerGraphId)
			throws ExecutionException
	{
		return coloredPathwayImageCache.get(ColoredPathwayParameters.create(pathwayId, impactPerGraphId));
	}

	/**
	 * Retrieves an uncolored pathway image
	 *
	 * @param pathwayId Id of the pathway from WikiPathways
	 * @return String containing pathway svg
	 * @throws ExecutionException if loading of the cache fails
	 */
	public String getUncoloredPathwayImage(String pathwayId) throws ExecutionException
	{
		return uncoloredPathwayImageCache.get(pathwayId);
	}

	/**
	 * Retrieves pathways according to gene, filtered for a certain species.
	 *
	 * @param gene    HGNC gene name
	 * @param species String with species name, e.g. Homo sapiens
	 * @return Collection of {@link Pathway}s
	 * @throws ExecutionException if loading of the cache fails
	 */
	public Collection<Pathway> getPathwaysForGene(String gene, String species) throws ExecutionException
	{
		return pathwaysPerGeneCache.get(PathwaysPerGeneParameters.create(species, gene));
	}

	/**
	 * Retrieves all pathways for a species.
	 *
	 * @param species String with species name, e.g. Homo sapiens
	 * @return Map mapping id to pathway name
	 * @throws ExecutionException if loading of the cache fails
	 */
	public Collection<Pathway> getAllPathways(String species) throws ExecutionException
	{
		return allPathwaysCache.get(species);
	}

}
