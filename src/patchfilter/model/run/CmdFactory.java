package patchfilter.model.run;

import lombok.extern.slf4j.Slf4j;
import patchfilter.configuration.Constant;
import patchfilter.model.entity.Project;

@Slf4j
public class CmdFactory {

	public static String[] createBuildSubjectCmd(Project subject) {
		return createD4JCmd(subject, "compile", Constant.COMPILE_TIMEOUT);
	}

	public static String[] createTestSubjectCmd(Project subject, int timeout) {
		return createD4JCmd(subject, "test", timeout);
	}

	public static String[] createTestSingleTestCaseCmd(Project subject, int timeout, String clazzAndMethod) {
		return createD4JCmd(subject, "test -t " + clazzAndMethod, timeout);
	}

	public static String[] createTestSingleTestCaseCmd(Project subject, int timeout, String clazz, String method) {
		return createD4JCmd(subject, "test -t " + clazz + "::" + method, timeout);
	}

	public static String[] createTestSingleTestCaseCmd(Project subject, String clazz, String method) {
		return createD4JCmd(subject, "test -t " + clazz + "::" + method, -1);
	}

	public static String[] createTestSingleTestCaseCmd(Project subject, String failTest) {
		return createD4JCmd(subject, "test -t " + failTest, -1);
	}

	private static String[] createD4JCmd(Project subject, String args, int timeout) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(Constant.COMMAND_CD).append(subject.getHome()).append(" && ");
		log.info(stringBuilder.toString());
		String[] D4JCmd = null;
		if (System.getProperty("os.name").startsWith("Windows")) {
			stringBuilder.append(Constant.COMMAND_PERL).append(Constant.COMMAND_D4J).append(" ").append(args);
			D4JCmd = new String[] { "cmd", "/C", stringBuilder.toString() };
			// System.out.println(stringBuilder.toString());
		} else {
			stringBuilder.append(Constant.COMMAND_D4J).append(" ").append(args);
			D4JCmd = new String[] { "/bin/bash", "-c", stringBuilder.toString() };
			System.out.println(stringBuilder.toString());
		}
		return D4JCmd;
	}

	public static String[] createTestSingleTestByJUnit(Project subject, String Arg) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(Constant.COMMAND_CD).append(subject.getHome()).append(" && ");
		stringBuilder.append(Arg);
		log.info(stringBuilder.toString());
		String[] testSingleTestByJUnitCmd = null;
		if (System.getProperty("os.name").startsWith("Windows")) {
			testSingleTestByJUnitCmd = new String[] { "cmd", "/C", stringBuilder.toString() };
		} else {
			testSingleTestByJUnitCmd = new String[] { "/bin/bash", "-c", stringBuilder.toString() };
		}
		System.out.println(stringBuilder.toString());
		return testSingleTestByJUnitCmd;
	}

	public static String[] createCompileEvoTestCmd(Project subject, String Arg) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(Constant.COMMAND_CD).append(subject.getHome()).append(" && ");
		stringBuilder.append(Arg);
		log.info(stringBuilder.toString());
		System.out.println(stringBuilder.toString());
		String[] GenTestCmd = null;
		if (System.getProperty("os.name").startsWith("Windows")) {
			GenTestCmd = new String[] { "cmd", "/C", stringBuilder.toString() };
		} else {
			GenTestCmd = new String[] { "/bin/bash", "-c", stringBuilder.toString() };
		}
		return GenTestCmd;
	}

	public static String[] createGenTestCmd(Project subject, String Arg) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(Constant.COMMAND_CD).append(subject.getHome()).append(" && ");
		stringBuilder.append(Arg);
		log.info(stringBuilder.toString());
		System.out.println(stringBuilder.toString());
		String[] GenTestCmd = null;
		if (System.getProperty("os.name").startsWith("Windows")) {
			GenTestCmd = new String[] { "cmd", "/C", stringBuilder.toString() };
		} else {
			GenTestCmd = new String[] { "/bin/bash", "-c", stringBuilder.toString() };
		}
		return GenTestCmd;
	}

	public static String[] createBTraceCmd(Project subject, String Arg, int timeout) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(Constant.COMMAND_D4J).append(Arg);
		log.info(stringBuilder.toString());
		String[] BTraceCmd = null;
		if (System.getProperty("os.name").startsWith("Windows")) {
			BTraceCmd = new String[] { "cmd", "/C", stringBuilder.toString() };
		} else {
			BTraceCmd = new String[] { "/bin/bash", "-c", stringBuilder.toString() };
		}
		return BTraceCmd;
	}

	public static String[] createPatchCmd(String targetFile, String patchFile) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(Constant.COMMAND_PATCH).append(targetFile).append(" ").append(patchFile);
		log.info(stringBuilder.toString());
		String[] PatchCmd = null;
		if (System.getProperty("os.name").startsWith("Windows")) {
			PatchCmd = new String[] { "cmd", "/C", stringBuilder.toString() };
		} else {
			PatchCmd = new String[] { "/bin/bash", "-c", stringBuilder.toString() };
			System.out.println(PatchCmd.toString());
		}
		return PatchCmd;
	}

	public static String[] createDiffCmd(String sourceFile, String targetFile, String patchFile) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(Constant.COMMAND_DIFF).append(sourceFile).append(" ").append(targetFile).append(">")
				.append(patchFile);
		log.info(stringBuilder.toString());
		String[] DiffCmd = null;
		if (System.getProperty("os.name").startsWith("Windows")) {
			DiffCmd = new String[] { "cmd", "/C", stringBuilder.toString() };
		} else {
			DiffCmd = new String[] { "/bin/bash", "-c", stringBuilder.toString() };
			System.out.println(DiffCmd.toString());
		}
		return DiffCmd;
	}
}
