// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.jobs.mallet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.gov.dstl.baleen.jobs.triage.MaxEntClassifierTrainer.CLASSIFICATION_FIELD;
import static uk.gov.dstl.baleen.jobs.triage.MaxEntClassifierTrainer.KEY_STOPWORDS;
import static uk.gov.dstl.baleen.jobs.triage.MaxEntClassifierTrainer.PARAM_DOCUMENT_COLLECTION;
import static uk.gov.dstl.baleen.jobs.triage.MaxEntClassifierTrainer.PARAM_LABELS_FILE;
import static uk.gov.dstl.baleen.jobs.triage.MaxEntClassifierTrainer.PARAM_MODEL_FILE;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import cc.mallet.classify.Classifier;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

import com.google.common.collect.ImmutableList;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import uk.gov.dstl.baleen.consumers.Mongo;
import uk.gov.dstl.baleen.jobs.triage.MaxEntClassifierTrainer;
import uk.gov.dstl.baleen.mallet.FileObject;
import uk.gov.dstl.baleen.resources.SharedFongoResource;
import uk.gov.dstl.baleen.resources.SharedMongoResource;
import uk.gov.dstl.baleen.resources.SharedStopwordResource;
import uk.gov.dstl.baleen.uima.AbstractBaleenTaskTest;

public class MaxEntClassifierTrainerTest extends AbstractBaleenTaskTest {

  private static final URL LABELS_URL =
      MaxEntClassifierTrainerTest.class.getResource("/labels.txt");

  private static final String SOURCE = Mongo.FIELD_DOCUMENT_SOURCE;
  private static final String CONTENT = Mongo.FIELD_CONTENT;
  private static final String DOCUMENT = Mongo.FIELD_DOCUMENT;
  private static final String COLLECTION = "collection";

  private Path modelPath;
  private MongoCollection<Document> documents;

  @Before
  public void before()
      throws URISyntaxException, ResourceInitializationException, AnalysisEngineProcessException,
          ResourceAccessException {

    ExternalResourceDescription stopWordsErd =
        ExternalResourceFactory.createExternalResourceDescription(
            MaxEntClassifierTrainer.KEY_STOPWORDS, SharedStopwordResource.class);

    // @formatter:off
    ImmutableList<String> data =
        ImmutableList.of(
            new Document()
                .append(DOCUMENT, new Document().append(SOURCE, "pos1.txt"))
                .append(CONTENT, "I love this sandwich.")
                .toJson(),
            new Document()
                .append(DOCUMENT, new Document().append(SOURCE, "pos2.txt"))
                .append(CONTENT, "this is an amazing place!'")
                .toJson(),
            new Document()
                .append(DOCUMENT, new Document().append(SOURCE, "pos3.txt"))
                .append(CONTENT, "I feel very good about these beers.")
                .toJson(),
            new Document()
                .append(DOCUMENT, new Document().append(SOURCE, "pos4.txt"))
                .append(CONTENT, "this is my best work.")
                .toJson(),
            new Document()
                .append(DOCUMENT, new Document().append(SOURCE, "pos5.txt"))
                .append(CONTENT, "what an awesome view")
                .toJson(),
            new Document()
                .append(DOCUMENT, new Document().append(SOURCE, "pos6.txt"))
                .append(CONTENT, "the beer was good.")
                .toJson(),
            new Document()
                .append(DOCUMENT, new Document().append(SOURCE, "pos7.txt"))
                .append(CONTENT, "I feel amazing!")
                .toJson(),
            new Document()
                .append(DOCUMENT, new Document().append(SOURCE, "pos8.txt"))
                .append(CONTENT, "Gary is a friend of mine.")
                .toJson(),
            new Document()
                .append(DOCUMENT, new Document().append(SOURCE, "neg1.txt"))
                .append(CONTENT, "I do not like this restaurant")
                .toJson(),
            new Document()
                .append(DOCUMENT, new Document().append(SOURCE, "neg2.txt"))
                .append(CONTENT, "I am tired of this stuff.")
                .toJson(),
            new Document()
                .append(DOCUMENT, new Document().append(SOURCE, "neg3.txt"))
                .append(CONTENT, "I can't deal with this")
                .toJson(),
            new Document()
                .append(DOCUMENT, new Document().append(SOURCE, "neg4.txt"))
                .append(CONTENT, "he is my sworn enemy!")
                .toJson(),
            new Document()
                .append(DOCUMENT, new Document().append(SOURCE, "neg5.txt"))
                .append(CONTENT, "my boss is horrible.")
                .toJson(),
            new Document()
                .append(DOCUMENT, new Document().append(SOURCE, "neg6.txt"))
                .append(CONTENT, "I do not enjoy my job")
                .toJson(),
            new Document()
                .append(DOCUMENT, new Document().append(SOURCE, "neg7.txt"))
                .append(CONTENT, "I ain't feeling dandy today.")
                .toJson(),
            new Document()
                .append(DOCUMENT, new Document().append(SOURCE, "neg8.txt"))
                .append(CONTENT, "I can't believe I'm doing this.")
                .toJson());

    // @formatter:on

    try {
      modelPath = Files.createTempFile("model", ".mallet");
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }

    ExternalResourceDescription fongoErd =
        ExternalResourceFactory.createExternalResourceDescription(
            SharedMongoResource.RESOURCE_KEY,
            SharedFongoResource.class,
            "fongo.collection",
            COLLECTION,
            "fongo.data",
            data.toString());

    final AnalysisEngine ae =
        create(
            MaxEntClassifierTrainer.class,
            KEY_STOPWORDS,
            stopWordsErd,
            SharedMongoResource.RESOURCE_KEY,
            fongoErd,
            PARAM_LABELS_FILE,
            Paths.get(LABELS_URL.toURI()).toString(),
            PARAM_DOCUMENT_COLLECTION,
            COLLECTION,
            PARAM_MODEL_FILE,
            modelPath.toString());

    execute(ae);

    SharedFongoResource sfr =
        (SharedFongoResource)
            ae.getUimaContext().getResourceObject(SharedMongoResource.RESOURCE_KEY);
    documents = sfr.getDB().getCollection(COLLECTION);
  }

  @Test
  public void testTaskProducesValidModelFile() throws Exception {

    File modelFile = modelPath.toFile();
    assertTrue(modelFile.exists());

    Classifier classifier = new FileObject<Classifier>(modelFile.getPath()).object();
    assertTrue(classifier.getLabelAlphabet().contains("pos"));
    assertTrue(classifier.getLabelAlphabet().contains("neg"));

    Pipe pipe = classifier.getInstancePipe();
    InstanceList instanceList = new InstanceList(pipe);
    instanceList.addThruPipe(
        new Instance("I love this amazing awesome classifier.", null, null, null));
    instanceList.addThruPipe(new Instance("I can't stand this horrible test.", null, null, null));

    assertEquals(
        "pos", classifier.classify(instanceList.get(0)).getLabeling().getBestLabel().toString());
    assertEquals(
        "neg", classifier.classify(instanceList.get(1)).getLabeling().getBestLabel().toString());
  }

  @Test
  public void testTaskSavesValuesToMongo() throws Exception {
    FindIterable<Document> find = documents.find();
    MongoCursor<Document> iterator = find.iterator();
    int count = 0;
    ImmutableList<String> labels = ImmutableList.of("pos", "neg");
    while (iterator.hasNext()) {
      Document document = iterator.next();
      String classification = document.getString(CLASSIFICATION_FIELD);
      assertTrue(labels.contains(classification));
      count++;
    }

    assertEquals(16, count);
  }
}
