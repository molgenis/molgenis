package org.molgenis.data.event;

import static org.molgenis.data.event.BootstrappingEvent.BootstrappingStatus.FINISHED;
import static org.molgenis.data.event.BootstrappingEvent.BootstrappingStatus.STARTED;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class BootstrappingEventPublisher {
  private final ApplicationEventPublisher publisher;

  @Autowired
  public BootstrappingEventPublisher(ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  public void publishBootstrappingStartedEvent() {
    publisher.publishEvent(new BootstrappingEvent(STARTED));
  }

  public void publishBootstrappingFinishedEvent() {
    publisher.publishEvent(new BootstrappingEvent(FINISHED));
  }
}
