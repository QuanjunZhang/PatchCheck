package patchfilter.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import patchfilter.configuration.Constant;
import patchfilter.model.entity.Project;
import patchfilter.model.initialization.visitor.MethodVisitor;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import patchfilter.model.util.FileIO;

public class TestFile {

	private String filePath;
	private Project subject;
	private Map<String, Integer> methodRangeList;

	public TestFile(Project project, String filePath) {
		this.subject = project;
		this.filePath = filePath;
	}

	public void parseFile() {
		String fileName = filePath.split("/")[filePath.split("/").length - 1];
		String methodRangeFile = Constant.CACHE + subject.getName() + "/" + subject.getId() + "/" + fileName;
		methodRangeList = new HashMap<String, Integer>();
		if (new File(methodRangeFile).exists()) {
			methodRangeList = json2Map(FileIO.readFileToString(methodRangeFile));
		} else {
			MethodVisitor methodVisitor = new MethodVisitor();
			CompilationUnit compilationUnit = FileIO.genASTFromSource(FileIO.readFileToString(filePath),
					ASTParser.K_COMPILATION_UNIT);
			compilationUnit.accept(methodVisitor);
			methodRangeList = methodVisitor.getMethodStart();
			String methodRange = JSONObject.toJSONString(methodRangeList);
			FileIO.writeStringToFile(methodRangeFile, methodRange);
		}
	}

	private static Map<String, Integer> json2Map(String str) {
		Map<String, Integer> map = new Gson().fromJson(str, new TypeToken<HashMap<String, Integer>>() {
		}.getType());
		return map;
	}

	public int getLine(String lineContent) {
		for (Entry<String, Integer> entry : this.methodRangeList.entrySet()) {
			if (entry.getKey().contains(lineContent)) {
				return entry.getValue();
			}
		}
		return 1;
	}

	public static void main(String[] args) {
		String filePath = "/home/xushicheng/defects4j/projects/Chart/Chart_13_buggy/tests/org/jfree/data/time/junit/TimeSeriesTests.java";
		Project subject = new Project("Chart", 3);
		TestFile testFile = new TestFile(subject, filePath);
		testFile.parseFile();
		System.out.println(testFile.getLine("testCreateCopy3"));
	}
}
