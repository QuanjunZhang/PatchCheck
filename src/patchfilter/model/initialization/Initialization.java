package patchfilter.model.initialization;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import patchfilter.configuration.Constant;
import patchfilter.model.entity.Patch;
import patchfilter.model.entity.Project;
import patchfilter.model.util.FileIO;

@Slf4j
public class Initialization {

	public static String traceLogFile = Constant.LOG_FILE + "ObtainTrace.log";

	public static void initialize(Project project) {
		System.out.println(project.toString());
		if (project.initPatchListByPath(Constant.AllPatchPath)) {
			log.info("Process " + project.toString());
			FileIO.writeStringToLog(traceLogFile, "Process " + project.toString());

			// init methods
			System.out.println("init methods for " + project.toString());
			MethodInitialization methodInitialization = new MethodInitialization(project);
			methodInitialization.MainProcess();
			methodInitialization.MainProcess4TestFile();

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			// init patches
			System.out.println("init patches for " + project.toString());
			List<Patch> patchList = project.getPatchList();
			PatchInitialization patchInitialization = new PatchInitialization();
			patchInitialization.initialization4Variable(project);
			patchInitialization.initialization4Line(project);

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			System.out.println("Process " + project.toString() + " done!!!");
		}
	}

	public static void main(String[] args) {
		Project subject = new Project("Math", 50);
		Initialization.initialize(subject);
	}

}
