package org.molgenis.bootstrap.populate;

import static org.eclipse.rdf4j.model.vocabulary.XMLSchema.TOKEN;
import static org.molgenis.data.meta.model.TagMetadata.TAG;
import static org.molgenis.data.semantic.Relation.isAssociatedWith;
import static org.molgenis.data.semantic.Relation.type;
import static org.molgenis.data.semantic.Vocabulary.AUDITED;
import static org.molgenis.data.semantic.Vocabulary.CASE_SENSITIVE;

import java.util.List;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.meta.model.TagFactory;
import org.springframework.stereotype.Component;

@Component
public class TagPopulator {

  private final TagFactory tagFactory;
  private final DataService dataService;

  public TagPopulator(TagFactory tagFactory, DataService dataService) {
    this.tagFactory = tagFactory;
    this.dataService = dataService;
  }

  public void populate() {
    Tag isAToken = tagFactory.create("token");
    isAToken.setLabel("Token");
    isAToken.setObjectIri(TOKEN.toString());
    isAToken.setRelationIri(type.getIRI());
    isAToken.setRelationLabel(type.getLabel());

    Tag isCaseSensitive = tagFactory.create("case-sensitive");
    isCaseSensitive.setLabel("Case Sensitive");
    isCaseSensitive.setObjectIri(CASE_SENSITIVE.toString());
    isCaseSensitive.setRelationIri(type.getIRI());
    isCaseSensitive.setRelationLabel(type.getLabel());

    Tag audited = tagFactory.create("audit-audited");
    audited.setLabel("Audited");
    audited.setObjectIri(AUDITED.toString());
    audited.setRelationIri(isAssociatedWith.getIRI());
    audited.setRelationLabel(isAssociatedWith.getIRI());

    dataService
        .getRepository(TAG, Tag.class)
        .upsertBatch(List.of(isAToken, isCaseSensitive, audited));
  }
}
