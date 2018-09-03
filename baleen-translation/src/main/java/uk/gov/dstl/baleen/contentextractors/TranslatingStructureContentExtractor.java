// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.contentextractors;

import java.util.List;

import org.apache.uima.fit.descriptor.ExternalResource;
import org.jsoup.nodes.Node;

import uk.gov.dstl.baleen.contentextractors.helpers.DocumentToJCasConverter;
import uk.gov.dstl.baleen.contentmappers.helpers.ContentMapper;
import uk.gov.dstl.baleen.resources.SharedTranslationResource;
import uk.gov.dstl.baleen.translation.TranslationException;

/**
 * Translates the output of the {@link
 * uk.gov.dstl.baleen.contentextractors.StructureContentExtractor} using the {@link
 * SharedTranslationResource}.
 *
 * <p>Attempts to preserve the extracted structure, however this may negatively affect the accuracy
 * of the translation.
 *
 * @baleen.javadoc
 */
public class TranslatingStructureContentExtractor extends StructureContentExtractor {

  /**
   * Translation service
   *
   * @baleen.resource uk.gov.dstl.baleen.resources.SharedTranslationResource
   */
  public static final String RESOURCE_KEY = SharedTranslationResource.RESOURCE_KEY;

  @ExternalResource(key = RESOURCE_KEY)
  protected SharedTranslationResource translationService;

  /** Default constructor for UIMA */
  public TranslatingStructureContentExtractor() {
    // DO NOTHING
  }

  /** Constructor for testing */
  protected TranslatingStructureContentExtractor(SharedTranslationResource translationService) {
    this.translationService = translationService;
  }

  @Override
  protected DocumentToJCasConverter createDocumentConverter(List<ContentMapper> mappers) {
    return new DocumentToJCasConverter(mappers) {
      @Override
      protected String mapToText(Node node) {
        String text = super.mapToText(node);
        try {
          return translationService.translate(text);
        } catch (TranslationException e) {
          return text;
        }
      }
    };
  }
}
