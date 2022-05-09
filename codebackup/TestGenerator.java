package patchfilter.backup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import patchfilter.configuration.Constant;
import patchfilter.model.entity.Patch;
import patchfilter.model.entity.Project;
import patchfilter.model.run.Runner;
import patchfilter.model.util.FileIO;
import patchfilter.util.TestFile;

@Slf4j
public class TestGenerator {
	private String evosuiteTestsDir;
	private String evosuiteReportDir;

	private Project subject;
	private Patch patch;

	String clazz;
	private List<String> newTestList;
	Map<String, String> coverageCriteria;

	public List<String> getNewTestList() {
		return newTestList;
	}

	public Map<String, String> getCoverageCriteria() {
		return coverageCriteria;
	}
	
	public String getClazz() {
		return clazz;
	}

	public TestGenerator(Patch patch) {
		this.subject = patch.getSubject();
		this.patch = patch;
	}

	public void mainProcess() {
		System.out.println("generating tests for " + patch.toString());
		testGeneration();
		for (String s : newTestList) {
			System.out.println(s);
		}
	}

	public List<String> testGeneration() {
		newTestList = new ArrayList<>();
		String fixedFile = patch.getFixedFile();
		clazz = fixedFile.split(subject.getSsrc())[1].split("\\.")[0].replace("/", ".").substring(1);
		System.out.println("generating test for " + clazz);
		// step 1: generate test for specific class
		if (generateTests(subject, clazz)) {
			// step 2: analyze test file and reports
//			System.out.println("analyzing tests and reports ");
			String testFilePath = evosuiteTestsDir + clazz.replace(".", "/") + "_ESTest.java";
			String scaffoldingPath = evosuiteTestsDir + clazz.replace(".", "/") + "_ESTest_scaffolding.java";
			String reportPath = evosuiteReportDir + "statistics.csv";
//			analyzeTest(testFilePath, reportPath);
			// step 3: move test file to the right place
			// String copy2Path1 = subject.getHome() + subject.getTsrc() + "/" +
			// clazz.replace(".", "/") + "_ESTest.java";
			// String copy2Path2 = subject.getHome() + subject.getTsrc() + "/" +
			// clazz.replace(".", "/")
			// + "_ESTest_scaffolding.java";
			// FileIO.copyFile(testFilePath, copy2Path1);
			// FileIO.copyFile(scaffoldingPath, copy2Path2);
			newTestList.add(testFilePath);
		} else {
			System.out.println("test generation failed! ");
		}
		return newTestList;
	}

	private boolean generateTests(Project subject, String clazz) {
		List<String> message = Runner.generateESTest(subject, clazz);
//		log.info(message.toString());
		evosuiteTestsDir = subject.getHome() + "/evosuite-tests/";
		evosuiteReportDir = subject.getHome() + "/evosuite-report/";
		if (new File(evosuiteTestsDir).isDirectory()) {
			parseCMD(message);
			return true;
		}
		return false;
	}

	private void analyzeTest(String testFilePath, String reportPath) {
		TestFile linenumber = new TestFile(subject, testFilePath);
		linenumber.parseFile();
	}

	private Map<String, List<String>> parseCSV(String csvPath) {
		Map<String, List<String>> esStaticsMap = new HashMap<>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(csvPath), "utf-8"));
			String line = reader.readLine();
			String[] items = line.split(",");
			for (String item : items) {
				esStaticsMap.put(item, new ArrayList<>());
			}
			while ((line = reader.readLine()) != null) {
				items = line.split(",");

				List<String> list = esStaticsMap.get("TARGET_CLASS");
				list.add(items[0]);
				esStaticsMap.put("TARGET_CLASS", list);

				list = esStaticsMap.get("criterion");
				list.add(items[1]);
				esStaticsMap.put("criterion", list);

				list = esStaticsMap.get("Coverage");
				list.add(items[2]);
				esStaticsMap.put("Coverage", list);

				list = esStaticsMap.get("Total_Goals");
				list.add(items[3]);
				esStaticsMap.put("Total_Goals", list);

				list = esStaticsMap.get("Covered_Goals");
				list.add(items[4]);
				esStaticsMap.put("Covered_Goals", list);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		for (Map.Entry<String, List<String>> entry : esStaticsMap.entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue().toString());
		}
		// remove reports
		// FileIO.deleteDir(new File(csvPath).getParent());
		return esStaticsMap;
	}

	private void parseCMD(List<String> message) {
		coverageCriteria = new HashMap<>();
		coverageCriteria.put("LINE", "0");
		coverageCriteria.put("BRANCH", "0");
		coverageCriteria.put("METHOD", "0");
		coverageCriteria.put("average", "0");
		for (int i = 0; i < message.size(); i++) {
			String s = message.get(i);
			if (s.startsWith("* Going to analyze")) {
				System.out.println("Start analyzing the coverage criteria");
			} else if (s.endsWith("LINE")) {
				String[] words = message.get(i + 1).split(" ");
				coverageCriteria.put("LINE", words[words.length - 1]);
			} else if (s.endsWith("BRANCH")) {
				String[] words = message.get(i + 1).split(" ");
				coverageCriteria.put("BRANCH", words[words.length - 1]);
			} else if (s.endsWith("METHOD")) {
				String[] words = message.get(i + 1).split(" ");
				coverageCriteria.put("METHOD", words[words.length - 1]);
			} else if (s.startsWith("* Resulting test suite's coverage")) {
				String[] words = s.split(" ");
				coverageCriteria.put("average", words[5]);
			}
		}
		 for (Map.Entry<String, String> entry : coverageCriteria.entrySet()) {
		 System.out.println(entry.getKey() + ": " + entry.getValue());
		 }
	}

	public static void main(String[] args) {
		Project subject = new Project("Math", 41);
		subject.initPatchListByPath(Constant.AllPatchPath);
		Patch patch = subject.getPatchList().get(0);
		TestGenerator tg = new TestGenerator(patch);
		tg.mainProcess();
	}

}
