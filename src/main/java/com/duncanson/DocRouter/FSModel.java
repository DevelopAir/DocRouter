
package com.duncanson.DocRouter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

/**
 * FSModel - For saving and loading files to and from the local file system.
 * 
 * Note: This implementation is useful for debugging purposes to provide
 *       a comparable means for saving and reading data.  This approach also circumvents
 *       the need to setup and deploy HDFS when simply wanting to verify functionality
 *       with a mock up design (i.e. prototyping) or a one-off solution.
 *       
 *       To switch out with HDFS simply replace FSModel's readFile() and saveFile() routines 
 *       with HDFSModel's comparable readFile() and saveFile() routines.
 *       
 *       One can also simply import the local files generated into hdfs manually.
 *       
 */
public class FSModel {
	private static final Logger logger = Logger.getLogger("com.duncanson.DocRouter");
	
	public boolean createDir(String directoryName) {
		boolean successful;
		
		File fileDirName = new File(directoryName);
		
		if (fileDirName.exists() && fileDirName.isDirectory()) {
			successful = true;
		} else {
			successful = fileDirName.mkdir();
		}
		
		return successful;
	}
	
	public List<String> readFile(String fileName) throws IOException {
		
		List<String> lines;
		try {
			Path currentPathToFile = Paths.get(fileName);
			//Charset charset = Charset.forName("US-ASCII");
			Charset charset = Charset.forName("ISO-8859-1");
			
			lines = Files.readAllLines(currentPathToFile, charset);
			for (String line : lines) {
				System.out.println("line read: " + line);
			}
		}
		catch(IOException e) {
			throw e;
		}
		return lines;
	}

	public boolean saveFile(String fileName, String dataToWrite) throws IOException {
		
		try {
			String pathName = "";
			// First check if path exists
			int startOfFileNameLoc = fileName.lastIndexOf("/");
			
			if (startOfFileNameLoc > -1) {
				pathName = fileName.substring(0, startOfFileNameLoc);
			}
			if (createDir(pathName) == false) {
				logger.severe("Cannot create directory for "+pathName+" to save "+fileName+".");
				return false;
			}
			
			Files.write(Paths.get(fileName), dataToWrite.getBytes());
			
		}
		catch (IOException e) {
			throw e;
		}
		return true;
	}
	
	public static void main(String[] args) {
	    String currentDirectory;
	    File file = new File(".");
		currentDirectory = file.getAbsolutePath();
		
		System.out.println("Current Working Directory: "+currentDirectory);
		
		FSModel localFS = new FSModel();
		
		boolean successful;
		
		//successful = localFS.createDir("/Users/pduncanson/Duncanson/DocRouter/Other");
		successful = localFS.createDir("./Other");
		
		System.out.println("createDir returned "+successful);
		List<String> lines;
		
		try {
			lines = localFS.readFile("./tessdata/ENG.traineddata");
			
			for (String line: lines) {
				System.out.println(line);
			}
		}
		catch(IOException e) {
			System.out.println("Exception occured when attempting to local.readFile. exception is "+e.toString());
		}

	}

}
