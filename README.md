# DocRouter
Java Framework that routes document images through a configurable work flow process.<p>
This work flow defines the order of events by utilizing the Libre Office designer tool with corresponding connected shapes.<p>
<p align="center">
  <img src="./WorkFlowExample.png" width="500"/>
</p>
One transformation defined in the work flow illustrated above, doOCR, includes Optical Character Recognition utilizing the Tesseract-OCR library.<p><p>
To run the attached Java Frameework:<p><p>
Step 1:  Open the DocRouter Eclipse project.<p>
Step 2:  Open CADEngine.java from Eclipse.<p>
Step 3:  Select from Eclipse 'Run->Run As->Java Application'.<p><p>
Step 4:  Verify that created *.txt files have been placed in the ./DocRouter/Other directory.<p><p>
<p>DocRouter is made up of the following modules:<p><p>
<b>OpCodeML.java</b> - Parses the XML form of this illustrated design into an internal hashmap for data driven invocation of corresponding operands.<p>
<b>Operands.java</b> - Class that includes functions invoked by the CADEngine in the order defined by the OpCode file.<p>
<b>CADEngine.java</b> - Utilizes an instance of the OpCodeML class to retrieve the specified order of events and then invokes the corresponding function within an instance of the Operand class.<p>
<b>OCRService.java</b> - Contains the layer of abstraction between the Tesseract-OCR library that performs the Optical Character Recognition and the consumer of the OCRService (i.e. Operands.doOCR).<p>
<b>HDFSModel.java</b> - Provides API routines to support persistence functionalsity Hadoops' HDFS (i.e. Operands.readFile, Operands.saveFile).<p><p>
Note: The readFile() and saveFile() routines can be utilized by uncommenting corresponding code after configuring the installation of HDFS with the appropriate adminstration priviledges.<p>As a proof of concept the default configuration will simply save the transformed files outside the HDFS system onto the local computer.
