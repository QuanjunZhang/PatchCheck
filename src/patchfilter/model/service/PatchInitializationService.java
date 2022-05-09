package patchfilter.model.service;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import patchfilter.configuration.Constant;
import patchfilter.model.entity.Patch;
import patchfilter.model.entity.Project;
import patchfilter.model.initialization.visitor.LineDiffInstrumentVisitor;
import patchfilter.model.initialization.visitor.TraversalVisitor;
import patchfilter.model.initialization.visitor.VarCollectionInstrumentVisitor;
import patchfilter.model.run.Runner;
import patchfilter.model.util.FileIO;
import patchfilter.model.util.PatchInfo;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Data
@Builder
@NoArgsConstructor
public class PatchInitializationService {

	// 对更改方法插桩以收集 var
	public void instrumentVarCollection(Map<String, List<Patch>> fixedFileMap, String writeFile) {
		for (Map.Entry<String, List<Patch>> entry : fixedFileMap.entrySet()) {
			String fixedFile = entry.getKey();
			Set<String> methodSet = entry.getValue().stream().filter(Objects::nonNull).map(Patch::getModifiedMethod)
					.collect(Collectors.toSet());
			VarCollectionInstrumentVisitor varCollectionInstrumentVisitor = new VarCollectionInstrumentVisitor();
			varCollectionInstrumentVisitor.setIntrumentMethodSet(methodSet);
			varCollectionInstrumentVisitor.setWriteFile(writeFile);
			writeInstrumentFile(fixedFile, varCollectionInstrumentVisitor);
		}
	}

	public void instrumentVarCollection(Patch patch, String writeFile) {
		String fixedFile = patch.getFixedFile();
		Set<String> methodSet = Collections.singleton(patch.getModifiedMethod());
		VarCollectionInstrumentVisitor varCollectionInstrumentVisitor = new VarCollectionInstrumentVisitor();
		varCollectionInstrumentVisitor.setIntrumentMethodSet(methodSet);
		varCollectionInstrumentVisitor.setWriteFile(writeFile);
		writeInstrumentFile(fixedFile, varCollectionInstrumentVisitor);
	}

	// 对更改方法插桩以收集 line diff
	public void instrumentLineDiff(Map<String, List<Patch>> fixedFileMap, String writeFile) {
		for (Map.Entry<String, List<Patch>> entry : fixedFileMap.entrySet()) {
			String fixedFile = entry.getKey();
			Set<String> methodSet = entry.getValue().stream().filter(Objects::nonNull).map(Patch::getModifiedMethod)
					.collect(Collectors.toSet());
			LineDiffInstrumentVisitor lineDiffInstrumentVisitor = new LineDiffInstrumentVisitor();
			lineDiffInstrumentVisitor.setInstrumentSet(methodSet);
			lineDiffInstrumentVisitor.setWriteFile(writeFile);
			writeInstrumentFile(fixedFile, lineDiffInstrumentVisitor);
		}
	}

	public void instrumentLineDiff(Patch patch, String writeFile) {
		String fixedFile = patch.getFixedFile();
		Set<String> methodSet = Collections.singleton(patch.getModifiedMethod());
		LineDiffInstrumentVisitor lineDiffInstrumentVisitor = new LineDiffInstrumentVisitor();
		lineDiffInstrumentVisitor.setInstrumentSet(methodSet);
		lineDiffInstrumentVisitor.setWriteFile(writeFile);
		writeInstrumentFile(fixedFile, lineDiffInstrumentVisitor);

	}

	private void writeInstrumentFile(String fixedFile, TraversalVisitor traversalVisitor) {
		System.out.println("Record File: " + fixedFile);
		String fixedFileContent = FileIO.readFileToString(fixedFile);
		CompilationUnit compilationUnit = FileIO.genASTFromSource(fixedFileContent, ASTParser.K_COMPILATION_UNIT);
		compilationUnit.accept(traversalVisitor);
		String compilationUnitContent = compilationUnit.toString();
		FileIO.writeStringToFile(fixedFile, compilationUnitContent);
	}

	// 编译项目
	public boolean Compile(Project subject) {
		String srcPath = subject.getHome() + "/" + subject.getSsrc();
		try {
			FileUtils.copyFile(new File(Constant.HOME + "/src/auxiliary/Dumper.java"),
					new File(srcPath + "/auxiliary/Dumper.java"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		subject.deleteTarget();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (!Runner.compileSubject(subject)) {
			log.error(subject.toString() + " Compile Failed! ");
			System.out.println(subject.toString() + " Compile Failed! ");
			return false;
		}
		log.info(subject.toString() + " Compile Success! ");
		System.out.println(subject.toString() + " Compile Success! ");
		return true;
	}

	// 执行失败的测试
	public void runFailTest(Project subject) {
		String resultString = Runner.jUnitTestSubject(subject, subject.getFailedTestList().get(0));
		log.info(resultString);
		System.out.println(resultString);
		// for (String failingTest :
		// subject.getFailedTestList()) { Runner.JUnitTestSubject(subject, failingTest);
		// }
	}

	public static void main(String[] args) {
		Project subject = new Project("Chart", 3);
		String PatchName = "SimFix-0";
		String path = Constant.AllPatchPath + "Chart/3/" + PatchName;
		Patch patchFile = new Patch(subject, path);
		patchFile.parsePatch();

		PatchInitializationService methodTraceService = new PatchInitializationService();
		PatchInfo.getModifyMethod(patchFile);
		Set<String> methodList = new LinkedHashSet<>();
		methodList.add(patchFile.getModifiedMethod());
		String fixedFile = patchFile.getFixedFile();

		String writeFile = "./test/init";

		FileIO.backUpFile(fixedFile, fixedFile + ".bak");
		methodTraceService.instrumentLineDiff(Collections.singletonMap(fixedFile, Collections.singletonList(patchFile)),
				writeFile);
		if (methodTraceService.Compile(subject)) {
			methodTraceService.runFailTest(subject);
		}
		FileIO.restoreFile(fixedFile, fixedFile + ".bak");

		FileIO.backUpFile(fixedFile, fixedFile + ".bak");
		patchFile.patchToFile();
		writeFile = "./test/" + PatchName;
		methodTraceService.instrumentLineDiff(Collections.singletonMap(fixedFile, Collections.singletonList(patchFile)),
				writeFile);
		if (methodTraceService.Compile(subject)) {
			methodTraceService.runFailTest(subject);
		}
		FileIO.restoreFile(fixedFile, fixedFile + ".bak");

		MapService traceMapService = new MapService(patchFile);
		traceMapService.traceMap(new LinkedHashSet<>());

		FileIO.backUpFile(fixedFile, fixedFile + ".bak");
		methodTraceService.instrumentVarCollection(
				Collections.singletonMap(fixedFile, Collections.singletonList(patchFile)), writeFile);
		if (methodTraceService.Compile(subject)) {
			methodTraceService.runFailTest(subject);
		}
		FileIO.restoreFile(fixedFile, fixedFile + ".bak");

		FileIO.backUpFile(fixedFile, fixedFile + ".bak");
		patchFile.patchToFile();
		methodTraceService.instrumentVarCollection(
				Collections.singletonMap(fixedFile, Collections.singletonList(patchFile)), writeFile);
		if (methodTraceService.Compile(subject)) {
			methodTraceService.runFailTest(subject);
		}
		FileIO.restoreFile(fixedFile, fixedFile + ".bak");
	}

}
