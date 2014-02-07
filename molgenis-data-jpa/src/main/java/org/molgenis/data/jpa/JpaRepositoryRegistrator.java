package org.molgenis.data.jpa;

import java.util.Map;

import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * Register the JpaRepositories by the DataService
 */
@Component
public class JpaRepositoryRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private final DataService dataService;

	@Autowired
	public JpaRepositoryRegistrator(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		this.dataService = dataService;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		Map<String, Object> beans = event.getApplicationContext().getBeansWithAnnotation(
				org.springframework.stereotype.Repository.class);

		for (Object bean : beans.values())
		{
			if (bean instanceof Repository)
			{
				dataService.addRepository((Repository) bean);
			}
		}
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE;
	}
}
