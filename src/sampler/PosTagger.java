package sampler;

import java.io.StringReader;
import java.util.List;

import corpus.DatasetConfig;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class PosTagger {

	private MaxentTagger tagger;

	private PosTagger() {
		tagger = new MaxentTagger(DatasetConfig.getPosTaggerFile());
	}

	private static PosTagger posTagger;

	public synchronized static PosTagger getPosTagger() {
		if (posTagger == null) {
			posTagger = new PosTagger();
		}
		return posTagger;
	}

	public boolean wordIsNONAdjective(String word) {

		if (word.endsWith("ic") || word.endsWith("ical"))
			return false;

		List<HasWord> sentence = MaxentTagger.tokenizeText(new StringReader(word)).get(0);

		TaggedWord taggedWord = tagger.tagSentence(sentence).get(0);
		// if (taggedWord.tag().equals("NN") || taggedWord.tag().equals("NNS")
		// || taggedWord.tag().equals("NNP")
		// || taggedWord.tag().equals("NNPS"))

		if (taggedWord.tag().equals("JJ"))
			return false;

		return true;
	}

}
