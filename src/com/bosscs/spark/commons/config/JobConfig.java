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

package com.bosscs.spark.commons.config;

import static com.bosscs.spark.commons.extractor.utils.ExtractorConstants.COLLECTION;
import static com.bosscs.spark.commons.extractor.utils.ExtractorConstants.DATABASE;
import static com.bosscs.spark.commons.extractor.utils.ExtractorConstants.FILTER_QUERY;
import static com.bosscs.spark.commons.extractor.utils.ExtractorConstants.HOST;
import static com.bosscs.spark.commons.extractor.utils.ExtractorConstants.INPUT_COLUMNS;
import static com.bosscs.spark.commons.extractor.utils.ExtractorConstants.PASSWORD;
import static com.bosscs.spark.commons.extractor.utils.ExtractorConstants.PORT;
import static com.bosscs.spark.commons.extractor.utils.ExtractorConstants.USERNAME;
import static com.bosscs.spark.commons.extractor.utils.ExtractorConstants.ES_REST_PORTS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.bosscs.spark.commons.entity.Cells;
import com.bosscs.spark.commons.filter.Filter;

/**
 * Created by Jerry Xiong on 13/02/16.
 *
 * @param <T> the type parameter
 */
public class JobConfig<T, S extends JobConfig> extends BaseConfig<T, S> implements Serializable {

    /**
     * The Username.
     */
    protected String username;

    /**
     * The Password.
     */
    protected String password;

    /**
     * The Name space (catalog+table / database+collection).
     */
    protected String nameSpace;

    /**
     * The Catalog / database.
     */
    protected String catalog;

    /**
     * The Table / collection.
     */
    protected String table;

    /**
     * The Port.
     */
    protected int port;

    /**
     * The Host.
     */
    protected List<String> host = new ArrayList<>();

    /**
     * The inputColumns.
     */
    protected String[] inputColumns;

    /**
     * Database filters
     */
    protected Filter[] filters;

    /**
     * Instantiates a new Deep job config.
     */
    public JobConfig() {
        super((Class<T>) Cells.class);
    }

    /**
     * Instantiates a new Deep job config.
     *
     * @param t the t
     */
    public JobConfig(Class<T> t) {
        super(t);
    }

    /**
     * Gets username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Get name space.
     *
     * @return the string
     */
    public String getNameSpace() {
        if (nameSpace == null) {
            nameSpace = new StringBuilder().append(catalog).append(".").append(table).toString();
        }
        return nameSpace;

    }

    /**
     * Gets catalog.
     *
     * @return the catalog
     */
    public String getCatalog() {
        return catalog;
    }

    /**
     * Gets table.
     *
     * @return the table
     */
    public String getTable() {
        return table;
    }

    public String[] getInputColumns() {
        return inputColumns;
    }

    public Filter[] getFilters() {
        return filters;
    }

    /**
     * Gets port.
     *
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Gets host.
     *
     * @return the host
     */
    public List<String> getHostList() {
        return host;
    }

    public String getHost() {
        return !host.isEmpty() ? host.get(0) : null;
    }

    /**
     * Initialize s.
     *
     * @param extractorConfig the extractor config
     * @return the s
     */
    public S initialize(ExtractorConfig extractorConfig) {

        setExtractorImplClassName(extractorConfig.getExtractorImplClassName());
        setEntityClass(extractorConfig.getEntityClass());
        setRddId(extractorConfig.getRddId());
        setPartitionId(extractorConfig.getPartitionId());

        Map<String, Serializable> values = extractorConfig.getValues();

        if (values.get(USERNAME) != null) {
            username(extractorConfig.getString(USERNAME));
        }

        if (values.get(PASSWORD) != null) {
            password(extractorConfig.getString(PASSWORD));
        }

        if (values.get(HOST) != null) {
            host((extractorConfig.getStringArray(HOST)));
        }

        if (values.get(ES_REST_PORTS) != null) {
            port((extractorConfig.getInteger(ES_REST_PORTS)));
        }

        if (values.get(PORT) != null) {
            port((extractorConfig.getInteger(PORT)));
        }


        if (values.get(COLLECTION) != null) {
            table(extractorConfig.getString(COLLECTION));
        }

        if (values.get(INPUT_COLUMNS) != null) {
            inputColumns(extractorConfig.getStringArray(INPUT_COLUMNS));
        }

        if (values.get(DATABASE) != null) {
            catalog(extractorConfig.getString(DATABASE));
        }

        if (values.get(FILTER_QUERY) != null) {
            filters(extractorConfig.getFilterArray(FILTER_QUERY));
        }

        return (S) this;
    }

    public S initialize(JobConfig deepJobConfig){
        if(deepJobConfig instanceof JobConfig) {
            this.catalog = deepJobConfig.getCatalog();
            this.table = deepJobConfig.getTable();
            this.username = deepJobConfig.getUsername();
            this.password = deepJobConfig.getPassword();
            this.filters = deepJobConfig.getFilters();
            this.inputColumns = deepJobConfig.getInputColumns();
            this.port = deepJobConfig.getPort();
            this.host = deepJobConfig.getHostList();
            this.extractorImplClass = deepJobConfig.getExtractorImplClass();
            this.extractorImplClassName = deepJobConfig.getExtractorImplClassName();
            this.entityClass = deepJobConfig.getEntityClass();
        }else{
            return (S) deepJobConfig.initialize();
        }
        return (S) this.initialize();
    }

    /**
     * Host deep job config.
     *
     * @param hostName the hostname
     * @return the deep job config
     */
    public S host(String hostName) {
        host.add(hostName);
        return (S) this;
    }

    public S host(String... hostNames) {
        for (String hostName : hostNames) {
            host.add(hostName);
        }
        return (S) this;
    }

    public S host(List<String> hostNames) {
        this.host.addAll(hostNames);
        return (S) this;
    }

    /**
     * Password s.
     *
     * @param password the password
     * @return the s
     */
    public S password(String password) {
        this.password = password;
        return (S) this;
    }

    public S table(String table) {
        this.table = table;
        return (S) this;
    }

    public S catalog(String catalog) {
        this.catalog = catalog;
        return (S) this;
    }

    /**
     * Username s.
     *
     * @param username the username
     * @return the s
     */
    public S username(String username) {
        this.username = username;
        return (S) this;
    }

    /**
     * Port s.
     *
     * @param port the username
     * @return the s
     */
    public S port(Integer port) {
        this.port = port;
        return (S) this;
    }

    /**
     * @param inputColumns
     * @return the s
     */
    public S inputColumns(String[] inputColumns) {
        this.inputColumns = inputColumns;
        return (S) this;
    }

    /**
     * @param filters
     * @return the s
     */
    public S filters(Filter[] filters) {
        this.filters = filters;
        return (S) this;
    }

    /**
     * Initialize s.
     *
     * @return the s
     */
    public S initialize() {
        return (S) this;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DeepJobConfig{");
        sb.append("username='").append(username).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", nameSpace='").append(nameSpace).append('\'');
        sb.append(", catalog='").append(catalog).append('\'');
        sb.append(", table='").append(table).append('\'');
        sb.append(", port=").append(port);
        sb.append(", host=").append(host);
        sb.append(", inputColumns=").append(inputColumns == null ? "null" : Arrays.asList(inputColumns).toString());
        sb.append(", filters=").append(filters == null ? "null" : Arrays.asList(filters).toString());
        sb.append('}');
        sb.append(super.toString());
        return sb.toString();
    }
}
