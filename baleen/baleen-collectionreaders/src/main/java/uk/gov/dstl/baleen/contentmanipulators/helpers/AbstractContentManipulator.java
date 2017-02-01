package uk.gov.dstl.baleen.contentmanipulators.helpers;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import uk.gov.dstl.baleen.uima.UimaMonitor;
import uk.gov.dstl.baleen.uima.utils.UimaUtils;

/**
 * A base implementation of a content manipulator which provides a monitor for logging to.
 * 
 * As per {@link ContentManipulator} only manipulate is required.
 *
 */
public abstract class AbstractContentManipulator implements ContentManipulator {

  private UimaMonitor monitor;

  /*
   * (non-Javadoc)
   * 
   * @see
   * uk.gov.dstl.baleen.contentmanipulators.helpers.ContentManipulator#initialize(org.apache.uima.
   * UimaContext)
   */
  @Override
  public void initialize(final UimaContext context) throws ResourceInitializationException {
    ContentManipulator.super.initialize(context);
    final String pipelineName = UimaUtils.getPipelineName(context);
    this.monitor = createMonitor(pipelineName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.gov.dstl.baleen.contentmanipulators.helpers.ContentManipulator#destroy()
   */
  @Override
  public void destroy() {
    ContentManipulator.super.destroy();
  }

  /**
   * Get monitor to write to.
   * 
   * @return monitor
   */
  protected UimaMonitor getMonitor() {
    return monitor;
  }

  /**
   * Create a monitor based on the pipeline name.
   * 
   * @param pipelineName
   * @return monitor (non null)
   */
  protected UimaMonitor createMonitor(final String pipelineName) {
    return new UimaMonitor(pipelineName, this.getClass());
  }

}
