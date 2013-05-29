package org.molgenis.lifelines.utils;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
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
		questionnaireMatrix.add("group1", "code1", Collections.singleton(new CohortTimePair(1, 1)));
		Set<CohortTimePair> info = questionnaireMatrix.get("group1", "code1");
		assertEquals(info.size(), 1);
		assertTrue(info.contains(new CohortTimePair(1, 1)));
	}

	@Test
	public void addAndGetProtocolDescription()
	{
		LifeLinesQuestionnaireMatrix questionnaireMatrix = new LifeLinesQuestionnaireMatrix();
		questionnaireMatrix.addProtocol(1, "Protocol1");
		assertEquals(questionnaireMatrix.getProtocolDescription(1), "Protocol1");
		assertNull(questionnaireMatrix.getProtocolDescription(-1));
	}

	@Test
	public void addAndGetVmidDescription()
	{
		LifeLinesQuestionnaireMatrix questionnaireMatrix = new LifeLinesQuestionnaireMatrix();
		questionnaireMatrix.addVmid(1, "VMID1");
		assertEquals(questionnaireMatrix.getVmidDescription(1), "VMID1");
		assertNull(questionnaireMatrix.getVmidDescription(-1));
	}

	@Test
	public void parse() throws IOException, URISyntaxException
	{
		InputStream is = this.getClass().getResourceAsStream("/questionnaire_checklist.xls");
		LifeLinesQuestionnaireMatrix questionnaireMatrix = LifeLinesQuestionnaireMatrix.parse(is);

		Set<CohortTimePair> info1 = questionnaireMatrix.get("group1", "code1");
		assertEquals(info1.size(), 3);
		assertTrue(info1.contains(new CohortTimePair(1, 1)));
		assertTrue(info1.contains(new CohortTimePair(2, 1)));
		assertTrue(info1.contains(new CohortTimePair(3, 2)));

		Set<CohortTimePair> info2 = questionnaireMatrix.get("group1", "code2");
		assertEquals(info2.size(), 2);
		assertTrue(info2.contains(new CohortTimePair(3, 2)));
		assertTrue(info2.contains(new CohortTimePair(4, 2)));

		Set<CohortTimePair> info3 = questionnaireMatrix.get("group2", "code3");
		assertEquals(info3.size(), 2);
		assertTrue(info3.contains(new CohortTimePair(1, 1)));
		assertTrue(info3.contains(new CohortTimePair(2, 1)));

		assertEquals(questionnaireMatrix.getProtocolDescription(1), "Protocol1");
		assertEquals(questionnaireMatrix.getProtocolDescription(2), "Protocol2");
		assertEquals(questionnaireMatrix.getProtocolDescription(3), "Protocol3");
		assertEquals(questionnaireMatrix.getProtocolDescription(4), "Protocol4");

		assertEquals(questionnaireMatrix.getVmidDescription(1), "VMID1");
		assertEquals(questionnaireMatrix.getVmidDescription(2), "VMID2");
	}
}
