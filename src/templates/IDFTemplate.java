package templates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dictionary.CollectiveDictionary;
import dictionary.CollectiveDictionaryFactory;
import dictionary.Concept;
import dictionary.subdicts.ISubDictionary;
import factors.Factor;
import factors.FactorScope;
import learning.Vector;
import templates.IDFTemplate.Scope;
import variables.JLinkState;
import variables.EntityAnnotation;
import variables.LabeledJlinkDocument;

public class IDFTemplate extends AbstractTemplate<LabeledJlinkDocument, JLinkState, Scope> {

	private static Logger log = LogManager.getFormatterLogger(IDFTemplate.class.getName());

	private final Map<String, Double> idfDict;

	static class Scope extends FactorScope {

		private String text;

		public Scope(AbstractTemplate<?, ?, Scope> template, String text) {
			super(template, text);
			this.text = text;
		}

		public String getText() {
			return text;
		}

		@Override
		public String toString() {
			return "Scope [text=" + text + "]";
		}

	}

	public IDFTemplate() {
		CollectiveDictionary dict = CollectiveDictionaryFactory.getInstance();
		idfDict = buildIDF(dict);

		idfDict.entrySet().forEach(e -> System.out.println(e.getKey() + "\t" + e.getValue()));
	}

	private Map<String, Double> buildIDF(CollectiveDictionary dict) {

		Map<String, Set<String>> documents = new HashMap<>();

		for (ISubDictionary subDict : dict.getDictionaries()) {

			for (Entry<Concept, Set<String>> entry : subDict.getConceptBasedDictionary().entrySet()) {

				documents.putIfAbsent(entry.getKey().conceptID, new HashSet<>());
				documents.get(entry.getKey().conceptID).addAll(entry.getValue().stream()
						.flatMap(e -> Arrays.stream(e.split(" "))).collect(Collectors.toList()));

			}
		}
		return getIDFs(documents);

	}

	@Override
	public List<Scope> generateFactorScopes(JLinkState state) {
		List<Scope> factors = new ArrayList<>();
		for (EntityAnnotation entity : state.getEntities()) {
			factors.add(new Scope(this, entity.getText()));
		}
		return factors;
	}

	@Override
	public void computeFactor(Factor<Scope> factor) {

		Vector featureVector = factor.getFeatureVector();

		final String[] tokens = factor.getFactorScope().getText().split(" ");

	}

	private Map<String, Double> getIDFs(final Map<String, Set<String>> documents) {

		final double N = documents.size();

		Map<String, Double> termCounts = new HashMap<String, Double>();
		Map<String, Double> idfs = new HashMap<String, Double>();

		for (Entry<String, Set<String>> document : documents.entrySet()) {

			for (String word : document.getValue()) {
				termCounts.put(word, termCounts.getOrDefault(word, 0d) + 1);
			}

		}

		termCounts.entrySet().stream().forEach(termCount -> {
			idfs.put(termCount.getKey(),
					(termCount.getValue().intValue()) == 0 ? 0 : Math.log(N / termCount.getValue().doubleValue()));
		});
		return idfs;
	}

}
