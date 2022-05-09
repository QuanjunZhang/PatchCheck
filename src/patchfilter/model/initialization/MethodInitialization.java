package patchfilter.model.initialization;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.alibaba.fastjson.JSONObject;

import patchfilter.configuration.Constant;
import patchfilter.model.entity.*;
import patchfilter.model.initialization.visitor.MethodVisitor;
import patchfilter.model.util.FileIO;
import patchfilter.model.util.PatchInfo;
import patchfilter.util.TestFile;

/*
 * 整理 cache 信息
 * 
 * fixedfile information
 * patch_methodname information
 * patch_method information 即补丁 patch 所修改的方法的信息 - method_name method_content method_startline method_endline 
 * 为了 之后插件能直接读取
 */
public class MethodInitialization {
	private Project project;

	public MethodInitialization(Project project) {
		this.project = project;
	}

	public void MainProcess() {
		List<Patch> patchList = project.getPatchList();
		PatchInfo.obainAllMethod(patchList); // patch 信息整理
		for (Patch patchFile : patchList) {
			String fixedFile = patchFile.getFixedFile();
			String methodName = patchFile.getModifiedMethod();
			Method method = new Method(methodName, fixedFile);
			MethodVisitor methodVisitor = new MethodVisitor();
			methodVisitor.setMethod(method);
			CompilationUnit compilationUnit = FileIO.genASTFromSource(FileIO.readFileToString(fixedFile),
					ASTParser.K_COMPILATION_UNIT);
			compilationUnit.accept(methodVisitor);
			method = methodVisitor.getMethod();
			String patchMethodFile = Constant.CACHE + patchFile.getSubject().getName() + "/"
					+ patchFile.getSubject().getId() + "/" + patchFile.getPatchName() + "_method";
			FileIO.writeStringToFile(patchMethodFile, JSONObject.toJSONString(method), false);
		}
	}

	public void MainProcess4TestFile() {
		List<String> failedTests = project.getFailedTestList();
		for (String failedTest : failedTests) {
			String failingTestPath = project.getHome() + "/" + project.getTsrc() + "/"
					+ failedTest.split("::")[0].replace(".", "/") + ".java";
			TestFile testFile = new TestFile(project, failingTestPath);
			testFile.parseFile();
		}
	}

	public static void main(String[] args) {
		String project = "Math";
		int Start = 41;
		int end = 41;
		for (int i = Start; i <= end; i++) {
			Project subject = new Project(project, i);
			if (subject.initPatchListByPath(Constant.AllPatchPath)) {
				System.out.println("Process : " + subject.toString());
				MethodInitialization methodInitialization = new MethodInitialization(subject);
				methodInitialization.MainProcess();
				methodInitialization.MainProcess4TestFile();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
