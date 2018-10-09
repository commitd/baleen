// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.translation;

import org.junit.Test;

public class TranslationExceptionTest {

  @Test(expected = TranslationException.class)
  public void canConstruct() throws TranslationException {
    throw new TranslationException();
  }

  @Test(expected = TranslationException.class)
  public void canConstructWithMessage() throws TranslationException {
    throw new TranslationException("Message");
  }

  @Test(expected = TranslationException.class)
  public void canConstructWithCause() throws TranslationException {
    throw new TranslationException(new RuntimeException());
  }

  @Test(expected = TranslationException.class)
  public void canConstructWithMessageAndCause() throws TranslationException {
    throw new TranslationException("message", new RuntimeException());
  }
}
