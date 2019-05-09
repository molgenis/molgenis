package org.molgenis.validation;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import org.molgenis.validation.exception.LocalhostNotAllowedException;
import org.molgenis.validation.exception.RelativePathNotAllowedException;

public class UriValidator {

  private UriValidator() {}

  public static URI getSafeUri(String fileLocation)
      throws UnknownHostException, URISyntaxException {
    URI uri = new URI(fileLocation);
    InetAddress inetAddress = InetAddress.getByName(uri.getHost());
    if (!uri.isAbsolute()) {
      throw new RelativePathNotAllowedException();
    }
    if (inetAddress.isAnyLocalAddress()
        || inetAddress.isLoopbackAddress()
        || inetAddress.isLinkLocalAddress()) {
      throw new LocalhostNotAllowedException();
    }
    return uri;
  }
}
