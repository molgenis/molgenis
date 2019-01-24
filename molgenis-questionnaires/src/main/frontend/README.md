# molgenis-questionnaire-ui

> Frontend code for the molgenis-questionnaire module


## Develop frontend-plugins for MOLGENIS
There are 2 ways to test and develop in frontend-plugins for MOLGENIS.

- locally without MOLGENIS
- locally with MOLGENIS

### Test locally without a running MOLGENIS instance

For local testing you can execute the following commands:

```bash
# To install the application
yarn install

# To run develop mode
yarn dev
```

It will render a local version of the questionnaires.

#### Run unit tests
You can run unit tests by executing this command:

```bash
# Run once
yarn unit

# Run in watch-mode
yarn debug
```

#### Run end-to-end tests
You can run edn-to-end test locally by running the following command:

```bash
yarn e2e
```

### Test with a running MOLGENIS instance

For local testing with a running MOLGENIS instance you have to alter the config of the app:

Comment in the following block

```src/main/frontend/config/index.js```

```javascript
module.exports = {
  dev: {

    // Paths
    assetsSubDirectory: 'static',
    assetsPublicPath: '/',
    // Beginning of block
    proxyTable: {
      '/login': {
        target: 'http://localhost:8080'
      },
      '/api': {
        target: 'http://localhost:8080'
      },
      '/menu/plugins/questionnaires': {
        target: 'http://localhost:8080'
      }
    },
    // End of block
```

And comment out this block in the same file.


```javascript
/**
 * GET and POST interceptors
 * Removes the need for a running backend during development
 */

//before (app) {
//  app.get('/menu/plugins/questionnaires/list', function (req, res) {
//    res.json(questionnaireList)
//  })

//  app.get('/api/v2/i18n/questionnaire/en', function (req, res) {
//    res.json(localizedMessages)
//  })

//  app.get('/menu/plugins/questionnaires/start/questionnaire_1', function (req, res) {
//    res.json('OK')
//  })

//  app.get('/menu/plugins/questionnaires/start/questionnaire_2', function (req, res) {
//    res.json('OK')
//  })

//  app.get('/api/v2/questionnaire_1', function (req, res) {
//    res.json(firstQuestionnaireResponse)
//  })

//  app.get('/api/v2/questionnaire_2', function (req, res) {
//    res.json(secondQuestionnaireResponse)
//  })

//  app.get('/api/v2/questionnaire_3', function (req, res) {
//    res.json(thirdQuestionnaireResponse)
//  })

//  app.put('/api/v1/*', function (req, res) {
//    res.json('OK')
//  })

//  app.post('/api/v2/*', function (req, res) {
//    res.json('OK')
//  })

//  app.get('/menu/plugins/questionnaires/submission-text/*', function (req, res) {
//    res.json('<h1>Thank you</h1>')
//  })
//}
```

That is it. Run a molgenis instance on localhost:8080 and start the questionnaire with:

```javascript
yarn dev
```

## Build for MOLGENIS production

You need to add the following code snipper in your plugin **pom.xml**.

```
   <build>
        <plugins>
            <plugin>
                <groupId>com.github.eirslett</groupId>
                <artifactId>frontend-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
```

That way MOLGENIS integrates the frontend build with the MOLGENIS package.
