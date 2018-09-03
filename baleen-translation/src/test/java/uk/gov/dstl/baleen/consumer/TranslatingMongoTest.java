// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.gov.dstl.baleen.resources.SharedFongoResource.PARAM_FONGO_COLLECTION;
import static uk.gov.dstl.baleen.resources.SharedFongoResource.PARAM_FONGO_DATA;

import java.util.Collections;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.impl.CustomResourceSpecifier_impl;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import uk.gov.dstl.baleen.consumers.analysis.convertors.AnalysisMockData;
import uk.gov.dstl.baleen.resources.SharedFongoResource;
import uk.gov.dstl.baleen.resources.SharedIdGenerator;
import uk.gov.dstl.baleen.resources.SharedTranslationResource;
import uk.gov.dstl.baleen.translation.TestConfiguredTranslateService;

public class TranslatingMongoTest {

  private static final String MONGO = "mongo";

  private static final String TRANSLATION = "test translation";

  private AnalysisEngine ae;

  private MongoCollection<Document> documentCollection;

  @Before
  public void setUp() throws ResourceInitializationException, ResourceAccessException {

    ExternalResourceDescription terd =
        ExternalResourceFactory.createExternalResourceDescription(
            SharedTranslationResource.RESOURCE_KEY,
            SharedTranslationResource.class,
            SharedTranslationResource.PARAM_SERVICE,
            TestConfiguredTranslateService.class.getSimpleName());

    // Create a description of an external resource - a fongo instance, in the same way we would
    // have created a shared mongo resource
    final ExternalResourceDescription merd =
        ExternalResourceFactory.createExternalResourceDescription(
            MONGO,
            SharedFongoResource.class,
            PARAM_FONGO_COLLECTION,
            "test",
            PARAM_FONGO_DATA,
            "[]");

    final ExternalResourceDescription idErd =
        ExternalResourceFactory.createExternalResourceDescription(
            SharedIdGenerator.RESOURCE_KEY, SharedIdGenerator.class);

    // Create the analysis engine
    final AnalysisEngineDescription aed =
        AnalysisEngineFactory.createEngineDescription(
            TranslatingMongo.class,
            MONGO,
            merd,
            SharedIdGenerator.RESOURCE_KEY,
            idErd,
            SharedTranslationResource.RESOURCE_KEY,
            terd,
            TestConfiguredTranslateService.RESPONSE,
            TRANSLATION);
    ae = AnalysisEngineFactory.createEngine(aed);
    ae.initialize(new CustomResourceSpecifier_impl(), Collections.emptyMap());

    final SharedFongoResource sfr =
        (SharedFongoResource) ae.getUimaContext().getResourceObject(MONGO);

    final MongoDatabase db = sfr.getDB();
    documentCollection = db.getCollection(TranslatingMongo.DEFAULT_TRANSLATION_COLLECTION);

    assertEquals(0, documentCollection.count());
  }

  @After
  public void tearDown() {
    if (ae != null) {
      ae.destroy();
    }
  }

  @Test
  public void test() throws AnalysisEngineProcessException {
    final AnalysisMockData data = new AnalysisMockData();

    ae.process(data.getJCas());

    assertEquals(1, documentCollection.count());

    MongoCursor<Document> found = documentCollection.find().iterator();

    assertTrue(found.hasNext());

    while (found.hasNext()) {
      Document next = found.next();

      assertEquals(TRANSLATION, next.get(TranslatingMongo.TRANSLATION_FIELD));
      assertEquals(AnalysisMockData.TEXT, next.get("content"));
    }
  }
}
