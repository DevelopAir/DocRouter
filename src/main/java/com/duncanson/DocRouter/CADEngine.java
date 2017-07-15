/**
 *  CADEngine - Contains the run loop, doRun, that invokes each of the operands as illustrated
 *              by the OpCodeML file.
 *     
 *  @since 07/08/2017
 *
*/

package com.duncanson.DocRouter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.io.File;
import java.lang.reflect.Method;

public class CADEngine {
	// Where does this go?
	private static final Logger logger = Logger.getLogger("com.duncanson.DocRouter");
	private OpCodeML theOpCode;
	
	/**
	 * retrieveArrayOfOperandNames - utilizes reflection to capture operands from Operands.java.
	 * @return List of names for each operand routine.
	 */
	private List<String> retrieveArrayOfOperandNames() {
		List<String> arrayOfOperandNames = new ArrayList<String>();
		try {
			// Using reflection capture an array of all operand names.
			Method[] publicMethods = Class.forName("com.duncanson.DocRouter.Operands").getMethods();
		
			System.out.println("Operands:");
			
			String operandPackageNamePrefix = "java.lang.String com.duncanson.DocRouter.Operands.";
			for (Method method: publicMethods) {
				String nextMethod = method.toString();
				
				// Capture only the relevant operands (i.e. filter out java provided functions like equals, wait, etc.)
				int startOfOperandNameLoc = nextMethod.indexOf(operandPackageNamePrefix);

				if (startOfOperandNameLoc > -1) {
					startOfOperandNameLoc = startOfOperandNameLoc + operandPackageNamePrefix.length();
					int endOfOperandNameLoc = nextMethod.indexOf("(java.util.List)");
					if (endOfOperandNameLoc > startOfOperandNameLoc + 1) {
						String nextOperandName;
						nextOperandName = nextMethod.substring(startOfOperandNameLoc, endOfOperandNameLoc);
						System.out.println(nextOperandName);
						arrayOfOperandNames.add(nextOperandName);
					}
				}
			}
		}
		catch (Exception e) {
			arrayOfOperandNames.clear();
			logger.severe("Exception error in CADEngine doRun "+e.getStackTrace());
		}
		
		return arrayOfOperandNames;
	}
    
	/**
	 * doRun - Run loop that invokes each Operand as specified in provided OpCode file at the indicated shape identifier.
	 * 
	 * @param OpCodeMLFileName - Location of Operand Code Macro Language file.
	 * @param startShapeName - Shape name that will be performed first.
	 * @param runContinuously - true -> restart to startShapeName after reaching end of work flow.  Used for processing a stream of files.
	 *                          false -> exit when reaching end of work flow.
	 *                          
	 * @return - true -> No errors, false otherwise
	 */
	public boolean doRun(String OpCodeMLFileName, String startShapeName, boolean runContinuously) {
		
	    File localFS = new File(OpCodeMLFileName);
	    if (!localFS.exists() || localFS.isDirectory()) {
	    	
	    	String errorMessage = "Specified OpCode file name '"+OpCodeMLFileName+"' not found or is a directory.";
	    	System.out.println(errorMessage);
	    	logger.severe(errorMessage);
	    	return false;
	    }
		
		theOpCode = new OpCodeML(OpCodeMLFileName, startShapeName);
		
		if (theOpCode.loadDOM() == true) {
			String errorMessage = "The OpCode File, " + theOpCode.getOpCodeFileName() + ", has been successfully parsed into an OpCode array for invocation from the CADEngine object.";
			logger.info(errorMessage);
		} else {
			System.out.println("Specified OCML file '"+theOpCode.getOpCodeFileName()+"' could not be loaded.  Is it valid XML and does referenced file exist?");
			System.exit(0);
		}
		
		// Retrieve list of operand names using reflection from the 'Operands' class.
		List<String> arrayOfOperandNames = retrieveArrayOfOperandNames();
		
		if (arrayOfOperandNames.isEmpty()) {
			logger.severe("CADEngine requires at least one Operand to be defined in Operands.java.");
			System.exit(0);
		}
		
		// Instantiate 
		Operands theOperands = new Operands();

		boolean running = true;
		String nextOperand = "";
		try {
			
			while (running) {
				nextOperand = theOpCode.getNextOperand();
				List<String> nextArgs = theOpCode.getNextArguments();
				
				Method nextMethod = Operands.class.getMethod(nextOperand, List.class);
				
				String returnValue = (String) nextMethod.invoke(theOperands, nextArgs);
				
				System.out.println("nextOperand is "+nextOperand);
				System.out.println("nextArgs: "+nextArgs);
				System.out.println("returnValue: "+returnValue);
				
				//theOperands.getOperand(nextOperandName);
				
				//String nextNode = getNextNode(returnValue);
	        }
			
		} catch(Exception e) {
			// !!!!! when there's more time selectively capture each of the following reflection-related exceptions with distinct conditions:
			// NoSuchMethodException, IllegalAccessException, InvocationTargetException.
			logger.severe("Exception occured in main run loop at "+nextOperand+". "+e.getStackTrace());
		}
			
		return true;
	}
	
	public static void main(String[] args) {

		CADEngine theCADEngine = new CADEngine();
		
		// Start the run loop.
		theCADEngine.doRun("./src/main/java/com/duncanson/DocRouter/DocRouterOpCode.fodg", "id1", true);	
	}
}

