####Table of contents

* [Introduction](#introduction)
* [Database XML](#db_xml)
  * [`<molgenis>`](#db_molgenis)
  * [`<module>`](#db_module)
  * [`<entity>`](#db_entity)
  * [`<field>`](#db_field)
  * [`<unique>`](#db_unique)
* [User interface XML](#ui_xml)
  * [`<molgenis>`](#ui_molgenis)
  * [`<menu>`](#ui_menu)
  * [`<form>`](#ui_form)
  * [`<plugin>`](#ui_plugin)

<a name="introduction"/>
# Introduction
This document is a hands-on guide for MOLGENIS application development.

MOLGENIS is a software generator + framework to generate rich database software to your specifications, including web user interfaces to manage and query your data, various database back ends to store your data, and programmatic interfaces to the R language and web services. You tell MOLGENIS what to generate using an data model and user interface model described in XML; at the push of a button MOLGENIS translates this model into SQL, Java and R program files. Also documentation is generated. While the standard generated MOLGENIS is sufficient for most data management needs, MOLGENIS also allows you to build on top and add your own handwritten software components that build on the auto-generated software. 

<a name="db_xml"/>

# Database XML format

<a name="db_molgenis"/>
## \<molgenis>
The `<molgenis>` element is the root of each MOLGENIS database definition file.

Example usage of the <molgenis> element:

```xml
<molgenis name="myfirstdb">
  <module name="mymodule">
.  <entity name="entity1">
. . <field name="f1" type="string" unique="true"/>
.  </entity>
  </module>
  ...
</molgenis>
```

###Attributes:
<table>
<tr><th>Attribute</th><th>Required</th><th>Description</th></tr>
<tr><td>name</td><td>required</td><td>Name of your MOLGENIS blueprint. This will be used by the generator to name the java packages that are generated. Example: name="name"</td><td></tr>
</table>

###Child elements
* One or more `<module>`

<a name="db_module"/>
## \<module>
The `<module>` element allows designers to group entities in packages which will show up in the generated documentation and in generated code package structure). Example usage:

```xml
<molgenis name="org.example">
.  <module name="module1">
. .   <description>This is my first module (will generate to package org.example.module1</description>
. . .  <entity name="entity1">
. . . . .  <field name="f1" type="string" unique="true"/>	
. . . . .  <field name="f2" type="string"/>
. . . . .  <field name="f3" type="string"/>
. . .  </entity>
. . .  <entity name="entity2">
. . . . .  <field name="f1" type="string" unique="true"/>	
. . . . .  <field name="f2" type="string"/>
. . . . .  <field name="f3" type="string"/>
. . .  </entity>
. .  </module>
</molgenis>
```

###Attributes
<table>
<tr><th>Attribute</th><th>Required</th><th>Description</th></tr>
<tr><td>name</td><td>required</td><td>Globally unique name for this module. This will be used to generate the package name and will be appended to the value of `<molgenis name="package">`.</td></tr>
</table>

###Child elements
* Zero or one `<description>`
* One or more `<entity>`

<a name="db_entity"/>
##\<entity>
The `<entity>` element defines the structure of one data entity and will result in a table in the database, and several Java classes. Example usage of the <entity> element:
```xml
<entity name="my_class">
.  <description>This is my first entity.</description>
.  <field name="name" type="string"/>
.  <field name="investigation" type="string"/>
.  <unique fields="name,investigation"/>
</entity>

<entity name="my_subclass" extends="my_class">
.  <description>This class extends my_class and will have fields name,investigation and otherField</description>
.  <field name="otherField"/>
</entity>
```

###Attributes
<table>
<tr><th>Attribute</th><th>Required</th><th>Description</th></tr>
<tr><td>name</td><td>required</td><td>Globally unique name for this entity (within this blueprint). Example: name="name"
</td><tr.
<tr><td>label</td><td></td><td>A user-friendly alias to show as form header (default: copied from name).  Example: label="Nice name"</td></tr>
<tr><td>extends</td><td></td><td>You can use inheritance to make your entity inherit the fields of its 'superclass'. Example: extends="other_entity"</td></tr>
<tr><td>abstract</td><td></td><td>You define what programmers call 'interfaces'. This are abstract objects that you can use as 'contract' for other entities to 'implement'. 
Example: abstract="true"</td></tr>
<tr><td>implements</td><td></td><td>You can use inheritance to make your entity inherit the fields of an 'interface' using implements and refering to 'abstract' entities. The implemented fields are copied to this entity. 
Example: implements="abstract_entity"</td></tr>
<tr>
<tr><td>xref_label</td><td></td><td>Defines what field should be used by lookup lists, i.e. xref fields to this entity (default: first non-auto unique field)</td>
<tr><td>decorator</td><td></td><td>You can add custom code to change the way entities are added, updated and removed. See the section on how to write a MappingDecorator plugin. Example: decorator="package.MyDecoratorClass"</td></tr>
</table>

###Child elements
 * Zero or one `<description>` to describe this entity; a description can contain xhtml.
 * Zero or more `<field>` that detail entity structure.
 * Zero or more `<unique>` indicating unique constraints on field(s).

###Notes
* Cascading deletes can be configured via FieldElement where you can set xref_cascade

<a name="db_field"/>
##\<field>
A `<field>` defines one property of an entity (i.e., a table column). 
Example usage of the `<field>` element:
```xml
<field name="field_name" description="this is my first field of type string"/>
<field name="field_name" type="autoid" description="this is a id field, unique autonum integer"/>
<field name="field_name" type="xref" xref_entity="other_entity"
       description="this is a crossrerence to otherentity"/>
<field name="field_name" type="enum" enum_options="[option1,option2]"
       description="this is field of type enum"/>
```

###Attributes:
<table>
<tr><th>Attribute</th><th>Required</th><th>Description</th></tr>
<tr><td>name</td><td>required</td><td>Locally unique name for this entity (within this entity). Example: name="name"</td></tr>
<tr><td>type</td><td></td><td>Define the type of data that can be stored in this field (default: string). Examples:
<ul>
   <li> type="autoid":  auto incremented column (useful for entity ID).
   </li><li> type="string": a single line text string of variable length, max 255 chars.
   </li><li> type="int": a natural number.
   </li><li> type="boolean": a boolean.
   </li><li> type="decimal": a decimal number.
   </li><li> type="date": a date.
   </li><li> type="datetime": a date that includes the time.
   </li><li> type="file": attaches a file to the entity.
   </li><li> type="text": a multiline textarea of max 2gb.
   </li><li> type="xref": references to a field in another entity specified by xref_field attribute (required for xref). This will be shown as variable lookup-list.
   </li><li> type="mref": many-to-many references to a field in another entity specified by xref_field attribute (required for mref). This will be shown as multiple select lookup-list. (Under the hood, a link table is generated)
   </li><li> type="enum": references to a fixed look-up list options, specificed by enum_options attribute (required for enum)
</ul>
</td></tr>
<tr><td>label</td><td></td><td>A user-friendly alias to show as form header (default: copied from name). Example: label="Nice entity name"</td></tr>
<tr><td>unique</td><td></td><td>Defines if values of this field must be unique within the entity (default: "false"). Example: unique="true"</td></tr>
<tr><td>nillable</td><td></td><td>Definies if this field can be left without value (default: "false"). Example: nillable="true"</td></tr>
<tr><td>readonly</td><td></td><td>Defines if this field cannot be edited once they are saved (default: "false"). Example: readonly="true"</td></tr>
<tr><td>length</td><td></td><td>Limits the length of a string to 1<=n<=255 (default: "255"). Example: length="12"</td></tr>
<tr><td>xref_entity</td><td>when type="xref"</td><td>Specifies a foreign key to the entity that this xref field must reference to. Example: xref_entity="OtherEntity"</td></tr>
<tr><td>xref_cascade</td><td></td><td>This will enable cascading deletes which means that is the related element is deleted this entity will be deleted as well (default: "false"). Example: xref_cascade="true"</td></tr>
<tr><td>enum_options</td><td>when type="enum"</td><td>The fixed list of options for this enum (required for enum). Example: enum_options="[value1,value2]"</td></tr>
<tr><td>description</td><td></td><td>Describes this field. This will be visibible to the user in the UI when (s)he mouses over the field or visits the documentation pages. Example: description="One line description"</td></tr>
<tr><td>default</td><td></td><td>Sets a default value for this field. This value is automatically filled in for this field unless the user decides otherwise. Example: default="Pre-filling"</td></tr>
<tr><td>hidden</td><td></td><td>Optional settings to hide field from view. This requires the fields to be nillable="true" or auto="true" or default!=""</td></tr>
</table>

###Child elements
none.

<a name="db_unique"/>
##\<unique>
A `<unique>` defines which properties of an entity (i.e., table columns) should be unique. There are two ways to make a field unique.

__A single column is unique__. This example below shows that field "f1" is defined unique via unique="true". This means that there cannot be two entity instances - two rows in table entity1 - with the same value “x” in the f1 column.

```xml
<molgenis name="example">	
  <entity name="entity1">
    <field name="f1" unique="true"/>	
    <field name="f2" />
    <field name="f3" />
  </entity>	
</molgenis>
```

__A combination of two or more columns is unique__. The example below shows that the combination of field “f1” and “f2” is defined as unique via the <unique> element. This means that there cannot be two entity instances - two rows in table entity1 - with the same value “x” in the f1 AND f2 column paired.
```xml
<molgenis name="example">	
  <entity name="entity1">
    <field name="f1" />	
    <field name="f2" />
    <field name="f3" />
    <unique fields="f1,f2"/>
  </entity>	
</molgenis>
```

###Attributes
<table>
<tr><th>Attribute</th><th>Required</th><th>Description</th></tr>
<tr><td>fields</td><td>required</td><td>Comma separated enumeration of the unique fields. Example: fields="field1,field2"</td></tr>
</table>

###Child elements
none.

<a name="ui_xml"/>
# User Interface XML format

<a name="ui_molgenis"/>
##\<molgenis>
The `<molgenis>` element is the root of the MOLGENIS user interface definition file.

Example usage of the <molgenis> element:


```xml
<molgenis name="my.package">
	<menu name="my_mainmenu">
		<form name="myfirsttab" entity="an_entity1" />
		<menu name="my_submenu">
			<form name="mythirdtab" entity="an_entity2" />
			<form name="myfourthab" entity="an_entity3" />
		</menu>
	</menu>
</molgenis>	
```

###Attributes:
<table>
<tr><th>Attribute</th><th>Required</th><th>Description</th></tr>
<tr><td>name</td><td>required</td><td>Name of your MOLGENIS blueprint. This will be used by the generator to name the java package in which the user interface is generated. Example: name="name"</td><td></tr>
<tr><td>label</td><td></td><td>Label of your MOLGENIS system. This will be shown on the screen. Example: label="My first MOLGENIS"</td></tr>
</table>

###Child elements
* Zero or more `<menu>` elements to denote subscreen(s).
* Zero or more `<form>` elements to denote subscreen(s).
* Zero or more `<plugin>` elements to denote subscreen(s).

<a name="ui_menu"/>
##\<menu>
The `<menu>` element allows the design of a hierarchical user interface with a menu on the left of the user interface and/or in tabs for each contained subscreen (menu, form, plugin). 

Usage example of the `<menu>` element:

```xml
<molgenis name="my.package">
	<menu name="my_mainmenu">
		<form name="myfirsttab" entity="an_entity1" />
		<menu name="my_submenu">
			<form name="mythirdtab" entity="an_entity2" />
			<form name="myfourthab" entity="an_entity3" />
		</menu>
	</menu>
</molgenis>	
```

###Attributes
<table>
<tr><th>Attribute</th><th>Required</th><th>Description</th></tr>
<tr><td>name</td><td>required</td><td>Locally unique name for this screen element. Example: name="menuname"</td></tr>
<tr><td>startwith</td><td></td><td>Subscreen tab that is selected when menu is first shown (default: first subscreen). Example: startswith="mysubelement"</td></tr>
<tr><td>position</td><td></td><td>Position where the menu is shown, either `top_left`, `top_right` or `left` (default: top_left unless wrapped in a parent menu having other position). Example: position="top_left"</td></tr>
</table>

###Child elements
 * Zero or more `<menu>` elements to denote subscreen(s).
 * Zero or more `<form>` elements to denote subscreen(s).
 * Zero or more `<plugin>` elements to denote subscreen(s).

<a name="ui_form"/>
##\<form>
The `<form>` element is used to define a user element that shows the records of a certain entity on screen (including insert, update, save, search, etc). A form may have tabbed `<menu>` or un-tabbed `<form>` or `<plugin>` subscreens which are defined by nesting other user interface elements. 

Example usage of `<form>` element:
```xml
<form name="myname" entity="myentity">
  <form name="myname" entity="mysubentity" sortby="name"/>
</form>
	
<form name="myname" entity="myentity" viewtype="list" limit="10"/>
```

###Attributes
<table>
<tr><th>Attribute</th><th>Required</th><th>Description</th></tr>
<tr><td>name</td><td>required</td><td>Locally unique name for this screen element (within its container). Example: name="name"</td><tr>
<tr><td>entity</td><td>required</td><td>Which data entity will be loaded (i.e., points to a <entity name="myentity"/>).  Example entity="myentity"</td></tr>
<tr><td>label</td><td></td><td>A user-friendly alias to show as form header (default: copied from name).  Example: label="Nice screen name"</td></tr>
<tr><td>header</td><td></td><td>A user-friendly title for the screen that is displayed when this form is selected (default: copied from label). Example: header="Nice screen title"</td><tr>
<tr><td>viewtype</td><td></td><td>Whether the form should start with a list or per-record, either 'record' or 'list' (default: "record"). Example: viewtype="record"</td></td>
<tr><td>sortby</td><td></td><td>On what field the data should be sorted on default (default: first unique field/autoid). Example: sortby="aFieldInEntity"</td></td>
<tr><td>limit</td><td></td><td>How many records must be shown in the list (default: "5"). Example: limit="10"</td></tr>
<tr><td>readonly</td><td></td><td>Can the records be edited or is the form readonly (default: "false"). Example: readonly="true"</td></tr>
<tr><td>compact_view</td><td></td><td>When in 'recordview' only show the selected fields on the form and have a 'show additional fields' button to expand to see all fields. Example: compact_view="field1,field2"</td></tr>
<tr><td>commands</td><td></td><td>Optional extension point to add custom commands to the generated forms. See section on "Custom commands in generated forms". Example: commands="package.Class1,package.Class2"</td></tr>
<tr><td>hide_fields</td><td></td><td>Optional setting to hide fields from view. This requires the fields that are hidden to be nillable="true" or auto="true" or default!="" (so no constraints are violated if the user tries to save the entity). Example: hide_fields="field1,field2"</td></tr>
</table>

###Child elements
 * Zero or more ```<menu>``` elements to denote subscreen(s).
 * Zero or more ```<form>``` elements to denote subscreen(s). __Nested forms are automatically linked to the containing form based on foreign key (xref) relationships.__
 * Zero or more ```<plugin>``` elements to denote subscreen(s).

<a name="ui_plugin"/>
##\<plugin>
The `<plugin>` element allows to plug-in custom screen elements into the MOLGENIS user interface next to the auto-generated `<form>` and `<menu>` elements. The implementation of how to add your own logic to the plug-in is described in the MolgenisPluginGuide.

Example usage:
```xml
<plugin name="myplugin" type="package.path.ClassName"/>
```

**When running the generator, a Java class for logic is automatically created, as well as a FreemarkerTemplate file for layout'. Its location and name is denoted by 'type' (and if it already exists this step is skipped).**

<table>
<tr><th>Attribute</th><th>Required</th><th>Description</th></tr>
<tr><td>name</td><td>required</td><td>Globally unique name for this entity (within this blueprint). Example: name="name"</td></td>
<tr><td>type</td><td>required</td><td>Reference to a java class that implements this plugin. Example: type=”package.path.ClassName”</td></tr>
<tr><td>label</td><td></td><td>User-friendly alias to show as form header (default: copied from name). Example: label="Nice screen name"</td></tr>
</table>

###Child elements
none.





