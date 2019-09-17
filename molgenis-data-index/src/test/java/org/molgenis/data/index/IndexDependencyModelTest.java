package org.molgenis.data.index;

import static com.google.common.collect.ImmutableSet.of;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

@ExtendWith(MockitoExtension.class)
class IndexDependencyModelTest {
  @Mock private EntityType entity0;
  @Mock private EntityType entity1;
  @Mock private EntityType entity2;
  @Mock private EntityType entity3;
  @Mock private EntityType entity4;

  @Test
  void testGetEntityTypesDependentOnDeepReferences() {
    when(entity0.getId()).thenReturn("0");
    when(entity1.getId()).thenReturn("1");
    when(entity2.getId()).thenReturn("2");
    when(entity3.getId()).thenReturn("3");
    when(entity4.getId()).thenReturn("4");

    when(entity1.getIndexingDepth()).thenReturn(1);
    when(entity2.getIndexingDepth()).thenReturn(1);
    when(entity3.getIndexingDepth()).thenReturn(3);
    when(entity4.getIndexingDepth()).thenReturn(1);

    List<EntityType> entityTypes = ImmutableList.of(entity0, entity1, entity2, entity3, entity4);

    addReferences(entity1, ImmutableList.of(entity0, entity3));
    addReferences(entity2, ImmutableList.of(entity1));
    addReferences(entity3, ImmutableList.of(entity2));
    addReferences(entity4, ImmutableList.of(entity0, entity3));
    addReferences(entity0, ImmutableList.of());

    IndexDependencyModel dependencyModel = new IndexDependencyModel(entityTypes);
    Set<String> dependencies = dependencyModel.getEntityTypesDependentOn("0").collect(toSet());
    assertEquals(of("1", "3", "4"), dependencies);
  }

  @Test
  void testGetEntityTypesDependentOnDeepExtension() {
    when(entity0.getId()).thenReturn("0");
    when(entity1.getId()).thenReturn("1");
    when(entity2.getId()).thenReturn("2");
    when(entity3.getId()).thenReturn("3");
    when(entity4.getId()).thenReturn("4");

    when(entity0.getIndexingDepth()).thenReturn(1);
    when(entity4.getIndexingDepth()).thenReturn(1);

    List<EntityType> entityTypes = ImmutableList.of(entity0, entity1, entity2, entity3, entity4);

    addReferences(entity0, ImmutableList.of());
    addReferences(entity1, ImmutableList.of());
    addReferences(entity2, ImmutableList.of(entity3));
    addReferences(entity3, ImmutableList.of());
    addReferences(entity4, ImmutableList.of(entity3));

    when(entity0.getExtends()).thenReturn(entity1);
    when(entity1.isAbstract()).thenReturn(true);
    when(entity1.getExtends()).thenReturn(entity2);
    when(entity2.isAbstract()).thenReturn(true);

    IndexDependencyModel dependencyModel = new IndexDependencyModel(entityTypes);
    Set<String> dependencies = dependencyModel.getEntityTypesDependentOn("3").collect(toSet());
    assertEquals(of("0", "4"), dependencies);
  }

  @Test
  void testGetEntityTypesDependentOnCircular() {
    when(entity0.getId()).thenReturn("0");

    when(entity0.getIndexingDepth()).thenReturn(1);

    List<EntityType> entityTypes = ImmutableList.of(entity0);

    addReferences(entity0, ImmutableList.of(entity0));

    IndexDependencyModel dependencyModel = new IndexDependencyModel(entityTypes);
    Set<String> dependencies = dependencyModel.getEntityTypesDependentOn("0").collect(toSet());
    assertEquals(of("0"), dependencies);
  }

  @Test
  void testGetEntityTypesDependentOnCircularZeroDepth() {
    when(entity0.getId()).thenReturn("0");

    when(entity0.getIndexingDepth()).thenReturn(0);

    List<EntityType> entityTypes = ImmutableList.of(entity0);

    addReferences(entity0, ImmutableList.of(entity0));

    IndexDependencyModel dependencyModel = new IndexDependencyModel(entityTypes);
    Set<String> dependencies = dependencyModel.getEntityTypesDependentOn("0").collect(toSet());
    assertEquals(of(), dependencies);
  }

  private void addReferences(EntityType referringEntity, List<EntityType> refEntities) {
    ImmutableList.Builder<Attribute> attributes = ImmutableList.builder();

    for (EntityType refEntity : refEntities) {
      Attribute attribute = Mockito.mock(Attribute.class);
      when(attribute.hasRefEntity()).thenReturn(true);
      when(attribute.getRefEntity()).thenReturn(refEntity);
      attributes.add(attribute);
    }

    when(referringEntity.getOwnAtomicAttributes()).thenReturn(attributes.build());
  }
}
