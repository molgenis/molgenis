package org.molgenis.web.converter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

class MolgenisGsonHttpMessageConverterTest {
  private GsonHttpMessageConverter gsonHttpMessageConverter;

  @BeforeEach
  void setUp() {
    Gson gson = new Gson();
    gsonHttpMessageConverter = new MolgenisGsonHttpMessageConverter(gson);
  }

  // regression test for https://github.com/molgenis/molgenis/issues/3078
  @Test
  void getSupportedMediaTypes() {
    boolean containsApplicationJsonWithoutCharset = false;
    for (MediaType mediaType : gsonHttpMessageConverter.getSupportedMediaTypes()) {
      if (mediaType.getType().equals("application")
          && mediaType.getSubtype().equals("json")
          && mediaType.getCharset() == null) {
        containsApplicationJsonWithoutCharset = true;
        break;
      }
    }
    assertTrue(containsApplicationJsonWithoutCharset);
  }
}
