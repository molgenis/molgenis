package org.molgenis.data.version.v1_8;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

import java.util.HashSet;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;

public class Step11ConvertNamesTest
{
	DataSource dataSource;
	Step11ConvertNames converter;

	@BeforeClass
	public void setup()
	{

		converter = new Step11ConvertNames(mock(SingleConnectionDataSource.class));
	}

	@Test
	public void testFixName()
	{
		assertEquals(converter.fixName("NameWith.InvalidChars()"), "NameWithInvalidChars");
		assertEquals(converter.fixName("debugger"), "debugger1");
		assertEquals(converter.fixName("52NameWithDigit"), "NameWithDigit");
		assertEquals(converter.fixName("ThisAttributeIsWayTooLongAndShouldBeTruncated"),
				"ThisAttributeIsWayTooLongAndSh");

		assertEquals(converter.fixName("debugger()"), "debugger1");
		assertEquals(converter.fixName("1debugger"), "debugger1");

	}

	@Test
	public void testMakeNameUnique()
	{
		HashSet<String> scope = Sets.newHashSet("test");
		assertEquals(converter.makeNameUnique("test", scope), "test1");

		scope.add("test1");
		assertEquals(converter.makeNameUnique("test", scope), "test2");

		scope.add("test55");
		assertEquals(converter.makeNameUnique("test", 55, scope), "test56");

		scope.add("ThisAttributeIsWayTooLongAndSh");
		assertEquals(converter.makeNameUnique("ThisAttributeIsWayTooLongAndSh", scope),
				"ThisAttributeIsWayTooLongAndS1");

		scope.add("ThisAttributeIsWayTooLongAndS1");
		scope.add("ThisAttributeIsWayTooLongAndS2");
		scope.add("ThisAttributeIsWayTooLongAndS3");
		scope.add("ThisAttributeIsWayTooLongAndS4");
		scope.add("ThisAttributeIsWayTooLongAndS5");
		scope.add("ThisAttributeIsWayTooLongAndS6");
		scope.add("ThisAttributeIsWayTooLongAndS7");
		scope.add("ThisAttributeIsWayTooLongAndS8");
		scope.add("ThisAttributeIsWayTooLongAndS9");
		assertEquals(converter.makeNameUnique("ThisAttributeIsWayTooLongAndSh", scope),
				"ThisAttributeIsWayTooLongAnd10");

		scope.add("ThisAttributeIsWayTooLongAn533");
		assertEquals(converter.makeNameUnique("ThisAttributeIsWayTooLongAndSh", 533, scope),
				"ThisAttributeIsWayTooLongAn534");
	}

}
