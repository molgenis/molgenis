package org.molgenis.omx.studymanager;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.molgenis.catalog.CatalogItem;
import org.molgenis.catalog.UnknownCatalogException;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.study.StudyDataRequest;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.study.StudyDefinition;
import org.molgenis.study.StudyDefinition.Status;
import org.molgenis.study.UnknownStudyDefinitionException;
import org.molgenis.studymanager.StudyManagerService;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class OmxStudyManagerService implements StudyManagerService
{
	private final DataService dataService;
	private final MolgenisUserService molgenisUserService;

	public OmxStudyManagerService(DataService dataService, MolgenisUserService molgenisUserService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		if (molgenisUserService == null) throw new IllegalArgumentException("MolgenisUserService is null");
		this.dataService = dataService;
		this.molgenisUserService = molgenisUserService;
	}

	@Override
	public List<StudyDefinition> getStudyDefinitions()
	{
		Iterable<StudyDataRequest> studyDataRequests = dataService.findAll(StudyDataRequest.ENTITY_NAME,
				StudyDataRequest.class);

		return Lists.newArrayList(Iterables.transform(studyDataRequests,
				new Function<StudyDataRequest, StudyDefinition>()
				{
					@Override
					public StudyDefinition apply(StudyDataRequest studyDataRequest)
					{
						return new OmxStudyDefinition(studyDataRequest, dataService);
					}
				}));
	}

	@Override
	public List<StudyDefinition> getStudyDefinitions(String username, Status status)
	{
		MolgenisUser user = molgenisUserService.getUser(username);
		Iterable<StudyDataRequest> studyDataRequest = dataService.findAll(
				StudyDataRequest.ENTITY_NAME,
				new QueryImpl().eq(StudyDataRequest.MOLGENISUSER, user).and()
						.eq(StudyDataRequest.REQUESTSTATUS, status.toString().toLowerCase()), StudyDataRequest.class);

		return Lists.newArrayList(Iterables.transform(studyDataRequest,
				new Function<StudyDataRequest, StudyDefinition>()
				{
					@Override
					public StudyDefinition apply(StudyDataRequest studyDataRequest)
					{
						return new OmxStudyDefinition(studyDataRequest, dataService);
					}
				}));
	}

	@Override
	public StudyDefinition getStudyDefinition(String id) throws UnknownStudyDefinitionException
	{
		StudyDataRequest studyDataRequest = dataService.findOne(StudyDataRequest.ENTITY_NAME,
				new QueryImpl().eq(StudyDataRequest.ID, id), StudyDataRequest.class);
		if (studyDataRequest == null) throw new UnknownStudyDefinitionException("Study definition [" + id
				+ "] does not exist");

		return new OmxStudyDefinition(studyDataRequest, dataService);
	}

	@Override
	public boolean canLoadStudyData()
	{
		return false;
	}

	@Override
	public void loadStudyData(String id) throws UnknownStudyDefinitionException
	{
		if (canLoadStudyData())
		{
			// FIXME implement load study data operation
			throw new UnknownStudyDefinitionException(
					"Study data loading not support, see http://www.molgenis.org/ticket/2072");
		}
	}

	@Override
	public boolean isStudyDataLoaded(String id) throws UnknownStudyDefinitionException
	{
		if (canLoadStudyData())
		{
			// FIXME implement load study data operation
			throw new UnknownStudyDefinitionException(
					"Study data loading not support, see http://www.molgenis.org/ticket/2072");
		}
		return false;
	}

	@Override
	public StudyDefinition createStudyDefinition(String username, String catalogId)
	{
		return createStudyDefinition(username, catalogId, UUID.randomUUID().toString());
	}

	public StudyDefinition createStudyDefinition(String username, String catalogId, String omxIdentifier)
	{
		MolgenisUser user = molgenisUserService.getUser(username);
		Protocol protocol = dataService.findOne(Protocol.ENTITY_NAME, new QueryImpl().eq(Protocol.ID, catalogId),
				Protocol.class);

		StudyDataRequest studyDataRequest = new StudyDataRequest();
		studyDataRequest.setIdentifier(omxIdentifier);
		studyDataRequest.setName(Status.DRAFT.toString());
		studyDataRequest.setProtocol(protocol);
		studyDataRequest.setMolgenisUser(user);
		studyDataRequest.setRequestDate(new Date());
		studyDataRequest.setRequestStatus(Status.DRAFT.toString().toLowerCase());
		studyDataRequest.setRequestForm("placeholder");
		dataService.add(StudyDataRequest.ENTITY_NAME, studyDataRequest);

		return new OmxStudyDefinition(studyDataRequest, dataService);
	}

	@Override
	public void updateStudyDefinition(StudyDefinition studyDefinition) throws UnknownStudyDefinitionException
	{
		String id = studyDefinition.getId();
		Query q = new QueryImpl().eq(StudyDataRequest.ID, id);
		StudyDataRequest studyDataRequest = dataService
				.findOne(StudyDataRequest.ENTITY_NAME, q, StudyDataRequest.class);
		if (studyDataRequest == null)
		{
			throw new UnknownStudyDefinitionException("Study definition [" + id + "] does not exist");
		}

		studyDataRequest.setName(studyDataRequest.getName());
		studyDataRequest.setFeatures(Lists.newArrayList(Lists.transform(studyDefinition.getItems(),
				new Function<CatalogItem, ObservableFeature>()
				{

					@Override
					public ObservableFeature apply(CatalogItem catalogItem)
					{
						String id = catalogItem.getId();
						ObservableFeature feature = dataService.findOne(ObservableFeature.ENTITY_NAME,
								new QueryImpl().eq(ObservableFeature.ID, id), ObservableFeature.class);
						if (feature == null)
						{
							throw new RuntimeException("Observable feature does not exist identifier: " + id);
						}

						return feature;
					}
				})));

		dataService.update(StudyDataRequest.ENTITY_NAME, studyDataRequest);
	}

	@Override
	public void submitStudyDefinition(String id, String catalogId) throws UnknownStudyDefinitionException,
			UnknownCatalogException
	{
		Query q = new QueryImpl().eq(StudyDataRequest.ID, id);
		StudyDataRequest studyDataRequest = dataService
				.findOne(StudyDataRequest.ENTITY_NAME, q, StudyDataRequest.class);
		if (studyDataRequest == null)
		{
			throw new UnknownStudyDefinitionException("Study definition [" + id + "] does not exist");
		}

		if (!studyDataRequest.getRequestStatus().equalsIgnoreCase(Status.DRAFT.toString()))
		{
			throw new RuntimeException("Study data request with status '" + studyDataRequest.getRequestStatus()
					+ "' is not submittable");
		}
		studyDataRequest.setRequestStatus(Status.SUBMITTED.toString().toLowerCase());
		dataService.update(StudyDataRequest.ENTITY_NAME, studyDataRequest);
	}
}
