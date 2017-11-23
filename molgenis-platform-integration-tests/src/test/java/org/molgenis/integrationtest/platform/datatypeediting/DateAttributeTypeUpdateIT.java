package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.ValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.*;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.ZoneId.systemDefault;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.util.MolgenisDateFormat.parseLocalDate;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class DateAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{

	@BeforeClass
	public void setup()
	{
		super.setup(DATE, STRING);
	}

	@AfterMethod
	public void afterMethod()
	{
		super.afterMethod(DATE);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionTestCases")
	public Object[][] validConversionTestCases()
	{
		return new Object[][] { { LocalDate.parse("2016-11-13"), STRING, "2016-11-13" },
				{ LocalDate.parse("2016-11-13"), TEXT, "2016-11-13" }, { LocalDate.parse("2016-11-13"), DATE_TIME,
				LocalDate.parse("2016-11-13").atStartOfDay(systemDefault()).toInstant() } };
	}

	/**
	 * Valid conversion cases for DATE to:
	 * STRING, TEXT, DATE_TIME
	 *
	 * @param valueToConvert  The value that will be converted
	 * @param typeToConvertTo The type to convert to
	 * @param convertedValue  The expected value after converting the type
	 */
	@Test(dataProvider = "validConversionTestCases")
	public void testValidConversion(LocalDate valueToConvert, AttributeType typeToConvertTo, Object convertedValue)
			throws ParseException
	{
		testTypeConversion(valueToConvert, typeToConvertTo);

		// Assert if conversion was successful
		assertEquals(getActualDataType(), typeToConvertTo);
		assertEquals(getActualValue(), convertedValue);
	}

	@DataProvider(name = "invalidConversionTestCases")
	public Object[][] invalidConversionTestCases()
	{
		return new Object[][] { { "2016-11-13", BOOL, "V94" },
				{ "2016-11-13", INT, "V94" },
				{ "2016-11-13", LONG, "V94" },
				{ "2016-11-13", DECIMAL, "V94" },
				{ "2016-11-13", XREF, "V94" },
				{ "2016-11-13", CATEGORICAL, "V94" },
				{ "2016-11-13", SCRIPT, "V94" },
				{ "2016-11-13", HYPERLINK, "V94" },
				{ "2016-11-13", EMAIL, "V94" },
				{ "2016-11-13", ENUM, "V94" },
				{ "2016-11-13", HTML, "V94" },
				{ "2016-11-13", MREF, "V94" },
				{ "2016-11-13", CATEGORICAL_MREF, "V94" },
				{ "2016-11-13", FILE, "V94" },
				{ "2016-11-13", COMPOUND, "V94" },
				{ "2016-11-13", ONE_TO_MANY, "V94" } };
	}

	/**
	 * Invalid conversion cases for TEXT to:
	 * BOOL, INT, LONG, DECIMAL, XREF, CATEGORICAL, SCRIPT, HYPERLINK, EMAIL, ENUM, HTML, MREF, CATEGORICAL_MREF, FILE, COMPOUND, ONE_TO_MANY
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 * @param errorCode The expected errorCode
	 */
	@Test(dataProvider = "invalidConversionTestCases")
	public void testInvalidConversions(Object valueToConvert, AttributeType typeToConvertTo, String errorCode)
			throws ParseException
	{
		try
		{
			valueToConvert = parseLocalDate(valueToConvert.toString());
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
