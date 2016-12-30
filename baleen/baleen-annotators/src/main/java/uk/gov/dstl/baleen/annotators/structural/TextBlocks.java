package uk.gov.dstl.baleen.annotators.structural;

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
 * The Class TextBlocks.
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
  @ConfigurationParameter(name = PARAM_TYPE_NAMES)
  private String[] typeNames;

  private Set<Class<?>> structuralClasses;

  @Override
  public void doInitialize(final UimaContext aContext) throws ResourceInitializationException {
    super.doInitialize(aContext);

    if (typeNames == null || typeNames.length > 0) {
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

      structures.stream().filter(structuralClasses::contains)
          .map(s -> new Text(jCas, s.getBegin(), s.getEnd()))
          .forEach(this::addToJCasIndex);


      // Now remove any that cover others, so we keep only the most detailed
      // TODO: This could be a parameter (keep the largest, keep the smallest)
      final Map<Text, Collection<Text>> covering =
          JCasUtil.indexCovering(jCas, Text.class, Text.class);
      covering.values().stream().flatMap(Collection::stream)
          .forEach(this::removeFromJCasIndex);
    }
  }

}
