// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.consumer;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.bson.Document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import uk.gov.dstl.baleen.consumers.analysis.Mongo;
import uk.gov.dstl.baleen.consumers.analysis.converters.DocumentConverter;
import uk.gov.dstl.baleen.consumers.analysis.data.AnalysisConstants;
import uk.gov.dstl.baleen.consumers.analysis.data.BaleenDocument;
import uk.gov.dstl.baleen.consumers.utils.ConsumerUtils;
import uk.gov.dstl.baleen.resources.SharedIdGenerator;
import uk.gov.dstl.baleen.resources.SharedMongoResource;
import uk.gov.dstl.baleen.resources.SharedTranslationResource;
import uk.gov.dstl.baleen.translation.TranslationException;
import uk.gov.dstl.baleen.uima.BaleenConsumer;

/**
 * A consumer that also translated the content of the document using the {@link
 * SharedTranslationResource}.
 *
 * <p>This uses components from the {@link Mongo} analysis consumer to keep in line with it's
 * output.
 */
public class TranslatingMongo extends BaleenConsumer {

  /** Default translation collection name */
  public static final String DEFAULT_TRANSLATION_COLLECTION = "translation";

  /** The field used to store the translation. */
  public static final String TRANSLATION_FIELD = "translation";

  private static final String CONTENT = "content";
  private static final String EXTERNAL_ID = "externalId";
  private static final String TEXT = "text";

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  /**
   * The common id generator
   *
   * @baleen.resource uk.gov.dstl.baleen.resources.SharedIdGenerator
   */
  @ExternalResource(key = SharedIdGenerator.RESOURCE_KEY)
  private SharedIdGenerator idGenerator;

  /**
   * The common translation resource
   *
   * @baleen.resource {@link uk.gov.dstl.baleen.resources.SharedTranslationResource}
   */
  @ExternalResource(key = SharedTranslationResource.RESOURCE_KEY)
  private SharedTranslationResource translationService;

  /**
   * Connection to Mongo
   *
   * @baleen.resource uk.gov.dstl.baleen.resources.SharedMongoResource
   */
  @ExternalResource(key = SharedMongoResource.RESOURCE_KEY)
  private SharedMongoResource mongoResource;

  /**
   * The collection to output translations to
   *
   * @baleen.config {@value #DEFAULT_TRANSLATION_COLLECTION}
   */
  public static final String PARAM_TRANSLATION_COLLECTION = "translationCollection";

  @ConfigurationParameter(
    name = PARAM_TRANSLATION_COLLECTION,
    defaultValue = DEFAULT_TRANSLATION_COLLECTION
  )
  private String translationCollectionName;

  /**
   * Should a hash of the content be used to generate the ID? If false, then a hash of the Source
   * URI is used instead.
   *
   * @baleen.config false
   */
  public static final String PARAM_CONTENT_HASH_AS_ID = "contentHashAsId";

  @ConfigurationParameter(name = PARAM_CONTENT_HASH_AS_ID, defaultValue = "false")
  private boolean contentHashAsId;

  private MongoCollection<Document> translationCollection;

  @Override
  public void doInitialize(final UimaContext aContext) throws ResourceInitializationException {
    super.doInitialize(aContext);

    final MongoDatabase db = mongoResource.getDB();
    translationCollection = db.getCollection(translationCollectionName);

    translationCollection.createIndex(new Document(EXTERNAL_ID, 1));
    translationCollection.createIndex(new Document(CONTENT, TEXT));
    translationCollection.createIndex(new Document(TRANSLATION_FIELD, TEXT));
  }

  @Override
  protected void doProcess(final JCas jCas) throws AnalysisEngineProcessException {
    idGenerator.resetIfNewJCas(jCas);

    final DocumentAnnotation documentAnnotation = getDocumentAnnotation(jCas);
    final String baleenDocumentId =
        ConsumerUtils.getExternalId(documentAnnotation, contentHashAsId);
    final String documentId = idGenerator.generateForExternalId(baleenDocumentId);

    final DocumentConverter documentConverter = new DocumentConverter();
    final BaleenDocument baleenDocument =
        documentConverter.convert(jCas, documentId, baleenDocumentId, documentAnnotation);

    String translation;
    try {
      translation = translationService.translate(baleenDocument.getContent());
    } catch (TranslationException te) {
      translation = "";
      getMonitor().warn("Unable to translate document", te);
    }

    try {
      deleteDocument(baleenDocument);
    } catch (final MongoException e) {
      getMonitor().warn("Unable to delete older content", e);
    }

    try {
      saveDocument(baleenDocument, translation);
    } catch (final AnalysisEngineProcessException e) {
      getMonitor().warn("Unable to save document", e);
    }
  }

  private void deleteDocument(final BaleenDocument document) {
    final String documentId = document.getBaleenId();
    translationCollection.deleteMany(Filters.eq(AnalysisConstants.BALEEN_ID, documentId));
  }

  protected void saveDocument(final BaleenDocument document, String translation)
      throws AnalysisEngineProcessException {
    Document bson = toBson(document);
    bson.append(TRANSLATION_FIELD, translation);
    translationCollection.insertOne(bson);
  }

  private Document toBson(final Object o) throws AnalysisEngineProcessException {
    // Somewhat ridiculous approach to generating something that Mongo can save, but at least its
    // consistent with ES and the original POJO
    try {
      final String json = OBJECT_MAPPER.writeValueAsString(o);
      return Document.parse(json);
    } catch (final Exception e) {
      throw new AnalysisEngineProcessException(e);
    }
  }
}
