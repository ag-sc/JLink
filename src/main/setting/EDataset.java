package main.setting;

public enum EDataset {

	TRAIN("trainset"), DEVELOP("developset"), TEST("testset");

	final public String fullName;

	private EDataset(final String fullName) {
		this.fullName = fullName;
	}

}
