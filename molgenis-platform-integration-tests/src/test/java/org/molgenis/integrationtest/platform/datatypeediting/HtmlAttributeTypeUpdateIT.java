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
public class HtmlAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	@BeforeClass
	private void setup()
	{
		super.setup(HTML, STRING);
	}

	@AfterMethod
	private void afterMethod()
	{
		super.afterMethod(HTML);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		return new Object[][] { { "<h1>This is the best MOLGENIS test in the world</h1>", STRING,
				"<h1>This is the best MOLGENIS test in the world</h1>" },
				{ "<h1>This is the best MOLGENIS test in the world</h1>", TEXT,
						"<h1>This is the best MOLGENIS test in the world</h1>" },
				{ "<h1>This is the best MOLGENIS test in the world</h1>", SCRIPT,
						"<h1>This is the best MOLGENIS test in the world</h1>" } };
	}

	/**
	 * Valid conversion cases for HTML to:
	 * STRING, TEXT, SCRIPT
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
		return new Object[][] { { "<h1>can not compute</h1>", BOOL, "V94" },
				{ "<h1>can not compute</h1>", INT, "V94" },
				{ "<h1>can not compute</h1>", LONG, "V94" },
				{ "<h1>can not compute</h1>", DECIMAL, "V94" },
				{ "<h1>can not compute</h1>", XREF, "V94" },
				{ "<h1>can not compute</h1>", CATEGORICAL, "V94" },
				{ "<h1>can not compute</h1>", HYPERLINK, "V94" },
				{ "<h1>can not compute</h1>", EMAIL, "V94" },
				{ "<h1>can not compute</h1>", ENUM, "V94" },
				{ "<h1>can not compute</h1>", DATE, "V94" },
				{ "<h1>can not compute</h1>", DATE_TIME, "V94" },
				{ "<h1>can not compute</h1>", MREF, "V94" },
				{ "<h1>can not compute</h1>", CATEGORICAL_MREF, "V94" },
				{ "<h1>can not compute</h1>", FILE, "V94" },
				{ "<h1>can not compute</h1>", COMPOUND, "V94" },
				{ "<h1>can not compute</h1>", ONE_TO_MANY, "V94" } };
	}

	/**
	 * Invalid conversions cases for HTML to:
	 * BOOL, INT, LONG, DECIMAL, XREF, CATEGORICAL, HYPERLINK, EMAIL, ENUM, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, FILE, COMPOUND, ONE_TO_MANY
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
