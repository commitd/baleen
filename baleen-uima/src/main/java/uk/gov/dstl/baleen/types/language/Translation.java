/* First created by JCasGen Fri Aug 24 13:05:43 BST 2018 */
package uk.gov.dstl.baleen.types.language;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import uk.gov.dstl.baleen.types.Base;

/**
 * This annotation holds a translation of the annotated text as the value. Updated by JCasGen Fri
 * Aug 24 13:33:53 BST 2018 XML source:
 * /Users/stuarthendren/git/committed/baleen/baleen/baleen-uima/src/main/resources/types/language_type_system.xml
 *
 * @generated
 */
public class Translation extends Base {
  /**
   * @generated
   * @ordered
   */
  @SuppressWarnings("hiding")
  public static final int typeIndexID = JCasRegistry.register(Translation.class);
  /**
   * @generated
   * @ordered
   */
  @SuppressWarnings("hiding")
  public static final int type = typeIndexID;
  /**
   * @generated
   * @return index of the type
   */
  @Override
  public int getTypeIndexID() {
    return typeIndexID;
  }

  /**
   * Never called. Disable default constructor
   *
   * @generated
   */
  protected Translation() {
    /* intentionally empty block */
  }

  /**
   * Internal - constructor used by generator
   *
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure
   */
  public Translation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }

  /**
   * @generated
   * @param jcas JCas to which this Feature Structure belongs
   */
  public Translation(JCas jcas) {
    super(jcas);
    readObject();
  }

  /**
   * @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA
   */
  public Translation(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }

  /**
   *
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable
   */
  private void readObject() {
    /*default - does nothing empty block */
  }

  // *--------------*
  // * Feature: language

  /**
   * getter for language - gets The language of the translation. This could be the 2 letter of 3
   * letter ISO 639 Code but is not restricted to that.
   *
   * @generated
   * @return value of the feature
   */
  public String getLanguage() {
    if (Translation_Type.featOkTst && ((Translation_Type) jcasType).casFeat_language == null)
      jcasType.jcas.throwFeatMissing("language", "uk.gov.dstl.baleen.types.language.Translation");
    return jcasType.ll_cas.ll_getStringValue(
        addr, ((Translation_Type) jcasType).casFeatCode_language);
  }

  /**
   * setter for language - sets The language of the translation. This could be the 2 letter of 3
   * letter ISO 639 Code but is not restricted to that.
   *
   * @generated
   * @param v value to set into the feature
   */
  public void setLanguage(String v) {
    if (Translation_Type.featOkTst && ((Translation_Type) jcasType).casFeat_language == null)
      jcasType.jcas.throwFeatMissing("language", "uk.gov.dstl.baleen.types.language.Translation");
    jcasType.ll_cas.ll_setStringValue(addr, ((Translation_Type) jcasType).casFeatCode_language, v);
  }

  // *--------------*
  // * Feature: translation

  /**
   * getter for translation - gets The translation result in the stated language.
   *
   * @generated
   * @return value of the feature
   */
  public String getTranslation() {
    if (Translation_Type.featOkTst && ((Translation_Type) jcasType).casFeat_translation == null)
      jcasType.jcas.throwFeatMissing(
          "translation", "uk.gov.dstl.baleen.types.language.Translation");
    return jcasType.ll_cas.ll_getStringValue(
        addr, ((Translation_Type) jcasType).casFeatCode_translation);
  }

  /**
   * setter for translation - sets The translation result in the stated language.
   *
   * @generated
   * @param v value to set into the feature
   */
  public void setTranslation(String v) {
    if (Translation_Type.featOkTst && ((Translation_Type) jcasType).casFeat_translation == null)
      jcasType.jcas.throwFeatMissing(
          "translation", "uk.gov.dstl.baleen.types.language.Translation");
    jcasType.ll_cas.ll_setStringValue(
        addr, ((Translation_Type) jcasType).casFeatCode_translation, v);
  }
}
