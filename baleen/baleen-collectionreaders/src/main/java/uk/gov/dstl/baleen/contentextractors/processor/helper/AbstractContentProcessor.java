package uk.gov.dstl.baleen.contentextractors.processor.helper;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import uk.gov.dstl.baleen.uima.UimaMonitor;
import uk.gov.dstl.baleen.uima.UimaSupport;

public abstract class AbstractContentProcessor implements ContentProcessor {


  private UimaContext context;

  private UimaSupport support;

  private UimaMonitor monitor;

  @Override
  public void initialize(UimaContext context, UimaSupport uimaSupport, UimaMonitor uimaMonitor)
      throws ResourceInitializationException {
    this.context = context;
    support = uimaSupport;
    monitor = uimaMonitor;
  }

  protected UimaMonitor getMonitor() {
    return monitor;
  }

  protected UimaSupport getSupport() {
    return support;
  }

  protected UimaContext getContext() {
    return context;
  }

  @Override
  public void destroy() {

  }

}
