# DocRouter
Java Framework that routes document images through a configurable work flow process.<p>
This work flow defines the order of events by utilizing the Libre Office designer tool with corresponding connected shapes.<p>
<p align="center">
  <img src="./WorkFlowExample.png" width="500"/>
</p>
One transformation defined in the work flow as illustrated above, doOCR, will perform Optical Character Recognition on supplied document images utilizing the <b>Tesseract-OCR</b> library.<p><p>
<b>To run the attached Java Frameework:</b><p><p>
<b>Step 1:</b>  Open the DocRouter Eclipse project.<p>
<b>Step 2:</b>  Open CADEngine.java from Eclipse.<p>
<b>Step 3:</b>  Select from Eclipse 'Run->Run As->Java Application'.<p>
<b>Step 4:</b>  Verify that created *.txt files have been placed in the ./DocRouter/Other directory.<p>
<p><b>DocRouter is made up of the following modules:</b><p><p>
<b>OpCodeML.java</b> - Parses the XML form of this illustrated design into an internal hashmap for data driven invocation of corresponding operands.<p>
<b>Operands.java</b> - Class that includes functions invoked by the CADEngine in the order defined by the OpCode file.<p>
<b>CADEngine.java</b> - Utilizes an instance of the OpCodeML class to retrieve the specified order of events and then invokes the corresponding function within an instance of the Operand class.<p>
<b>OCRService.java</b> - Contains the layer of abstraction between the Tesseract-OCR library that performs the Optical Character Recognition and the consumer of the OCRService (i.e. Operands.doOCR).<p>
<b>HDFSModel.java</b> - Provides API routines to support persistence functionality with Hadoop HDFS (i.e. Operands.readFile, Operands.saveFile).<p>
<b>FSModel.java</b> - Provides API routines to support persistence to the local file system (i.e. Operands.readFile, Operands.saveFile)
Note: The readFile() and saveFile() routines within the Operand.java can be switched between local file system support to HDFS support by uncommenting corresponding code.
<p><p>The default configuration that uses the local file system acts well as a prototyping tool to refine work flow processes that can be quickly verified outside of the HDFS system.<p>One off solutions can also be generated this way and then resultant files can be manually added from the command line into a corresponding destination within HDFS.

