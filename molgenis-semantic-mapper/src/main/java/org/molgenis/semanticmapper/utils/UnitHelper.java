package org.molgenis.semanticmapper.utils;

public class UnitHelper {
  private UnitHelper() {}

  public static String superscriptToNumber(String str) {
    str = str.replace("⁰", "0");
    str = str.replace("¹", "1");
    str = str.replace("²", "2");
    str = str.replace("³", "3");
    str = str.replace("⁴", "4");
    str = str.replace("⁵", "5");
    str = str.replace("⁶", "6");
    str = str.replace("⁷", "7");
    str = str.replace("⁸", "8");
    str = str.replace("⁹", "9");
    return str;
  }

  public static String numberToSuperscript(String str) {
    str = str.replace("0", "⁰");
    str = str.replace("1", "¹");
    str = str.replace("2", "²");
    str = str.replace("3", "³");
    str = str.replace("4", "⁴");
    str = str.replace("5", "⁵");
    str = str.replace("6", "⁶");
    str = str.replace("7", "⁷");
    str = str.replace("8", "⁸");
    str = str.replace("9", "⁹");
    return str;
  }
}
