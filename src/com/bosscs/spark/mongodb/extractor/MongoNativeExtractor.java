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

package com.bosscs.spark.mongodb.extractor;

import static com.bosscs.spark.commons.utils.Utils.initConfig;
import static com.bosscs.spark.commons.utils.Utils.removeAddressPort;
import static com.bosscs.spark.mongodb.utils.UtilMongoDB.MONGO_DEFAULT_ID;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.spark.Partition;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryBuilder;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.bosscs.spark.commons.config.BaseConfig;
import com.bosscs.spark.commons.config.JobConfig;
import com.bosscs.spark.commons.config.ExtractorConfig;
import com.bosscs.spark.commons.exception.GenericException;
import com.bosscs.spark.commons.impl.HadoopPartition;
import com.bosscs.spark.commons.querybuilder.UpdateQueryBuilder;
import com.bosscs.spark.commons.rdd.TokenRange;
import com.bosscs.spark.commons.rdd.IExtractor;
import com.bosscs.spark.commons.utils.Pair;
import com.bosscs.spark.mongodb.config.MongoDeepJobConfig;
import com.bosscs.spark.mongodb.partition.MongoPartition;
import com.bosscs.spark.mongodb.reader.MongoReader;
import com.bosscs.spark.mongodb.writer.MongoWriter;

/**
 * Created by Jerry Xiong on 7/02/16.
 *
 * @param <T> the type parameter
 * @param <S> the type parameter
 */
public abstract class MongoNativeExtractor<T, S extends BaseConfig> implements IExtractor<T, S> {

    /**
     * The constant SPLIT_KEYS.
     */
    public static final String SPLIT_KEYS = "splitKeys";

    /**
     * The constant serialVersionUID.
     */
    private static final long serialVersionUID = -4020891863696443624L;

    /**
     * The Split size.
     */
    private int splitSize = 10;

    /**
     * The constant MONGO_DEFAULT_ID.
     */
    /**
     * The Reader.
     */
    private MongoReader reader;

    /**
     * The Writer.
     */
    private MongoWriter writer;

    /**
     * The Mongo deep job config.
     */
    protected MongoDeepJobConfig<T> mongoDeepJobConfig;

    @Override
    public Partition[] getPartitions(S config) {
        MongoClient mongoClient = null;

        try {

            mongoDeepJobConfig = initConfig(config, mongoDeepJobConfig);

            DBCollection collection;
            ServerAddress address = new ServerAddress(mongoDeepJobConfig.getHost());

            List<ServerAddress> addressList = new ArrayList<>();
            addressList.add(address);
            mongoClient = new MongoClient(addressList);

            //mongoClient.setReadPreference(ReadPreference.nearest());
            DB db = mongoClient.getDB(mongoDeepJobConfig.getDatabase());
            collection = db.getCollection(mongoDeepJobConfig.getCollection());
            return isShardedCollection(collection) ? calculateShardChunks(collection) : calculateSplits(collection);
        } catch (UnknownHostException e) {

            throw new GenericException(e);
        } finally {
            if (mongoClient != null) {
                mongoClient.close();
            }

        }
    }


    /**
     * Is sharded collection.
     *
     * @param collection the collection
     * @return the boolean
     */
    private boolean isShardedCollection(DBCollection collection) {

        DB config = collection.getDB().getMongo().getDB("config");
        DBCollection configCollections = config.getCollection("collections");

        DBObject dbObject = configCollections.findOne(new BasicDBObject(MONGO_DEFAULT_ID, collection.getFullName()));
        return dbObject != null;
    }

    /**
     * Gets shards.
     *
     * @param collection the collection
     * @return the shards
     */
    private Map<String, String[]> getShards(DBCollection collection) {
        DB config = collection.getDB().getSisterDB("config");
        DBCollection configShards = config.getCollection("shards");

        DBCursor cursorShards = configShards.find();

        Map<String, String[]> map = new HashMap<>();
        while (cursorShards.hasNext()) {
            DBObject currentShard = cursorShards.next();
            String currentHost = (String) currentShard.get("host");
            int slashIndex = currentHost.indexOf("/");
            if (slashIndex > 0) {
                map.put((String) currentShard.get(MONGO_DEFAULT_ID),
                        currentHost.substring(slashIndex + 1).split(","));
            }
        }
        return map;
    }

    /**
     * Gets chunks.
     *
     * @param collection the collection
     * @return the chunks
     */
    private DBCursor getChunks(DBCollection collection) {
        DB config = collection.getDB().getSisterDB("config");
        DBCollection configChunks = config.getCollection("chunks");
        return configChunks.find(new BasicDBObject("ns", collection.getFullName()));
    }

    /**
     * Calculate splits.
     *
     * @param collection the collection
     * @return the deep partition [ ]
     */
    private HadoopPartition[] calculateSplits(DBCollection collection) {

        BasicDBList splitData = getSplitData(collection);
        List<ServerAddress> serverAddressList = collection.getDB().getMongo().getServerAddressList();

        if (splitData == null) {
            Pair<BasicDBList, List<ServerAddress>> pair = getSplitDataCollectionShardEnviroment(getShards(collection),
                    collection.getDB().getName(),
                    collection.getName());
            splitData = pair.left;
            serverAddressList = pair.right;
        }

        Object lastKey = null; // Lower boundary of the first min split

        List<String> stringHosts = new ArrayList<>();

        for (ServerAddress serverAddress : serverAddressList) {
            stringHosts.add(serverAddress.toString());
        }
        int i = 0;

        MongoPartition[] partitions = new MongoPartition[splitData.size() + 1];

        for (Object aSplitData : splitData) {

            BasicDBObject currentKey = (BasicDBObject) aSplitData;

            Object currentO = currentKey.get(MONGO_DEFAULT_ID);

            partitions[i] = new MongoPartition(mongoDeepJobConfig.getRddId(), i, new TokenRange(lastKey,
                    currentO, stringHosts), MONGO_DEFAULT_ID);

            lastKey = currentO;
            i++;
        }
        QueryBuilder queryBuilder = QueryBuilder.start(MONGO_DEFAULT_ID);
        queryBuilder.greaterThanEquals(lastKey);
        partitions[i] = new MongoPartition(0, i, new TokenRange(lastKey, null, stringHosts), MONGO_DEFAULT_ID);
        return partitions;
    }

    /**
     * Gets split data.
     *
     * @param collection the collection
     * @return the split data
     */
    private BasicDBList getSplitData(DBCollection collection) {

        final DBObject cmd = BasicDBObjectBuilder.start("splitVector", collection.getFullName())
                .add("keyPattern", new BasicDBObject(MONGO_DEFAULT_ID, 1))
                .add("force", false)
                .add("maxChunkSize", splitSize)
                .get();

        CommandResult splitVectorResult = collection.getDB().getSisterDB("admin").command(cmd);
        return (BasicDBList) splitVectorResult.get(SPLIT_KEYS);

    }

    /**
     * Gets split data collection shard enviroment.
     *
     * @param shards         the shards
     * @param dbName         the db name
     * @param collectionName the collection name
     * @return the split data collection shard enviroment
     */
    private Pair<BasicDBList, List<ServerAddress>> getSplitDataCollectionShardEnviroment(Map<String, String[]> shards,
                                                                                         String dbName,
                                                                                         String collectionName) {
        MongoClient mongoClient = null;
        try {
            Set<String> keys = shards.keySet();

            for (String key : keys) {

                List<ServerAddress> addressList = getServerAddressList(Arrays.asList(shards.get(key)));

                mongoClient = new MongoClient(addressList);

                BasicDBList dbList = getSplitData(mongoClient.getDB(dbName).getCollection(collectionName));

                if (dbList != null) {
                    return Pair.create(dbList, addressList);
                }
            }
        } catch (UnknownHostException e) {
            throw new GenericException(e);
        } finally {
            if (mongoClient != null) {
                mongoClient.close();
            }

        }

        return null;

    }

    @Override
    public List<String> getPreferredLocations(Partition split) {
        return removeAddressPort(((HadoopPartition) split).splitWrapper().getReplicas());
    }

    /**
     * Calculates shard chunks.
     *
     * @param collection the collection
     * @return the deep partition [ ]
     */
    private HadoopPartition[] calculateShardChunks(DBCollection collection) {

        DBCursor chuncks = getChunks(collection);

        Map<String, String[]> shards = getShards(collection);

        MongoPartition[] deepPartitions = new MongoPartition[chuncks.count()];
        int i = 0;
        boolean keyAssigned = false;
        String key = null;
        while (chuncks.hasNext()) {

            DBObject dbObject = chuncks.next();
            if (!keyAssigned) {
                Set<String> keySet = ((DBObject) dbObject.get("min")).keySet();
                for (String s : keySet) {
                    key = s;
                    keyAssigned = true;
                }
            }
            deepPartitions[i] = new MongoPartition(mongoDeepJobConfig.getRddId(), i,
                    new TokenRange(shards.get(dbObject.get
                            ("shard")),
                            ((DBObject) dbObject.get
                                    ("min")).get(key),
                            ((DBObject) dbObject.get("max")).get(key)), key);
            i++;
        }
        List<MongoPartition> mongoPartitions = Arrays.asList(deepPartitions);

        Collections.shuffle(mongoPartitions);
        return mongoPartitions.toArray(new MongoPartition[mongoPartitions.size()]);
    }

    /**
     * Gets server address list.
     *
     * @param addressStringList the address string list
     * @return the server address list
     * @throws UnknownHostException the unknown host exception
     */
    private List<ServerAddress> getServerAddressList(List<String> addressStringList) throws UnknownHostException {

        List<ServerAddress> addressList = new ArrayList<>();

        for (String addressString : addressStringList) {
            addressList.add(new ServerAddress(addressString));
        }
        return addressList;
    }

    @Override
    public boolean hasNext() {
        return reader.hasNext();
    }

    @Override
    public T next() {
        return transformElement(reader.next());
    }

    @Override
    public void close() {
        if (reader != null) {
            reader.close();
        }

        if (writer != null) {
            writer.close();
        }

    }

    @Override
    public void initIterator(Partition dp, S config) {

        mongoDeepJobConfig = initConfig(config, mongoDeepJobConfig);

        reader = new MongoReader(mongoDeepJobConfig);
        reader.init(dp);
    }



    @Override
    public void saveRDD(T entity) {
        writer.save(transformElement(entity));
    }

    @Override
    public void initSave(S config, T first, UpdateQueryBuilder queryBuilder) {

        mongoDeepJobConfig = initConfig(config, mongoDeepJobConfig);

        try {
            writer = new MongoWriter(getServerAddressList(mongoDeepJobConfig.getHostList()),
                    mongoDeepJobConfig.getDatabase(),
                    mongoDeepJobConfig.getCollection(), mongoDeepJobConfig.getWriteConcern());
        } catch (UnknownHostException e) {
            throw new GenericException(e);
        }
    }

    /**
     * Transform element.
     *
     * @param dbObject the db object
     * @return the t
     */
    protected abstract T transformElement(DBObject dbObject);

    /**
     * Transform element.
     *
     * @param entity the entity
     * @return the dB object
     */
    protected abstract DBObject transformElement(T entity);

}
