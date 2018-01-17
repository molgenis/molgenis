package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.EntityReferenceUnknownConstraintViolationException;
import org.molgenis.data.validation.ValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class LongAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	@BeforeClass
	private void setup()
	{
		super.setup(LONG, LONG);
	}

	@AfterMethod
	private void afterMethod()
	{
		super.afterMethod(LONG);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		return new Object[][] { { 1L, STRING, "1" }, { 1L, TEXT, "1" }, { 1L, INT, 1 }, { 1L, DECIMAL, 1.0 },
				{ 1L, ENUM, "1" }, { 1L, XREF, "label1" }, { 1L, CATEGORICAL, "label1" } };
	}

	/**
	 * Valid conversion cases for LONG to:
	 * STRING, TEXT, INT, DECIMAL, ENUM, XREF, CATEGORICAL
	 *
	 * @param valueToConvert  The value that will be converted
	 * @param typeToConvertTo The type to convert to
	 * @param convertedValue  The expected value after converting the type
	 */
	@Test(dataProvider = "validConversionData")
	public void testValidConversion(long valueToConvert, AttributeType typeToConvertTo, Object convertedValue)
	{
		testTypeConversion(valueToConvert, typeToConvertTo);

		// Assert if conversion was successful
		assertEquals(getActualDataType(), typeToConvertTo);
		assertEquals(getActualValue(), convertedValue);
	}

	@DataProvider(name = "invalidConversionTestCases")
	public Object[][] invalidConversionTestCases()
	{
		return new Object[][] { { 1L, DATE, "V94" },
				{ 1L, DATE_TIME, "V94" },
				{ 1L, MREF, "V94" },
				{ 1L, CATEGORICAL_MREF, "V94" },
				{ 1L, EMAIL, "V94" },
				{ 1L, HTML, "V94" },
				{ 1L, HYPERLINK, "V94" },
				{ 1L, COMPOUND, "V94" },
				{ 1L, FILE, "V94" },
				{ 1L, BOOL, "V94" },
				{ 1L, SCRIPT, "V94" },
				{ 1L, ONE_TO_MANY, "V94" } };
	}

	/**
	 * Invalid conversions cases for LONG to:
	 * XREF, CATEGORICAL, ENUM, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, EMAIL, HTML, HYPERLINK, COMPOUND, FILE, BOOL, SCRIPT, ONE_TO_MANY
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 * @param errorCode       The expected errorCode
	 */
	@Test(dataProvider = "invalidConversionTestCases")
	public void testInvalidConversion(long valueToConvert, AttributeType typeToConvertTo, String errorCode)
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

	@DataProvider(name = "invalidForeignKeyTestCases")
	public Object[][] invalidForeignKeyTestCases()
	{
		return new Object[][] { { 123L, XREF, EntityReferenceUnknownConstraintViolationException.class,
				"type:MAINENTITY attribute:mainAttribute value: 123" },
				{ 123L, CATEGORICAL, EntityReferenceUnknownConstraintViolationException.class,
						"type:MAINENTITY attribute:mainAttribute value: 123" } };
	}

	@Test(dataProvider = "invalidForeignKeyTestCases")
	public void testInvalidForeignKey(long valueToConvert, AttributeType typeToConvertTo, Class expected,
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
}
