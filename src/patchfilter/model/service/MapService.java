package patchfilter.model.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import patchfilter.configuration.Constant;
import patchfilter.model.entity.Patch;
import patchfilter.model.entity.Project;
import patchfilter.model.util.BuildFilePath;
import patchfilter.model.util.FileIO;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MapService {

	private Patch patchFile;

	public static String resultFile = Constant.LOG_FILE + "ObtainTrace.log";

	// 将插入补丁之后的trace映射在原代码上
	public boolean traceMap(Set<String> allDetelteLine) {
		log.info("Trace Correct Line");
		String lineInfoFileName = Constant.Record + patchFile.getSubject().getName() + "/" + patchFile.getSubject().getId()
				+ "/" + Constant.INSTRUMENT_LINE_DIR + patchFile.getPatchName()
				+ Constant.INSTRUMENT_LINE_SEPARATORINIT;
		String patchPath = patchFile.getPatchPath();
		String fixedFile = patchFile.getFixedFile();
		
		try {
			String lineContent = FileIO.readFileToString(lineInfoFileName);
			if (lineContent.equals("")) {
				log.error("Content of " + patchFile.getPatchName() + " Trace is Empty.");
				FileIO.writeStringToLog(resultFile, "Content of " + patchFile.getPatchName() + " Trace is Empty.");
				return false;
			}
			String mapTraceFile = lineInfoFileName + Constant.MAPTRACE;
			
			String formatFixedFileClass = fixedFile.replaceAll("\\/\\/", "").replaceAll("/", "\\.");
			if (formatFixedFileClass.startsWith(".")) {
				formatFixedFileClass = formatFixedFileClass.split("\\.", 2)[1];
			}
			String[] lineContentArray = lineContent.split("\n");
			int maxLine = 0;
			for (String line : lineContentArray) {
				String className = line.split("#")[0];
				if (className.contains("$")) {
					className = className.split("\\$")[0];
				}
				className = className + ".java";
				if (formatFixedFileClass.contains(className)) {
					Integer lineNum = Integer.valueOf(line.split("#")[line.split("#").length - 1]);
					maxLine = Math.max(maxLine, lineNum);
				}
			}
			int[] newIndex = new int[maxLine + 10];
			for (int i = 0; i <= maxLine; ++i) {
				newIndex[i] = 0;
			}
			
			LinkedHashSet<Integer> removedLineSet = new LinkedHashSet<Integer>();
			
			BufferedReader patchReader = new BufferedReader(new FileReader(patchPath));
			String patchContentLine = patchReader.readLine();
			int patchedLine = -1;
			while (patchContentLine != null) {
				if (patchContentLine.startsWith("---") || patchContentLine.startsWith("+++")) {
					patchContentLine = patchReader.readLine();
					continue;
				} else if (patchContentLine.startsWith("@@")) {
					patchedLine = Integer.valueOf(patchContentLine.split(",")[0].split("-")[1]);
				} else if (patchContentLine.startsWith("-") && (!patchContentLine.startsWith("---"))) {
					if (patchedLine <= maxLine) {
						newIndex[patchedLine] += 1;
					}
				} else if (patchContentLine.startsWith("+") && (!patchContentLine.startsWith("+++"))) {
					removedLineSet.add(patchedLine);
					patchedLine += 1;
					if (patchedLine <= maxLine) {
						newIndex[patchedLine] -= 1;
					}
				} else {
					patchedLine += 1;
				}
				patchContentLine = patchReader.readLine();
			}
			patchReader.close();
			for (int i = 1; i <= maxLine; i += 1) {
				newIndex[i] += newIndex[i - 1];
			}

			StringBuilder tmpLines = new StringBuilder();
			for (String line : lineContentArray) {
				String className = line.split("#")[0];
				if (className.contains("$")) {
					className = className.split("\\$")[0];
				}
				className = className + ".java";
				Integer lineNum = Integer.valueOf(line.split("#")[line.split("#").length - 1]);

				if (formatFixedFileClass.contains(className)) {
					if (removedLineSet.contains(lineNum)) {
						continue;
					} else {
						int newLineNumber = newIndex[lineNum] + lineNum;
						String tmpLine = className + "#" + newLineNumber;
						// 所有其它补丁内对修改前代码 进行修改的行不做考虑
						boolean shouldDelete = false;
						for (String deleteLine : allDetelteLine) {
							if (deleteLine.contains(tmpLine)) {
								shouldDelete = true;
								break;
							}
						}
						if (!shouldDelete) {
							tmpLines.append(line.split(lineNum + "")[0]).append(newIndex[lineNum] + lineNum)
									.append("\n");
						}
					}

				} else {
					String tmpLine = className + "#" + lineNum;
					// 所有其它补丁内对修改前代码 进行修改的行不做考虑
					boolean shouldDelete = false;
					for (String deleteLine : allDetelteLine) {
						if (deleteLine.contains(tmpLine)) {
							shouldDelete = true;
							break;
						}
					}
					if (!shouldDelete) {
						tmpLines.append(line).append("\n");
					}
				}
			}
			FileIO.writeStringToFile(mapTraceFile, tmpLines.toString(), false);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return true;
	}
	
	public static void generateMap(Project project) {
		List<Patch> patchFiles = project.getPatchList();
		Map<String, Set<String>> linePatchMap = new LinkedHashMap<>();// <line, patchlist>
		StringBuilder result = new StringBuilder();
		int index = 0;
		boolean findDifference = false;
		while (!findDifference) {
			linePatchMap.clear();
			for (Patch patchFile : patchFiles) {
				System.out.println(patchFile.getPatchName()+":::::");
				String traceContent = FileIO
						.readFileToString(BuildFilePath.tmpMapTraceLine(patchFile.getPatchName(), project));
				if (traceContent.equals("")) {
					log.error(patchFile.getPatchName() + " trace is empty");
				}
				
				String splitLine = traceContent.split("\n")[0];
				String[] fbsArr = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|" };
				for (String key : fbsArr) {
					if (splitLine.contains(key)) {
						splitLine = splitLine.replace(key, "\\" + key);
					}
				}
				
				String patchSplitContent[] = traceContent.split(splitLine + "\n");
				if (index >= patchSplitContent.length) {
					log.error(patchFile.getPatchName() + " paragraph length is different!");
					continue;
				}
				for (String line : patchSplitContent[index].split("\n")) {
					if (line.contains("START#0") || line.equals("")) {
						continue;
					}
					if (linePatchMap.containsKey(line)) {
						linePatchMap.get(line).add(patchFile.getPatchName());
					} else {
						Set<String> patchSet = new LinkedHashSet<>();
						patchSet.add(patchFile.getPatchName());
						linePatchMap.put(line, patchSet);
					}
				}
			}

			if (linePatchMap.size() != 0) {
				for (Map.Entry entry : linePatchMap.entrySet()) {
					Set<String> patchSet = (Set<String>) entry.getValue();
					if (patchSet.size() < patchFiles.size()) {
						result.append(entry.getKey() + ":");
						for (String patchName : patchSet) {
							result.append(patchName).append("\t");
						}
						result.append("\n");
					}
				}
				if (!result.toString().equals("")) {
					findDifference = true;
				}
			}
			index++;
		}
		FileIO.writeStringToFile(BuildFilePath.tmpMapLine(project), result.toString(), false);
	}

	public static void main(String arg[]) {
		Project subject = new Project("Chart", 1);
		String PatchName = "ACS-Patch204";
		String path = Constant.AllPatchPath + "Math/85/" + PatchName;
		Patch patchFile = new Patch(subject, path);
		patchFile.parsePatch();

		MapService traceMapService = new MapService(patchFile);
		traceMapService.traceMap(new LinkedHashSet<>());
		
//		Project project = new Project("Math", 85);
//		if (project.initPatchListByPath(Constant.AllPatchPath)) {
//			generateMap(project);
//		}
	}

}
