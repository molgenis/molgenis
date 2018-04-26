package org.molgenis.data.importer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.molgenis.util.UnexpectedEnumException;

import java.util.*;
import java.util.Map.Entry;

/**
 * Value object to store the {@link EntitiesValidationReport}.
 */
public class MyEntitiesValidationReport implements EntitiesValidationReport
{
	public enum AttributeState
	{
		/**
		 * Present in the source, known in the target.
		 */
		IMPORTABLE(true), /**
	 * Present in the source, unknown in the target
	 */
	UNKNOWN(true), /**
	 * Required in the target, missing in the source
	 */
	REQUIRED(false), /**
	 * Available in the target, missing in the source
	 */
	AVAILABLE(true);

		private final boolean valid;

		AttributeState(boolean valid)
		{
			this.valid = valid;
		}

		public boolean isValid()
		{
			return valid;
		}
	}

	private final Map<String, Boolean> sheetsImportable = new LinkedHashMap<>();
	private final Map<String, Collection<String>> fieldsImportable = new LinkedHashMap<>();
	private final Map<String, Collection<String>> fieldsUnknown = new LinkedHashMap<>();
	private final Map<String, Collection<String>> fieldsRequired = new LinkedHashMap<>();
	private final Map<String, Collection<String>> fieldsAvailable = new LinkedHashMap<>();
	private final List<String> importOrder = new ArrayList<>();
	private final List<String> packages = new ArrayList<>();
	private boolean valid = true;

	/**
	 * Creates a new report, with an entity added to it.
	 *
	 * @param entityTypeId name of the entity
	 * @param importable   true if the entity is importable
	 * @return this report
	 */
	public MyEntitiesValidationReport addEntity(String entityTypeId, boolean importable)
	{
		sheetsImportable.put(entityTypeId, importable);
		valid = valid && importable;
		if (importable)
		{
			fieldsImportable.put(entityTypeId, new ArrayList<>());
			fieldsUnknown.put(entityTypeId, new ArrayList<>());
			fieldsRequired.put(entityTypeId, new ArrayList<>());
			fieldsAvailable.put(entityTypeId, new ArrayList<>());
			importOrder.add(entityTypeId);
		}
		return this;
	}

	/**
	 * Creates a new report, with an attribute with state {@link AttributeState#IMPORTABLE} added to the last added
	 * entity;
	 *
	 * @return new {@link MyEntitiesValidationReport} with attribute added.
	 */
	public MyEntitiesValidationReport addAttribute(String attributeName)
	{
		return addAttribute(attributeName, AttributeState.IMPORTABLE);
	}

	/**
	 * Add a package to the report
	 */
	public MyEntitiesValidationReport addPackage(String pack)
	{
		packages.add(pack);
		return this;
	}

	/**
	 * Creates a new report, with an attribute added to the last added entity;
	 *
	 * @param attributeName name of the attribute to add
	 * @param state         state of the attribute to add
	 * @return this report
	 */
	public MyEntitiesValidationReport addAttribute(String attributeName, AttributeState state)
	{
		if (getImportOrder().isEmpty())
		{
			throw new IllegalStateException("Must add entity first");
		}
		String entityTypeId = getImportOrder().get(getImportOrder().size() - 1);
		valid = valid && state.isValid();
		switch (state)
		{
			case IMPORTABLE:
				addField(fieldsImportable, entityTypeId, attributeName);
				break;
			case UNKNOWN:
				addField(fieldsUnknown, entityTypeId, attributeName);
				break;
			case AVAILABLE:
				addField(fieldsAvailable, entityTypeId, attributeName);
				break;
			case REQUIRED:
				addField(fieldsRequired, entityTypeId, attributeName);
				break;
			default:
				throw new UnexpectedEnumException(state);
		}
		return this;
	}

	private void addField(Map<String, Collection<String>> sheets, String entityTypeId, String attributeName)
	{
		if (!sheets.containsKey(entityTypeId))
		{
			sheets.put(entityTypeId, new ArrayList<>());
		}
		sheets.get(entityTypeId).add(attributeName);
	}

	/**
	 * Returns true for importable sheets and false for unimportable sheets
	 */
	@Override
	public ImmutableMap<String, Boolean> getSheetsImportable()
	{
		return ImmutableMap.copyOf(sheetsImportable);
	}

	private static ImmutableMap<String, Collection<String>> getImmutableCopy(Map<String, Collection<String>> map)
	{
		Builder<String, Collection<String>> builder = ImmutableMap.builder();
		for (Entry<String, Collection<String>> entry : map.entrySet())
		{
			builder.put(entry.getKey(), ImmutableList.copyOf(entry.getValue()));
		}
		return builder.build();
	}

	/**
	 * lists per entity what fields can be imported
	 */
	@Override
	public ImmutableMap<String, Collection<String>> getFieldsImportable()
	{
		return getImmutableCopy(fieldsImportable);
	}

	/**
	 * lists per entity what fields cannot be imported
	 */
	@Override
	public ImmutableMap<String, Collection<String>> getFieldsUnknown()
	{
		return getImmutableCopy(fieldsUnknown);
	}

	/**
	 * lists per entity what fields should have been filled in
	 */
	@Override
	public ImmutableMap<String, Collection<String>> getFieldsRequired()
	{
		return getImmutableCopy(fieldsRequired);
	}

	/**
	 * lists per entity what fields could have been filled in but were not provided
	 */
	@Override
	public ImmutableMap<String, Collection<String>> getFieldsAvailable()
	{
		return getImmutableCopy(fieldsAvailable);
	}

	/**
	 * provides import order based on dependency
	 */
	@Override
	public ImmutableList<String> getImportOrder()
	{
		return ImmutableList.copyOf(importOrder);
	}

	@Override
	public List<String> getPackages()
	{
		return ImmutableList.copyOf(packages);
	}

	@Override
	public boolean valid()
	{
		return valid;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MyEntitiesValidationReport that = (MyEntitiesValidationReport) o;
		return valid == that.valid && Objects.equals(sheetsImportable, that.sheetsImportable) && Objects.equals(
				fieldsImportable, that.fieldsImportable) && Objects.equals(fieldsUnknown, that.fieldsUnknown) && Objects
				.equals(fieldsRequired, that.fieldsRequired) && Objects.equals(fieldsAvailable, that.fieldsAvailable)
				&& Objects.equals(importOrder, that.importOrder) && Objects.equals(packages, that.packages);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(sheetsImportable, fieldsImportable, fieldsUnknown, fieldsRequired, fieldsAvailable,
				importOrder, packages, valid);
	}
}
