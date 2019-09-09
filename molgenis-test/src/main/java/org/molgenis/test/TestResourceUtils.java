package org.molgenis.test;

import static freemarker.template.Configuration.VERSION_2_3_21;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;

import com.google.common.io.Resources;
import freemarker.core.InvalidReferenceException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class TestResourceUtils {
  private static final Configuration CONFIGURATION = new Configuration(VERSION_2_3_21);

  public static String getRenderedString(Class<?> contextClass, String resourceName)
      throws IOException {
    return getRenderedString(contextClass, resourceName, emptyMap());
  }

  public static String getRenderedString(
      Class<?> contextClass, String resourceName, Map<String, String> model) throws IOException {
    String resourceContent = getString(contextClass, resourceName);

    Template template = new Template("template", resourceContent, CONFIGURATION);
    Map<String, Object> values = new HashMap<>(model);
    System.getProperties().forEach((key, value) -> values.put(key.toString(), value));

    StringWriter stringWriter = new StringWriter();
    try {
      template.process(values, stringWriter);
    } catch (InvalidReferenceException e) {
      throw new IllegalArgumentException("did you forget to declare a system property?", e);
    } catch (TemplateException e) {
      throw new IllegalArgumentException("template error", e);
    }
    return stringWriter.toString();
  }

  /**
   * Based on org.molgenis.util.ResourceUtils which can't be used here because of circular
   * dependency issues.
   */
  @SuppressWarnings("UnstableApiUsage")
  public static String getString(Class<?> contextClass, String resourceName) throws IOException {
    URL resourceUrl = Resources.getResource(contextClass, resourceName);
    return Resources.toString(resourceUrl, UTF_8);
  }
}
