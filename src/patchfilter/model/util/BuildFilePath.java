package patchfilter.model.util;

import patchfilter.configuration.Constant;
import patchfilter.model.entity.Project;

public class BuildFilePath {

	public static String cacheModifiedMethodName(String fileString, Project subject) {
		return Constant.CACHE + "/" + subject.getName() + "/" + subject.getId() + "/" + fileString;
	}

	public static String cacheModifiedMethodContent(String fileString, Project subject) {
		return Constant.CACHE + "/" + subject.getName() + "/" + subject.getId() + "/" + fileString + "_method";
	}

	public static String cacheModifiedMethodRange(String fileString, Project subject) {
		return Constant.CACHE + "/" + subject.getName() + "/" + subject.getId() + "/" + fileString;
	}

	public static String tmpLine(String fileString, Project subject) {
		return Constant.Record + "/" + subject.getName() + "/" + subject.getId() + Constant.INSTRUMENT_LINE_DIR
				+ fileString + Constant.INSTRUMENT_LINE_SEPARATORINIT;
	}

	public static String tmpMapTraceLine(String fileString, Project subject) {
		return Constant.Record + "/" + subject.getName() + "/" + subject.getId() + Constant.INSTRUMENT_LINE_DIR
				+ fileString + Constant.INSTRUMENT_LINE_SEPARATORINIT + Constant.MAPTRACE;
	}

	public static String tmpMapLine(Project subject) {
		return Constant.Record + "/" + subject.getName() + "/" + subject.getId() + Constant.INSTRUMENT_LINE_DIR + "map";
	}

	public static String tmpState(String fileString, Project subject) {
		return Constant.Record + "/" + subject.getName() + "/" + subject.getId() + Constant.INSTRUMENT_STATE_DIR
				+ fileString + Constant.INSTRUMENT_STATE_SEPARATORINIT;
	}

	public static void main(String[] args) {
	}

}
