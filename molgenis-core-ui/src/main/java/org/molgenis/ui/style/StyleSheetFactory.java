package org.molgenis.ui.style;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StyleSheetFactory extends AbstractSystemEntityFactory<StyleSheet, StyleMetadata, String>
{
	@Autowired
	StyleSheetFactory(StyleMetadata styleMetadata, EntityPopulator entityPopulator)
	{
		super(StyleSheet.class, styleMetadata, entityPopulator);
	}
}
