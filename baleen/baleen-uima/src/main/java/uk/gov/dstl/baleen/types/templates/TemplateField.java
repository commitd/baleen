

/* First created by JCasGen Thu Feb 02 11:58:46 GMT 2017 */
package uk.gov.dstl.baleen.types.templates;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import uk.gov.dstl.baleen.types.Base;


/** An field identified from a template
 * Updated by JCasGen Thu Mar 23 16:25:28 GMT 2017
 * XML source: /Users/jrfry/dev/dstl/private/baleen/baleen/baleen-uima/src/main/resources/types/template_type_system.xml
 * @generated */
public class TemplateField extends Base {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(TemplateField.class);
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
  protected TemplateField() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public TemplateField(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public TemplateField(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public TemplateField(JCas jcas, int begin, int end) {
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

  /** getter for name - gets The name of the identified template field.
   * @generated
   * @return value of the feature 
   */
  public String getName() {
    if (TemplateField_Type.featOkTst && ((TemplateField_Type)jcasType).casFeat_name == null)
      jcasType.jcas.throwFeatMissing("name", "uk.gov.dstl.baleen.types.templates.TemplateField");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TemplateField_Type)jcasType).casFeatCode_name);}
    
  /** setter for name - sets The name of the identified template field. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setName(String v) {
    if (TemplateField_Type.featOkTst && ((TemplateField_Type)jcasType).casFeat_name == null)
      jcasType.jcas.throwFeatMissing("name", "uk.gov.dstl.baleen.types.templates.TemplateField");
    jcasType.ll_cas.ll_setStringValue(addr, ((TemplateField_Type)jcasType).casFeatCode_name, v);}    
   
    
  //*--------------*
  //* Feature: source

  /** getter for source - gets 
   * @generated
   * @return value of the feature 
   */
  public String getSource() {
    if (TemplateField_Type.featOkTst && ((TemplateField_Type)jcasType).casFeat_source == null)
      jcasType.jcas.throwFeatMissing("source", "uk.gov.dstl.baleen.types.templates.TemplateField");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TemplateField_Type)jcasType).casFeatCode_source);}
    
  /** setter for source - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSource(String v) {
    if (TemplateField_Type.featOkTst && ((TemplateField_Type)jcasType).casFeat_source == null)
      jcasType.jcas.throwFeatMissing("source", "uk.gov.dstl.baleen.types.templates.TemplateField");
    jcasType.ll_cas.ll_setStringValue(addr, ((TemplateField_Type)jcasType).casFeatCode_source, v);}    
  }

    