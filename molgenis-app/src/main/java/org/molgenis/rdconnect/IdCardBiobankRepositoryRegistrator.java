package org.molgenis.rdconnect;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.support.DataServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class IdCardBiobankRepositoryRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private static final Logger LOG = LoggerFactory.getLogger(IdCardBiobankRepositoryRegistrator.class);

	private final IdCardBiobankRepository idCardBiobankRepository;
	private final DataServiceImpl dataServiceImpl;

	@Autowired
	public IdCardBiobankRepositoryRegistrator(IdCardBiobankRepository idCardBiobankRepository,
			DataServiceImpl dataServiceImpl)
	{
		this.idCardBiobankRepository = requireNonNull(idCardBiobankRepository);
		this.dataServiceImpl = requireNonNull(dataServiceImpl);
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		// FIXME only works when meta data was imported
		if (dataServiceImpl.hasRepository("rdconnect_regbb"))
		{
			// replace the default registered repository (based on meta data stored in the backend with a repository
			// that uses the index to retrieve entity ids and the ID-Card REST API to retrieve the entity attribute
			// values.

			LOG.debug("Registering IdCardBiobankRepository ...");
			dataServiceImpl.removeRepository("rdconnect_regbb");
			dataServiceImpl.addRepository(idCardBiobankRepository);
			LOG.info("Registered IdCardBiobankRepository");
		}
	}

	@Override
	public int getOrder()
	{
		return Ordered.LOWEST_PRECEDENCE;
	}
}
