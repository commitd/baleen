// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.translation.joshua;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.function.Supplier;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.uima.resource.ResourceInitializationException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;

import uk.gov.dstl.baleen.translation.TranslationException;
import uk.gov.dstl.baleen.translation.TranslationService;

/** Implementation of {@link TranslationService} for connection to an Apache Joshua server. */
public class JoshuaTranslationService implements TranslationService {

  public static final String URL = "url";
  public static final String SOURCE = "source";
  public static final String TARGET = "target";

  public static final String DEFAULT_URL = "http://localhost:61616";

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final JoshuaConfiguration configuration;

  /**
   * Construct an Josua translation service.
   *
   * <p>configuration parameters should be passed as an array in key, value order.
   *
   * <p>{@value #URL}, the full url of the Josua server (including port, defaults to {@value
   * #DEFAULT_URL}
   *
   * <p>{@value #SOURCE}, the source language (e.g. 2 or 3 letter language code)
   *
   * <p>{@value #TARGET}, the target language (e.g. 2 or 3 letter language code)
   *
   * @param configuration
   * @throws ResourceInitializationException if can not be correctly configured
   */
  public JoshuaTranslationService(String[] configuration) throws ResourceInitializationException {
    this(
        JoshuaConfiguration.create(Arrays.asList(configuration)),
        new ObjectMapper(),
        DefaultHttpClient::new);
  }

  @VisibleForTesting
  protected JoshuaTranslationService(
      JoshuaConfiguration configuration,
      ObjectMapper objectMapper,
      Supplier<HttpClient> clientSupplier) {
    this.configuration = configuration;
    this.objectMapper = objectMapper;
    httpClient = clientSupplier.get();
  }

  @Override
  public String translate(String input) throws TranslationException {

    try {
      URIBuilder builder = new URIBuilder(configuration.getUrl()).setParameter("q", input);
      HttpGet httpget = new HttpGet(builder.build());

      HttpResponse response = httpClient.execute(httpget);
      HttpEntity entity = response.getEntity();

      if (entity == null) {
        throw new TranslationException("No translation in response:" + response.toString());
      }
      return readResponse(httpget, entity);
    } catch (IOException | URISyntaxException e) {
      throw new TranslationException(e);
    }
  }

  private String readResponse(HttpGet httpget, HttpEntity entity) throws TranslationException {
    try (InputStream instream = entity.getContent()) {
      JoshuaResponse joshuaResponse = objectMapper.readValue(instream, JoshuaResponse.class);
      return joshuaResponse.getData().getTranslations().get(0).getTranslatedText();
    } catch (IndexOutOfBoundsException | IOException ex) {
      throw new TranslationException(ex);
    } catch (RuntimeException ex) {
      httpget.abort();
      throw new TranslationException(ex);
    }
  }

  @Override
  public void close() {
    httpClient.getConnectionManager().shutdown();
  }

  @Override
  public String getSourceLanguage() {
    return configuration.getSource();
  }

  @Override
  public String getTargetLanguage() {
    return configuration.getTarget();
  }
}
