package org.molgenis.fieldtypes;

import static org.molgenis.MolgenisFieldTypes.*;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by mswertz on 06/04/14.
 */
public class FieldTypeTest
{
	@Test
	public void testConvert()
	{
		// bool
		Assert.assertEquals(BOOL.convert("true").getClass(), Boolean.class);
		Assert.assertEquals(BOOL.convert(1), true);
		Assert.assertEquals(BOOL.convert(true), true);

		// string
		Assert.assertEquals(STRING.convert(1).getClass(), String.class);
		Assert.assertEquals(TEXT.convert(1).getClass(), String.class);
		Assert.assertEquals(INT.convert("1").getClass(), Integer.class);

		// date
		Assert.assertEquals(DATE.convert("2013-03-13").getClass(), java.util.Date.class);
		java.util.Date d = new java.util.Date();
		Assert.assertEquals(DATE.convert(d), d);
		try
		{
			DECIMAL.convert("blaat");
			Assert.fail("should have failed");
		}
		catch (Exception e)
		{
		}

		// datetime
		Assert.assertEquals(DATETIME.convert("2013-03-13 23:22:10").getClass(), java.util.Date.class);
		Assert.assertEquals(DATETIME.convert(new java.sql.Date(113, 02, 12)), new java.sql.Date(113, 02, 12));

		// decimal
		Assert.assertEquals(DECIMAL.convert("2.3").getClass(), Double.class);
		Assert.assertEquals(DECIMAL.convert("2.3"), 2.3);
		Assert.assertEquals(DECIMAL.convert(2.3), 2.3);
		try
		{
			DECIMAL.convert(true);
			Assert.fail("should have failed");
		}
		catch (Exception e)
		{
		}

	}

}
