package lucene;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import configuration.ConfigManager;
import configuration.impl.ConfigManagerImpl;
import mapper.ClassMapper;
import mapper.EntityMapper;
import mapper.RelationMapper;
import mapper.impl.ClassMapperImpl;
import mapper.impl.EntityMapperImpl;
import mapper.impl.RelationMapperImpl;
import inputmanagement.InputManager;
import inputmanagement.candidates.impl.EntityCandidate;
import inputmanagement.candidates.impl.RelationCandidate;
import inputmanagement.impl.QueryTriple;

public class LuceneMapper {

	private InputManager inputManager;
	private Logger logger;

	public LuceneMapper(InputManager inputManager) {
		this.inputManager = inputManager;

		ConfigManager configManager = new ConfigManagerImpl();
		this.logger = configManager.getLogger();
	}

	public void mapQueryTriple(QueryTriple queryTriple) {

		ClassMapper classMapper = new ClassMapperImpl(inputManager);
		if (inputManager.isActiveOption("findEntityAndClass", "true")) {
			queryTriple.setEntity1Candidates(classMapper
					.getClassCandidates(queryTriple.getEntity1()));
			queryTriple.setEntity2Candidates(classMapper
					.getClassCandidates(queryTriple.getEntity2()));
		}
		EntityMapper entityMapper = new EntityMapperImpl(inputManager);
		queryTriple.addEntity1Candidates(entityMapper
				.getEntityCandidates(queryTriple.getEntity1()));
		queryTriple.addEntity2Candidates(entityMapper
				.getEntityCandidates(queryTriple.getEntity2()));

		RelationMapper relMapper = new RelationMapperImpl(inputManager);
		queryTriple.addRelationCandidates(relMapper
				.findRelationCandidatesWithLucene(queryTriple.getPredicate()));

		if (inputManager.isActiveOption("considerRelationEnvironment", true)) {
			List<RelationCandidate> list = new ArrayList<RelationCandidate>();
			list.addAll(relMapper.findRelationCandidatesFromEntityEnvironment(
					queryTriple.getEntity1Candidates(),
					queryTriple.getPredicate()));
			list.addAll(relMapper.findRelationCandidatesFromEntityEnvironment(
					queryTriple.getEntity2Candidates(),
					queryTriple.getPredicate()));
			queryTriple.addRelationCandidates(list);
		}

		logger.info("Found URIs by Standard Lucene Mapper for "
				+ queryTriple.getTriple() + ":");

		if (queryTriple.getEntity1Candidates().isEmpty())
			logger.info("Standard Lucene Mapper found no entity1 candidates.");
		for (EntityCandidate entityCandidate : queryTriple
				.getEntity1Candidates())
			logger.info("Standard Lucene Mapper found entity1 candidates: "
					+ entityCandidate);

		if (queryTriple.getRelationCandidates().isEmpty())
			logger.info("Standard Lucene Mapper found no relation candidates.");
		for (RelationCandidate relationCandidate : queryTriple
				.getRelationCandidates())
			logger.info("Standard Lucene Mapper found relation candidates: "
					+ relationCandidate);

		if (queryTriple.getEntity2Candidates().isEmpty())
			logger.info("Standard Lucene Mapper found no entity2 candidates.");
		for (EntityCandidate entityCandidate : queryTriple
				.getEntity2Candidates())
			logger.info("Standard Lucene Mapper found entity2 candidates: "
					+ entityCandidate);
	}

	public void mapEntitiesAndClasses(QueryTriple queryTriple) {

		ClassMapper classMapper = new ClassMapperImpl(inputManager);
		if (inputManager.isActiveOption("findEntityAndClass", "true")) {
			queryTriple.setEntity1Candidates(classMapper
					.getClassCandidates(queryTriple.getEntity1()));
			queryTriple.setEntity2Candidates(classMapper
					.getClassCandidates(queryTriple.getEntity2()));
		}
		EntityMapper entityMapper = new EntityMapperImpl(inputManager);
		queryTriple.addEntity1Candidates(entityMapper
				.getEntityCandidates(queryTriple.getEntity1()));
		queryTriple.addEntity2Candidates(entityMapper
				.getEntityCandidates(queryTriple.getEntity2()));

		logger.info("Found URIs by Standard Lucene Mapper for "
				+ queryTriple.getTriple() + ":");

		if (queryTriple.getEntity1Candidates().isEmpty())
			logger.info("Standard Lucene Mapper found no entity1 candidates.");
		for (EntityCandidate entityCandidate : queryTriple
				.getEntity1Candidates())
			logger.info("Standard Lucene Mapper found entity1 candidates: "
					+ entityCandidate);

		if (queryTriple.getEntity2Candidates().isEmpty())
			logger.info("Standard Lucene Mapper found no entity2 candidates.");
		for (EntityCandidate entityCandidate : queryTriple
				.getEntity2Candidates())
			logger.info("Standard Lucene Mapper found entity2 candidates: "
					+ entityCandidate);

	}

}
