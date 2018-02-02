package org.molgenis.data.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class BootstrappingEventPublisher
{
	private final ApplicationEventPublisher publisher;

	@Autowired
	public BootstrappingEventPublisher(ApplicationEventPublisher publisher)
	{
		this.publisher = publisher;
	}

	public void createBoostrapEvent(boolean done)
	{
		publisher.publishEvent(new BootstrappingEvent(done));
	}
}