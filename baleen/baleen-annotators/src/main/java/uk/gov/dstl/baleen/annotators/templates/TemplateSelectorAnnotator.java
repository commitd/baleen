package uk.gov.dstl.baleen.annotators.templates;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dstl.baleen.cpe.CpeBuilderUtils;
import uk.gov.dstl.baleen.exceptions.InvalidParameterException;
import uk.gov.dstl.baleen.types.structure.Anchor;
import uk.gov.dstl.baleen.types.structure.Aside;
import uk.gov.dstl.baleen.types.structure.Caption;
import uk.gov.dstl.baleen.types.structure.DefinitionDescription;
import uk.gov.dstl.baleen.types.structure.DefinitionItem;
import uk.gov.dstl.baleen.types.structure.DefinitionList;
import uk.gov.dstl.baleen.types.structure.Details;
import uk.gov.dstl.baleen.types.structure.Document;
import uk.gov.dstl.baleen.types.structure.Figure;
import uk.gov.dstl.baleen.types.structure.Footer;
import uk.gov.dstl.baleen.types.structure.Header;
import uk.gov.dstl.baleen.types.structure.Heading;
import uk.gov.dstl.baleen.types.structure.Link;
import uk.gov.dstl.baleen.types.structure.ListItem;
import uk.gov.dstl.baleen.types.structure.Ordered;
import uk.gov.dstl.baleen.types.structure.Page;
import uk.gov.dstl.baleen.types.structure.Paragraph;
import uk.gov.dstl.baleen.types.structure.Preformatted;
import uk.gov.dstl.baleen.types.structure.Quotation;
import uk.gov.dstl.baleen.types.structure.Section;
import uk.gov.dstl.baleen.types.structure.Sentence;
import uk.gov.dstl.baleen.types.structure.Sheet;
import uk.gov.dstl.baleen.types.structure.Slide;
import uk.gov.dstl.baleen.types.structure.SlideShow;
import uk.gov.dstl.baleen.types.structure.SpreadSheet;
import uk.gov.dstl.baleen.types.structure.Structure;
import uk.gov.dstl.baleen.types.structure.Style;
import uk.gov.dstl.baleen.types.structure.Summary;
import uk.gov.dstl.baleen.types.structure.Table;
import uk.gov.dstl.baleen.types.structure.TableBody;
import uk.gov.dstl.baleen.types.structure.TableCell;
import uk.gov.dstl.baleen.types.structure.TableFooter;
import uk.gov.dstl.baleen.types.structure.TableHeader;
import uk.gov.dstl.baleen.types.structure.TableRow;
import uk.gov.dstl.baleen.types.structure.TextDocument;
import uk.gov.dstl.baleen.types.structure.Unordered;
import uk.gov.dstl.baleen.types.templates.TemplateField;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Annotates template fields found in documents using configurable selector queries.
 * <p>
 * The current syntax follows a very limited subset of CSS selectors. The only operators supported
 * are immediate children (>) and the pseudo-selector "nth-of-type(n)", eg: <code>
 * Document > Paragraph:nth-of-type(3)
 * </code>
 * </p>
 */
public class TemplateSelectorAnnotator extends BaleenAnnotator {

  private static final Logger LOGGER = LoggerFactory.getLogger(TemplateSelectorAnnotator.class);

  private static final String DEFAULT_STRUCTURAL_PACKAGE = "uk.gov.dstl.baleen.types.structure";

  private static final Class<?>[] DEFAULT_STRUCTURAL_CLASSES = { Anchor.class, Aside.class,
      Caption.class, DefinitionDescription.class, DefinitionItem.class, DefinitionList.class,
      Details.class, Document.class, SlideShow.class, SpreadSheet.class, TextDocument.class,
      Figure.class, Footer.class, Header.class, Heading.class, Link.class, ListItem.class,
      Ordered.class, Page.class, Sheet.class, Slide.class, Paragraph.class, Preformatted.class,
      Quotation.class, Section.class, Sentence.class, Style.class, Summary.class, Table.class,
      TableBody.class, TableCell.class, TableFooter.class, TableHeader.class, TableRow.class,
      Unordered.class };

  /**
   * A list of structural types which will be considered during template path analysis.
   * 
   * @baleen.config Paragraph,TableCell,ListItem,Aside, ...
   */
  public static final String PARAM_TYPE_NAMES = "types";
  @ConfigurationParameter(name = PARAM_TYPE_NAMES, mandatory = false)
  private String[] typeNames;

  private Map<String, Class<Structure>> structuralClasses;

  public static final String DEFAULT_DATA_TYPE = "String";

  public static final String PARAM_SELECTORS_DIRECTORY = "selectorsDirectory";

  @ConfigurationParameter(name = PARAM_SELECTORS_DIRECTORY, defaultValue = "templateSelectors")
  private String selectorsDirectory = "templateSelectors";

  private Map<String, String> selectors = new HashMap<>();

  private static final String NTH_OF_TYPE_REGEX = "nth-of-type\\((\\d+)\\)";

  private static final Pattern NTH_OF_TYPE_PATTERN = Pattern.compile(NTH_OF_TYPE_REGEX);

  @Override
  public void doInitialize(UimaContext aContext) throws ResourceInitializationException {
    super.doInitialize(aContext);
    initialiseTypes();
    readSelectors();
  }

  private void initialiseTypes() throws ResourceInitializationException {
    structuralClasses = new HashMap<>();
    if (typeNames == null || typeNames.length == 0) {
      for (Class<?> clazz : DEFAULT_STRUCTURAL_CLASSES) {
        structuralClasses.put(clazz.getSimpleName(), cast(clazz));
      }
    } else {
      for (final String typeName : typeNames) {
        try {
          Class<Structure> clazz = CpeBuilderUtils.getClassFromString(typeName,
              DEFAULT_STRUCTURAL_PACKAGE);
          structuralClasses.put(clazz.getSimpleName(), clazz);
        } catch (final InvalidParameterException e) {
          throw new ResourceInitializationException(e);
        }
      }
    }
  }

  // method to minimise scope of cast
  @SuppressWarnings("unchecked")
  private Class<Structure> cast(Class<?> clazz) {
    return (Class<Structure>) clazz;
  }

  private void readSelectors() throws ResourceInitializationException {
    final Path path = Paths.get(selectorsDirectory);
    try {
      Files.list(path).filter(Files::isRegularFile).forEach(this::readSelectorsFromPath);
    } catch (IOException e) {
      throw new ResourceInitializationException(
          new FileNotFoundException("Template selector path not found: " + path.toAbsolutePath()));
    }
  }

  private void readSelectorsFromPath(final Path path) {
    final Properties selectorsFromPath = new Properties();
    try {
      selectorsFromPath.load(Files.newBufferedReader(path, StandardCharsets.UTF_8));
      /*
       * perhaps this should map the keys to include the basename of the path they were read from as
       * an anti-collision and (during downstream annotation processing) disambiguation mechanism?
       */
      selectorsFromPath.stringPropertyNames()
          .forEach(p -> selectors.put(p, selectorsFromPath.getProperty(p)));
    } catch (IOException e) {
      LOGGER.warn("Failed to read from selectors file {}", path);
    }
  }

  @Override
  protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {
    for (Entry<String, String> selector : selectors.entrySet()) {
      processSelector(jCas, selector.getKey(), parseSelector(selector.getValue()));
    }
  }

  private void processSelector(JCas jCas, String key, List<SelectorPart> selectorParts) {
    Iterator<SelectorPart> iterator = selectorParts.iterator();

    if (iterator.hasNext()) {
      SelectorPart selector = iterator.next();
      List<Structure> candidates = JCasUtil.selectCovered(jCas, selector.type, 0,
          jCas.getDocumentText().length());
      while (iterator.hasNext()) {
        List<Structure> newCandidates = new ArrayList<>();
        selector = iterator.next();
        for (Structure structure : candidates) {
          List<Structure> covered = JCasUtil.selectCovered(selector.type, structure);
          if (selector.psuedoSelector != null) {
            Matcher matcher = NTH_OF_TYPE_PATTERN.matcher(selector.psuedoSelector);
            if (matcher.matches()) {
              int nth = Integer.parseInt(matcher.group(1));
              int parentDepth = structure.getDepth();
              int count = 0;
              for (Structure child : covered) {
                if (child.getDepth() == parentDepth + 1) {
                  count++;
                }
                if (count == nth) {
                  newCandidates.add(child);
                  break;
                }
              }
            }
          } else {
            newCandidates.addAll(covered);
          }
        }
        candidates = newCandidates;
        if (!newCandidates.isEmpty() && !iterator.hasNext()) {
          candidates.forEach(c -> addAnnotation(jCas, key, c));
        }
      }

    }
  }

  private void addAnnotation(JCas jCas, String key, Structure element) {
    TemplateField templateField = new TemplateField(jCas);
    templateField.setName(key);
    templateField.setBegin(element.getBegin());
    templateField.setEnd(element.getEnd());
    addToJCasIndex(templateField);
  }

  private List<SelectorPart> parseSelector(String value) {
    List<SelectorPart> selectorParts = new ArrayList<>();
    String[] parts = value.split("\\s*>\\s*");
    for (String part : parts) {
      int colon = part.indexOf(":");
      if (colon != -1) {
        String[] typeAndQualifier = part.split(":");
        selectorParts.add(new SelectorPart(getType(typeAndQualifier[0]), typeAndQualifier[1]));
      } else {
        selectorParts.add(new SelectorPart(getType(part)));
      }
    }
    return selectorParts;
  }

  private Class<Structure> getType(String part) {
    return structuralClasses.get(part);
  }

  private static class SelectorPart {

    private Class<Structure> type;

    private String psuedoSelector;

    private SelectorPart(Class<Structure> type) {
      this.type = type;
    }

    private SelectorPart(Class<Structure> type, String psuedoSelector) {
      this.type = type;
      this.psuedoSelector = psuedoSelector;
    }
  }

}
