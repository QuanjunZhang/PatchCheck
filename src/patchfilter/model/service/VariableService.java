package patchfilter.model.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import patchfilter.model.entity.LineInfo;
import patchfilter.model.entity.Patch;
import patchfilter.model.entity.Project;
import patchfilter.model.util.StateType;
import patchfilter.util.LocationInfo;
import patchfilter.util.VariableInfo;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VariableService {

	private Project subject;
	private List<VariableInfo> processList = new ArrayList<VariableInfo>();
	private List<Patch> candidatePatchFiles = new ArrayList<Patch>();

	public VariableService(Project subject, List<VariableInfo> varLineList) {
		this.subject = subject;
		this.processList.addAll(varLineList);
		this.candidatePatchFiles.addAll(subject.getPatchList());
	}

	public void updateCandidates(List<Patch> currentPatches) {
		this.candidatePatchFiles.clear();
		this.candidatePatchFiles.addAll(currentPatches);
	}

	public void updateListByCandidates() {
		Set<String> currentPatchSet = candidatePatchFiles.stream().filter(Objects::nonNull).map(Patch::getPatchName)
				.collect(Collectors.toSet());
		for (VariableInfo lineInfo : processList) {
			List<Patch> modifyPatchList = lineInfo.getPatchFiles().stream().filter(Objects::nonNull)
					.filter(patchFile -> currentPatchSet.contains(patchFile.getPatchName()))
					.collect(Collectors.toCollection(LinkedList::new));
			lineInfo.setPatchFiles(modifyPatchList);
		}
		List<VariableInfo> tmpList = processList.stream().filter(Objects::nonNull)
				// .filter(lineInfo -> !currentLineInfo.equals(lineInfo))
				// .filter(lineInfo -> !compareTwoLine(lineInfo, currentLineInfo))
				.filter(lineInfo -> (lineInfo.getPatchFiles().size() > 0))
				.filter(lineInfo -> lineInfo.getType().equals(StateType.UNCLEAR))
				.collect(Collectors.toCollection(LinkedList::new));
		processList = tmpList;
	}

	// public boolean isTerminate() {
	// boolean result = false;
	// List<VariableInfo> testLineInfos =
	// processList.stream().filter(Objects::nonNull)
	// .filter(variableLine ->
	// variableLine.getStateType().equals(StateType.UNCLEAR))
	// .filter(variableLine -> variableLine.getPatchFiles().size() >
	// 0).collect(Collectors.toList());
	// if (testLineInfos.size() == 0 || candidatePatchFiles.size() == 1) {
	// return true;
	// }
	// return result;
	// }
	//
	// public VariableInfo pickOne() {
	// return processList.get(0);
	// }
}
