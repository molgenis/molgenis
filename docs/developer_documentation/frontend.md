**
This section describes how you can create a new front-end component
**

This example shows how to create a user interface plugin. We build on industry strandard [Spring MVC (model view controller) framework](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/mvc.html). We use Java to define the 'controller' and 'model' and use Freemarker for the 'view'.

# Controller
First the controller + model:

```java
@Controller
@RequestMapping(URI)
public class MyPluginController extends MolgenisPluginController
{
  public static final String ID = "myplugin";
  public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
    
  public MyPluginController()
  {
    super(URI);
  }
  
  @RequestMapping(method = RequestMethod.GET)
  public String init(Model model) throws Exception
  {
    model.addAttribute("my_property", "my_value");
    return "view-myplugin";
  }
}
```

# View
The layout of this is determined via a freemarker file matching the 'return' above:

```
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["myplugin.css"]>
<#assign js=["myplugin.js"]>
<@header css js/>
    <p>My plugin content here</p>
<@footer/>
```

# Reusing components
Via the myplugin.js you can use MOLGENIS standard UI components (Table, Form, Entity select etc.) that retrieve data via the REST API (see [REST reference](/developer_documentation/guide-rest). See components at [Github](https://github.com/molgenis/molgenis/tree/master//molgenis-core-ui/src/main/resources/js/component)

# Adding to your menu

Finally you can use this component within your menu (see [Customization guide](/user_documentation/guide-customize)) and give it permissions so users can see it (see [Admin guide](/user_documentation/guide-admin))

# Advanced topics

## Settings

Optionally, you can add settings to your plug-in. Settings associated with a plugin are managed through the settings cog icon on the top right of a plugin. Alternatively they can be managed through the Settings plugin together with application settings and other settings. The following example illustrates how to create a settings entity for your plugin:

```java
public interface ExamplePluginSettings
{
	String getTitle();

	void setTitle(String title);
}
```

The default implementation of this interface extends DefaultSettingsEntity and contains an inner class that describes the settings meta data by extending DefaultSettingsEntityMetaData. The plugin ID is used to associate these settings with a specific plugin.

```java
@Component
public class ExamplePluginDbSettings extends DefaultSettingsEntity implements ExamplePluginSettings
{
	private static final long serialVersionUID = 1L;

	private static final String ID = ExamplePluginController.ID;

	public ExamplePluginSettings()
	{
		super(ID);
	}

	@Component
	private static class Meta extends DefaultSettingsEntityMetaData
	{
		private static final String TITLE = "title";
		
		public Meta()
		{
			super(ID);
			setLabel("Example plugin settings");
			setDescription("Settings for the example plugin.");
		    addAttribute(TITLE).setDataType(STRING).setLabel("Title");
       }
	}
	
	@Override
	public String getTitle()
	{
		return getString(Meta.TITLE);
	}
	
	@Override
	public void setTitle(String title)
    {
		set(Meta.TITLE, title);
	}
}
```

### Using settings in Java
Settings entities are automatically registered and updated on application startup. Since setting beans are spring managed you can inject them into the plugin controller:

```java
@Controller
@RequestMapping(URI)
public class ExamplePluginController extends MolgenisPluginController
{
  private final ExamplePluginSettings examplePluginSettings;

  @Autowired
  public ExamplePluginController(ExamplePluginSettings examplePluginSettings) {
	super(URI);
	this.examplePluginSettings = checkNotNull(examplePluginSettings);
  }
}
```

### Using settings in Freemarker
Setting beans are automatically injected into the Model making them available in your view (e.g. Freemarker template) under the key plugin_settings. 

### Using settings in JavaScript
In JavaScript the settings for the current plugin can be retrieved as follows:
molgenis.getPluginSettings() which returns a promise. This allows the following in JavaScript plugin files:

```javascript
$.when( $, window.top.molgenis = window.top.molgenis || {}, molgenis.getPluginSettings()).then(
  function($, molgenis, settingsXhr) {
	...
  }
);
```


