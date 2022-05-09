package patchfilter.model.initialization;

import lombok.extern.slf4j.Slf4j;
import patchfilter.configuration.Constant;
import patchfilter.model.entity.Patch;
import patchfilter.model.entity.Project;
import patchfilter.model.service.PatchInitializationService;
import patchfilter.model.util.BuildFilePath;
import patchfilter.model.util.FileIO;
import patchfilter.model.util.PatchInfo;
import patchfilter.model.service.MapService;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class PatchInitialization {

	public static String traceLogFile = Constant.LOG_FILE + "ObtainTrace.log";

	public void initialization4Variable(Project project) {
		List<Patch> patchFileList = project.getPatchList();
		Set<String> fixedFileSet = patchFileList.stream().filter(Objects::nonNull).map(Patch::getFixedFile)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		log.info("RUNNING inital Program for State ");
		System.out.println("==================================================================================");

		// fixedFileSet.forEach(fixedFile -> FileIO.backUpFile(fixedFile, fixedFile +
		// ".bak"));
		// String writeFile = Constant.Record + project.getName() + "/" +
		// project.getId() + "/"
		// + Constant.INSTRUMENT_STATE_DIR + Constant.INSTRUMENT_INIT_SEPARATORINIT
		// + Constant.INSTRUMENT_STATE_SEPARATORINIT;
		// if (new File(writeFile).exists()) {
		// new File(writeFile).delete();
		// }
		// PatchInitializationService methodTraceService = new
		// PatchInitializationService();
		// methodTraceService.instrumentVarCollection(fixedFileMap, writeFile);
		// if (methodTraceService.Compile(project)) {
		// methodTraceService.runFailTest(project);
		// }
		// fixedFileSet.forEach(fixedFile -> FileIO.restoreFile(fixedFile, fixedFile +
		// ".bak"));

		for (Patch patchFile : patchFileList) {
			System.out.println("------------------------------------------------------------------------------");
			log.info("RUNNING Patch " + patchFile.getPatchName());
			System.out.println("RUNNING Patch " + patchFile.getPatchName());
			String fixedFile = patchFile.getFixedFile();

			// patch 前插桩
			FileIO.backUpFile(fixedFile, fixedFile + ".bak");
			String writeFile = Constant.Record + project.getName() + "/" + project.getId() + "/"
					+ Constant.INSTRUMENT_STATE_DIR + patchFile.getPatchName() + "_"
					+ Constant.INSTRUMENT_INIT_SEPARATORINIT + Constant.INSTRUMENT_STATE_SEPARATORINIT;
			if (new File(writeFile).exists()) {
				new File(writeFile).delete();
			}
			PatchInitializationService methodTraceService = new PatchInitializationService();
			methodTraceService.instrumentVarCollection(patchFile, writeFile);
			if (methodTraceService.Compile(project)) {
				methodTraceService.runFailTest(project);
			}
			FileIO.restoreFile(fixedFile, fixedFile + ".bak");

			// patch 后插桩
			FileIO.backUpFile(fixedFile, fixedFile + ".bak");
			if (patchFile.patchToFile()) {
				log.info("Patch " + patchFile.getPatchName() + " Patches File Success.");
				writeFile = Constant.Record + project.getName() + "/" + project.getId() + "/"
						+ Constant.INSTRUMENT_STATE_DIR + patchFile.getPatchName()
						+ Constant.INSTRUMENT_STATE_SEPARATORINIT;
				if (new File(writeFile).exists()) {
					new File(writeFile).delete();
				}
				methodTraceService.instrumentVarCollection(patchFile, writeFile);
				if (methodTraceService.Compile(project)) {
					methodTraceService.runFailTest(project);
				}
			} else {
				log.error("Patch " + patchFile.getPatchName() + " Patches File Fail.");
				FileIO.writeStringToLog(traceLogFile, "Patch " + patchFile.getPatchName() + " Patches File Fail.");
			}
			FileIO.restoreFile(fixedFile, fixedFile + ".bak");
		}
	}

	public void initialization4Line(Project project) {
		List<Patch> patchFileList = project.getPatchList();
		Set<String> fixedFileSet = patchFileList.stream().filter(Objects::nonNull).map(Patch::getFixedFile)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		Set<String> allDelteLine = patchFileList.stream().filter(Objects::nonNull).map(Patch::getDeleteLine)
				.flatMap(Collection::stream).collect(Collectors.toCollection(LinkedHashSet::new));

		log.info("RUNNING inital Program for Line ");
		System.out.println("==================================================================================\n");

		// fixedFileSet.forEach(fixedFile -> FileIO.backUpFile(fixedFile, fixedFile +
		// ".bak"));
		// String writeFile = Constant.Record + project.getName() + "/" +
		// project.getId() + "/"
		// + Constant.INSTRUMENT_LINE_DIR + Constant.INSTRUMENT_INIT_SEPARATORINIT
		// + Constant.INSTRUMENT_LINE_SEPARATORINIT;
		// if (new File(writeFile).exists()) {
		// new File(writeFile).delete();
		// }
		// PatchInitializationService methodTraceService = new
		// PatchInitializationService();
		// methodTraceService.instrumentLineDiff(fixedFileMap, writeFile);
		// if (methodTraceService.Compile(project)) {
		// methodTraceService.runFailTest(project);
		// }
		// fixedFileSet.forEach(fixedFile -> FileIO.restoreFile(fixedFile, fixedFile +
		// ".bak"));

		for (Patch patchFile : patchFileList) {
			System.out.println("------------------------------------------------------------------------------");
			log.info("RUNNING Patch " + patchFile.getPatchName());
			System.out.println("RUNNING Patch " + patchFile.getPatchName());
			String fixedFile = patchFile.getFixedFile();

			// patch 前插桩
			FileIO.backUpFile(fixedFile, fixedFile + ".bak");
			String writeFile = Constant.Record + project.getName() + "/" + project.getId() + "/"
					+ Constant.INSTRUMENT_LINE_DIR + patchFile.getPatchName() + "_"
					+ Constant.INSTRUMENT_INIT_SEPARATORINIT + Constant.INSTRUMENT_LINE_SEPARATORINIT;
			if (new File(writeFile).exists()) {
				new File(writeFile).delete();
			}
			PatchInitializationService methodTraceService = new PatchInitializationService();
			methodTraceService.instrumentLineDiff(patchFile, writeFile);
			if (methodTraceService.Compile(project)) {
				methodTraceService.runFailTest(project);
			}
			FileIO.restoreFile(fixedFile, fixedFile + ".bak");

			// patch 后插桩
			FileIO.backUpFile(fixedFile, fixedFile + ".bak");
			if (patchFile.patchToFile()) {
				log.info("Patch " + patchFile.getPatchName() + " Patches File Success.");
				writeFile = Constant.Record + project.getName() + "/" + project.getId() + "/"
						+ Constant.INSTRUMENT_LINE_DIR + patchFile.getPatchName()
						+ Constant.INSTRUMENT_LINE_SEPARATORINIT;
				if (new File(writeFile).exists()) {
					new File(writeFile).delete();
				}
				methodTraceService.instrumentLineDiff(patchFile, writeFile);
				if (methodTraceService.Compile(project)) {
					methodTraceService.runFailTest(project);
				}
			} else {
				log.error("Patch " + patchFile.getPatchName() + " Patches File Fail.");
				FileIO.writeStringToLog(traceLogFile, "Patch " + patchFile.getPatchName() + " Patches File Fail.");
			}
			FileIO.restoreFile(fixedFile, fixedFile + ".bak");
		}
	}

	public static void main(String[] args) {
		String project = "Math";
		int start = 41;
		int end = 41;
		for (int i = start; i <= end; i++) {
			Project subject = new Project(project, i);
			if (subject.initPatchListByPath(Constant.AllPatchPath)) {
				log.info("Process " + subject.toString());
				FileIO.writeStringToLog(traceLogFile, "Process " + subject.toString());

				MethodInitialization methodInitialization = new MethodInitialization(subject);
				methodInitialization.MainProcess();
				methodInitialization.MainProcess4TestFile();

				List<Patch> patchList = subject.getPatchList();
				PatchInitialization patchVariationMain = new PatchInitialization();
				patchVariationMain.initialization4Variable(subject);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
