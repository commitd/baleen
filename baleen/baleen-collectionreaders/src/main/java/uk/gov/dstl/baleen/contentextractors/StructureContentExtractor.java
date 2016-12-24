// Dstl (c) Crown Copyright 2015
package uk.gov.dstl.baleen.contentextractors;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import com.tenode.baleen.extraction.Extraction;
import com.tenode.baleen.extraction.exception.ExtractionException;
import com.tenode.baleen.extraction.tika.TikaFormatExtractor;

import uk.gov.dstl.baleen.contentextractors.helpers.AbstractContentExtractor;
import uk.gov.dstl.baleen.contentextractors.helpers.DocumentToJCasConverter;
import uk.gov.dstl.baleen.contentmanipulators.helpers.ContentManipulator;
import uk.gov.dstl.baleen.contentmappers.StructuralAnnotations;
import uk.gov.dstl.baleen.contentmappers.helpers.ContentMapper;
import uk.gov.dstl.baleen.cpe.CpeBuilderUtils;
import uk.gov.dstl.baleen.exceptions.InvalidParameterException;

/**
 * Extracts metadata, structure and text content from the supplied input.
 */
public class StructureContentExtractor extends AbstractContentExtractor {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = LoggerFactory.getLogger(StructureContentExtractor.class);

  public static final String CORRUPT_FILE_TEXT = "FILE CONTENTS CORRUPT - UNABLE TO PROCESS";

  /** The Constant CONTENT_MAPPER_DEFAULT_PACKAGE. */
  public static final String CONTENT_MAPPER_DEFAULT_PACKAGE =
      "uk.gov.dstl.baleen.contentmappers";

  /** The Constant CONTENT_MAPPER_DEFAULT_PACKAGE. */
  public static final String CONTENT_MANIPULATOR_DEFAULT_PACKAGE =
      "uk.gov.dstl.baleen.contentmanipulators";

  private static final String METADATA_CONTENT_MANIPULATORS = "baleen:content-manipulators";
  private static final String METADATA_CONTENT_MAPPERS = "baleen:content-mappers";

  private List<String> contentManipulatorClasses;
  private List<String> contentMapperClasses;

  private List<ContentManipulator> manipulators = Collections.emptyList();

  private DocumentToJCasConverter documentConverter;

  private TikaFormatExtractor formatExtractor;

  @Override
  public void doInitialize(final UimaContext context, final Map<String, Object> params)
      throws ResourceInitializationException {
    super.doInitialize(context, params);

    final Object manipulatorConfig = params.get("contentManipulators");
    if (manipulatorConfig != null && manipulatorConfig instanceof String[]) {
      try {
        manipulators = createContentProcessor(ContentManipulator.class,
            CONTENT_MANIPULATOR_DEFAULT_PACKAGE, context, (String[]) manipulatorConfig);
      } catch (final InvalidParameterException e) {
        throw new ResourceInitializationException(e);
      }
    }


    List<ContentMapper> mappers;
    final Object mapperConfig = params.get("contentMappers");
    if (mapperConfig != null && mapperConfig instanceof String[]) {
      try {
        mappers = createContentProcessor(ContentMapper.class, CONTENT_MAPPER_DEFAULT_PACKAGE,
            context, (String[]) mapperConfig);
      } catch (final InvalidParameterException e) {
        throw new ResourceInitializationException(e);
      }
    } else {
      // Defaults to extraction of the Structural Annotations only
      mappers = Collections.singletonList(new StructuralAnnotations());
    }


    contentManipulatorClasses =
        manipulators.stream().map(m -> m.getClass().getName()).collect(Collectors.toList());
    contentMapperClasses =
        mappers.stream().map(m -> m.getClass().getName()).collect(Collectors.toList());

    documentConverter = new DocumentToJCasConverter(mappers);
    formatExtractor = new TikaFormatExtractor();

  }

  // Note this is checked by clazz isInstance
  @SuppressWarnings("unchecked")
  private <T> List<T> createContentProcessor(final Class<T> clazz, final String defaultPackage,
      final UimaContext context,
      final String[] classes)
      throws InvalidParameterException {
    final List<T> list = new ArrayList<>();
    for (final String c : classes) {
      try {
        final Object instance = CpeBuilderUtils.getClassFromString(c, defaultPackage).newInstance();

        if (clazz.isInstance(instance)) {
          list.add((T) instance);
        } else {
          LOGGER.warn(
              String.format("Unable to create, as %s is not of  type %s", c, clazz.getName()));
        }

      } catch (InstantiationException | IllegalAccessException e) {
        LOGGER.info("Could not find or instantiate  " + c, e);
      }
    }
    return list;
  }


  @Override
  public void doProcessStream(final InputStream stream, final String source, final JCas jCas)
      throws IOException {

    try {
      final Extraction extraction = extract(stream, source);
      final Multimap<String, String> metadata = extraction.getMetadata();

      final Document document = Jsoup.parse(extraction.getHtml());

      for (final ContentManipulator manipulator : manipulators) {
        manipulator.manipulate(document);
      }

      documentConverter.apply(document, jCas);


      // Add information on content mappers and content manipulators to the metadata
      metadata.putAll(METADATA_CONTENT_MANIPULATORS, contentManipulatorClasses);
      metadata.putAll(METADATA_CONTENT_MAPPERS, contentMapperClasses);

      // Add the metadata to the document
      metadata.entries()
          .forEach(e -> addMetadata(jCas, e.getKey(), e.getValue()));

      super.doProcessStream(stream, source, jCas);
    } catch (final Exception e) {
      getMonitor().warn("Couldn't extract structure from document '{}'", source, e);
      setCorrupt(jCas);
    }
  }

  /**
   * Perform actual extraction.
   * 
   * THis is a separate function to allow it to be overridden during testing (or by other
   * implementations).
   *
   * @param stream the stream
   * @param source the source
   * @return the extraction
   * @throws ExtractionException the extraction exception
   */
  protected Extraction extract(final InputStream stream, final String source)
      throws ExtractionException {
    return formatExtractor.parse(stream, source);
  }

  private void setCorrupt(final JCas jCas) {
    if (Strings.isNullOrEmpty(jCas.getDocumentText())) {
      jCas.setDocumentText(CORRUPT_FILE_TEXT);
    }
  }

}
