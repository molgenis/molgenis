package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.DataTypeConstraintViolationException;
import org.molgenis.data.validation.EntityReferenceUnknownConstraintViolationException;
import org.molgenis.data.validation.EnumConstraintModificationException;
import org.molgenis.data.validation.ValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.*;

import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.util.MolgenisDateFormat.parseInstant;
import static org.molgenis.util.MolgenisDateFormat.parseLocalDate;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class StringAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	@BeforeClass
	public void setUp()
	{
		super.setup(STRING, STRING);
	}

	@AfterMethod
	public void afterMethod()
	{
		super.afterMethod(STRING);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		return new Object[][] { { "true", BOOL, true }, { "1", INT, 1 }, { "4243298", LONG, 4243298L },
				{ "1.234", DECIMAL, 1.234 }, { "1", XREF, "label1" }, { "1", CATEGORICAL, "label1" },
				{ "A VERY LONG TEXT!!!!@#$#{@}{@}{#%$#*($&@#", TEXT, "A VERY LONG TEXT!!!!@#$#{@}{@}{#%$#*($&@#" },
				{ "1", ENUM, "1" }, { "<h1>Hello World</h1>", HTML, "<h1>Hello World</h1>" },
				{ "Compounds go!", COMPOUND, null }, { "1990-11-13", DATE, "1990-11-13" },
				{ "2016-11-13T20:20:20+0100", DATE_TIME, "2016-11-13T20:20:20+0100" },
				{ "2016-11-13T20:20:20+0500", DATE_TIME, "2016-11-13T20:20:20+0500" } };
	}

	/**
	 * Valid conversion cases for STRING to:
	 * INT, TEXT, BOOL, DECIMAL, LONG, XREF, CATEGORICAL, COMPOUND, ENUM, HTML, DATE, DATE_TIME
	 *
	 * @param valueToConvert  The value that will be converted
	 * @param typeToConvertTo The type to convert to
	 * @param convertedValue  The expected value after converting the type
	 */
	@Test(dataProvider = "validConversionData")
	public void testValidConversion(String valueToConvert, AttributeType typeToConvertTo, Object convertedValue)
			throws ParseException
	{
		testTypeConversion(valueToConvert, typeToConvertTo);

		if (typeToConvertTo.equals(DATE)) convertedValue = parseLocalDate(convertedValue.toString());
		if (typeToConvertTo.equals(DATE_TIME)) convertedValue = parseInstant(convertedValue.toString());

		// Assert if conversion was successful
		assertEquals(getActualDataType(), typeToConvertTo);
		assertEquals(getActualValue(), convertedValue);
	}

	@DataProvider(name = "invalidValueTestCases")
	public Object[][] invalidValueTestCases()
	{
		return new Object[][] { { "not true", BOOL, "V03" }, { "1b", INT, "V03" }, { "1234567890b", LONG, "V03" },
				{ "1.123b", DECIMAL, "V03" }, { "Not a date time", DATE_TIME, "V03" } };
	}


	@DataProvider(name = "invalidConversionTestCases")
	public Object[][] invalidConversionTestCases()
	{
		return new Object[][] {
				{ "Test@Test.Test", EMAIL, "V94" },
				{ "https://www.google.com", HYPERLINK, "V94" }, { "ref123", MREF, "V94" },

				{ "ref123", FILE, "V94" },
				{ "ref123", ONE_TO_MANY, "V94" } };
	}

	/**
	 * Invalid conversion cases for STRING to:
	 * BOOL, INT, LONG, DECIMAL, XREF, CATEGORICAL, EMAIL, HYPERLINK, ENUM, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, FILE
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

	@Test(dataProvider = "invalidValueTestCases")
	public void testInvalidValue(String valueToConvert, AttributeType typeToConvertTo, String errorCode)
	{
		try
		{
			testTypeConversion(valueToConvert, typeToConvertTo);
			fail("Conversion should have failed");
		}
		catch (DataTypeConstraintViolationException exception)
		{
			assertEquals(exception.getErrorCode(), errorCode);
		}
	}

	@DataProvider(name = "invalidForeignKeyTestCases")
	public Object[][] invalidForeignKeyTestCases()
	{
		return new Object[][] { { "ref123", XREF, EntityReferenceUnknownConstraintViolationException.class,
				"type:MAINENTITY attribute:mainAttribute value: ref123" },
				{ "ref123", CATEGORICAL, EntityReferenceUnknownConstraintViolationException.class,
						"type:MAINENTITY attribute:mainAttribute value: ref123" },
				{ "ref123", CATEGORICAL_MREF, ValidationException.class,
						"constraint:TYPE_UPDATE entityType:MAINENTITY attribute:mainAttribute" } };
	}

	@Test(dataProvider = "invalidForeignKeyTestCases")
	public void testInvalidForeignKey(String valueToConvert, AttributeType typeToConvertTo, Class expected,
			String message)
	{
		try
		{
			testTypeConversion(valueToConvert, typeToConvertTo);
			fail("Conversion should have failed");
		}
		catch (Exception exception)
		{
			assertTrue(exception.getClass().isAssignableFrom(expected));
			assertEquals(exception.getMessage(), message);
		}
	}

	@Test
	public void testInvalidEnumValue()
	{
		try
		{
			testTypeConversion("enumOption100", ENUM);
			fail("Conversion should have failed");
		}
		catch (EnumConstraintModificationException exception)
		{
			assertEquals(exception.getErrorCode(), "V09");
		}
	}
}
