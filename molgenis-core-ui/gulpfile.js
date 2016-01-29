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
            done(err);
        } else if (!fs.statSync('package.json')) {
            done('package.json does not exist');
        } else {
            writeVersionToPackageJson("package.json", pomResponse.pomObject.project.parent.version);
            done();
        }
    });
});

function writeVersionToPackageJson(file, version) {
    var pkg = JSON.parse(fs.readFileSync(file).toString());
    if(pkg.version != version){
        console.log('Writing pomVersion ' + version + ' to ' + file);
        pkg.version = version;
        fs.writeFile(file, JSON.stringify(pkg, null, 2));
    }
}