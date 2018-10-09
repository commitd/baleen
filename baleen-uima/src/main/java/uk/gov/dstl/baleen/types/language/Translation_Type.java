/* First created by JCasGen Fri Aug 24 13:05:43 BST 2018 */
package uk.gov.dstl.baleen.types.language;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

import uk.gov.dstl.baleen.types.Base_Type;

/**
 * This annotation holds a translation of the annotated text as the value. Updated by JCasGen Fri
 * Aug 24 13:33:53 BST 2018
 *
 * @generated
 */
public class Translation_Type extends Base_Type {
  /** @generated */
  @SuppressWarnings("hiding")
  public static final int typeIndexID = Translation.typeIndexID;
  /**
   * @generated
   * @modifiable
   */
  @SuppressWarnings("hiding")
  public static final boolean featOkTst =
      JCasRegistry.getFeatOkTst("uk.gov.dstl.baleen.types.language.Translation");

  /** @generated */
  final Feature casFeat_language;
  /** @generated */
  final int casFeatCode_language;
  /**
   * @generated
   * @param addr low level Feature Structure reference
   * @return the feature value
   */
  public String getLanguage(int addr) {
    if (featOkTst && casFeat_language == null)
      jcas.throwFeatMissing("language", "uk.gov.dstl.baleen.types.language.Translation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_language);
  }
  /**
   * @generated
   * @param addr low level Feature Structure reference
   * @param v value to set
   */
  public void setLanguage(int addr, String v) {
    if (featOkTst && casFeat_language == null)
      jcas.throwFeatMissing("language", "uk.gov.dstl.baleen.types.language.Translation");
    ll_cas.ll_setStringValue(addr, casFeatCode_language, v);
  }

  /** @generated */
  final Feature casFeat_translation;
  /** @generated */
  final int casFeatCode_translation;
  /**
   * @generated
   * @param addr low level Feature Structure reference
   * @return the feature value
   */
  public String getTranslation(int addr) {
    if (featOkTst && casFeat_translation == null)
      jcas.throwFeatMissing("translation", "uk.gov.dstl.baleen.types.language.Translation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_translation);
  }
  /**
   * @generated
   * @param addr low level Feature Structure reference
   * @param v value to set
   */
  public void setTranslation(int addr, String v) {
    if (featOkTst && casFeat_translation == null)
      jcas.throwFeatMissing("translation", "uk.gov.dstl.baleen.types.language.Translation");
    ll_cas.ll_setStringValue(addr, casFeatCode_translation, v);
  }

  /**
   * initialize variables to correspond with Cas Type and Features
   *
   * @generated
   * @param jcas JCas
   * @param casType Type
   */
  public Translation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl) this.casType, getFSGenerator());

    casFeat_language = jcas.getRequiredFeatureDE(casType, "language", "uima.cas.String", featOkTst);
    casFeatCode_language =
        (null == casFeat_language)
            ? JCas.INVALID_FEATURE_CODE
            : ((FeatureImpl) casFeat_language).getCode();

    casFeat_translation =
        jcas.getRequiredFeatureDE(casType, "translation", "uima.cas.String", featOkTst);
    casFeatCode_translation =
        (null == casFeat_translation)
            ? JCas.INVALID_FEATURE_CODE
            : ((FeatureImpl) casFeat_translation).getCode();
  }
}
