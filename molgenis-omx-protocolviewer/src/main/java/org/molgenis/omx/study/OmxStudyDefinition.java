package org.molgenis.omx.study;

import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.filter.StudyDataRequest;
import org.molgenis.omx.observ.ObservableFeature;

public class OmxStudyDefinition implements StudyDefinition
{
	private final StudyDataRequest studyDataRequest;

	public OmxStudyDefinition(StudyDataRequest studyDataRequest)
	{
		if (studyDataRequest == null) throw new IllegalArgumentException("study data request is null");
		this.studyDataRequest = studyDataRequest;
	}

	@Override
	public String getId()
	{
		return studyDataRequest.getIdentifier();
	}

	@Override
	public String getName()
	{
		return studyDataRequest.getName();
	}

	@Override
	public Iterable<ObservableFeature> getFeatures()
	{
		return studyDataRequest.getFeatures();
	}

	@Override
	public MolgenisUser getAuthor()
	{
		return studyDataRequest.getMolgenisUser();
	}
}
