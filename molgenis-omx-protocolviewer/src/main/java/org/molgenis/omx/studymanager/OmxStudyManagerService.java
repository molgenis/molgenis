package org.molgenis.omx.studymanager;

import java.util.Collections;
import java.util.List;

import org.molgenis.catalog.CatalogItem;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
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
	private final Database database;

	public OmxStudyManagerService(Database database)
	{
		if (database == null) throw new IllegalArgumentException("database is null");
		this.database = database;
	}

	@Override
	public List<StudyDefinitionMeta> getStudyDefinitions()
	{
		List<StudyDataRequest> studyDataRequests;
		try
		{
			studyDataRequests = database.find(StudyDataRequest.class);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
		if (studyDataRequests == null) return Collections.emptyList();

		return Lists.transform(studyDataRequests, new Function<StudyDataRequest, StudyDefinitionMeta>()
		{
			@Override
			public StudyDefinitionMeta apply(StudyDataRequest studyDataRequest)
			{
				return new StudyDefinitionMeta(studyDataRequest.getIdentifier(), studyDataRequest.getName());
			}
		});
	}

	@Override
	public StudyDefinition getStudyDefinition(String id) throws UnknownStudyDefinitionException
	{
		StudyDataRequest studyDataRequest;
		try
		{
			studyDataRequest = StudyDataRequest.findByIdentifier(database, id);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
		if (studyDataRequest == null) throw new UnknownStudyDefinitionException("Study definition [" + id
				+ "] does not exist");
		return new OmxStudyDefinition(studyDataRequest);
	}

	@Override
	public void loadStudyData(String id) throws UnknownStudyDefinitionException
	{
		// TODO decide how to implement
	}

	@Override
	public boolean isStudyDataLoaded(String id) throws UnknownStudyDefinitionException
	{
		// TODO decide how to implement
		return false;
	}

	@Override
	public void updateStudyDefinition(StudyDefinition studyDefinition) throws UnknownStudyDefinitionException
	{
		String id = studyDefinition.getId();
		StudyDataRequest studyDataRequest;
		try
		{
			studyDataRequest = StudyDataRequest.findByIdentifier(database, id);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
		if (studyDataRequest == null) throw new UnknownStudyDefinitionException("Study definition [" + id
				+ "] does not exist");

		studyDataRequest.setName(studyDataRequest.getName());
		studyDataRequest.setFeatures(Lists.transform(studyDefinition.getItems(),
				new Function<CatalogItem, ObservableFeature>()
				{

					@Override
					public ObservableFeature apply(CatalogItem catalogItem)
					{
						String id = catalogItem.getId();
						ObservableFeature feature;
						try
						{
							feature = ObservableFeature.findByIdentifier(database, id);
						}
						catch (DatabaseException e)
						{
							throw new RuntimeException(e);
						}
						if (feature == null)
						{
							throw new RuntimeException("Observable feature does not exist identifier: " + id);
						}
						return feature;
					}
				}));
		try
		{
			database.update(studyDataRequest); // FIXME Duplicate entry '...' for key 'PRIMARY' exceptions
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public StudyDefinition persistStudyDefinition(StudyDefinition studyDefinition)
	{
		// do nothing, at the moment study definitions are persisted in OrderStudyDataService
		return studyDefinition;
	}
}
