package uk.gov.dstl.baleen.contentmappers.helpers;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import uk.gov.dstl.baleen.uima.UimaMonitor;
import uk.gov.dstl.baleen.uima.utils.UimaUtils;

/**
 * A base implementation of the content mapper providing monitor.
 *
 * Implementation need only implement map as per {@link ContentMapper}.
 */
public abstract class AbstractContentMapper implements ContentMapper {

  private UimaMonitor monitor;

  /*
   * (non-Javadoc)
   * 
   * @see
   * uk.gov.dstl.baleen.contentmappers.helpers.ContentMapper#initialize(org.apache.uima.UimaContext)
   */
  @Override
  public void initialize(final UimaContext context) throws ResourceInitializationException {
    ContentMapper.super.initialize(context);
    final String pipelineName = UimaUtils.getPipelineName(context);
    this.monitor = createMonitor(pipelineName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.gov.dstl.baleen.contentmappers.helpers.ContentMapper#destroy()
   */
  @Override
  public void destroy() {
    ContentMapper.super.destroy();
  }

  /**
   * Get the Uima monitor
   * 
   * @return monitor
   */
  protected UimaMonitor getMonitor() {
    return monitor;
  }

  /**
   * Create a monitor based on pipeline.
   * 
   * @param pipelineName
   * @return monitor (non-null)
   */
  protected UimaMonitor createMonitor(final String pipelineName) {
    return new UimaMonitor(pipelineName, this.getClass());
  }

}
