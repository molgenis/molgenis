/*
 * Copyright 2007 Philip Jones, EMBL-European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * For further details of the mydas project, including source code,
 * downloads and documentation, please see:
 *
 * http://code.google.com/p/mydas/
 *
 */

package uk.ac.ebi.mydas.template;



import javax.servlet.ServletContext;

import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.configuration.PropertyType;
import uk.ac.ebi.mydas.controller.CacheManager;
import uk.ac.ebi.mydas.datasource.AnnotationDataSource;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasEntryPoint;
import uk.ac.ebi.mydas.model.DasType;

import java.net.URL;
import java.util.Collection;
import java.util.Map;

/**
 * Created Using IntelliJ IDEA.
 * Date: 18-Jul-2007
 * Time: 16:51:37
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * NOTE TO DATA SOURCE DEVELOPERS:
 *
 * This template is based upon the AnnotationDataSource interface,
 * there are however three other interfaces available that may be
 * more appropriate for your needs, described here:
 *
 * <a href="http://code.google.com/p/mydas/wiki/HOWTO_WritePluginIntro">
 *     Writing a MyDas Data Source - Selecting the Best Inteface
 * </a> 
 */
public class TemplateAnnotationDataSource implements AnnotationDataSource {

	CacheManager cacheManager = null;
	ServletContext svCon;
	Map<String, PropertyType> globalParameters;
	DataSourceConfiguration config;


	/**
	 * This method is called by the MydasServlet class at Servlet initialisation.
	 * <p/>
	 * The AnnotationDataSource is passed the servletContext, a handle to globalParameters in the
	 * form of a Map &lt;String, String&gt; and a DataSourceConfiguration object.
	 * <p/>
	 * The latter two parameters contain all of the pertinent information in the
	 * ServerConfig.xml file relating to the server as a whole and specifically to
	 * this data source.  This mechanism allows the datasource author to set up
	 * required configuration in one place, including AnnotationDataSource specific configuration.
	 * <p/>
	 * <bold>It is highly desirable for the implementation to test itself in this init method and throw
	 * a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
	 * and read a record.</bold>
	 *
	 * @param servletContext   being the ServletContext of the servlet container that the
	 *                         Mydas servlet is running in.
	 * @param globalParameters being a Map &lt;String, String&gt; of keys and values
	 *                         as defined in the ServerConfig.xml file.
	 * @param dataSourceConfig containing the pertinent information frmo the ServerConfig.xml
	 *                         file for this datasource, including (optionally) a Map of datasource specific configuration.
	 * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
	 *          should be thrown if there is any
	 *          fatal problem with loading this data source.  <bold>It is highly desirable
	 *          for the implementation to test itself in this init method and throw
	 *          a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
	 *          and read a record.</bold>
	 */
	public void init(ServletContext servletContext, Map<String, PropertyType> globalParameters, DataSourceConfiguration dataSourceConfig) throws DataSourceException {
		this.svCon = servletContext;
		this.globalParameters = globalParameters;
		this.config = dataSourceConfig;
	}

	/**
	 * This method is called when the DAS server is shut down and should be used
	 * to clean up resources such as database connections as required.
	 */
	public void destroy() {
	}

	/**
	 * This method returns a List of DasAnnotatedSegment objects, describing the annotated segment and the features
	 * of the segmentId passed in as argument.
	 *
	 * @param segmentId being the reference of the segment requested in the DAS request (not including
	 *                  start and stop coordinates)
	 *                  <p/>
	 *                  If your datasource implements only this interface,
	 *                  the MydasServlet will handle restricting the features returned to
	 *                  the start / stop coordinates in the request and you will only need to
	 *                  implement this method to return Features.  If on the other hand, your data source
	 *                  includes massive segments, you may wish to implement the
	 *                  {@link uk.ac.ebi.mydas.datasource.RangeHandlingAnnotationDataSource}
	 *                  interface.  It will then be the responsibility of your AnnotationDataSource plugin to
	 *                  restrict the features returned for the requested range.
	 * @return A DasAnnotatedSegment object.  This describes the segment that is annotated, limited
	 *         to the information required for the /DASGFF/GFF/SEGMENT element.  References a Collection of
	 *         DasFeature objects.   Note that this is a basic Collection - this gives you complete control over the details
	 *         of the Collection type - so you can create your own comparators etc.
	 * @throws uk.ac.ebi.mydas.exceptions.BadReferenceObjectException
	 *          in the event that your server does not include information about this segment.
	 * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
	 *          should be thrown if there is any
	 *          fatal problem with loading this data source.  <bold>It is highly desirable
	 *          for the implementation to test itself in this init method and throw
	 *          a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
	 *          and read a record.</bold>
	 */
	public DasAnnotatedSegment getFeatures(String segmentId,Integer maxbins) throws BadReferenceObjectException, DataSourceException {
		return null;
	}

	/**
	 * This method is used to implement the DAS types command.  (See <a href="http://biodas.org/documents/spec.html#types">
	 * DAS 1.53 Specification : types command</a>.  This method should return a Collection containing <b>all</b> the
	 * types described by the data source (one DasType object for each type ID).
	 * <p/>
	 * For some data sources it may be desirable to populate this Collection from a configuration file or to
	 *
	 * @return a Collection of DasType objects - one for each type id described by the data source.
	 * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
	 *          should be thrown if there is any
	 *          fatal problem with loading this data source.  <bold>It is highly desirable
	 *          for the implementation to test itself in this init method and throw
	 *          a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
	 *          and read a record.</bold>
	 */
	public Collection<DasType> getTypes() throws DataSourceException {
		return null;
	}

	/**
	 * <b>For some Datasources, especially ones with many entry points, this method may be hard or impossible
	 * to implement.  If this is the case, you should just throw an {@link uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException} as your
	 * implementation of this method, so that a suitable error HTTP header
	 * (X-DAS-Status: 501 Unimplemented feature) is returned to the DAS client as
	 * described in the DAS 1.53 protocol.</b><br/><br/>
	 * <p/>
	 * This method is used by the features command when no segments are included, but feature_id and / or
	 * group_id filters have been included, to meet the following specification:<br/><br/>
	 * <p/>
	 * "<b>feature_id</b> (zero or more; new in 1.5)<br/>
	 * Instead of, or in addition to, <b>segment</b> arguments, you may provide one or more <b>feature_id</b>
	 * arguments, whose values are the identifiers of particular features.  If the server supports this operation,
	 * it will translate the feature ID into the segment(s) that strictly enclose them and return the result in
	 * the <i>features</i> response.  It is possible for the server to return multiple segments if the requested
	 * feature is present in multiple locations.
	 * <b>group_id</b> (zero or more; new in 1.5)<br/>
	 * The <b>group_id</b> argument, is similar to <b>feature_id</b>, but retrieves segments that contain
	 * the indicated feature group."  (Direct quote from the DAS 1.53 specification, available from
	 * <a href="http://biodas.org/documents/spec.html#features">http://biodas.org/documents/spec.html#features</a>.)
	 * <p/>
	 * Note that if segments are included in the request, this method is not used, so feature_id and group_id
	 * filters accompanying a list of segments will work correctly, even if your implementation of this method throws an
	 * {@link uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException}.
	 *
	 * @param featureIdCollection a Collection&lt;String&gt; of feature_id values included in the features command / request.
	 *                            May be a <code>java.util.Collections.EMPTY_LIST</code> but will <b>not</b> be null.
	 * @return A Collection of {@link uk.ac.ebi.mydas.model.DasAnnotatedSegment} objects. These describe the segments that is annotated, limited
	 *         to the information required for the /DASGFF/GFF/SEGMENT element.  Each References a Collection of
	 *         DasFeature objects.   Note that this is a basic Collection - this gives you complete control over the details
	 *         of the Collection type - so you can create your own comparators etc.
	 * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
	 *          should be thrown if there is any
	 *          fatal problem with loading this data source.  <bold>It is highly desirable for the
	 *          implementation to test itself in this init method and throw
	 *          a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
	 *          and read a record.</bold>
	 * @throws uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException
	 *          Throw this if you cannot
	 *          provide a working implementation of this method.
	 */
	public Collection<DasAnnotatedSegment> getFeatures(Collection<String> featureIdCollection, Integer maxbins)
	throws UnimplementedFeatureException, DataSourceException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * This method allows the DAS server to report a total count for a particular type
	 * for all annotations across the entire data source.  If it is not possible to retrieve this value from your dsn, you
	 * should return <code>null</code>.
	 *
	 * @param type containing the information needed to retrieve the type count
	 *             (type id and optionally the method id and category id.  Note that the last two may
	 *             be null, which needs to be taken into account by the implementation.)
	 * @return The total count <i>across the entire data source</i> (not
	 *         just for one segment) for the specified type.  If it is not possible to determine
	 *         this count, this method should return <code>null</code>.
	 * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
	 *          should be thrown if there is any
	 *          fatal problem with loading this data source.  <bold>It is highly desirable for the
	 *          implementation to test itself in this init method and throw
	 *          a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
	 *          and read a record.</bold>
	 */
	public Integer getTotalCountForType(DasType type) throws DataSourceException {
		return null;
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
	 * This method returns a URL, based upon a request built as part of the DAS 'link' command.
	 * The nature of this URL is entirely up to the data source implementor.
	 * <p/>
	 * The mydas servlet will redirect to the URL provided.  This command is intended for use in an internet browser,
	 * so the URL returned should be a valid internet address.  The page can return content of any MIME type and
	 * is intended to be 'human readable' rather than material for consumption by a DAS client.
	 * <p/>
	 * The link command takes two mandatory
	 * arguments:
	 * <ul>
	 * <li>
	 * a 'field' parameter which is limited to one of five valid values.  This method is guaranteed
	 * to be called with the 'field' parameter set to one of these values (any other request will be handled as
	 * an error by the mydas DAS server servlet.)  The 'field' parameter will be one of the five static String constants
	 * that are members of the AnnotationDataSource interface.
	 * </li>
	 * <li>
	 * an 'id' field.  Again, this will be validated by the mydas servlet to ensure that it
	 * is a non-null, non-zero length String.
	 * </li>
	 * <ul>
	 * See <a href="http://biodas.org/documents/spec.html#feature_linking">DAS 1.53 Specification: Linking to a Feature</a>
	 * for details.
	 * <p/>
	 * If your data source does not implement this method, an UnimplementedFeatureException should be thrown.
	 *
	 * @param field one of 'feature', 'type', 'method', 'category' or 'target' as documented in the DAS 1.53
	 *              specification
	 * @param id    being the ID of the indicated annotation field
	 * @return a valid URL.
	 * @throws uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException
	 *          in the event that the DAS data source
	 *          does not implement the link command
	 * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
	 *          should be thrown if there is any
	 *          fatal problem with loading this data source.  <bold>It is highly desirable for the implementation
	 *          to test itself in this init method and throw
	 *          a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
	 *          and read a record.</bold>
	 */
	public URL getLinkURL(String field, String id) throws UnimplementedFeatureException, DataSourceException {
		return null;
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


}
