//package org.molgenis.data.index.meta;
//
//import java.util.HashMap;
//import java.util.Map;
//import org.junit.jupiter.api.Test;
//import org.molgenis.data.config.EntityBaseTestConfig;
//import org.molgenis.data.index.meta.IndexActionMetadata.IndexStatus;
//import org.molgenis.data.meta.AbstractSystemEntityTest;
//import org.molgenis.util.Pair;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.ContextConfiguration;
//
//@ContextConfiguration(
//    classes = {
//      EntityBaseTestConfig.class,
//      IndexActionMetadata.class,
//      IndexActionFactory.class,
//      IndexActionGroupMetadata.class,
//      IndexPackage.class
//    })
//public class IndexActionTest extends AbstractSystemEntityTest {
//
//  @Autowired IndexActionMetadata metadata;
//  @Autowired IndexActionFactory factory;
//
//  @Override
//  protected Map<String, Pair<Class, Object>> getOverriddenReturnTypes() {
//    Map<String, Pair<Class, Object>> map = new HashMap<>();
//
//    Pair<Class, Object> status = new Pair<>();
//    status.setA(IndexStatus.class);
//    status.setB(IndexStatus.FAILED);
//    map.put(IndexActionMetadata.INDEX_STATUS, status);
//
//    return map;
//  }
//
//  @SuppressWarnings("java:S2699") // Tests should include assertions
//  @Test
//  protected void testSystemEntity() {
//    internalTestAttributes(
//        metadata, IndexAction.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
//  }
//}
