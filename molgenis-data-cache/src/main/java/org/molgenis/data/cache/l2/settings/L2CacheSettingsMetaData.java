package org.molgenis.data.cache.l2.settings;

import org.molgenis.data.cache.l2.settings.L2CacheSettings.ValueReferenceType;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataMetaData;
import org.molgenis.data.system.model.RootSystemPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;
import static org.molgenis.fieldtypes.StringField.DURATION_REGEX;

@Component
public class L2CacheSettingsMetaData extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "L2CacheSettings";
	public static final String L2CACHE_SETTINGS = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	static final String ID = "id";
	static final String CACHED_ENTITY = "entityName";
	static final String CONCURRENCY_LEVEL = "concurrencyLevel";
	static final String INITIAL_CAPACITY = "initialCapacity";
	static final String MAXIMUM_SIZE = "maximumSize";
	static final String EXPIRE_AFTER_ACCESS = "expireAfterAccess";
	static final String EXPIRE_AFTER_WRITE = "expireAfterWrite";
	static final String REFRESH_AFTER_WRITE = "refreshAfterWrite";
	static final String WEAK_KEYS = "weakKeys";
	static final String VALUE_REFERENCE_TYPE = "valueReferenceType";
	static final String RECORD_STATS = "recordStats";

	private final RootSystemPackage rootSystemPackage;
	private final EntityMetaDataMetaData entityMetaDataMetaData;

	@Autowired
	public L2CacheSettingsMetaData(RootSystemPackage rootSystemPackage, EntityMetaDataMetaData entityMetaDataMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
		this.rootSystemPackage = requireNonNull(rootSystemPackage);
		this.entityMetaDataMetaData = requireNonNull(entityMetaDataMetaData);
	}

	@Override
	public void init()
	{
		setLabel("L2 cache settings");
		setPackage(rootSystemPackage);
		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false)
				.setDescription("automatically generated internal id, only for internal use.");
		addAttribute(CACHED_ENTITY).setDescription("Name of the entity whose L2 Cache settings these are.")
				.setDataType(XREF).setRefEntity(entityMetaDataMetaData).setNillable(true).setUnique(true);
		addAttribute(CONCURRENCY_LEVEL).setDataType(INT).setRangeMin(0L)
				.setDescription("Guides the allowed concurrency among update operations.").setNillable(false)
				.setDefaultValue("4");
		addAttribute(INITIAL_CAPACITY).setDataType(INT).setRangeMin(0L)
				.setDescription("Sets the minimum total size for the internal hash tables.").setDefaultValue("16")
				.setNillable(false);
		addAttribute(MAXIMUM_SIZE).setDataType(LONG).setRangeMin(0L).setNillable(true);
		addAttribute(EXPIRE_AFTER_ACCESS).setValidationExpression(getDurationValidationExpression(EXPIRE_AFTER_ACCESS))
				.setDescription(
						"Specifies that each entry should be automatically removed from the cache once a fixed duration has "
								+ "elapsed after the entry's creation, the most recent replacement of its value, or its last "
								+ "access. Durations are represented by an integer, followed by one of \"d\", \"h\", \"m\", or "
								+ "\"s\", representing days, hours, minutes, or seconds respectively.")
				.setNillable(true);
		addAttribute(EXPIRE_AFTER_WRITE).setValidationExpression(getDurationValidationExpression(EXPIRE_AFTER_WRITE))
				.setDescription("Specifies that each entry should be automatically removed from "
						+ "the cache once a fixed duration has elapsed after the entry's creation, or the most recent "
						+ "replacement of its value. Durations are represented by an integer, followed by one of "
						+ "\"d\", \"h\", \"m\", or \"s\", representing days, hours, minutes, or seconds respectively.")
				.setNillable(true);
		addAttribute(REFRESH_AFTER_WRITE).setValidationExpression(getDurationValidationExpression(REFRESH_AFTER_WRITE))
				.setDescription(
						"Specifies that active entries are eligible for automatic refresh once a fixed duration has elapsed "
								+ "after the entry's creation, or the most recent replacement of its value. Durations are "
								+ "represented by an integer, followed by one of \"d\", \"h\", \"m\", or \"s\", representing "
								+ "days, hours, minutes, or seconds respectively.").setNillable(true);
		addAttribute(WEAK_KEYS).setDataType(BOOL).setNillable(false).setDefaultValue("false").setDescription(
				"Specifies that each key (not value) stored in the cache should be wrapped in a WeakReference "
						+ "(by default, strong references are used).");
		addAttribute(VALUE_REFERENCE_TYPE).setDataType(ENUM).setNillable(false).setEnumOptions(ValueReferenceType.class)
				.setDefaultValue("Strong").setDescription("Specifies how references to cached values are stored. "
				+ "By default, strong references are used. Weak means that a WeakReference is used. "
				+ "Soft means that SoftReference is used. Softly-referenced objects will be garbage-collected in a "
				+ "globally least-recently-used manner, in response to memory demand.");
		addAttribute(RECORD_STATS).setDataType(BOOL).setNillable(false).setDefaultValue("true");
	}

	private static String getDurationValidationExpression(String attributeName)
	{
		return "$('" + attributeName + "').isNull().or($('" + attributeName + "').matches(" + DURATION_REGEX
				+ ")).value()";
	}
}
