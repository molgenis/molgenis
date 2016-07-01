package org.molgenis.data.annotation;

import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.annotation.entity.impl.RepositoryAnnotatorImpl;

public class EffectsAnnotator extends RepositoryAnnotatorImpl {
    public EffectsAnnotator(String NAME) {
        super(NAME);
    }

    public void init(EntityAnnotator entityAnnotator){
        super.init(entityAnnotator);
    }
}
