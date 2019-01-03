package org.molgenis.web.menu;

import java.util.Scanner;

public class MenuUtils {

  /* The start of string delimiter pattern */
  private static final String START_OF_STRING = "\\A";
  /* The name of the default menu resource on the class path. */
  private static final String DEFAULT_MENU_RESOURCE_NAME = "/molgenis_ui.json";

  private MenuUtils() {}

  /** Reads the default menu contents from the classpath. */
  public static String readDefaultMenuValueFromClasspath() {
    return new Scanner(MenuUtils.class.getResourceAsStream(DEFAULT_MENU_RESOURCE_NAME))
        .useDelimiter(START_OF_STRING)
        .next();
  }
}
