package org.molgenis.integrationtest.data.abstracts;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.MolgenisUserFactory;
import org.molgenis.data.Entity;
import org.molgenis.data.i18n.I18nUtils;
import org.molgenis.data.i18n.LanguageFactory;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.i18n.model.I18nStringMetaData;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataMetaData;
import org.molgenis.data.meta.model.EntityMetaDataMetaData;
import org.molgenis.data.support.DynamicEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.util.ArrayList;
import java.util.List;

import static org.molgenis.auth.MolgenisUserMetaData.MOLGENIS_USER;
import static org.molgenis.data.i18n.I18nStringMetaData.I18N_STRING;
import static org.molgenis.data.i18n.LanguageMetaData.LANGUAGE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;

;

public abstract class AbstractI18nIT extends AbstractDataIntegrationIT
{
	@Autowired
	LanguageService languageService;

	@Autowired
	private EntityMetaDataMetaData entityMetaDataMetaData;

	@Autowired
	private AttributeMetaDataMetaData attributeMetaDataMetaData;

	@Autowired
	private I18nStringMetaData i18nStringMetaData;

	@Autowired
	private LanguageFactory languageFactory;

	@Autowired
	private MolgenisUserFactory molgenisUserFactory;

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
		for (AttributeMetaData attr : attributeMetaDataMetaData.getAttributes())
		{
			if (I18nUtils.isI18n(attr.getName()))
			{
				languageAttrs.add(attr);
			}
		}
		languageAttrs.forEach(attributeMetaDataMetaData::removeAttribute);

		languageAttrs.clear();
		for (AttributeMetaData attr : entityMetaDataMetaData.getAttributes())
		{
			if (I18nUtils.isI18n(attr.getName()))
			{
				languageAttrs.add(attr);
			}
		}
		languageAttrs.forEach(entityMetaDataMetaData::removeAttribute);
	}

	public void testLanguageService()
	{
		assertEquals(languageService.getCurrentUserLanguageCode(), "nl");
		assertEqualsNoOrder(languageService.getLanguageCodes().toArray(), new String[]
		{ "en", "nl" });

		Entity car = new DynamicEntity(i18nStringMetaData);
		car.set(I18nStringMetaData.MSGID, "car");
		car.set("en", "car");
		car.set("nl", "auto");
		dataService.add(I18N_STRING, car);

		assertEquals(languageService.getBundle("en").getString("car"), "car");
		assertEquals(languageService.getBundle("nl").getString("car"), "auto");
		assertEquals(languageService.getBundle().getString("car"), "auto");
	}

	public void testMetaData()
	{
		//		EntityMetaData entityMetaData = new EntityMetaData("I18nTest"); // FIXME
		//		entityMetaData.setDescription("en", "The description");
		//		entityMetaData.setDescription("nl", "De omschrijving");
		//		entityMetaData.setLabel("en", "The label");
		//		entityMetaData.setLabel("nl", "Het label");
		//		entityMetaData.addAttribute("id", ROLE_ID).setNillable(false);
		//		entityMetaData.addAttribute("attr-nl", ROLE_LABEL).setDescription("en", "The description (nl)")
		//				.setDescription("nl", "De omschrijving (nl)").setLabel("en", "The label (nl)")
		//				.setLabel("nl", "Het label (nl)");
		//		entityMetaData.addAttribute("attr-en").setDescription("en", "The description (en)")
		//		.setDescription("nl", "De omschrijving (en)").setLabel("en", "The label (en)")
		//		.setLabel("nl", "Het label (en)");
		//
		//		metaDataService.addEntityMeta(entityMetaData);
		//
		//		EntityMetaData retrieved = metaDataService.getEntityMetaData(entityMetaData.getName());
		//		String languageCode = languageService.getCurrentUserLanguageCode();
		//		assertEquals(retrieved.getDescription(languageCode), "De omschrijving");
		//		assertEquals(retrieved.getLabel(languageCode), "Het label");
		//
		//		AttributeMetaData attr = entityMetaData.getAttribute("attr-" + languageCode);
		//		assertNotNull(attr);
		//		assertEquals(attr.getDescription(languageCode), "De omschrijving (nl)");
		//		assertEquals(attr.getLabel(languageCode), "Het label (nl)");
		//		assertEquals(entityMetaData.getLabelAttribute(), attr);
		//
		//
		//		attr = entityMetaData.getAttribute("attr-en");
		//		entityMetaData.setLabelAttribute(attr);
		//		assertNotNull(attr);
		//		assertEquals(attr.getDescription(languageCode), "De omschrijving (en)");
		//		assertEquals(attr.getLabel(languageCode), "Het label (en)");
		//		assertEquals(entityMetaData.getLabelAttribute(), attr);
	}

	protected void createAdminUser()
	{
		MolgenisUser admin = molgenisUserFactory.create();
		admin.setUsername("admin");
		admin.setActive(true);
		admin.setEmail("admin@molgenis.org");
		admin.setLanguageCode("nl");
		admin.setSuperuser(true);
		admin.setChangePassword(false);
		admin.setPassword("molgenis");
		dataService.add(MOLGENIS_USER, admin);
	}

	protected void createLanguages()
	{
		dataService.add(LANGUAGE, languageFactory.create("en", "English"));
		dataService.add(LANGUAGE, languageFactory.create("nl", "Nederlands"));
	}
}
