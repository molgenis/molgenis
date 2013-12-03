package org.molgenis.omx.studymanager;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.molgenis.catalog.CatalogItem;
import org.molgenis.data.DataService;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.study.StudyDataRequest;
import org.molgenis.study.StudyDefinition;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class OmxStudyDefinition implements StudyDefinition
{
	private final StudyDataRequest studyDataRequest;
	private final DataService dataService;

	public OmxStudyDefinition(StudyDataRequest studyDataRequest, DataService dataService)
	{
		if (studyDataRequest == null) throw new IllegalArgumentException("study data request is null");
		if (dataService == null) throw new IllegalArgumentException("data service is null");
		this.studyDataRequest = studyDataRequest;
		this.dataService = dataService;
	}

	@Override
	public String getId()
	{
		return studyDataRequest.getId().toString();
	}

	@Override
	public void setId(String id)
	{
		studyDataRequest.setId(Integer.valueOf(id));
	}

	@Override
	public String getName()
	{
		return studyDataRequest.getName();
	}

	@Override
	public String getDescription()
	{
		return null; // TODO https://github.com/molgenis/molgenis/issues/859
	}

	@Override
	public String getVersion()
	{
		return null; // TODO https://github.com/molgenis/molgenis/issues/860
	}

	@Override
	public Date getDateCreated()
	{
		return new Date(studyDataRequest.getRequestDate().getTime());
	}

	@Override
	public Status getStatus()
	{
		String requestStatus = studyDataRequest.getRequestStatus();
		return Status.valueOf(requestStatus.toUpperCase());
	}

	@Override
	public List<CatalogItem> getItems()
	{
		return Lists.transform(studyDataRequest.getFeatures(), new Function<ObservableFeature, CatalogItem>()
		{
			@Override
			public CatalogItem apply(ObservableFeature observableFeature)
			{
				return new OmxStudyDefinitionItem(observableFeature);
			}
		});
	}

	@Override
	public void setItems(List<CatalogItem> items)
	{
		List<ObservableFeature> features;
		if (!items.isEmpty())
		{
			Iterable<ObservableFeature> featuresIterable = dataService.findAll(ObservableFeature.ENTITY_NAME,
					Lists.transform(items, new Function<CatalogItem, Integer>()
					{
						@Override
						public Integer apply(CatalogItem catalogItem)
						{
							return Integer.valueOf(catalogItem.getId());
						}
					}));
			features = Lists.newArrayList(featuresIterable);
		}
		else
		{
			features = Collections.emptyList();
		}
		studyDataRequest.setFeatures(features);
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
	public String getRequestProposalForm()
	{
		return studyDataRequest.getRequestForm();
	}

	@Override
	public boolean containsItem(CatalogItem anItem)
	{
		boolean contains = false;
		for (CatalogItem item : getItems())
		{
			if (item.getId().equals(anItem.getId()))
			{
				contains = true;
				break;
			}
		}
		return contains;
	}

	public StudyDataRequest getStudyDataRequest()
	{
		return studyDataRequest;
	}
}
