package uk.gov.dstl.baleen.uima;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import uk.gov.dstl.baleen.types.language.Text;
import uk.gov.dstl.baleen.uima.data.TextBlock;

public abstract class BaleenTextAwareAnnotator extends BaleenAnnotator {

  /**
   * A list of structural types which will be mapped to TextBlocks.
   * 
   * @baleen.config false
   */
  public static final String PARAM_WHOLE_DOCUMENT = "wholeDocument";
  @ConfigurationParameter(name = PARAM_WHOLE_DOCUMENT, defaultValue = "false")
  private boolean wholeDocumentAsText;

  @Override
  protected void doProcess(final JCas jCas) throws AnalysisEngineProcessException {
    final List<TextBlock> blocks = getTextBlocks(jCas);

    for (final TextBlock b : blocks) {
      doProcessTextBlock(b);
    }
  }

  protected void doProcessTextBlock(final TextBlock block) throws AnalysisEngineProcessException {
    // Do nothing
  }

  protected List<TextBlock> getTextBlocks(final JCas jCas) {
    if (!wholeDocumentAsText) {
      final Collection<Text> collection = JCasUtil.select(jCas, Text.class);

      // If there are no text blocks, then treat do the whole document looking for something.
      // This is effectively legacy compatibility, they will have no structural or text annotations
      // therefore this preserves the functionality of existing pipelines.
      // TODO: Perhaps this should be configurable as a parameter?

      if (!collection.isEmpty()) {
        return JCasUtil.select(jCas, Text.class).stream()
            .map(t -> new TextBlock(jCas, t))
            .collect(Collectors.toList());
      }
    }


    // Doesn't matter what we have here we create a new Text
    return Collections.singletonList(new TextBlock(jCas));

  }

}
