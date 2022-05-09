package patchfilter.model.entity;

import lombok.Data;
import patchfilter.model.util.FileIO;

//@Slf4j
//@Builder
@Data

public class Method {

	private String methodName;
	private String fixedFile;
	private Integer startLine;
	private Integer endLine;
	private String contentString;

	public Method() {
	}

	public Method(String methodName, String fixedFile) {
		this.methodName = methodName;
		this.fixedFile = fixedFile;
	}

	public void updateMethodContent() {
		if (!fixedFile.equals("") && endLine != null && startLine != null) {
			String fileCnt[] = FileIO.readFileToString(fixedFile).split("\n");
			StringBuilder newStringBuilder = new StringBuilder();
			for (int i = startLine - 1; i <= endLine - 1; i++) {
				newStringBuilder.append(fileCnt[i]).append("\n");
			}
			contentString = newStringBuilder.toString();
		}
	}

	@Override
	public String toString() {
		return "Method [methodNameString = " + methodName + ", StartLine = " + startLine + ", endLine = " + endLine
				+ ", contentString = " + contentString + "]";
	}

}
