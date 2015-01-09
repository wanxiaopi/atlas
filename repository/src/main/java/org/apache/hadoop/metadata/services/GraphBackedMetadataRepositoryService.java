/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.metadata.services;

import com.google.common.base.Preconditions;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;

import org.apache.hadoop.metadata.service.Services;
import org.apache.hadoop.metadata.util.GraphUtils;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

/**
 * An implementation backed by Titan Graph DB.
 */
public class GraphBackedMetadataRepositoryService implements MetadataRepositoryService {

    private static final Logger LOG =
            LoggerFactory.getLogger(GraphBackedMetadataRepositoryService.class);
    public static final String NAME = GraphBackedMetadataRepositoryService.class.getSimpleName();

    private GraphService graphService;
    
    @Inject
    GraphBackedMetadataRepositoryService(GraphService service) {
    	this.graphService = service;
    }

    /**
     * Name of the service.
     *
     * @return name of the service
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Starts the service. This method blocks until the service has completely started.
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
    }

    /**
     * Stops the service. This method blocks until the service has completely shut down.
     */
    @Override
    public void stop() {
        // do nothing
        graphService = null;
    }

    /**
     * A version of stop() that is designed to be usable in Java7 closure
     * clauses.
     * Implementation classes MUST relay this directly to {@link #stop()}
     *
     * @throws java.io.IOException never
     * @throws RuntimeException    on any failure during the stop operation
     */
    @Override
    public void close() throws IOException {
        stop();
    }

    private Graph getBlueprintsGraph() {
        return graphService.getBlueprintsGraph();
    }

    private TransactionalGraph getTransactionalGraph() {
        return graphService.getTransactionalGraph();
    }

    @Override
    public String submitEntity(String entity, String entityType) {
        LOG.info("adding entity={} type={}", entity, entityType);
        @SuppressWarnings("unchecked")
        Map<String, String> properties = (Map<String, String>) JSONValue.parse(entity);

        final String entityName = properties.get("entityName");
        Preconditions.checkNotNull(entityName, "entity name cannot be null");

        // todo check if this is a duplicate

        final String guid = UUID.randomUUID().toString();
        final TransactionalGraph transactionalGraph = getTransactionalGraph();
        try {
            transactionalGraph.rollback();

            Vertex entityVertex = transactionalGraph.addVertex(null);
            entityVertex.setProperty("guid", guid);
            entityVertex.setProperty("entityName", entityName);
            entityVertex.setProperty("entityType", entityType);
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                entityVertex.setProperty(entry.getKey(), entry.getValue());
            }
        } catch (Exception e) {
            transactionalGraph.rollback();
        } finally {
            transactionalGraph.commit();
        }

        return guid;
    }

    @Override
    public String getEntityDefinition(String entityName, String entityType) {
        LOG.info("Retrieving entity name={} type={}", entityName, entityType);
        Vertex entityVertex = GraphUtils.findVertex(getBlueprintsGraph(), entityName, entityType);
        if (entityVertex == null) {
            return null;
        }

        Map<String, String> properties = GraphUtils.extractProperties(entityVertex);
        return JSONValue.toJSONString(properties);
    }

    @Override
    public List<String> getEntityList(String entityType) {
        LOG.info("Retrieving entity list for type={}", entityType);
        return Collections.emptyList();
    }
}
