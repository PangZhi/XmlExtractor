import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;

// Copyright (c) 2014, Jing Fan. All Rights Reserved

/**
 * @brief Extract information from XML. See README.txt to see the usage
 *        examples.
 */
public class XmlExtractor {
	// Unit name.
	private String unitname = null;
	// All the useful attributes.
	private ArrayList<String> attrs = new ArrayList<String>();
	// The index of every attribute. If no entry in the map means get all the
	// values of this attribute.
	private HashMap<String, Integer> string2indexMap = new HashMap<String, Integer>();
	// The values of every attribute.
	private ArrayList<HashMap<String, HashSet<String>>> results = new ArrayList<HashMap<String, HashSet<String>>>();
	// Config file path.
	private String configPath = null;
	// Input file path.
	private String inputPath = null;
	// Output file path.
	private String outputPath = null;

	/**
	 * @brief Constructor.
	 * @param configPath Configure file path.
	 * @param inputPath Input file path.
	 * @param outputPath Output file path.
	 */
	public XmlExtractor(String configPath, String inputPath, String outputPath) {
		this.configPath = configPath;
		this.inputPath = inputPath;
		this.outputPath = outputPath;
	}

	/**
	 * @brief Extract information from xml file and output it.
	 */
	public void extract() {
		loadConfig();
		loadInput();
		output();
	}

	/**
	 * @brief Load configure file.
	 */
	private void loadConfig() {
		try {
			BufferedReader bw = new BufferedReader(new FileReader(new File(this.configPath)));
			String s;
			s = bw.readLine();
			if (null == s) {
				System.out.println("ERROR: Can't read config file");
				System.exit(-1);
			}
			this.unitname = s;
			while ((s = bw.readLine()) != null) {
				int l = s.indexOf('(');
				String attr = null;
				if (l != -1) {
					Integer index = Integer.valueOf(s.substring(l + 1, s.length() - 1));
					attr = s.substring(0, l);
					string2indexMap.put(attr, index);
				} else {
					attr = s;
				}
				attrs.add(attr);
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("ERROR: Can't read config file");
			System.exit(-1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("ERROR: Can't read config file");
			System.exit(-1);
		}

	}

	/**
	 * @brief Get elements with given name in all the descendants.
	 * @param root Current node.
	 * @param name	Descendant name.
	 * @return	The list of valid elements. Return an empty list if no valid one is found.
	 */
	private List<Element> getElements(Element root, String name) {
		List<Element> elements = root.elements(name);
		if (0 == elements.size()) {
			for (Element e : root.elements()) {
				elements = getElements(e, name);
				if (elements.size() != 0) {
					return elements;
				}
			}
		}
		return elements;
	}

	/**
	 * @brief Load input file.
	 */
	private void loadInput() {
		File f = new File(this.inputPath);
		SAXReader reader = new SAXReader();
		try {
			Document doc = reader.read(f);
			Element root = doc.getRootElement();

			List<Element> units = this.getElements(root, this.unitname);
			if (units == null || units.size() == 0) {
				System.out.println("WARNING: The size of units is 0");
				return;
			}

			Iterator<Element> it = units.iterator();
			while (it.hasNext()) {
				Element e = it.next();
				String ss = e.getName();
				HashMap<String, HashSet<String>> string2valuesMap = new HashMap<String, HashSet<String>>();
				for (String s : this.attrs) {
					HashSet<String> values = new HashSet<String>();
					List<Element> elements = this.getElements(e, s);

					if (this.string2indexMap.containsKey(s)) {
						values.add(((Element) (elements.get(this.string2indexMap.get(s) - 1))).getText());
					} else {
						Iterator valueIter = elements.iterator();
						while (valueIter.hasNext()) {
							Element attrValues = (Element) (valueIter.next());
							values.add(attrValues.getText());
						}
					}
					string2valuesMap.put(s, values);
				}
				this.results.add(string2valuesMap);
			}

		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("ERROR: Can't read input file");
			System.exit(-1);
		}

	}

	/**
	 * @brief Output the result.
	 */
	private void output() {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(this.outputPath)));
			for (HashMap<String, HashSet<String>> map : this.results) {
				for (String s : this.attrs) {
					HashSet<String> strSet = map.get(s);
					Iterator<String> iter = strSet.iterator();
					bw.write(iter.next());
					while (iter.hasNext()) {
						bw.write(";" + iter.next());
					}
					bw.write('\t');
				}
				bw.newLine();
			}

			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		if (args.length != 2 && args.length != 3) {
			System.out.println("ERROR: The length of argument incorrect, should be 3.\n"
							+ "usage: java XmlExtractor inputfile outputfile (configfile)");
		}

		String configFile = null;
		if (args.length == 2) {
			System.out.println("WARNING: Will use default config.txt");
			configFile = "config.txt";
		} else {
			configFile = args[2];
		}

		XmlExtractor xmlExtractor = new XmlExtractor(configFile, args[0], args[1]);
		xmlExtractor.extract();
	}
}
