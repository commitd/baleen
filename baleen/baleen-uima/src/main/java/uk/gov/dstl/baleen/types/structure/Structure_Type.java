
/* First created by JCasGen Thu Oct 13 13:09:13 BST 2016 */
package uk.gov.dstl.baleen.types.structure;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import uk.gov.dstl.baleen.types.Base_Type;

/** A base type for all Structure types.
 * Updated by JCasGen Thu Oct 13 15:37:31 BST 2016
 * @generated */
public class Structure_Type extends Base_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Structure.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("uk.gov.dstl.baleen.types.structure.Structure");



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Structure_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    