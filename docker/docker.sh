#!/usr/bin/env sh
echo "JPortal Docker"

dockerCmd() {
  if command -v docker >/dev/null 2>&1; then
    echo "Using DOCKER..."
    docker "$@"
  elif command -v podman >/dev/null 2>&1; then
    echo "Using PODMAN..."
    podman "$@"
  else
    echo "Please install docker or podman!"
  fi
}

containerName() {
  NAME=$1
  NUM=$(($(docker ps -a| grep $NAME|wc -l) + 1))
  echo "${NAME}_${NUM}"
}

WORKDIR=$(pwd)
echo $WORKDIR
PROJECTNAME="jportal"
DATAPATH=$WORKDIR/data
DBPATH=$DATAPATH/db
DBNAME=$(containerName "${PROJECTNAME}_db")
SOLRPATH=$DATAPATH/solr
SOLRHOME=$DATAPATH/solr-home
SOLRCONF=$WORKDIR/solr/configsets
SOLRCORESPATH=$WORKDIR/solr/cores
SOLRNAME=$(containerName "${PROJECTNAME}_solr")
APPPATH=$DATAPATH/app
APPIMAGE="${PROJECTNAME}_app"
APPNAME=$(containerName "${PROJECTNAME}_app")
APPWAR=$WORKDIR/../jportal_webapp/build/libs/jportal_webapp-2.0.19-SNAPSHOT.war
APPPROPS=$WORKDIR/app/mycore.properties
APPDBCONF=$WORKDIR/app/persistence.xml
NETWORK="jportal-net"

if [ ! -d $DATAPATH ]; then
  mkdir -p $DATAPATH;
fi

buildApp() {
  cd app;
  dockerCmd build -t $APPIMAGE .
}

if ! dockerCmd images| grep "${APPIMAGE}" >/dev/null 2>&1; then
  echo "Build app image.."
  buildApp
fi

startSolr(){
  if [ ! -d $SOLRHOME ]; then
    cp -r $WORKDIR/solr-home $DATAPATH;
  fi
  dockerCmd run -d -p 8391:8983 -v $SOLRHOME:/opt/jpsolrhome -e SOLR_HOME=/opt/jpsolrhome --network $NETWORK --name $SOLRNAME solr:slim
}

#dockerCmd "$@"
#dockerCmd network create --driver bridge $NETWORK
#dockerCmd run -d -p 50000:5432 -v $DBPATH:/var/lib/postgresql/data -v $WORKDIR/init.sql:/docker-entrypoint-initdb.d/init.sql:Z --env-file $WORKDIR/db.env --network $NETWORK --name $DBNAME postgres:alpine
startSolr
#dockerCmd run -d -p 8080:8080 -p 8295:8000 -v $APPPATH:/mcrHome/jportal/data -v $APPWAR:/usr/local/tomcat/webapps/jportal.war -v $APPPROPS:/mcrHome/jportal/mycore.properties -v $APPDBCONF:/mcrHome/jportal/resources/META-INF/persistence.xml --env-file $WORKDIR/app.env --network $NETWORK --name $APPNAME $APPIMAGE