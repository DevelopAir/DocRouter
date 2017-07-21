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

abstract class Observer {
	protected Subject subject;
	public abstract void update();
}

public class CADEngine extends Observer {
	
	private static final Logger logger = Logger.getLogger("com.duncanson.DocRouter");
	private OpCodeML theOpCode;
	private boolean running = false;
	
	public void update() {
		running = subject.getRunningState();
	}
	
	public CADEngine(Subject subject) {
		this.subject = subject;
		this.subject.add(this);
	}
	
	/**
	 * retrieveArrayOfOperandNames - utilizes reflection to capture operands from Operands.java.
	 * @return List of names for each operand routine.
	 */
	private List<String> retrieveOperandNames() {
		List<String> arrayOfOperandNames = new ArrayList<String>();
		try {
			// Using reflection capture an array of all operand names.
			Method[] publicMethods = Class.forName("com.duncanson.DocRouter.Operands").getMethods();
		
			System.out.println("---------\nOperands:\n---------");
			
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
			System.out.println("---------");
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
	 * @param startShapeID - Unique Shape Identifier that will be performed first.  Designated in Libre Office's *.fodg file with xml:id tag.
	 * @param runContinuously - true -> restart to startShapeName after reaching end of work flow.  Used for processing a stream of files.
	 *                          false -> exit when reaching end of work flow.
	 *                          
	 * @return - true -> No errors, false otherwise
	 */
	public boolean doRun(String OpCodeMLFileName, String startShapeID, boolean runContinuously) {
		
	    File localFS = new File(OpCodeMLFileName);
	    if (!localFS.exists() || localFS.isDirectory()) {
	    	
	    	String errorMessage = "Specified OpCode file name '"+OpCodeMLFileName+"' not found or is a directory.";
	    	System.out.println(errorMessage);
	    	logger.severe(errorMessage);
	    	return false;
	    }
		
		theOpCode = new OpCodeML(OpCodeMLFileName, startShapeID);
		
		if (theOpCode.loadDOM() == true) {
			String errorMessage = "The OpCode File, " + theOpCode.getOpCodeFileName() + ", has been successfully parsed into an OpCode array for invocation from the CADEngine object.";
			System.out.println(errorMessage);
		} else {
			String errorMessage = "Specified OCML file '"+theOpCode.getOpCodeFileName()+"' could not be loaded.  Is it valid XML and does referenced file exist?";
			logger.severe(errorMessage);
			System.exit(0);
		}
		
		// Retrieve list of operand names using reflection from the 'Operands' class.
		List<String> arrayOfOperandNames = retrieveOperandNames();
		
		if (arrayOfOperandNames.isEmpty()) {
			logger.severe("CADEngine requires at least one Operand to be defined in Operands.java.");
			System.exit(0);
		}
		
		Operands theOperands = new Operands(subject);
		
		String currentOperand = "";
		
		running = true;
		try {
			
			// CADEngine run loop
			while (running) {
				
				currentOperand = theOpCode.getCurrentOperand();
				List<String> nextArgs = theOpCode.getCurrentArgs();
				
				System.out.println(currentOperand+"("+nextArgs.toString()+")");
				
				Method nextMethod = Operands.class.getMethod(currentOperand, List.class);
				
				String returnValue = (String) nextMethod.invoke(theOperands, nextArgs);
				
				if (running) {
					if (returnValue.length() < 20) {
						System.out.println("returnValue:\n-----\n"+returnValue+"\n-----");
					} else {
						System.out.println("returnValue:\n-----\n"+returnValue.substring(0,20)+"...\n-----");
					}

					// get next shape id
					String nextNode = theOpCode.getNextNode(returnValue);
					
					// If the last operand invoked did not have any to-connectors (i.e. it reached the end shape) 
					if (nextNode.length() < 1) {
						if (runContinuously) {
							// Reset to initial start shape for subsequent run.
							theOpCode.resetOpCode();
						} else {
							System.out.println("Process complete.");
							running = false;
						}
					}
				}
	        }
			
		} catch(Exception e) {
			logger.severe("Exception occured in main run loop at "+currentOperand+". "+e.toString());
		}
			
		System.out.println("----- DocRouter ending succesfully. -----");
		return true;
	}
	
	public static void main(String[] args) {
		Subject subject = new Subject();
		
		CADEngine theCADEngine = new CADEngine(subject);
		
		// Start the run loop.
		theCADEngine.doRun("./src/main/java/com/duncanson/DocRouter/DocRouterOpCode.fodg", "id7", true);	
	}
}

