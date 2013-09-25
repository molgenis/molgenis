package uk.ac.ebi.mydas.examples;


import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.configuration.PropertyType;
import uk.ac.ebi.mydas.controller.CacheManager;
import uk.ac.ebi.mydas.datasource.ReferenceDataSource;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.extendedmodel.DasUnknownFeatureSegment;
import uk.ac.ebi.mydas.model.*;

import javax.servlet.ServletContext;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Data Source that reads a GFF 2 file which path has been specified in the
 * configuration file as a property of the datasource element
 * Methods related to a Reference server (getSequence, getEntryPointVersion and getEntryPoints)
 * do not provide real data related to the GFF File. 
 */
public class GFFFileReferenceDataSource implements ReferenceDataSource {

	CacheManager cacheManager = null;
	ServletContext svCon;
	Map<String, PropertyType> globalParameters;
	DataSourceConfiguration config;
	String path,path2;
	private Collection<DasAnnotatedSegment> segments;
	private Collection<DasType> types;
	private Map<String,DasSequence> sequences;


	/**
	 * The path is recovery from the configuration, the file is then parsed and
	 * keep in memory as a DasSegment collection object that is queried for each method
	 */
	public void init(ServletContext servletContext, Map<String, PropertyType> globalParameters, DataSourceConfiguration dataSourceConfig) throws DataSourceException {
		this.svCon = servletContext;
		this.globalParameters = globalParameters;
		this.config = dataSourceConfig;
		path = config.getDataSourceProperties().get("gff_file").getValue();
		path2 = config.getDataSourceProperties().get("fasta_file").getValue();
		try {
			GFF2Parser parser = new GFF2Parser(new FileInputStream(servletContext.getRealPath(path)));
			segments = parser.parse();
			types = parser.getTypes();
		} catch (FileNotFoundException e) {
			throw new DataSourceException("The data source cannot be loaded. The file couldn't be oppened",e);
		} catch (Exception e) {
			throw new DataSourceException("The data source cannot be loaded because of parsing problems",e);
		}
		try {
			FastaParser parser2 = new FastaParser(new FileInputStream(servletContext.getRealPath(path2)),path2);
			sequences = parser2.parse();
		} catch (FileNotFoundException e) {
			throw new DataSourceException("The reference data source cannot be loaded. The fasta file couldn't be oppened",e);
		} catch (Exception e) {
			throw new DataSourceException("The reference data source cannot be loaded because of parsing problems with the fasta file",e);
		}
	}

	/**
	 * Nothing to destroy
	 */
	public void destroy() { }

	/**
	 * Look into the list of segments for the one with the same ID. if is not there it throws a BadReferenceObjectException
	 */
	public DasAnnotatedSegment getFeatures(String segmentId,Integer maxbins) throws BadReferenceObjectException, DataSourceException {
		for(DasAnnotatedSegment segment:segments){
			if (segment.getSegmentId().equals(segmentId))
				return segment;
		}
		throw new BadReferenceObjectException("The id is not in the file", segmentId);
	}

	/**
	 * return the already built list of types.
	 */
	public Collection<DasType> getTypes() throws DataSourceException {
		return types;
	}

	/**
	 *
	 */
	public Collection<DasAnnotatedSegment> getFeatures(Collection<String> featureIdCollection, Integer maxbins)
	throws UnimplementedFeatureException, DataSourceException {
		Collection<DasAnnotatedSegment> segmentsResponse =new ArrayList<DasAnnotatedSegment>();
		for (String featureId:featureIdCollection){
			boolean found=false;
			for (DasAnnotatedSegment segment:segments)
				for(DasFeature feature:segment.getFeatures())
					if(feature.getFeatureId().equals(featureId)){
						segmentsResponse.add(new DasAnnotatedSegment(segment.getSegmentId(),segment.getStartCoordinate(),segment.getStopCoordinate(),segment.getVersion(),segment.getSegmentLabel(),Collections.singleton(feature)));
						found=true;
					}
			if(!found)
				segmentsResponse.add(new DasUnknownFeatureSegment(featureId));
		}
		return segmentsResponse;
	}

	/**
	 * count the number of times that the type id appears in all the segments
	 */
	public Integer getTotalCountForType(DasType type) throws DataSourceException {
		int count=0;
		for (DasAnnotatedSegment segment:segments)
			for(DasFeature feature:segment.getFeatures())
				if(type.getId().equals(feature.getType().getId()))
					count++;
		return count;
	}

	/**
	 * The mydas DAS server implements caching within the server.  This method passes your datasource a reference
	 * to a {@link uk.ac.ebi.mydas.controller.CacheManager} object.  To implement this method, you should simply retain a reference to this object.
	 * In your code you can then make use of this object to manipulate caching in the mydas servlet.
	 * <p/>
	 * At present the {@link uk.ac.ebi.mydas.controller.CacheManager} class provides you with a single method public void emptyCache() that
	 * you can call if (for example) the underlying data source has changed.
	 *
	 * @param cacheManager a reference to a {@link uk.ac.ebi.mydas.controller.CacheManager} object that the data source can use to empty
	 *                     the cache for this data source.
	 */
	public void registerCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	/**
	 */
	public URL getLinkURL(String field, String id) throws UnimplementedFeatureException, DataSourceException {
		throw new UnimplementedFeatureException("No implemented");
	}

	public DasSequence getSequence(String segmentId) throws BadReferenceObjectException, DataSourceException {
		DasSequence seq=sequences.get(segmentId);
		if (seq==null)
			throw new BadReferenceObjectException("",segmentId);
		return seq;
	}

	public String getEntryPointVersion() throws DataSourceException {
		return "1.0"; 
	}

    /**
     * This method is provided just for testing purposes, it does not retrieve real data.
     * @param start Initial row position on the entry points collection for this server
     * @param stop Final row position ont the entry points collection for this server
     * @return A sub ordered collection of entry points from the start row to the stop row 
     * @throws DataSourceException
     */
	public Collection<DasEntryPoint> getEntryPoints(Integer start, Integer stop) throws DataSourceException {
		ArrayList<DasEntryPoint> entryPoints = new ArrayList<DasEntryPoint>();
		for (String id:sequences.keySet()) {
			DasSequence seq= sequences.get(id);
			entryPoints.add(
					new DasEntryPoint(
							id, seq.getStopCoordinate(), seq.getStartCoordinate(), "DNA", "1.0",
							null,
							seq.getLabel(), false));
		}
		if ((start != null) && (stop != null)) 
			return entryPoints.subList(start, stop);

		return entryPoints;
	}

	public int getTotalEntryPoints() throws DataSourceException {
		return sequences.size();
	}
}
