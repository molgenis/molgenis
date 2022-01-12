 package org.molgenis.data.index.meta;

 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.junit.jupiter.api.Test;
 import org.molgenis.data.config.EntityBaseTestConfig;
 import org.molgenis.data.index.meta.IndexActionMetadata.IndexStatus;
 import org.molgenis.data.meta.AbstractSystemEntityTest;
 import org.molgenis.jobs.model.JobExecutionMetaData;
 import org.molgenis.util.Pair;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;

 @ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      IndexActionMetadata.class,
      IndexActionFactory.class,
      IndexPackage.class
    })
 public class IndexActionTest extends AbstractSystemEntityTest {

  @Autowired IndexActionMetadata metadata;
  @Autowired IndexActionFactory factory;

  @Override
  protected Map<String, Pair<Class, Object>> getOverriddenReturnTypes() {
    Map<String, Pair<Class, Object>> map = new HashMap<>();

    Pair<Class, Object> status = new Pair<>();
    status.setA(IndexStatus.class);
    status.setB(IndexStatus.FAILED);
    map.put(IndexActionMetadata.INDEX_STATUS, status);

    return map;
  }

   @Override
   protected List<String> getExcludedAttrs() {
     List<String> attrs = new ArrayList<>();
     attrs.add(IndexActionMetadata.END_DATE_TIME);
     attrs.add(IndexActionMetadata.CREATION_DATE_TIME);
     return attrs;
   }

  @SuppressWarnings("java:S2699") // Tests should include assertions
  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata, IndexAction.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
 }
