package org.molgenis.data.omx;

import org.molgenis.data.DataService;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.search.SearchService;
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

	@Autowired
	public OmxRepositoryRegistrator(DataService dataService, SearchService searchService)
	{
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		if (searchService == null) throw new IllegalArgumentException("searchService is null");
		this.dataService = dataService;
		this.searchService = searchService;
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
				OmxRepository repo = new OmxRepository(dataService, searchService, dataSet.getIdentifier());
				dataService.addRepository(repo);
			}
			
			for (Protocol protocol : dataService.findAll(Protocol.ENTITY_NAME, Protocol.class))
			{
				OmxRepository repo = new OmxRepository(dataService, searchService, protocol.getIdentifier());
				dataService.addRepository(repo);
			}
		}
		finally
		{
			// Set the original context back when method is finished
			SecurityContextHolder.setContext(origCtx);
		}

	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE + 2;
	}
}