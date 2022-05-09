package patchfilter.model.entity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileNotFoundException;
import java.util.LinkedList;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import patchfilter.configuration.Constant;
import patchfilter.model.util.FileIO;

@Slf4j
public class Project {

	private String _name = null;
	private int _id = 0;
	private String _ssrc = null;
	private String _tsrc = null;
	private String _sbin = null;
	private String _tbin = null;
	private String _dependency = null;
	private List<String> _instrumentPackages = null;
	private List<String> _testClasses = null;
	private LinkedList<Patch> patchList = new LinkedList<>();

	public Project(String name, int id) {
		_name = name;
		_id = id;
		this.setSrcPath();
		this.setDependency();
	}

	private void setSrcPath() {
		// Constant.PROJ_INFO = InPaFer/d4j-info/src_path
		String fileName = Constant.PROJ_INFO + "/" + _name + "/" + _id + ".txt";
		File file = new File(fileName);
		if (!file.exists()) {
			System.out.println("File : " + fileName + " does not exist!");
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String line = null;
		List<String> source = new ArrayList<String>();
		try {
			while ((line = br.readLine()) != null) {
				source.add(line);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (source.size() < 4) {
			System.err.println("PROJEC INFO CONFIGURE ERROR !");
			System.exit(0);
		}
		// /src/main/java 不同类型的项目不一样
		_ssrc = source.get(0);
		// /target/classes
		_sbin = source.get(1);
		// /src/test/java
		_tsrc = source.get(2);
		// /target/test-classes
		_tbin = source.get(3);
	}

	public Project(String name, int id, String ssrc, String tsrc, String sbin, String tbin) {
		this(name, id, ssrc, tsrc, sbin, tbin, null);
	}

	public Project(String name, int id, String ssrc, String tsrc, String sbin, String tbin, String dependency) {
		_name = name;
		_id = id;
		_ssrc = ssrc;
		_tsrc = tsrc;
		_sbin = sbin;
		_tbin = tbin;
		_dependency = dependency;
	}

	private void setDependency() {
		String classpath = Constant.D4J_HOME + "/major/lib/junit-4.11.jar:" + Constant.JUNIT_RUN;
		switch (_name) {
		case "Math":
			classpath = classpath + ":target/test-classes:target/classes";
			break;
		case "Chart":
			classpath = classpath + ":build:build-tests";
			break;
		case "Lang":
			classpath = classpath + ":target/test-classes:target/tests:target/classes";
			break;
		case "Closure":
			classpath = classpath + ":build/lib/rhino.jar:lib/*:build/classes:build/test";
			break;
		case "Time":
			classpath = classpath + ":target/test-classes:target/classes:build/classes:build/tests";
			break;
		case "mockito":
			break;
		default:
			System.err.println("UNKNOWN project name : " + _name);
		}
		this._dependency = classpath;
	}

	public String getName() {
		return _name;
	}

	public int getId() {
		return _id;
	}

	public String getSsrc() {
		return _ssrc;
	}

	public String getTsrc() {
		return _tsrc;
	}

	public String getSbin() {
		return _sbin;
	}

	public String getTbin() {
		return _tbin;
	}

	public String getDependency() {
		return _dependency;
	}

	public List<String> getTestClasses() {
		if (_testClasses == null) {
			_testClasses = new ArrayList<>(getTestClasses(new File(getHome() + _tsrc)));
		}
		return _testClasses;
	}

	public List<String> getInstrumentPackage() {
		if (_instrumentPackages == null) {
			_instrumentPackages = new ArrayList<>(getPackage(getHome() + _ssrc, getHome() + _ssrc));
		}
		return _instrumentPackages;
	}

	/**
	 * Absolute home path for subject e.g., /Projects/Math/Math_41_buggy/"
	 */
	public String getHome() {
		return Constant.PROJECT_HOME + "/" + _name + "/" + _name + "_" + _id + "_buggy";
	}

	public String getFailedTestRecFile() {
		return getHome() + "/failing_tests";
	}

	public String getAllTestRecFile() {
		return getHome() + "/all-tests.txt";
	}

	public List<String> getFailedTestList() {
		String failTestFile = Constant.HOME + "/d4j-info/trigger_tests/" + _name + "/" + _id + ".txt";
		String content = FileIO.readFileToString(failTestFile);
		List<String> failedTestList = new LinkedList<String>();
		for (String line : content.split("\n")) {
			failedTestList.add(line);
		}
		return failedTestList;
	}

	public void backup(String folder) throws IOException {
		File file = new File(folder + "_ori");
		if (!file.exists()) {
			FileUtils.copyDirectory(new File(folder), file);
		} else {
			FileUtils.deleteDirectory(new File(folder));
			FileUtils.copyDirectory(file, new File(folder));
		}
	}

	public void restore(String folder) throws IOException {
		File file = new File(folder + "_ori");
		if (file.exists()) {
			FileUtils.copyDirectory(file, new File(folder));
		} else {
			System.out.println("Restore source file failed : cannot find file " + file.getAbsolutePath());
		}
	}

	public void backup() throws IOException {
		String src = getHome() + _ssrc;
		File file = new File(src + "_ori");
		if (!file.exists()) {
			FileUtils.copyDirectory(new File(src), file);
		} else {
			FileUtils.deleteDirectory(new File(src));
			FileUtils.copyDirectory(file, new File(src));
		}
	}

	public void restore() throws IOException {
		String src = getHome() + _ssrc;
		File file = new File(src + "_ori");
		if (file.exists()) {
			FileUtils.copyDirectory(file, new File(src));
		} else {
			System.out.println("Restore source file failed : cannot find file " + file.getAbsolutePath());
		}
	}

	private Set<String> getPackage(String rootPath, String currPath) {
		Set<String> packages = new HashSet<>();
		File file = new File(currPath);
		File[] files = file.listFiles();
		for (File f : files) {
			if (f.getName().equals(".DS_Store")) {
				continue;
			}
			if (f.isDirectory()) {
				String absPath = f.getAbsolutePath();
				String packageName = absPath.replace(rootPath + "/", "");
				packageName = packageName.replace("/", ".");
				packages.add(packageName);
				packages.addAll(getPackage(rootPath, f.getAbsolutePath()));
			}
		}
		return packages;
	}

	private Set<String> getTestClasses(File root) {
		Set<String> classes = new HashSet<>();
		File[] files = root.listFiles();
		String pack = null;
		for (File f : files) {
			if (f.isFile()) {
				String fName = f.getName();
				if (fName.equals(".DS_Store")) {
					continue;
				}
				if (fName.endsWith(".java")) {
					fName = fName.substring(0, fName.length() - 5);
					if (!fName.endsWith("Tests")) {
						continue;
					}
					if (pack != null) {
						classes.add(pack + "." + fName);
					} else {
						BufferedReader br = null;
						try {
							br = new BufferedReader(new FileReader(f));
							String line = null;
							while ((line = br.readLine()) != null) {
								line = line.trim();
								if (line.startsWith("package")) {
									Pattern pattern = Pattern.compile("(?<=package\\s)[\\s\\S]*(?=;)");
									Matcher matcher = pattern.matcher(line);
									if (matcher.find()) {
										pack = matcher.group(0);
										classes.add(pack + "." + fName);
										break;
									}
								}
							}
							br.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			} else if (f.isDirectory()) {
				classes.addAll(getTestClasses(f));
			}
		}
		return classes;
	}

	public void deleteTarget() {
		log.info("----- Delete Class File ---- ");
		try {
			FileUtils.deleteDirectory(new File(getHome() + "/" + "target"));
			FileUtils.deleteDirectory(new File(getHome() + "/" + "build"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean initPatchListByPath(String path) {
		this.patchList.clear();
		String patchPath = path + "/" + _name + "/" + _id;
		File patchList = new File(patchPath);
		if (!patchList.exists()) {
			return false;
		}
		File[] patches = patchList.listFiles();
		for (int i = 0; i < patches.length; i++) {
			String patchAbPath = patches[i].getAbsolutePath();
			if (patchAbPath.contains(".DS_Store")) {
				continue;
			}
			Patch patchFile = new Patch(this, patchAbPath);
			patchFile.parsePatch();
			patchFile.setAliaName("Patch" + (i + 1));
			this.patchList.add(patchFile);
		}
		return true;
	}

	public LinkedList<Patch> getPatchList() {
		return this.patchList;
	}

	@Override
	public String toString() {
		return "Subject [_name = " + _name + ", _id = " + _id + "]";
	}

}