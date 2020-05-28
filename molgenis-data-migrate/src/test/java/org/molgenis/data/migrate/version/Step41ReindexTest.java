package org.molgenis.data.migrate.version;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.data.elasticsearch.client.ClientFacade;
import org.molgenis.data.elasticsearch.generator.model.Index;

@ExtendWith(MockitoExtension.class)
class Step41ReindexTest {

  private Step41Reindex step41Reindex;
  @Mock private ClientFacade clientFacade;
  private final Index attributeIndex = Index.create("sysmdattribute_c8d9a252");

  @BeforeEach
  void setup() {
    step41Reindex = new Step41Reindex(clientFacade);
  }

  @Test
  void testUpgrade() {
    step41Reindex.upgrade();

    verify(clientFacade).deleteIndex(attributeIndex);
  }
}
