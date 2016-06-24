# MOLGENIS core UI

## Javascript
We use the [frontend maven plugin](https://github.com/eirslett/frontend-maven-plugin)
to allow maven to install node and run npm and other javascript tools.

The javascript files in and other static resources src/main/javascript are packed into bundles using webpack.

### Directory structure
```
|-- molgenis-core-ui
    |-- README.md // this file
    |-- gulpfile.js // javascript build actions, used to write version from pom.xml to package.json
    |-- package.json // describes what npm packages are used
    |-- pom.xml // the maven buildfile
    |-- webpack.config.js // webpack configuration, lists the entry points for the bundles and the loaders
    |-- webpack.test.config.js // webpack configuration for tape tests
    |-- node // node gets installed here by frontend maven plugin
    |   |-- node
    |   |-- npm
    |   |-- npm.cmd
    |-- node_modules // npm modules listed in package.json get downloaded to here by npm and read by webpack and node
    |   |-- [...]
    |-- src
    |   |-- main
    |   |   |-- [...]
    |   |   |-- javascript
    |   |   |   |-- molgenis-global-ui-webpack.js // bundle files specify the contents of the bundles
    |   |   |   |-- molgenis-global-webpack.js
    |   |   |   |-- molgenis-vendor-webpack.js
    |   |   |   |-- modules // contain the source files for the molgenis modules, interpreted by the babel loader, included in the bundles
    |   |   |   |   |-- i18n
    |   |   |   |   |   |-- I18nStrings.js
    |   |   |   |   |-- react-components
    |   |   |   |   |   |-- AggregateTable.js
    |   |   |   |   |   |-- AlertMessage.js
    |   |   |   |   |   |-- [...]
    |   |   |   |   |   |-- index.js
    |   |   |   |   |   |-- css // if css files are required by the javascript files in the module, webpack bundles them
    |   |   |   |   |   |   |-- Checkbox.css
    |   |   |   |   |   |   |-- [...]
    |   |   |   |   |   |   |-- wrapper
    |   |   |   |   |   |       |-- DateTimePicker.css
    |   |   |   |   |   |       |-- [...]
    |   |   |   |   |   |       |-- images
    |   |   |   |   |   |           |-- ui-bg_flat_0_aaaaaa_40x100.png // images included by the css files are bundled too
    |   |   |   |   |   |           |-- [...]
    |   |   |   |-- plugins // third party modules that are customized or not available in npm
    |   |   |       |-- jQEditRangeSlider-min.js 
    |   |   |       |-- jquery-ui-1.9.2.custom.min.js
    |   |   |       |-- select2-patched.js
    |   |-- test
    |       |-- [...]
    |       |-- javascript
    |       |   |-- ButtonTest.js // 
    |       |   |-- testsuite.js
    |       |   |-- __tests__
    |       |       |-- CheckboxWithLabel-test.js
    |-- target
        |-- json.stats // stats file produced by npm run-script 
        |-- molgenis-core-ui-1.16.0-SNAPSHOT.jar
        |-- tests.tap // test report produced by tape
        |-- classes
        |   |-- js
        |   |   |-- dist // bundles are written here by webpack
        |   |   |   |-- molgenis-global-ui.js
        |   |   |   |-- molgenis-global.js
        |   |   |   |-- molgenis-vendor-bundle.js
        |-- test-classes
            |-- js
            |   |-- dist // bundle is written here by webpack when you run npm test
            |       |-- test.bundle.js
            |       |-- test.bundle.js.map
```
 
### Webpack

#### stats
Stats file can be created using

    npm run-script webpack-stats

Stats file can be uploaded to http://webpack.github.io/analyse/


### Ace
Including all Ace editor modes takes 4Mb which is overly much.
If you miss an editor mode, add it to molgenis-vendor-webpack.js

### Gulp, versioning
We do our versioning in maven, see pom.xml.
However, npm also wants to know the version number of the modules you define.
So the gulpfile.js updates the package.json if the version numbers have run out of sync.