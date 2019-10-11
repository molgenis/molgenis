package org.molgenis.data.elasticsearch.generator;

import static java.util.Arrays.asList;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.constantScoreQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchPhrasePrefixQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.nestedQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.elasticsearch.FieldConstants.DEFAULT_ANALYZER;
import static org.molgenis.data.elasticsearch.FieldConstants.FIELD_NOT_ANALYZED;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.meta.AttributeType.DATE;
import static org.molgenis.data.meta.AttributeType.DATE_TIME;
import static org.molgenis.data.meta.AttributeType.DECIMAL;
import static org.molgenis.data.meta.AttributeType.EMAIL;
import static org.molgenis.data.meta.AttributeType.ENUM;
import static org.molgenis.data.meta.AttributeType.HTML;
import static org.molgenis.data.meta.AttributeType.HYPERLINK;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.LONG;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.SCRIPT;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataConverter;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisQueryException;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;

// FIXME add nillable tests
@MockitoSettings(strictness = Strictness.LENIENT)
class QueryGeneratorTest extends AbstractMolgenisSpringTest {
  private static final String idAttrName = "xid";

  private static final String boolAttrName = "xbool";
  private static final String categoricalAttrName = "xcategorical";
  private static final String compoundAttrName = "xcompound";
  private static final String compoundPart0AttrName = "xcompoundpart0";
  private static final String compoundPart1AttrName = "xcompoundpart1";
  private static final String dateAttrName = "xdate";
  private static final String dateTimeAttrName = "xdatetime";
  private static final String decimalAttrName = "xdecimal";
  private static final String emailAttrName = "xemail";
  private static final String enumAttrName = "xenum";
  private static final String htmlAttrName = "xhtml";
  private static final String hyperlinkAttrName = "xhyperlink";
  private static final String intAttrName = "xint";
  private static final String longAttrName = "xlong";
  private static final String mrefAttrName = "xmref";
  private static final String scriptAttrName = "xscript";
  private static final String stringAttrName = "xstring";
  private static final String textAttrName = "xtext";
  private static final String xrefAttrName = "xxref";
  private static final String refStringAttrName = "ref_xstring";
  private static final String refMrefAttrName = "ref_xmref";

  private EntityType entityType;
  private EntityType refEntityType;

  @Autowired EntityTypeFactory entityTypeFactory;

  @Autowired AttributeFactory attrFactory;

  private QueryGenerator queryGenerator;

  @BeforeEach
  void setUp() {
    refEntityType = entityTypeFactory.create("ref_entity");
    refEntityType.addAttribute(attrFactory.create().setName(idAttrName), ROLE_ID);
    refEntityType.addAttribute(
        attrFactory.create().setName(refStringAttrName).setUnique(true), ROLE_LABEL);
    refEntityType.addAttribute(
        attrFactory
            .create()
            .setName(refMrefAttrName)
            .setDataType(MREF)
            .setNillable(true)
            .setRefEntity(refEntityType));

    entityType = entityTypeFactory.create("entity");
    entityType.addAttribute(attrFactory.create().setName(idAttrName), ROLE_ID);
    entityType.addAttribute(attrFactory.create().setName(boolAttrName).setDataType(BOOL));
    entityType.addAttribute(
        attrFactory
            .create()
            .setName(categoricalAttrName)
            .setDataType(CATEGORICAL)
            .setRefEntity(refEntityType));
    Attribute compoundAttr = attrFactory.create().setName(compoundAttrName).setDataType(COMPOUND);
    Attribute compoundPart0Attr =
        attrFactory
            .create()
            .setName(compoundPart0AttrName)
            .setDataType(STRING)
            .setParent(compoundAttr);
    Attribute compoundPart1Attr =
        attrFactory
            .create()
            .setName(compoundPart1AttrName)
            .setDataType(STRING)
            .setParent(compoundAttr);
    entityType
        .addAttribute(compoundAttr)
        .addAttribute(compoundPart0Attr)
        .addAttribute(compoundPart1Attr);
    entityType.addAttribute(attrFactory.create().setName(dateAttrName).setDataType(DATE));
    entityType.addAttribute(attrFactory.create().setName(dateTimeAttrName).setDataType(DATE_TIME));
    entityType.addAttribute(attrFactory.create().setName(decimalAttrName).setDataType(DECIMAL));
    entityType.addAttribute(attrFactory.create().setName(emailAttrName).setDataType(EMAIL));
    entityType.addAttribute(
        attrFactory
            .create()
            .setName(enumAttrName)
            .setDataType(ENUM)
            .setEnumOptions(asList("enum0", "enum1", "enum2")));
    entityType.addAttribute(attrFactory.create().setName(htmlAttrName).setDataType(HTML));
    entityType.addAttribute(attrFactory.create().setName(hyperlinkAttrName).setDataType(HYPERLINK));
    entityType.addAttribute(attrFactory.create().setName(intAttrName).setDataType(INT));
    entityType.addAttribute(attrFactory.create().setName(longAttrName).setDataType(LONG));
    entityType.addAttribute(
        attrFactory.create().setName(mrefAttrName).setDataType(MREF).setRefEntity(refEntityType));
    entityType.addAttribute(attrFactory.create().setName(scriptAttrName).setDataType(SCRIPT));
    entityType.addAttribute(attrFactory.create().setName(stringAttrName).setDataType(STRING));
    entityType.addAttribute(attrFactory.create().setName(textAttrName).setDataType(TEXT));
    entityType.addAttribute(
        attrFactory.create().setName(xrefAttrName).setDataType(XREF).setRefEntity(refEntityType));

    DocumentIdGenerator documentIdGenerator = mock(DocumentIdGenerator.class);
    when(documentIdGenerator.generateId(any(Attribute.class)))
        .thenAnswer(invocation -> ((Attribute) invocation.getArguments()[0]).getName());
    queryGenerator = new QueryGenerator(documentIdGenerator);
  }

  @Test
  void generateOneQueryRuleGreaterInvalidAttribute() {
    String value = "str";
    Query<Entity> q = new QueryImpl<>().gt(stringAttrName, value);
    assertThrows(
        MolgenisQueryException.class, () -> queryGenerator.createQueryBuilder(q, entityType));
  }

  @Test
  void generateOneQueryRuleGreaterDate() throws ParseException {
    String date = "2015-01-22";
    LocalDate value = LocalDate.parse(date);
    Query<Entity> q = new QueryImpl<>().gt(dateAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(rangeQuery(dateAttrName).gt(date));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleGreaterDateTime() throws ParseException {
    Instant value = Instant.parse("2015-05-22T06:12:13Z");
    Query<Entity> q = new QueryImpl<>().gt(dateTimeAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(rangeQuery(dateTimeAttrName).gt(DataConverter.toString(value)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleGreaterDecimal() {
    Double value = 1.23;
    Query<Entity> q = new QueryImpl<>().gt(decimalAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(rangeQuery(decimalAttrName).gt(value));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleGreaterInt() {
    Integer value = 1;
    Query<Entity> q = new QueryImpl<>().gt(intAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(rangeQuery(intAttrName).gt(value));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleGreaterLong() {
    Long value = 1L;
    Query<Entity> q = new QueryImpl<>().gt(longAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(rangeQuery(longAttrName).gt(value));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleGreaterEqualInvalidAttribute() {
    String value = "str";
    Query<Entity> q = new QueryImpl<>().ge(stringAttrName, value);
    assertThrows(
        MolgenisQueryException.class, () -> queryGenerator.createQueryBuilder(q, entityType));
  }

  @Test
  void generateOneQueryRuleGreaterEqualDate() throws ParseException {
    String date = "2015-05-22";
    LocalDate value = LocalDate.parse(date);
    Query<Entity> q = new QueryImpl<>().ge(dateAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(rangeQuery(dateAttrName).gte(date));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleGreaterEqualDateTime() throws ParseException {
    Instant value = Instant.parse("2015-05-22T06:12:13Z");
    Query<Entity> q = new QueryImpl<>().ge(dateTimeAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(rangeQuery(dateTimeAttrName).gte(DataConverter.toString(value)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleGreaterEqualDecimal() {
    Double value = 1.23;
    Query<Entity> q = new QueryImpl<>().ge(decimalAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(rangeQuery(decimalAttrName).gte(value));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleGreaterEqualInt() {
    Integer value = 1;
    Query<Entity> q = new QueryImpl<>().ge(intAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(rangeQuery(intAttrName).gte(value));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleGreaterEqualLong() {
    Long value = 1L;
    Query<Entity> q = new QueryImpl<>().ge(longAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(rangeQuery(longAttrName).gte(value));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleLesserEqualInvalidAttribute() {
    String value = "str";
    Query<Entity> q = new QueryImpl<>().le(stringAttrName, value);
    assertThrows(
        MolgenisQueryException.class, () -> queryGenerator.createQueryBuilder(q, entityType));
  }

  @Test
  void generateOneQueryRuleLesserEqualDate() throws ParseException {
    String date = "2015-05-22";
    LocalDate value = LocalDate.parse(date);
    Query<Entity> q = new QueryImpl<>().le(dateAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(rangeQuery(dateAttrName).lte(date));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleLesserEqualDateTime() throws ParseException {
    Instant value = Instant.parse("2015-05-22T06:12:13Z");
    Query<Entity> q = new QueryImpl<>().le(dateTimeAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(rangeQuery(dateTimeAttrName).lte(DataConverter.toString(value)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleLesserEqualDecimal() {
    Double value = 1.23;
    Query<Entity> q = new QueryImpl<>().le(decimalAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(rangeQuery(decimalAttrName).lte(value));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleLesserEqualInt() {
    Integer value = 1;
    Query<Entity> q = new QueryImpl<>().le(intAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(rangeQuery(intAttrName).lte(value));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleLesserEqualLong() {
    Long value = 1L;
    Query<Entity> q = new QueryImpl<>().le(longAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(rangeQuery(longAttrName).lte(value));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleLesserInvalidAttribute() {
    String value = "str";
    Query<Entity> q = new QueryImpl<>().lt(stringAttrName, value);
    assertThrows(
        MolgenisQueryException.class, () -> queryGenerator.createQueryBuilder(q, entityType));
  }

  @Test
  void generateOneQueryRuleInBool() {
    Iterable<Object> values = Arrays.asList(Boolean.TRUE, Boolean.FALSE);
    Query<Entity> q = new QueryImpl<>().in(boolAttrName, values);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(termsQuery(boolAttrName, Boolean.TRUE, Boolean.FALSE));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleInCategorical_Ids() {
    Iterable<String> values = asList("id0", "id1", "id2");
    Query<Entity> q = new QueryImpl<>().in(categoricalAttrName, values);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(
            nestedQuery(
                categoricalAttrName,
                termsQuery(
                    categoricalAttrName + '.' + idAttrName + '.' + FIELD_NOT_ANALYZED,
                    new Object[] {"id0", "id1", "id2"}),
                ScoreMode.Avg));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleInCategorical_Entities() {
    Entity ref0 = new DynamicEntity(refEntityType);
    ref0.set(idAttrName, "id0");
    Entity ref1 = new DynamicEntity(refEntityType);
    ref1.set(idAttrName, "id1");
    Entity ref2 = new DynamicEntity(refEntityType);
    ref2.set(idAttrName, "id2");

    Iterable<Object> values = Arrays.asList(ref0, ref1, ref2);
    Query<Entity> q = new QueryImpl<>().in(categoricalAttrName, values);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(
            nestedQuery(
                categoricalAttrName,
                termsQuery(
                    categoricalAttrName + '.' + idAttrName + '.' + FIELD_NOT_ANALYZED,
                    new Object[] {"id0", "id1", "id2"}),
                ScoreMode.Avg));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleInDate() throws ParseException {
    LocalDate date1 = LocalDate.parse("2015-05-22");
    LocalDate date2 = LocalDate.parse("2015-05-23");
    Iterable<Object> values = Arrays.asList(date1, date2);
    Query<Entity> q = new QueryImpl<>().in(dateAttrName, values);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(
            termsQuery(dateAttrName, new Object[] {date1.toString(), date2.toString()}));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleInDateTime() throws ParseException {
    Instant date1 = Instant.parse("2015-05-22T05:12:13Z");
    Instant date2 = Instant.parse("2015-05-23T06:12:13Z");
    Iterable<Object> values = Arrays.asList(date1, date2);
    Query<Entity> q = new QueryImpl<>().in(dateTimeAttrName, values);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(
            termsQuery(dateTimeAttrName, new Object[] {date1.toString(), date2.toString()}));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleInDecimal() {
    Double double1 = 1.23;
    Double double2 = 2.34;
    Iterable<Object> values = Arrays.asList(double1, double2);
    Query<Entity> q = new QueryImpl<>().in(decimalAttrName, values);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(termsQuery(decimalAttrName, new Object[] {double1, double2}));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleInEmail() {
    String value1 = "e@mail.com";
    String value2 = "em@ail.com";
    Iterable<Object> values = Arrays.asList(value1, value2);
    Query<Entity> q = new QueryImpl<>().in(emailAttrName, values);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(
            termsQuery(emailAttrName + '.' + FIELD_NOT_ANALYZED, new Object[] {value1, value2}));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleInEnum() {
    String value1 = "enum0";
    String value2 = "enum1";
    Iterable<Object> values = Arrays.asList(value1, value2);
    Query<Entity> q = new QueryImpl<>().in(enumAttrName, values);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(
            termsQuery(enumAttrName + '.' + FIELD_NOT_ANALYZED, new Object[] {value1, value2}));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleInHtml() {
    String value1 = "<h1>title</h1>";
    String value2 = "<h2>subtitle</h2>";
    Iterable<Object> values = Arrays.asList(value1, value2);
    Query<Entity> q = new QueryImpl<>().in(htmlAttrName, values);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(
            termsQuery(htmlAttrName + '.' + FIELD_NOT_ANALYZED, new Object[] {value1, value2}));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleInHyperlink() {
    String value1 = "http://www.site0.com/";
    String value2 = "http://www.site1.com/";
    Iterable<Object> values = Arrays.asList(value1, value2);
    Query<Entity> q = new QueryImpl<>().in(hyperlinkAttrName, values);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(
            termsQuery(
                hyperlinkAttrName + '.' + FIELD_NOT_ANALYZED, new Object[] {value1, value2}));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleInInt() {
    Integer value1 = 1;
    Integer value2 = 2;
    Iterable<Object> values = Arrays.asList(value1, value2);
    Query<Entity> q = new QueryImpl<>().in(intAttrName, values);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(termsQuery(intAttrName, new Object[] {value1, value2}));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleInLong() {
    Long value1 = 0L;
    Long value2 = 1L;
    Iterable<Object> values = Arrays.asList(value1, value2);
    Query<Entity> q = new QueryImpl<>().in(longAttrName, values);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(termsQuery(longAttrName, new Object[] {value1, value2}));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleInMref_Entities() {
    Entity ref0 = new DynamicEntity(refEntityType);
    ref0.set(idAttrName, "id0");
    Entity ref1 = new DynamicEntity(refEntityType);
    ref1.set(idAttrName, "id1");
    Entity ref2 = new DynamicEntity(refEntityType);
    ref2.set(idAttrName, "id2");

    Iterable<Object> values = Arrays.asList(ref0, ref1, ref2);
    Query<Entity> q = new QueryImpl<>().in(mrefAttrName, values);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(
            nestedQuery(
                mrefAttrName,
                termsQuery(
                    mrefAttrName + '.' + idAttrName + '.' + FIELD_NOT_ANALYZED,
                    new Object[] {"id0", "id1", "id2"}),
                ScoreMode.Avg));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleInMref_Ids() {
    Iterable<String> values = asList("id0", "id1", "id2");
    Query<Entity> q = new QueryImpl<>().in(mrefAttrName, values);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(
            nestedQuery(
                mrefAttrName,
                termsQuery(
                    mrefAttrName + '.' + idAttrName + '.' + FIELD_NOT_ANALYZED,
                    new Object[] {"id0", "id1", "id2"}),
                ScoreMode.Avg));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleInScript() {
    String value1 = "var a = 0;";
    String value2 = "var b = 'a'";
    Iterable<Object> values = Arrays.asList(value1, value2);
    Query<Entity> q = new QueryImpl<>().in(scriptAttrName, values);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(
            termsQuery(scriptAttrName + '.' + FIELD_NOT_ANALYZED, new Object[] {value1, value2}));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleInString() {
    String value1 = "str0";
    String value2 = "str1";
    Iterable<Object> values = Arrays.asList(value1, value2);
    Query<Entity> q = new QueryImpl<>().in(stringAttrName, values);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(
            termsQuery(stringAttrName + '.' + FIELD_NOT_ANALYZED, new Object[] {value1, value2}));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleInText() {
    String value1 = "some very long text";
    String value2 = "a bit shorter text";
    Iterable<Object> values = Arrays.asList(value1, value2);
    Query<Entity> q = new QueryImpl<>().in(textAttrName, values);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(
            termsQuery(textAttrName + '.' + FIELD_NOT_ANALYZED, new Object[] {value1, value2}));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleInXref_Ids() {
    Iterable<String> values = asList("id0", "id1", "id2");
    Query<Entity> q = new QueryImpl<>().in(xrefAttrName, values);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(
            nestedQuery(
                xrefAttrName,
                termsQuery(
                    xrefAttrName + '.' + idAttrName + '.' + FIELD_NOT_ANALYZED,
                    new Object[] {"id0", "id1", "id2"}),
                ScoreMode.Avg));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleInXref_Entities() {
    Entity ref0 = new DynamicEntity(refEntityType);
    ref0.set(idAttrName, "id0");
    Entity ref1 = new DynamicEntity(refEntityType);
    ref1.set(idAttrName, "id1");
    Entity ref2 = new DynamicEntity(refEntityType);
    ref2.set(idAttrName, "id2");

    Iterable<Object> values = Arrays.asList(ref0, ref1, ref2);
    Query<Entity> q = new QueryImpl<>().in(xrefAttrName, values);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(
            nestedQuery(
                xrefAttrName,
                termsQuery(
                    xrefAttrName + '.' + idAttrName + '.' + FIELD_NOT_ANALYZED,
                    new Object[] {"id0", "id1", "id2"}),
                ScoreMode.Avg));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleLesserDate() throws ParseException {
    String date = "2015-05-22";
    LocalDate value = LocalDate.parse(date);
    Query<Entity> q = new QueryImpl<>().lt(dateAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(rangeQuery(dateAttrName).lt(date));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleLesserDateTime() throws ParseException {
    Instant value = Instant.parse("2015-05-22T06:12:13Z");
    Query<Entity> q = new QueryImpl<>().lt(dateTimeAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(rangeQuery(dateTimeAttrName).lt(DataConverter.toString(value)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleLesserDecimal() {
    Double value = 1.23;
    Query<Entity> q = new QueryImpl<>().lt(decimalAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(rangeQuery(decimalAttrName).lt(value));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleLesserInt() {
    Integer value = 1;
    Query<Entity> q = new QueryImpl<>().lt(intAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(rangeQuery(intAttrName).lt(value));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleLesserLong() {
    Long value = 1L;
    Query<Entity> q = new QueryImpl<>().lt(longAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(rangeQuery(longAttrName).lt(value));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleLikeBool() {
    String value = "value";
    Query<Entity> q = new QueryImpl<>().like(boolAttrName, value);
    assertThrows(
        MolgenisQueryException.class, () -> queryGenerator.createQueryBuilder(q, entityType));
  }

  @Test
  void generateOneQueryRuleLikeCategorical() {
    String value = "value";
    Query<Entity> q = new QueryImpl<>().like(categoricalAttrName, value);
    assertThrows(
        UnsupportedOperationException.class,
        () -> queryGenerator.createQueryBuilder(q, entityType));
  }

  @Test
  void generateOneQueryRuleLikeCompound() {
    String value = "value";
    Query<Entity> q = new QueryImpl<>().like(compoundAttrName, value);
    assertThrows(
        MolgenisQueryException.class, () -> queryGenerator.createQueryBuilder(q, entityType));
  }

  @Test
  void generateOneQueryRuleLikeCompoundPartString() {
    String value = "value";
    Query<Entity> q = new QueryImpl<>().like(compoundPart0AttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        matchPhrasePrefixQuery(compoundPart0AttrName, value).slop(10).analyzer(DEFAULT_ANALYZER);
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleLikeDate() {
    String value = "value";
    Query<Entity> q = new QueryImpl<>().like(dateAttrName, value);
    assertThrows(
        MolgenisQueryException.class, () -> queryGenerator.createQueryBuilder(q, entityType));
  }

  @Test
  void generateOneQueryRuleLikeDateTime() {
    String value = "value";
    Query<Entity> q = new QueryImpl<>().like(dateTimeAttrName, value);
    assertThrows(
        MolgenisQueryException.class, () -> queryGenerator.createQueryBuilder(q, entityType));
  }

  @Test
  void generateOneQueryRuleLikeDecimal() {
    String value = "value";
    Query<Entity> q = new QueryImpl<>().like(decimalAttrName, value);
    assertThrows(
        MolgenisQueryException.class, () -> queryGenerator.createQueryBuilder(q, entityType));
  }

  @Test
  void generateOneQueryRuleLikeEmail() {
    String value = "e@mail.com";
    Query<Entity> q = new QueryImpl<>().like(emailAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        matchPhrasePrefixQuery(emailAttrName, value).slop(10).analyzer(DEFAULT_ANALYZER);
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleLikeEnum() {
    String value = "enum0";
    Query<Entity> q = new QueryImpl<>().like(enumAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        matchPhrasePrefixQuery(enumAttrName, value).slop(10).analyzer(DEFAULT_ANALYZER);
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleLikeHtml() {
    String value = "<h1>html</h1>";
    Query<Entity> q = new QueryImpl<>().like(htmlAttrName, value);
    assertThrows(
        UnsupportedOperationException.class,
        () -> queryGenerator.createQueryBuilder(q, entityType));
  }

  @Test
  void generateOneQueryRuleLikeHyperlink() {
    String value = "http://www.website.com/";
    Query<Entity> q = new QueryImpl<>().like(hyperlinkAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        matchPhrasePrefixQuery(hyperlinkAttrName, value).slop(10).analyzer(DEFAULT_ANALYZER);
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleLikeInt() {
    String value = "value";
    Query<Entity> q = new QueryImpl<>().like(intAttrName, value);
    assertThrows(
        MolgenisQueryException.class, () -> queryGenerator.createQueryBuilder(q, entityType));
  }

  @Test
  void generateOneQueryRuleLikeLong() {
    String value = "value";
    Query<Entity> q = new QueryImpl<>().like(longAttrName, value);
    assertThrows(
        MolgenisQueryException.class, () -> queryGenerator.createQueryBuilder(q, entityType));
  }

  @Test
  void generateOneQueryRuleLikeMref() {
    String value = "value";
    Query<Entity> q = new QueryImpl<>().like(mrefAttrName, value);
    assertThrows(
        UnsupportedOperationException.class,
        () -> queryGenerator.createQueryBuilder(q, entityType));
  }

  @Test
  void generateOneQueryRuleLikeScript() {
    String value = "int a = 1;";
    Query<Entity> q = new QueryImpl<>().like(scriptAttrName, value);
    assertThrows(
        UnsupportedOperationException.class,
        () -> queryGenerator.createQueryBuilder(q, entityType));
  }

  @Test
  void generateOneQueryRuleLikeString() {
    String value = "value";
    Query<Entity> q = new QueryImpl<>().like(stringAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        matchPhrasePrefixQuery(stringAttrName, value).slop(10).analyzer(DEFAULT_ANALYZER);
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleLikeText() {
    String value = "some long text";
    Query<Entity> q = new QueryImpl<>().like(textAttrName, value);
    assertThrows(
        UnsupportedOperationException.class,
        () -> queryGenerator.createQueryBuilder(q, entityType));
  }

  @Test
  void generateOneQueryRuleLikeXref() {
    String value = "value";
    Query<Entity> q = new QueryImpl<>().like(xrefAttrName, value);
    assertThrows(
        UnsupportedOperationException.class,
        () -> queryGenerator.createQueryBuilder(q, entityType));
  }

  @Test
  void generateOneQueryRuleEqualsBoolNull() {
    Boolean value = null;
    Query<Entity> q = new QueryImpl<>().eq(boolAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(boolQuery().mustNot(existsQuery(boolAttrName)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  // FIXME add test for ref entity where id attribute is int
  // FIXME add test where value is entity
  @Test
  void generateOneQueryRuleEqualsCategoricalNull() {
    String value = null;
    Query<Entity> q = new QueryImpl<>().eq(categoricalAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(
            boolQuery()
                .mustNot(
                    nestedQuery(
                        categoricalAttrName,
                        existsQuery(categoricalAttrName + ".xid"),
                        ScoreMode.Avg)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleEqualsCompoundNull() {
    Object value = null;
    Query<Entity> q = new QueryImpl<>().eq(compoundAttrName, value);
    assertThrows(
        MolgenisQueryException.class, () -> queryGenerator.createQueryBuilder(q, entityType));
  }

  @Test
  void generateOneQueryRuleEqualsCompoundPartStringNull() {
    String value = null;
    Query<Entity> q = new QueryImpl<>().eq(compoundPart0AttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(boolQuery().mustNot(existsQuery(compoundPart0AttrName)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleEqualsDateNull() throws ParseException {
    LocalDate value = null;
    Query<Entity> q = new QueryImpl<>().eq(dateAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(boolQuery().mustNot(existsQuery(dateAttrName)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleEqualsDateTimeNull() throws ParseException {
    Instant value = null;
    Query<Entity> q = new QueryImpl<>().eq(dateTimeAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(boolQuery().mustNot(existsQuery(dateTimeAttrName)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleEqualsDecimalNull() {
    Double value = null;
    Query<Entity> q = new QueryImpl<>().eq(decimalAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(boolQuery().mustNot(existsQuery(decimalAttrName)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleEqualsEmailNull() {
    String value = null;
    Query<Entity> q = new QueryImpl<>().eq(emailAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(boolQuery().mustNot(existsQuery(emailAttrName)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleEqualsEnumNull() {
    String value = null;
    Query<Entity> q = new QueryImpl<>().eq(enumAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(boolQuery().mustNot(existsQuery(enumAttrName)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleEqualsHtmlNull() {
    String value = null;
    Query<Entity> q = new QueryImpl<>().eq(htmlAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(boolQuery().mustNot(existsQuery(htmlAttrName)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleEqualsHyperlinkNull() {
    String value = null;
    Query<Entity> q = new QueryImpl<>().eq(hyperlinkAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(boolQuery().mustNot(existsQuery(hyperlinkAttrName)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleEqualsIntNull() {
    Integer value = null;
    Query<Entity> q = new QueryImpl<>().eq(intAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(boolQuery().mustNot(existsQuery(intAttrName)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleEqualsLongNull() {
    Long value = null;
    Query<Entity> q = new QueryImpl<>().eq(longAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(boolQuery().mustNot(existsQuery(longAttrName)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  // TODO enable when implemented in QueryGenerator (see note in QueryGenerator)
  // @Test
  // void generateOneQueryRuleEqualsMrefNull()
  // {
  // }

  @Test
  void generateOneQueryRuleEqualsScriptNull() {
    String value = null;
    Query<Entity> q = new QueryImpl<>().eq(scriptAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(boolQuery().mustNot(existsQuery(scriptAttrName)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleEqualsStringNull() {
    String value = null;
    Query<Entity> q = new QueryImpl<>().eq(stringAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(boolQuery().mustNot(existsQuery(stringAttrName)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleEqualsTextNull() {
    String value = null;
    Query<Entity> q = new QueryImpl<>().eq(textAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(boolQuery().mustNot(existsQuery(textAttrName)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  // FIXME add test for ref entity where id attribute is int
  // FIXME add test where value is entity
  @Test
  void generateOneQueryRuleEqualsXrefNull() {
    String value = null;
    Query<Entity> q = new QueryImpl<>().eq(xrefAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(
            boolQuery()
                .mustNot(
                    nestedQuery(xrefAttrName, existsQuery(xrefAttrName + ".xid"), ScoreMode.Avg)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleNotEqualsBoolNull() {
    Boolean value = null;
    Query<Entity> q = new QueryImpl<>().not().eq(boolAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery().mustNot(constantScoreQuery(boolQuery().mustNot(existsQuery(boolAttrName))));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  // FIXME add test for ref entity where id attribute is int
  // FIXME add test where value is entity
  @Test
  void generateOneQueryRuleNotEqualsCategoricalNull() {
    String value = null;
    Query<Entity> q = new QueryImpl<>().not().eq(categoricalAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery()
            .mustNot(
                constantScoreQuery(
                    boolQuery()
                        .mustNot(
                            nestedQuery(
                                categoricalAttrName,
                                existsQuery(categoricalAttrName + ".xid"),
                                ScoreMode.Avg))));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleNotEqualsCompoundNull() {
    Object value = null;
    Query<Entity> q = new QueryImpl<>().not().eq(compoundAttrName, value);
    assertThrows(
        MolgenisQueryException.class, () -> queryGenerator.createQueryBuilder(q, entityType));
  }

  @Test
  void generateOneQueryRuleNotEqualsCompoundPartStringNull() {
    String value = null;
    Query<Entity> q = new QueryImpl<>().not().eq(compoundPart0AttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery()
            .mustNot(constantScoreQuery(boolQuery().mustNot(existsQuery(compoundPart0AttrName))));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleNotEqualsDateNull() throws ParseException {
    LocalDate value = null;
    Query<Entity> q = new QueryImpl<>().not().eq(dateAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery().mustNot(constantScoreQuery(boolQuery().mustNot(existsQuery(dateAttrName))));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleNotEqualsDateTimeNull() throws ParseException {
    Instant value = null;
    Query<Entity> q = new QueryImpl<>().not().eq(dateTimeAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery().mustNot(constantScoreQuery(boolQuery().mustNot(existsQuery(dateTimeAttrName))));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleNotEqualsDecimalNull() {
    Double value = null;
    Query<Entity> q = new QueryImpl<>().not().eq(decimalAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery().mustNot(constantScoreQuery(boolQuery().mustNot(existsQuery(decimalAttrName))));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleNotEqualsEmailNull() {
    String value = null;
    Query<Entity> q = new QueryImpl<>().not().eq(emailAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery().mustNot(constantScoreQuery(boolQuery().mustNot(existsQuery(emailAttrName))));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleNotEqualsEnumNull() {
    String value = null;
    Query<Entity> q = new QueryImpl<>().not().eq(enumAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery().mustNot(constantScoreQuery(boolQuery().mustNot(existsQuery(enumAttrName))));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleNotEqualsHtmlNull() {
    String value = null;
    Query<Entity> q = new QueryImpl<>().not().eq(htmlAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery().mustNot(constantScoreQuery(boolQuery().mustNot(existsQuery(htmlAttrName))));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleNotEqualsHyperlinkNull() {
    String value = null;
    Query<Entity> q = new QueryImpl<>().not().eq(hyperlinkAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery()
            .mustNot(constantScoreQuery(boolQuery().mustNot(existsQuery(hyperlinkAttrName))));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleNotEqualsIntNull() {
    Integer value = null;
    Query<Entity> q = new QueryImpl<>().not().eq(intAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery().mustNot(constantScoreQuery(boolQuery().mustNot(existsQuery(intAttrName))));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleNotEqualsLongNull() {
    Long value = null;
    Query<Entity> q = new QueryImpl<>().not().eq(longAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery().mustNot(constantScoreQuery(boolQuery().mustNot(existsQuery(longAttrName))));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  // TODO enable when implemented in QueryGenerator (see note in QueryGenerator)
  // @Test
  // void generateOneQueryRuleNotEqualsMrefNull()
  // {
  // }

  @Test
  void generateOneQueryRuleNotEqualsScriptNull() {
    String value = null;
    Query<Entity> q = new QueryImpl<>().not().eq(scriptAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery().mustNot(constantScoreQuery(boolQuery().mustNot(existsQuery(scriptAttrName))));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleNotEqualsStringNull() {
    String value = null;
    Query<Entity> q = new QueryImpl<>().not().eq(stringAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery().mustNot(constantScoreQuery(boolQuery().mustNot(existsQuery(stringAttrName))));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleNotEqualsTextNull() {
    String value = null;
    Query<Entity> q = new QueryImpl<>().not().eq(textAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery().mustNot(constantScoreQuery(boolQuery().mustNot(existsQuery(textAttrName))));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  // FIXME add test for ref entity where id attribute is int
  // FIXME add test where value is entity
  @Test
  void generateOneQueryRuleNotEqualsXrefNull() {
    String value = null;
    Query<Entity> q = new QueryImpl<>().not().eq(xrefAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery()
            .mustNot(
                constantScoreQuery(
                    boolQuery()
                        .mustNot(
                            nestedQuery(
                                xrefAttrName, existsQuery(xrefAttrName + ".xid"), ScoreMode.Avg))));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleEqualsBool() {
    Boolean value = Boolean.TRUE;
    Query<Entity> q = new QueryImpl<>().eq(boolAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(termQuery(boolAttrName, value));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  // FIXME add test for ref entity where id attribute is int
  // FIXME add test where value is entity
  @Test
  void generateOneQueryRuleEqualsCategorical() {
    String value = "id";
    Query<Entity> q = new QueryImpl<>().eq(categoricalAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(
            nestedQuery(
                categoricalAttrName,
                termQuery(categoricalAttrName + ".xid." + FIELD_NOT_ANALYZED, value),
                ScoreMode.Avg));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleEqualsCompound() {
    Object value = "value";
    Query<Entity> q = new QueryImpl<>().eq(compoundAttrName, value);
    assertThrows(
        MolgenisQueryException.class, () -> queryGenerator.createQueryBuilder(q, entityType));
  }

  @Test
  void generateOneQueryRuleEqualsCompoundPartString() {
    String value = "value";
    Query<Entity> q = new QueryImpl<>().eq(compoundPart0AttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(termQuery(compoundPart0AttrName + '.' + FIELD_NOT_ANALYZED, value));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleEqualsDate() throws ParseException {
    LocalDate value = LocalDate.parse("2015-01-15");
    Query<Entity> q = new QueryImpl<>().eq(dateAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(termQuery(dateAttrName, "2015-01-15"));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleEqualsDateTime() throws ParseException {
    Instant value = Instant.parse("2015-05-22T06:12:13Z");
    Query<Entity> q = new QueryImpl<>().eq(dateTimeAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(termQuery(dateTimeAttrName, value.toString()));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleEqualsDecimal() {
    Double value = 1.23;
    Query<Entity> q = new QueryImpl<>().eq(decimalAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(termQuery(decimalAttrName, value));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleEqualsEmail() {
    String value = "e@mail.com";
    Query<Entity> q = new QueryImpl<>().eq(emailAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(termQuery(emailAttrName + '.' + FIELD_NOT_ANALYZED, value));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleEqualsEnum() {
    String value = "enum0";
    Query<Entity> q = new QueryImpl<>().eq(enumAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(termQuery(enumAttrName + '.' + FIELD_NOT_ANALYZED, value));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleEqualsHtml() {
    String value = "<h1>html</h1>";
    Query<Entity> q = new QueryImpl<>().eq(htmlAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(termQuery(htmlAttrName + '.' + FIELD_NOT_ANALYZED, value));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleEqualsHyperlink() {
    String value = "http://www.website.com/";
    Query<Entity> q = new QueryImpl<>().eq(hyperlinkAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(termQuery(hyperlinkAttrName + '.' + FIELD_NOT_ANALYZED, value));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleEqualsInt() {
    Integer value = 1;
    Query<Entity> q = new QueryImpl<>().eq(intAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(termQuery(intAttrName, value));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleEqualsLong() {
    Long value = 1L;
    Query<Entity> q = new QueryImpl<>().eq(longAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(termQuery(longAttrName, value));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  // TODO enable when implemented in QueryGenerator (see note in QueryGenerator)
  // @Test
  // void generateOneQueryRuleEqualsMref()
  // {
  // }

  @Test
  void generateOneQueryRuleEqualsScript() {
    String value = "int a = 1;";
    Query<Entity> q = new QueryImpl<>().eq(scriptAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(termQuery(scriptAttrName + '.' + FIELD_NOT_ANALYZED, value));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleEqualsString() {
    String value = "value";
    Query<Entity> q = new QueryImpl<>().eq(stringAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(termQuery(stringAttrName + '.' + FIELD_NOT_ANALYZED, value));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleEqualsText() {
    String value = "some long text";
    Query<Entity> q = new QueryImpl<>().eq(textAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(termQuery(textAttrName + '.' + FIELD_NOT_ANALYZED, value));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  // FIXME add test for ref entity where id attribute is int
  // FIXME add test where value is entity
  @Test
  void generateOneQueryRuleEqualsXref() {
    String value = "id";
    Query<Entity> q = new QueryImpl<>().eq(xrefAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        constantScoreQuery(
            nestedQuery(
                xrefAttrName,
                termQuery(xrefAttrName + ".xid." + FIELD_NOT_ANALYZED, value),
                ScoreMode.Avg));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleNotEqualsBool() {
    Boolean value = Boolean.TRUE;
    Query<Entity> q = new QueryImpl<>().not().eq(boolAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery().mustNot(constantScoreQuery(termQuery(boolAttrName, value)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  // FIXME add test for ref entity where id attribute is int
  // FIXME add test where value is entity
  @Test
  void generateOneQueryRuleNotEqualsCategorical() {
    String value = "id";
    Query<Entity> q = new QueryImpl<>().not().eq(categoricalAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery()
            .mustNot(
                constantScoreQuery(
                    nestedQuery(
                        categoricalAttrName,
                        termQuery(categoricalAttrName + ".xid." + FIELD_NOT_ANALYZED, value),
                        ScoreMode.Avg)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleNotEqualsCompound() {
    Object value = "value";
    Query<Entity> q = new QueryImpl<>().not().eq(compoundAttrName, value);
    assertThrows(
        MolgenisQueryException.class, () -> queryGenerator.createQueryBuilder(q, entityType));
  }

  @Test
  void generateOneQueryRuleNotEqualsCompoundPartString() {
    String value = "value";
    Query<Entity> q = new QueryImpl<>().not().eq(compoundPart0AttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery()
            .mustNot(
                constantScoreQuery(
                    termQuery(compoundPart0AttrName + '.' + FIELD_NOT_ANALYZED, value)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleNotEqualsDate() throws ParseException {
    LocalDate value = LocalDate.parse("2015-05-22");
    Query<Entity> q = new QueryImpl<>().not().eq(dateAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery().mustNot(constantScoreQuery(termQuery(dateAttrName, value.toString())));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleNotEqualsDateTime() throws ParseException {
    Instant value = Instant.parse("2015-05-22T06:12:13Z");
    Query<Entity> q = new QueryImpl<>().not().eq(dateTimeAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery().mustNot(constantScoreQuery(termQuery(dateTimeAttrName, value.toString())));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleNotEqualsDecimal() {
    Double value = 1.23;
    Query<Entity> q = new QueryImpl<>().not().eq(decimalAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery().mustNot(constantScoreQuery(termQuery(decimalAttrName, value)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleNotEqualsEmail() {
    String value = "e@mail.com";
    Query<Entity> q = new QueryImpl<>().not().eq(emailAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery()
            .mustNot(
                constantScoreQuery(termQuery(emailAttrName + '.' + FIELD_NOT_ANALYZED, value)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleNotEqualsEnum() {
    String value = "enum0";
    Query<Entity> q = new QueryImpl<>().not().eq(enumAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery()
            .mustNot(constantScoreQuery(termQuery(enumAttrName + '.' + FIELD_NOT_ANALYZED, value)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleNotEqualsHtml() {
    String value = "<h1>html</h1>";
    Query<Entity> q = new QueryImpl<>().not().eq(htmlAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery()
            .mustNot(constantScoreQuery(termQuery(htmlAttrName + '.' + FIELD_NOT_ANALYZED, value)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleNotEqualsHyperlink() {
    String value = "http://www.website.com/";
    Query<Entity> q = new QueryImpl<>().not().eq(hyperlinkAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery()
            .mustNot(
                constantScoreQuery(termQuery(hyperlinkAttrName + '.' + FIELD_NOT_ANALYZED, value)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleNotEqualsInt() {
    Integer value = 1;
    Query<Entity> q = new QueryImpl<>().not().eq(intAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery().mustNot(constantScoreQuery(termQuery(intAttrName, value)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleNotEqualsLong() {
    Long value = 1L;
    Query<Entity> q = new QueryImpl<>().not().eq(longAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery().mustNot(constantScoreQuery(termQuery(longAttrName, value)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  // TODO enable when implemented in QueryGenerator (see note in QueryGenerator)
  // @Test
  // void generateOneQueryRuleNotEqualsMref()
  // {
  // }

  @Test
  void generateOneQueryRuleNotEqualsScript() {
    String value = "int a = 1;";
    Query<Entity> q = new QueryImpl<>().not().eq(scriptAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery()
            .mustNot(
                constantScoreQuery(termQuery(scriptAttrName + '.' + FIELD_NOT_ANALYZED, value)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleNotEqualsString() {
    String value = "value";
    Query<Entity> q = new QueryImpl<>().not().eq(stringAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery()
            .mustNot(
                constantScoreQuery(termQuery(stringAttrName + '.' + FIELD_NOT_ANALYZED, value)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleNotEqualsText() {
    String value = "some long text";
    Query<Entity> q = new QueryImpl<>().not().eq(textAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery()
            .mustNot(constantScoreQuery(termQuery(textAttrName + '.' + FIELD_NOT_ANALYZED, value)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  // FIXME add test for ref entity where id attribute is int
  // FIXME add test where value is entity
  @Test
  void generateOneQueryRuleNotEqualsXref() {
    String value = "id";
    Query<Entity> q = new QueryImpl<>().not().eq(xrefAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        boolQuery()
            .mustNot(
                constantScoreQuery(
                    nestedQuery(
                        xrefAttrName,
                        termQuery(xrefAttrName + ".xid." + FIELD_NOT_ANALYZED, value),
                        ScoreMode.Avg)));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleRangeInt() {
    Integer low = 3;
    Integer high = 9;
    Query<Entity> q = new QueryImpl<>().rng(intAttrName, low, high);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(rangeQuery(intAttrName).gte(3).lte(9));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleRangeLong() {
    Long low = 3L;
    Long high = 9L;
    Query<Entity> q = new QueryImpl<>().rng(longAttrName, low, high);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = constantScoreQuery(rangeQuery(longAttrName).gte(3).lte(9));
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleSearchAllFields() {
    String value = "my text";
    Query<Entity> q = new QueryImpl<>().search(value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = matchPhraseQuery("_all", value).slop(10);
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleSearchOneFieldBool() {
    String value = "value";
    Query<Entity> q = new QueryImpl<>().search(boolAttrName, value);
    assertThrows(
        MolgenisQueryException.class, () -> queryGenerator.createQueryBuilder(q, entityType));
  }

  @Test
  void generateOneQueryRuleSearchOneFieldCategorical() {
    String value = "text";
    Query<Entity> q = new QueryImpl<>().search(categoricalAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        nestedQuery(
            categoricalAttrName, matchQuery(categoricalAttrName + "._all", value), ScoreMode.Avg);
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleSearchOneFieldCompound() {
    String value = "value";
    Query<Entity> q = new QueryImpl<>().search(compoundAttrName, value);
    assertThrows(
        MolgenisQueryException.class, () -> queryGenerator.createQueryBuilder(q, entityType));
  }

  @Test
  void generateOneQueryRuleSearchOneFieldCompoundPartString() {
    String value = "value";
    Query<Entity> q = new QueryImpl<>().search(compoundPart0AttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = matchQuery(compoundPart0AttrName, value);
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleSearchOneFieldDate() throws ParseException {
    String value = "2015-05-22";
    Query<Entity> q = new QueryImpl<>().search(dateAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = matchQuery(dateAttrName, value);
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleSearchOneFieldDateTime() throws ParseException {
    String value = "2015-05-22T06:12:13Z";
    Query<Entity> q = new QueryImpl<>().search(dateTimeAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = matchQuery(dateTimeAttrName, value);
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleSearchOneFieldDecimal() {
    String value = Double.valueOf(1.23).toString();
    Query<Entity> q = new QueryImpl<>().search(decimalAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = matchQuery(decimalAttrName, value);
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleSearchOneFieldEmail() {
    String value = "e@mail.com";
    Query<Entity> q = new QueryImpl<>().search(emailAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = matchQuery(emailAttrName, value);
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleSearchOneFieldEnum() {
    String value = "enum0";
    Query<Entity> q = new QueryImpl<>().search(enumAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = matchQuery(enumAttrName, value);
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleSearchOneFieldHtml() {
    String value = "<h1>html</h1>";
    Query<Entity> q = new QueryImpl<>().search(htmlAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = matchQuery(htmlAttrName, value);
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleSearchOneFieldHyperlink() {
    String value = "http://www.website.com/";
    Query<Entity> q = new QueryImpl<>().search(hyperlinkAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = matchQuery(hyperlinkAttrName, value);
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleSearchOneFieldInt() {
    String value = Integer.valueOf(1).toString();
    Query<Entity> q = new QueryImpl<>().search(intAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = matchQuery(intAttrName, value);
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleSearchOneFieldLong() {
    String value = Long.valueOf(1).toString();
    Query<Entity> q = new QueryImpl<>().search(longAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = matchQuery(longAttrName, value);
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleSearchOneFieldMref() {
    String value = "value";
    Query<Entity> q = new QueryImpl<>().search(mrefAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        nestedQuery(mrefAttrName, matchQuery(mrefAttrName + "._all", value), ScoreMode.Avg);
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleSearchOneFieldScript() {
    String value = "int a = 1;";
    Query<Entity> q = new QueryImpl<>().search(scriptAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = matchQuery(scriptAttrName, value);
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleSearchOneFieldString() {
    String value = "value";
    Query<Entity> q = new QueryImpl<>().search(stringAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = matchQuery(stringAttrName, value);
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleSearchOneFieldText() {
    String value = "some long text";
    Query<Entity> q = new QueryImpl<>().search(textAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery = matchQuery(textAttrName, value);
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateOneQueryRuleSearchOneFieldXref() {
    String value = "text";
    Query<Entity> q = new QueryImpl<>().search(xrefAttrName, value);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder expectedQuery =
        nestedQuery(xrefAttrName, matchQuery(xrefAttrName + "._all", value), ScoreMode.Avg);
    assertQueryBuilderEquals(query, expectedQuery);
  }

  @Test
  void generateMultipleQueryRule() {
    // query: a or (b and c)
    Boolean booleanValue = Boolean.TRUE;
    String stringValue = "str";
    Integer intValue = 1;
    Query<Entity> q =
        new QueryImpl<>()
            .eq(boolAttrName, booleanValue)
            .or()
            .nest()
            .eq(stringAttrName, stringValue)
            .and()
            .eq(intAttrName, intValue)
            .unnest();
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder booleanQuery = constantScoreQuery(termQuery(boolAttrName, booleanValue));
    QueryBuilder stringQuery =
        constantScoreQuery(termQuery(stringAttrName + '.' + FIELD_NOT_ANALYZED, stringValue));
    QueryBuilder intQuery = constantScoreQuery(termQuery(intAttrName, intValue));
    BoolQueryBuilder stringIntQuery = boolQuery().must(stringQuery).must(intQuery);
    QueryBuilder expectedQuery =
        boolQuery().should(booleanQuery).should(stringIntQuery).minimumShouldMatch(1);
    assertQueryBuilderEquals(query, expectedQuery);
  }

  // regression test for https://github.com/molgenis/molgenis/issues/2326
  @Test
  void generateMultipleQueryRuleMultipleNotClauses() {
    // query: a or (b and c)
    Boolean booleanValue = Boolean.TRUE;
    String stringValue = "str";
    Integer intValue = 1;
    Query<Entity> q =
        new QueryImpl<>()
            .eq(boolAttrName, booleanValue)
            .and()
            .not()
            .eq(stringAttrName, stringValue)
            .and()
            .not()
            .eq(intAttrName, intValue);
    QueryBuilder query = queryGenerator.createQueryBuilder(q, entityType);
    QueryBuilder booleanQuery = constantScoreQuery(termQuery(boolAttrName, booleanValue));
    QueryBuilder stringQuery =
        constantScoreQuery(termQuery(stringAttrName + '.' + FIELD_NOT_ANALYZED, stringValue));
    QueryBuilder intQuery = constantScoreQuery(termQuery(intAttrName, intValue));
    QueryBuilder expectedQuery =
        boolQuery().must(booleanQuery).mustNot(stringQuery).mustNot(intQuery);
    assertQueryBuilderEquals(query, expectedQuery);
  }

  private void assertQueryBuilderEquals(QueryBuilder actual, QueryBuilder expected) {
    // QueryBuilder classes do not implement equals
    assertEquals(
        expected.toString().replaceAll("\\s", ""), actual.toString().replaceAll("\\s", ""));
  }
}
