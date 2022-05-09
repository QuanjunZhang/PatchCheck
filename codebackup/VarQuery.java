package patchfilter.model.experiment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;
import patchfilter.configuration.Constant;
import patchfilter.controller.VariableCollection;
import patchfilter.model.entity.CorrectPatch;
import patchfilter.model.entity.LineInfo;
import patchfilter.model.entity.Patch;
import patchfilter.model.entity.Project;
import patchfilter.model.service.QueryService;
import patchfilter.model.service.VariableService;
import patchfilter.model.util.FileIO;
import patchfilter.util.VariableInfo;

@Slf4j
public class VarQuery {

	private Project subject;

	public VarQuery(Project subject) {
		this.subject = subject;
	}

	private boolean containsCorretPatch() {
		String correctPatchString = FileIO.readFileToString(Constant.CorrectPatchInfo + "patchinfo.json");
		List<CorrectPatch> correctPatchList = JSONObject.parseArray(correctPatchString, CorrectPatch.class);
		boolean contains = false;
		List<Patch> patchFileList = subject.getPatchList();
		for (Patch patchFile : patchFileList) {
			CorrectPatch correctPatch = new CorrectPatch(patchFile.getPatchName(), subject.getName(),
					String.valueOf(subject.getId()));
			if (correctPatchList.contains(correctPatch)) {
				patchFile.setCorrectness(true);
				contains = true;
			}
		}
		return contains;
	}

	public void queryProcess() {
		StringBuilder resultBuilder = new StringBuilder();
		String resultPath = "./varquery.csv";
		resultBuilder.append(subject.getName() + subject.getId()).append(",").append(subject.getPatchList().size())
				.append(",");

		VariableCollection variableCollection = new VariableCollection(subject);
		variableCollection.mainProcess();
		List<VariableInfo> variableLines = variableCollection.getVariableLines();
		HashMap<String, String>  correctHashMap = variableCollection.getCorrectKeyValue();

		VariableService queryService = new VariableService(subject, variableLines);
		int queryNumber = 0;

		while (!queryService.isTerminate()) {

			queryNumber++;
			VariableInfo currentLine = queryService.pickOne();
			log.info("----------- QueryNumber: " + queryNumber);
			log.info("Pick line info {}", currentLine.getVarName(), currentLine.getValue(),
					currentLine.getPatchFiles().toString());
			boolean isTrue = getAnswer(currentLine, correctHashMap);

			log.info("Answer: " + isTrue);
			if (currentLine != null && !isTrue) {
				queryService.processAfterWrongTrace(currentLine);
			} else if (currentLine != null && isTrue) {
				queryService.processAfterRightTrace(currentLine);
			}
			log.info("ProcessList Size: " + queryService.getProcessList().size());
			log.info("PatchList Size: " + queryService.getCandidatePatchFiles().size());
			log.info("QueryNumber: " + queryNumber);
			resultBuilder.append(queryService.getCandidatePatchFiles().size()).append(",");

		}
		resultBuilder.append("\n");
		FileIO.writeStringToFile(resultPath, resultBuilder.toString(), true);
	}
	private boolean getAnswer(VariableInfo currentLine, HashMap<String, String> correctHashMap) {
		boolean isTrue = false;
		if (containsCorretPatch()) {
			for (Patch patchFile : currentLine.getPatchFiles()) {
				if (patchFile.isCorrectness()) {
					isTrue = true;
					break;
				}
			}
		}else {
			String key = currentLine.getVarName();
			if(currentLine.getValue().equals(correctHashMap.get(key))) {
				isTrue = true;
			}else {
				isTrue = false;
			}
		}
		return isTrue;
	}

	public static void main(String[] args) {
		String project = "Lang";
		int Start = 1;
		int end = 65;
		for (int i = Start; i <= end; i++) {
			Project subject = new Project(project, i);
			if (subject.initPatchListByPath(Constant.AllPatchPath)) {
				log.info("Process " + subject.toString());
				VarQuery varQuery = new VarQuery(subject);
				varQuery.queryProcess();
			}
		}
	}

}
