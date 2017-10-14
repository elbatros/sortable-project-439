#!/usr/bin/env bash
echo "Sortable Coding Take-Home Project by Anas H. Sulaiman"

set -e

# Check Java [https://stackoverflow.com/a/7335524]
echo "Checking Java..."
if type -p java; then
    #echo "Found java executable in PATH"
    _java=java
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    #echo "Found java executable in JAVA_HOME"
    _java="$JAVA_HOME/bin/java"
else
    echo "Java is not available to this script"
    echo "Please ensure at least JDK8 is available to this script before running it"
    echo "You may install OpenJDK8 from [http://openjdk.java.net/install/]"
    exit 1
fi

#echo "Checking Java version..."
version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
#echo version "$version"
if [[ "$version" < "1.8" ]]; then        
    echo "Found java $version, but at least 1.8 is required"
    echo "Please ensure at least JDK8 is available to this script before running it"
    echo "You may install OpenJDK8 from [http://openjdk.java.net/install/]"
    exit 1
fi
echo "Java is good"

mkdir -p build
mkdir -p workspace

echo "Checking dependencies..."
if [ ! -f "workspace/json.jar" ]; then
    echo "json.jar library is required, but was not found in workspace directory"
    # Maven: [https://mvnrepository.com/artifact/org.json/json/20170516]
    echo "downloading from [http://central.maven.org/maven2/org/json/json/20170516/json-20170516.jar]"
    wget -q -O 'workspace/json.jar' 'http://central.maven.org/maven2/org/json/json/20170516/json-20170516.jar'
fi

echo "Verifying dependencies..."
if sha1sum 'workspace/json.jar' | grep -q '949abe1460757b8dc9902c562f83e49675444572'; then
    echo "Dependencies are good"
else
    echo "json.jar is invalid"
    echo "Please remove it from workspace directory and run again"
    exit 1
fi

echo "Compiling project sources..."
find ./src -name "*.java" > sources.txt
javac -cp "./workspace/json.jar" -d "./build" @sources.txt
rm sources.txt
echo "Compilation done"

echo "Running the project..."
if [ ! -f "workspace/products.txt" ]; then
    echo "products.txt was not found in workspace directory."
    echo "Downloading project input files..."
    wget -q -O 'workspace/challenge_data.tar.gz' 'https://s3.amazonaws.com/sortable-public/challenge/challenge_data_20110429.tar.gz'
    tar -zxf 'workspace/challenge_data.tar.gz' -C 'workspace/'
fi

java -cp "./build:./workspace/json.jar" pro.sulaiman.sortable.Main 'workspace/products.txt' 'workspace/listings.txt'
curl -XPOST -F file=@results.txt https://challenge-check.sortable.com/validate
