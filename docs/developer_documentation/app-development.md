# MOLGENIS App store

You can make your own app in MOLGENIS using the appstore.

You can perform the following steps to get your app in the App store.

- [Create a project](#create-a-project)
- [Update your build configuration](#update-build-configuration)
- [Update your language specific configuration](#update-your-language-specific-configuration)
- [Create a freemarker-template](#create-a-freemarker-template)
- [Upload your app](#upload-your-app)
- [Active the app](#active-the-app)

## Create a project
You can create a javascript project in a language you like. 
We develop our own apps in VUE so most documentation will point out the VUE-configuration.

## Update your build configuration
You must update build configuration to build a zip-file which you can upload in MOLGENIS. 
At the moment we only describe how to do this in webpack.

### webpack
In your config/index.js

```
 assetsRoot: path.resolve(__dirname, '../dist/static'),
 assetsSubDirectory: '',
 assetsPublicPath: '/apps/${app.id}',
 
 /**
  * Source Maps
  */
 
 productionSourceMap: true,
 // https://webpack.js.org/configuration/devtool/#production
 devtool: '#eval-source-map',
```

## Update your language specific configuration
You have to update some  configuration in your app to make it work in MOLGENIS. 
We now only describe how to do this in VUE.

### VUE

- Add the following code-snippet in src/main.js

```
if (window.__webpack_public_path__) {
  /* eslint-disable no-undef, camelcase */
  __webpack_public_path__ = window.__webpack_public_path__
  /* eslint-enable */
}
```

- Add the following code-snippet in src/router/index.js

```
...
const {baseUrl} = window.__INITIAL_STATE__ || {}

export default new Router({
  mode: 'history',
  base: baseUrl,
  routes: [
    {
      path: '/lifecycle',
      component: LifeCycleCatalogue
    },
...
```     
     
```
...
  {
    path: '/',
    redirect: '/lifecycle'
  }
...
```

## Create a freemarker-template
```
<!DOCTYPE html><html>
  <head>
    <meta charset=utf-8><meta name=viewport content="width=device-width,initial-scale=1">
    <script src=https://code.jquery.com/jquery-3.2.1.slim.min.js integrity=#hash# crossorigin=anonymous></script>
    <script src=https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js integrity=#hash# crossorigin=anonymous></script>
    <script src=https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js integrity=#hash# crossorigin=anonymous></script>
    <link rel=stylesheet href=https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css integrity=#hash# crossorigin=anonymous>
    <link rel=stylesheet href=https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css>
    <title>molgenis-app-lifecycle</title>
    <link href=/apps/${app.id}/css/app.#hash#.css rel=stylesheet>
  </head>
  <body>
    <script>
      var server = '#server placeholder#'
      window.__INITIAL_STATE__ = {
        baseUrl: '/menu/main/apps/${app.id}/',
        server: server,
        menuItems: [
          {'label': '#item 1#', 'link': server + '#link to item 1#'},
          {'label': '#item 2#', 'link': server + '#link to item 2#'}
         ]
       },
       __webpack_public_path__ = '/apps/${app.id}/'
    </script>
    <div id=app></div>
    <script type=text/javascript src=/apps/${app.id}/js/manifest.#hash#.js></script>
    <script type=text/javascript src=/apps/${app.id}/js/vendor.#hash#.js></script>
    <script type=text/javascript src=/apps/${app.id}/js/app.#hash#.js></script>
  </body>
</html> 
```
## Upload your app
You can now upload your app in MOLGENIS.

- Go to Plugins --> App store
- Add a new app
- Enter a name
- Upload the zip-file
- Add the freemarker-template to the app

## Active the app
When you uploaded the app you can activate it in the App store. It can now be used in the menu for exmaple.

- Go to Plugins --> App store
- Click on activate on the uploaded app
- Go to Admin --> Menu manager
- Select the app and put it into the menu

