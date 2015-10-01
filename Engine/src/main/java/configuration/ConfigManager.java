package configuration;

import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * central Manager for configuration settings
 * 
 * @author Sebastian Bader (sebastian.bader@student.kit.edu)
 *
 */
public interface ConfigManager {

	/**
	 * create a new index of entity files in /Engine/rdf-files/entities/
	 * 
	 * @return true if index could be created
	 */
	public boolean indexEntities();

	/**
	 * create a new index of relation files in /Engine/rdf-files/relations/
	 * 
	 * @return true if index could be created
	 */
	public boolean indexRelations();

	/**
	 * create a new index of class files in /Engine/rdf-files/classes/
	 * 
	 * @return true if index could be created
	 */
	public boolean indexClasses();

	/**
	 * initiate the log4j logger
	 */
	public Logger initLogger();

	public abstract Logger getLogger();
	
	/**
	 * imports properties from file 'config.properties'
	 * mainly the directory of necessary files (home_dir)
	 * @throws IOException 
	 */
	public void loadProperties() throws IOException;

	/**
	 * adds the path to the home directory of the application
	 * 
	 * @param home
	 */
	public void setHome(String home);

	public String getHome();


}
