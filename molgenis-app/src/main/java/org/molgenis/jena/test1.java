package org.molgenis.jena;

    import com.hp.hpl.jena.rdf.model.*;
    import org.apache.jena.riot.RDFDataMgr;
    import org.apache.jena.riot.RDFFormat;
    import org.molgenis.MolgenisFieldTypes;
    import org.molgenis.data.AttributeMetaData;
    import org.molgenis.data.Entity;
    import org.molgenis.data.support.DefaultAttributeMetaData;
    import org.molgenis.data.support.DefaultEntityMetaData;
    import org.molgenis.data.support.MapEntity;

    import java.io.File;
    import java.io.FileNotFoundException;
    import java.io.FileOutputStream;
    import java.util.ArrayList;
    import java.util.List;

    public class test1 extends Object {


        public static void main (String args[]) {
            List<Entity> entities = getEntities();
            Model model = ModelFactory.createDefaultModel();

            for(Entity entity : entities) {
                String ns = "http://www.molgenis.org/";
                for (AttributeMetaData metaData : entity.getEntityMetaData().getAtomicAttributes()) {
                    if (metaData.isIdAtrribute()) {
                        Resource subject = model.createResource(entity.getString(metaData.getName()));
                        Property predicate = model.createProperty(ns.concat("is_a"));
                        Resource object = model.createResource(ns.concat(entity.getEntityMetaData().getName()));

                        connect(subject, predicate, object, model);
                    } else {
                        Resource subject = model.createResource(entity.getString(entity.getEntityMetaData().getIdAttribute().getName()));
                        Property predicate = model.createProperty(ns.concat(metaData.getName()));
                        Resource object = model.createResource(ns.concat(entity.getString(metaData.getName())));

                        connect(subject, predicate, object, model);
                    }
                }
            }
            printTriples(model);
            exportTriples(model);
        }

    public static void printTriples(Model model) {
        StmtIterator iter = model.listStatements();
        if (iter.hasNext()) {
            System.out.println("The database contains vcards for:");
            while (iter.hasNext()) {
                System.out.println("  " + iter.nextStatement()
                        .toString() );
            }
        } else {
            System.out.println("No vcards were found in the database");
        }
    }


        public static void exportTriples(Model model) {
            File file = new File("rdf.txt");
            try {
                FileOutputStream fop = new FileOutputStream(file);
                RDFDataMgr.write(fop, model, RDFFormat.JSONLD) ;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

    public static List<Entity> getEntities() {
        DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("byod_entity");
        DefaultAttributeMetaData attributeMetaDataID = new DefaultAttributeMetaData("ID", MolgenisFieldTypes.FieldTypeEnum.HYPERLINK);
        attributeMetaDataID.setIdAttribute(true);
        DefaultAttributeMetaData attributeMetaDataField1 = new DefaultAttributeMetaData("Field1", MolgenisFieldTypes.FieldTypeEnum.STRING);
        DefaultAttributeMetaData attributeMetaDataField2 = new DefaultAttributeMetaData("Field2", MolgenisFieldTypes.FieldTypeEnum.STRING);
        DefaultAttributeMetaData attributeMetaDataField3 = new DefaultAttributeMetaData("Field3", MolgenisFieldTypes.FieldTypeEnum.STRING);

        entityMetaData.addAttributeMetaData(attributeMetaDataID);
        entityMetaData.addAttributeMetaData(attributeMetaDataField1);
        entityMetaData.addAttributeMetaData(attributeMetaDataField2);
        entityMetaData.addAttributeMetaData(attributeMetaDataField3);

        MapEntity entity1 = new MapEntity(entityMetaData);
        entity1.set("ID","http://www.molgenis.org/entity/1");
        entity1.set("Field1", "test1.1");
        entity1.set("Field2", "test2.1");
        entity1.set("Field3", "test3.1");
        MapEntity entity2 = new MapEntity(entityMetaData);
        entity2.set("ID","http://www.molgenis.org/entity/2");
        entity2.set("Field1", "test1.2");
        entity2.set("Field2", "test2.2");
        entity2.set("Field3", "test3.2");
        MapEntity entity3 = new MapEntity(entityMetaData);
        entity3.set("ID","http://www.molgenis.org/entity/3");
        entity3.set("Field1", "test1.3");
        entity3.set("Field2", "test2.3");
        entity3.set("Field3", "test3.3");

        List<Entity> entities = new ArrayList<Entity>();
        entities.add(entity1);
        entities.add(entity2);
        entities.add(entity3);
        return entities;
    }

    private static Statement connect(
                Resource subject,
                Property predicate,
                Resource object,
                Model model)
        {
            model.add(subject, predicate, object);
            return model.createStatement(subject, predicate, object);
        }
    }
