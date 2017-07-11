# DocRouter
Java Framework that routes document images through a configurable work flow process.<p>
This work flow defines the order of events by utilizing the Libre Office designer tool with corresponding connected shapes.<p>
One transformation defined in the work flow, doOCR, includes Optical Character Recognition utilizing the Tesseract-OCR library.<p>
The Java Framework is made up of the following modules:<p><p>
OpCodeML.java - Parses the XML form of the design into an internal hashmap for data driven invocation of corresponding operands.<p>
Operands.java - Class that includes functions invoked by the CADEngine in the order defined by the OpCode file.<p>
CADEngine.java - Utilizes an instance of the OpCodeML class to retrieve the specified order of events and then invokes the corresponding function within an instance of the Operand class.<p>
OCRService.java - Contains the layer of abstraction between the Tesseract-OCR library that performs the Optical Character Recognition and the consumer of the OCRService (i.e. Operands.doOCR).<p>
HDFSModel.java - Layer of abstraction between Hadoops' HDFS and the consumer (i.e. Operands.readFile, Operands.saveFile).<p><p>
Note: The readFile and saveFile routines can be utilized by uncommenting corresponding code after configuring the installation of HDFS with the appropriate adminstration priviledges.  As a proof of concept the default configuration will simply save the transformed files outside the HDFS system onto the local computer.

