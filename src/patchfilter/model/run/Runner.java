package patchfilter.model.run;

import lombok.extern.slf4j.Slf4j;
import patchfilter.configuration.Constant;
import patchfilter.model.entity.Project;

import java.util.List;

@Slf4j
public class Runner {

	private final static String __name__ = "@Runner ";
	private final static String SUCCESSTEST = "Failing tests: 0";

	public static boolean compileSubject(Project subject) {
		System.out.println("Compile " + subject.toString());
		log.info("Compile " + subject.toString());
		List<String> message = null;
		try {
			message = Executor.execute(CmdFactory.createBuildSubjectCmd(subject));
		} catch (Exception e) {
			e.printStackTrace();
		}
		boolean success = true;
		for (int i = 0; i < message.size(); i++) {
			// System.out.println(message.get(i));
			if (message.get(i).contains(Constant.ANT_BUILD_FAILED)) {
				success = false;
			}
		}
		return success;
	}

	public static List<String> compileSubjectRs(Project subject) {
		System.out.println("Compile " + subject.toString());
		log.info("Compile " + subject.toString());
		List<String> message = null;
		try {
			message = Executor.execute(CmdFactory.createBuildSubjectCmd(subject));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message;
	}

	public static List<String> compileEvoTest(Project subject, String evoTest) {
		System.out.println("Compile " + evoTest);
		log.info("Compile " + evoTest);
		String evoDependency = "evosuite-tests:target/classes:evoDependency/evosuite-standalone-runtime-1.0.6.jar:evoDependency/junit-4.12.jar:evoDependency/hamcrest-core-1.3.jar";
		List<String> message = null;
		try {
			String javacArg = "javac -cp \"" + evoDependency + "\" " + evoTest;
			message = Executor.execute(CmdFactory.createCompileEvoTestCmd(subject, javacArg));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message;
	}

	public static boolean runTestSuite(Project subject) {
		List<String> message = null;
		try {
			System.out.println("TESTING : " + subject.getName() + "_" + subject.getId());
			message = Executor.execute(CmdFactory.createTestSubjectCmd(subject, 10 * 60));
		} catch (Exception e) {
			e.printStackTrace();
		}
		boolean success = false;
		for (int i = message.size() - 1; i >= 0; i--) {
			System.out.println(message.get(i));
			if (message.get(i).contains(SUCCESSTEST)) {
				success = true;
				break;
			}
		}
		return success;
	}

	public static List<String> runEvoTest(Project subject, String testClass) {
		System.out.println("TESTING : " + subject.getName() + "_" + subject.getId() + " " + testClass);
		String evoDependency = "evosuite-tests:target/classes:evoDependency/evosuite-standalone-runtime-1.0.6.jar:evoDependency/junit-4.12.jar:evoDependency/hamcrest-core-1.3.jar";
		List<String> message = null;
		try {
			String junitArg = Constant.JAVA_HOME + "/bin/java -cp \"" + evoDependency + "\" org.junit.runner.JUnitCore "
					+ testClass;
			message = Executor.execute(CmdFactory.createTestSingleTestByJUnit(subject, junitArg));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message;
	}

	public static boolean testSingleTest(Project subject, String clazz, String method) {
		return testSingleTest(subject, clazz + "::" + method);
	}

	private static boolean testSingleTest(Project subject, String clazzAndMethod) {
		List<String> message = null;
		try {
			System.out.println("TESTING : " + clazzAndMethod);
			message = Executor.execute(CmdFactory.createTestSingleTestCaseCmd(subject, 30, clazzAndMethod));
		} catch (Exception e) {
			e.printStackTrace();
		}
		boolean success = false;
		for (int i = message.size() - 1; i >= 0; i--) {
			// System.out.println(message.get(i));
			if (message.get(i).contains(SUCCESSTEST)) {
				success = true;
				break;
			}
		}
		return success;
	}

	public static boolean runFailTest(Project subject, String failTest) {
		List<String> message = null;
		try {
			System.out.println("TESTING : " + subject.getName() + "_" + subject.getId());
			message = Executor.execute(CmdFactory.createTestSingleTestCaseCmd(subject, failTest));
			// System.out.println(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		boolean success = false;
		for (int i = message.size() - 1; i >= 0; i--) {
			// System.out.println(message.get(i));
			if (message.get(i).contains(SUCCESSTEST)) {
				success = true;
				break;
			}
		}
		return success;
	}

	public static String jUnitTestSubject(Project subject, String failingTest) {
		System.out.println("Run Test For: " + failingTest);
		log.info("Begin Run Failing Test And Get Trace: " + failingTest);
		List<String> message = null;
		try {
			String junitArg = Constant.JAVA_HOME + "/bin/java -Xms4g -Xmx8g -cp \"" + subject.getDependency() + "\" "
					+ Constant.JUNIT_RUN_MAIN + " " + failingTest;
			message = Executor.execute(CmdFactory.createTestSingleTestByJUnit(subject, junitArg));
		} catch (Exception e) {
			e.printStackTrace();
		}
		String result = "";
		for (String s : message) {
			result = result + s + " ";
		}
		return result;
	}

	public static List<String> generateESTest(Project subject, String clazz) {
		System.out.println("Begin generating tests for: " + clazz);
		log.info("Begin generating tests for: " + clazz);
		List<String> message = null;
		String genArg = Constant.JAVA_HOME + "/bin/java -jar " + Constant.evosuiteJarPath + " -class " + clazz
				+ " -projectCP " + subject.getSbin().substring(1) + " -Dsearch_budget=20";
		message = Executor.execute(CmdFactory.createGenTestCmd(subject, genArg));
		return message;
	}

	public static List<String> generateESTest(Project subject, String clazz, String func) {
		System.out.println("Begin generating tests for: " + clazz);
		log.info("Begin generating tests for: " + clazz);
		List<String> message = null;
		String genArg = Constant.JAVA_HOME + "/bin/java -jar " + Constant.evosuiteJarPath + " -class " + clazz
				+ " -projectCP " + subject.getSbin().substring(1) + " -criterion branch:line" + " -Dsearch_budget=20"
				+ "-Djunit_check=false" + " -Dtarget_method=\"" + func + "\"";
		System.out.println(genArg);
		message = Executor.execute(CmdFactory.createGenTestCmd(subject, genArg));
		return message;
	}

	public static boolean traceSubject(Project subject, String arg) {
		List<String> message = null;
		try {
			message = Executor.execute(CmdFactory.createBTraceCmd(subject, arg, 90));
		} catch (Exception e) {
			// LevelLogger.fatal(__name__ + "#buildSubject run build subject failed !", e);
			System.out.println(e);
		}
		return true;
	}

	// targetFile: fixedFile patchFile: diffFile
	public static boolean patchFile(String targetFile, String patchFile) {
		List<String> message = null;
		boolean success = true;
		try {
			message = Executor.execute(CmdFactory.createPatchCmd(targetFile, patchFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (int i = 0; i < message.size(); i++) {
			System.out.println(message.get(i));
			if (message.get(i).contains(Constant.PATCH_FAILED)) {
				success = false;
				break;
			}
		}
		return success;
	}

	public static void diff2File(String sourceFile, String targetFile, String patchFile) {
		List<String> message = null;
		try {
			message = Executor.execute(CmdFactory.createDiffCmd(sourceFile, targetFile, patchFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (int i = 0; i < message.size(); i++) {
			System.out.println(message.get(i));
		}

	}
}
