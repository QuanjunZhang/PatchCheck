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

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryTraceService {
	private Project subject;
	private List<LineInfo> processList = new ArrayList<LineInfo>();
	private List<Patch> candidatePatchFiles = new ArrayList<Patch>();

	public QueryTraceService(Project subject, List<LineInfo> traceLinelist) {
		this.subject = subject;
		this.processList.addAll(traceLinelist);
		this.candidatePatchFiles.addAll(subject.getPatchList());
	}

	public void processAfterRightTrace(LineInfo currentLine) {
		Set<String> currentPatchNameSet = currentLine.getPatchList().stream().filter(Objects::nonNull)
				.map(Patch::getPatchName).collect(Collectors.toCollection(LinkedHashSet::new));
		candidatePatchFiles = candidatePatchFiles.stream().filter(Objects::nonNull)
				.filter(patchFile -> currentPatchNameSet.contains(patchFile.getPatchName()))
				.collect(Collectors.toList());
		// candidatePatchFiles.addAll(currentLine.getPatchList());
		currentLine.setStateType(StateType.YES);
		String currents = currentLine.getPatchList().stream().filter(Objects::nonNull).map(Patch::getPatchName)
				.sorted().collect(Collectors.joining());
		for (LineInfo lineInfo : processList) {
			String tmpPatches = lineInfo.getPatchList().stream().filter(Objects::nonNull).map(Patch::getPatchName)
					.sorted().collect(Collectors.joining());
			if (tmpPatches.equals(currents)) {
				lineInfo.setStateType(StateType.YES);
			}
		}
	}

	public void processAfterWrongTrace(LineInfo currentLine) {
		Set<String> currentPatchNameSet = currentLine.getPatchList().stream().filter(Objects::nonNull)
				.map(Patch::getPatchName).collect(Collectors.toCollection(LinkedHashSet::new));
		candidatePatchFiles.removeIf(patchFile -> currentPatchNameSet.contains(patchFile.getPatchName()));
		currentLine.setStateType(StateType.NO);

		String currents = currentLine.getPatchList().stream().filter(Objects::nonNull).map(Patch::getPatchName)
				.sorted().collect(Collectors.joining());
		for (LineInfo lineInfo : processList) {
			String tmpPatches = lineInfo.getPatchList().stream().filter(Objects::nonNull).map(Patch::getPatchName)
					.sorted().collect(Collectors.joining());
			if (tmpPatches.equals(currents)) {
				lineInfo.setStateType(StateType.NO);
			}
		}
	}

	public void updateCandidates(List<Patch> patchFiles) {
		this.candidatePatchFiles.clear();
		this.candidatePatchFiles.addAll(patchFiles);
	}

	public void updateListByCandidates() {
		Set<String> currentPatchSet = candidatePatchFiles.stream().filter(Objects::nonNull).map(Patch::getPatchName)
				.collect(Collectors.toSet());

		for (LineInfo lineInfo : processList) {
			List<Patch> modifyPatchList = lineInfo.getPatchList().stream().filter(Objects::nonNull)
					.filter(patchFile -> currentPatchSet.contains(patchFile.getPatchName()))
					.collect(Collectors.toCollection(LinkedList::new));
			lineInfo.setPatchList(modifyPatchList);
		}
		List<LineInfo> tmpList = processList.stream().filter(Objects::nonNull)
				.filter(lineInfo -> (lineInfo.getPatchList().size() > 0))
				.collect(Collectors.toCollection(LinkedList::new));
		processList = tmpList;
	}

}
