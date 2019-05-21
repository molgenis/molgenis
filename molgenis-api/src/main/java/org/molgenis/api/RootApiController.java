package org.molgenis.api;

import static java.time.ZonedDateTime.now;
import static java.util.Objects.requireNonNull;
import static org.molgenis.api.ApiNamespace.API_PATH;
import static org.springframework.web.bind.annotation.RequestMethod.OPTIONS;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(API_PATH)
public class RootApiController extends ApiController {

  private final String molgenisVersion;
  private String molgenisBuildDate;
  public static final String VERSION_DATE_FORMAT = "yyyy-MM-dd hh:mm z";

  public RootApiController(
      @Value("${molgenis.version:@null}") String molgenisVersion,
      @Value("${molgenis.build.date:@null}") String molgenisBuildDate) {
    super("", 1);
    this.molgenisVersion = requireNonNull(molgenisVersion);
    this.molgenisBuildDate = requireNonNull(molgenisBuildDate);
  }

  @Autowired
  @RequestMapping(method = OPTIONS)
  public VersionResponse getVersion() throws ParseException {
    Instant date;
    if (molgenisBuildDate.equals("${maven.build.timestamp}")) {
      date = now().toInstant();
    } else {
      date = new SimpleDateFormat(VERSION_DATE_FORMAT).parse(molgenisBuildDate).toInstant();
    }
    AppVersionResponse appVersionResponse = AppVersionResponse.create(molgenisVersion, date);
    return VersionResponse.create(appVersionResponse);
  }
}
