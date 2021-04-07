package org.molgenis.data.migrate.version;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.jdbc.core.JdbcTemplate;

class Step47AddMaxLengthTest extends AbstractMockitoTest {

  @Mock private JdbcTemplate jdbcTemplate;
  private Step47AddMaxLength step47;

  @BeforeEach
  void setUpBeforeEach() {
    step47 = new Step47AddMaxLength(jdbcTemplate);
  }

  @Test
  void upgrade() {
    step47.upgrade();
    verify(jdbcTemplate)
        .execute(
            "alter table \"sys_md_Attribute#c8d9a252\" add column \"maxLength\" integer;\n"
                + "INSERT INTO \"sys_md_Attribute#c8d9a252\"\n"
                + "    (id, name, entity, \"sequenceNr\", type, \"isIdAttribute\", \"isLabelAttribute\", \"lookupAttributeIndex\", parent, \"refEntityType\", \"isCascadeDelete\", \"mappedBy\", \"orderBy\", expression, \"isNullable\", \"isAuto\", \"isVisible\", label, description, \"isAggregatable\", \"enumOptions\", \"rangeMin\", \"rangeMax\", \"maxLength\", \"isReadOnly\", \"isUnique\", \"nullableExpression\", \"visibleExpression\", \"validationExpression\", \"defaultValue\", \"labelEn\", \"descriptionEn\", \"labelNl\", \"descriptionNl\", \"labelDe\", \"descriptionDe\", \"labelEs\", \"descriptionEs\", \"labelIt\", \"descriptionIt\", \"labelPt\", \"descriptionPt\", \"labelFr\", \"descriptionFr\", \"labelXx\", \"descriptionXx\")\n"
                + "    VALUES ('aaaac6flvg7gtbroh7raetqaca', 'maxLength', 'sys_md_Attribute', 24, 'int', null, null, null, null, null, null, null, null, null, true, false, true, 'Max length', 'Maximum length for string attributes. If not set, falls back to the default for the attribute type.', false, null, null, null, null, false, false, null, null, '$(''maxLength'').isNull().or($(''type'').matches(/^(email|html|hyperlink|script|string|text)$/)).value()', null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);");
  }
}
