package patchfilter.model.script;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import patchfilter.model.util.FileIO;

public class GraspPatches {

	static String infoPath = "D:\\Graduation\\Dataset\\patches\\DefectRepairing-master\\tool\\patches\\INFO";
	static String patchPath = "D:\\Graduation\\Dataset\\patches\\patches\\";

	public static void getPatchInfo() {
		StringBuilder totalInfo = new StringBuilder();
		totalInfo.append("{");
		for (File infoJson : new File(infoPath).listFiles()) {
			String info = FileIO.readFileToString(infoJson);
			System.out.println(infoJson.getName() + " " + info.toString());
			totalInfo.append(info.toString()).append(",");

			Map<String, String> infoMap = json2Map(info);
			String correctness = infoMap.get("correctness");
			String project = infoMap.get("project");
			String bug_id = infoMap.get("bug_id");
			String tool = infoMap.get("tool");
			String ID = infoMap.get("ID");

			if (project.equals("Closure") || project.equals("Mockito") || project.equals("Time")) {
				continue;
			}
			if (correctness.equals("Unknown")) {
				continue;
			}

			File srcPatch = new File("D:\\Graduation\\Dataset\\patches\\DefectRepairing-master\\tool\\patches\\"
					+ infoJson.getName().split("\\.")[0]);
			String[] srcPatchContent = FileIO.readFileToString(srcPatch).split("\n");
			StringBuilder sb = new StringBuilder();
			for (String line : srcPatchContent) {
				if (line.startsWith("diff")) {
					continue;
				}
				sb.append(line).append("\n");
			}
			String targetPatchPath = patchPath + project + "\\" + bug_id + "\\" + tool + "-" + ID;
			FileIO.writeStringToFile(targetPatchPath, sb.toString());

		}
		totalInfo.deleteCharAt(totalInfo.length() - 1).append("}");
		FileIO.writeStringToFile(patchPath + "info.json", totalInfo.toString());
	}

	public static Map<String, String> json2Map(String str) {
		Map<String, String> map = new Gson().fromJson(str, new TypeToken<HashMap<String, String>>() {
		}.getType());
		return map;
	}

	public static void identiyCorrectness() {
		Map<String, List<String>> correctnessMap = new HashMap<>();
		for (File infoJson : new File(infoPath).listFiles()) {
			String info = FileIO.readFileToString(infoJson);
			// System.out.println(infoJson.getName() + " " + info.toString());

			Map<String, String> infoMap = json2Map(info);
			String correctness = infoMap.get("correctness");
			String project = infoMap.get("project");
			String bug_id = infoMap.get("bug_id");
			String tool = infoMap.get("tool");
			String ID = infoMap.get("ID");

			if (project.equals("Closure") || project.equals("Mockito") || project.equals("Time")) {
				continue;
			}
			if (correctness.equals("Unknown")) {
				continue;
			}

			if (!correctnessMap.containsKey(correctness)) {
				correctnessMap.put(correctness, new ArrayList<String>());
			}
			correctnessMap.get(correctness).add(project + "-" + bug_id + "-" + tool + "-" + ID);
		}
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, List<String>> entry : correctnessMap.entrySet()) {
			String correctness = entry.getKey();
			System.out.print(correctness + ": ");
			sb.append(correctness).append(": ");
			List<String> list = entry.getValue();
			for (String patch : list) {
				System.out.print(patch + ", ");
				sb.append(patch).append(", ");
			}
			System.out.println();
			sb.append("\n");
		}
		FileIO.writeStringToFile(patchPath + "correctnessInfo.txt", sb.toString());
	}

	public static void statistics() {
		int count = 0;
		int C = 0, IC = 0;
		for (File infoJson : new File(infoPath).listFiles()) {
			String info = FileIO.readFileToString(infoJson);
			// System.out.println(infoJson.getName() + " " + info.toString());

			Map<String, String> infoMap = json2Map(info);
			String correctness = infoMap.get("correctness");
			String project = infoMap.get("project");
			String bug_id = infoMap.get("bug_id");
			String tool = infoMap.get("tool");
			String ID = infoMap.get("ID");
			if (project.equals("Chart")) {
				if (correctness.equals("Unknown")) {
					continue;
				}
				count++;
				if (correctness.equals("Correct")) {
					C++;
				} else if (correctness.equals("Incorrect")) {
					IC++;
				}
			}

		}
		System.out.println("total: " + count + " IC: " + IC + " C: " + C);
	}

	public static void statistics2() {
		int id = 1;
		for (; id <= 106; id++) {
			String bugIdPath = patchPath + "Math" + "\\" + id;
			File file = new File(bugIdPath);
			if (file.exists()) {
				System.out.println(file.getName() + ": " + file.listFiles().length);
			}
		}
	}

	public static void main(String[] args) {
		// getPatchInfo();
		// identiyCorrectness();
		// statistics2();
	}

}
