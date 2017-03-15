**
This section describes how to define data model migrations, for if you make changes to core data model
**

When you make changes to entity meta data you sometimes need to write migration code in order for updated MOLGENIS web apps to works correctly:

# When to use

1) Adding an attribute to an entity

```java
@Component
public class MyEntityMetaData extends DefaultEntityMetaData {
	...
	public MyEntityMetaData()
	{
		...
		addAttribute("newAttr");
	}
}
```
What to do? Nothing if the attribute is nillable or has a default value (MOLGENIS adds attribute automagically), else write a migration step.

2) Delete an attribute from an entity
```java
@Component
public class MyEntityMetaData extends DefaultEntityMetaData {
	...
	public MyEntityMetaData()
	{
		...
		addAttribute("attr");
	}
}
```
What to do? Write a migration step

3) Update an attribute from an entity
```java
@Component
public class MyEntityMetaData extends DefaultEntityMetaData {
	...
	public MyEntityMetaData()
	{
		...
		addAttribute("attr").setDataType(STRING);
		addAttribute("attr").setDataType(INT);
	}
}
```
What to do? Write a migration step

# Writing migration scripts

For data model changes that are required when updating MOLGENIS the molgenis-data-migrate module is used. Adding a migration scripts involves the following steps:

1. Increment MolgenisVersionService.CURRENT_VERSION
2. Create a class that extends MolgenisUpgrade and put it in the package based on the current maven version.
3. In molgenis-app register the migration step by adding it in WebAppConfig.addUpgrades

Using the DataService in Migration scripts
Migration steps cannot use the DataService since the web application has not finished initializing when the migration step is executed. The following example illustrates a workaround:

```java
public class StepXX extends MolgenisUpgrade {
	private final ExampleMigrationListener exampleMigrationListener;

	public StepXX(ExampleMigrationListener exampleMigrationListener) {
		...
		this.exampleMigrationListener = checkNotNull(exampleMigrationListener);
		...
	}

	RuntimePropertyToAppSettingsMigrator runtimePropertyToAppSettingsMigrator

	@Override
	public void upgrade()
	{
		exampleMigrationListener.enableMigrator()
	}
}

@Component
public class ExampleMigrationListener implements ApplicationListener<ContextRefreshedEvent> {
	private boolean enabled;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent)
	{
		// perform migration
	}

	public void enableMigrator()
	{
		this.enabled = true;
	}
}
```

Testing migration
Migration should be tested on a MOLGENIS server, since different servers of MySQL behave differently. Do not test locally.
