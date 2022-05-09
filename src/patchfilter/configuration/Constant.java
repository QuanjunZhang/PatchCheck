package patchfilter.configuration;

public class Constant {

	public static String HOME = "D:/Graduation/PatchCheck";
	// for normal program run
	public static String PROJECT_HOME = "D:/Graduation/Dataset/Projects";
	public static String JAVA_HOME = "C:/Program Files/Java/jdk1.8.0_201";
	public static String PERL_HOME = "C:/Strawberry/perl";

	public static String D4J_HOME = "D:/Graduation/Dataset/defects4j";
	public static String COMMAND_D4J = D4J_HOME + "/framework/bin/defects4j ";
	public static String COMMAND_TIMEOUT = "gtimeout ";

	public static String DUMPER_HOME = HOME + "/src/auxiliary/";

	public static String PROJ_INFO = HOME + "/d4j-info/src_path";
	public static String LOG_FILE = HOME + "/log/";

	public static String AllPatchPath = HOME + "/FinalPatch/";
	public static final String BProcessPatches = HOME + "/2bProcessPatches";

	public static String Record = HOME + "/tmp/";
	public static String CACHE = HOME + "/cache/";

	public static String CorrectPatchInfo = HOME + "/d4j-info/";

	public final static String COMMAND_CD = "cd ";
	public final static String COMMAND_PERL = "perl ";
	public final static String COMMAND_DIFF = "diff -w -r -u ";
	public final static String COMMAND_PATCH = "patch -u -p0 ";
	public final static int COMPILE_TIMEOUT = 300;
	public final static int TEST_TIMEOUT = 300;
	public final static int FailingTestNumber = 3;

	public static String ENV_D4J = "DEFECTS4J_HOME";

	// for mac
	public final static String ANT_BUILD_FAILED = "FAIL";
	public final static String PATCH_FAILED = "FAILED";
	public final static String JUNIT_FAILED = "Failure";

	public static String TRACE_RESULT_HOME = HOME + "/result/";

	public static String JUNIT_RUN = HOME + "/lib";
	public static String JUNIT_RUN_MAIN = "SingleJUnitTestRunner";

	public static final String INSTRUMENT_DOT_SEPARATOR = ".";
	public static final String INSTRUMENT_START = " START";
	public static final String INSTRUMENT_END = " END";
	public static final String INSTRUMENT_LINE_DIR = "/line/";
	public static final String INSTRUMENT_STATE_DIR = "/state/";

	public static final String INSTRUMENT_INIT_SEPARATORINIT = "initial";
	public static final String INSTRUMENT_FIXED_SEPARATORINIT = "fixed";
	public static final String INSTRUMENT_MAP = "map";
	public static final String INSTRUMENT_STATE_SEPARATORINIT = "_state";
	public static final String INSTRUMENT_LINE_SEPARATORINIT = "_line";

	public static final String MAPTRACE = "_maptrace";
	// public static final String INSTRUMENT_FIX_SEPARATORINIT = "fix";

	// test use
	public static final String evosuiteJarPath = HOME + "/src/auxiliary/lib/evosuite.jar";

}
