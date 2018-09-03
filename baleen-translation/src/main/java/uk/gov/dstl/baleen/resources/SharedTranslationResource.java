// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.resources;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import uk.gov.dstl.baleen.core.utils.BuilderUtils;
import uk.gov.dstl.baleen.exceptions.InvalidParameterException;
import uk.gov.dstl.baleen.translation.TranslationException;
import uk.gov.dstl.baleen.translation.TranslationService;
import uk.gov.dstl.baleen.uima.BaleenResource;
import uk.gov.dstl.baleen.uima.utils.UimaUtils;

/**
 * <b>Shared resource for accessing TranslationServices</b>
 *
 * <p>This resource removes the need for individual Baleen components to create their own
 * translation service, instead providing a single instance for use across all services.
 *
 * <p>Different resource keys can be used to create services for different languages.
 *
 * <p>Can be configured to use different implementations of TranslationService
 *
 * @baleen.javadoc
 */
public class SharedTranslationResource extends BaleenResource implements TranslationService {

  /** default key for access to the translation resource */
  private static final String DEFAULT_PACKAGE = "uk.gov.dstl.baleen.translation";

  /** default key for access to the translation resource */
  public static final String RESOURCE_KEY = "translation";

  public static final String DEFAULT_TRANSLATION_SERVICE = "joshua.JoshuaTranslationService";

  /**
   * The translation service class to use
   *
   * @baleen.config JonahTranslationService
   */
  public static final String PARAM_SERVICE = "translation.service";

  @ConfigurationParameter(name = PARAM_SERVICE, defaultValue = DEFAULT_TRANSLATION_SERVICE)
  private String service;

  protected TranslationService delegate;

  @Override
  protected boolean doInitialize(
      ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
      throws ResourceInitializationException {
    try {
      Class<? extends TranslationService> clazz =
          BuilderUtils.getClassFromString(service, DEFAULT_PACKAGE);

      delegate =
          constructTranslationService(clazz, UimaUtils.getConfigParameters(getUimaContext()));

    } catch (NoSuchMethodException
        | SecurityException
        | InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | InvalidParameterException ex) {
      throw new ResourceInitializationException(ex);
    }

    getMonitor().info("Initialised shared translation resource");
    return true;
  }

  private TranslationService constructTranslationService(
      Class<? extends TranslationService> clazz, Map<String, Object> configuration)
      throws InstantiationException, IllegalAccessException, InvocationTargetException,
          NoSuchMethodException {
    try {
      Constructor<? extends TranslationService> constructor = clazz.getConstructor(Map.class);
      return constructor.newInstance(configuration);
    } catch (NoSuchMethodException e) {
      getMonitor().warn("No configuration based constructor for {} using default", service);
      Constructor<? extends TranslationService> constructor;
      constructor = clazz.getConstructor();
      return constructor.newInstance();
    }
  }

  @Override
  public String translate(String input) throws TranslationException {
    return delegate.translate(input);
  }

  @Override
  public String getSourceLanguage() {
    return delegate.getSourceLanguage();
  }

  @Override
  public String getTargetLanguage() {
    return delegate.getTargetLanguage();
  }

  @Override
  protected void doDestroy() {
    try {
      delegate.close();
    } catch (TranslationException e) {
      getMonitor().warn("Unable to close translation service", e);
    }
    super.doDestroy();
  }
}
