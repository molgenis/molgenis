package org.molgenis.data.importer;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.molgenis.framework.db.EntitiesValidationReport;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;

/**
 * Value object to store the {@link EntitiesValidationReport}.
 */
@AutoValue
public abstract class ImmutableEntitiesValidationReport implements EntitiesValidationReport
{
	public enum AttributeState
	{
		/**
		 * Present in the source, known in the target.
		 */
		IMPORTABLE(true),
		/**
		 * Present in the source, unknown in the target
		 */
		UNKNOWN(false),
		/**
		 * Required in the target, missing in the source
		 */
		REQUIRED(false),
		/**
		 * Available in the target, missing in the source
		 */
		AVAILABLE(true);

		private boolean valid;

		private AttributeState(boolean valid)
		{
			this.valid = valid;
		}

		public boolean isValid()
		{
			return valid;
		}
	}

	/**
	 * Creates an empty {@link EntitiesValidationReport}.
	 */
	public static ImmutableEntitiesValidationReport createNew()
	{
		return new AutoValue_ImmutableEntitiesValidationReport(ImmutableMap.<String, Boolean> of(),
				ImmutableMap.<String, Collection<String>> of(), ImmutableMap.<String, Collection<String>> of(),
				ImmutableMap.<String, Collection<String>> of(), ImmutableMap.<String, Collection<String>> of(),
				ImmutableList.<String> of(), true);
	}

	/**
	 * Creates a new report, with an entity added to it.
	 * 
	 * @param entityName
	 *            name of the entity
	 * @param importable
	 *            true if the entity is importable
	 * @return new {@link ImmutableEntitiesValidationReport} with entity added
	 */
	public ImmutableEntitiesValidationReport addEntity(String entityName, boolean importable)
	{
		if (importable)
		{
			// add empty sheets for this entity
			return new AutoValue_ImmutableEntitiesValidationReport(addToMap(getSheetsImportable(), entityName, importable), addEmptyList(getFieldsImportable(), entityName), addEmptyList(getFieldsUnknown(), entityName),
					addEmptyList(getFieldsRequired(), entityName), addEmptyList(getFieldsAvailable(), entityName), add(getImportOrder(), entityName), valid() && importable);
		}
		else
		{
			return new AutoValue_ImmutableEntitiesValidationReport(addToMap(getSheetsImportable(), entityName, importable),
					getFieldsImportable(), getFieldsUnknown(), getFieldsRequired(), getFieldsAvailable(), add(
							getImportOrder(), entityName), valid() && importable);
		}
	}

	private static ImmutableMap<String, Collection<String>> addEmptyList(
			ImmutableMap<String, Collection<String>> immutableMap, String entityName)
	{
		return ImmutableMap.<String, Collection<String>> builder().putAll(immutableMap)
				.put(entityName, ImmutableList.<String> of()).build();
	}

	/**
	 * Creates a new report, with an attribute with state {@value AttributeState#IMPORTABLE} added to the last added
	 * entity;
	 * 
	 * @param attributeName
	 * @return new {@link ImmutableEntitiesValidationReport} with attribute added.
	 */
	public ImmutableEntitiesValidationReport addAttribute(String attributeName)
	{
		return addAttribute(attributeName, AttributeState.IMPORTABLE);
	}

	/**
	 * Creates a new report, with an attribute added to the last added entity;
	 * 
	 * @param attributeName
	 *            name of the attribute to add
	 * @param state
	 *            state of the attribute to add
	 * @return new {@link ImmutableEntitiesValidationReport} with attribute added
	 */
	public ImmutableEntitiesValidationReport addAttribute(String attributeName, AttributeState state)
	{
		if (getImportOrder().size() == 0)
		{
			throw new IllegalStateException("Must add entity first");
		}
		String entityName = getImportOrder().get(getImportOrder().size() - 1);
		switch (state)
		{
			case IMPORTABLE:
				return new AutoValue_ImmutableEntitiesValidationReport(getSheetsImportable(), add(
						getFieldsImportable(), entityName, attributeName), getFieldsUnknown(), getFieldsRequired(),
						getFieldsAvailable(), getImportOrder(), state.isValid() && valid());
			case UNKNOWN:
				return new AutoValue_ImmutableEntitiesValidationReport(getSheetsImportable(), getFieldsImportable(),
						add(getFieldsUnknown(), entityName, attributeName), getFieldsRequired(), getFieldsAvailable(),
						getImportOrder(), state.isValid() && valid());
			case AVAILABLE:
				return new AutoValue_ImmutableEntitiesValidationReport(getSheetsImportable(), getFieldsImportable(),
						getFieldsUnknown(), getFieldsRequired(), add(getFieldsAvailable(), entityName, attributeName),
						getImportOrder(), state.isValid() && valid());
			case REQUIRED:
				return new AutoValue_ImmutableEntitiesValidationReport(getSheetsImportable(), getFieldsImportable(),
						getFieldsUnknown(), add(getFieldsRequired(), entityName, attributeName), getFieldsAvailable(),
						getImportOrder(), state.isValid() && valid());
			default:
				throw new IllegalArgumentException();
		}
	}

	/**
	 * Utility method to add a value to one of the immutable sheet lists.
	 * 
	 * @param original
	 *            Map to add to
	 * @param key
	 *            key of the collection to add the value to
	 * @param value
	 *            value to add
	 * @return {@link ImmutableMap} with {@value} added to {@key}'s collection
	 */
	public static <K, V> ImmutableMap<K, Collection<V>> add(Map<K, Collection<V>> original, K key, V value)
	{
		Map<K, Collection<V>> map = new LinkedHashMap<K, Collection<V>>(original);
		map.put(key, add(original.get(key), value));
		return ImmutableMap.<K, Collection<V>> copyOf(map);
	}

	/**
	 * Utility method to add a value to create an empty immutable sheet list.
	 * 
	 * @param original
	 *            Map to add to
	 * @param key
	 *            key of the collection to create
	 * @return {@link ImmutableMap} with a new empty collection for {@key}.
	 */
	public static <K, V> ImmutableMap<K, Collection<V>> add(Map<K, Collection<V>> original, K key)
	{
		Map<K, Collection<V>> map = new LinkedHashMap<K, Collection<V>>(original);
		map.put(key, ImmutableList.<V> of());
		return ImmutableMap.<K, Collection<V>> copyOf(map);
	}

	/**
	 * Utility method to add a value to an immutable map.
	 * 
	 * @param original
	 *            Map to add to
	 * @param key
	 *            key of the collection to add the value to
	 * @param value
	 *            value to add
	 * @return {@link ImmutableMap} with {@value} added to {@key}'s collection
	 */
	public static <K, V> ImmutableMap<K, V> addToMap(Map<K, V> original, K key, V value)
	{
		return ImmutableMap.<K, V> builder().putAll(original).put(key, value).build();
	}

	/**
	 * Utility method to add value to immutable collection.
	 * 
	 * @param original
	 *            Map to add to
	 * @param key
	 *            key of the collection to add the value to
	 * @param value
	 *            value to add
	 * @return {@link ImmutableMap} with {@value} added to {@key}'s collection
	 */
	public static <V> ImmutableList<V> add(Collection<V> original, V value)
	{
		Builder<V> builder = ImmutableList.<V> builder();
		if (original != null)
		{
			builder.addAll(original);
		}
		return builder.add(value).build();
	}

	/**
	 * Adds an attribute that is in the target but not in the source.
	 * 
	 * @param entityName
	 *            name of the entity
	 * @param attributeName
	 *            name of the attribute
	 * @param required
	 *            true if the attribute is required in the target, false if it isn't
	 * @return {@link ImmutableEntitiesValidationReport} with target attribute added
	 */
	public ImmutableEntitiesValidationReport addTargetAttribute(String entityName, String attributeName,
			boolean required)
	{
		return new AutoValue_ImmutableEntitiesValidationReport(getSheetsImportable(), getFieldsImportable(),
				getFieldsUnknown(), getFieldsRequired(), getFieldsAvailable(),
				ImmutableList.<String> copyOf(getImportOrder()), valid() && !required);
	}

	/** Returns true for importable sheets and false for unimportable sheets */
	@Override
	public abstract ImmutableMap<String, Boolean> getSheetsImportable();

	/** lists per entity what fields can be imported */
	@Override
	public abstract ImmutableMap<String, Collection<String>> getFieldsImportable();

	/** lists per entity what fields cannot be imported */
	@Override
	public abstract ImmutableMap<String, Collection<String>> getFieldsUnknown();

	/** lists per entity what fields should have been filled in */
	@Override
	public abstract ImmutableMap<String, Collection<String>> getFieldsRequired();

	/** lists per entity what fields could have been filled in but were not provided */
	@Override
	public abstract ImmutableMap<String, Collection<String>> getFieldsAvailable();

	/** provides import order based on dependency */
	@Override
	abstract public ImmutableList<String> getImportOrder();

	@Override
	abstract public boolean valid();

}
