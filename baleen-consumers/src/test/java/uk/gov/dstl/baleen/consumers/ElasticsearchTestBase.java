// Dstl (c) Crown Copyright 2017
// Modified by NCA (c) Crown Copyright 2017
package uk.gov.dstl.baleen.consumers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.join.ScoreMode;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.gov.dstl.baleen.annotators.testing.Annotations;
import uk.gov.dstl.baleen.resources.EmbeddedElasticsearch5;
import uk.gov.dstl.baleen.types.common.CommsIdentifier;
import uk.gov.dstl.baleen.types.metadata.PublishedId;
import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.types.semantic.Temporal;
import uk.gov.dstl.baleen.uima.testing.JCasSingleton;
import uk.gov.dstl.baleen.uima.utils.UimaTypesUtils;

public abstract class ElasticsearchTestBase {
  private static final String EXTERNAL_ID = "externalId";
  private static final String VALUE = "value";
  private static final String TYPE = "type";
  private static final String CONFIDENCE = "confidence";
  private static final String END = "end";
  private static final String BEGIN = "begin";
  private static final String DOC_TYPE = "docType";

  protected static final String BALEEN_INDEX = "baleen_index";

  protected JCas jCas;
  protected AnalysisEngine ae;
  protected EmbeddedElasticsearch5 elasticsearch;

  @Before
  public void beforeTest() throws Exception {
    jCas = JCasSingleton.getJCasInstance();
    elasticsearch = new EmbeddedElasticsearch5();
  }

  @After
  public void afterTest() {
    ae.destroy();
    try {
      elasticsearch.close();
    } catch (IOException ioe) {
      // Do nothing
    }
    elasticsearch = null;
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testNoEntities() throws Exception {
    long timestamp = createNoEntitiesDocument();
    ae.process(jCas);

    elasticsearch.flush(BALEEN_INDEX);

    assertEquals(new Long(1), getCount());

    SearchHit result =
        elasticsearch.client().search(new SearchRequest()).actionGet().getHits().getHits()[0];
    assertEquals("Hello World", result.getSource().get("content"));
    assertEquals("en", result.getSource().get("language"));
    assertEquals(timestamp, result.getSource().get("dateAccessed"));
    assertEquals("test/no_entities", result.getSource().get("sourceUri"));
    assertEquals("test", result.getSource().get(DOC_TYPE));

    assertEquals("OFFICIAL", result.getSource().get("classification"));

    List<String> rels = (List<String>) result.getSource().get("releasability");
    assertEquals(3, rels.size());
    assertTrue(rels.contains("ENG"));

    List<String> cavs = (List<String>) result.getSource().get("caveats");
    assertEquals(2, cavs.size());
    assertTrue(cavs.contains("TEST_A"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testMetadata() throws Exception {
    createMetadataDocument();
    ae.process(jCas);

    elasticsearch.flush(BALEEN_INDEX);

    assertEquals(new Long(1), getCount());

    SearchHit result =
        elasticsearch.client().search(new SearchRequest()).actionGet().getHits().getHits()[0];
    List<String> pids = (List<String>) result.getSource().get("publishedId");
    assertEquals("id_1", pids.get(0));
    assertEquals("id_2", pids.get(1));

    Map<String, Object> metadataMap = (Map<String, Object>) result.getSource().get("metadata");

    assertEquals("D3", metadataMap.get("sourceAndInformationGrading"));

    assertEquals("test_value", metadataMap.get("test_key"));
    assertEquals("Test Title", metadataMap.get("documentTitle"));
    assertEquals("ENG|WAL|SCO", metadataMap.get("countryInfo"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testEntities() throws Exception {
    createEntitiesDocument();
    ae.process(jCas);

    elasticsearch.flush(BALEEN_INDEX);

    assertEquals(new Long(1), getCount());

    SearchHit result =
        elasticsearch.client().search(new SearchRequest()).actionGet().getHits().getHits()[0];
    List<Map<String, Object>> entities =
        (List<Map<String, Object>>) result.getSource().get("entities");
    assertEquals(4, entities.size());

    Map<String, Object> person = entities.get(0);
    assertTrue(person.size() >= 6); // The REST API only adds non-null fields, the Transport API add
    // null fields too
    assertEquals(0, person.get(BEGIN));
    assertEquals(5, person.get(END));
    assertEquals(0.0, person.get(CONFIDENCE));
    assertEquals("Person", person.get(TYPE));
    assertEquals("James", person.get(VALUE));
    assertNotNull(person.get(EXTERNAL_ID));

    Map<String, Object> location = entities.get(1);
    assertTrue(location.size() >= 7); // The REST API only adds non-null fields, the Transport API
    // add null fields too
    assertEquals(14, location.get(BEGIN));
    assertEquals(20, location.get(END));
    assertEquals(0.0, location.get(CONFIDENCE));
    assertEquals("Location", location.get(TYPE));
    assertEquals("London", location.get(VALUE));
    assertNotNull(location.get(EXTERNAL_ID));

    Map<String, Object> geometryMap = new HashMap<>();
    geometryMap.put(TYPE, "Point");
    geometryMap.put("coordinates", new ArrayList<>(Arrays.asList(-0.1, 51.5)));

    assertEquals(geometryMap, location.get("geoJson"));

    Map<String, Object> date = entities.get(2);
    assertTrue(date.size() >= 6); // The REST API only adds non-null fields, the Transport API add
    // null fields too
    assertEquals(24, date.get(BEGIN));
    assertEquals(42, date.get(END));
    assertEquals(1.0, date.get(CONFIDENCE));
    assertEquals("Temporal", date.get(TYPE));
    assertEquals("19th February 2015", date.get(VALUE));
    assertNotNull(date.get(EXTERNAL_ID));

    Map<String, Object> email = entities.get(3);
    assertEquals(8, email.size());
    assertEquals(66, email.get(BEGIN));
    assertEquals(83, email.get(END));
    assertEquals(0.0, email.get(CONFIDENCE));
    assertEquals("CommsIdentifier", email.get(TYPE));
    assertEquals("email", email.get("subType"));
    assertEquals("james@example.com", email.get(VALUE));
    assertNotNull(email.get(EXTERNAL_ID));
  }

  @Test
  public void testReindexEntities() throws Exception {
    createEntitiesDocument();
    ae.process(jCas);
    ae.process(jCas);

    // Change the last document so we can check its been updated
    getDocumentAnnotation(jCas).setDocumentClassification("TEST");
    ae.process(jCas);

    elasticsearch.flush(BALEEN_INDEX);

    assertEquals(new Long(1), getCount());
    SearchHit result =
        elasticsearch.client().search(new SearchRequest()).actionGet().getHits().getHits()[0];

    // This checks the last document is tone we are getting
    assertEquals("TEST", result.getSource().get("classification"));
  }

  @Test
  public void testNestedEntities() throws Exception {
    createEntitiesDocument();
    ae.process(jCas);

    createEntitiesDocument2();
    ae.process(jCas);

    elasticsearch.flush(BALEEN_INDEX);

    assertEquals(new Long(2), getCount());

    SearchRequestBuilder srb =
        elasticsearch
            .client()
            .prepareSearch(BALEEN_INDEX)
            .setQuery(
                QueryBuilders.nestedQuery(
                    "entities",
                    QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("entities.type", "Location"))
                        .must(QueryBuilders.matchQuery("entities.value", "London")),
                    ScoreMode.Avg));

    SearchHits results = elasticsearch.client().search(srb.request()).actionGet().getHits();
    assertEquals(1, results.getTotalHits());
  }

  protected DocumentAnnotation getDocumentAnnotation(JCas jCas) {
    return (DocumentAnnotation) jCas.getDocumentAnnotationFs();
  }

  protected long createNoEntitiesDocument() {
    jCas.reset();
    jCas.setDocumentText("Hello World");
    jCas.setDocumentLanguage("en");

    long timestamp = System.currentTimeMillis();

    DocumentAnnotation da = getDocumentAnnotation(jCas);
    da.setTimestamp(timestamp);
    da.setSourceUri("test/no_entities");
    da.setDocType("test");
    da.setDocumentClassification("OFFICIAL");
    da.setDocumentCaveats(
        UimaTypesUtils.toArray(jCas, Arrays.asList(new String[] {"TEST_A", "TEST_B"})));
    da.setDocumentReleasability(
        UimaTypesUtils.toArray(jCas, Arrays.asList(new String[] {"ENG", "SCO", "WAL"})));

    return timestamp;
  }

  protected void createMetadataDocument() {
    jCas.reset();
    jCas.setDocumentText("Hello World");

    PublishedId pid1 = new PublishedId(jCas);
    pid1.setValue("id_1");
    pid1.addToIndexes();

    PublishedId pid2 = new PublishedId(jCas);
    pid2.setValue("id_2");
    pid2.addToIndexes();

    Annotations.createMetadata(jCas, "sourceAndInformationGrading", "D3");
    Annotations.createMetadata(jCas, "countryInfo", "ENG|WAL|SCO");
    Annotations.createMetadata(jCas, "documentTitle", "Test Title");
    Annotations.createMetadata(jCas, "test_key", "test_value");
  }

  protected void createEntitiesDocument() {
    jCas.reset();
    jCas.setDocumentText(
        "James went to London on 19th February 2015. His e-mail address is james@example.com");

    Annotations.createPerson(jCas, 0, 5, "James");
    Annotations.createLocation(
        jCas, 14, 20, "London", "{\"type\": \"Point\", \"coordinates\": [-0.1, 51.5]}");
    Temporal d = Annotations.createTemporal(jCas, 24, 42, "");
    d.setConfidence(1.0);

    CommsIdentifier ci = new CommsIdentifier(jCas);
    ci.setBegin(66);
    ci.setEnd(83);
    ci.setSubType("email");
    ci.addToIndexes();
  }

  protected void createEntitiesDocument2() {
    jCas.reset();
    jCas.setDocumentText(
        "Paula went to London on 12th February 2017. In Paris, she met a UID male.");

    Annotations.createPerson(jCas, 0, 5, "Paula");
    Annotations.createLocation(jCas, 47, 53, "Paris", null);
    Temporal d = Annotations.createTemporal(jCas, 24, 42, "");
    d.setConfidence(1.0);

    Entity e = new Entity(jCas);
    e.setBegin(14);
    e.setEnd(20);
    e.setValue("London");
    e.addToIndexes();
  }

  protected Long getCount() {
    SearchResponse sr =
        elasticsearch.client().prepareSearch(BALEEN_INDEX).setSize(0).execute().actionGet();
    return sr.getHits().getTotalHits();
  }
}
