package patchfilter.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import patchfilter.configuration.Constant;
import patchfilter.model.entity.LineInfo;
import patchfilter.model.entity.Method;
import patchfilter.model.entity.Patch;
import patchfilter.model.service.LocationService;
import patchfilter.model.service.VariableService;
import patchfilter.model.entity.Project;
import patchfilter.model.util.BuildFilePath;
import patchfilter.model.util.FileIO;
import patchfilter.model.util.StateType;
import patchfilter.util.LocationInfo;
import patchfilter.util.TestInfo;
import patchfilter.util.VariableInfo;

@Data
@Slf4j
public class ProjectController {

	private Project project;

	private List<TestInfo> testInfoList = new ArrayList<TestInfo>();
	private List<LocationInfo> locationInfoList = new ArrayList<LocationInfo>();
	private List<Patch> currentPatchList = new ArrayList<Patch>();

	private LocationService locationService;

	public ProjectController(Project project) {
		this.project = project;
	}

	public void initSet() {
		clear();
		project.initPatchListByPath(Constant.AllPatchPath);
		currentPatchList.addAll(project.getPatchList());
		this.initTestInfoList();
		this.initLocationInfoList();
		locationService = new LocationService(project, locationInfoList);
	}

	private void clear() {
		testInfoList.clear();
		locationInfoList.clear();
		currentPatchList.clear();
	}

	private void initTestInfoList() {
		testInfoList.clear();
		for (String failingTest : project.getFailedTestList()) {
			TestInfo testLine = new TestInfo(failingTest, 1, currentPatchList.size());
			testInfoList.add(testLine);
		}
	}

	private void initLocationInfoList() {
		locationInfoList.clear();
		for (Patch patch : project.getPatchList()) {
			String patchMethodFile = Constant.CACHE + patch.getSubject().getName() + "/" + patch.getSubject().getId()
					+ "/" + patch.getPatchName() + "_method";
			Method method = JSONObject.parseObject(FileIO.readFileToString(patchMethodFile), Method.class);
			patch.setModifiedMethod(method.getMethodName());
		}
		Map<String, List<Patch>> methodPatchMap = project.getPatchList().stream().filter(Objects::nonNull)
				.collect(Collectors.groupingBy(Patch::getModifiedMethod, Collectors.toCollection(LinkedList::new)));
		for (Entry<String, List<Patch>> entry : methodPatchMap.entrySet()) {
			LocationInfo locationLine = new LocationInfo(entry.getKey(), StateType.UNCLEAR, entry.getValue());
			locationInfoList.add(locationLine);
			System.out.println(locationLine.toString());
		}
	}

	public List<TestInfo> getFailTestList() {
		return testInfoList;
	}

	public void restoreList() {
		log.info("Restore Answer!");
		this.initSet();
	}

	public void updateTestInfoList() {
		this.testInfoList.clear();
		for (String failingTest : project.getFailedTestList()) {
			TestInfo testLine = new TestInfo(failingTest, 1, currentPatchList.size());
			testInfoList.add(testLine);
		}
	}

	public void updateCurrentPatches(List<Patch> patchFiles) {
		this.currentPatchList.clear();
		this.currentPatchList.addAll(patchFiles);
	}

	public void deleteCandidatePatches(Patch patchFile) {
		currentPatchList = currentPatchList.stream().filter(Objects::nonNull)
				.filter(patch -> !patchFile.getPatchName().equalsIgnoreCase(patch.getPatchName()))
				.collect(Collectors.toList());
		updateLocationByCandidates();
	}

	public void updateLocationByCandidates() {
		locationService.updateCandidates(this.currentPatchList);
		locationService.updateListByCandidates();
		this.locationInfoList.clear();
		this.locationInfoList.addAll(locationService.getProcessList());
	}

}
