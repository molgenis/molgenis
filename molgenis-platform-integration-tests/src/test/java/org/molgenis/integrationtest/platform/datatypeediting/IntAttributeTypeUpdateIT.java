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
public class IntAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	@BeforeClass
	private void setup()
	{
		super.setup(INT, INT);
	}

	@AfterMethod
	private void afterMethod()
	{
		super.afterMethod(INT);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		return new Object[][] { { 1, STRING, "1" }, { 0, BOOL, false }, { 1, BOOL, true }, { 1, TEXT, "1" },
				{ 1, DECIMAL, 1.0 }, { 1, LONG, 1L }, { 1, XREF, "label1" }, { 1, CATEGORICAL, "label1" },
				{ 1, ENUM, "1" } };
	}

	/**
	 * Valid conversion cases for INT to:
	 * STRING, TEXT, DECIMAL, LONG, BOOL, ENUM
	 *
	 * @param valueToConvert  The value that will be converted
	 * @param typeToConvertTo The type to convert to
	 * @param convertedValue  The expected value after converting the type
	 */
	@Test(dataProvider = "validConversionData")
	public void testValidConversion(int valueToConvert, AttributeType typeToConvertTo, Object convertedValue)
	{
		testTypeConversion(valueToConvert, typeToConvertTo);

		// Assert if conversion was successful
		assertEquals(getActualDataType(), typeToConvertTo);
		assertEquals(getActualValue(), convertedValue);
	}

	@DataProvider(name = "invalidConversionTestCases")
	public Object[][] invalidConversionTestCases()
	{
		return new Object[][] { { 10, EMAIL, "V94" },
				{ 10, HYPERLINK, "V94" },
				{ 10, HTML, "V94" }, { 10, DATE, "V94" },
				{ 10, DATE_TIME, "V94" },
				{ 10, MREF, "V94" },
				{ 10, CATEGORICAL_MREF, "V94" },
				{ 10, FILE, "V94" },
				{ 10, COMPOUND, "V94" }, { 10, ONE_TO_MANY, "V94" } };
	}

	/**
	 * Invalid conversions cases for INT to:
	 * XREF, CATEGORICAL, EMAIL, HYPERLINK, HTML, ENUM, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, FILE, COMPOUND, ONE_TO_MANY
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 * @param errorCode       The expected errorCode
	 */
	@Test(dataProvider = "invalidConversionTestCases")
	public void testInvalidConversions(int valueToConvert, AttributeType typeToConvertTo, String errorCode)
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
		return new Object[][] { { 10, XREF, EntityReferenceUnknownConstraintViolationException.class,
				"type:MAINENTITY attribute:mainAttribute value: 10" },
				{ 10, CATEGORICAL, EntityReferenceUnknownConstraintViolationException.class,
						"type:MAINENTITY attribute:mainAttribute value: 10" } };
	}

	@Test(dataProvider = "invalidForeignKeyTestCases")
	public void testInvalidForeignKey(int valueToConvert, AttributeType typeToConvertTo, Class expected, String message)
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
