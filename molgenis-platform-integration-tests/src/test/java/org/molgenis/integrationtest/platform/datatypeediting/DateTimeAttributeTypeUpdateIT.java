package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.ValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.*;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.util.MolgenisDateFormat.parseInstant;
import static org.molgenis.util.MolgenisDateFormat.parseLocalDate;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class DateTimeAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{

	@BeforeClass
	public void setup()
	{
		super.setup(DATE_TIME, STRING);
	}

	@AfterMethod
	public void afterMethod()
	{
		super.afterMethod(DATE_TIME);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionTestCases")
	public Object[][] validConversionTestCases()
	{
		Instant value = Instant.parse("2016-11-13T19:20:20Z");
		String formattedInDefaultTimezone = value.atZone(ZoneId.systemDefault())
												 .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssx"));
		return new Object[][] { { value, STRING, formattedInDefaultTimezone },
				{ value, TEXT, formattedInDefaultTimezone }, { value, DATE, "2016-11-13" } };
	}

	/**
	 * Valid conversion cases for DATE_TIME to:
	 * STRING, TEXT, DATE
	 *
	 * @param valueToConvert  The value that will be converted
	 * @param typeToConvertTo The type to convert to
	 * @param convertedValue  The expected value after converting the type
	 */
	@Test(dataProvider = "validConversionTestCases")
	public void testValidConversion(Instant valueToConvert, AttributeType typeToConvertTo, Object convertedValue)
			throws ParseException
	{
		valueToConvert = parseInstant(valueToConvert.toString());
		testTypeConversion(valueToConvert, typeToConvertTo);

		if (typeToConvertTo.equals(DATE)) convertedValue = parseLocalDate(convertedValue.toString());

		// Assert if conversion was successful
		assertEquals(getActualDataType(), typeToConvertTo);
		assertEquals(getActualValue(), convertedValue);
	}

	@DataProvider(name = "invalidConversionTestCases")
	public Object[][] invalidConversionTestCases()
	{
		return new Object[][] { { "2016-11-13T20:20:20+0100", BOOL, "V94" },
				{ "2016-11-13T20:20:20+0100", INT, "V94" },
				{ "2016-11-13T20:20:20+0100", LONG, "V94" },
				{ "2016-11-13T20:20:20+0100", DECIMAL, "V94" },
				{ "2016-11-13T20:20:20+0100", XREF, "V94" },
				{ "2016-11-13T20:20:20+0100", CATEGORICAL, "V94" },
				{ "2016-11-13T20:20:20+0100", SCRIPT, "V94" },
				{ "2016-11-13T20:20:20+0100", HYPERLINK, "V94" },
				{ "2016-11-13T20:20:20+0100", EMAIL, "V94" },
				{ "2016-11-13T20:20:20+0100", ENUM, "V94" },
				{ "2016-11-13T20:20:20+0100", HTML, "V94" },
				{ "2016-11-13T20:20:20+0100", MREF, "V94" },
				{ "2016-11-13T20:20:20+0100", CATEGORICAL_MREF, "V94" },
				{ "2016-11-13T20:20:20+0100", FILE, "V94" },
				{ "2016-11-13T20:20:20+0100", COMPOUND, "V94" },
				{ "2016-11-13T20:20:20+0100", ONE_TO_MANY, "V94" } };
	}

	/**
	 * Invalid conversion cases for DATE_TIME to:
	 * BOOL, INT, LONG, DECIMAL, XREF, CATEGORICAL, SCRIPT, HYPERLINK, EMAIL, ENUM, HTML, MREF, CATEGORICAL_MREF, FILE, COMPOUND, ONE_TO_MANY
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 * @param errorCode       The expected errorCode
	 */
	@Test(dataProvider = "invalidConversionTestCases")
	public void testInvalidConversions(Object valueToConvert, AttributeType typeToConvertTo, String errorCode)
			throws ParseException
	{
		try
		{
			valueToConvert = parseInstant(valueToConvert.toString());
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
