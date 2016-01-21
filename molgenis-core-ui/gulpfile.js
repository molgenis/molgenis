var gulp = require('gulp');
var pomParser = require("pom-parser");
var fs = require("fs");

/**
 *  Writes the parent project version from pom.xml to the version property in package.json
 */
gulp.task('update-version', function (done) {
    // Parse the pom based on a path
    pomParser.parse({
        filePath: __dirname + "/pom.xml"
    }, function (err, pomResponse) {
        if (err) {
            console.log("ERROR: " + err);
        } else {
            writeVersionToPackageJson("package.json", pomResponse.pomObject.project.parent.version);
        }
        done();
    });
});

function writeVersionToPackageJson(file, version) {
    console.log('Writing pomVersion ' + version + ' to ' + file);
    var pkg = JSON.parse(getStringContentsOfFile(file));
    pkg.version = version;
    fs.writeFile(file, JSON.stringify(pkg, null, 2));
}

function getStringContentsOfFile(filename) {
    if (!fs.statSync(filename)) {
        this.error(filename + ' does not exist');
    }
    return fs.readFileSync(filename).toString();
}