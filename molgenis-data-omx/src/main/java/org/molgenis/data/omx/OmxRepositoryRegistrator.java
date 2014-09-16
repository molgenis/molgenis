package org.molgenis.data.omx;

import org.molgenis.data.CrudRepositorySecurityDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.omx.importer.OmxImporterServiceImpl;
import org.molgenis.data.validation.EntityValidator;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.security.runas.SystemSecurityToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Add startup add an OmxRepository to the DataService for all existing DataSets
 */
@Component
public class OmxRepositoryRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private final DataService dataService;
	private final SearchService searchService;
	private final EntityValidator entityValidator;
	private final ImportServiceFactory importServiceFactory;
	private final OmxImporterServiceImpl omxImporterServiceImpl;

	@Autowired
	public OmxRepositoryRegistrator(DataService dataService, SearchService searchService,
			EntityValidator entityValidator, ImportServiceFactory importServiceFactory,
			OmxImporterServiceImpl omxImporterServiceImpl)
	{
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		if (searchService == null) throw new IllegalArgumentException("searchService is null");
		if (entityValidator == null) throw new IllegalArgumentException("entityValidator is null");
		if (importServiceFactory == null) throw new IllegalArgumentException("importServiceFactory is null");
		if (omxImporterServiceImpl == null) throw new IllegalArgumentException("omxImporterServiceImpl is null");
		this.dataService = dataService;
		this.searchService = searchService;
		this.entityValidator = entityValidator;
		this.importServiceFactory = importServiceFactory;
		this.omxImporterServiceImpl = omxImporterServiceImpl;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		// Run it as System
		// Remember the original context
		SecurityContext origCtx = SecurityContextHolder.getContext();
		try
		{
			// Set a SystemSecurityToken
			SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
			SecurityContextHolder.getContext().setAuthentication(new SystemSecurityToken());

			for (DataSet dataSet : dataService.findAll(DataSet.ENTITY_NAME, DataSet.class))
			{
				OmxRepository repo = new OmxRepository(dataService, searchService, dataSet.getIdentifier(),
						entityValidator);
				dataService.addRepository(new CrudRepositorySecurityDecorator(repo));
			}
		}
		finally
		{
			// Set the original context back when method is finished
			SecurityContextHolder.setContext(origCtx);
		}

		importServiceFactory.addImportService(omxImporterServiceImpl);
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE + 2;
	}
}