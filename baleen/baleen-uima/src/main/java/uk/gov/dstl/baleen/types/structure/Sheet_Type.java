
/* First created by JCasGen Thu Oct 13 13:31:25 BST 2016 */
package uk.gov.dstl.baleen.types.structure;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;

/** A sheet of a spread sheet.
 * Updated by JCasGen Thu Oct 13 15:37:31 BST 2016
 * @generated */
public class Sheet_Type extends Page_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Sheet.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("uk.gov.dstl.baleen.types.structure.Sheet");



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Sheet_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    