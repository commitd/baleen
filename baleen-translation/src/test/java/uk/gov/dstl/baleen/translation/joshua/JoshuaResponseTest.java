// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.translation.joshua;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JoshuaResponseTest {

  @Test
  public void canCreateJoshusaResponse()
      throws JsonParseException, JsonMappingException, IOException {
    String sampleOutput =
        "{\n"
            + "  \"data\": {\n"
            + "    \"translations\": [\n"
            + "      {\n"
            + "        \"translatedText\": \"Figure less than what the polls predicted, claiming more than 60 % of electoral participation.\",\n"
            + "        \"raw_nbest\": [\n"
            + "          {\n"
            + "            \"hyp\": \"figure less than what the polls predicted , claiming more than 60 % of electoral participation .\",\n"
            + "            \"totalScore\": -8.429729\n"
            + "          }\n"
            + "        ]\n"
            + "      },\n"
            + "      {\n"
            + "        \"translatedText\": \"I want taco bell\",\n"
            + "        \"raw_nbest\": [\n"
            + "          {\n"
            + "            \"hyp\": \"i want taco bell\",\n"
            + "            \"totalScore\": -3.8622975\n"
            + "          }\n"
            + "        ]\n"
            + "      }\n"
            + "    ]\n"
            + "  },\n"
            + "  \"metadata\": [\n"
            + "    \"weights tm_custom_0\\u003d0.000 tm_pt_0\\u003d0.004 tm_pt_1\\u003d0.029 tm_pt_2\\u003d0.002 tm_pt_3\\u003d0.325 tm_pt_4\\u003d0.106 tm_pt_5\\u003d0.087 OOVPenalty\\u003d0.006 WordPenalty\\u003d-0.090 lm_0\\u003d0.221 Distortion\\u003d0.094 PhrasePenalty\\u003d-0.002 lm_1\\u003d0.034\"\n"
            + "  ]\n"
            + "}";

    ObjectMapper objectMapper = new ObjectMapper();
    JoshuaResponse response = objectMapper.readValue(sampleOutput, JoshuaResponse.class);

    assertNotNull(response);
    String metadata = response.getMetadata().get(0);
    assertTrue(metadata.startsWith("weights"));
    assertTrue(metadata.endsWith("lm_1=0.034"));

    List<JoshuaTranslation> translations = response.getData().getTranslations();

    assertEquals(2, translations.size());

    JoshuaTranslation translation = translations.get(0);

    assertEquals(
        "Figure less than what the polls predicted, claiming more than 60 % of electoral participation.",
        translation.getTranslatedText());

    JoshuaNBest best = translation.getRawNBest().get(0);

    assertEquals(
        "figure less than what the polls predicted , claiming more than 60 % of electoral participation .",
        best.getHyp());
    assertEquals(-8.429729, best.getTotalScore(), 0.0);
  }
}
