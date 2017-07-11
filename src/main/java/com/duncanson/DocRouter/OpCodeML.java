/**
 * OpCode Macro Language - Parses the XML form of the design into an internal hashmap for data driven invocation.
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
import java.util.HashMap;

public class OpCodeML {

	public boolean done = false;
	public String nextNodeName;
	
	private String xmlOpCodeFileName;
	private String startShapeName;
	private HashMap<String, Object> shapes = new HashMap<String, Object>();
	private HashMap<String, Object> variables = new HashMap<String, Object>();
	
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
		
		System.out.println("currentDirectory is " + currentDirectory);
		
		File fXmlFile = new File(xmlOpCodeFileName);
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);

		//optional, but recommended to improve performance and reduces white space issues.
		//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		doc.getDocumentElement().normalize();

		System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
				
		NodeList nList = doc.getElementsByTagName("draw:custom-shape");

		System.out.println("----------------------------");

 		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node nNode = nList.item(temp);

			System.out.println("\nCurrent Element :" + nNode.getNodeName());
			
			String nextNodeName = nNode.getNodeName();
			
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
			
				if (nextNodeName.contentEquals("draw:custom-shape")) {

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
			
			String nextNodeName = nNode.getNodeName();
			
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
			
				if (nextNodeName.contentEquals("draw:connector")) {

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
 		
 		for (HashMap.Entry<String, Object> shape : shapes.entrySet()) {
 			System.out.println("Key:Value "+shape.getKey()+":"+shape.getValue().toString());
 		}
 		
		returnValue = true;
		
	    } catch (Exception e) {
		  e.printStackTrace();
		  returnValue = false;
		  this.setNextNode("");
	  }
	  
	  return returnValue;
	}

	public OpCodeML() {
		this.setOpCodeFileName("src\\main\\java\\com\\duncanson\\DocRouter\\DocRouterOpCode.fodg");
		this.setStartShapeName("id1");
	}
	
	public OpCodeML(String strOpCodeFileName, String strStartShapeName) {
		this.setOpCodeFileName(strOpCodeFileName);
		this.setStartShapeName(strStartShapeName);
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
		setNextNode(startShapeName);
	}
	
	/**
	 * getNextNode - simply returns current node as it hasn't been invoked yet when consumer calls without an argument.
	 * @return - node name of shape to perform next.
	 */
	public String getNextNode() {
		return nextNodeName;
	}

	/**
	 * getNextNode - Called when needing the next node corresponding with a given return value.
	 * @param lastReturnValue - returned from last routine.
	 * @return - node name of shape to perform next.
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
		String thisShapeValue = shapes.get(nextNodeName).toString();
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
		// Note: first element after split is the current shape identifier, so ignore.
		for (int i = 1; i < connectors.length; i++) {
			String[] toShapeAndConnectorText = connectors[i].split("%%");
			String connectorText = toShapeAndConnectorText[1].trim();
			
			if (connectorText.equals("[(TRUE)]") && lastReturnValue == "0") {
				nextNodeName = toShapeAndConnectorText[0].trim();
				break;
			} else if (connectorText.equals("[(FALSE)]") && lastReturnValue == "1") {
				nextNodeName = toShapeAndConnectorText[0].trim();
				break;
			} else {
				int startOfLiteralLoc = connectorText.indexOf("\"");
				int endOfLiteralLoc = connectorText.indexOf("\"", startOfLiteralLoc + 1);
				
				// Compare return value with a literal value when specified specified
				if (startOfLiteralLoc > -1 && endOfLiteralLoc > -1) {
					String literalValue = connectorText.substring(startOfLiteralLoc+1, endOfLiteralLoc);
					
					// compare return value for match
					if (lastReturnValue.equals(literalValue)) {
						nextNodeName = toShapeAndConnectorText[0].trim();
						break;
					}
				}
				
				if (connectors.length == 2) {
					/**
					 *  If we got this far and there's only one connector then it's because
					 *  the connector text is not a literal value and it's not a hard coded condition (i.e. [(TRUE)], [(FALSE)])
					 *  indicating that we'll need to assign the return value to the indicated key variable name for future
					 *  reference.
					 */
					if (connectorText.length() > 0) {
					  variables.put(toShapeAndConnectorText[1].trim(), lastReturnValue);
					  nextNodeName = toShapeAndConnectorText[0].trim();
					  break;
					} 
					else {
					  nextNodeName = toShapeAndConnectorText[0].trim();
					  break;
					}
				}
			}
		}
		
		System.out.println("thisShapeValue is " + thisShapeValue);
		return nextNodeName;
	}

	private void setNextNode(String inputNextNodeName) {
		nextNodeName = inputNextNodeName;
	}
	
	private void setStartShapeName(String inputStartShapeName) {
		startShapeName = inputStartShapeName;
		setNextNode(startShapeName);
	}
	
	public String getStartShapeName() {
		return startShapeName;
	}
	
	public String getVariable(String inputKey) {
		return variables.get(inputKey).toString();
	}
	
	/**
	 * main - test application that also demonstrates how to initialize this object.  Further examples found in CADEngine object.
	 * @param args - not used.
	 */
	public static void main(String[] args) {
		// Test routine for OCML object
		OpCodeML theOCML = new OpCodeML("/Users/pduncanson/workspace/DocRouter/src/main/java/com/duncanson/DocRouter/DocRouterOpCode.fodg", "id1");
		
		if (theOCML.loadDOM() == true) {
			System.out.println("The OpCode File, " + theOCML.getOpCodeFileName() + ", has been successfully parsed into a privately accessible OpCode array for invocation from the CADEngine object.");
		} else {
			System.out.println("Specified OCML file could not be loaded.");
		}
		
		System.out.println("Successfully processed: '" + theOCML.getOpCodeFileName() + "'");
	}
}

