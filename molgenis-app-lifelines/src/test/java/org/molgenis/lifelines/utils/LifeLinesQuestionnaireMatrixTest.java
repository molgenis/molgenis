package org.molgenis.lifelines.utils;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Set;

import org.molgenis.lifelines.utils.LifeLinesQuestionnaireMatrix.CohortTimePair;
import org.testng.annotations.Test;

public class LifeLinesQuestionnaireMatrixTest
{
	@Test
	public void addAndGet()
	{
		LifeLinesQuestionnaireMatrix questionnaireMatrix = new LifeLinesQuestionnaireMatrix();
		questionnaireMatrix.add("group1", "code1", Collections.singleton(new CohortTimePair("cohort1", "types1")));
		Set<CohortTimePair> info = questionnaireMatrix.get("group1", "code1");
		assertEquals(info.size(), 1);
		assertTrue(info.contains(new CohortTimePair("cohort1", "types1")));
	}

	@Test
	public void parse() throws IOException, URISyntaxException
	{
		InputStream is = this.getClass().getResourceAsStream("/questionnaire_checklist.xls");
		LifeLinesQuestionnaireMatrix questionnaireMatrix = LifeLinesQuestionnaireMatrix.parse(is);

		Set<CohortTimePair> info1 = questionnaireMatrix.get("group1", "code1");
		assertEquals(info1.size(), 1);
		assertTrue(info1.contains(new CohortTimePair("Cohort1", "Baseline")));

		Set<CohortTimePair> info2 = questionnaireMatrix.get("group1", "code2");
		assertEquals(info2.size(), 2);
		assertTrue(info2.contains(new CohortTimePair("Cohort1", "Baseline")));
		assertTrue(info2.contains(new CohortTimePair("Cohort1", "Follow-up")));

		Set<CohortTimePair> info3 = questionnaireMatrix.get("group2", "code2");
		assertEquals(info3.size(), 3);
		assertTrue(info3.contains(new CohortTimePair("Cohort1", "Baseline")));
		assertTrue(info3.contains(new CohortTimePair("Cohort1", "Follow-up")));
		assertTrue(info3.contains(new CohortTimePair("Cohort2", "Baseline")));
	}
}
