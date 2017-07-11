/**
 * HDFSModel - data abstraction layer providing accessor functions for reading and writing files 
 *             to and from HDFS.
 *
 * @since 07/08/2017
 */

package com.duncanson.DocRouter;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.net.URI;
import java.util.logging.Logger;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

public class HDFSModel {
	private Configuration conf;
	private String hdfsURI = "hdfs://localhost:9000";
	private static final Logger logger = Logger.getLogger("com.duncanson.DocRouter");
	private FileSystem fs;
	
	public HDFSModel() {
	  try {
        // Initialize HDFS File System Object
        conf = new Configuration();
      
        // Set FileSystem URI
        conf.set("fs.defaultFS", hdfsURI);
      
        // For Maven integration
        conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
        conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
      
        // Set HADOOP user
        System.setProperty("HADOOP_USER_NAME", "pduncanson");
        System.setProperty("hadoop.home.dir", "/");
      
        fs = FileSystem.get(URI.create(hdfsURI), conf);
        
	  }
	  
	  catch(Exception e) {
		  String errorDescriptor = "Exception occured in "+this.getClass().getSimpleName()+" Error Message: "+e.getMessage()+" Stack Trace: "+e.getStackTrace();
		  System.out.println(errorDescriptor);
		  logger.severe(errorDescriptor);
	  }
	}

	/**
	 * saveFile will persist specified local file to specified HDFS location.
	 * @param localPathName - local path name excluding file name
	 * @param localFileName - local file name
	 * @param outputHDFSPathName - pathname within HDFS URI
	 * @param outputHDFSFileName - filename to be used for created HDFS file
	 * @return true or false to indicate success of save procedure.
	 * @throws Exception
	 */
	public boolean saveFile(String localPathName, String localFileName, String outputHDFSPathName, String outputHDFSFileName) throws Exception {
	  String readLine;
	  
	  try {
	    if (localPathName.length() < 1 || localFileName.length() < 1 || outputHDFSFileName.length() < 1) {
		   System.err.println("Invalid arguments specified for saveFile - localPathName, localFileName and outputHDFSFileName cannot be blank.");
           logger.severe("Invalid arguments specified for saveFile - localPathName, localFileName and outputHDFSFileName cannot be blank.");
           return false;
	    }
	    
	    File localFS = new File(localPathName + "/" + localFileName);
	    if (localFS.exists() && !localFS.isDirectory()) {
	    	logger.severe("Specified local file '"+localPathName+"/"+localFileName+"' to be saved in saveFile was not found.");
	    	return false;
	    }
	    
        //==== Create target folder in HDFS if it doesn't already exists
        Path newFolderPath= new Path(outputHDFSPathName);
        if(!fs.exists(newFolderPath)) {
          // Create new Directory
          fs.mkdirs(newFolderPath);
          logger.info("Path "+outputHDFSPathName+" created.");
        }

        //==== Write file
        logger.info("Start of Writing file into hdfs");
        
        //Create a path
        Path hdfswritepath = new Path(outputHDFSPathName + outputHDFSFileName);
        
        //Init output stream
        FSDataOutputStream outputStream=fs.create(hdfswritepath);
      
        // Read in the specified text file in the default encoding.
        FileReader fileReader = new FileReader(localPathName+localFileName);

        // Always wrap FileReader in BufferedReader.
        BufferedReader bufferedReader = 
            new BufferedReader(fileReader);

        while((readLine = bufferedReader.readLine()) != null) {
            System.out.println(readLine);
            outputStream.writeBytes(readLine);
        }   

        // Always close files.
        bufferedReader.close();         

        outputStream.close();
        logger.info("End Write file into hdfs");
	  }
	  
	  catch (Exception e) {
		  logger.severe("Exception error in "+this.getClass().getSimpleName()+" "+e);
		  return false;
	  }
      return true;
	}
	/**
	 * readFile will return specified hdfs file into a String
	 * @param filePath - HDFS path with appended "/" where interested file will reside.
	 * @param fileName - file name to be loaded into a returned String
	 * @return - String of entire file specified.
	 */
	public String readFile(String filePath, String fileName) {
	  String out;
	  try {
        //==== Read file
        logger.info("Read file into hdfs");
      
        //Create a path
        Path hdfsReadPath = new Path(filePath + "/" + fileName);
        
        //Initialize input stream
        FSDataInputStream inputStream = fs.open(hdfsReadPath);
      
        out = IOUtils.toString(inputStream, "UTF-8");
      
        logger.info(out);
        inputStream.close();
        fs.close();
	  }
	  
	  catch(Exception e) {
		  String error = "Exception error:"+e.toString()+" occurred in readFile.";
		  logger.severe(error);
		  System.err.println(error);
		  out = "";
	  }
      
      return out;
	}

	public static void main(String[] args) {

	}
}
