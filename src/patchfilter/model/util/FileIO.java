package patchfilter.model.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class FileIO {

	public static String readFileToString(String filePath) {
		if (filePath == null) {
			return "";
		}
		File file = new File(filePath);
		if (!file.exists() || !file.isFile()) {
			return "";
		}
		return readFileToString(file);
	}

	public static String readFileToString(File file) {
		if (file == null) {
			return "";
		}
		StringBuilder stringBuilder = new StringBuilder();
		InputStream inputStream = null;
		InputStreamReader inputStreamReader = null;
		try {
			inputStream = new FileInputStream(file);
			inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
			char[] ch = new char[1024];
			int readCount = 0;
			while ((readCount = inputStreamReader.read(ch)) != -1) {
				stringBuilder.append(ch, 0, readCount);
			}
			inputStreamReader.close();
			inputStream.close();
		} catch (Exception e) {
			if (inputStreamReader != null) {
				try {
					inputStreamReader.close();
				} catch (IOException e1) {
					return "";
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e1) {
					return "";
				}
			}
		}
		return stringBuilder.toString();
	}

	public static boolean writeStringToFile(String filePath, String string) {
		return writeStringToFile(filePath, string, false);
	}

	public static boolean writeStringToFile(String filePath, String string, boolean append) {
		if (filePath == null) {
			return false;
		}
		File file = new File(filePath);
		return writeStringToFile(file, string, append);
	}

	public static boolean writeStringToFile(File file, String string) {
		return writeStringToFile(file, string, false);
	}

	public static boolean writeStringToFile(File file, String string, boolean append) {
		if (file == null || string == null) {
			return false;
		}
		if (!file.exists()) {
			try {
				file.getParentFile().mkdirs();
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		BufferedWriter bufferedWriter = null;
		try {
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), "UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			bufferedWriter.write(string);
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bufferedWriter != null) {
				try {
					bufferedWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	public static boolean writeStringToLog(String logFile, String info) {
		return writeStringToFile(logFile, info + "\n", true);
	}

	public static boolean writeStringToLog(File logFile, String info) {
		return writeStringToFile(logFile, info + "\n", true);
	}

	public static void deleteComments(String filePath) {
		try {
			File file = new File(filePath);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			StringBuilder result = new StringBuilder();
			boolean breakContinue = false;
			while (br.ready()) {
				line = br.readLine();
				if (line.trim().equals("")) {
					continue;
				}
				if (line.trim().startsWith("//")) {
					continue;
				}
				if (line.trim().startsWith("/*")) {
					breakContinue = true;
				}
				if (line.trim().endsWith("*/")) {
					breakContinue = false;
					continue;
				}
				if (!breakContinue)
					result.append(line).append("\n");
			}
			br.close();
			String content = result.toString()
					.replaceAll("\\/\\/[^\\n]*|\\/\\*([^\\*^\\/]*|[\\*^\\/*]*|[^\\**\\/]*)*\\*+\\/", "");
			FileIO.writeStringToFile(filePath, content.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String deleteZhushi(String line) {
		int index = line.indexOf("//");
		return index >= 0 ? line.substring(0, index) : line;
	}

	public static void normalizeFile(String filePath) {
		try {
			File file = new File(filePath);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			StringBuilder result = new StringBuilder();

			while (br.ready()) {
				line = deleteZhushi(br.readLine());
				if (!line.trim().startsWith("{") && !line.trim().startsWith("@") && !line.endsWith("{")
						&& !line.endsWith(";") && !line.endsWith("}")) {
					result.append(line.replaceAll("   ", ""));
					continue;
				}
				result.append(line).append("\n");
			}
			br.close();
			FileIO.writeStringToFile(filePath, result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void backUpFile(String source, String sourceCopy) {
		File sourceFile = new File(source);
		File sourceCopyFile = new File(sourceCopy);
		try {
			if (sourceCopyFile.exists()) {
				FileUtils.copyFile(sourceCopyFile, sourceFile);
			} else {
				FileUtils.copyFile(sourceFile, sourceCopyFile);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void restoreFile(String source, String sourceCopy) {
		File sourceFile = new File(source);
		File sourceCopyFile = new File(sourceCopy);
		if (sourceCopyFile.exists()) {
			try {
				FileUtils.copyFile(sourceCopyFile, sourceFile);
				deleteFile(sourceCopy);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void copyFile(String source, String sourceCopy) {
		File sourceFile = new File(source);
		File sourceCopyFile = new File(sourceCopy);
		try {
			FileUtils.copyFile(sourceFile, sourceCopyFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void copyDirectory(String source, String sourceCopy) {
		File file = new File(source);
		String[] filePath = file.list();
		if (!(new File(sourceCopy)).exists()) {
			(new File(sourceCopy)).mkdir();
		}
		for (int i = 0; i < filePath.length; i++) {
			if (new File(source + File.separator + filePath[i]).isDirectory()) {
				copyDirectory(source + File.separator + filePath[i], sourceCopy + File.separator + filePath[i]);
			}
			if (new File(source + File.separator + filePath[i]).isFile()) {
				copyFile(source + File.separator + filePath[i], sourceCopy + File.separator + filePath[i]);
			}
		}
	}

	public static void deleteFile(String file) {
		File deleteFile = new File(file);
		if (deleteFile.exists()) {
			try {
				FileUtils.forceDelete(deleteFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void deleteDir(String file) {
		File deleteDir = new File(file);
		if (deleteDir.exists()) {
			try {
				FileUtils.deleteDirectory(deleteDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static List<File> getAllFile(File file, List<File> fileList) {
		if (fileList == null) {
			fileList = new LinkedList<File>();
		}
		for (File f : file.listFiles()) {
			if (f.isDirectory()) {
				getAllFile(f, fileList);
			} else if (f.isFile()) {
				fileList.add(f);
			}
		}
		return fileList;
	}

	public static CompilationUnit genASTFromICU(ICompilationUnit icu) {
		ASTParser astParser = ASTParser.newParser(AST.JLS8);
		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
		astParser.setCompilerOptions(options);
		astParser.setSource(icu);
		astParser.setKind(ASTParser.K_COMPILATION_UNIT);
		astParser.setResolveBindings(true);
		return (CompilationUnit) astParser.createAST(null);
	}

	public static CompilationUnit genASTFromSource(String sourceCode, int type) {
		ASTParser astParser = ASTParser.newParser(AST.JLS8);
		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
		astParser.setCompilerOptions(options);
		astParser.setSource(sourceCode.toCharArray());
		astParser.setKind(type);
		astParser.setResolveBindings(true);
		return (CompilationUnit) astParser.createAST(null);
	}

	public static CompilationUnit genASTFromFile(String fileName) {
		return (CompilationUnit) genASTFromSource(readFileToString(fileName), ASTParser.K_COMPILATION_UNIT);
	}

	public static CompilationUnit genASTFromFile(File file) {
		return (CompilationUnit) genASTFromSource(readFileToString(file), ASTParser.K_COMPILATION_UNIT);
	}

	private static String[] getClassPath() {
		String property = System.getProperty("java.class.path", ".");
		return property.split(File.pathSeparator);
	}

	public static void main(String[] args) {
		String source = "/home/xushicheng/eclipse-workspace/InPaFer/src/auxiliary/lib/evoDependency";
		String sourceCopy = "/home/xushicheng/testgentest/jar/evoDependency";
		copyDirectory(source, sourceCopy);
	}

}
