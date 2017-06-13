package templates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import factors.Factor;
import factors.FactorScope;
import learning.Vector;
import templates.EmptyTemplate.Scope;
import variables.JLinkState;
import variables.LabeledJlinkDocument;

public class EmptyTemplate extends AbstractTemplate<LabeledJlinkDocument, JLinkState, Scope> implements Serializable {

	private static Logger log = LogManager.getFormatterLogger(EmptyTemplate.class.getName());

	class Scope extends FactorScope {

		public Scope(AbstractTemplate<?, ?, ?> template) {
			super(template);
		}

		@Override
		public String toString() {
			return "Scope []";
		}

	}

	@Override
	public List<Scope> generateFactorScopes(JLinkState state) {
		List<Scope> factors = new ArrayList<>();
		factors.add(new Scope(this));
		return factors;
	}

	@Override
	public void computeFactor(Factor<Scope> factor) {
		Vector featureVector = factor.getFeatureVector();
	}

}