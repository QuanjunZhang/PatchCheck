package patchfilter.model.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import patchfilter.configuration.Constant;
import patchfilter.model.entity.Patch;
import patchfilter.model.entity.Project;
import patchfilter.model.run.Runner;
import patchfilter.model.util.FileIO;

@Slf4j
public class InstallPatch4GenService {

	private Patch patch;

	public InstallPatch4GenService() {
	}

	public InstallPatch4GenService(Patch patch) {
		this.patch = patch;
	}

	public void mainProcess() {
		System.out.println("installing patch " + patch.getAliaName());
		if (patch.patchToFile()) {// Linux only
			System.out.println("patch " + patch.getAliaName() + " succeed!\nnow compiling subject");
			compile(patch.getSubject());
		}
	}

	public boolean installPatch() {
		File fixedFile = new File(patch.getFixedFile());
		if (!fixedFile.exists()) {
			return false;
		}
		int lineNo = 0;
		StringBuffer stringBuffer = new StringBuffer();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fixedFile), "utf-8"));
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				lineNo++;
				if (lineNo > patch.getStartLine() && lineNo <= patch.getStartLine() + patch.getLineRange() - 1) {
					continue;
				}
				if (lineNo == patch.getStartLine()) {
					stringBuffer.append(patch.getFixedContent());
				} else {
					stringBuffer.append(line + "\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileIO.backUpFile(patch.getFixedFile(), patch.getFixedFile() + ".bak");
		if (!FileIO.writeStringToFile(fixedFile, stringBuffer.toString())) {
			return false;
		}
		return true;
	}

	public boolean compile(Project subject) {
		subject.deleteTarget();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		List<String> message = Runner.compileSubjectRs(subject);
		for (int i = 0; i < message.size(); i++) {
			System.out.println(message.get(i));
			if (message.get(i).contains(Constant.ANT_BUILD_FAILED)) {
				log.error(subject.toString() + " Compile Failed! ");
				System.out.println(subject.toString() + " Compile Failed! ");
				return false;
			}
		}
		log.info(subject.toString() + " Compile Success! ");
		System.out.println(subject.toString() + " Compile Success! ");
		return true;
	}

	public static void main(String[] args) {
		Project subject = new Project("Math", 41);
		subject.initPatchListByPath(Constant.AllPatchPath);
		Patch patch = subject.getPatchList().get(0);
		System.out.println(patch.toString());
		InstallPatch4GenService patchInstallation = new InstallPatch4GenService(patch);
		patchInstallation.mainProcess();
	}

}
