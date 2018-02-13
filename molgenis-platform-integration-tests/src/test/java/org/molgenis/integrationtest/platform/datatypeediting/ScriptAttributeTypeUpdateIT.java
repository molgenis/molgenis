package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.*;

import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
@TestExecutionListeners(listeners = { WithSecurityContextTestExecutionListener.class })
@Transactional
public class ScriptAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	private static final String USERNAME = "script-attribute-type-update-user";

	@BeforeClass
	public void setup()
	{
		super.setup(SCRIPT, STRING);
	}

	@AfterMethod
	public void afterMethod()
	{
		super.afterMethod(SCRIPT);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		return new Object[][] {
				{ "import test; function(){this is a test script in STRING format; return MOLGENIS}", STRING,
						"import test; function(){this is a test script in STRING format; return MOLGENIS}" },
				{ "import test; function(){this is a test script in TEXT format; return MOLGENIS}", TEXT,
						"import test; function(){this is a test script in TEXT format; return MOLGENIS}" } };
	}

	/**
	 * Valid conversion cases for SCRIPT to:
	 * STRING, TEXT
	 *
	 * @param valueToConvert  The value that will be converted
	 * @param typeToConvertTo The type to convert to
	 * @param convertedValue  The expected value after converting the type
	 */
	@WithMockUser(username = USERNAME)
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
		return new Object[][] { { "function log(data){console.log(data)}", BOOL, MolgenisValidationException.class,
				"Attribute data type update from [SCRIPT] to [BOOL] not allowed, allowed types are [STRING, TEXT]" },
				{ "function log(data){console.log(data)}", INT, MolgenisValidationException.class,
						"Attribute data type update from [SCRIPT] to [INT] not allowed, allowed types are [STRING, TEXT]" },
				{ "function log(data){console.log(data)}", LONG, MolgenisValidationException.class,
						"Attribute data type update from [SCRIPT] to [LONG] not allowed, allowed types are [STRING, TEXT]" },
				{ "function log(data){console.log(data)}", DECIMAL, MolgenisValidationException.class,
						"Attribute data type update from [SCRIPT] to [DECIMAL] not allowed, allowed types are [STRING, TEXT]" },
				{ "function log(data){console.log(data)}", XREF, MolgenisValidationException.class,
						"Attribute data type update from [SCRIPT] to [XREF] not allowed, allowed types are [STRING, TEXT]" },
				{ "function log(data){console.log(data)}", CATEGORICAL, MolgenisValidationException.class,
						"Attribute data type update from [SCRIPT] to [CATEGORICAL] not allowed, allowed types are [STRING, TEXT]" },
				{ "function log(data){console.log(data)}", EMAIL, MolgenisValidationException.class,
						"Attribute data type update from [SCRIPT] to [EMAIL] not allowed, allowed types are [STRING, TEXT]" },
				{ "function log(data){console.log(data)}", HYPERLINK, MolgenisValidationException.class,
						"Attribute data type update from [SCRIPT] to [HYPERLINK] not allowed, allowed types are [STRING, TEXT]" },
				{ "function log(data){console.log(data)}", HTML, MolgenisValidationException.class,
						"Attribute data type update from [SCRIPT] to [HTML] not allowed, allowed types are [STRING, TEXT]" },
				{ "function log(data){console.log(data)}", ENUM, MolgenisValidationException.class,
						"Attribute data type update from [SCRIPT] to [ENUM] not allowed, allowed types are [STRING, TEXT]" },
				{ "function log(data){console.log(data)}", DATE, MolgenisValidationException.class,
						"Attribute data type update from [SCRIPT] to [DATE] not allowed, allowed types are [STRING, TEXT]" },
				{ "function log(data){console.log(data)}", DATE_TIME, MolgenisValidationException.class,
						"Attribute data type update from [SCRIPT] to [DATE_TIME] not allowed, allowed types are [STRING, TEXT]" },
				{ "function log(data){console.log(data)}", MREF, MolgenisValidationException.class,
						"Attribute data type update from [SCRIPT] to [MREF] not allowed, allowed types are [STRING, TEXT]" },
				{ "function log(data){console.log(data)}", CATEGORICAL_MREF, MolgenisValidationException.class,
						"Attribute data type update from [SCRIPT] to [CATEGORICAL_MREF] not allowed, allowed types are [STRING, TEXT]" },
				{ "function log(data){console.log(data)}", FILE, MolgenisValidationException.class,
						"Attribute data type update from [SCRIPT] to [FILE] not allowed, allowed types are [STRING, TEXT]" },
				{ "function log(data){console.log(data)}", COMPOUND, MolgenisValidationException.class,
						"Attribute data type update from [SCRIPT] to [COMPOUND] not allowed, allowed types are [STRING, TEXT]" },
				{ "function log(data){console.log(data)}", ONE_TO_MANY, MolgenisValidationException.class,
						"Invalid [xref] value [] for attribute [Referenced entity] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('refEntityType').isNull().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/).not()).or($('refEntityType').isNull().not().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/))).value().Invalid [xref] value [] for attribute [Mapped by] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('mappedBy').isNull().and($('type').eq('onetomany').not()).or($('mappedBy').isNull().not().and($('type').eq('onetomany'))).value()" } };
	}

	/**
	 * Invalid conversion cases for SCRIPT to:
	 * BOOL, INT, LONG, DECIMAL, XREF, CATEGORICAL, EMAIL, HYPERLINK, HTML, ENUM, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, FILE, COMPOUND, ONE_TO_MANY
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 * @param exceptionClass   The expected class of the exception that will be thrown
	 * @param exceptionMessage The expected exception message
	 */
	@WithMockUser(username = USERNAME)
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
