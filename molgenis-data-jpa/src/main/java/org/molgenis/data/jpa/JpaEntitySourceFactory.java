package org.molgenis.data.jpa;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.molgenis.data.EntitySource;
import org.molgenis.data.EntitySourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JpaEntitySourceFactory implements EntitySourceFactory
{
	@Autowired
	private JpaEntitySource jpaEntitySource;

	public Set<String> getUrls()
	{
		return new HashSet<String>(Arrays.asList(jpaEntitySource.getUrl()));
	}

	@Override
	public String getUrlPrefix()
	{
		return "jpa://";
	}

	@Override
	public EntitySource create(String url)
	{
		return jpaEntitySource;
	}

}
