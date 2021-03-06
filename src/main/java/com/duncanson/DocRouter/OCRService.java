/**
 * 
 * OCRService provides abstraction layer between Tesseract-OCR and this objects' consumer.
 *
 * @since 07/08/2017
 */
package com.duncanson.DocRouter;

import static org.bytedeco.javacpp.lept.pixDestroy;
import static org.bytedeco.javacpp.lept.pixRead;
//import static org.junit.Assert.assertTrue;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept.PIX;
import org.bytedeco.javacpp.tesseract.TessBaseAPI;
import java.util.logging.Logger;
 
public class OCRService {
    private static final Logger logger = Logger.getLogger("com.duncanson.DocRouter");
    private TessBaseAPI api;
    private int tesseractInitError;
    
	/**
	 * doOCR translates provided document image file (i.e. *.png, jpg, tiff, etc.) into a text string. 
	 * @param docImageFileOrDirName - name of image file (i.e. *.png, *.tiff, *.jpg) or name of containing directory
	 * @return - string of characters recognized from image file
	 * @throws Exception
	 */
    
    public OCRService() {    	
    	
	    api = new TessBaseAPI();
		    
	    /** Initialize tesseract-ocr without tessdata path to use English  */
	    tesseractInitError = api.Init(".", "ENG");
	    
	    if (tesseractInitError != 0) {
	    	logger.severe("Initialization of tesseract failed with error: " + tesseractInitError + ".");
	    }
    }
    
	public String doOCR(String docImageFileName) throws Exception {
    	
	    BytePointer outText;
	    String recognizedText = "";
	    
	    if (tesseractInitError == -1) {
	    	return "";
	    }
	    
	    try {
	    	
	      System.out.println("docImageFileName "+docImageFileName);
	
	      // Open specified image with leptonica library
	      PIX image = pixRead(docImageFileName);
	      api.SetImage(image);
	    
	      // Get OCR result in UTF8
	      outText = api.GetUTF8Text();
	      recognizedText = outText.getString();
	    
	      if (recognizedText.isEmpty()) {
	    	  System.out.println("No recognized characters found in "+docImageFileName);
	      }
	    
	      outText.deallocate();
	      pixDestroy(image);
	    }
	    
	    catch (Exception e) {
	    	recognizedText = "";
	    	String errorDescriptor = "Exception error in ";
	    	System.err.println(errorDescriptor+e);
	    	logger.severe(errorDescriptor+e);
	    	throw e;
	    } 
	    return recognizedText;
	}
	
	protected void finalize() {
	      api.End();
	      api.close();
	}

	/**
	 * main - unit tests and demonstrates how to instantiate and use OCRService
	 * @param args - not used
	 */
	public static void main(String[] args) {
		OCRService theOCRService = new OCRService();
		
		try {
			String docInString = theOCRService.doOCR("./DocImages/test.png");
			System.out.println("Captured the following text\n" + docInString);
			
			HDFSModel persistToHDFS = new HDFSModel();
			
			persistToHDFS.saveFile(".\\DocImages\\", "test.txt", ".\\", "test.txt");
		}
		
		catch (Exception e) {
			System.out.println("Exception error occured in doOCR with " + e.toString());
		}
	}
}
