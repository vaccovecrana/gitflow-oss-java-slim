# brew install coreutils

rm -rfv ~/.m2/repository/io/vacco/oss/gitflow
gradle clean sign publishToMavenLocal
cd ~/.m2/repository
pwd
rm -fv io/vacco/oss/gitflow/io.vacco.oss.gitflow.gradle.plugin/maven-metadata-local.xml
find . -type f -exec sh -c 'md5sum  "$1" > "$1.md5"' _ {} \;
find . -type f -exec sh -c 'sha1sum "$1" > "$1.sha"' _ {} \;
zip -r ~/Desktop/bundle.zip .

echo 'Upload to Maven Central portal... sigh...'
