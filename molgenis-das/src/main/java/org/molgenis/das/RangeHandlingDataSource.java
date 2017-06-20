package org.molgenis.das;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.das.impl.MolgenisDasTarget;
import uk.ac.ebi.mydas.datasource.RangeHandlingAnnotationDataSource;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.CoordinateErrorException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.model.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@SuppressFBWarnings(value = "DMI_COLLECTION_OF_URLS", justification = "Third party class requires a map of URLs")
public abstract class RangeHandlingDataSource implements RangeHandlingAnnotationDataSource
{
	protected DasFeature createDasFeature(Integer start, Integer stop, String identifier, String name,
			String description, String link, DasType type, DasMethod method, String dataSet, String patient,
			List<String> notes) throws DataSourceException
	{
		if (stop == null) stop = start;// no stop? assume length of 1;

		// create description based on available information
		String featureDescription = "";
		if (StringUtils.isNotEmpty(description))
		{
			featureDescription = StringUtils.isNotEmpty(name) ? name + "," + description : description;
		}
		else
		{
			featureDescription = identifier;
		}
		notes.add("track:" + dataSet);
		notes.add("source:MOLGENIS");
		if (StringUtils.isNotEmpty(patient))
		{
			notes.add("patient:" + patient);
		}

		Map<URL, String> linkout = new HashMap<URL, String>();
		try
		{
			linkout.put(new URL(link), "Link");
		}
		catch (MalformedURLException e)
		{
		}

		List<DasTarget> dasTargets = new ArrayList<DasTarget>();
		dasTargets.add(new MolgenisDasTarget(identifier, start, stop, featureDescription));

		List<String> parents = new ArrayList<String>();
		DasFeature feature = new DasFeature(identifier, featureDescription, type, method, start, stop, new Double(0),
				DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE, DasPhase.PHASE_NOT_APPLICABLE, notes, linkout,
				dasTargets, parents, null);
		return feature;
	}

	// unimplemented functions
	@Override
	public void destroy()
	{
		// Mandatory to override this function, but no destroy needed.
	}

	@Override
	public DasAnnotatedSegment getFeatures(String arg0, Integer arg1)
			throws BadReferenceObjectException, DataSourceException
	{
		throw new BadReferenceObjectException(arg0, "The handling of this request is not supported");
	}

	@Override
	public Collection<DasAnnotatedSegment> getFeatures(Collection<String> arg0, Integer arg1, Range arg2)
			throws UnimplementedFeatureException, DataSourceException
	{
		throw new UnimplementedFeatureException("Not implemented");
	}

	@Override
	public URL getLinkURL(String arg0, String arg1) throws UnimplementedFeatureException, DataSourceException
	{
		throw new UnimplementedFeatureException("Not implemented");
	}

	@Override
	public int getTotalEntryPoints() throws UnimplementedFeatureException, DataSourceException
	{
		throw new UnimplementedFeatureException("Not implemented");
	}

	@Override
	public DasAnnotatedSegment getFeatures(String arg0, int arg1, int arg2, Integer arg3, Range arg4)
			throws BadReferenceObjectException, CoordinateErrorException, DataSourceException,
			UnimplementedFeatureException
	{
		throw new UnimplementedFeatureException("Not implemented");
	}

	@Override
	public DasAnnotatedSegment getFeatures(String arg0, Integer arg1, Range arg2)
			throws BadReferenceObjectException, DataSourceException, UnimplementedFeatureException
	{
		throw new UnimplementedFeatureException("Not implemented");
	}

	@Override
	public Collection<DasAnnotatedSegment> getFeatures(Collection<String> arg0, Integer arg1)
			throws UnimplementedFeatureException, DataSourceException
	{
		throw new UnimplementedFeatureException("Not implemented");
	}

	@Override
	public String getEntryPointVersion() throws UnimplementedFeatureException, DataSourceException
	{
		throw new UnimplementedFeatureException("Not implemented");
	}

	@Override
	public Collection<DasEntryPoint> getEntryPoints(Integer segmentId, Integer notUsed)
			throws UnimplementedFeatureException, DataSourceException
	{
		throw new UnimplementedFeatureException("Not implemented");
	}
}
