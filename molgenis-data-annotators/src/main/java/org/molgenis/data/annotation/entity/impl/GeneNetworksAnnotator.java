package org.molgenis.data.annotation.entity.impl;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AbstractRepositoryAnnotator;
import org.molgenis.data.annotation.CmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Status;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Type;
import org.molgenis.data.annotator.websettings.GeneNetworksAnnotatorSettings;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.STRING;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.CGDAttributeName.GENE;

// flow:
// 1) upload VCF
// 2) run snpEff to join the genename to the VCF
// 3) create new instance of the Examination entity through data explorer UI
// 4) run this annotator

@Configuration
public class GeneNetworksAnnotator {
    private static final Logger LOG = LoggerFactory.getLogger(GeneNetworksAnnotator.class);
    public static final String NAME = "GeneNetwork";

    public static final String PVALUE = "P_value";
    public static final String EXTRA = "extra";

    @Autowired
    private DataService dataService;

    @Autowired
    private Entity geneNetworksAnnotatorSettings;

    @Bean
    public RepositoryAnnotator geneNetwork() {
        return new GeneNetworksRepositoryAnnotator(geneNetworksAnnotatorSettings, dataService);
    }

    //FIXME: this whole thing is a POC and should not be used for any "real" work!!!!
    public static class GeneNetworksRepositoryAnnotator extends AbstractRepositoryAnnotator {
        private DataService dataService;
        private final Entity pluginSettings;
        private final AnnotatorInfo info = AnnotatorInfo.create(Status.READY, Type.EFFECT_PREDICTION, NAME,
                "Prototype for the gene networks", getOutputMetaData());

        public GeneNetworksRepositoryAnnotator(Entity pluginSettings, DataService dataService) {
            this.pluginSettings = pluginSettings;
            this.dataService = dataService;
        }

        @Override
        public AnnotatorInfo getInfo() {
            return info;
        }

        @Override
        public Iterator<Entity> annotate(Iterable<Entity> source) {
            Entity superEntity = dataService.findOne(GeneNetworksAnnotatorSettings.Meta.EXAM_ENTITY,
                    new QueryImpl().eq(GeneNetworksAnnotatorSettings.Meta.VARIANT_ENTITY_ATTRIBUTE,
                            source.iterator().next().getEntityMetaData().getName()));
            Iterable<Entity> phenotypeEntities = superEntity.getEntities("Phenotypes");
            List<String> phenotypes = parsePhenotypes(phenotypeEntities);
            Map<String, Map<String, String>> geneNetworkResults = getGeneNetworkResults(phenotypes);
            // query genenetworks for genes + info -> write to Map<GeneName,Map<key, value>>
            // iterate over repo and add info from map based on gene name
            Iterator<Entity> iterator = source.iterator();
            return new Iterator<Entity>() {
                @Override
                public boolean hasNext() {
                    boolean next = iterator.hasNext();
                    return next;
                }

                @Override
                public Entity next() {
                    Entity entity = iterator.next();
                    DefaultEntityMetaData meta = new DefaultEntityMetaData(entity.getEntityMetaData());
                    info.getOutputAttributes().forEach(meta::addAttributeMetaData);
                    Entity copy = new MapEntity(entity, meta);
                    Map<String, String> resultsForGene = geneNetworkResults.get(entity.getString(GENE.getAttributeName()));
                    for (String key : resultsForGene.keySet()) {
                        copy.set(key, resultsForGene.get(key));
                    }
                    return copy;
                }

            };
        }

        private Map<String, Map<String, String>> getGeneNetworkResults(List<String> phenotypes) {
            //add all data, keys should match the output attribute names
            return new HashMap<>();
        }

        private List<String> parsePhenotypes(Iterable<Entity> phenotypeEntities) {
            List<String> results = new ArrayList<>();
            for (Entity entity : phenotypeEntities) {
                String iri = entity.getString("ontologyIRI");
                String hpoTerm = iri.replace("http://purl.obolibrary.org/obo/HP_", "HP:");
                results.add(hpoTerm);
            }
            return results;
        }

        @Override
        public String canAnnotate(EntityMetaData repoMetaData) {
            long resultCount = dataService.count(GeneNetworksAnnotatorSettings.Meta.EXAM_ENTITY, new QueryImpl()
                    .eq(GeneNetworksAnnotatorSettings.Meta.VARIANT_ENTITY_ATTRIBUTE, repoMetaData.getName()));
            if (resultCount <= 0) {
                return "No matching entity found in: " + GeneNetworksAnnotatorSettings.Meta.EXAM_ENTITY + " for: "
                        + repoMetaData.getName();
            } else if (resultCount > 1) {
                return "more than one matching entity found in: " + GeneNetworksAnnotatorSettings.Meta.EXAM_ENTITY
                        + " for: " + repoMetaData.getName();
            }
            return super.canAnnotate(repoMetaData);
        }

        @Override
        public List<AttributeMetaData> getOutputMetaData() {
            List<AttributeMetaData> attributes = new ArrayList<>();

            DefaultAttributeMetaData annotation = new DefaultAttributeMetaData(PVALUE, STRING);
            annotation.setDescription("TODO");
            attributes.add(annotation);

            DefaultAttributeMetaData putative_impact = new DefaultAttributeMetaData(EXTRA, STRING);
            putative_impact.setDescription("TODO");
            attributes.add(putative_impact);

            DefaultAttributeMetaData compoundAttributeMetaData = new DefaultAttributeMetaData(this.getFullName(),
                    MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
            compoundAttributeMetaData.setLabel(this.getSimpleName());

            for (AttributeMetaData attributeMetaData : attributes) {
                compoundAttributeMetaData.addAttributePart(attributeMetaData);
            }

            return Collections.singletonList(compoundAttributeMetaData);
        }

        @Override
        public List<AttributeMetaData> getInputMetaData() {
            List<AttributeMetaData> attributes = new ArrayList<>();
            attributes.add(new DefaultAttributeMetaData(GENE.getAttributeName()));

            return attributes;
        }

        @Override
        public String getSimpleName() {
            return NAME;
        }

        @Override
        protected boolean annotationDataExists() {
            return dataService.hasRepository(pluginSettings.getString(GeneNetworksAnnotatorSettings.Meta.EXAM_ENTITY));
        }

        @Override
        public CmdLineAnnotatorSettingsConfigurer getCmdLineAnnotatorSettingsConfigurer() {
            throw new UnsupportedOperationException("Commandline version not yet implemented");
        }
    }

}
