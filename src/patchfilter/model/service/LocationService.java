package patchfilter.model.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import patchfilter.configuration.Constant;
import patchfilter.model.entity.LineInfo;
import patchfilter.model.entity.Method;
import patchfilter.model.entity.Patch;
import patchfilter.model.entity.Project;
import patchfilter.model.util.FileIO;
import patchfilter.model.util.StateType;
import patchfilter.util.LocationInfo;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class LocationService {

	private Project subject;
	private List<LocationInfo> processList = new ArrayList<LocationInfo>();
	private List<Patch> candidatePatchList = new ArrayList<Patch>();

	public LocationService(Project subject, List<LocationInfo> locationList) {
		this.subject = subject;
		this.processList.addAll(locationList);
		candidatePatchList.addAll(subject.getPatchList());
	}

	public void updateCandidates(List<Patch> patchFiles) {
		this.candidatePatchList.clear();
		this.candidatePatchList.addAll(patchFiles);
	}

	public void updateListByCandidates() {
		Set<String> currentPatchSet = candidatePatchList.stream().filter(Objects::nonNull).map(Patch::getPatchName)
				.collect(Collectors.toSet());

		for (LocationInfo lineInfo : processList) {
			List<Patch> modifyPatchList = lineInfo.getPatchList().stream().filter(Objects::nonNull)
					.filter(patchFile -> currentPatchSet.contains(patchFile.getPatchName()))
					.collect(Collectors.toCollection(LinkedList::new));
			lineInfo.setPatchList(modifyPatchList);
		}
		List<LocationInfo> tmpList = processList.stream().filter(Objects::nonNull)
				// .filter(lineInfo -> !currentLineInfo.equals(lineInfo))
				// .filter(lineInfo -> !compareTwoLine(lineInfo, currentLineInfo))
				.filter(lineInfo -> (lineInfo.getPatchList().size() > 0))
				.collect(Collectors.toCollection(LinkedList::new));
		processList = tmpList;
	}

}
