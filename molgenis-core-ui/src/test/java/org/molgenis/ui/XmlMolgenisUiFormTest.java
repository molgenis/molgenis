package org.molgenis.ui;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.framework.server.MolgenisPermissionService.Permission;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class XmlMolgenisUiFormTest
{
	private MolgenisPermissionService molgenisPermissionService;

	@BeforeMethod
	public void setUp()
	{
		molgenisPermissionService = mock(MolgenisPermissionService.class);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void XmlMolgenisUiForm()
	{
		new XmlMolgenisUiForm(null, null);
	}

	@Test
	public void getId()
	{
		String formId = "formId";
		FormType formType = new FormType();
		formType.setName(formId);

		XmlMolgenisUiForm xmlMolgenisUiForm = new XmlMolgenisUiForm(molgenisPermissionService, formType);
		assertEquals(xmlMolgenisUiForm.getId(), formId);
	}

	@Test
	public void getName()
	{
		String formId = "formId";
		String formName = "formName";
		FormType formType = new FormType();
		formType.setName(formId);
		formType.setLabel(formName);

		XmlMolgenisUiForm xmlMolgenisUiForm = new XmlMolgenisUiForm(molgenisPermissionService, formType);
		assertEquals(xmlMolgenisUiForm.getName(), formName);
	}

	@Test
	public void getNameByName()
	{
		String formId = "formId";
		FormType formType = new FormType();
		formType.setName(formId);

		XmlMolgenisUiForm xmlMolgenisUiForm = new XmlMolgenisUiForm(molgenisPermissionService, formType);
		assertEquals(xmlMolgenisUiForm.getName(), formId);
	}

	@Test
	public void getType()
	{
		XmlMolgenisUiForm xmlMolgenisUiForm = new XmlMolgenisUiForm(molgenisPermissionService, new FormType());
		assertEquals(xmlMolgenisUiForm.getType(), MolgenisUiMenuItemType.FORM);
	}

	@Test
	public void isAuthorized()
	{
		String formEntity = "entity";
		when(molgenisPermissionService.hasPermissionOnEntity(formEntity, Permission.READ)).thenReturn(true);
		FormType formType = new FormType();
		formType.setEntity(formEntity);
		XmlMolgenisUiForm xmlMolgenisUiForm = new XmlMolgenisUiForm(molgenisPermissionService, formType);
		assertTrue(xmlMolgenisUiForm.isAuthorized());
	}

	@Test
	public void isAuthorized_notAuthorized()
	{
		FormType formType = new FormType();
		formType.setEntity("entity_notauthorized");
		XmlMolgenisUiForm xmlMolgenisUiForm = new XmlMolgenisUiForm(molgenisPermissionService, new FormType());
		assertFalse(xmlMolgenisUiForm.isAuthorized());
	}
}
