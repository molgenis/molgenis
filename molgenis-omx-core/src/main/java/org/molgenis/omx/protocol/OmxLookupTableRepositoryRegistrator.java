package org.molgenis.omx.protocol;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.support.QueryResolver;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.security.runas.SystemSecurityToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * On startup add an OmxLookupTableRepositoryRegistrator to the DataService for each categorical ObservableFeature
 */
@Component
public class OmxLookupTableRepositoryRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private final DataService dataService;
	private final QueryResolver queryResolver;

	@Autowired
	public OmxLookupTableRepositoryRegistrator(DataService dataService, QueryResolver queryResolver)
	{
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		if (queryResolver == null) throw new IllegalArgumentException("queryResolver is null");
		this.dataService = dataService;
		this.queryResolver = queryResolver;
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

			Query q = new QueryImpl()
					.eq(ObservableFeature.DATATYPE, FieldTypeEnum.CATEGORICAL.toString().toLowerCase());
			for (ObservableFeature categoricalFeature : dataService.findAll(ObservableFeature.ENTITY_NAME, q,
					ObservableFeature.class))
			{
				OmxLookupTableRepository repo = new OmxLookupTableRepository(dataService,
						categoricalFeature.getIdentifier(), queryResolver);
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