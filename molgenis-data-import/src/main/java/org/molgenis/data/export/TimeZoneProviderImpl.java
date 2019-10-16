package org.molgenis.data.export;

import java.util.TimeZone;
import org.springframework.stereotype.Component;

@Component
public class TimeZoneProviderImpl implements TimeZoneProvider {

  @Override
  public TimeZone getSystemTimeZone() {
    return TimeZone.getDefault();
  }
}
