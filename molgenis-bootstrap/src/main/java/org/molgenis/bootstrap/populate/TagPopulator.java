package org.molgenis.bootstrap.populate;

import static org.eclipse.rdf4j.model.vocabulary.XMLSchema.TOKEN;
import static org.molgenis.data.meta.model.TagMetadata.TAG;
import static org.molgenis.data.semantic.Relation.isAudited;
import static org.molgenis.data.semantic.Relation.type;
import static org.molgenis.data.semantic.Vocabulary.AUDIT_USAGE;
import static org.molgenis.data.semantic.Vocabulary.CASE_SENSITIVE;
import static org.molgenis.data.semantic.Vocabulary.SCRAMBLED;

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

    Tag audited = tagFactory.create("audit-usage");
    audited.setLabel("Audit Usage");
    audited.setObjectIri(AUDIT_USAGE.toString());
    audited.setRelationIri(isAudited.getIRI());
    audited.setRelationLabel(isAudited.getLabel());

    Tag scrambled = tagFactory.create("scrambled");
    scrambled.setLabel("Scrambled");
    scrambled.setObjectIri(SCRAMBLED.toString());
    scrambled.setRelationIri(type.getIRI());
    scrambled.setRelationLabel(type.getLabel());

    dataService
        .getRepository(TAG, Tag.class)
        .upsertBatch(List.of(isAToken, isCaseSensitive, audited, scrambled));
  }
}
