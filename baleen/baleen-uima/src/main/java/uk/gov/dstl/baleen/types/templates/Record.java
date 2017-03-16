

/* First created by JCasGen Thu Feb 16 11:18:50 GMT 2017 */
package uk.gov.dstl.baleen.types.templates;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import uk.gov.dstl.baleen.types.Base;


/** Start marker of a record (multiple fields) in a document (eg a row in a table).
 * Updated by JCasGen Thu Mar 16 15:32:13 GMT 2017
 * XML source: /Users/jrfry/dev/dstl/private/baleen/baleen/baleen-uima/src/main/resources/types/template_type_system.xml
 * @generated */
public class Record extends Base {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Record.class);
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
  protected Record() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Record(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Record(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Record(JCas jcas, int begin, int end) {
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
  //* Feature: marker

  /** getter for marker - gets Which type of record marker - either 'begin' or 'end'
   * @generated
   * @return value of the feature 
   */
  public String getMarker() {
    if (Record_Type.featOkTst && ((Record_Type)jcasType).casFeat_marker == null)
      jcasType.jcas.throwFeatMissing("marker", "uk.gov.dstl.baleen.types.templates.Record");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Record_Type)jcasType).casFeatCode_marker);}
    
  /** setter for marker - sets Which type of record marker - either 'begin' or 'end' 
   * @generated
   * @param v value to set into the feature 
   */
  public void setMarker(String v) {
    if (Record_Type.featOkTst && ((Record_Type)jcasType).casFeat_marker == null)
      jcasType.jcas.throwFeatMissing("marker", "uk.gov.dstl.baleen.types.templates.Record");
    jcasType.ll_cas.ll_setStringValue(addr, ((Record_Type)jcasType).casFeatCode_marker, v);}    
   
    
  //*--------------*
  //* Feature: name

  /** getter for name - gets The name of the record, eg Address
   * @generated
   * @return value of the feature 
   */
  public String getName() {
    if (Record_Type.featOkTst && ((Record_Type)jcasType).casFeat_name == null)
      jcasType.jcas.throwFeatMissing("name", "uk.gov.dstl.baleen.types.templates.Record");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Record_Type)jcasType).casFeatCode_name);}
    
  /** setter for name - sets The name of the record, eg Address 
   * @generated
   * @param v value to set into the feature 
   */
  public void setName(String v) {
    if (Record_Type.featOkTst && ((Record_Type)jcasType).casFeat_name == null)
      jcasType.jcas.throwFeatMissing("name", "uk.gov.dstl.baleen.types.templates.Record");
    jcasType.ll_cas.ll_setStringValue(addr, ((Record_Type)jcasType).casFeatCode_name, v);}    
  }

    