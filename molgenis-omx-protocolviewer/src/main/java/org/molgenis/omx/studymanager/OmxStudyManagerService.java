package org.molgenis.omx.studymanager;

import java.util.List;

import org.molgenis.catalog.CatalogItem;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.study.StudyDataRequest;
import org.molgenis.study.StudyDefinition;
import org.molgenis.study.StudyDefinitionMeta;
import org.molgenis.study.UnknownStudyDefinitionException;
import org.molgenis.studymanager.StudyManagerService;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class OmxStudyManagerService implements StudyManagerService
{
	private final DataService dataService;

	public OmxStudyManagerService(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		this.dataService = dataService;
	}

	@Override
	public List<StudyDefinitionMeta> getStudyDefinitions()
	{
		List<StudyDataRequest> studyDataRequests = dataService.findAllAsList(StudyDataRequest.ENTITY_NAME,
				new QueryImpl());

		return Lists.transform(studyDataRequests, new Function<StudyDataRequest, StudyDefinitionMeta>()
		{
			@Override
			public StudyDefinitionMeta apply(StudyDataRequest studyDataRequest)
			{
				return new StudyDefinitionMeta(studyDataRequest.getIdentifier(), studyDataRequest.getName(),
						studyDataRequest.getMolgenisUser().getEmail(), studyDataRequest.getRequestDate());
			}
		});
	}

	@Override
	public StudyDefinition getStudyDefinition(String id) throws UnknownStudyDefinitionException
	{
		StudyDataRequest studyDataRequest = dataService.findOne(StudyDataRequest.ENTITY_NAME,
				new QueryImpl().eq(StudyDataRequest.IDENTIFIER, id));
		if (studyDataRequest == null) throw new UnknownStudyDefinitionException("Study definition [" + id
				+ "] does not exist");

		return new OmxStudyDefinition(studyDataRequest);
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
	public void updateStudyDefinition(StudyDefinition studyDefinition) throws UnknownStudyDefinitionException
	{
		String id = studyDefinition.getId();
		StudyDataRequest studyDataRequest = dataService.findOne(StudyDataRequest.ENTITY_NAME,
				new QueryImpl().eq(StudyDataRequest.IDENTIFIER, id));

		if (studyDataRequest == null) throw new UnknownStudyDefinitionException("Study definition [" + id
				+ "] does not exist");

		studyDataRequest.setName(studyDataRequest.getName());
		studyDataRequest.setFeatures(Lists.newArrayList(Lists.transform(studyDefinition.getItems(),
				new Function<CatalogItem, ObservableFeature>()
				{

					@Override
					public ObservableFeature apply(CatalogItem catalogItem)
					{
						String id = catalogItem.getId();
						ObservableFeature feature = dataService.findOne(ObservableFeature.ENTITY_NAME,
								new QueryImpl().eq(ObservableFeature.IDENTIFIER, id));
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
	public StudyDefinition persistStudyDefinition(StudyDefinition studyDefinition)
	{
		// do nothing, at the moment study definitions are persisted in OrderStudyDataService
		return studyDefinition;
	}
}
