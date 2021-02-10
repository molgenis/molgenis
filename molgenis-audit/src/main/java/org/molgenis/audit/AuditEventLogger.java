package org.molgenis.audit;

import static net.logstash.logback.marker.Markers.append;
import static net.logstash.logback.marker.Markers.appendEntries;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class AuditEventLogger {

  private static final Logger logger = LoggerFactory.getLogger(AuditEventLogger.class);

  @EventListener
  public void onAuditApplicationEvent(@NonNull AuditApplicationEvent auditApplicationEvent) {
    AuditEvent event = auditApplicationEvent.getAuditEvent();
    if (logger.isInfoEnabled()) {
      logger.info(
          append("timestamp", event.getTimestamp())
              .and(append("principal", event.getPrincipal()))
              .and(append("type", event.getType()))
              .and(appendEntries(Map.of("data", event.getData()))),
          event.toString());
    }
  }
}