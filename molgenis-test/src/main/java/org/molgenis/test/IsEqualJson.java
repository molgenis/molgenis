package org.molgenis.test;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class IsEqualJson extends BaseMatcher<String> {
  private final String expectedJson;
  private final JSONCompareMode jsonCompareMode;

  public IsEqualJson(String expectedJson) {
    this(expectedJson, JSONCompareMode.STRICT);
  }

  public IsEqualJson(String expectedJson, JSONCompareMode jsonCompareMode) {
    this.expectedJson = expectedJson;
    this.jsonCompareMode = jsonCompareMode;
  }

  @Override
  public boolean matches(Object item) {
    try {
      return matches(expectedJson, item);
    } catch (JSONException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public void describeTo(Description description) {
    description.appendText(expectedJson);
  }

  @Factory
  public static IsEqualJson isEqualJson(String expected) {
    return new IsEqualJson(expected);
  }

  private boolean matches(String expectedStr, Object actual) throws JSONException {
    if (actual == null && expectedStr == null) {
      return true;
    }
    if (!(actual instanceof String)) {
      throw new AssertionError("actual value must be of type String");
    }
    JSONAssert.assertEquals(expectedStr, (String) actual, jsonCompareMode);
    return true;
  }
}
