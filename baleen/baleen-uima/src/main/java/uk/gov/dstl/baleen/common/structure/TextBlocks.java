package uk.gov.dstl.baleen.common.structure;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.collect.Sets;

import uk.gov.dstl.baleen.cpe.CpeBuilderUtils;
import uk.gov.dstl.baleen.exceptions.InvalidParameterException;
import uk.gov.dstl.baleen.types.language.Text;
import uk.gov.dstl.baleen.types.structure.Aside;
import uk.gov.dstl.baleen.types.structure.Caption;
import uk.gov.dstl.baleen.types.structure.DefinitionDescription;
import uk.gov.dstl.baleen.types.structure.DefinitionItem;
import uk.gov.dstl.baleen.types.structure.Details;
import uk.gov.dstl.baleen.types.structure.Heading;
import uk.gov.dstl.baleen.types.structure.ListItem;
import uk.gov.dstl.baleen.types.structure.Paragraph;
import uk.gov.dstl.baleen.types.structure.Preformatted;
import uk.gov.dstl.baleen.types.structure.Quotation;
import uk.gov.dstl.baleen.types.structure.Structure;
import uk.gov.dstl.baleen.types.structure.Summary;
import uk.gov.dstl.baleen.types.structure.TableCell;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

/**
 * Converts selected Structure annotations to Text annotations.
 * 
 * Rather than annotators need to deal with every time of structure type in order to get the right
 * text form a document. This annotator maps selected (configurable) structural types to Text
 * annotations.
 * 
 * The list of structure types to map is Paragraph, Aside, Details, ListItem, TableCell, Summary,
 * Quotation, Heading, Caption, DefinitionItem, DefinitionList, Preformatted.
 * 
 * This list can be configured by providing class names (or full qualified classes) to the types
 * field.
 * 
 * This annotator ensures that no Text annotation overlap. If they did overlap then other annotator
 * would process the same text (within two different text field) resulting in duplicate annotations.
 * You can control the overlap removal by setting the keepSmallest parameter.
 * 
 * NOTE: Test cases are in baleen-annotators.ß
 * 
 * @baleen.javadoc
 * 
 */
public class TextBlocks extends BaleenAnnotator {

  private static final String DEFAULT_PACKAGE = "uk.gov.dstl.baleen.types.structure";

  private static final Class<?>[] DEFAULT_STRUCTURAL_CLASSES = {
      Paragraph.class,
      Aside.class,
      Details.class,
      ListItem.class,
      TableCell.class,
      Summary.class,
      Quotation.class,
      Heading.class,
      Caption.class,
      DefinitionItem.class,
      DefinitionDescription.class,
      Preformatted.class
  };

  /**
   * A list of structural types which will be mapped to TextBlocks.
   * 
   * @baleen.config Paragraph,TableCell,ListItem,Aside, ...
   */
  public static final String PARAM_TYPE_NAMES = "types";
  @ConfigurationParameter(name = PARAM_TYPE_NAMES, mandatory = false)
  private String[] typeNames;

  /**
   * In order to remove overlapping Text annotations we can either remove the annotation covering
   * (biggest) or the annotations covered (smallest).
   * 
   * We default to picking the smallest units of text.
   * 
   * @baleen.config true
   */
  public static final String PARAM_KEEP_SMALLEST = "keepSmallest";
  @ConfigurationParameter(name = PARAM_KEEP_SMALLEST, defaultValue = "true")
  private boolean keepSmallest;

  private Set<Class<?>> structuralClasses;

  @Override
  public void doInitialize(final UimaContext aContext) throws ResourceInitializationException {
    super.doInitialize(aContext);

    if (typeNames == null || typeNames.length == 0) {
      structuralClasses = Sets.newHashSet(DEFAULT_STRUCTURAL_CLASSES);
    } else {
      structuralClasses = new HashSet<>();
      for (final String typeName : typeNames) {
        try {
          structuralClasses.add(CpeBuilderUtils.getClassFromString(typeName, DEFAULT_PACKAGE));
        } catch (final InvalidParameterException e) {
          throw new ResourceInitializationException(e);
        }
      }
    }


  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.gov.dstl.baleen.uima.BaleenAnnotator#doProcess(org.apache.uima.jcas.JCas)
   */
  @Override
  protected void doProcess(final JCas jCas) throws AnalysisEngineProcessException {

    final Collection<Structure> structures = JCasUtil.select(jCas, Structure.class);


    if (structures.isEmpty()) {
      // If the jCas has no structural annotations then the entire text should be marked as a text
      // block

      final int end = jCas.getDocumentText().length();
      final Text t = new Text(jCas, 0, end);
      addToJCasIndex(t);

    } else {
      // Otherwise add the types we want...

      structures.stream().filter(s -> structuralClasses.contains(s.getClass()))
          .map(s -> new Text(jCas, s.getBegin(), s.getEnd()))
          .forEach(this::addToJCasIndex);


      // Now remove any that cover others, so we keep only biggest/most detailed as per request
      final Map<Text, Collection<Text>> cover;
      if (keepSmallest) {
        cover = JCasUtil.indexCovering(jCas, Text.class, Text.class);
      } else {
        cover = JCasUtil.indexCovered(jCas, Text.class, Text.class);
      }
      cover.values().stream().flatMap(Collection::stream)
          .forEach(this::removeFromJCasIndex);
    }
  }

}
