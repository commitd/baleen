// Dstl (c) Crown Copyright 2015
package uk.gov.dstl.baleen.annotators.language;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import uk.gov.dstl.baleen.exceptions.BaleenException;
import uk.gov.dstl.baleen.resources.SharedOpenNLPModel;
import uk.gov.dstl.baleen.types.language.PhraseChunk;
import uk.gov.dstl.baleen.types.language.Sentence;
import uk.gov.dstl.baleen.types.language.WordToken;
import uk.gov.dstl.baleen.uima.BaleenTextAwareAnnotator;
import uk.gov.dstl.baleen.uima.data.TextBlock;

/**
 * Annotate linguistic features using the OpenNLP libraries
 * 
 * <p>
 * The document content is passed through the OpenNLP Tokenizer, Sentence Detector, Part of Speech
 * Tagger, and Chunker (in that order). The appropriate annotations and properties are added to the
 * CAS, and associations between the relevant annotations (e.g. WordTokens associated with the
 * Sentence) are made.
 * </p>
 * 
 * 
 * 
 */
public class OpenNLP extends BaleenTextAwareAnnotator {
  /**
   * OpenNLP Resource (Tokens)
   * 
   * @baleen.resource uk.gov.dstl.baleen.resources.SharedOpenNLPModel
   */
  public static final String KEY_TOKEN = "tokens";
  @ExternalResource(key = KEY_TOKEN)
  SharedOpenNLPModel tokensModel;

  /**
   * OpenNLP Resource (Sentences)
   * 
   * @baleen.resource uk.gov.dstl.baleen.resources.SharedOpenNLPModel
   */
  public static final String KEY_SENTENCES = "sentences";
  @ExternalResource(key = KEY_SENTENCES)
  SharedOpenNLPModel sentencesModel;

  /**
   * OpenNLP Resource (Part of Speech Tags)
   * 
   * @baleen.resource uk.gov.dstl.baleen.resources.SharedOpenNLPModel
   */
  public static final String KEY_POS = "posTags";
  @ExternalResource(key = KEY_POS)
  SharedOpenNLPModel posModel;

  /**
   * OpenNLP Resource (Phrase Chunks)
   * 
   * @baleen.resource uk.gov.dstl.baleen.resources.SharedOpenNLPModel
   */
  public static final String KEY_CHUNKS = "phraseChunks";
  @ExternalResource(key = KEY_CHUNKS)
  SharedOpenNLPModel chunkModel;

  private SentenceDetectorME sentenceDetector;
  private TokenizerME wordTokenizer;
  private POSTaggerME posTagger;
  private ChunkerME phraseChunker;

  private final Set<String> prepositions = new HashSet<String>(
      Arrays.asList("about", "above", "across", "against", "amid", "around", "at", "atop",
          "behind", "below", "beneath", "beside", "between", "beyond", "by", "for", "from",
          "down", "in", "including", "inside", "into", "mid", "near", "of", "off", "on", "onto",
          "opposite", "out",
          "outside", "over", "round", "through", "throughout", "to", "under", "underneath", "with",
          "within", "without"));

  @Override
  public void doInitialize(final UimaContext aContext) throws ResourceInitializationException {
    try {
      tokensModel.loadModel(TokenizerModel.class, getClass().getResourceAsStream("en_token.bin"));
      sentencesModel.loadModel(SentenceModel.class, getClass().getResourceAsStream("en_sent.bin"));
      posModel.loadModel(POSModel.class, getClass().getResourceAsStream("en_pos_maxent.bin"));
      chunkModel.loadModel(ChunkerModel.class, getClass().getResourceAsStream("en_chunker.bin"));
    } catch (final BaleenException be) {
      getMonitor().error("Unable to load OpenNLP Language Models", be);
      throw new ResourceInitializationException(be);
    }

    try {
      sentenceDetector = new SentenceDetectorME((SentenceModel) sentencesModel.getModel());
      wordTokenizer = new TokenizerME((TokenizerModel) tokensModel.getModel());
      posTagger = new POSTaggerME((POSModel) posModel.getModel());
      phraseChunker = new ChunkerME((ChunkerModel) chunkModel.getModel());
    } catch (final Exception e) {
      getMonitor().error("Unable to create OpenNLP taggers", e);
      throw new ResourceInitializationException(e);
    }
  }


  @Override
  protected void doProcessTextBlock(final TextBlock block) throws AnalysisEngineProcessException {
    final List<Sentence> sentences = createBaseSentences(block);

    for (final Sentence sentence : sentences) {
      final List<WordToken> wordTokens = addSentenceWordTokensWithPosTags(sentence, block);
      addSentencePhraseChunk(wordTokens, block);
    }
  }

  @Override
  public void doDestroy() {
    tokensModel = null;
    sentencesModel = null;
    posModel = null;
    chunkModel = null;
  }

  /**
   * Use the OpenNLP Sentence Detector to detect sentences and add them to the JCas index
   */
  private List<Sentence> createBaseSentences(final TextBlock block)
      throws AnalysisEngineProcessException {
    final List<Sentence> sentences = new ArrayList<>();

    try {
      final String text = block.getCoveredText();
      final Span[] sentenceSpans = sentenceDetector.sentPosDetect(text);

      for (final Span sentSpan : sentenceSpans) {
        final Sentence sent =
            block.newAnnotation(Sentence.class, sentSpan.getStart(), sentSpan.getEnd());

        addToJCasIndex(sent);
        sentences.add(sent);
      }
    } catch (final Exception e) {
      throw new AnalysisEngineProcessException(e);
    }

    return sentences;
  }

  /**
   * Use the OpenNLP Word Tokenizer and POS Tagger to produce word tokens for each sentence and add
   * them to the JCas index
   */
  private List<WordToken> addSentenceWordTokensWithPosTags(final Sentence sentIn,
      final TextBlock block)
      throws AnalysisEngineProcessException {
    final List<WordToken> wordTokens = new ArrayList<>();

    try {
      String sentValue = sentIn.getCoveredText();

      if (isUpperCase(sentValue)) {
        // The sentence model was trained on mixed-case text, and assumes upper-case words are
        // proper nouns.
        // If the sentence is entirely upper-case, then make it lower case to improve accuracy.
        sentValue = sentValue.toLowerCase();
      }

      final Span[] tokens = wordTokenizer.tokenizePos(sentValue);
      final String[] words = new String[tokens.length];
      for (int a = 0; a < tokens.length; a++) {
        words[a] = tokens[a].getCoveredText(sentValue).toString();
      }
      final String[] posTags = posTagger.tag(words);

      for (int a = 0; a < tokens.length; a++) {
        final Span wordSpan = tokens[a];

        final WordToken wordToken = new WordToken(block.getJCas());
        // No need to use the block offset, as we are offseting relative to sentence
        wordToken.setBegin(sentIn.getBegin() + wordSpan.getStart());
        wordToken.setEnd(sentIn.getBegin() + wordSpan.getEnd());
        wordToken.setSentenceOrder(a);
        wordToken.setPartOfSpeech(posTags[a]);

        addToJCasIndex(wordToken);
        wordTokens.add(wordToken);
      }
    } catch (final Exception e) {
      throw new AnalysisEngineProcessException(e);
    }

    return wordTokens;
  }

  /**
   * Add phrase chunks and POS tags to a sentence
   */
  private void addSentencePhraseChunk(final List<WordToken> tokenList, final TextBlock block) {
    final List<PhraseChunk> sentPhraseChunks = new ArrayList<PhraseChunk>();

    final String[] tokens = new String[tokenList.size()];
    final String[] posTags = new String[tokenList.size()];

    int ix = 0;
    for (final WordToken token : tokenList) {
      tokens[ix] = token.getCoveredText();
      posTags[ix] = token.getPartOfSpeech();
      ix++;
    }

    final Span[] result = phraseChunker.chunkAsSpans(tokens, posTags);

    for (final Span element : result) {
      PhraseChunk chunk = new PhraseChunk(block.getJCas());

      chunk.setBegin( tokenList.get(element.getStart()).getBegin() );
      chunk.setEnd( tokenList.get(element.getEnd() - 1).getEnd() );
      chunk.setChunkType(element.getType());

      chunk = addPhraseWordsAndHead(chunk, block);
      addToJCasIndex(chunk);

      sentPhraseChunks.add(chunk);
    }
  }

  /**
   * Add constituent words and the head word to a PhraseChunk
   */
  private PhraseChunk addPhraseWordsAndHead(final PhraseChunk chunk, final TextBlock block) {
    final List<WordToken> constituentWords = new ArrayList<WordToken>();
    for (final WordToken word : JCasUtil.selectCovered(block.getJCas(), WordToken.class, chunk)) {
      constituentWords.add(word);
    }

    chunk.setConstituentWords(FSCollectionFactory.createFSArray(block.getJCas(), constituentWords));
    int headWordId = constituentWords.size() - 1;

    // Run through prior words, check for propositional - if so skip, if not break
    for (int a = constituentWords.size() - 2; a > 1; a--) {
      final WordToken wtA = constituentWords.get(a);

      // If a POS tag or word value is prepositional, end increment head word index
      if ("IN".equals(wtA.getPartOfSpeech()) || ",".equals(wtA.getPartOfSpeech())
          || prepositions.contains(wtA.getCoveredText())) {
        headWordId = a - 1;
      } else {
        headWordId = a;
        break;
      }
    }

    chunk.setHeadWord(constituentWords.get(headWordId));

    return chunk;
  }

  private static boolean isUpperCase(final String s) {
    for (final char c : s.toCharArray()) {
      if (Character.isLetter(c) && Character.isLowerCase(c)) {
        return false;
      }
    }

    return true;
  }
}
