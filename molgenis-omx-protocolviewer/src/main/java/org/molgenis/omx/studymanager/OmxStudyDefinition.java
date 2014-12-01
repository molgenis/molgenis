package org.molgenis.omx.studymanager;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.molgenis.catalog.CatalogFolder;
import org.molgenis.catalog.CatalogItem;
import org.molgenis.data.DataService;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.study.StudyDataRequest;
import org.molgenis.study.StudyDefinition;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
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
	public void setName(String name)
	{
		studyDataRequest.setName(name);
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
	public Iterable<CatalogFolder> getItems()
	{
		return Lists.transform(studyDataRequest.getProtocols(), new Function<Protocol, CatalogFolder>()
		{
			@Override
			public CatalogFolder apply(Protocol protocol)
			{
				return new OmxStudyDefinitionItem(protocol, studyDataRequest.getProtocol().getId());
			}
		});
	}

	@Override
	public void setItems(Iterable<CatalogFolder> items)
	{
		List<Protocol> protocols;
		if (items.iterator().hasNext())
		{
			Iterable<Protocol> protocolIterable = dataService.findAll(Protocol.ENTITY_NAME,
					Iterables.transform(items, new Function<CatalogItem, Object>()
					{
						@Override
						public Object apply(CatalogItem catalogItem)
						{
							return Integer.valueOf(catalogItem.getId());
						}
					}), Protocol.class);
			protocols = Lists.newArrayList(protocolIterable);
		}
		else
		{
			protocols = Collections.emptyList();
		}
		studyDataRequest.setProtocols(protocols);
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
	public void setRequestProposalForm(String path)
	{
		studyDataRequest.setRequestForm(path);
	}

	@Override
	public boolean containsItem(CatalogFolder anItem)
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

	@Override
	public String getExternalId()
	{
		return studyDataRequest.getExternalId();
	}

	@Override
	public void setExternalId(String externalId)
	{
		studyDataRequest.setExternalId(externalId);
	}
}
