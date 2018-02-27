package org.molgenis.data.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import static org.molgenis.data.event.BootstrappingEvent.BootstrappingStatus.FINISHED_SYSTEM_ENTITY_TYPES;

@Component
public class BootstrappingEventPublisher
{
	private final ApplicationEventPublisher publisher;

	@Autowired
	public BootstrappingEventPublisher(ApplicationEventPublisher publisher)
	{
		this.publisher = publisher;
	}

	public void publishBootstrappingSystemEntitiesFinishedEvent()
	{
		publisher.publishEvent(new BootstrappingEvent(FINISHED_SYSTEM_ENTITY_TYPES));
	}
}