package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.*;

import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class CompoundAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	@BeforeClass
	public void setup()
	{
		super.setup(COMPOUND, STRING);
	}

	@AfterMethod
	public void afterMethod()
	{
		super.afterMethod(COMPOUND);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		return new Object[][] { { null, STRING, null } };
	}

	/**
	 * Valid conversion cases for COMPOUND to:
	 * STRING
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
		return new Object[][] { { null, BOOL, MolgenisValidationException.class,
				"Attribute data type update from [COMPOUND] to [BOOL] not allowed, allowed types are [STRING]" },
				{ null, TEXT, MolgenisValidationException.class,
						"Attribute data type update from [COMPOUND] to [TEXT] not allowed, allowed types are [STRING]" },
				{ null, SCRIPT, MolgenisValidationException.class,
						"Attribute data type update from [COMPOUND] to [SCRIPT] not allowed, allowed types are [STRING]" },
				{ null, INT, MolgenisValidationException.class,
						"Attribute data type update from [COMPOUND] to [INT] not allowed, allowed types are [STRING]" },
				{ null, LONG, MolgenisValidationException.class,
						"Attribute data type update from [COMPOUND] to [LONG] not allowed, allowed types are [STRING]" },
				{ null, DECIMAL, MolgenisValidationException.class,
						"Attribute data type update from [COMPOUND] to [DECIMAL] not allowed, allowed types are [STRING]" },
				{ null, XREF, MolgenisValidationException.class,
						"Attribute data type update from [COMPOUND] to [XREF] not allowed, allowed types are [STRING]" },
				{ null, CATEGORICAL, MolgenisValidationException.class,
						"Attribute data type update from [COMPOUND] to [CATEGORICAL] not allowed, allowed types are [STRING]" },
				{ null, EMAIL, MolgenisValidationException.class,
						"Attribute data type update from [COMPOUND] to [EMAIL] not allowed, allowed types are [STRING]" },
				{ null, HYPERLINK, MolgenisValidationException.class,
						"Attribute data type update from [COMPOUND] to [HYPERLINK] not allowed, allowed types are [STRING]" },
				{ null, HTML, MolgenisValidationException.class,
						"Attribute data type update from [COMPOUND] to [HTML] not allowed, allowed types are [STRING]" },
				{ null, ENUM, MolgenisValidationException.class,
						"Attribute data type update from [COMPOUND] to [ENUM] not allowed, allowed types are [STRING]" },
				{ null, DATE, MolgenisValidationException.class,
						"Attribute data type update from [COMPOUND] to [DATE] not allowed, allowed types are [STRING]" },
				{ null, DATE_TIME, MolgenisValidationException.class,
						"Attribute data type update from [COMPOUND] to [DATE_TIME] not allowed, allowed types are [STRING]" },
				{ null, MREF, MolgenisValidationException.class,
						"Attribute data type update from [COMPOUND] to [MREF] not allowed, allowed types are [STRING]" },
				{ null, CATEGORICAL_MREF, MolgenisValidationException.class,
						"Attribute data type update from [COMPOUND] to [CATEGORICAL_MREF] not allowed, allowed types are [STRING]" },
				{ null, FILE, MolgenisValidationException.class,
						"Attribute data type update from [COMPOUND] to [FILE] not allowed, allowed types are [STRING]" },
				{ null, ONE_TO_MANY, MolgenisValidationException.class,
						"Invalid [xref] value [] for attribute [Referenced entity] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('refEntityType').isNull().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/).not()).or($('refEntityType').isNull().not().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/))).value().Invalid [xref] value [] for attribute [Mapped by] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('mappedBy').isNull().and($('type').eq('onetomany').not()).or($('mappedBy').isNull().not().and($('type').eq('onetomany'))).value()" } };
	}

	/**
	 * Invalid conversion cases for COMPOUND to:
	 * BOOL, TEXT, INT, LONG, DECIMAL, XREF, CATEGORICAL, EMAIL, HYPERLINK, HTML, ENUM, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, FILE, ONE_TO_MANY
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 * @param exceptionClass   The expected class of the exception that will be thrown
	 * @param exceptionMessage The expected exception message
	 */
	@Test(dataProvider = "invalidConversionTestCases")
	public void testInvalidConversion(String valueToConvert, AttributeType typeToConvertTo, Class exceptionClass,
			String exceptionMessage)
	{
		try
		{
			testTypeConversion(valueToConvert, typeToConvertTo);
			fail("Conversion should have failed");
		}
		catch (Exception exception)
		{
			assertTrue(exception.getClass().isAssignableFrom(exceptionClass));
			assertEquals(exception.getMessage(), exceptionMessage);
		}
	}
}
