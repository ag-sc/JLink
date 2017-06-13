package candidateretrieval.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import candidateretrieval.ICandidateRetrieval;
import dictionary.CollectiveDictionary;
import dictionary.CollectiveDictionaryFactory;
import dictionary.Concept;
import dictionary.DictionaryEntry;
import dictionary.subdicts.ISubDictionary;

public class LuceneRetrieval implements ICandidateRetrieval {

	final public static int MAX_RESULTS = 20;
	final public static double SIMILARITY_TRESHOLD = 0.7d;

	private RAMDirectory indexDir;

	private Map<Integer, Concept> luceneObjectMapping = new HashMap<>();

	private Map<String, List<LuceneCandidate>> cache = new ConcurrentHashMap<>();

	private static LuceneRetrieval retrieval = null;

	public static void main(String[] args) {
		LuceneRetrieval r = LuceneRetrieval.getInstance();

		r.getFuzzyCandidates("cardiac disease").forEach(System.out::println);
	}

	public static LuceneRetrieval getInstance() {

		if (retrieval == null)
			retrieval = new LuceneRetrieval();

		return retrieval;
	}

	private LuceneRetrieval() {
		CollectiveDictionary dict = CollectiveDictionaryFactory.getInstance();

		System.out.print("Prepare LuceneRetrieval component... ");

		try {
			indexDir = new RAMDirectory();

			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46,
					new StandardAnalyzer(Version.LUCENE_46));
			IndexWriter indexWriter;
			indexWriter = new IndexWriter(indexDir, config);

			int index = 0;

			for (ISubDictionary subDict : dict.getDictionaries()) {
				for (Entry<DictionaryEntry, Set<Concept>> entry : subDict.getDictionary().entrySet()) {
					for (Concept concept : entry.getValue()) {
						Document doc = new Document();

						luceneObjectMapping.put(index, concept);
						doc.add(new TextField("conceptID", String.valueOf(index), Field.Store.YES));
						doc.add(new TextField("surfaceFormTokens", entry.getKey().normalizedSurfaceForm,
								Field.Store.YES));
						index++;
						indexWriter.addDocument(doc);
					}
				}
			}

			indexWriter.prepareCommit();
			indexWriter.commit();
			indexWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(" done.");
	}

	public List<LuceneCandidate> getFuzzyCandidates(String search) {
		return getFuzzyCandidates(search, MAX_RESULTS, SIMILARITY_TRESHOLD);
	}

	public List<LuceneCandidate> getFuzzyCandidates(String search, final int numbOfResults,
			final double minLuceneScore) {

		search = QueryParser.escape(search);

		String fuzzySearch = "";
		for (String s : search.split(" ")) {
			fuzzySearch += s + "~ ";
		}
		// System.out.println("fuzzy Search = " + fuzzySearch);
		// String fuzzySearch = search + "~";
		return getNonFuzzyCandidates(fuzzySearch.trim(), numbOfResults, minLuceneScore);
	}

	public List<LuceneCandidate> getNonFuzzyCandidates(String search, final int numbOfResults,
			final double minLuceneScore) {

		if (cache.containsKey(search))
			return cache.get(search);

		Map<String, Float> result = new LinkedHashMap<String, Float>(numbOfResults);
		List<LuceneCandidate> resultList = new ArrayList<LuceneCandidate>(numbOfResults);
		/*
		 * Do this for AND = and ... etc.
		 */
		search = search.toLowerCase();

		if (search.trim().isEmpty())
			return resultList;

		IndexSearcher searcher;
		try {
			searcher = new IndexSearcher(DirectoryReader.open(indexDir));

			synTokenQuery(search, numbOfResults, minLuceneScore, result, searcher);
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}

		result.entrySet().stream().forEach(r -> resultList
				.add(new LuceneCandidate(luceneObjectMapping.get(Integer.parseInt(r.getKey())), r.getValue())));
		Collections.sort(resultList);

		cache.put(search, resultList);

		return resultList;
	}

	// private void synTokenQuery(String search, final int numbOfResults, final
	// double minLuceneScore,
	// Map<String, Float> result, IndexSearcher searcher) throws ParseException,
	// IOException {
	// QueryParser parser = new QueryParser(Version.LUCENE_46,
	// "surfaceFormTokens",
	// new StandardAnalyzer(Version.LUCENE_46));
	//
	// Query q = parser.parse(search);
	// /*
	// * Works only in String field!!
	// */
	// // Query q = new FuzzyQuery(new Term("surfaceFormTokens",
	// // QueryParser.escape(search)), 2);
	//
	// TopDocs top = searcher.search(q, numbOfResults);
	//
	// double softMaxMean = 0;// (1 * top.getMaxScore()) / 3;
	//
	// for (ScoreDoc doc : top.scoreDocs) {
	// softMaxMean += Math.exp(doc.score);
	// }
	// softMaxMean /= top.totalHits;
	//
	// for (ScoreDoc doc : top.scoreDocs) {
	// if (Math.exp(doc.score) >= softMaxMean) {
	// final String key = searcher.doc(doc.doc).get("conceptID");
	// if (result.getOrDefault(key, 0f) < doc.score) {
	// result.put(key, doc.score);
	// }
	// }
	// }
	//
	// for (ScoreDoc doc : top.scoreDocs) {
	// if (result.size() <= 20) {
	// final String key = searcher.doc(doc.doc).get("conceptID");
	// if (result.getOrDefault(key, 0f) < doc.score) {
	// result.put(key, doc.score);
	// }
	// }
	// }
	// }

	/*
	 * 200: max/3: min 20
	 */
	// tp = 1762.0
	// fp = 37320.0
	// fn = 131.0
	// Micro precision = 0.045
	// Micro recall = 0.931
	// Micro F1 = 0.086

	/*
	 * 200:SoftmaxMean: min 20
	 */
	// tp = 1786.0
	// fp = 68859.0
	// fn = 114.0
	// Micro precision = 0.025
	// Micro recall = 0.94
	// Micro F1 = 0.049

	private void synTokenQuery(String search, final int numbOfResults, final double minLuceneScore,
			Map<String, Float> result, IndexSearcher searcher) throws ParseException, IOException {

		QueryParser parser = new QueryParser(Version.LUCENE_46, "surfaceFormTokens",
				new StandardAnalyzer(Version.LUCENE_46));

		search = QueryParser.escape(search);

		Query q = parser.parse(search);
		/*
		 * Works only in String field!!
		 */
		// Query q = new FuzzyQuery(new Term("surfaceFormTokens",
		// QueryParser.escape(search)), 2);

		TopDocs top = searcher.search(q, numbOfResults);

		for (ScoreDoc doc : top.scoreDocs) {
			if (doc.score >= minLuceneScore) {
				final String key = searcher.doc(doc.doc).get("conceptID");
				if (result.getOrDefault(key, 0f) < doc.score) {
					result.put(key, doc.score);
				}
			}
		}
	}
}
