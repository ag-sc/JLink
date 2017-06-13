package dictionary;

public class DictionaryEntry {

	final public String surfaceForm;

	final public String normalizedSurfaceForm;

	final public String noVovalsNormalizedSurfaceForm;

	final boolean isPrefferedSurfaceForm;

	public DictionaryEntry(String surfaceForm, String normalizedSurfaceForm, final String noVovalsNormalizedSurfaceForm,
			boolean isPrefferedSurfaceForm) {
		this.surfaceForm = surfaceForm;
		this.normalizedSurfaceForm = normalizedSurfaceForm;
		this.noVovalsNormalizedSurfaceForm = noVovalsNormalizedSurfaceForm;
		this.isPrefferedSurfaceForm = isPrefferedSurfaceForm;
	}

	public String getSurfaceForm() {
		return surfaceForm;
	}

	public String getNormalizedSurfaceForm() {
		return normalizedSurfaceForm;
	}

	public String getNoVovalsNormalizedSurfaceForm() {
		return noVovalsNormalizedSurfaceForm;
	}

	public boolean isPrefferedSurfaceForm() {
		return isPrefferedSurfaceForm;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isPrefferedSurfaceForm ? 1231 : 1237);
		result = prime * result
				+ ((noVovalsNormalizedSurfaceForm == null) ? 0 : noVovalsNormalizedSurfaceForm.hashCode());
		result = prime * result + ((normalizedSurfaceForm == null) ? 0 : normalizedSurfaceForm.hashCode());
		result = prime * result + ((surfaceForm == null) ? 0 : surfaceForm.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DictionaryEntry other = (DictionaryEntry) obj;
		if (isPrefferedSurfaceForm != other.isPrefferedSurfaceForm)
			return false;
		if (noVovalsNormalizedSurfaceForm == null) {
			if (other.noVovalsNormalizedSurfaceForm != null)
				return false;
		} else if (!noVovalsNormalizedSurfaceForm.equals(other.noVovalsNormalizedSurfaceForm))
			return false;
		if (normalizedSurfaceForm == null) {
			if (other.normalizedSurfaceForm != null)
				return false;
		} else if (!normalizedSurfaceForm.equals(other.normalizedSurfaceForm))
			return false;
		if (surfaceForm == null) {
			if (other.surfaceForm != null)
				return false;
		} else if (!surfaceForm.equals(other.surfaceForm))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DictionaryEntry [surfaceForm=" + surfaceForm + ", normalizedSurfaceForm=" + normalizedSurfaceForm
				+ ", noVovalsNormalizedSurfaceForm=" + noVovalsNormalizedSurfaceForm + ", isPrefferedSurfaceForm="
				+ isPrefferedSurfaceForm + "]";
	}

}
