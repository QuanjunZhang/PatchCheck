package patchfilter.controller;

import java.io.File;
import java.util.List;
import java.util.Map;

import patchfilter.model.entity.Patch;
import patchfilter.model.entity.Project;
import patchfilter.model.service.GenerateService;
import patchfilter.model.service.InstallPatch4GenService;
import patchfilter.model.service.TestEvoTestService;
import patchfilter.model.util.FileIO;

public class TestController {

	private Project project;
	private Patch patch;

	private List<String> newTestList;
	Map<String, String> coverageCriteria;
	String clazz;
	List<String> message;

	public TestController(Patch patch) {
		this.patch = patch;
		this.project = patch.getSubject();
	}

	public void genNewTest() {
		String fixedFile = patch.getFixedFile();
		if (new File(fixedFile + ".bak").exists()) {
			FileIO.restoreFile(fixedFile, fixedFile + ".bak");
		}
		FileIO.backUpFile(fixedFile, fixedFile + ".bak");

		InstallPatch4GenService patchInstallation = new InstallPatch4GenService(patch);
		patchInstallation.mainProcess();
		String evosuiteTestsDir = project.getHome() + "/evosuite-tests/";
		String evosuiteReportDir = project.getHome() + "/evosuite-report/";
		if (new File(evosuiteTestsDir).exists()) {
			new File(evosuiteTestsDir).delete();
		}
		if (new File(evosuiteReportDir).exists()) {
			new File(evosuiteReportDir).delete();
		}
		GenerateService testGeneration = new GenerateService(patch);
		testGeneration.mainProcess();
		newTestList = testGeneration.getNewTestList();
		coverageCriteria = testGeneration.getCoverageCriteria();
		clazz = testGeneration.getClazz();
	}

	public void testProject() {
		TestEvoTestService projectTester = new TestEvoTestService(project);
		String evoTestClass = "evosuite-tests/" + clazz.replace(".", "/");
		int x = evoTestClass.lastIndexOf("/");
		String evoTest = evoTestClass.substring(0, x) + "/*.java";
		String testClass = clazz + "_ESTest";
		projectTester.compileEvoTest(evoTest);
		projectTester.runTest(testClass);
		message = projectTester.getMessage();
	}

	public List<String> getNewTestList() {
		return newTestList;
	}

	public Map<String, String> getCoverageCriteria() {
		return coverageCriteria;
	}

	public String getClazz() {
		return clazz;
	}

	public List<String> getMessage() {
		return message;
	}

	public static void main(String[] args) {
		String s = "org.apache.commons.math.stat.descriptive.moment.Variance";
		String str = "evosuite-tests/" + s.replace(".", "/");
		int a = str.lastIndexOf("/");
		String str2 = str.substring(0, a) + "/*.java";
		System.out.println(str2);
	}

}
