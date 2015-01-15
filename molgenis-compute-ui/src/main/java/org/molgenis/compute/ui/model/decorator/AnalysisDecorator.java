package org.molgenis.compute.ui.model.decorator;

import java.util.EnumSet;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.compute.ui.meta.AnalysisMetaData;
import org.molgenis.compute.ui.model.Analysis;
import org.molgenis.compute.ui.model.AnalysisStatus;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.CrudRepositoryDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AnalysisJob decorator that updates analysis status on job status change
 * 
 * TODO handle analysis job deletes
 */
public class AnalysisDecorator extends CrudRepositoryDecorator
{
	private final CrudRepository decoratedRepository;
	private final DataService dataService;

	private static final Logger LOG = LoggerFactory.getLogger(AnalysisDecorator.class);

	public AnalysisDecorator(CrudRepository decoratedRepository, DataService dataService)
	{
		super(decoratedRepository);
		this.decoratedRepository = decoratedRepository;
		this.dataService = dataService;
	}

	@Override
	public void add(Entity entity)
	{
		// validate job status
		validateNewAnalysisStatus(entity);

		// assign current user
		decoratedRepository.add(entity);
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		// validate job status
		for (Entity entity : entities)
			validateNewAnalysisStatus(entity);

		return decoratedRepository.add(entities);
	}

	@Override
	public void update(Entity entity)
	{
		// validate job status
		validateExistingAnalysisStatus(entity);
		decoratedRepository.update(entity);
	}

	@Override
	public void update(Iterable<? extends Entity> entities)
	{
		// validate job status
		for (Entity entity : entities)
			validateExistingAnalysisStatus(entity);

		decoratedRepository.update(entities);
	}

	@Override
	public void delete(Entity entity)
	{
		// validate if delete is allowed
		validateIsAnalysisDeleteAllowed(entity);

		decoratedRepository.delete(entity);
	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		// validate if delete is allowed
		for (Entity entity : entities)
			validateIsAnalysisDeleteAllowed(entity);

		decoratedRepository.delete(entities);
	}

	@Override
	public void deleteById(Iterable<Object> ids)
	{
		// validate if delete is allowed
		Iterable<Analysis> analysis = dataService.findAll(AnalysisMetaData.INSTANCE.getName(), ids, Analysis.class);
		for (Entity entity : analysis)
			validateIsAnalysisDeleteAllowed(entity);

		decoratedRepository.deleteById(ids);
	}

	@Override
	public void deleteAll()
	{
		// validate if delete is allowed
		Iterable<Analysis> analysis = dataService.findAll(AnalysisMetaData.INSTANCE.getName(), Analysis.class);
		for (Entity entity : analysis)
			validateIsAnalysisDeleteAllowed(entity);

		decoratedRepository.deleteAll();
	}

	@Override
	public void deleteById(Object id)
	{
		// validate if delete is allowed
		Analysis analysis = dataService.findOne(AnalysisMetaData.INSTANCE.getName(), id, Analysis.class);
		validateIsAnalysisDeleteAllowed(analysis);

		decoratedRepository.deleteById(id);
	}

	private void assignCurrentUser(Entity entity)
	{

		String username = SecurityUtils.getCurrentUsername();
		MolgenisUser user = dataService.findOne(MolgenisUser.ENTITY_NAME,
				new QueryImpl().eq(MolgenisUser.USERNAME, username), MolgenisUser.class);
		entity.set(AnalysisMetaData.USER, user);
	}

	private boolean validateIsAnalysisDeleteAllowed(Entity entity)
	{
		AnalysisStatus analysisStatus = AnalysisStatus.valueOf(entity.getString(AnalysisMetaData.STATUS));
		if (analysisStatus == AnalysisStatus.CREATED) return true;
		else throw new RuntimeException("Deleting analysis with status " + analysisStatus + " is not allowed");
	}

	private void validateNewAnalysisStatus(Entity entity)
	{
		AnalysisStatus analysisStatus = AnalysisStatus.valueOf(entity.getString(AnalysisMetaData.STATUS));
		if (!analysisStatus.isInitialState())
		{
			String attributeLabel = AnalysisMetaData.INSTANCE.getAttribute(AnalysisMetaData.STATUS).getLabel();
			throw new RuntimeException(attributeLabel + " is not an initial state");
		}
	}

	private void validateExistingAnalysisStatus(Entity entity)
	{
		Analysis currentAnalysis = dataService.findOne(AnalysisMetaData.INSTANCE.getName(), entity.getIdValue(),
				Analysis.class);
		AnalysisStatus currentAnalysisStatus = currentAnalysis.getStatus();

		AnalysisStatus analysisStatus = AnalysisStatus.valueOf(entity.getString(AnalysisMetaData.STATUS));
		if (analysisStatus != currentAnalysisStatus)
		{
			EnumSet<AnalysisStatus> allowedStateTransitions = currentAnalysisStatus.getStateTransitions();
			if (!allowedStateTransitions.contains(analysisStatus))
			{
				String attributeLabel = AnalysisMetaData.INSTANCE.getAttribute(AnalysisMetaData.STATUS).getLabel();
				LOG.warn(attributeLabel + " transition from " + currentAnalysisStatus + " to "
						+ analysisStatus + " is not allowed. Allowed transitions " + allowedStateTransitions);
			}
		}
	}
}
