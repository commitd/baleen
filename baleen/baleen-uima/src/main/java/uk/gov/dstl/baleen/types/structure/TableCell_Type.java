
/* First created by JCasGen Thu Oct 13 13:31:25 BST 2016 */
package uk.gov.dstl.baleen.types.structure;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;

import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** A cell in a Table.
 * Updated by JCasGen Mon Nov 28 16:26:01 GMT 2016
 * @generated */
public class TableCell_Type extends TablePart_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = TableCell.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("uk.gov.dstl.baleen.types.structure.TableCell");



  /** @generated */
  final Feature casFeat_row;
  /** @generated */
  final int     casFeatCode_row;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getRow(int addr) {
        if (featOkTst && casFeat_row == null)
      jcas.throwFeatMissing("row", "uk.gov.dstl.baleen.types.structure.TableCell");
    return ll_cas.ll_getIntValue(addr, casFeatCode_row);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setRow(int addr, int v) {
        if (featOkTst && casFeat_row == null)
      jcas.throwFeatMissing("row", "uk.gov.dstl.baleen.types.structure.TableCell");
    ll_cas.ll_setIntValue(addr, casFeatCode_row, v);}
    
  
 
  /** @generated */
  final Feature casFeat_column;
  /** @generated */
  final int     casFeatCode_column;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getColumn(int addr) {
        if (featOkTst && casFeat_column == null)
      jcas.throwFeatMissing("column", "uk.gov.dstl.baleen.types.structure.TableCell");
    return ll_cas.ll_getIntValue(addr, casFeatCode_column);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setColumn(int addr, int v) {
        if (featOkTst && casFeat_column == null)
      jcas.throwFeatMissing("column", "uk.gov.dstl.baleen.types.structure.TableCell");
    ll_cas.ll_setIntValue(addr, casFeatCode_column, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public TableCell_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_row = jcas.getRequiredFeatureDE(casType, "row", "uima.cas.Integer", featOkTst);
    casFeatCode_row  = (null == casFeat_row) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_row).getCode();

 
    casFeat_column = jcas.getRequiredFeatureDE(casType, "column", "uima.cas.Integer", featOkTst);
    casFeatCode_column  = (null == casFeat_column) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_column).getCode();

  }
}



    