package org.molgenis.core.ui.style;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class StyleSheetFactory extends AbstractSystemEntityFactory<StyleSheet, StyleSheetMetadata, String>
{
	StyleSheetFactory(StyleSheetMetadata styleSheetMetadata, EntityPopulator entityPopulator)
	{
		super(StyleSheet.class, styleSheetMetadata, entityPopulator);
	}
}
