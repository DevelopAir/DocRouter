/**
 *  CADEngine
 *
 *  Generates an OpCode (i.e. Operand Code) file after a clean compilation
 *  of the output of LibreOffice's Drawing application (i.e. *.fodg).
 *  <p>
 *  The 'LibreOffice Drawing' application has been chosen as the design tool
 *  that will both graphically illustrate and drive
 *  the corresponding transformations and mappings that will be performed
 *  between a given section of an OCR Document and it's desired destination.
 *  <p>
 *  Transformations to date include:
 *    - capturing address information
 *    - capturing date
 *    - capturing phone number
 *    - capturing email address
 *  <p>
 *  Supported destinations to date include:
 *     - XML results written back to a specified Hadoop database,
 *     - output to stdout/log or
 *     - a generated XML response to a restful web service
 *     
 *  @since 07/08/2017
 *
*/

package com.duncanson.DocRouter;

public class CADEngine {
	
	public doRun(OpCodeML theOCML) {
		while () paul
		String nextRoutine = theOCML.getNextNode();
	}
	
	public static void main(String[] args) {

		CADEngine theCADEngine = new CADEngine();
		
		OpCodeML theOCML = new OpCodeML("/Users/pduncanson/workspace/DocRouter/src/main/java/com/duncanson/DocRouter/DocRouterOpCode.fodg", "id1");
		
		if (theOCML.loadDOM() == true) {
			theCADEngine.doRun(theOCML);
		} else {
			System.out.println("Specified OCML file could not be loaded.");
		}
	}

}

