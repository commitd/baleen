// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.translation.joshua;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;

import uk.gov.dstl.baleen.translation.TranslationException;

@RunWith(MockitoJUnitRunner.class)
public class JoshuaTranslationServiceTest {

  private static final String url = "url";
  private static final String source = "source";
  private static final String target = "target";
  private static final String input = "input";
  private static final String translation = "translation";

  @Mock private HttpClient mockHttpClient;

  @Mock private HttpGet mockHttpGet;

  @Mock private HttpResponse mockHttpResponse;

  @Mock private HttpEntity mockHttpEntity;

  @Mock private ClientConnectionManager mockConnectionManager;

  @Mock private ObjectMapper mockObjectMapper;

  @Mock private InputStream mockInputStream;

  private JoshuaConfiguration dummyConfig;

  @Before
  public void setup() throws ClientProtocolException, IOException {
    dummyConfig = new JoshuaConfiguration(url, source, target);

    when(mockHttpClient.execute(Mockito.isA(HttpGet.class))).thenReturn(mockHttpResponse);
    when(mockHttpClient.getConnectionManager()).thenReturn(mockConnectionManager);
  }

  @Test
  public void testJoshuaTranslationServiceSourceAndTarget() {

    try (JoshuaTranslationService service =
        new JoshuaTranslationService(dummyConfig, mockObjectMapper, () -> mockHttpClient)) {

      assertEquals(source, service.getSourceLanguage());
      assertEquals(target, service.getTargetLanguage());
    }

    verify(mockConnectionManager).shutdown();
    ;
  }

  @Test
  public void testJoshuaTranslationServiceTranslate()
      throws TranslationException, UnsupportedOperationException, IOException {

    JoshuaTranslations data =
        new JoshuaTranslations(ImmutableList.of(new JoshuaTranslation(translation, null)));

    JoshuaResponse joshuaResponse = new JoshuaResponse(data, ImmutableList.of());

    when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);
    when(mockHttpEntity.getContent()).thenReturn(mockInputStream);
    when(mockObjectMapper.readValue(mockInputStream, JoshuaResponse.class))
        .thenReturn(joshuaResponse);

    try (JoshuaTranslationService service =
        new JoshuaTranslationService(dummyConfig, mockObjectMapper, () -> mockHttpClient)) {
      assertEquals(translation, service.translate(input));
    }
  }

  @Test(expected = TranslationException.class)
  public void testJoshuaTranslationServiceTranslateMappingError()
      throws TranslationException, UnsupportedOperationException, IOException {

    when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);
    when(mockHttpEntity.getContent()).thenReturn(mockInputStream);
    when(mockObjectMapper.readValue(mockInputStream, JoshuaResponse.class))
        .thenThrow(new IOException());

    try (JoshuaTranslationService service =
        new JoshuaTranslationService(dummyConfig, mockObjectMapper, () -> mockHttpClient)) {
      service.translate(input);
    }
  }

  @Test(expected = TranslationException.class)
  public void testJoshuaTranslationServiceTranslateRuntimeError()
      throws TranslationException, UnsupportedOperationException, IOException {

    when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);
    when(mockHttpEntity.getContent()).thenThrow(new RuntimeException());

    try (JoshuaTranslationService service =
        new JoshuaTranslationService(dummyConfig, mockObjectMapper, () -> mockHttpClient)) {
      service.translate(input);
    }
  }

  @Test(expected = TranslationException.class)
  public void testJoshuaTranslationServiceTranslateNoTranslation()
      throws TranslationException, UnsupportedOperationException, IOException {

    JoshuaTranslations data = new JoshuaTranslations(ImmutableList.of());
    JoshuaResponse joshuaResponse = new JoshuaResponse(data, ImmutableList.of());

    when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);
    when(mockHttpEntity.getContent()).thenReturn(mockInputStream);
    when(mockObjectMapper.readValue(mockInputStream, JoshuaResponse.class))
        .thenReturn(joshuaResponse);

    try (JoshuaTranslationService service =
        new JoshuaTranslationService(dummyConfig, mockObjectMapper, () -> mockHttpClient)) {
      service.translate(input);
    }
  }

  @Test(expected = TranslationException.class)
  public void testJoshuaTranslationServiceTranslateNoEntity()
      throws TranslationException, UnsupportedOperationException, IOException {

    when(mockHttpResponse.getEntity()).thenReturn(null);

    try (JoshuaTranslationService service =
        new JoshuaTranslationService(dummyConfig, mockObjectMapper, () -> mockHttpClient)) {
      service.translate(input);
    }
  }

  @Test(expected = TranslationException.class)
  public void testJoshuaTranslationServiceTranslateConfigError()
      throws TranslationException, UnsupportedOperationException, IOException {

    JoshuaConfiguration invalidConfig = new JoshuaConfiguration("#invalidUrl#", source, target);

    try (JoshuaTranslationService service =
        new JoshuaTranslationService(invalidConfig, mockObjectMapper, () -> mockHttpClient)) {
      service.translate(input);
    }
  }
}
