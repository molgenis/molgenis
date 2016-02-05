package org.molgenis.data.rest.v2;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.COMPOUND;
import static org.molgenis.MolgenisFieldTypes.FILE;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.file.FileMeta;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AttributeFilterToFetchConverterTest
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
	private DefaultEntityMetaData selfRefEntityMetaData;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		selfRefEntityMetaData = new DefaultEntityMetaData("SelfRefEntity");
		selfRefEntityMetaData.addAttribute("id", ROLE_ID);
		selfRefEntityMetaData.addAttribute("label", ROLE_LABEL);
		selfRefEntityMetaData.addAttribute("selfRef").setDataType(XREF).setRefEntity(selfRefEntityMetaData);

		xrefEntityMeta = mock(EntityMetaData.class);
		when(xrefEntityMeta.getName()).thenReturn("xrefEntity");
		AttributeMetaData refAttr0 = when(mock(AttributeMetaData.class).getName()).thenReturn(REF_ID_ATTR_NAME)
				.getMock();
		when(refAttr0.getDataType()).thenReturn(STRING);
		AttributeMetaData refAttr1 = when(mock(AttributeMetaData.class).getName()).thenReturn(REF_LABEL_ATTR_NAME)
				.getMock();
		when(refAttr1.getDataType()).thenReturn(STRING);
		AttributeMetaData refAttr2 = when(mock(AttributeMetaData.class).getName()).thenReturn(REF_ATTR_NAME).getMock();
		when(refAttr2.getDataType()).thenReturn(STRING);
		when(xrefEntityMeta.getAttribute(REF_ID_ATTR_NAME.toLowerCase())).thenReturn(refAttr0);
		when(xrefEntityMeta.getAttribute(REF_ID_ATTR_NAME)).thenReturn(refAttr0);
		when(xrefEntityMeta.getAttribute(REF_LABEL_ATTR_NAME.toLowerCase())).thenReturn(refAttr1);
		when(xrefEntityMeta.getAttribute(REF_LABEL_ATTR_NAME)).thenReturn(refAttr1);
		when(xrefEntityMeta.getAttribute(REF_ATTR_NAME.toLowerCase())).thenReturn(refAttr2);
		when(xrefEntityMeta.getAttribute(REF_ATTR_NAME)).thenReturn(refAttr2);
		when(xrefEntityMeta.getAttributes()).thenReturn(Arrays.asList(refAttr0, refAttr1, refAttr2));
		when(xrefEntityMeta.getAtomicAttributes()).thenReturn(Arrays.asList(refAttr0, refAttr1, refAttr2));
		when(xrefEntityMeta.getIdAttribute()).thenReturn(refAttr0);
		when(xrefEntityMeta.getLabelAttribute("en")).thenReturn(refAttr1);

		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(ID_ATTR_NAME).getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		labelAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(LABEL_ATTR_NAME).getMock();
		when(labelAttr.getDataType()).thenReturn(STRING);
		AttributeMetaData compoundPartAttr = when(mock(AttributeMetaData.class).getName())
				.thenReturn(COMPOUND_PART_ATTR_NAME).getMock();
		when(compoundPartAttr.getDataType()).thenReturn(STRING);
		AttributeMetaData compoundPartFileAttr = when(mock(AttributeMetaData.class).getName())
				.thenReturn(COMPOUND_PART_FILE_ATTR_NAME).getMock();
		when(compoundPartFileAttr.getDataType()).thenReturn(FILE);
		when(compoundPartFileAttr.getRefEntity()).thenReturn(FileMeta.META_DATA);
		AttributeMetaData compoundAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(COMPOUND_ATTR_NAME)
				.getMock();
		AttributeMetaData compoundPartCompoundAttr = when(mock(AttributeMetaData.class).getName())
				.thenReturn(COMPOUND_PART_COMPOUND_ATTR_NAME).getMock();
		when(compoundPartCompoundAttr.getDataType()).thenReturn(COMPOUND);

		AttributeMetaData compoundPartCompoundPartAttr = when(mock(AttributeMetaData.class).getName())
				.thenReturn(COMPOUND_PART_COMPOUND_PART_ATTR_NAME).getMock();
		when(compoundPartCompoundPartAttr.getDataType()).thenReturn(STRING);

		AttributeMetaData compoundPartCompoundPartAttr2 = when(mock(AttributeMetaData.class).getName())
				.thenReturn(COMPOUND_PART_COMPOUND_PART_ATTR2_NAME).getMock();
		when(compoundPartCompoundPartAttr2.getDataType()).thenReturn(STRING);

		when(compoundPartCompoundAttr.getAttributeParts())
				.thenReturn(Arrays.asList(compoundPartCompoundPartAttr, compoundPartCompoundPartAttr2));
		when(compoundPartCompoundAttr.getAttributePart(COMPOUND_PART_COMPOUND_PART_ATTR_NAME))
				.thenReturn(compoundPartCompoundPartAttr);
		when(compoundPartCompoundAttr.getAttributePart(COMPOUND_PART_COMPOUND_PART_ATTR_NAME.toLowerCase()))
				.thenReturn(compoundPartCompoundPartAttr);

		when(compoundPartCompoundAttr.getAttributePart(COMPOUND_PART_COMPOUND_PART_ATTR2_NAME))
				.thenReturn(compoundPartCompoundPartAttr2);
		when(compoundPartCompoundAttr.getAttributePart(COMPOUND_PART_COMPOUND_PART_ATTR2_NAME.toLowerCase()))
				.thenReturn(compoundPartCompoundPartAttr2);

		when(compoundAttr.getDataType()).thenReturn(COMPOUND);
		when(compoundAttr.getAttributeParts())
				.thenReturn(Arrays.asList(compoundPartAttr, compoundPartFileAttr, compoundPartCompoundAttr));
		when(compoundAttr.getAttributePart(COMPOUND_PART_ATTR_NAME.toLowerCase())).thenReturn(compoundPartAttr);
		when(compoundAttr.getAttributePart(COMPOUND_PART_ATTR_NAME)).thenReturn(compoundPartAttr);
		when(compoundAttr.getAttributePart(COMPOUND_PART_FILE_ATTR_NAME.toLowerCase()))
				.thenReturn(compoundPartFileAttr);
		when(compoundAttr.getAttributePart(COMPOUND_PART_FILE_ATTR_NAME)).thenReturn(compoundPartFileAttr);
		when(compoundAttr.getAttributePart(COMPOUND_PART_COMPOUND_ATTR_NAME.toLowerCase()))
				.thenReturn(compoundPartCompoundAttr);
		when(compoundAttr.getAttributePart(COMPOUND_PART_COMPOUND_ATTR_NAME)).thenReturn(compoundPartCompoundAttr);

		xrefAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(XREF_ATTR_NAME).getMock();
		when(xrefAttr.getDataType()).thenReturn(XREF);
		when(xrefAttr.getRefEntity()).thenReturn(xrefEntityMeta);

		entityMeta = mock(EntityMetaData.class);
		when(entityMeta.getName()).thenReturn("entity");
		when(entityMeta.getAttribute(ID_ATTR_NAME.toLowerCase())).thenReturn(idAttr);
		when(entityMeta.getAttribute(ID_ATTR_NAME)).thenReturn(idAttr);
		when(entityMeta.getAttribute(LABEL_ATTR_NAME.toLowerCase())).thenReturn(labelAttr);
		when(entityMeta.getAttribute(LABEL_ATTR_NAME)).thenReturn(labelAttr);
		when(entityMeta.getAttribute(COMPOUND_ATTR_NAME.toLowerCase())).thenReturn(compoundAttr);
		when(entityMeta.getAttribute(COMPOUND_ATTR_NAME)).thenReturn(compoundAttr);
		when(entityMeta.getAttribute(COMPOUND_PART_ATTR_NAME.toLowerCase())).thenReturn(compoundPartAttr);
		when(entityMeta.getAttribute(COMPOUND_PART_ATTR_NAME)).thenReturn(compoundPartAttr);
		when(entityMeta.getAttribute(XREF_ATTR_NAME.toLowerCase())).thenReturn(xrefAttr);
		when(entityMeta.getAttribute(XREF_ATTR_NAME)).thenReturn(xrefAttr);
		when(entityMeta.getAttribute(COMPOUND_PART_COMPOUND_ATTR_NAME)).thenReturn(compoundPartCompoundAttr);
		when(entityMeta.getAttribute(COMPOUND_PART_COMPOUND_ATTR_NAME.toLowerCase()))
				.thenReturn(compoundPartCompoundAttr);
		when(entityMeta.getAttribute(COMPOUND_PART_COMPOUND_PART_ATTR_NAME)).thenReturn(compoundPartCompoundPartAttr);
		when(entityMeta.getAttribute(COMPOUND_PART_COMPOUND_PART_ATTR_NAME.toLowerCase()))
				.thenReturn(compoundPartCompoundPartAttr);
		when(entityMeta.getAttribute(COMPOUND_PART_COMPOUND_PART_ATTR2_NAME)).thenReturn(compoundPartCompoundPartAttr2);
		when(entityMeta.getAttribute(COMPOUND_PART_COMPOUND_PART_ATTR2_NAME.toLowerCase()))
				.thenReturn(compoundPartCompoundPartAttr2);
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		when(entityMeta.getLabelAttribute("en")).thenReturn(labelAttr);
		when(entityMeta.getAttributes()).thenReturn(Arrays.asList(idAttr, labelAttr, compoundAttr, xrefAttr));
		when(entityMeta.getAtomicAttributes()).thenReturn(Arrays.asList(idAttr, labelAttr, compoundPartAttr,
				compoundPartFileAttr, compoundPartCompoundPartAttr, compoundPartCompoundPartAttr2, xrefAttr));
	}

	@Test
	public void convertNoAttrFilter()
	{
		Fetch fetch = new Fetch().field(ID_ATTR_NAME).field(LABEL_ATTR_NAME).field(COMPOUND_PART_ATTR_NAME)
				.field(COMPOUND_PART_FILE_ATTR_NAME,
						new Fetch().field(FileMeta.ID).field(FileMeta.FILENAME).field(FileMeta.URL))
				.field(XREF_ATTR_NAME, new Fetch().field(REF_ID_ATTR_NAME).field(REF_LABEL_ATTR_NAME))
				.field(COMPOUND_PART_COMPOUND_PART_ATTR_NAME).field(COMPOUND_PART_COMPOUND_PART_ATTR2_NAME);
		assertEquals(AttributeFilterToFetchConverter.convert(null, entityMeta, "en"), fetch);
	}

	@Test
	public void convertAttrFilterIncludeAll()
	{
		AttributeFilter attrFilter = new AttributeFilter().setIncludeAllAttrs(true);
		assertEquals(AttributeFilterToFetchConverter.convert(attrFilter, entityMeta, "en"),
				new Fetch().field("attrId").field("attrLabel").field("attrCompoundPart")
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
		assertEquals(AttributeFilterToFetchConverter.convert(attrFilter, entityMeta, "en"),
				new Fetch().field(COMPOUND_PART_ATTR_NAME)
						.field(COMPOUND_PART_FILE_ATTR_NAME,
								new Fetch().field(FileMeta.META_DATA.getIdAttribute().getName())
										.field(FileMeta.META_DATA.getLabelAttribute().getName()).field(FileMeta.URL))
						.field(COMPOUND_PART_COMPOUND_PART_ATTR_NAME).field(COMPOUND_PART_COMPOUND_PART_ATTR2_NAME));
	}

	@Test
	public void convertAttrFilterCompoundAttrPart()
	{
		AttributeFilter attrFilter = new AttributeFilter().add(COMPOUND_ATTR_NAME,
				new AttributeFilter().add(COMPOUND_PART_ATTR_NAME));
		assertEquals(AttributeFilterToFetchConverter.convert(attrFilter, entityMeta, "en"),
				new Fetch().field(COMPOUND_PART_ATTR_NAME));
	}

	@Test
	public void convertAttrFilterCompoundPartCompoundAttr()
	{
		AttributeFilter attrFilter = new AttributeFilter().add(COMPOUND_ATTR_NAME, new AttributeFilter().add(
				COMPOUND_PART_COMPOUND_ATTR_NAME, new AttributeFilter().add(COMPOUND_PART_COMPOUND_PART_ATTR_NAME)));
		assertEquals(AttributeFilterToFetchConverter.convert(attrFilter, entityMeta, "en"),
				new Fetch().field(COMPOUND_PART_COMPOUND_PART_ATTR_NAME));
	}

	@Test
	public void convertAttrFilterXrefAttr()
	{
		AttributeFilter attrFilter = new AttributeFilter().add(XREF_ATTR_NAME,
				new AttributeFilter().add(REF_ATTR_NAME));
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
		Fetch fetch = new Fetch().field(ID_ATTR_NAME).field(LABEL_ATTR_NAME).field(COMPOUND_PART_ATTR_NAME)
				.field(COMPOUND_PART_FILE_ATTR_NAME,
						new Fetch().field(FileMeta.ID).field(FileMeta.FILENAME).field(FileMeta.URL))
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
	 */
	public void testConvertSelfRefIncludeId()
	{
		AttributeFilter filter = new AttributeFilter().setIncludeIdAttr(true).add("selfRef");
		Fetch fetch = AttributeFilterToFetchConverter.convert(filter, selfRefEntityMetaData, "en");
		assertEquals(fetch, new Fetch().field("id").field("selfRef", new Fetch().field("id").field("label")));
	}

	@Test
	/**
	 * attrs=id,selfRef should fetch id, and selfRef(id, label)
	 */
	public void testConvertIdSelfRef()
	{
		AttributeFilter filter = new AttributeFilter().add("id").add("selfRef");
		Fetch fetch = AttributeFilterToFetchConverter.convert(filter, selfRefEntityMetaData, "en");
		assertEquals(fetch, new Fetch().field("id").field("selfRef", new Fetch().field("id").field("label")));
	}

	@Test
	/**
	 * A Fetch for attrs=~id,selfRef(*) should fetch id and selfRef(id, label, selfRef(id, label))
	 */
	public void testConvertNestedSelfRef()
	{
		AttributeFilter filter = new AttributeFilter().setIncludeIdAttr(true).add("selfRef",
				new AttributeFilter().setIncludeAllAttrs(true));
		Fetch fetch = AttributeFilterToFetchConverter.convert(filter, selfRefEntityMetaData, "en");
		assertEquals(fetch, new Fetch().field("id").field("selfRef",
				new Fetch().field("id").field("label").field("selfRef", new Fetch().field("id").field("label"))));
	}

	@Test
	/**
	 * An AttributeFilter for ~id,~lbl,selfRef(*,selfRef(*)) should fetch (id, label, selfRef(id, label, selfRef(id,
	 * label)))
	 */
	public void testConvertDoubleNestedSelfRef()
	{
		AttributeFilter filter = new AttributeFilter().setIncludeIdAttr(true).setIncludeLabelAttr(true).add("selfRef",
				new AttributeFilter().setIncludeAllAttrs(true).add("selfRef",
						new AttributeFilter().setIncludeAllAttrs(true)));
		Fetch fetch = AttributeFilterToFetchConverter.convert(filter, selfRefEntityMetaData, "en");
		assertEquals(fetch,
				new Fetch().field("id").field("label").field("selfRef",
						new Fetch().field("id").field("label").field("selfRef", new Fetch().field("id").field("label")
								.field("selfRef", new Fetch().field("id").field("label")))));
	}
}
