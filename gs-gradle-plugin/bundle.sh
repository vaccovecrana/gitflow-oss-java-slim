# brew install coreutils
rm -rfv ~/.m2/repository/io/vacco/oss/gitflow
gradle clean sign publishToMavenLocal
cd ~/.m2/repository
pwd

tree .
rm -fv io/vacco/oss/gitflow/io.vacco.oss.gitflow.gradle.plugin/maven-metadata-local.xml
rm -fv io/vacco/oss/gitflow/io.vacco.oss.gitflow.gradle.plugin/1.8.0/io.vacco.oss.gitflow.gradle.plugin-1.8.0.module*
find . -type f -not -name "*.asc" -not -name "*.md5" -not -name "*.sha1" -exec sh -c 'md5sum  "$1" | awk "{print \$1}" > "$1.md5"' _ {} \;
find . -type f -not -name "*.asc" -not -name "*.md5" -not -name "*.sha1" -exec sh -c 'sha1sum "$1" | awk "{print \$1}" > "$1.sha1"' _ {} \;
tree .
rm -fv ~/Desktop/bundle.zip
zip -r ~/Desktop/bundle.zip .
unzip -l ~/Desktop/bundle.zip

echo 'Upload to Maven Central portal... sigh...'
