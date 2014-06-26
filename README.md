XmlExtractor
============
This class provide a convenient way to extract information from XML files.  
usage: java XmlExtractor input_file output_file configFile(config.txt as default).  
The config.txt should be like:  
 	unit name  
	attribute name0  
	attribute name1  
 
You can add (index) after the attribute to specify the index of useful attribute if there exist multiple
attribute of the same name. If not specify the index, all available attributes will be extracted. For example,  
	record
	datastamp
	setSpec
	dc:description(1)

It means that the unit name is record, and useful attribute are datastamp, setSpec and the first description.

<root>
	<record>
		<datastamp>2012-01-01</datastamp>
		<setSpec>cs</setSpec>
		<setSpec>math</setSpec>
 		<metadata>
			<dc:description>Hello World</dc:description>
 			<dc:description>Wahaha</dc:description>
 		</metadata>
 	</record>
	<record>
 		<datastamp>2012-01-02</datastamp>
 		<setSpec>painting</setSpec>
 		<metadata>
			<dc:description>sunny</dc:description>
		</metadata>
 	<record>
</root>
  
 will generate the following content('\t' as delimiter):
 	2012-01-01 	cs;math		Hello World
 	2012-01-02	painting	sunny		

