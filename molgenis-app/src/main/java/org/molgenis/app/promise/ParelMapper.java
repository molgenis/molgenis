package org.molgenis.app.promise;

import java.io.IOException;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class ParelMapper implements PromiseMapper, ApplicationListener<ContextRefreshedEvent>
{
	private final String ID = "PAREL";

	private PromiseMapperFactory promiseMapperFactory;

	@Autowired
	public ParelMapper(PromiseMapperFactory promiseMapperFactory)
	{
		this.promiseMapperFactory = Objects.requireNonNull(promiseMapperFactory);
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent arg0)
	{
		promiseMapperFactory.registerMapper(ID, this);
	}

	@Override
	public String getId()
	{
		return ID;
	}

	@Override
	public void map(String biobankId) throws IOException
	{
		// TODO Auto-generated method stub

	}

}
