

# Confluent IIOT tracking App
The Android application targets to utilize and demonstrate the power and features of MongoDB atlas available in latest release 5.0 .
The android application has features such as time series collection, RealmDB, sync, triggers and push notifications.
We have utilised time series collection hosted on mongodb managed atlas cluster as a sink for confluent connector to store the stream data for stimulation of moving vehicles. The stream can be generated using python script in data_generator folder.

## Setup
Prerequisites to build the apk.

    1. Android Studio.
    2. Mongodb Atlas cluster with mongodb version 5.0 or higher.
    3. Firebase Account. (for Alerts and Push notifications).
    4. GCP cloud credentials for maps service and firebase service.


- #### Install android studio:
   Download and Install android studio from [here](https://developer.android.com/studio). Clone the repository and open the IoT/AndroidApp in android studio. Sync all the gradle dependencies.

- #### Configure MongoDB Atlas:
   Create collections in vehicle database:
  1. TrackingGeospatial: Holds the data of current location of tracked users also the details of users such as city, name etc.
  2. tracking-historic (Time series collection) - Live / Stimulated data is loaded to this collection. The data is generated from python script << git location >> and loaded using confluent connector for MongoDB Atlas.

- #### Configure Realm:
   We need following preconfigured in realm application to run the android application.
  * ##### Realm schema for TrackingGeospatial.
    TrackingGeospatial schema:

        {
           "title": "TrackingGeoSpatial",
           "bsonType": "object",
           "properties": {
              "_id": {
                 "bsonType": "objectId"
              },
              "Timestamp": {
                 "bsonType": "date"
              },
              "location": {
                 "bsonType": "object",
                 "properties": {
                    "coordinates": {
                       "bsonType": "array",
                       "items": {
                          "bsonType": "double"
                       }
                    },
                    "type": {
                       "bsonType": "string"
                    }
                 }
              },
              "partition_key": {
                 "bsonType": "string"
              },
              "reg_num": {
                 "bsonType": "string"
              },
              "city": {
                 "bsonType": "string"
              },
              "owner": {
                 "bsonType": "string"
              }
           }
        }  

    Create a realm application with following schema. Note: The "partition_key" can be set as per requirement depending upon use case please refer [here](https://docs.mongodb.com/realm/sync/partitions/). Verify the data model is generated for the schema by navigating to SDK on side pane of Realm UI.

  * ##### Webhooks :
    Create webhooks to access the time series collection data and the tracking collection for displaying all vehicles.

    Function 1: GetTimeline : Returns all coordinates for requested vehicle.

        // This function is the webhook's request handler.
         exports = function(payload, response) {
             const body = payload.body;
             console.log(payload.body);
             const doc = context.services.get("mongodb-atlas").db("vehicle").collection("tracking-historic").find(payload.query);
             return  doc;
         };

    Function 2 : GetLatestLocation : Returns latest location of all vehicles.

        // This function is the webhook's request handler.
          exports = function(payload, response) {
              const query = [
                {"$sort":{"Timestamp": -1}},
                {"$group":{
                  "_id":"$reg_num",
                  "reg_num":{"$first":"$reg_num"},
                  "Timestamp":{"$first":"$Timestamp"},
                  "lat":{"$first":"$lat"},
                  "lon":{"$first":"$lon"}
                  }
                },
                {"$project":{"_id":0}}];
              const doc = context.services.get("mongodb-atlas").db("vehicle").collection("tracking-historic").aggregate(query);
          return  doc;
          };


        Copy the webhook URLs to MyApplication class to their respective delarations.


   * ##### Triggers for database collection update:
        Create a trigger function to listen to the database change event. Function is configured to send push notifications to the application on change event on TrackingGeospatial collection.

             exports = function(changeEvent) {
               const { updateDescription, fullDocument } = changeEvent;
             
               const doc = context.services.get("mongodb-atlas").db("vehicle").collection("TrackingGeospatial").aggregate([
               {"$geoNear": {"near": { "type": 'Point', "coordinates": [12.97182, 77.59499] },"distanceField": 'dist',"maxDistance": 5000}}
               ]);
               context.services.get("gcm").send({
                 "to": "/topics/GeofenceTrigger",
                 "notification":{
                 "title":"Alert!!",
                 "body":String(doc)
               }
               });
               // const collection = context.services.get("mongodb-atlas").db("vehicle").collection("GeofenceViolation");
               // delete fullDocument['_id'];
               // collection.insertOne(fullDocument);
               return doc;
             
             };



* ##### Realm App id :
  Copy the app id to appid variable in MyApplication class.

* ##### GCP map token:
  Create google API_KEY for accessing maps service and paste it in AndroidManifest.xml file.

* ##### Firebase Account for push notifications:
  Create a Firebase account add the api and api_key to the push notification settings.


Start the sync by navigating to sync on side pane from realm UI. Follow the [documentation](https://docs.mongodb.com/realm/sync/get-started/) for more details

## Confluent Configuration
Follow the instruction in [here](https://github.com/AskMeiPaaS/iiot-hybrid-with-mongodb-confluent) to create a topic and MongoDBAtlasSink connector.

MongoDBAtlasSink connector configurations.


       {
          "name": "MongoDbAtlasSinkConnector_0",
          "config": {
             "connector.class": "MongoDbAtlasSink",
             "name": "MongoDbAtlasSinkConnector_0",
             "input.data.format": "JSON",
             "topics": "iiot_tracking",
             "connection.host": <MongoDB host>,
             "connection.user": <MongoDB username>,
             "database": <MongoDB db name>,
             "collection": <MongoAB collection name>",
             "timeseries.timefield": "Timestamp",
             "timeseries.timefield.auto.convert": "true",
             "timeseries.timefield.auto.convert.date.format": "yyyy-MM-dd'T'HH:mm:ss'Z'",
             "tasks.max": "1"
          }
       }      
##### Note:
Create the atlas cluster and confluent cluster in same region. The sample payload to time series collection is shown below.

#### Time series data format:
The Timestamp field should be of string format.

      {
      "Timestamp": "2021-09-19T14:51:16.331Z",
      "reg_num": "KA01A1111",
      "lat": 12.97182,
      "lon": 77.59499
      }

#### Geospatial data format:
The users should be registered before sending data to time series collection. The documents from time series collection will be pulled into geospatial collection using http webhooks at time of loading "TRACK ALL" vehicle page.

      {
         "partition_key": "security",
         "Timestamp": "2021-09-16T18:08:49.520Z"
         "city": "banglore",
         "location": {
            "coordinates": [12.9716, 77.5946],
            "type": "Point"
         },
         "owner": "Jane Doe",
         "reg_num": "KA01A1111"
      }

"For this collection a 2d index need to be created on location field."

## Caution:

The following use case uses MongoDB Atlas resources and Confluent Cloud that may be billable. To run the application we need a new Confluent Cloud environment, Kafka cluster, topics, API keys, as well as resources that have hourly charges like connectors. Also MongoDB Atlas with mongo version 5.0 may be chargeable under dedicated resources. To avoid unexpected charges, carefully evaluate the cost of resources before you start. After you are done running the application carefully destroy/pause all chargeable resources to avoid accruing hourly charges for services and verify that they have been deleted/paused.


## Reference:

1. [Kafka producer script](https://github.com/confluentinc/confluent-kafka-python)
2. [Confluent cloud setup](https://github.com/AskMeiPaaS/iiot-hybrid-with-mongodb-confluent)
3. [Realm http API setup](https://docs.mongodb.com/realm/services/http/)
4. [Push notification setup](https://docs.mongodb.com/realm/services/push-notifications/)
5. [MongoDB Trigger](https://docs.mongodb.com/realm/triggers/database-triggers/)
6. [Time series collection](https://docs.mongodb.com/manual/core/timeseries-collections/)
7. [Geospatial queries](https://docs.mongodb.com/manual/geospatial-queries/)