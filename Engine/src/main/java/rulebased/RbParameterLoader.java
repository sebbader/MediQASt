package rulebased;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import configuration.ConfigManager;
import configuration.impl.ConfigManagerImpl;

public class RbParameterLoader {

	private Logger logger;

	public RbParameterLoader() {
		ConfigManager configManager = new ConfigManagerImpl();
		this.logger = configManager.getLogger();
	}

	public List<RbRule> loadRules(String path) throws SAXException,
			IOException, ParserConfigurationException, XPathExpressionException {
		List<RbRule> rules = new ArrayList<RbRule>();
		List<RbRule> defaultRules = new ArrayList<RbRule>();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new File(path));
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();

		// get all rules
		XPathExpression expr = xpath.compile("/rules/rule");
		NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < nl.getLength(); i++) {
			RbRule rule = new RbRule();

			Node ruleNode = nl.item(i);

			// get the 'priority' attribut
			boolean isDefaultRule = false;
			try {
				isDefaultRule = ruleNode.getAttributes().item(0).getNodeValue()
						.equalsIgnoreCase("default");
			} catch (NullPointerException e) {
			}
			rule.setDefault(isDefaultRule);

			// load conditions
			expr = xpath.compile("condition");
			NodeList conditionsNL = (NodeList) expr.evaluate(ruleNode,
					XPathConstants.NODESET);
			for (int j = 0; j < conditionsNL.getLength(); j++) {
				expr = xpath.compile("GrammaticalRelation");
				NodeList gr = (NodeList) expr.evaluate(conditionsNL.item(j),
						XPathConstants.NODESET);
				String grammaticalRelation = gr.item(0).getTextContent();

				expr = xpath.compile("first");
				NodeList f = (NodeList) expr.evaluate(conditionsNL.item(j),
						XPathConstants.NODESET);
				String first = f.item(0).getTextContent();

				expr = xpath.compile("second");
				NodeList s = (NodeList) expr.evaluate(conditionsNL.item(j),
						XPathConstants.NODESET);
				String second = s.item(0).getTextContent();

				rule.addCondition(new RbCondition(grammaticalRelation,
						first, second));
			}

			expr = xpath.compile("implies");
			NodeList implificationNL = (NodeList) expr.evaluate(ruleNode,
					XPathConstants.NODESET);
			for (int j = 0; j < implificationNL.getLength(); j++) {
				try {
					expr = xpath.compile("a");
					NodeList aNL = (NodeList) expr.evaluate(
							implificationNL.item(j), XPathConstants.NODESET);
					String a = aNL.item(0).getTextContent();

					expr = xpath.compile("b");
					NodeList bNL = (NodeList) expr.evaluate(
							implificationNL.item(j), XPathConstants.NODESET);
					String b = bNL.item(0).getTextContent();

					expr = xpath.compile("c");
					NodeList cNL = (NodeList) expr.evaluate(
							implificationNL.item(j), XPathConstants.NODESET);
					String c = cNL.item(0).getTextContent();

					rule.addImplication(new RbImplication(a, b, c));
				} catch (NullPointerException e) {
					logger.error(
							"Error in CasiaParameterLoader: could not parse XML rule file: ",
							e);
					logger.error("continue reading rules.");
				}
			}

			if (!rule.isDefault()) {
				rules.add(rule);
			} else {
				defaultRules.add(rule);
			}
		}
		rules.addAll(defaultRules);
		logger.debug("CasiaParameterLoader - Found Rules: " + rules.toString());
		return rules;
	}

	public HashMap<String, Boolean> loadMergingRelations(String path)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException {
		HashMap<String, Boolean> gr = new HashMap<String, Boolean>();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new File(path));
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();

		// get all rules
		XPathExpression expr = xpath
				.compile("//GrammaticalRelationsToMerge/GrammaticalRelation");
		NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < nl.getLength(); i++) {
			boolean deleteAfterCompress = nl.item(i).getAttributes().item(0)
					.getNodeValue().equalsIgnoreCase("true");
			gr.put(nl.item(i).getTextContent(), deleteAfterCompress);
		}
		logger.debug("Found GrammaticalRelationsToMerge: " + gr.toString());
		return gr;
	}
}
