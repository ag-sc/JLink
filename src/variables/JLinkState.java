package variables;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JLinkState extends AbstractState<LabeledJlinkDocument> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger log = LogManager.getFormatterLogger(JLinkState.class.getName());

	private static final String GENERATED_ENTITY_ID_PREFIX = "G";
	private static final DecimalFormat scoreFormat = new DecimalFormat("0.00000000");

	/**
	 * Since Entities only hold weak pointer via references to one another,
	 * using a Map is sensible to enable an efficient access to the entities.
	 * 
	 * key = UUID unique for EnitityAnnotation.
	 */
	private Map<String, EntityAnnotation> entities = new HashMap<>();

	/**
	 * Shortcut to get annotationId of token.
	 * 
	 * TokenIndex, AnnotationID
	 */
	private Map<Integer, Set<String>> tokenToEntities = new HashMap<>();

	private AtomicInteger entityIDIndex = new AtomicInteger();
	/**
	 * The state needs to keep track of the changes that were made to its
	 * entities in order to allow for efficient computation of factors and their
	 * features. Note: The changes are not stored in the Entity object since it
	 * is more efficient to just clear this map instead of iterating over all
	 * entities and reset a field in order to mark all entities as unchanged.
	 */

	/**
	 * This Copy Constructor creates an exact copy of itself including all
	 * internal annotations.
	 * 
	 * @param state
	 */
	public JLinkState(JLinkState state) {
		super(state);
		this.entityIDIndex = new AtomicInteger(state.entityIDIndex.get());
		for (EntityAnnotation e : state.entities.values()) {
			this.entities.put(e.entityID, new EntityAnnotation(this, e));
		}
		for (Entry<Integer, Set<String>> e : state.tokenToEntities.entrySet()) {
			this.tokenToEntities.put(e.getKey(), new HashSet<String>(e.getValue()));
		}
	}

	public JLinkState(LabeledJlinkDocument document) {
		super(document);
	}

	public LabeledJlinkDocument getDocument() {
		return getInstance();
	}

	public void addEntity(EntityAnnotation entity) {
		entities.put(entity.entityID, entity);
		addToTokenToEntityMapping(entity);
	}

	/**
	 * Returns ALL entity IDs in this state. This includes entities marked as
	 * fixed. Explorers should consider using the getNonFixedEntityIDs() method.
	 * 
	 * @return
	 */
	public Set<String> getEntityIDs() {
		return entities.keySet();
	}

	/**
	 * Returns ALL entities in this state. This includes entities marked as
	 * fixed. Explorers should consider using the getNonFixedEntities() method.
	 * 
	 * @return
	 */
	public Collection<EntityAnnotation> getEntities() {
		return entities.values();
	}

	public EntityAnnotation getEntity(String entityID) {
		return entities.get(entityID);
	}

	public boolean tokenHasAnnotation(Token token) {
		Set<String> entitiesForToken = tokenToEntities.get(token.getIndex());
		return entitiesForToken != null && !entitiesForToken.isEmpty();
	}

	public boolean tokenHasAnnotation(int tokenIndex) {
		if (tokenIndex >= getInstance().getTokens().size()) {
			log.error("Token index %s exceeds bounds of document of length %s", tokenIndex,
					getInstance().getTokens().size());
		}
		Set<String> entitiesForToken = tokenToEntities.get(tokenIndex);
		return entitiesForToken != null && !entitiesForToken.isEmpty();
	}

	public Set<String> getAnnotationsForToken(Token token) {
		return getAnnotationsForToken(token.getIndex());
	}

	public Set<String> getAnnotationsForToken(int tokenIndex) {
		Set<String> entitiesForToken = tokenToEntities.get(tokenIndex);
		if (entitiesForToken == null) {
			entitiesForToken = new HashSet<String>();
			tokenToEntities.put(tokenIndex, entitiesForToken);
		}
		return entitiesForToken;
	}

	protected void removeFromTokenToEntityMapping(EntityAnnotation entityAnnotation) {
		for (int i = entityAnnotation.getBeginTokenIndex(); i < entityAnnotation.getEndTokenIndex(); i++) {
			Set<String> entitiesForToken = tokenToEntities.get(i);
			if (entitiesForToken != null) {
				entitiesForToken.remove(entityAnnotation.entityID);
			}
		}
	}

	protected void addToTokenToEntityMapping(EntityAnnotation entityAnnotation) {
		for (int i = entityAnnotation.getBeginTokenIndex(); i < entityAnnotation.getEndTokenIndex(); i++) {
			Set<String> entitiesForToken = tokenToEntities.get(i);
			if (entitiesForToken == null) {
				entitiesForToken = new HashSet<String>();
				tokenToEntities.put(i, entitiesForToken);
			}
			entitiesForToken.add(entityAnnotation.entityID);
		}
	}

	protected String generateEntityID() {
		int currentID = entityIDIndex.getAndIncrement();
		String id = GENERATED_ENTITY_ID_PREFIX + currentID;
		return new String(id);
	}

	public Map<Integer, Set<String>> getTokenToEntityMapping() {
		return tokenToEntities;
	}

	/**
	 * Returns all those entities that are not marked as prior knowledge. This
	 * function is especially useful for the explorers (since they should not
	 * alter fixed variables).
	 * 
	 * @return
	 */
	public Collection<EntityAnnotation> getEditableEntities() {
		return entities.values().stream().filter(e -> !e.isPriorKnowledge).collect(Collectors.toList());
	}

	/**
	 * Returns all those entity IDs for which the entities are not marked as
	 * prior knowledge. This function is especially useful for the explorers
	 * (since they should not alter fixed variables).
	 * 
	 * @return
	 */
	public Set<String> getEditableEntityIDs() {
		return entities.values().stream().filter(e -> !e.isPriorKnowledge).map(e -> e.entityID)
				.collect(Collectors.toSet());
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("#OfAnnotations:");
		builder.append(entities.size());
		builder.append(" [");
		builder.append(scoreFormat.format(modelScore));
		builder.append("]: ");
		builder.append(" [");
		builder.append(scoreFormat.format(objectiveScore));
		builder.append("]: ");
		for (Token t : getInstance().getTokens()) {
			Set<String> entities = getAnnotationsForToken(t);
			List<EntityAnnotation> begin = new ArrayList<>();
			List<EntityAnnotation> end = new ArrayList<>();
			for (String entityID : entities) {
				EntityAnnotation e = getEntity(entityID);
				if (e.getBeginTokenIndex() == t.getIndex())
					begin.add(e);
				if (e.getEndTokenIndex() == t.getIndex() + 1)
					end.add(e);
			}
			if (!begin.isEmpty())
				buildTokenPrefix(builder, begin);
			builder.append(t.getText());
			builder.append(" ");
			if (!end.isEmpty())
				buildTokenSuffix(builder, end);
		}
		return builder.toString();
	}

	private void buildTokenPrefix(StringBuilder builder, List<EntityAnnotation> begin) {
		builder.append("[");
		for (EntityAnnotation e : begin) {
			builder.append("(");
			builder.append(e.getText());
			builder.append(")");
			builder.append(e.getType().conceptID);
			builder.append(":");
		}
		builder.append(" ");
	}

	private void buildTokenSuffix(StringBuilder builder, List<EntityAnnotation> end) {
		builder.append("]");
		builder.append(" ");
	}

	public String toDetailedString() {
		StringBuilder builder = new StringBuilder();
		for (EntityAnnotation e : getEntities()) {
			builder.append(e);
			builder.append("\n");
		}
		for (Entry<Integer, Set<String>> e : getTokenToEntityMapping().entrySet()) {
			builder.append(e);
			builder.append("\n");
		}
		return builder.toString().trim();
	}
}
