
/* First created by JCasGen Thu Oct 13 13:31:25 BST 2016 */
package uk.gov.dstl.baleen.types.structure;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;

import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** A heading in a Docuemnt.
 * Updated by JCasGen Thu Oct 13 15:37:31 BST 2016
 * @generated */
public class Heading_Type extends Structure_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Heading.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("uk.gov.dstl.baleen.types.structure.Heading");



  /** @generated */
  final Feature casFeat_level;
  /** @generated */
  final int     casFeatCode_level;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getLevel(int addr) {
        if (featOkTst && casFeat_level == null)
      jcas.throwFeatMissing("level", "uk.gov.dstl.baleen.types.structure.Heading");
    return ll_cas.ll_getIntValue(addr, casFeatCode_level);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setLevel(int addr, int v) {
        if (featOkTst && casFeat_level == null)
      jcas.throwFeatMissing("level", "uk.gov.dstl.baleen.types.structure.Heading");
    ll_cas.ll_setIntValue(addr, casFeatCode_level, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Heading_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_level = jcas.getRequiredFeatureDE(casType, "level", "uima.cas.Integer", featOkTst);
    casFeatCode_level  = (null == casFeat_level) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_level).getCode();

  }
}



    