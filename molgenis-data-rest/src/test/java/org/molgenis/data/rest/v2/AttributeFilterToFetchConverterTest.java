package org.molgenis.data.rest.v2;

import org.molgenis.auth.SecurityPackage;
import org.molgenis.data.Fetch;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.system.model.RootSystemPackage;
import org.molgenis.file.model.FileMetaMetaData;
import org.molgenis.security.owned.OwnedEntityMetaData;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static java.util.Arrays.asList;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@ContextConfiguration(classes = { AttributeFilterToFetchConverterTest.Config.class })
public class AttributeFilterToFetchConverterTest extends AbstractMolgenisSpringTest
{
	private static final String ID_ATTR_NAME = "attrId";
	private static final String LABEL_ATTR_NAME = "attrLabel";
	private static final String COMPOUND_ATTR_NAME = "attrCompound";
	private static final String COMPOUND_PART_ATTR_NAME = "attrCompoundPart";
	private static final String COMPOUND_PART_FILE_ATTR_NAME = "attrCompoundPartFile";
	private static final String COMPOUND_PART_COMPOUND_ATTR_NAME = "attrCompoundPartCompound";
	private static final String COMPOUND_PART_COMPOUND_PART_ATTR_NAME = "attrCompoundPartCompoundPart";
	private static final String COMPOUND_PART_COMPOUND_PART_ATTR2_NAME = "attr2CompoundPartCompoundPart";
	private static final String XREF_ATTR_NAME = "xrefAttr";

	private static final String REF_ID_ATTR_NAME = "refAttrId";
	private static final String REF_LABEL_ATTR_NAME = "refAttrLabel";
	private static final String REF_ATTR_NAME = "refAttr";

	private EntityMetaData entityMeta;
	private AttributeMetaData labelAttr;
	private AttributeMetaData xrefAttr;
	private EntityMetaData xrefEntityMeta;
	private EntityMetaData selfRefEntityMetaData;

	@Autowired
	private EntityMetaDataFactory entityMetaDataFactory;
	@Autowired
	private AttributeMetaDataFactory attributeMetaDataFactory;
	@Autowired
	private FileMetaMetaData fileMetaMeta;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		selfRefEntityMetaData = entityMetaDataFactory.create().setName("SelfRefEntity");
		AttributeMetaData selfRefIdAttr = attributeMetaDataFactory.create().setName("id");
		selfRefEntityMetaData.addAttribute(selfRefIdAttr, ROLE_ID)
				.addAttribute(attributeMetaDataFactory.create().setName("label"), ROLE_LABEL).addAttribute(
				attributeMetaDataFactory.create().setName("selfRef").setDataType(XREF)
						.setRefEntity(selfRefEntityMetaData));

		labelAttr = attributeMetaDataFactory.create().setName(REF_LABEL_ATTR_NAME);
		xrefEntityMeta = entityMetaDataFactory.create().setName("xrefEntity")
				.addAttribute(attributeMetaDataFactory.create().setName(REF_ID_ATTR_NAME), ROLE_ID)
				.addAttribute(labelAttr, ROLE_LABEL)
				.addAttribute(attributeMetaDataFactory.create().setName(REF_ATTR_NAME));

		entityMeta = entityMetaDataFactory.create().setName("entity")
				.addAttribute(attributeMetaDataFactory.create().setName(ID_ATTR_NAME), ROLE_ID)
				.addAttribute(attributeMetaDataFactory.create().setName(LABEL_ATTR_NAME), ROLE_LABEL);

		AttributeMetaData compoundPartAttr = attributeMetaDataFactory.create().setName(COMPOUND_PART_ATTR_NAME)
				.setDataType(COMPOUND);
		AttributeMetaData compoundPartFileAttr = attributeMetaDataFactory.create().setName(COMPOUND_PART_FILE_ATTR_NAME)
				.setDataType(FILE).setRefEntity(fileMetaMeta);
		AttributeMetaData compoundAttr = attributeMetaDataFactory.create().setName(COMPOUND_ATTR_NAME)
				.setDataType(COMPOUND);
		AttributeMetaData compoundPartCompoundAttr = attributeMetaDataFactory.create()
				.setName(COMPOUND_PART_COMPOUND_ATTR_NAME).setDataType(COMPOUND);
		AttributeMetaData compoundPartCompoundPartAttr = attributeMetaDataFactory.create()
				.setName(COMPOUND_PART_COMPOUND_PART_ATTR_NAME);
		AttributeMetaData compoundPartCompoundPartAttr2 = attributeMetaDataFactory.create()
				.setName(COMPOUND_PART_COMPOUND_PART_ATTR2_NAME);
		compoundAttr.setAttributeParts(asList(compoundPartAttr, compoundPartFileAttr, compoundPartCompoundAttr));
		compoundPartCompoundAttr.setAttributeParts(asList(compoundPartCompoundPartAttr, compoundPartCompoundPartAttr2));
		entityMeta.addAttribute(compoundAttr);

		xrefAttr = attributeMetaDataFactory.create().setName(XREF_ATTR_NAME).setDataType(XREF)
				.setRefEntity(xrefEntityMeta);
		entityMeta.addAttribute(xrefAttr);
	}

	@Test
	public void convertNoAttrFilter()
	{
		Fetch fetch = new Fetch().field(ID_ATTR_NAME).field(LABEL_ATTR_NAME).field(COMPOUND_PART_FILE_ATTR_NAME,
				new Fetch().field(FileMetaMetaData.ID).field(FileMetaMetaData.FILENAME).field(FileMetaMetaData.URL))
				.field(XREF_ATTR_NAME, new Fetch().field(REF_ID_ATTR_NAME).field(REF_LABEL_ATTR_NAME))
				.field(COMPOUND_PART_COMPOUND_PART_ATTR_NAME).field(COMPOUND_PART_COMPOUND_PART_ATTR2_NAME);
		assertEquals(AttributeFilterToFetchConverter.convert(null, entityMeta, "en"), fetch);
	}

	@Test
	public void convertAttrFilterIncludeAll()
	{
		AttributeFilter attrFilter = new AttributeFilter().setIncludeAllAttrs(true);
		assertEquals(AttributeFilterToFetchConverter.convert(attrFilter, entityMeta, "en"),
				new Fetch().field("attrId").field("attrLabel")
						.field("attrCompoundPartFile", new Fetch().field("id").field("filename").field("url"))
						.field("attrCompoundPartCompoundPart").field("attr2CompoundPartCompoundPart")
						.field("xrefAttr", new Fetch().field("refAttrId").field("refAttrLabel")));
	}

	@Test
	public void convertAttrFilterIncludeIdAndLabelAttrs()
	{
		AttributeFilter attrFilter = new AttributeFilter().setIncludeIdAttr(true).setIncludeLabelAttr(true);
		assertEquals(AttributeFilterToFetchConverter.convert(attrFilter, entityMeta, "en"),
				new Fetch().field(ID_ATTR_NAME).field(LABEL_ATTR_NAME));
	}

	@Test
	public void convertAttrFilterIncludeAttr()
	{
		AttributeFilter attrFilter = new AttributeFilter().add(LABEL_ATTR_NAME);
		assertEquals(AttributeFilterToFetchConverter.convert(attrFilter, entityMeta, "en"),
				new Fetch().field(LABEL_ATTR_NAME));
	}

	@Test
	public void convertAttrFilterCompoundAttr()
	{
		AttributeFilter attrFilter = new AttributeFilter().add(COMPOUND_ATTR_NAME);
		Fetch actual = AttributeFilterToFetchConverter.convert(attrFilter, entityMeta, "en");
		Fetch expected = new Fetch().field(COMPOUND_PART_FILE_ATTR_NAME,
				new Fetch().field(FileMetaMetaData.ID).field(FileMetaMetaData.FILENAME).field(FileMetaMetaData.URL))
				.field(COMPOUND_PART_COMPOUND_PART_ATTR_NAME).field(COMPOUND_PART_COMPOUND_PART_ATTR2_NAME);
		assertEquals(actual, expected);
	}

	@Test
	public void convertAttrFilterCompoundAttrPart()
	{
		// FIXME: filtering a compound attribute inside a compound attribute results in an EMPTY fetch because the
		// attribute is not atomic. Unsure if this is intended, but it seems off.
		AttributeFilter attrFilter = new AttributeFilter()
				.add(COMPOUND_ATTR_NAME, new AttributeFilter().add(COMPOUND_PART_ATTR_NAME));
		assertEquals(AttributeFilterToFetchConverter.convert(attrFilter, entityMeta, "en"),
				new Fetch());
	}

	@Test
	public void convertAttrFilterCompoundPartCompoundAttr()
	{
		AttributeFilter attrFilter = new AttributeFilter().add(COMPOUND_ATTR_NAME, new AttributeFilter()
				.add(COMPOUND_PART_COMPOUND_ATTR_NAME,
						new AttributeFilter().add(COMPOUND_PART_COMPOUND_PART_ATTR_NAME)));
		assertEquals(AttributeFilterToFetchConverter.convert(attrFilter, entityMeta, "en"),
				new Fetch().field(COMPOUND_PART_COMPOUND_PART_ATTR_NAME));
	}

	@Test
	public void convertAttrFilterXrefAttr()
	{
		AttributeFilter attrFilter = new AttributeFilter()
				.add(XREF_ATTR_NAME, new AttributeFilter().add(REF_ATTR_NAME));
		assertEquals(AttributeFilterToFetchConverter.convert(attrFilter, entityMeta, "en"),
				new Fetch().field(XREF_ATTR_NAME, new Fetch().field(REF_ATTR_NAME)));
	}

	@Test(expectedExceptions = UnknownAttributeException.class)
	public void convertAttrFilterUnknownAttr()
	{
		AttributeFilter attrFilter = new AttributeFilter().add("unknown");
		AttributeFilterToFetchConverter.convert(attrFilter, entityMeta, "en");
	}

	@Test
	public void createDefaultEntityFetchRefs()
	{
		Fetch fetch = new Fetch().field(ID_ATTR_NAME).field(LABEL_ATTR_NAME).field(COMPOUND_PART_FILE_ATTR_NAME,
				new Fetch().field(FileMetaMetaData.ID).field(FileMetaMetaData.FILENAME).field(FileMetaMetaData.URL))
				.field(XREF_ATTR_NAME, new Fetch().field(REF_ID_ATTR_NAME).field(REF_LABEL_ATTR_NAME))
				.field(COMPOUND_PART_COMPOUND_PART_ATTR_NAME).field(COMPOUND_PART_COMPOUND_PART_ATTR2_NAME);
		assertEquals(AttributeFilterToFetchConverter.createDefaultEntityFetch(entityMeta, "en"), fetch);
	}

	@Test
	public void createDefaultEntityFetchNoRefs()
	{
		assertNull(AttributeFilterToFetchConverter.createDefaultEntityFetch(xrefEntityMeta, "en"));
	}

	@Test
	public void createDefaultEntityFetchRefAttr()
	{
		Fetch fetch = new Fetch().field(REF_ID_ATTR_NAME).field(REF_LABEL_ATTR_NAME);
		assertEquals(AttributeFilterToFetchConverter.createDefaultAttributeFetch(xrefAttr, "en"), fetch);
	}

	@Test
	public void createDefaultAttributeFetchNoRefAttr()
	{
		assertNull(AttributeFilterToFetchConverter.createDefaultAttributeFetch(labelAttr, "en"));
	}

	@Test
	/**
	 * attrs=~id,selfRef should fetch id, and selfRef(id, label)
	 */ public void testConvertSelfRefIncludeId()
	{
		AttributeFilter filter = new AttributeFilter().setIncludeIdAttr(true).add("selfRef");
		Fetch fetch = AttributeFilterToFetchConverter.convert(filter, selfRefEntityMetaData, "en");
		assertEquals(fetch, new Fetch().field("id").field("selfRef", new Fetch().field("id").field("label")));
	}

	@Test
	/**
	 * attrs=id,selfRef should fetch id, and selfRef(id, label)
	 */ public void testConvertIdSelfRef()
	{
		AttributeFilter filter = new AttributeFilter().add("id").add("selfRef");
		Fetch fetch = AttributeFilterToFetchConverter.convert(filter, selfRefEntityMetaData, "en");
		assertEquals(fetch, new Fetch().field("id").field("selfRef", new Fetch().field("id").field("label")));
	}

	@Test
	/**
	 * A Fetch for attrs=~id,selfRef(*) should fetch id and selfRef(id, label, selfRef(id, label))
	 */ public void testConvertNestedSelfRef()
	{
		AttributeFilter filter = new AttributeFilter().setIncludeIdAttr(true)
				.add("selfRef", new AttributeFilter().setIncludeAllAttrs(true));
		Fetch fetch = AttributeFilterToFetchConverter.convert(filter, selfRefEntityMetaData, "en");
		assertEquals(fetch, new Fetch().field("id").field("selfRef",
				new Fetch().field("id").field("label").field("selfRef", new Fetch().field("id").field("label"))));
	}

	@Test
	/**
	 * An AttributeFilter for ~id,~lbl,selfRef(*,selfRef(*)) should fetch (id, label, selfRef(id, label, selfRef(id,
	 * label)))
	 */ public void testConvertDoubleNestedSelfRef()
	{
		AttributeFilter filter = new AttributeFilter().setIncludeIdAttr(true).setIncludeLabelAttr(true).add("selfRef",
				new AttributeFilter().setIncludeAllAttrs(true)
						.add("selfRef", new AttributeFilter().setIncludeAllAttrs(true)));
		Fetch fetch = AttributeFilterToFetchConverter.convert(filter, selfRefEntityMetaData, "en");
		assertEquals(fetch, new Fetch().field("id").field("label").field("selfRef",
				new Fetch().field("id").field("label").field("selfRef", new Fetch().field("id").field("label")
						.field("selfRef", new Fetch().field("id").field("label")))));
	}

	@BeforeClass
	@Override
	public void bootstrap()
	{
		// bootstrap meta data
		EntityMetaDataMetaData entityMetaMeta = applicationContext.getBean(EntityMetaDataMetaData.class);
		applicationContext.getBean(AttributeMetaDataMetaData.class).bootstrap(entityMetaMeta);
		applicationContext.getBean(OwnedEntityMetaData.class).bootstrap(entityMetaMeta);
		Map<String, SystemEntityMetaData> systemEntityMetaMap = applicationContext
				.getBeansOfType(SystemEntityMetaData.class);
		systemEntityMetaMap.values().forEach(systemEntityMetaData -> systemEntityMetaData.bootstrap(entityMetaMeta));
	}

	@Configuration
	@Import({ FileMetaMetaData.class, OwnedEntityMetaData.class, SecurityPackage.class, RootSystemPackage.class })
	public static class Config
	{

	}
}
