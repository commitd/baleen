//Dstl (c) Crown Copyright 2015
package uk.gov.dstl.baleen.annotators;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ExternalResourceDescription;
import org.junit.Test;

import uk.gov.dstl.baleen.annotators.language.OpenNLP;
import uk.gov.dstl.baleen.annotators.testing.AnnotatorTestBase;
import uk.gov.dstl.baleen.resources.SharedOpenNLPModel;
import uk.gov.dstl.baleen.types.language.PhraseChunk;
import uk.gov.dstl.baleen.types.language.Sentence;
import uk.gov.dstl.baleen.types.language.Text;
import uk.gov.dstl.baleen.types.language.WordToken;

public class LanguageOpenNLPTest extends AnnotatorTestBase {
	
	AnalysisEngine ae;
	
	@Override
	public void beforeTest() throws UIMAException {
		super.beforeTest();

		final ExternalResourceDescription tokensDesc = ExternalResourceFactory.createExternalResourceDescription("tokens", SharedOpenNLPModel.class);
		final ExternalResourceDescription sentencesDesc = ExternalResourceFactory.createExternalResourceDescription("sentences", SharedOpenNLPModel.class);
		final ExternalResourceDescription posDesc = ExternalResourceFactory.createExternalResourceDescription("posTags", SharedOpenNLPModel.class);
		final ExternalResourceDescription chunksDesc = ExternalResourceFactory.createExternalResourceDescription("phraseChunks", SharedOpenNLPModel.class);

		final AnalysisEngineDescription desc = AnalysisEngineFactory.createEngineDescription(OpenNLP.class, "tokens", tokensDesc, "sentences", sentencesDesc, "posTags", posDesc, "phraseChunks", chunksDesc);
		
		ae = AnalysisEngineFactory.createEngine(desc);
	}

	@Test
	public void test() throws Exception{

		final String text = "This is some text. It has three sentences. The first sentence has four words.";

		jCas.setDocumentText(text);
		SimplePipeline.runPipeline(jCas, ae);

		assertEquals(3, JCasUtil.select(jCas, Sentence.class).size()); // 3 sentences

		final Sentence s1 = JCasUtil.selectByIndex(jCas, Sentence.class, 0);
		final List<WordToken> tokens = JCasUtil.selectCovered(jCas, WordToken.class, s1);

		assertEquals(5, tokens.size()); // 5 tokens in the first sentence
		assertEquals("NN", tokens.get(3).getPartOfSpeech()); // 4th token of first sentence is a noun
		
		final List<PhraseChunk> phrases = JCasUtil.selectCovered(jCas, PhraseChunk.class, s1);
		assertEquals(3, phrases.size()); // 3 chunks in the first sentence
		assertEquals("some text", phrases.get(2).getCoveredText()); // 3rd chunk of 1st sentence is "some text"
	}
	
	@Test
    public void testWiithText() throws Exception{

        final String text = "This is some text. It has three sentences. The first sentence has four words.";
        jCas.setDocumentText(text);
        
        final Text t1 = new Text(jCas, 19, 43);
        t1.addToIndexes();
        final Text t2 = new Text(jCas, 43, jCas.getDocumentText().length());
        t2.addToIndexes();
        

        SimplePipeline.runPipeline(jCas, ae);

        assertEquals(2, JCasUtil.select(jCas, Sentence.class).size()); // 2 sentences
        
        // note due to text the first sentence annotation is the second in the text

        final Sentence s1 = JCasUtil.selectByIndex(jCas, Sentence.class, 0);
        final List<WordToken> tokens = JCasUtil.selectCovered(jCas, WordToken.class, s1);

        assertEquals(5, tokens.size()); // 5 tokens in the first sentence
        assertEquals("NNS", tokens.get(3).getPartOfSpeech()); // 4th token of first sentence is a noun
        
        final List<PhraseChunk> phrases = JCasUtil.selectCovered(jCas, PhraseChunk.class, s1);
        assertEquals(3, phrases.size()); // 3 chunks in the first sentence
        assertEquals("three sentences", phrases.get(2).getCoveredText()); // 3rd chunk of 1st sentence is "some text"
    }
}
