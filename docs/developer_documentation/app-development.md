# Creating apps for MOLGENIS
To be able to host your app inside MOLGENIS, you need to create an archive in a specific format.

To get started right away, you can download an [example archive here](../data/example-app.zip).

Following paragraphs will explain in detail how to construct and configure your resources to make it a certified MOLGENIS app.

## Archive structure
If you downloaded the example archive, you probably noticed that the structure of the archive is pretty straight forward.

```
|-- config.json
|-- index.html
|-- js
    |-- example.js
|-- css
    |-- example.css
```

The index.html is your main entry point, and the contents will be interpreted and rendered by a FreeMarker engine. 
Relative to your index.html you can include js and css. 

##### Note
If you use absolute paths, MOLGENIS is unable to serve your resources properly

### Example
The following example assumes we have a setup according to the tree structure shown in the previous paragraph.

_index.html_
```html
<link href="css/example.css"
<div class="container">
    <h1>This is an example of including resources with relative paths</h1>
</div>
<script src="js/example.js"></script>
```

_example.js_
```js
alert('this is a wonderful example')
```

_example.css_
```css
.container {
    background: blue;
    height: 400px;
    width: 100%
}
```

## Configuration
The config.json is a small configuration file that will tell MOLGENIS about your app. 
It contains the following parameters

| Parameter | type | mandatory | Description |
| ----------|-------------|-------------|-------------|
| label     |string| yes| The label used to name your app in the app manager screen |
| description |string| yes | A description for your app. Useful for making your app more findable in the app manager |
| version | string |yes |The version of your app. Used to manage app version, updating etc etc.. |
| apiDependency | string |yes | The version of the MOLGENIS REST api you used in your app. Can be used to give warnings about possible incompatibility with a specific version of MOLGENIS |
| uri | string |yes | the URL on which the app will be deployed once activated. Best if you make it a simple word without weird characters |
| includeMenuAndFooter | boolean |yes | If you want to use the MOLGENIS menu, you can this value to __true__ |
| runtimeOptions | object |no | A map containing initial parameters you might want to give to your app on init |

### Example
The following example shows every possible configuration option currently available

_config.json_
```json
{
	"label": "Example app",
	"description": "This is an example app",
	"version": "v1.0.0",
	"apiDependency": "v2",
	"uri": "example",
	"includeMenuAndFooter": true,
	"runtimeOptions": {
		"language": "en"
	}
}
```

_Note that you can not create a config.json on your root path of the project. It will not build anymore._



## Available javascript variables
MOLGENIS deploys apps with a bit of system information inside a global variable to get your app up and running. These are described below.

```js
window.__INITIAL_STATE__ = {
  baseUrl: '', // This is the root URL
  lng: '', // the language set by the system, allows you to implement i18n
  fallbackLng: '' // the fallback language set by the system, defaults to english (en)
}
```

## App resource caching
To enable caching of your resources, ensure your resource files have a hash code included in their file names.
So instead of _example.js_, use _example.0943Ajd09834fd.js_.

Build tools like webpack already generate your source files with a hash code included in the file name.

## Using webpack for rapid app prototyping
To use webpack in rapid prototyping of your MOLGENIS apps, use the standard webpack template 
and make some small tweaks to make it a `build -> compress -> upload` workflow.

Make sure that you set the following two parameters in your production config
```js
module.exports = {
  // ...
  build: {
    // ...
    assetsSubDirectory: '',
    assetsPublicPath: ''
    // ...
  }
}
```
These settings will ensure that your compiled resources are already in the correct file structure, and references to your resources are relative.

##### Note
If you set `includeMenuAndFooter: true`, you will have to edit your `index.html` to only include 
the contents of your body. So remove all `<head>`, `<body`, and `<html>` tags, and move your included resources as body content. The example archive contains an `index.html` as a good example
