package org.molgenis.data.migrate.version;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.elasticsearch.client.ClientFacade;
import org.molgenis.data.elasticsearch.generator.model.Index;
import org.molgenis.data.index.exception.UnknownIndexException;
import org.molgenis.data.migrate.framework.MolgenisUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Step41Reindex extends MolgenisUpgrade {
  private static final Logger LOG = LoggerFactory.getLogger(Step41Reindex.class);
  private final ClientFacade clientFacade;

  public Step41Reindex(ClientFacade clientFacade) {
    super(40, 41);
    this.clientFacade = requireNonNull(clientFacade);
  }

  @Override
  public void upgrade() {
    LOG.debug("Removing attribute index to trigger a full reindex...");
    // Right now is too early to do metadata and reindex work.
    // Remove attrMetadata index.
    // Then the IndexBootstrapper will schedule the full reindex.
    try {
      clientFacade.deleteIndex(Index.create("sysmdattribute_c8d9a252"));
      LOG.info("The standard tokenizer got changed, will do a full reindex.");
    } catch (UnknownIndexException ignore) {
      LOG.info("Index not found, full index will be done already.");
    }
  }
}
