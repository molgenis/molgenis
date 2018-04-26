package org.molgenis.util;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class RegexUtilsTest
{
	private String javaCronJobRegex;
	private String javaCommaSeparatedEmailListRegex;

	@BeforeClass
	public void setup() {
		this.javaCronJobRegex = toJavaRegex(RegexUtils.JAVA_SCRIPT_CRON_REGEX);
		this.javaCommaSeparatedEmailListRegex = toJavaRegex(RegexUtils.JAVA_SCRIPT_COMMA_SEPARATED_EMAIL_LIST_REGEX);
	}

	@Test
	public void cronJobRegex() {
		assertTrue("0 0 12 * * ?".matches(javaCronJobRegex));
		assertTrue("".matches(javaCronJobRegex));
	}

	@Test
	public void commaSeparatedEmailListRegex() {
		assertTrue("c.stroomberg@umcg.nl".matches(javaCommaSeparatedEmailListRegex));
		assertTrue("c.stroomberg@umcg.nl, janjansen@gmail.com".matches(javaCommaSeparatedEmailListRegex));
		assertTrue("c.stroomberg@umcg.nl,janjansen@gmail.com".matches(javaCommaSeparatedEmailListRegex));
		assertTrue("".matches(javaCommaSeparatedEmailListRegex));
		assertTrue("undefined".matches(javaCommaSeparatedEmailListRegex));
		assertTrue("null".matches(javaCommaSeparatedEmailListRegex));

		assertFalse("c.stroomberg".matches(javaCommaSeparatedEmailListRegex));
		assertFalse("c.stroomberg@".matches(javaCommaSeparatedEmailListRegex));
		assertFalse("umcg.nl".matches(javaCommaSeparatedEmailListRegex));
		assertFalse("c.stroomberg@umcg.nljanjansen@gmail.com".matches(javaCommaSeparatedEmailListRegex));
		assertFalse("c.stroomberg@umcg.nl janjansen@gmail.com".matches(javaCommaSeparatedEmailListRegex));

	}

	private String toJavaRegex(String javaScriptRegex) {
		return javaScriptRegex.substring(1, javaScriptRegex.length()-1);
	}
}
