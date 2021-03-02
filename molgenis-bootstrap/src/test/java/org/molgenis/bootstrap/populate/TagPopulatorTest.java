package org.molgenis.bootstrap.populate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.semantic.Relation.isAssociatedWith;

import java.util.List;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.meta.model.TagFactory;
import org.molgenis.data.meta.model.TagMetadata;
import org.molgenis.data.semantic.Vocabulary;

@ExtendWith(MockitoExtension.class)
class TagPopulatorTest {
  @Mock DataService dataService;
  @Mock TagFactory tagFactory;
  @Mock Tag token;
  @Mock Tag caseSensitive;
  @Mock Tag audited;
  @Mock Repository<Tag> tagRepository;

  private TagPopulator tagPopulator;

  @BeforeEach
  public void setup() {
    tagPopulator = new TagPopulator(tagFactory, dataService);
  }

  @Test
  public void testPopulate() {
    when(tagFactory.create("token")).thenReturn(token);
    when(tagFactory.create("case-sensitive")).thenReturn(caseSensitive);
    when(tagFactory.create("audit-audited")).thenReturn(audited);

    when(dataService.getRepository(TagMetadata.TAG, Tag.class)).thenReturn(tagRepository);

    tagPopulator.populate();

    verify(tagRepository).upsertBatch(List.of(token, caseSensitive, audited));
    verify(token).setRelationIri(RDF.TYPE.toString());
    verify(token).setObjectIri(XMLSchema.TOKEN.toString());

    verify(caseSensitive).setRelationIri(RDF.TYPE.toString());
    verify(caseSensitive).setObjectIri(Vocabulary.CASE_SENSITIVE.toString());

    verify(audited).setRelationIri(isAssociatedWith.getIRI());
    verify(audited).setObjectIri(Vocabulary.AUDIT_USAGE.toString());
  }
}
