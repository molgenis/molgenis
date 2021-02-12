package org.molgenis.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Map;
import net.logstash.logback.marker.LogstashMarker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

class AuditEventLoggerTest {

  private AuditEventLogger auditEventLogger;
  private final Logger logger = (Logger) LoggerFactory.getLogger(AuditEventLogger.class);

  @BeforeEach
  void beforeEach() {
    auditEventLogger = new AuditEventLogger();
  }

  @Test
  void testJsonMarkers() throws IOException {
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);

    AuditApplicationEvent event = new AuditApplicationEvent(
        AuditEvent.create(
            Instant.parse("2021-01-06T11:35:02.781470Z"),
            "principal",
            AuditEventType.AUTHENTICATION_FAILURE,
            Map.of("detail", Map.of("foo", "bar"))));
    auditEventLogger.onAuditApplicationEvent(event);

    appender.stop();
    logger.detachAppender(appender);
    assertEquals(1, appender.list.size());
    var loggingEvent = appender.list.get(0);
    assertEquals(
        "{\"timestamp\":\"2021-01-06T11:35:02.781470Z\",\"principal\":\"principal\",\"type\":\"AUTHENTICATION_FAILURE\",\"data\":{\"detail\":{\"foo\":\"bar\"}}",
        writeMarkersToString(loggingEvent.getMarker()));
  }

  private String writeMarkersToString(Marker marker) throws IOException {
    StringWriter writer = new StringWriter();
    JsonGenerator generator = new JsonFactory().createGenerator(writer);
    generator.setCodec(new ObjectMapper());
    generator.writeStartObject();
    ((LogstashMarker) marker).writeTo(generator);
    marker
        .iterator()
        .forEachRemaining(
            m -> {
              try {
                ((LogstashMarker) m).writeTo(generator);
              } catch (Exception ex) {
                throw new RuntimeException(ex);
              }
            });
    generator.writeEndObject();
    return writer.toString();
  }
}
