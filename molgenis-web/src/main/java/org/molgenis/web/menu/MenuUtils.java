package org.molgenis.web.menu;

import java.util.Scanner;

public class MenuUtils {
  public static String readDefaultMenuValueFromClasspath() {
    return new Scanner(MenuUtils.class.getResourceAsStream("/molgenis_ui.json"))
        .useDelimiter("\\A")
        .next();
  }
}
