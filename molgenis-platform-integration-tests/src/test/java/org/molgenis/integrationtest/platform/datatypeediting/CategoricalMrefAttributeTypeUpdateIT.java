package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.ValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class CategoricalMrefAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	@BeforeClass
	public void setup()
	{
		super.setup(CATEGORICAL_MREF, INT);
	}

	@AfterMethod
	public void afterMethod()
	{
		super.afterMethod(CATEGORICAL_MREF);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		List<Entity> entities = dataService.findAll("REFERENCEENTITY").collect(toList());
		return new Object[][] { { entities, MREF, newArrayList("label1", "email label", "hyperlink label") } };
	}

	/**
	 * Valid conversion cases for CATEGORICAL_MREF to:
	 * MREF
	 *
	 * @param valueToConvert  The value that will be converted
	 * @param typeToConvertTo The type to convert to
	 * @param convertedValue  The expected value after converting the type
	 */
	@Test(dataProvider = "validConversionData")
	public void testValidConversion(List<Entity> valueToConvert, AttributeType typeToConvertTo,
			List<String> convertedValue)
	{
		testTypeConversion(valueToConvert, typeToConvertTo);

		// Assert if conversion was successful
		assertEquals(getActualDataType(), typeToConvertTo);

		List<String> actualValues = newArrayList();
		Entity entity1 = dataService.findOneById("MAINENTITY", "1");
		entity1.getEntities("mainAttribute").forEach(entity -> actualValues.add(entity.getLabelValue().toString()));
		assertEquals(actualValues, convertedValue);
	}

	@DataProvider(name = "invalidConversionTestCases")
	public Object[][] invalidConversionTestCases()
	{
		List<Entity> entities = dataService.findAll("REFERENCEENTITY").collect(toList());
		return new Object[][] { { entities, BOOL, "V94" },
				{ entities, STRING, "V94" },
				{ entities, TEXT, "V94" },
				{ entities, SCRIPT, "V94" },
				{ entities, INT, "V94" },
				{ entities, LONG, "V94" },
				{ entities, DECIMAL, "V94" },
				{ entities, XREF, "V94" },
				{ entities, CATEGORICAL, "V94" },
				{ entities, EMAIL, "V94" },
				{ entities, HYPERLINK, "V94" },
				{ entities, HTML, "V94" },
				{ entities, ENUM, "V94" },
				{ entities, DATE, "V94" },
				{ entities, DATE_TIME, "V94" },
				{ entities, FILE, "V94" },
				{ entities, COMPOUND, "V94" },
				{ entities, ONE_TO_MANY, "V94" } };
	}

	/**
	 * Invalid conversion cases for CATEGORICAL to:
	 * BOOL, TEXT, SCRIPT INT, LONG, DECIMAL, EMAIL, HYPERLINK, HTML, ENUM, DATE, DATE_TIME, MREF, MREF, FILE, COMPOUND, ONE_TO_MANY
	 *
	 * @param valueToConvert  The value that will be converted
	 * @param typeToConvertTo The type to convert to
	 * @param errorCode       The expected errorCode
	 */
	@Test(dataProvider = "invalidConversionTestCases")
	public void testInvalidConversion(List<Entity> valueToConvert, AttributeType typeToConvertTo, String errorCode)
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
