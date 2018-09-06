package org.molgenis.web.converter;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.charset.Charset;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;

public abstract class BaseHttpMessageConverter<T> extends AbstractHttpMessageConverter<T> {
  protected static final Charset DEFAULT_CHARSET = UTF_8;

  public BaseHttpMessageConverter() {
    super();
  }

  public BaseHttpMessageConverter(MediaType... supportedMediaTypes) {
    super(supportedMediaTypes);
  }

  public BaseHttpMessageConverter(MediaType supportedMediaType) {
    super(supportedMediaType);
  }

  protected Charset getCharset(HttpHeaders headers) {
    if (headers != null
        && headers.getContentType() != null
        && headers.getContentType().getCharset() != null) {
      return headers.getContentType().getCharset();
    }
    return DEFAULT_CHARSET;
  }
}
