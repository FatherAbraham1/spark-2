/*
 * Copyright 2016, Jerry Xiong, BOSSCS
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.bosscs.spark.mongodb.writer;

import java.util.List;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

/**
 * Created by Jerry Xiong on 5/02/16.
 */
public class MongoWriter implements AutoCloseable {

    /**
     * The Mongo client.
     */
    protected MongoClient mongoClient = null;
    /**
     * The Db collection.
     */
    protected DBCollection dbCollection = null;

    protected WriteConcern writeConcern = null;


    /**
     * Instantiates a new Mongo writer.
     * 
     * @param serverAddresses
     *            the server addresses
     * @param databaseName
     *            the database name
     * @param collectionName
     *            the collection name
     */
    public MongoWriter(List<ServerAddress> serverAddresses, String databaseName, String collectionName,
            WriteConcern writeConcern) {
        mongoClient = new MongoClient(serverAddresses);
        dbCollection = mongoClient.getDB(databaseName).getCollection(collectionName);
        this.writeConcern = writeConcern;
    }

    /**
     * Save void.
     * 
     * @param dbObject
     *            the db object
     */
    public void save(DBObject dbObject) {
        dbCollection.save(dbObject, writeConcern);
    }

    /**
     * Close void.
     */
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

}
