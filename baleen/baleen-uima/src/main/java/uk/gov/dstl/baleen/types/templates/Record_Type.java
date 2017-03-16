
/* First created by JCasGen Thu Feb 16 11:18:50 GMT 2017 */
package uk.gov.dstl.baleen.types.templates;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import uk.gov.dstl.baleen.types.Base_Type;

/** Start marker of a record (multiple fields) in a document (eg a row in a table).
 * Updated by JCasGen Thu Mar 16 15:32:13 GMT 2017
 * @generated */
public class Record_Type extends Base_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Record.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("uk.gov.dstl.baleen.types.templates.Record");
 
  /** @generated */
  final Feature casFeat_marker;
  /** @generated */
  final int     casFeatCode_marker;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getMarker(int addr) {
        if (featOkTst && casFeat_marker == null)
      jcas.throwFeatMissing("marker", "uk.gov.dstl.baleen.types.templates.Record");
    return ll_cas.ll_getStringValue(addr, casFeatCode_marker);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setMarker(int addr, String v) {
        if (featOkTst && casFeat_marker == null)
      jcas.throwFeatMissing("marker", "uk.gov.dstl.baleen.types.templates.Record");
    ll_cas.ll_setStringValue(addr, casFeatCode_marker, v);}
    
  
 
  /** @generated */
  final Feature casFeat_name;
  /** @generated */
  final int     casFeatCode_name;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getName(int addr) {
        if (featOkTst && casFeat_name == null)
      jcas.throwFeatMissing("name", "uk.gov.dstl.baleen.types.templates.Record");
    return ll_cas.ll_getStringValue(addr, casFeatCode_name);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setName(int addr, String v) {
        if (featOkTst && casFeat_name == null)
      jcas.throwFeatMissing("name", "uk.gov.dstl.baleen.types.templates.Record");
    ll_cas.ll_setStringValue(addr, casFeatCode_name, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Record_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_marker = jcas.getRequiredFeatureDE(casType, "marker", "uima.cas.String", featOkTst);
    casFeatCode_marker  = (null == casFeat_marker) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_marker).getCode();

 
    casFeat_name = jcas.getRequiredFeatureDE(casType, "name", "uima.cas.String", featOkTst);
    casFeatCode_name  = (null == casFeat_name) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_name).getCode();

  }
}



    