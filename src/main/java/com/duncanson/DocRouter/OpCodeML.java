/**
 * OpCode Macro Language - Parses the XML form of the design into an internal hashmap for data driven invocation orchestration.
 *                   
 *                         OpCodesML by design is intended to be utilized by the CADEngine to drive the order of events being performed.
 *                       
 * @since 07/08/2017
 */

package com.duncanson.DocRouter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class OpCodeML {

	public boolean done = false;
	public String currentShapeID;
	
	private String xmlOpCodeFileName;
	private String startShapeID;
	private HashMap<String, Object> shapes = new HashMap<String, Object>();
	private HashMap<String, Object> variables = new HashMap<String, Object>();
	
	private static final Logger logger = Logger.getLogger("com.duncanson.DocRouter");
	
	private final String FALSE = "[(FALSE)]";
	private final String TRUE = "[(TRUE)]";
	
	/**
	 * loadDom - populates DOM object with specified OpCodeML file and parses relevant nodes (i.e. shapes and connectors) into an internal hashmap. 
	 * @return true when successful, false otherwise
	 */
	public boolean loadDOM() {
	  boolean returnValue = false;
	  
	  try {

	    String currentDirectory;
	    File file = new File(".");
		currentDirectory = file.getAbsolutePath();
		
		System.out.println("Current Working Directory: " + currentDirectory);
		
		File fXmlFile = new File(xmlOpCodeFileName);
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);

		// optional, but recommended to improve performance and reduces white space issues.
		doc.getDocumentElement().normalize();

		System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
				
		NodeList nList = doc.getElementsByTagName("draw:custom-shape");

		System.out.println("----------------------------");

 		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node nNode = nList.item(temp);

			System.out.println("\nCurrent Element :" + nNode.getNodeName());
			
			String currentShapeID = nNode.getNodeName();
			
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
			
				if (currentShapeID.contentEquals("draw:custom-shape")) {

					Element eElement = (Element) nNode;
	
					String shapeName = eElement.getAttribute("xml:id");

					NodeList nTextList = eElement.getElementsByTagName("text:p");
					
					String shapeText = "";
					for (int i = 0; i < nTextList.getLength(); i++) {
						String nextTextNode = nTextList.item(i).getTextContent();
						int idxOfleftBracket = nextTextNode.indexOf("[");
						if (idxOfleftBracket > -1 && nextTextNode.indexOf("]") > idxOfleftBracket) {
							shapeText = nextTextNode;
							break;
						}
					}
					
					System.out.println("Shape Name : " + shapeName);
					System.out.println("Shape Text : " + shapeText);
					shapes.put(shapeName, shapeText);
				}
			}
		}
 		
		nList = doc.getElementsByTagName("draw:connector");

		System.out.println("----------------------------");

 		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node nNode = nList.item(temp);

			System.out.println("\nCurrent Element :" + nNode.getNodeName());
			
			String currentShapeID = nNode.getNodeName();
			
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
			
				if (currentShapeID.contentEquals("draw:connector")) {

					Element eElement = (Element) nNode;
	
					String shapeName = eElement.getAttribute("draw:style-name");
					String shapeText = eElement.getElementsByTagName("text:p").item(0).getTextContent();
					
					System.out.println("Shape Name : " + shapeName);
					System.out.println("Shape Text : " + shapeText);
					
					String startShape = eElement.getAttribute("draw:start-shape");
					String endShape = eElement.getAttribute("draw:end-shape");
					
					System.out.println("Start Shape : " + startShape);
					System.out.println("End Shape   : " + endShape);
					
					String matchingShape = shapes.get(startShape).toString();
					
					if (matchingShape.indexOf("%!") == -1) {
						matchingShape = matchingShape + "%!";
					}
					shapes.put(startShape, matchingShape+endShape+"%%"+shapeText+"!");
				}
			}
 		}
 		
 		System.out.println("-------------------------------------------------------------------------------------------------------");
 		System.out.println("Shapes:\nshapeID:[operandName, <operandArg(s),...>]%!<toConnectorShapeId%%[toConnectorReturnValue]!...>");
 		System.out.println("-------------------------------------------------------------------------------------------------------");
 		for (HashMap.Entry<String, Object> shape : shapes.entrySet()) {
 			System.out.println(shape.getKey()+":"+shape.getValue().toString());
 		}
 		
		returnValue = true;
		
	    } catch (Exception e) {
		  e.printStackTrace();
		  returnValue = false;
		  this.setNextNode("");
	  }
	  
	  return returnValue;
	}

	// Disable public access to a non-argument constructor 
	private OpCodeML() {
	}
	
	public OpCodeML(String strOpCodeFileName, String strstartShapeID) {
		this.setOpCodeFileName(strOpCodeFileName);
		this.setStartShapeID(strstartShapeID);
	}
	
	private void setOpCodeFileName(String inputFileName) {
		xmlOpCodeFileName = inputFileName;
	}
	
	public String getOpCodeFileName() {
		return xmlOpCodeFileName;
	}
	/**
	 * resetOpCode - called by consumer when a given set of nodes has reached a terminating shape (i.e. shape without any attached 'to-connectors')
	 */
	public void resetOpCode() {
		setNextNode(startShapeID);
	}
	
	/**
	 * getCurrentShapeID - returns the current unique shape identifier.
	 * @return - unique shape identifier
	 */
	public String getCurrentShapeID() {
		return currentShapeID;
	}

	/**
	 * getNextNode - Called when needing the next node corresponding with a given return value.
	 * @param lastReturnValue - returned from last routine.
	 * @return - Unique shape ID to perform next.
	 *
	 * Example of opCode information to be matched with lastReturnValue:
	 *  
     *    Key:Value id2:[containsTarget, docStr, “java|scala”]%!id3%%[(TRUE)]!id4%%[(FALSE)]!
     *    Key:Value id1:[doOCR,”.\DocImages\”]%!id2%%[docStr]!
     *    Key:Value id4:[saveFile, docStr, “\Other”]
     *    Key:Value id3:[saveFile, docStr, “\SoftwareDoc”]
     *
     */
	public String getNextNode(String lastReturnValue) {
		String thisShapeValue = shapes.get(currentShapeID).toString();
		int toConnectorStartLoc = thisShapeValue.indexOf("%!");
		
		String [] connectors;

		// if there's at least one connector
		if (toConnectorStartLoc > -1) {
			connectors = thisShapeValue.split("!");
		} else {
			return "";
		}
		
		if (connectors.length < 2) {
			// let this objects' consumer decide to resetOpCode when we've reached end (i.e. shape performed without any to-connectors).
			return "";
		}
		
		// Search for to-connector with matching output value
		// Note: first element after split is the current shape identifier, so skip first.
		for (int i = 1; i < connectors.length; i++) {
			System.out.println("connectors["+i+"] "+connectors[i].toString());
			String[] toShapeAndConnectorText = connectors[i].split("%%");
			String connectorText = toShapeAndConnectorText[1].trim();
			
			if (connectorText.equals(TRUE) && lastReturnValue.equals(TRUE)) {
				currentShapeID = toShapeAndConnectorText[0].trim();
				break;
			} else if (connectorText.equals(FALSE) && lastReturnValue.equals(FALSE)) {
				currentShapeID = toShapeAndConnectorText[0].trim();
				break;
			} else {
				int startOfLiteralLoc = connectorText.indexOf("\"");
				int endOfLiteralLoc = connectorText.indexOf("\"", startOfLiteralLoc + 1);
				
				// If the return value is the same as a literal specified on the to-connector shape then...
				if (startOfLiteralLoc > -1 && endOfLiteralLoc > -1) {
					String literalValue = connectorText.substring(startOfLiteralLoc+1, endOfLiteralLoc);
					
					if (lastReturnValue.equals(literalValue)) {
						// set up for branching down this to-connector
						currentShapeID = toShapeAndConnectorText[0].trim();
						break;
					}
				}
				
				if (connectors.length == 2) {
					/**
					 *  If we got this far and there's only one connector then it's because
					 *  the connector text is not a literal value and it's not a hard coded condition (i.e. [(TRUE)], [(FALSE)])
					 *  indicating that we'll need to assign the return value to the specified key variable name for future
					 *  reference.
					 */
					if (connectorText.length() > 0) {
					  String variableKey = toShapeAndConnectorText[1].trim();
					  variableKey = variableKey.substring(1,variableKey.length()-1);
					  variables.put(variableKey, lastReturnValue);
					  currentShapeID = toShapeAndConnectorText[0].trim();
					  break;
					} 
					else {
					  currentShapeID = toShapeAndConnectorText[0].trim();
					  break;
					}
				}
			}
		}
		setNextNode(currentShapeID);
		return currentShapeID;
	}

	private void setNextNode(String inputCurrentShapeID) {
		currentShapeID = inputCurrentShapeID;
	}
	
	private void setStartShapeID(String inputStartShapeID) {
		startShapeID = inputStartShapeID;
		setNextNode(startShapeID);
	}
	
	public String getStartShapeID() {
		return startShapeID;
	}
	
	public String getVariable(String inputKey) {
		return variables.get(inputKey).toString();
	}
	
	public String retreiveNextOperand() {
		return shapes.get(currentShapeID).toString();
	}
	
	/**
	 * getCurrentOperand - returns the next operands' function name.
	 * 
	 * @return
	 */
	public String getCurrentOperand() {
		// Parse the next operand function name out of the shapes hashmap that will look something like this:
		//  [doOCR,”.\DocImages\”]%!id2%%[docStr]!
		String nextOperand = shapes.get(currentShapeID).toString();
		
		String nextOperandParts[] = nextOperand.split("!");
		if (nextOperandParts.length > 0) {
			int commaLoc = nextOperandParts[0].indexOf(",");
			int firstLeftBracketLoc = nextOperandParts[0].indexOf("[");
			if (firstLeftBracketLoc > -1 && commaLoc > firstLeftBracketLoc) {
				return nextOperandParts[0].substring(firstLeftBracketLoc+1, commaLoc);
			}
			
		}
		return "";
	}

	public List<String> getCurrentArgs() {
		// Builds the next operands' arguments array out of the shapes hashmap that will look something like this:
		//
		//  [doOCR,”.\DocImages\”]%!id2%%[docStr]! or
		//  [containsTarget, docStr, "java|scala"]!
		//
		// Note: Arguments surrounded by quotes are taken as a literal string.
		//       Arguments without the quotes reference a variable name for a value 
		//       that's been previously stored in the variables' hashmap.
		//
		String currentOperandRow = shapes.get(currentShapeID).toString();
		
		int endOfFunctionArgumentsLoc = currentOperandRow.indexOf("]%!");
		
		// The deliminating characters will not exist when there are no to-connectors 
		// attached to the current shape (i.e. an end of work flow shape)
		if (endOfFunctionArgumentsLoc == -1 && currentOperandRow.length() > 0) {
			endOfFunctionArgumentsLoc = currentOperandRow.length()-1;
		}
		
		String operandNameWithArgs;
		
		ArrayList<String> returnArgList = new ArrayList<String>();
		
		try {
			if (endOfFunctionArgumentsLoc > -1) {
				operandNameWithArgs = currentOperandRow.substring(0, endOfFunctionArgumentsLoc);
			} else {
				operandNameWithArgs = "";
			}
			
			// trim off any leading and trailing spaces and remove the outer brackets.
			operandNameWithArgs = operandNameWithArgs.trim();
			operandNameWithArgs = operandNameWithArgs.substring(1, operandNameWithArgs.length());
			
			String currentOperandParts[] = operandNameWithArgs.split(",");
	
			String arg;
			
			for (int i = 1; i < currentOperandParts.length; i++) {
				arg = currentOperandParts[i];
				arg = arg.trim();
				char firstChar = arg.charAt(0);
				char lastChar = arg.charAt(arg.length()-1);
				int firstCharInt = (int) firstChar;
				int lastCharInt = (int) lastChar;
				
				if (((int)firstChar == 34 || (int)firstChar == 8221 || (int)firstChar == 8220) && 
					((int) lastChar == 34 || (int)lastChar == 8221  || (int)lastChar == 8220)) {
						// strip off the double quotes
						arg = arg.substring(1,arg.length()-1);
						returnArgList.add(arg);
				} else {
					String variableValue = getVariable(arg);
					if (!variableValue.isEmpty()) {
						returnArgList.add(variableValue);
					} else {
						System.out.println("Warning: The value for variable named '"+arg+"' was not located.  Will be passed to "+getCurrentOperand()+" as an empty value.");
						returnArgList.add("");
					}
				}
			}
		} catch (Exception e) {
			logger.info("Exception error while parsing arguments out of "+currentOperandRow+". Exception error is "+e.toString());
		}

		return returnArgList;
	}
	
	public boolean advanceOperand(String lastReturnValue) {
		// Builds the next operands' arguments array out of the shapes hashmap that will look something like this:
		//
		//  [doOCR,”.\DocImages\”]%!id2%%[docStr]! or
		//  [containsTarget, docStr, "java|scala"]!
		//
		// Note: Arguments surrounded by quotes are taken as a literal string.
		//       Arguments without the quotes reference a variable name for a value 
		//       that's been previously stored in the variables' hashmap.
		//
		String currentOperand = shapes.get(currentShapeID).toString();
		
		int startOfToConnectorsWithArgsLoc = currentOperand.indexOf("]%!") + 2;
		
		String operandNameWithArgs;
		
		if (startOfToConnectorsWithArgsLoc > 2 && startOfToConnectorsWithArgsLoc < currentOperand.length()) {
			operandNameWithArgs = currentOperand.substring(startOfToConnectorsWithArgsLoc, currentOperand.length());
		} else {
			operandNameWithArgs = "";
		}
		
		// trim off any leading and trailing spaces and remove the outer brackets.
		operandNameWithArgs = operandNameWithArgs.trim();
		operandNameWithArgs = operandNameWithArgs.substring(1, operandNameWithArgs.length());
		
		String currentOperandParts[] = operandNameWithArgs.split(",");
		
		ArrayList<String> returnArgList = new ArrayList<String>();

		String arg;
		
		for (int i = 1; i < currentOperandParts.length; i++) {
			arg = currentOperandParts[i];
			char firstChar = arg.charAt(0);
			char lastChar = arg.charAt(arg.length()-1);
			// If the next operand is a literal (i.e. wrapped in a double quote in either ascii and unibyte) then
			if (((int)firstChar == 34 || (int)firstChar == 8221) && 
				((int) lastChar == 34 || (int) lastChar == 8221)) {
				// return the value as is.
				returnArgList.add(arg);
				
			} else {
				// Retrieve corresponding value that was previously stored.
				String variableValue = getVariable(arg);
				
				if (!variableValue.isEmpty()) {
					returnArgList.add(variableValue);
				} else {
					System.out.println("Warning: The value for variable named '"+arg+"' was not located.  Will be passed to "+getCurrentOperand()+" as an empty value.");
					returnArgList.add("");
				}
			}
		}
		
		return true;
	}
	
	/**
	 * main - test application that also demonstrates how to initialize this object.  Further examples found in CADEngine object.
	 * @param args - not used.
	 */
	public static void main(String[] args) {
		
		// where is current directory?
		String current = System.getProperty("user.dir");
        System.out.println("Current working directory in Java : " + current);

		// Test routine for OCML object
		OpCodeML theOCML = new OpCodeML("./src/main/java/com/duncanson/DocRouter/DocRouterOpCode.fodg", "id1");
		
		if (theOCML.loadDOM() == true) {
			System.out.println("Successfully processed: '" + theOCML.getOpCodeFileName() + "'");
		} else {
			System.out.println("Specified OpCodeML file '" + theOCML.getOpCodeFileName() + "'could not be loaded.  Does it contain valid XML formatted tags and does it exist?");
		}
		
		System.out.println("Successfully processed: '" + theOCML.getOpCodeFileName() + "'");
		
		System.out.println("Next Operand "+theOCML.retreiveNextOperand());
		
		String currentShapeID = theOCML.getCurrentShapeID();
		
		System.out.println("Next Shape Identifier is "+currentShapeID);
		
		System.out.println("Next operand function "+theOCML.getCurrentOperand());
		
		System.out.println("Next operands argument "+theOCML.getCurrentArgs());
		
		OCRService theOCRService = new OCRService();
		
		try {
			String docInString = theOCRService.doOCR("./DocImages/test.png");
			System.out.println("Captured the following text\n" + docInString);
		}
		
		catch (Exception e) {
			System.out.println("Exception error occured in doOCR with " + e.toString());
		}
		
		System.out.println("Variable for docStr "+theOCML.getVariable("docStr"));
	}
}

