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
We develop our own apps in VueJS so most documentation will point to the VueJS-configuration.

## Update your build configuration
You must update your build configuration to build a zip-file which you can upload in MOLGENIS. 

### webpack
In your webpack production build configuration

```js
  ...
  assetsRoot: path.resolve(__dirname, '../dist/static'),
  assetsSubDirectory: '',
  assetsPublicPath: '/apps/${app.id}',
  ...
```

## Update your language specific configuration
You have to update some configuration in your app to make it work in MOLGENIS. 

### VueJS

- Add the following code-snippet in src/main.js

```js
...
if (window.__webpack_public_path__) {
  /* eslint-disable no-undef, camelcase */
  __webpack_public_path__ = window.__webpack_public_path__
  /* eslint-enable */
}
...
```

- Add the following code-snippet in src/router/index.js

```js
...
const {baseUrl} = window.__INITIAL_STATE__ || {}

export default new Router({
  mode: 'history',
  base: baseUrl,
...
```     

## Create a freemarker-template
```html
<!DOCTYPE html><html>
  <head>
    <meta charset=utf-8><meta name=viewport content="width=device-width,initial-scale=1">
    <title>#molgenis-app#</title>
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

## Activate the app
When you uploaded the app you can activate it in the App store. It can now be used in the menu for exmaple.

- Go to Plugins --> App store
- Click on activate on the uploaded app
- Go to Admin --> Menu manager
- Create redirect plugin with an URL to the app
- Select the plugin and put it into the menu

