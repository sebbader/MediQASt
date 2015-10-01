package configuration.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import lucene.LuceneIndexer;
import configuration.ConfigManager;

public class ConfigManagerImpl implements ConfigManager {

	private static Logger logger;
	private static String home;

	@Override
	public boolean indexEntities() {
		LuceneIndexer indexer = new LuceneIndexer();
		try {
			indexer.indexEntities();
		} catch (Exception e) {
			logger.error("Error during indexing of entities: ", e);
			return false;
		}
		return true;
	}

	@Override
	public boolean indexRelations() {
		LuceneIndexer indexer = new LuceneIndexer();
		try {
			indexer.indexRelations();
		} catch (Exception e) {
			logger.error("Error during indexing of relations: ", e);
			return false;
		}
		return true;
	}

	@Override
	public boolean indexClasses() {
		LuceneIndexer indexer = new LuceneIndexer();
		try {
			indexer.indexClasses();
		} catch (Exception e) {
			logger.error("Error during indexing of classes: ", e);
			return false;
		}
		return true;
	}

	@Override
	public Logger initLogger() {
		try {
			PatternLayout layout = new PatternLayout();
			layout.setConversionPattern("[%p] %d %C %M - %m%n");

			try {
				logger.getAllAppenders().nextElement();
			} catch (NoSuchElementException | NullPointerException | LinkageError e) {
				logger = Logger.getLogger("MediQAStLogger");

				// add appenders for console and log file if and only if no one
				// has been created yet
				ConsoleAppender consoleAppender = new ConsoleAppender(layout);
				logger.addAppender(consoleAppender);

				Date date = new Date();
				@SuppressWarnings("deprecation")
				FileAppender fileAppender = new FileAppender(layout,
						ConfigManagerImpl.home + "logs/MediQASt-"
								+ date.toLocaleString().replace(" ", "_")
										.replace(":", "-") + ".log", true);
				logger.addAppender(fileAppender);
			}

			logger.setLevel(Level.INFO);
		} catch (Exception ex) {
			System.out
					.println("Error: Could not initialize Logger. Terminate Application.");
			System.out.println(ex);
			System.exit(1);
		}

		return logger;
	}

	@Override
	public Logger getLogger() {
		if (logger == null)
			initLogger();
		return logger;
	}

	@Override
	public void setHome(String home) {
		ConfigManagerImpl.home = home;
	}

	@Override
	public String getHome() {
		if (ConfigManagerImpl.home == null) {
			
			try {
				loadProperties();
			} catch (IOException e) {
				logger.error("Could not read 'config.properties'.", e);
			}
		}
		
		return ConfigManagerImpl.home;
	}

	@Override
	public void loadProperties() throws IOException {
		Properties properties = new Properties();
		InputStream inputStream = null;
		
		try {
	 
	        System.out.println("Your dir to place 'config.properties': " + System.getProperty("user.dir"));
	        
			inputStream = new FileInputStream("config.properties");
			
			properties.load(inputStream);
			
			ConfigManagerImpl.home = properties.getProperty("home_dir");
			if (!ConfigManagerImpl.home.endsWith("/")){
				ConfigManagerImpl.home = ConfigManagerImpl.home + "/";
			}
			
		} catch (IOException e) {
			
			String errorText = "Could not load 'config.properties'. Please check if file exists and format is correct.";
			if (logger != null) {
				logger.error(errorText, e);
			} else {
				System.err.println(errorText);
			}
			
			throw e;
		}
		
	}

}
