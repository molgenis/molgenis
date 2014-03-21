/* 
 * 
 * generator:   org.molgenis.generators.db.EntitiesImporterGen 4.0.0-testing
 *
 * 
 * THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
 */
package org.molgenis.omx.importer;

import java.util.Set;

import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.importer.EntityImportService;
import org.molgenis.framework.db.AbstractEntitiesImporter;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

@Component
public class EntitiesImporterImpl extends AbstractEntitiesImporter
{
	/** importable entity names (lowercase) */
	private static final Set<String> ENTITIES_IMPORTABLE;

	static
	{
		// entities added in import order
		ENTITIES_IMPORTABLE = Sets.newLinkedHashSet();
		ENTITIES_IMPORTABLE.add("characteristic");
		ENTITIES_IMPORTABLE.add("observationtarget");
		ENTITIES_IMPORTABLE.add("observablefeature");
		ENTITIES_IMPORTABLE.add("category");
		ENTITIES_IMPORTABLE.add("protocol");
		ENTITIES_IMPORTABLE.add("dataset");
		ENTITIES_IMPORTABLE.add("observationset");
		ENTITIES_IMPORTABLE.add("value");
		ENTITIES_IMPORTABLE.add("boolvalue");
		ENTITIES_IMPORTABLE.add("categoricalvalue");
		ENTITIES_IMPORTABLE.add("datevalue");
		ENTITIES_IMPORTABLE.add("datetimevalue");
		ENTITIES_IMPORTABLE.add("decimalvalue");
		ENTITIES_IMPORTABLE.add("emailvalue");
		ENTITIES_IMPORTABLE.add("htmlvalue");
		ENTITIES_IMPORTABLE.add("hyperlinkvalue");
		ENTITIES_IMPORTABLE.add("intvalue");
		ENTITIES_IMPORTABLE.add("longvalue");
		ENTITIES_IMPORTABLE.add("mrefvalue");
		ENTITIES_IMPORTABLE.add("stringvalue");
		ENTITIES_IMPORTABLE.add("textvalue");
		ENTITIES_IMPORTABLE.add("observedvalue");
		ENTITIES_IMPORTABLE.add("individual");
		ENTITIES_IMPORTABLE.add("panel");
		ENTITIES_IMPORTABLE.add("xrefvalue");
		ENTITIES_IMPORTABLE.add("sample");
	}

	public EntitiesImporterImpl(FileRepositoryCollectionFactory fileRepositorySourceFactory,
			EntityImportService entityImportService)
	{
		super(fileRepositorySourceFactory, entityImportService);
	}

	@Override
	protected Set<String> getEntitiesImportable()
	{
		return ENTITIES_IMPORTABLE;
	}
}