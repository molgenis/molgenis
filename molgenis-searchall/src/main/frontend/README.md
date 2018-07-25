# molgenis-searchall

> Frontend code for the molgenis-searchall module


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

It should render a local version of the searchall plugin, however the mock data is not yet available here. It needs a running backend at the moment.

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
      }
    },
    // End of block
```

And comment out this block in the same file.

```javascript
// there is no mock data available in this module
```

That is it. Run a MOLGENIS instance on localhost:8080 and start the searchall plugin with:

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
