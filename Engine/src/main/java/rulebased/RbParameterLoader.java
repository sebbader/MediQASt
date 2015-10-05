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

	public List<RbConstructingRule> loadRules(String path) throws SAXException,
			IOException, ParserConfigurationException, XPathExpressionException {
		List<RbConstructingRule> rules = new ArrayList<RbConstructingRule>();
		List<RbConstructingRule> defaultRules = new ArrayList<RbConstructingRule>();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new File(path));
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();

		// get all rules
		XPathExpression expr = xpath.compile("/rules/rule");
		NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < nl.getLength(); i++) {
			RbConstructingRule rule = new RbConstructingRule();

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

				expr = xpath.compile("gov");
				NodeList f = (NodeList) expr.evaluate(conditionsNL.item(j),
						XPathConstants.NODESET);
				String gov = f.item(0).getTextContent();

				expr = xpath.compile("dep");
				NodeList s = (NodeList) expr.evaluate(conditionsNL.item(j),
						XPathConstants.NODESET);
				String dep = s.item(0).getTextContent();

				rule.addCondition(new RbCondition(grammaticalRelation,
						gov, dep));
			}

			expr = xpath.compile("implication");
			NodeList implicationNL = (NodeList) expr.evaluate(ruleNode,
					XPathConstants.NODESET);
			for (int j = 0; j < implicationNL.getLength(); j++) {
				try {
					expr = xpath.compile("first");
					NodeList aNL = (NodeList) expr.evaluate(
							implicationNL.item(j), XPathConstants.NODESET);
					String first = aNL.item(0).getTextContent();

					expr = xpath.compile("second");
					NodeList bNL = (NodeList) expr.evaluate(
							implicationNL.item(j), XPathConstants.NODESET);
					String second = bNL.item(0).getTextContent();

					expr = xpath.compile("third");
					NodeList cNL = (NodeList) expr.evaluate(
							implicationNL.item(j), XPathConstants.NODESET);
					String third = cNL.item(0).getTextContent();

					rule.addImplication(new RbImplication(first, second, third));
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
	
	public List<RbMergingRule> loadDualMergingRelations(String path)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException {
		List<RbMergingRule> rules = new ArrayList<RbMergingRule>();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new File(path));
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();

		// get all rules
		XPathExpression expr = xpath
				.compile("//CombinedGrammaticalRelationsToMerge/mergeRule");
		NodeList mergeRulesNodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < mergeRulesNodeList.getLength(); i++) {
			List<RbCondition> conditions = new ArrayList<RbCondition>();
			
			// load conditions
			expr = xpath.compile("condition");
			NodeList conditionsNL = (NodeList) expr.evaluate(mergeRulesNodeList.item(i), XPathConstants.NODESET);
			for (int j = 0; j < conditionsNL.getLength(); j++) {
				expr = xpath.compile("GrammaticalRelation");
				NodeList gr = (NodeList) expr.evaluate(conditionsNL.item(j),
						XPathConstants.NODESET);
				String grammaticalRelation = gr.item(0).getTextContent();
				boolean deleteAfterCompress = gr.item(0).getAttributes().item(0)
						.getNodeValue().equalsIgnoreCase("true");

				expr = xpath.compile("gov");
				NodeList f = (NodeList) expr.evaluate(conditionsNL.item(j),
						XPathConstants.NODESET);
				String gov = f.item(0).getTextContent();

				expr = xpath.compile("dep");
				NodeList s = (NodeList) expr.evaluate(conditionsNL.item(j),
						XPathConstants.NODESET);
				String dep = s.item(0).getTextContent();

				conditions.add(new RbCondition(grammaticalRelation, gov, dep, deleteAfterCompress));
			}
			
			RbMergingRule rule = new RbMergingRule();
			rule.setConditions(conditions);
			rules.add( rule );

		}
		logger.debug("Found CombinedGrammaticalRelationsToMerge: " + rules.toString());
		return rules;
	}
	
	public HashMap<String, Boolean> loadSingularMergingRelations(String path)
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
