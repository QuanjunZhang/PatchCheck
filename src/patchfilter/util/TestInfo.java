package patchfilter.util;

public class TestInfo {
	
	private String testCase;
	private double score;
	// private int lineNum;
	private int remainPatchNum;

	public TestInfo(String testCaseString, double score, int patchNum) {
		this.testCase = testCaseString;
		this.score = score;
		this.remainPatchNum = patchNum;
	}

	public String getTestCaseString() {
		return testCase;
	}

	public void setTestCaseString(String testCaseString) {
		this.testCase = testCaseString;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public int getRemainPatchNum() {
		return remainPatchNum;
	}

	public void setRemainPatchNum(int remainPatchNum) {
		this.remainPatchNum = remainPatchNum;
	}

	@Override
	public String toString() {
		return "TestLine [testCaseString = " + testCase + ", score = " + score + "]";
	}

}
