package patchfilter.model.initialization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import patchfilter.configuration.Constant;
import patchfilter.model.entity.Patch;
import patchfilter.model.entity.Project;
import patchfilter.model.service.PatchInitializationService;
import patchfilter.model.util.FileIO;
import patchfilter.model.util.PatchInfo;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


/*
 * Get Correct Trace and Variable value for the subjects which do not contain correct patches.
 * notice that change the Constant.Project_home to fixed_projects when running this file.
 */
@Slf4j
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraceFixedMain {

	private Project subject;

	private Map<String, List<Patch>> fixedFileMap = new LinkedHashMap<>();

	public TraceFixedMain(Project subject) {
		this.subject = subject;
	}

	public void initModifiedMethodMap(List<Patch> patchFileList) {
		fixedFileMap = patchFileList.stream().filter(Objects::nonNull).collect(Collectors
				.groupingBy(Patch::getFixedFile, LinkedHashMap::new, Collectors.toCollection(LinkedList::new)));
	}

	public void mainProcessForLine() {
		List<Patch> patchFileList = subject.getPatchList();
		Set<String> fixedFileSet = patchFileList.stream().filter(Objects::nonNull).map(Patch::getFixedFile)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		Set<String> allDelteLine = patchFileList.stream().filter(Objects::nonNull).map(Patch::getDeleteLine)
				.flatMap(Collection::stream).collect(Collectors.toCollection(LinkedHashSet::new));

		log.info("RUNNING inital Program for Line ");

		PatchInitializationService methodTraceService = new PatchInitializationService();
		fixedFileSet.forEach(fixedFile -> FileIO.backUpFile(fixedFile, fixedFile + ".bak"));
		String writeFile = Constant.Record + subject.getName() + "/" + subject.getId() + "/"
				+ Constant.INSTRUMENT_LINE_DIR + Constant.INSTRUMENT_FIXED_SEPARATORINIT
				+ Constant.INSTRUMENT_LINE_SEPARATORINIT;
		if (new File(writeFile).exists()) {
			new File(writeFile).delete();
		}
		methodTraceService.instrumentLineDiff(fixedFileMap, writeFile);
		if (methodTraceService.Compile(subject)) {
			methodTraceService.runFailTest(subject);
		}
		// deleteFixedLine(writeFile, allDelteLine);

	}

	private void deleteFixedLine(String writeFile, Set<String> allDelteLine) {

		String content = FileIO.readFileToString(writeFile);
		StringBuilder result = new StringBuilder();
		for (String line : content.split("\n")) {
			String className = line.split("#")[0] + ".java";
			Integer lineNum = Integer.valueOf(line.split("#")[line.split("#").length - 1]);
			String tmpLine = className + "#" + lineNum;
			boolean delete = false;
			for (String delteLine : allDelteLine) {
				if (delteLine.contains(tmpLine)) {
					delete = true;
					break;
				}
			}
			if (delete) {
				continue;
			}
			result.append(line).append("\n");

		}
		FileIO.writeStringToFile(writeFile, result.toString(), false);
	}

	public void mainProcessForState() {
		List<Patch> patchFileList = subject.getPatchList();
		Set<String> fixedFileSet = patchFileList.stream().filter(Objects::nonNull).map(Patch::getFixedFile)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		log.info("RUNNING fixed Program for State ");

		PatchInitializationService methodTraceService = new PatchInitializationService();

		fixedFileSet.forEach(fixedFile -> FileIO.backUpFile(fixedFile, fixedFile + ".bak"));
		String writeFile = Constant.Record + subject.getName() + "/" + subject.getId() + "/"
				+ Constant.INSTRUMENT_STATE_DIR + Constant.INSTRUMENT_FIXED_SEPARATORINIT
				+ Constant.INSTRUMENT_STATE_SEPARATORINIT;
		if (new File(writeFile).exists()) {
			new File(writeFile).delete();
		}
		methodTraceService.instrumentVarCollection(fixedFileMap, writeFile);
		if (methodTraceService.Compile(subject)) {
			methodTraceService.runFailTest(subject);
		} else {
			FileIO.writeStringToFile(Constant.HOME + "/fixedstate.log", subject.toString() + " need to check!", true);
		}
		fixedFileSet.forEach(fixedFile -> FileIO.restoreFile(fixedFile, fixedFile + ".bak"));
	}

	public static void main(String[] args) {
		String project = "Chart";
		int Start = 13;
		int end = 13;
		List<Integer> idlist = Arrays.asList(13, 15, 21, 25, 26);
		// chart (13, 15, 21, 25, 26)
		// lang 53
		// math (2,8,28,49,81,84,87,88,95,97,105)
		for (int i = Start; i <= end; i++) {
			if (!idlist.contains(i)) {
				continue;
			}
			Project subject = new Project(project, i);
			if (subject.initPatchListByPath(Constant.AllPatchPath)) {
				log.info("Process " + subject.toString());
				List<Patch> patchList = subject.getPatchList();
				TraceFixedMain tarceFixedMain = new TraceFixedMain(subject);
				PatchInfo.obainAllMethod(patchList);
				tarceFixedMain.initModifiedMethodMap(patchList);
				// tarceFixedMain.mainProcessForLine();
				tarceFixedMain.mainProcessForState();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	}

}
