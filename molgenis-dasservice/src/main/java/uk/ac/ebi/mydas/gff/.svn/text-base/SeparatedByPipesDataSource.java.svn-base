package uk.ac.ebi.mydas.examples;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletContext;

import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.configuration.PropertyType;
import uk.ac.ebi.mydas.controller.CacheManager;
import uk.ac.ebi.mydas.datasource.AnnotationDataSource;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.extendedmodel.DasUnknownFeatureSegment;
import uk.ac.ebi.mydas.model.*;

public class SeparatedByPipesDataSource implements AnnotationDataSource {
	CacheManager cacheManager = null;
	ServletContext svCon;
	Map<String, PropertyType> globalParameters;
	DataSourceConfiguration config;
	String path;
	private Collection<DasAnnotatedSegment> segments;
	private Collection<DasType> types;

	public void init(ServletContext servletContext,
			Map<String, PropertyType> globalParameters,
			DataSourceConfiguration dataSourceConfig)
			throws DataSourceException {
		this.svCon = servletContext;
		this.globalParameters = globalParameters;
		this.config = dataSourceConfig;
		path = config.getDataSourceProperties().get("pipes_file").getValue();
		try {
			SeparatedByPipesParser parser = new SeparatedByPipesParser(new FileInputStream(servletContext.getRealPath(path)));
			segments = parser.parse();
			types = parser.getTypes();
		} catch (FileNotFoundException e) {
			throw new DataSourceException("The data source cannot be loaded. The file couldn't be oppened",e);
		} catch (Exception e) {
			throw new DataSourceException("The data source cannot be loaded because of parsing problems",e);
		}
	}

	public void destroy() {
		this.svCon=null;
		this.globalParameters=null;
		this.config=null;
		this.path=null;
		this.segments=null;
		this.types=null;
		this.cacheManager = null;
	}

	public DasAnnotatedSegment getFeatures(String segmentId, Integer maxbeans)
			throws BadReferenceObjectException, DataSourceException {
		for(DasAnnotatedSegment segment:segments){
			if (segment.getSegmentId().equals(segmentId))
				return segment;
		}
		throw new BadReferenceObjectException("The id is not in the file", segmentId);
	}

	public Collection<DasAnnotatedSegment> getFeatures(
			Collection<String> featureIdCollection, Integer maxbins)
			throws UnimplementedFeatureException, DataSourceException {
		Collection<DasAnnotatedSegment> segmentsResponse =new ArrayList<DasAnnotatedSegment>();
		for (String featureId:featureIdCollection){
			boolean found=false;
			for (DasAnnotatedSegment segment:segments){
				for(DasFeature feature:segment.getFeatures())
					if(feature.getFeatureId().equals(featureId)){
						segmentsResponse.add(new DasAnnotatedSegment(segment.getSegmentId(),segment.getStartCoordinate(),segment.getStopCoordinate(),segment.getVersion(),segment.getSegmentLabel(),Collections.singleton(feature)));
						found=true;
						break;
					}else if(this.lookInside((DasComponentFeature)feature, featureId, segmentsResponse, segment)){
						found= true;
						break;
					}
			}
			if(!found)
				segmentsResponse.add(new DasUnknownFeatureSegment(featureId));
		}
		return segmentsResponse;
	}
	private boolean lookInside(DasComponentFeature component,String featureId,Collection<DasAnnotatedSegment> segmentsResponse,DasAnnotatedSegment segment) throws DataSourceException{
		if (component.hasSubParts()){
			for (DasComponentFeature subcomponent: component.getReportableSubComponents()){
				if(subcomponent.getFeatureId().equals(featureId)){
					segmentsResponse.add(new DasAnnotatedSegment(segment.getSegmentId(),segment.getStartCoordinate(),segment.getStopCoordinate(),segment.getVersion(),segment.getSegmentLabel(),Collections.singleton((DasFeature)subcomponent)));
					return true;
				}else
					if(this.lookInside(subcomponent, featureId, segmentsResponse, segment))
						return true;
			}
		}
		return false;
	}

	public URL getLinkURL(String field, String id)
			throws UnimplementedFeatureException, DataSourceException {
		throw new UnimplementedFeatureException("No implemented");
	}
    
    public Collection<DasEntryPoint> getEntryPoints(Integer start, Integer stop) throws UnimplementedFeatureException, DataSourceException {
        throw new UnimplementedFeatureException("No implemented");
    }

    public String getEntryPointVersion() throws UnimplementedFeatureException, DataSourceException {
        throw new UnimplementedFeatureException("No implemented");
    }

    public int getTotalEntryPoints() throws UnimplementedFeatureException, DataSourceException {
        throw new UnimplementedFeatureException("No implemented");
    }

	public Integer getTotalCountForType(DasType type)
			throws DataSourceException {
		int count=0;
		for (DasAnnotatedSegment segment:segments)
			for(DasFeature feature:segment.getFeatures())
				if(type.getId().equals(feature.getType().getId()))
					count++;
		return count;
	}

	public Collection<DasType> getTypes() throws DataSourceException {
		return types;
	}

	public void registerCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
}
