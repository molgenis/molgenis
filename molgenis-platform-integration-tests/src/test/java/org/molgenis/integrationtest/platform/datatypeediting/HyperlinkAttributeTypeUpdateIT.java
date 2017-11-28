package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.ValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class HyperlinkAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	@BeforeClass
	private void setup()
	{
		super.setup(HYPERLINK, STRING);
	}

	@AfterMethod
	private void afterMethod()
	{
		super.afterMethod(HYPERLINK);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		return new Object[][] { { "https://www.google.com", STRING, "https://www.google.com" },
				{ "https://www.google.com", TEXT, "https://www.google.com" },
				{ "https://www.google.com", CATEGORICAL, "hyperlink label" },
				{ "https://www.google.com", XREF, "hyperlink label" } };
	}

	/**
	 * Valid conversion cases for HYPERLINK to:
	 * STRING, TEXT, CATEGORICAL, XREF
	 *
	 * @param valueToConvert  The value that will be converted
	 * @param typeToConvertTo The type to convert to
	 * @param convertedValue  The expected value after converting the type
	 */
	@Test(dataProvider = "validConversionData")
	public void testValidConversion(String valueToConvert, AttributeType typeToConvertTo, Object convertedValue)
	{
		testTypeConversion(valueToConvert, typeToConvertTo);

		// Assert if conversion was successful
		assertEquals(getActualDataType(), typeToConvertTo);
		assertEquals(getActualValue(), convertedValue);
	}

	@DataProvider(name = "invalidConversionTestCases")
	public Object[][] invalidConversionTestCases()
	{
		return new Object[][] { { "https://www.google.com", BOOL, "V94" },
				{ "https://www.google.com", INT, "V94" },
				{ "https://www.google.com", LONG, "V94" },
				{ "https://www.google.com", DECIMAL, "V94" },
				{ "https://www.google.com", SCRIPT, "V94" },
				{ "https://www.google.com", EMAIL, "V94" },
				{ "https://www.google.com", HTML, "V94" },
				{ "https://www.google.com", ENUM, "V94" },
				{ "https://www.google.com", DATE, "V94" },
				{ "https://www.google.com", DATE_TIME, "V94" },
				{ "https://www.google.com", MREF, "V94" },
				{ "https://www.google.com", CATEGORICAL_MREF, "V94" },
				{ "https://www.google.com", FILE, "V94" },
				{ "https://www.google.com", COMPOUND, "V94" },
				{ "https://www.google.com", ONE_TO_MANY, "V94" } };
	}

	/**
	 * Invalid conversions cases for HYPERLINK to:
	 * BOOL, INT, LONG, DECIMAL, XREF, CATEGORICAL, SCRIPT, EMAIL, HTML, ENUM, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, FILE, COMPOUND, ONE_TO_MANY
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 * @param errorCode       The expected errorCode
	 */
	@Test(dataProvider = "invalidConversionTestCases")
	public void testInvalidConversion(String valueToConvert, AttributeType typeToConvertTo, String errorCode)
	{
		try
		{
			testTypeConversion(valueToConvert, typeToConvertTo);
			fail("Conversion should have failed");
		}
		catch (ValidationException exception)
		{
			//match on error code only since the message has no parameters
			List<String> messageList = exception.getValidationMessages()
												.map(message -> message.getErrorCode())
												.collect(Collectors.toList());
			assertTrue(messageList.contains(errorCode));
		}
	}
}
