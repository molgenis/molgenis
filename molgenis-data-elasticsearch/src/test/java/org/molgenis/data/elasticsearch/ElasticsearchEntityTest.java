package org.molgenis.data.elasticsearch;

import com.google.common.collect.Lists;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.*;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ElasticsearchEntityTest {

    ElasticsearchEntity entity;
    private DefaultEntityMetaData entityMetaData;
    private Entity refEntity;

    @BeforeMethod
    public void setUp() throws IOException
    {
        refEntity = mock(Entity.class);
        Map<String, Object> source = new HashMap<String, Object>();
        source.put("String","string");
        source.put("Entity",refEntity);
        source.put("Entity.id","ReferentieID");
        source.put("Label","label");
        source.put("Identifier","id");
        source.put("Integer",1);

        EntityMetaData refEntityMetaData = mock(EntityMetaData.class);
        AttributeMetaData refAttributeMetaDataMock = mock(AttributeMetaData.class);
        Repository referenenceRepository = mock(Repository.class, withSettings().extraInterfaces(Queryable.class));

        entityMetaData = new DefaultEntityMetaData("test");
        entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData("String", MolgenisFieldTypes.FieldTypeEnum.STRING));
        DefaultAttributeMetaData refAttributeMetaData = new DefaultAttributeMetaData("Entity", MolgenisFieldTypes.FieldTypeEnum.MREF);
        refAttributeMetaData.setRefEntity(refEntityMetaData);
        entityMetaData.addAttributeMetaData(refAttributeMetaData);
        DefaultAttributeMetaData idAttributeMetaData = new DefaultAttributeMetaData("Identifier",
                MolgenisFieldTypes.FieldTypeEnum.STRING);
        idAttributeMetaData.setIdAttribute(true);
        idAttributeMetaData.setVisible(false);
        entityMetaData.addAttributeMetaData(idAttributeMetaData);
        DefaultAttributeMetaData nameAttributeMetaData = new DefaultAttributeMetaData("Label",
                MolgenisFieldTypes.FieldTypeEnum.STRING);
        nameAttributeMetaData.setLabelAttribute(true);
        entityMetaData.addAttributeMetaData(nameAttributeMetaData);
        entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData("Integer", MolgenisFieldTypes.FieldTypeEnum.INT));

        DataService dataService = mock(DataService.class);
        entity = new ElasticsearchEntity("id", source, entityMetaData, dataService);

        when(refEntityMetaData.getName()).thenReturn("Referentie");
        when(refEntityMetaData.getIdAttribute()).thenReturn(refAttributeMetaDataMock);
        when(refAttributeMetaDataMock.getName()).thenReturn("ID");
        when(dataService.getRepositoryByEntityName("referentie")).thenReturn(referenenceRepository);
        QueryRule rule = new QueryRule("ID",
                QueryRule.Operator.EQUALS, "ReferentieID");
        QueryImpl q = new QueryImpl();
        q.addRule(rule);
        ArrayList refEntities = new ArrayList<Entity>();
        refEntities.add(refEntity);
        when(((Queryable) referenenceRepository).findAll(q)).thenReturn(refEntities);
    }

    @Test
    public void getEntityMetaData()
    {
         assertEquals(entity.getEntityMetaData(), entityMetaData);
    }

    @Test
    public void getAttributeNames()
    {

       assertTrue(Lists.newArrayList(entity.getAttributeNames()).containsAll(Arrays.asList(new String[]{"Entity", "String", "Label", "Integer", "Identifier"})));
    }

    @Test
    public void getIdValue()
    {
        assertEquals(entity.getIdValue(), "id");
    }

    @Test
    public void getLabelValue()
    {
        assertEquals(entity.getLabelValue(), "label");
    }

    @Test
    public void getLabelAttributeNames()
    {
        assertEquals(entity.getLabelAttributeNames(), Collections.singleton("Label"));
    }

    @Test
    public void getString()
    {
        assertEquals(entity.get("String"), "string");
    }

    @Test
    public void getInteger()
    {
        assertEquals(entity.get("Integer"), 1);
    }

    @Test
    public void getMREFEntity()
    {
        assertTrue(((ArrayList<Entity>) entity.get("Entity")).contains(refEntity));
        assertEquals(((ArrayList<Entity>) entity.get("Entity")).size(), 1);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void set()
    {
          entity.set("key","value");
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void setEntity()
    {
        entity.set(new MapEntity());
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void setEntityStrict()
    {
        entity.set(new MapEntity(), true);
    }
}
