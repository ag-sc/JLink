package corpus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import variables.LabeledJlinkDocument;

public class DocumentCorpus {

	private List<LabeledJlinkDocument> documents = new ArrayList<LabeledJlinkDocument>();

	public List<LabeledJlinkDocument> getDocuments() {
		return documents;
	}

	public void addDocument(LabeledJlinkDocument doc) {
		documents.add(doc);
	}

	public void addDocuments(Collection<LabeledJlinkDocument> docs) {
		documents.addAll(docs);
	}

	@Override
	public String toString() {
		return "DocumentCorpus [documents=" + documents + "]";
	}

	public String toDetailedString() {
		StringBuilder builder = new StringBuilder();
		for (LabeledJlinkDocument doc : documents) {
			builder.append(doc.getName());
			builder.append("\n\t");
			builder.append(doc.getContent());
			builder.append("\n\t");
			builder.append(doc.getTokens());
			builder.append("\n\t");
			builder.append(doc.getResult());
			builder.append("\n");
		}
		return "DocumentCorpus [#documents=" + documents.size() + ", documents=\n" + builder.toString() + "]";
	}

}
