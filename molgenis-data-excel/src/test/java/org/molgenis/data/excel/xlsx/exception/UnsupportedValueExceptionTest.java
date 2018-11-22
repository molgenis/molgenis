package org.molgenis.data.excel.xlsx.exception;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class UnsupportedValueExceptionTest  extends ExceptionMessageTest {

  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("excel");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    List<String> list = Arrays.asList("test1","test2");

    ExceptionMessageTest.assertExceptionMessageEquals(
        new UnsupportedValueException(list), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
        new Object[] {"en", "Class 'ArrayList' of value '[test1, test2]' is not of a supported type for the XLXS writer."},
        {"nl", "Klasse 'ArrayList' van waarde '[test1, test2]' is niet van een ondersteund type voor de XLXS writer."}
    };
  }
}
