package lucene;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

import org.apache.log4j.Logger;

import configuration.ConfigManager;
import configuration.impl.ConfigManagerImpl;

public class MyStemmer {

	private HashSet<String> stop_words = new HashSet<String>();
	private Logger logger;

	public MyStemmer() {
		ConfigManager configManager = new ConfigManagerImpl();
		this.logger = configManager.getLogger();

		try {
			Path path = FileSystems.getDefault().getPath(
					configManager.getHome() + "stopwords", "stopwords.txt");
			BufferedReader reader = Files.newBufferedReader(path,
					StandardCharsets.UTF_8);

			while (true) {
				String line = reader.readLine();
				if (line != null) {
					stop_words.add(line);
				} else {
					break;
				}
			}
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public String doRemoveStopwordsStemming(String phrase) {
		String core_phrase = "";

		// remove all special characters & use lower case letters only
		phrase = phrase.toLowerCase();
		phrase = phrase.replaceAll("[^\\w\\s]", "");

		// remove all stopwords
		for (String s : phrase.split(" ")) {
			if (!stop_words.contains(s)) {

				PorterStemmer stemmer = new PorterStemmer();
				for (char ch : s.toCharArray()) {
					stemmer.add(ch);
				}
				stemmer.stem();

				String stem = stemmer.toString();

				core_phrase = core_phrase.concat(" ").concat(stem);
			}
		}

		return core_phrase.substring(1);
	}
}
