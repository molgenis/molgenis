package org.molgenis.integrationtest.export;

import java.util.TimeZone;
import org.molgenis.data.export.TimeZoneProvider;
import org.springframework.stereotype.Component;

@Component
public class TestTimeZoneProvider implements TimeZoneProvider {

  @Override
  public TimeZone getSystemTimeZone() {
    return TimeZone.getTimeZone("Europe/Paris");
  }
}
