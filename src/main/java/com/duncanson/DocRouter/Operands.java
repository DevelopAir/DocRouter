/**
 * Operands - Functions run by CADEngine with orchestrated work flow defined in an OpCodeML file.
 * 
 *            OpCodeML file is generated from Libre Offices' designer application by saving the
 *            design with the top menu selection as follows:
 *            
 *            File/Save As.../File type: 'File XML ODF Drawing (.fodg)'
 *
 */
package com.duncanson.DocRouter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

public class Operands {
	private static final Logger logger = Logger.getLogger("com.duncanson.DocRouter");
	private static int getNextFileIdx;
	private static int getNextImageFileIdx;
	private static int getNextDocFileIdx;
	
	private OCRService theOCRService;
	private Subject subject;
	
	private final String TRUE = "[(TRUE)]";
	private final String FALSE = "[(FALSE)]";
	
	private FSModel persistenceModel;
	// private HDFSModel persistenceModel; // uncomment for HDFS persistence
	
	private Operands() {
		
	}
	
	public Operands(Subject inputSubject) {
		getNextFileIdx = -1;
		getNextImageFileIdx = -1;
		getNextDocFileIdx = -1;

		theOCRService = new OCRService();
		
		persistenceModel = new FSModel();
		// HDFSModel persistenceModel = new HDFSModel();  // uncomment for HDFS persistence
		
		subject = inputSubject;
	}
	
	/**
	 * getNextFile - retrieve each file from specified subdirectory.
	 * 
	 * 	 * Note: To emulate a data stream that might otherwise come from a cloud-based service this
	 *       routine was developed to serially process image files within a subdirectory while keeping
	 *       track of current file (i.e. getNextFileIdx) the way Kafka, for example, would keep track
	 *       of read location within the data stream. 
	 *       
	 * @param args
	 * @return
	 */
	public String getNextFile(List<String> args) {
		String returnValue = "";
		
		if (args.size() != 1) {
			logger.info("getNextFile requires a directory path where document images reside.");
			subject.setRunningState(false);
		}

	    File localFS = new File(args.get(0));
	    if (!localFS.exists() || !localFS.isDirectory()) {
	    	logger.severe("getNextFile cannot locate specified directory '"+args.get(0)+"' from "+System.getProperty("user.dir")+".");
	    	subject.setRunningState(false);
	    }
	    
	    File[] fileList = localFS.listFiles();
	    
	    getNextFileIdx++;
	    
    	for (; getNextFileIdx < fileList.length; getNextFileIdx++) {
    		if (fileList[getNextFileIdx].isFile()) {
    			String fileName = fileList[getNextFileIdx].getName();
    			System.out.println(fileName);
    			String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
    			
    			// Ignore the .DS_Store file on Mac OS
    			if (extension.contentEquals("DS_Store")) {
    				continue;
    			}
    		}
    	}
	    
	    while (getNextFileIdx < fileList.length && !fileList[getNextFileIdx].isFile()) {
	    	getNextFileIdx++;
	    }

	    if (getNextFileIdx < fileList.length) {
	    	File nextFile = fileList[getNextFileIdx];
	    	returnValue = nextFile.toString();
	    } else {
	    	subject.setRunningState(false);
	    }
		
		return returnValue;
	}
	
	/**
	 * getNextImageFile - retrieve each image file name (*.jpg, *.tiff, *.png) from specified subdirectory.
	 * 
	 * 	 * Note: To emulate a data stream that might otherwise come from a cloud-based service this
	 *       routine was developed to serially process image files within a subdirectory while keeping
	 *       track of current file (i.e. getNextFileIdx) the way Kafka, for example, would keep track
	 *       of read location within the data stream. 
	 *       
	 * @param args
	 * @return
	 */
	public String getNextImageFile(List<String> args) {
		String returnValue = "";
		
		if (args.size() != 1) {
			logger.info("getNextFile requires a directory path where document images reside.");
			subject.setRunningState(false);
		}

	    File localFS = new File(args.get(0));
	    if (!localFS.exists() || !localFS.isDirectory()) {
	    	logger.severe("getNextFile cannot locate specified directory '"+args.get(0)+"' from "+System.getProperty("user.dir")+".");
	    	subject.setRunningState(false);
	    }
	    
	    File[] fileList = localFS.listFiles();
	    
	    getNextImageFileIdx++;
    	
    	for (; getNextImageFileIdx < fileList.length; getNextImageFileIdx++) {
    		if (fileList[getNextImageFileIdx].isFile()) {
    			String fileName = fileList[getNextImageFileIdx].getName();
    			System.out.println(fileName);
    			String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
    			if (extension.contentEquals("jpg") || 
    				extension.contentEquals("png") || 
    				extension.contentEquals("tiff") ||
    				extension.contentEquals("bmp") ||
    				extension.contentEquals("pnm") ||
    				extension.contentEquals("jfif")) {
    				break;
    			}
    		}
    	}

	    if (getNextImageFileIdx < fileList.length) {
	    	File nextFile = fileList[getNextImageFileIdx];
	    	returnValue = nextFile.toString();
	    } else {
	    	subject.setRunningState(false);
	    }
		
		return returnValue;
	}

	/**
	 * getNextImageFile - retrieve each document file name (*.jpg, *.tiff, *.png, bmp, *.pnm, *.jfif or *.pdf) from specified subdirectory.
	 * 
	 * 	 * Note: To emulate a data stream that might otherwise come from a cloud-based service this
	 *       routine was developed to serially process image files within a subdirectory while keeping
	 *       track of current file (i.e. getNextFileIdx) the way Kafka, for example, would keep track
	 *       of read location within the data stream. 
	 *       
	 * @param args
	 * @return
	 */
	public String getNextDocFile(List<String> args) {
		String returnValue = "";
		
		if (args.size() != 1) {
			logger.severe("getNextFile requires a directory path where document images reside.");
			subject.setRunningState(false);
		}

	    File localFS = new File(args.get(0));
	    if (!localFS.exists() || !localFS.isDirectory()) {
	    	logger.severe("getNextFile cannot locate specified directory '"+args.get(0)+"' from "+System.getProperty("user.dir")+".");
	    	subject.setRunningState(false);
	    }
	    
	    File[] fileList = localFS.listFiles();
	    
	    getNextDocFileIdx++;
    	
    	for (; getNextDocFileIdx < fileList.length; getNextDocFileIdx++) {
    		if (fileList[getNextDocFileIdx].isFile()) {
    			String fileName = fileList[getNextDocFileIdx].getName();
    			System.out.println(fileName);
    			String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
    			if (extension.contentEquals("jpg") || 
    				extension.contentEquals("png") || 
    				extension.contentEquals("tiff") ||
    				extension.contentEquals("bmp") ||
    				extension.contentEquals("pnm") ||
    				extension.contentEquals("jfif") ||
    				extension.contentEquals("pdf")) {
    				break;
    			}
    		}
    	}

	    if (getNextDocFileIdx < fileList.length) {
	    	File nextFile = fileList[getNextDocFileIdx];
	    	returnValue = nextFile.toString();
	    } else {
	    	subject.setRunningState(false);
	    }
		
		return returnValue;
	}
	
	public String fileExtensionMatches(List<String> args) {
		int lastPeriodLoc = -1;
		String fileName;
		String targetFileExtension;
		if (args.size() != 2) {
			logger.severe("fileExtensionMatches requires 2 arguments: String fileName, String targetFileExtension.");
			subject.setRunningState(false);
			return FALSE;
		}
		
	    fileName = args.get(0);
	    targetFileExtension = args.get(1);
	    
	    lastPeriodLoc = fileName.lastIndexOf(".");
	    
	    if (lastPeriodLoc > -1) {
	    	if (fileName.substring(lastPeriodLoc, fileName.length()).contentEquals("."+targetFileExtension)) {
	    		return TRUE;
	    	}
	    }
	    return FALSE;
	}
	
	/**
	 * doOCR - converts document image files to a text string utilizing the Tesseract-OCR library.
	 * 
	 * @param args - file name of file to be OCR'd.  Must contain a valid relative or absolute path.
	 * @return - Text string representing recognized characters.
	 */
	public String doOCR(List<String> args) {
		String returnValue = "";
		
		if (args.size() != 1) {
			logger.info("doOCR requires an argument specifying a single image file or directory where document images reside.");
			returnValue = "";
		}
		
	    File localFS = new File(args.get(0));
	    if (!localFS.exists()) {
	    	logger.severe("doOCR cannot locate specified image file '"+args.get(0)+"'.");
	    }
	    
	    if (!localFS.isDirectory()) {
	    	try {
	    		returnValue = theOCRService.doOCR(args.get(0));
    		}
    		catch(Exception e) {
    			logger.severe("doOCR Exception error "+e.getStackTrace());
    			subject.setRunningState(false);
    		}
	    } else {
	    	logger.severe("doOCR needs a file name and not a subdirectory.  Use the getNextFile, getNextImageFile, getNextDocFile operands to retrieve files from a targeted subdirectory.");
	    	subject.setRunningState(false);
	    }
    
		return returnValue;
		
	}
	
	public String doPDFToText(List<String> args) {
		String returnValue = "";
		
		if (args.size() != 1) {
			logger.severe("doPDFToText needs 1 argument: String fileNameOfPDF.");
			return FALSE;
		}
		
	    File localFS = new File(args.get(0));
	    if (!localFS.exists()) {
	    	logger.severe("doPDFToText cannot locate specified image file '"+args.get(0)+"'.");
	    }
	    
	    if (!localFS.isDirectory()) {
			try {
			    PDDocument document = null;
			    document = PDDocument.load(new File(localFS.toString()));
			    document.getClass();
			    if (!document.isEncrypted()) {
			        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
			        stripper.setSortByPosition(true);
			        PDFTextStripper Tstripper = new PDFTextStripper();
			        returnValue = Tstripper.getText(document);
			        System.out.println("Text:" + returnValue);
			    }
			} catch (Exception e) {
				returnValue = "";
				logger.severe("Exception occured in doPDFToText: "+e.toString());
			}
	    }
		return returnValue;
	}
	
	public String containsRegex(List<String> args) {
		String returnValue = FALSE;
		
		if (args.size() != 2) {
			logger.severe("containsRegex needs 2 arguments: String stringToMatch, String regexValue.");
			returnValue = FALSE;
		}
		
		String stringToMatch = args.get(0);
		String regexValue = args.get(1);
		
		if (stringToMatch.matches(regexValue)) {
			returnValue = TRUE;
		} else {
			returnValue = FALSE;
		}
		
		return returnValue;
	}
	
	public String containsWord(List<String> args) {
		String returnValue = FALSE;
		
		if (args.size() != 2) {
			returnValue = FALSE;
		}
		
		String stringToMatch = args.get(0);
		String[] wordsToMatch = stringToMatch.split(" ");

		String[] targetWords = args.get(1).toLowerCase().trim().split("\\|");
		
		for (String word: wordsToMatch) {
			for (String targetWord: targetWords) {
				if (word.trim().toLowerCase().equals(targetWord)) {
					returnValue = TRUE;
					break;
				}
			}
		}
		return returnValue;
	}
	
	public String saveFile(List<String> args) {
		String returnValue = FALSE;
		
		if (args.size() != 3) {
			returnValue = FALSE;
		}
		
		String dataToSave = args.get(0);
		String fileNamePath = args.get(1);
		String fileName = args.get(2);
		
		int startOfFileLoc = fileName.lastIndexOf("/");
		
		if (startOfFileLoc > -1) {
			fileName = fileName.substring(startOfFileLoc+1);
		}
		
		fileName = fileNamePath+fileName;
		
		int startOfExtensionLoc = fileName.lastIndexOf(".");
		
		if (startOfExtensionLoc > -1) {
			fileName = fileName.substring(0, startOfExtensionLoc);
		}
		
		fileName = fileName+".txt";
		
		try {
			persistenceModel.saveFile(fileName, dataToSave);
			returnValue = TRUE;
			
		} catch(IOException e) {
			logger.severe("Exception occured in saveFile: "+e.toString());
			returnValue = FALSE;
		}
		
		return returnValue;
	}
	
	public String stopRunning(List<String> args) {
		subject.setRunningState(false);
		return "";
	}
	
	public static void main(String[] args) {

		Operands theOperands = new Operands();
		
		List<String> arrayOfArgs = new ArrayList<String>();
		
		arrayOfArgs.add("./DocImages/ScalaOverview.png");
		
		String returnValue = theOperands.doOCR(arrayOfArgs);
		
		System.out.println("Return value from doOCR: " + returnValue);
	}

}
