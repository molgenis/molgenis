package org.molgenis.data.jpa;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.molgenis.data.EntitySource;
import org.molgenis.data.EntitySourceFactory;
import org.molgenis.data.MolgenisDataException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class JpaEntitySourceFactory implements EntitySourceFactory, ApplicationContextAware
{
	private final Map<String, EntitySource> jpaDataSourceMap = new HashMap<String, EntitySource>();

	public Set<String> getUrls()
	{
		return jpaDataSourceMap.keySet();
	}

	@Override
	public String getUrlPrefix()
	{
		return "jpa";
	}

	@Override
	public EntitySource create(String url)
	{
		EntitySource dataSource = jpaDataSourceMap.get(url);
		if (dataSource == null)
		{
			throw new MolgenisDataException("Unknown jpa DataSource [" + url + "]");
		}

		return dataSource;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
	{
		Map<String, JpaEntitySource> jpaDataSources = applicationContext.getBeansOfType(JpaEntitySource.class);

		for (JpaEntitySource jpaDataSource : jpaDataSources.values())
		{
			jpaDataSourceMap.put(jpaDataSource.getUrl(), jpaDataSource);
		}

	}

}
