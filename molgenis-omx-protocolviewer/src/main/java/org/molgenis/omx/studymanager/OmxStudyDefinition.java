package org.molgenis.omx.study;

import java.util.Collections;
import java.util.List;

import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.filter.StudyDataRequest;
import org.molgenis.omx.observ.ObservableFeature;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

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
	public void setId(String id)
	{
		studyDataRequest.setIdentifier(id);
	}

	@Override
	public String getName()
	{
		return studyDataRequest.getName();
	}

	@Override
	public String getDescription()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getCatalogVersion()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<StudyDefinitionItem> getItems()
	{
		return Iterables.transform(studyDataRequest.getFeatures(),
				new Function<ObservableFeature, StudyDefinitionItem>()
				{
					@Override
					public StudyDefinitionItem apply(ObservableFeature observableFeature)
					{
						return new OmxStudyDefinitionItem(observableFeature);
					}
				});
	}

	@Override
	public List<String> getAuthors()
	{
		MolgenisUser molgenisUser = studyDataRequest.getMolgenisUser();
		return Collections.singletonList(molgenisUser.getFirstName() + ' ' + molgenisUser.getLastName());
	}

	@Override
	public String getAuthorEmail()
	{
		return studyDataRequest.getMolgenisUser().getEmail();
	}

	@Override
	public boolean containsItem(StudyDefinitionItem anItem)
	{
		boolean contains = false;
		for (StudyDefinitionItem item : getItems())
		{
			if (item.getId().equals(anItem.getId()))
			{
				contains = true;
				break;
			}
		}
		return contains;
	}
}
