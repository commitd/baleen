

/* First created by JCasGen Mon Mar 20 15:20:44 GMT 2017 */
package uk.gov.dstl.baleen.types.templates;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import uk.gov.dstl.baleen.types.Base;


/** Beginning / end marker of a record (multiple fields) in a template document, used to create record definitions for subsequent annotation of real documents.
 * Updated by JCasGen Mon Mar 20 15:20:44 GMT 2017
 * XML source: /Users/jrfry/dev/dstl/private/baleen/baleen/baleen-uima/src/main/resources/types/template_type_system.xml
 * @generated */
public class RecordDefinition extends Base {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(RecordDefinition.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected RecordDefinition() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public RecordDefinition(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public RecordDefinition(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public RecordDefinition(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: name

  /** getter for name - gets The name of the record, eg Address
   * @generated
   * @return value of the feature 
   */
  public String getName() {
    if (RecordDefinition_Type.featOkTst && ((RecordDefinition_Type)jcasType).casFeat_name == null)
      jcasType.jcas.throwFeatMissing("name", "uk.gov.dstl.baleen.types.templates.RecordDefinition");
    return jcasType.ll_cas.ll_getStringValue(addr, ((RecordDefinition_Type)jcasType).casFeatCode_name);}
    
  /** setter for name - sets The name of the record, eg Address 
   * @generated
   * @param v value to set into the feature 
   */
  public void setName(String v) {
    if (RecordDefinition_Type.featOkTst && ((RecordDefinition_Type)jcasType).casFeat_name == null)
      jcasType.jcas.throwFeatMissing("name", "uk.gov.dstl.baleen.types.templates.RecordDefinition");
    jcasType.ll_cas.ll_setStringValue(addr, ((RecordDefinition_Type)jcasType).casFeatCode_name, v);}    
  }

    