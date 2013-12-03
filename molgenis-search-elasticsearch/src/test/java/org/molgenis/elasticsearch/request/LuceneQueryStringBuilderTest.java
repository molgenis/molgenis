package org.molgenis.elasticsearch.request;

import static org.molgenis.data.QueryRule.Operator.AND;
import static org.molgenis.data.QueryRule.Operator.EQUALS;
import static org.molgenis.data.QueryRule.Operator.GREATER;
import static org.molgenis.data.QueryRule.Operator.GREATER_EQUAL;
import static org.molgenis.data.QueryRule.Operator.LESS;
import static org.molgenis.data.QueryRule.Operator.LESS_EQUAL;
import static org.molgenis.data.QueryRule.Operator.LIKE;
import static org.molgenis.data.QueryRule.Operator.NOT;
import static org.molgenis.data.QueryRule.Operator.OR;
import static org.molgenis.data.QueryRule.Operator.SEARCH;
import static org.molgenis.elasticsearch.request.LuceneQueryStringBuilder.buildQueryString;
import static org.molgenis.elasticsearch.request.LuceneQueryStringBuilder.escapeField;
import static org.molgenis.elasticsearch.request.LuceneQueryStringBuilder.escapeValue;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.molgenis.data.QueryRule;
import org.testng.annotations.Test;

public class LuceneQueryStringBuilderTest
{

	@Test
	public void testBuildQueryString()
	{
		assertEquals(buildQueryString(Collections.<QueryRule> emptyList()), "*:*");
		assertEquals(buildQueryString(Arrays.asList(new QueryRule("test", EQUALS, "xxx"))), "test:xxx");
		assertEquals(buildQueryString(Arrays.asList(new QueryRule("test", EQUALS, null))), "_missing_:test");
		assertEquals(buildQueryString(Arrays.asList(new QueryRule("test", SEARCH, "xxx"))), "test:xxx");
		assertEquals(buildQueryString(Arrays.asList(new QueryRule(SEARCH, "xxx"))), "xxx");
		assertEquals(buildQueryString(Arrays.asList(new QueryRule("test", NOT, "xxx"))), "-test:xxx");
		assertEquals(buildQueryString(Arrays.asList(new QueryRule("test", LIKE, "xxx"))), "test:*xxx*");
		assertEquals(buildQueryString(Arrays.asList(new QueryRule("test", LESS, 9))), "test:{* TO 9}");
		assertEquals(buildQueryString(Arrays.asList(new QueryRule("test", LESS_EQUAL, 9))), "test:[* TO 9]");
		assertEquals(buildQueryString(Arrays.asList(new QueryRule("test", GREATER, 9))), "test:{9 TO *}");
		assertEquals(buildQueryString(Arrays.asList(new QueryRule("test", GREATER_EQUAL, 9))), "test:[9 TO *]");
		assertEquals(buildQueryString(Arrays.asList(new QueryRule("test", EQUALS, "xxx"), new QueryRule(AND),
				new QueryRule("aaa", EQUALS, "yyy"))), "test:xxx AND aaa:yyy");
		assertEquals(buildQueryString(Arrays.asList(new QueryRule("test", EQUALS, "xxx"), new QueryRule(OR),
				new QueryRule("aaa", EQUALS, "yyy"))), "test:xxx OR aaa:yyy");
	}

	@Test
	public void testEscapeValue()
	{
		// The characters that need to be escaped are: + - ! ( ) { } [ ] ^
		// " ~ * ? : \ + |
		assertEquals(escapeValue("+"), "\\+");
		assertEquals(escapeValue("-"), "\\-");
		assertEquals(escapeValue("!"), "\\!");
		assertEquals(escapeValue("("), "\\(");
		assertEquals(escapeValue(")"), "\\)");
		assertEquals(escapeValue("{"), "\\{");
		assertEquals(escapeValue("}"), "\\}");
		assertEquals(escapeValue("["), "\\[");
		assertEquals(escapeValue("]"), "\\]");
		// assertEquals(escapeValue("^"), "\\^");
		assertEquals(escapeValue("\""), "\\\"");
		assertEquals(escapeValue("~"), "\\~");
		assertEquals(escapeValue("*"), "\\*");
		assertEquals(escapeValue("?"), "\\?");
		assertEquals(escapeValue("\\"), "\\\\");
		assertEquals(escapeValue("/"), "\\/");
		assertEquals(escapeValue(":"), "\\:");
		assertEquals(escapeValue("&"), "\\&");
		assertEquals(escapeValue("|"), "\\|");
	}

	@Test
	public void testEscapeField()
	{
		assertEquals(escapeField("+"), "\\+");
		assertEquals(escapeField("-"), "\\-");
		assertEquals(escapeField("!"), "\\!");
		assertEquals(escapeField("("), "\\(");
		assertEquals(escapeField(")"), "\\)");
		assertEquals(escapeField("{"), "\\{");
		assertEquals(escapeField("}"), "\\}");
		assertEquals(escapeField("["), "\\[");
		assertEquals(escapeField("]"), "\\]");
		assertEquals(escapeField("^"), "\\^");
		assertEquals(escapeField("\""), "\\\"");
		assertEquals(escapeField("~"), "\\~");
		assertEquals(escapeField("*"), "\\*");
		assertEquals(escapeField("?"), "\\?");
		assertEquals(escapeField("\\"), "\\\\");
		assertEquals(escapeField(":"), "\\:");
		assertEquals(escapeField("&"), "\\&");
		assertEquals(escapeField(" "), "\\ ");
		assertEquals(escapeField("	"), "\\	");
	}
}
