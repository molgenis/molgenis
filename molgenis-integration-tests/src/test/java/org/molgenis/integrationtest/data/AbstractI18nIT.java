package org.molgenis.integrationtest.data;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.i18n.I18nStringMetaData;
import org.molgenis.data.i18n.I18nUtils;
import org.molgenis.data.i18n.LanguageMetaData;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;;

public abstract class AbstractI18nIT extends AbstractDataIntegrationIT
{
	@Autowired
	LanguageService languageService;

	@BeforeClass
	public void setUp()
	{
		createAdminUser();
		createLanguages();
	}

	@Override
	@AfterClass
	public void cleanUp()
	{
		super.cleanUp();

		List<AttributeMetaData> languageAttrs = new ArrayList<>();
		for (AttributeMetaData attr : AttributeMetaDataMetaData.INSTANCE.getAttributes())
		{
			if (I18nUtils.isI18n(attr.getName()))
			{
				languageAttrs.add(attr);
			}
		}
		languageAttrs.forEach(AttributeMetaDataMetaData.INSTANCE::removeAttributeMetaData);

		languageAttrs.clear();
		for (AttributeMetaData attr : EntityMetaDataMetaData.INSTANCE.getAttributes())
		{
			if (I18nUtils.isI18n(attr.getName()))
			{
				languageAttrs.add(attr);
			}
		}
		languageAttrs.forEach(EntityMetaDataMetaData.INSTANCE::removeAttributeMetaData);
	}

	public void testLanguageService()
	{
		assertEquals(languageService.getCurrentUserLanguageCode(), "nl");
		assertEqualsNoOrder(languageService.getLanguageCodes().toArray(), new String[]
		{ "en", "nl" });

		Entity car = new DefaultEntity(I18nStringMetaData.INSTANCE, dataService);
		car.set(I18nStringMetaData.MSGID, "car");
		car.set("en", "car");
		car.set("nl", "auto");
		dataService.add(I18nStringMetaData.ENTITY_NAME, car);

		assertEquals(languageService.getBundle("en").getString("car"), "car");
		assertEquals(languageService.getBundle("nl").getString("car"), "auto");
		assertEquals(languageService.getBundle().getString("car"), "auto");
	}

	public void testMetaData()
	{
		EditableEntityMetaData entityMetaData = new DefaultEntityMetaData("I18nTest");
		entityMetaData.setDescription("en", "The description");
		entityMetaData.setDescription("nl", "De omschrijving");
		entityMetaData.setLabel("en", "The label");
		entityMetaData.setLabel("nl", "Het label");
		entityMetaData.addAttribute("id", ROLE_ID).setNillable(false);
		entityMetaData.addAttribute("attr-nl", ROLE_LABEL).setDescription("en", "The description (nl)")
				.setDescription("nl", "De omschrijving (nl)").setLabel("en", "The label (nl)")
				.setLabel("nl", "Het label (nl)");
		entityMetaData.addAttribute("attr-en").setDescription("en", "The description (en)")
		.setDescription("nl", "De omschrijving (en)").setLabel("en", "The label (en)")
		.setLabel("nl", "Het label (en)");

		metaDataService.addEntityMeta(entityMetaData);

		EntityMetaData retrieved = metaDataService.getEntityMetaData(entityMetaData.getName());
		String languageCode = languageService.getCurrentUserLanguageCode();
		assertEquals(retrieved.getDescription(languageCode), "De omschrijving");
		assertEquals(retrieved.getLabel(languageCode), "Het label");

		AttributeMetaData attr = entityMetaData.getAttribute("attr-" + languageCode);
		assertNotNull(attr);
		assertEquals(attr.getDescription(languageCode), "De omschrijving (nl)");
		assertEquals(attr.getLabel(languageCode), "Het label (nl)");
		assertEqualsNoOrder(attr.getLabelLanguageCodes().toArray(), new String[]
		{ "en", "nl" });
		assertEquals(entityMetaData.getLabelAttribute(), attr);

		
		attr = entityMetaData.getAttribute("attr-en");
		entityMetaData.setLabelAttribute(attr);
		assertNotNull(attr);
		assertEquals(attr.getDescription(languageCode), "De omschrijving (en)");
		assertEquals(attr.getLabel(languageCode), "Het label (en)");
		assertEqualsNoOrder(attr.getLabelLanguageCodes().toArray(), new String[]
		{ "en", "nl" });
		assertEquals(entityMetaData.getLabelAttribute(), attr);
	}

	protected void createAdminUser()
	{
		MolgenisUser admin = new MolgenisUser();
		admin.setUsername("admin");
		admin.setActive(true);
		admin.setEmail("admin@molgenis.org");
		admin.setLanguageCode("nl");
		admin.setSuperuser(true);
		admin.setChangePassword(false);
		admin.setPassword("molgenis");
		dataService.add(MolgenisUser.ENTITY_NAME, admin);
	}

	protected void createLanguages()
	{
		Entity en = new DefaultEntity(LanguageMetaData.INSTANCE, dataService);
		en.set(LanguageMetaData.CODE, "en");
		en.set(LanguageMetaData.NAME, "English");
		dataService.add(LanguageMetaData.ENTITY_NAME, en);

		Entity nl = new DefaultEntity(LanguageMetaData.INSTANCE, dataService);
		nl.set(LanguageMetaData.CODE, "nl");
		nl.set(LanguageMetaData.NAME, "Nederlands");
		dataService.add(LanguageMetaData.ENTITY_NAME, nl);
	}
}
