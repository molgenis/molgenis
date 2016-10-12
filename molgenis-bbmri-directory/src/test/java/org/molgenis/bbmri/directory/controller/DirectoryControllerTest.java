package org.molgenis.bbmri.directory.controller;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Test DirectoryController
 */
public class DirectoryControllerTest
{
	@Test
	public void generateBase64AuthenticationTest()
	{
		String base64_1 = DirectoryController.generateBase64Authentication("username", "password");
		assertEquals(base64_1, "Basic dXNlcm5hbWU6cGFzc3dvcmQ=");
	}
}
