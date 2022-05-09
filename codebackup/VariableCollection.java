package patchfilter.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import patchfilter.configuration.Constant;
import patchfilter.model.entity.Patch;
import patchfilter.model.entity.Project;
import patchfilter.model.util.BuildFilePath;
import patchfilter.model.util.FastJsonParseUtil;
import patchfilter.model.util.FileIO;
import patchfilter.model.util.StateType;
import patchfilter.util.VariableInfo;
import sun.security.ssl.KerberosClientKeyExchange;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariableCollection {

	private Project subject;

	// private Map<String, List<PatchFile>> varPatchMap = new HashMap<String,
	// List<PatchFile>>();
	private Set<String> filterout = new HashSet<>(Arrays.asList("clock", "timestamp", "pData"));
	private List<VariableInfo> variableLines = new LinkedList<VariableInfo>();
	private HashMap<String, String> correctKeyValue = new LinkedHashMap<String, String>();

	public VariableCollection(Project subject) {
		this.subject = subject;
	}

	public void mainProcess() {
		List<Patch> patchFiles = subject.getPatchList();
		// int patchNumber = patchFiles.size();
		Map<String, List<JSONObject>> variableMap = new LinkedHashMap<>();
		for (Patch patchFile : patchFiles) {
			String varPath = BuildFilePath.tmpState(patchFile.getPatchName(), subject);
			System.out.println(varPath);
			String variableFileContent = FileIO.readFileToString(varPath);
			List<JSONObject> variableJsonList = Arrays.stream(variableFileContent.split("\n"))
					.filter(StringUtils::isNotBlank)
					.filter(line -> !(line.endsWith("START#0") || line.endsWith("END#0"))).map(variableLine -> {
						String[] array = variableLine.split(": \\{", 2);
						if (Objects.nonNull(array) && array.length >= 2) {
							array[1] = "{" + array[1];
							String value = array[1].replaceAll("\\{null\\}", "\\{\\}").replaceAll("\\\"<null>\\\"",
									"null");
							JSONObject resultJsonObject = FastJsonParseUtil.jsonFormatter(value, array[0]);
							resultJsonObject.put("patchName", patchFile);
							return resultJsonObject;
						}
						return null;
					}).filter(Objects::nonNull).collect(Collectors.toList());
			variableMap.put(patchFile.getPatchName(), variableJsonList);
		}
		Integer jsonSize = variableMap.entrySet().stream().filter(entry -> Objects.nonNull(entry.getValue()))
				.mapToInt(entry -> entry.getValue().size()).min().orElse(0);
		if (jsonSize <= 0) {
			return;
		}
		for (int i = 0; i < jsonSize; i++) {
			int index = i;
			List<JSONObject> currentLineVariableList = patchFiles.stream().filter(Objects::nonNull)
					.map(patchFile -> variableMap.get(patchFile.getPatchName()).get(index)).filter(Objects::nonNull)
					.collect(Collectors.toList());
			if (CollectionUtils.isEmpty(currentLineVariableList)) {
				continue;
			}
			Set<String> keySet = currentLineVariableList.stream()
					.flatMap(variableJson -> variableJson.keySet().stream()).filter(StringUtils::isNotEmpty)
					.filter(key -> {
						for (String str : filterout) {
							if (key.endsWith(str)) {
								return false;
							}
						}
						return true;
					}).collect(Collectors.toSet());
			Set<String> differentKey = new LinkedHashSet<String>();
			for (String key : keySet) {
				if (key.equals("patchName")) {
					continue;
				}
				Map<String, List<Patch>> valuePatchMap = currentLineVariableList.stream()
						.filter(json -> Objects.nonNull(json.get(key)))
						.collect(Collectors.groupingBy(variableJson -> variableJson.getString(key),
								Collectors.mapping(variableJson -> variableJson.getObject("patchName", Patch.class),
										Collectors.toList())));
				if (valuePatchMap.size() <= 1) {
					continue;
				} else {
					for (Entry<String, List<Patch>> entry : valuePatchMap.entrySet()) {
						VariableInfo variableLine = new VariableInfo(key, entry.getKey(),"", ""/*StateType.UNCLEAR*/,
								entry.getValue());
						variableLines.add(variableLine);
						differentKey.add(key);
					}
				}
			}
			if (variableLines.size() > 0) {
				log.info("Differences in line: " + index);
				System.out.println("Differences in line: " + index);
				String fixedContent = FileIO.readFileToString(BuildFilePath.tmpState("fixed", subject));
				if (!fixedContent.equals("")) {
					// collectCorrect(fixedContent, index, differentKey);
				}
				break;
			}
		}
		if (variableLines.size() == 0) {
			System.out.println("All lines are same ");
			log.info("All lines are same ");
		}
	}

	public static void main(String[] args) {
		Project subject = new Project("Math", 105);
		subject.initPatchListByPath(Constant.AllPatchPath);
		VariableCollection variableCollection = new VariableCollection(subject);
		variableCollection.mainProcess();
		List<VariableInfo> variableLines = variableCollection.getVariableLines();
		System.out.println(variableLines.size());
		for (VariableInfo line : variableLines) {
			System.out.println(line.toString());
		}
		// HashMap<String, String> correctKeyValue =
		// variableCollection.getCorrectKeyValue();
		// System.out.println(correctKeyValue.size());
		// for (Map.Entry<String, String> entry : correctKeyValue.entrySet()) {
		// System.out.println("key = " + entry.getKey() + "value = " +
		// entry.getValue());
		// }
	}

}
