package patchfilter.model.service;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import patchfilter.configuration.Constant;
import patchfilter.model.entity.Project;
import patchfilter.model.run.Runner;
import patchfilter.model.util.FileIO;

@Slf4j
public class TestEvoTestService {

	private Project project;

	List<String> testMessage;

	public TestEvoTestService(Project project) {
		this.project = project;
		testMessage=new ArrayList<>();
	}

	public List<String> getMessage() {
		return testMessage;
	}

	public void runTest(String testClass) {
		List<String> message = Runner.runEvoTest(project, testClass);
		for (String s : message) {
			if (s.contains("SLF4J") || s.startsWith(".")) {
				continue;
			}
			testMessage.add(s);
		}
	}

	public boolean compileEvoTest(String evoTest) {
		FileIO.copyDirectory(Constant.DUMPER_HOME + "lib/evoDependency", project.getHome() + "/evoDependency");
		List<String> message = Runner.compileEvoTest(project, evoTest);
		log.info(message.toString());
		return true;
	}

	public static void main(String[] args) {
		Project subject = new Project("Math", 41);
		String evoTest = "evosuite-tests/org/apache/commons/math/stat/descriptive/moment/*.java";
		String testClass = "org.apache.commons.math.stat.descriptive.moment.Variance_ESTest";
		TestEvoTestService st = new TestEvoTestService(subject);
		st.compileEvoTest(evoTest);
		st.runTest(testClass);
	}

}
