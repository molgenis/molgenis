# Guide to developing MOLGENIS Frontend



## Tools and compiling
When developing client code, you will need to following tools:
 - Node v8.9.0 (LTS version) and included NPM version
 - Yarn v0.24.5 or greater


## Developing
To start developing a MOLGENIS app, we encourage you to use the molgenis-vue-template.

If you do not have the vue-cli (version 3), please check [vue-cli](https://cli.vuejs.org/). 

If you have the vue-cli installed you can use the following steps to quickly install a working Vue template

```bash
cd packages/
vue init **#module-name#**
```

Then answer the questions to setup your frontend working directory:
1. Name: The name of the project. The metadata-manager was created with the name 'metadata-manager', as you might expect.
2. Description: A description of your project
3. author: Email adres or name of the author
4. vuex: This is used for state management
5. vue-router: This is used for routing
6. Flow: This is used for typing
7. MOLGENIS: And answering yes on the MOLGENIS plugin question will ensure that your compiled client code is placed inside the `target/classes` directory 

Inside the newly created frontend directory, the following three commands can be used to install, develop and test client code:

```bash
yarn install
yarn run dev
yarn run test
```

Third party dependencies can be added: 

```bash
yarn add <library_name>
```

Third party development dependencies can be added: 

```bash
yarn add --dev <library_name>
```

or removed:

```bash
yarn remove <library_name>
```

using yarn.

Yarn produces a `yarn.lock` file. 
Commit this file to your Git repository as it ensures future builds to use the versions that were used to create the client code.

When running your client code in development on port 8081, it will help to run the MOLGENIS locally on port 8080. 
The molgenis-vue-template comes with a proxy table that will redirect any REST calls to localhost:8080

## Some guidelines
Below you can find some guidelines + code examples for stuff that we view is standard when creating a MOLGENIS plugin.

**Use Bootstrap-VUE for your CSS[<sup>1</sup>](#guidelines-1)**
Install bootstrap-vue.

```
yarn add bootstrap-vue
```

Example component.
```html
<div class="container">
    <div class="row">
        <div class="col">
            <h1>Bootstrap grid system is amazing!</h1>
        </div>
    </div>
</div>
```

**Use Font awesome for icons[<sup>1</sup>](#guidelines-1)**


```html
<i class="fa fa-plus"></i>
```

**Write unit tests for mutations, actions, getters and other pure JS code**
```js

// utils.spec.js

import { swapArrayElements } from 'utils'

describe('swapArrayElements', () => {
    it('should swap the location of two objects in an array', () => {
        const array = [1, 2, 3, 4, 5]

        const actual = swapArrayElements(array, 2, 3)
        const expected = [1, 2, 4, 3, 5]

        expect(expected).to.deep.equal(actual)
    })
})
```

**Create named Vue components**
```js

// ComponentA.vue

<script>
    export default {
        name: 'component-A'
    }
</script>
```

**To allow theme changes to affect all specific color sets, use sass mixins and variables[<sup>1</sup>](#guidelines-1) when setting colors** 
```scss
<style lang="scss">
  @import "~variables";
  @import "~mixins";
  
   .some_class {
      background-color: $red;
    }
    
    .some_other_class {
      background-color: darken($red, 20%)
    }
</style>
```

**When using Vuex for your state, use TypeScript to add typing your state parameters[<sup>1</sup>](#guidelines-1)**
```js

// state.js
export type State = {
  message: ?string
  isUpdated: boolean
}

const state: State = {
  message: null,
  isUpdated: false
}
```

**Use i18n for labels**
```html
<button>{{ 'back-to-home-button' | i18n }}</button>
```

**Use the INITIAL_STATE object to load configuration from the server inside Freemarker templates**
```js

// index.html

window.__INITIAL_STATE__ = {
    baseUrl: '/',
    lng: 'en',
    fallbackLng: 'en'
}
```

**When writing actions or mutations for your store, use constants for the different types**
```js

// mutations.js
export default {
    'setMessage' (state, message) {
        state.message = message
    }
}
```

More to come...

<sup><a name="guidelines-1">1</a> Configuration is provided in the molgenis-vue-template</sup>

## Windows
To run yarn commands on a windows machine, you will have to open up a shell terminal as administrator