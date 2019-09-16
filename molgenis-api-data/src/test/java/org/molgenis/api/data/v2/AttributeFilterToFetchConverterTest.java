package org.molgenis.api.data.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.molgenis.api.data.v2.AttributeFilterToFetchConverter.convert;
import static org.molgenis.api.data.v2.AttributeFilterToFetchConverter.createDefaultAttributeFetch;
import static org.molgenis.api.data.v2.AttributeFilterToFetchConverter.createDefaultEntityFetch;
import static org.molgenis.data.file.model.FileMetaMetadata.FILENAME;
import static org.molgenis.data.file.model.FileMetaMetadata.ID;
import static org.molgenis.data.file.model.FileMetaMetadata.URL;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.meta.AttributeType.FILE;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Fetch;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.file.model.FileMetaMetadata;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.security.auth.SecurityPackage;
import org.molgenis.data.system.model.RootSystemPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {AttributeFilterToFetchConverterTest.Config.class})
class AttributeFilterToFetchConverterTest extends AbstractMolgenisSpringTest {
  private static final String ID_ATTR_NAME = "attrId";
  private static final String LABEL_ATTR_NAME = "attrLabel";
  private static final String COMPOUND_ATTR_NAME = "attrCompound";
  private static final String COMPOUND_PART_ATTR_NAME = "attrCompoundPart";
  private static final String COMPOUND_PART_FILE_ATTR_NAME = "attrCompoundPartFile";
  private static final String COMPOUND_PART_COMPOUND_ATTR_NAME = "attrCompoundPartCompound";
  private static final String COMPOUND_PART_COMPOUND_PART_ATTR_NAME =
      "attrCompoundPartCompoundPart";
  private static final String COMPOUND_PART_COMPOUND_PART_ATTR2_NAME =
      "attr2CompoundPartCompoundPart";
  private static final String XREF_ATTR_NAME = "xrefAttr";

  private static final String REF_ID_ATTR_NAME = "refAttrId";
  private static final String REF_LABEL_ATTR_NAME = "refAttrLabel";
  private static final String REF_ATTR_NAME = "refAttr";

  /**
   *
   *
   * <ul>
   *   <li>entity
   *       <ul>
   *         <li>attrId
   *         <li>attrLabel
   *         <li>attrCompound
   *             <ul>
   *               <li>attrCompoundPart
   *               <li>attrCompoundPartFile
   *               <li>attrCompoundPartCompound
   *                   <ul>
   *                     <li>attrCompoundPartCompoundPart
   *                     <li>attr2CompoundPartCompoundPart
   *                   </ul>
   *             </ul>
   *       </ul>
   */
  private EntityType entityType;

  private Attribute labelAttr;
  private Attribute xrefAttr;
  private EntityType xrefEntityType;
  private EntityType selfRefEntityType;

  @Autowired private EntityTypeFactory entityTypeFactory;
  @Autowired private AttributeFactory attributeFactory;
  @Autowired private FileMetaMetadata fileMetaMeta;

  @BeforeEach
  void setUpBeforeMethod() {
    selfRefEntityType = entityTypeFactory.create("SelfRefEntity");
    Attribute selfRefIdAttr = attributeFactory.create().setName("id");
    selfRefEntityType
        .addAttribute(selfRefIdAttr, ROLE_ID)
        .addAttribute(attributeFactory.create().setName("label"), ROLE_LABEL)
        .addAttribute(
            attributeFactory
                .create()
                .setName("selfRef")
                .setDataType(XREF)
                .setRefEntity(selfRefEntityType));

    labelAttr = attributeFactory.create().setName(REF_LABEL_ATTR_NAME);
    xrefEntityType =
        entityTypeFactory
            .create("xrefEntity")
            .addAttribute(attributeFactory.create().setName(REF_ID_ATTR_NAME), ROLE_ID)
            .addAttribute(labelAttr, ROLE_LABEL)
            .addAttribute(attributeFactory.create().setName(REF_ATTR_NAME));

    entityType =
        entityTypeFactory
            .create("entity")
            .addAttribute(attributeFactory.create().setName(ID_ATTR_NAME), ROLE_ID)
            .addAttribute(attributeFactory.create().setName(LABEL_ATTR_NAME), ROLE_LABEL);

    Attribute compoundAttr =
        attributeFactory.create().setName(COMPOUND_ATTR_NAME).setDataType(COMPOUND);
    Attribute compoundPartAttr =
        attributeFactory
            .create()
            .setName(COMPOUND_PART_ATTR_NAME)
            .setDataType(COMPOUND)
            .setParent(compoundAttr);
    Attribute compoundPartFileAttr =
        attributeFactory
            .create()
            .setName(COMPOUND_PART_FILE_ATTR_NAME)
            .setParent(compoundAttr)
            .setDataType(FILE)
            .setRefEntity(fileMetaMeta);
    Attribute compoundPartCompoundAttr =
        attributeFactory
            .create()
            .setName(COMPOUND_PART_COMPOUND_ATTR_NAME)
            .setDataType(COMPOUND)
            .setParent(compoundAttr);

    Attribute compoundPartCompoundPartAttr =
        attributeFactory
            .create()
            .setName(COMPOUND_PART_COMPOUND_PART_ATTR_NAME)
            .setParent(compoundPartCompoundAttr);
    Attribute compoundPartCompoundPartAttr2 =
        attributeFactory
            .create()
            .setName(COMPOUND_PART_COMPOUND_PART_ATTR2_NAME)
            .setParent(compoundPartCompoundAttr);
    entityType.addAttribute(compoundAttr);
    entityType.addAttribute(compoundPartAttr);
    entityType.addAttribute(compoundPartFileAttr);
    entityType.addAttribute(compoundPartCompoundPartAttr);
    entityType.addAttribute(compoundPartCompoundPartAttr2);

    xrefAttr =
        attributeFactory
            .create()
            .setName(XREF_ATTR_NAME)
            .setDataType(XREF)
            .setRefEntity(xrefEntityType);
    entityType.addAttribute(xrefAttr);
  }

  @Test
  void convertNoAttrFilter() {
    Fetch fetch =
        new Fetch()
            .field(ID_ATTR_NAME)
            .field(LABEL_ATTR_NAME)
            .field(
                COMPOUND_PART_FILE_ATTR_NAME,
                new Fetch()
                    .field(FileMetaMetadata.ID)
                    .field(FileMetaMetadata.FILENAME)
                    .field(FileMetaMetadata.URL))
            .field(XREF_ATTR_NAME, new Fetch().field(REF_ID_ATTR_NAME).field(REF_LABEL_ATTR_NAME))
            .field(COMPOUND_PART_COMPOUND_PART_ATTR_NAME)
            .field(COMPOUND_PART_COMPOUND_PART_ATTR2_NAME);
    assertEquals(fetch, convert(null, entityType, "en"));
  }

  @Test
  void convertAttrFilterIncludeAll() {
    AttributeFilter attrFilter = new AttributeFilter().setIncludeAllAttrs(true);
    assertEquals(
        new Fetch()
            .field("attrId")
            .field("attrLabel")
            .field("attrCompoundPartFile", new Fetch().field("id").field("filename").field("url"))
            .field("attrCompoundPartCompoundPart")
            .field("attr2CompoundPartCompoundPart")
            .field("xrefAttr", new Fetch().field("refAttrId").field("refAttrLabel")),
        convert(attrFilter, entityType, "en"));
  }

  @Test
  void convertAttrFilterIncludeIdAndLabelAttrs() {
    AttributeFilter attrFilter =
        new AttributeFilter().setIncludeIdAttr(true).setIncludeLabelAttr(true);
    assertEquals(
        new Fetch().field(ID_ATTR_NAME).field(LABEL_ATTR_NAME),
        convert(attrFilter, entityType, "en"));
  }

  @Test
  void convertAttrFilterIncludeAttr() {
    AttributeFilter attrFilter = new AttributeFilter().add(LABEL_ATTR_NAME);
    assertEquals(new Fetch().field(LABEL_ATTR_NAME), convert(attrFilter, entityType, "en"));
  }

  @Test
  void convertAttrFilterCompoundAttr() {
    AttributeFilter attrFilter = new AttributeFilter().add(COMPOUND_ATTR_NAME);
    assertEquals(
        new Fetch()
            .field(COMPOUND_PART_FILE_ATTR_NAME, new Fetch().field(ID).field(FILENAME).field(URL))
            .field(COMPOUND_PART_COMPOUND_PART_ATTR_NAME)
            .field(COMPOUND_PART_COMPOUND_PART_ATTR2_NAME),
        convert(attrFilter, entityType, "en"));
  }

  @Test
  void convertAttrFilterCompoundAttrPart() {
    AttributeFilter attrFilter =
        new AttributeFilter()
            .add(COMPOUND_ATTR_NAME, new AttributeFilter().add(COMPOUND_PART_COMPOUND_ATTR_NAME));
    assertEquals(
        new Fetch()
            .field(COMPOUND_PART_COMPOUND_PART_ATTR_NAME)
            .field(COMPOUND_PART_COMPOUND_PART_ATTR2_NAME),
        convert(attrFilter, entityType, "en"));
  }

  @Test
  void convertAttrFilterCompoundPartCompoundAttr() {
    AttributeFilter attrFilter =
        new AttributeFilter()
            .add(
                COMPOUND_ATTR_NAME,
                new AttributeFilter()
                    .add(
                        COMPOUND_PART_COMPOUND_ATTR_NAME,
                        new AttributeFilter().add(COMPOUND_PART_COMPOUND_PART_ATTR_NAME)));
    assertEquals(
        new Fetch().field(COMPOUND_PART_COMPOUND_PART_ATTR_NAME),
        convert(attrFilter, entityType, "en"));
  }

  @Test
  void convertAttrFilterXrefAttr() {
    AttributeFilter attrFilter =
        new AttributeFilter().add(XREF_ATTR_NAME, new AttributeFilter().add(REF_ATTR_NAME));
    assertEquals(
        new Fetch().field(XREF_ATTR_NAME, new Fetch().field(REF_ATTR_NAME)),
        convert(attrFilter, entityType, "en"));
  }

  @Test
  void convertAttrFilterUnknownAttr() {
    AttributeFilter attrFilter = new AttributeFilter().add("unknown");
    assertThrows(
        UnknownAttributeException.class,
        () -> AttributeFilterToFetchConverter.convert(attrFilter, entityType, "en"));
  }

  @Test
  void createDefaultEntityFetchRefs() {
    Fetch fetch =
        new Fetch()
            .field(ID_ATTR_NAME)
            .field(LABEL_ATTR_NAME)
            .field(
                COMPOUND_PART_FILE_ATTR_NAME,
                new Fetch()
                    .field(FileMetaMetadata.ID)
                    .field(FileMetaMetadata.FILENAME)
                    .field(FileMetaMetadata.URL))
            .field(XREF_ATTR_NAME, new Fetch().field(REF_ID_ATTR_NAME).field(REF_LABEL_ATTR_NAME))
            .field(COMPOUND_PART_COMPOUND_PART_ATTR_NAME)
            .field(COMPOUND_PART_COMPOUND_PART_ATTR2_NAME);
    assertEquals(fetch, createDefaultEntityFetch(entityType, "en"));
  }

  @Test
  void createDefaultEntityFetchNoRefs() {
    assertNull(AttributeFilterToFetchConverter.createDefaultEntityFetch(xrefEntityType, "en"));
  }

  @Test
  void createDefaultEntityFetchRefAttr() {
    Fetch fetch = new Fetch().field(REF_ID_ATTR_NAME).field(REF_LABEL_ATTR_NAME);
    assertEquals(fetch, createDefaultAttributeFetch(xrefAttr, "en"));
  }

  @Test
  void createDefaultAttributeFetchNoRefAttr() {
    assertNull(AttributeFilterToFetchConverter.createDefaultAttributeFetch(labelAttr, "en"));
  }

  /** attrs=~id,selfRef should fetch id, and selfRef(id, label) */
  @Test
  void testConvertSelfRefIncludeId() {
    AttributeFilter filter = new AttributeFilter().setIncludeIdAttr(true).add("selfRef");
    Fetch fetch = AttributeFilterToFetchConverter.convert(filter, selfRefEntityType, "en");
    assertEquals(
        new Fetch().field("id").field("selfRef", new Fetch().field("id").field("label")), fetch);
  }

  /** attrs=id,selfRef should fetch id, and selfRef(id, label) */
  @Test
  void testConvertIdSelfRef() {
    AttributeFilter filter = new AttributeFilter().add("id").add("selfRef");
    Fetch fetch = AttributeFilterToFetchConverter.convert(filter, selfRefEntityType, "en");
    assertEquals(
        new Fetch().field("id").field("selfRef", new Fetch().field("id").field("label")), fetch);
  }

  /** A Fetch for attrs=~id,selfRef(*) should fetch id and selfRef(id, label, selfRef(id, label)) */
  @Test
  void testConvertNestedSelfRef() {
    AttributeFilter filter =
        new AttributeFilter()
            .setIncludeIdAttr(true)
            .add("selfRef", new AttributeFilter().setIncludeAllAttrs(true));
    Fetch fetch = AttributeFilterToFetchConverter.convert(filter, selfRefEntityType, "en");
    assertEquals(
        new Fetch()
            .field("id")
            .field(
                "selfRef",
                new Fetch()
                    .field("id")
                    .field("label")
                    .field("selfRef", new Fetch().field("id").field("label"))),
        fetch);
  }

  /**
   * An AttributeFilter for ~id,~lbl,selfRef(*,selfRef(*)) should fetch (id, label, selfRef(id,
   * label, selfRef(id, label)))
   */
  @Test
  void testConvertDoubleNestedSelfRef() {
    AttributeFilter filter =
        new AttributeFilter()
            .setIncludeIdAttr(true)
            .setIncludeLabelAttr(true)
            .add(
                "selfRef",
                new AttributeFilter()
                    .setIncludeAllAttrs(true)
                    .add("selfRef", new AttributeFilter().setIncludeAllAttrs(true)));
    Fetch fetch = AttributeFilterToFetchConverter.convert(filter, selfRefEntityType, "en");
    assertEquals(
        new Fetch()
            .field("id")
            .field("label")
            .field(
                "selfRef",
                new Fetch()
                    .field("id")
                    .field("label")
                    .field(
                        "selfRef",
                        new Fetch()
                            .field("id")
                            .field("label")
                            .field("selfRef", new Fetch().field("id").field("label")))),
        fetch);
  }

  @Configuration
  @Import({FileMetaMetadata.class, SecurityPackage.class, RootSystemPackage.class})
  static class Config {}
}
