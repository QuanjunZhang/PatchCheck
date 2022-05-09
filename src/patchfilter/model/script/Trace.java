package patchfilter.model.script;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import patchfilter.configuration.Constant;
import patchfilter.model.entity.Method;
import patchfilter.model.entity.Patch;
import patchfilter.model.entity.Project;
import patchfilter.model.util.BuildFilePath;
import patchfilter.model.util.FileIO;
import patchfilter.model.util.PatchInfo;

public class Trace {

	private static final String Cover = "Cover";
	private static final String NotCover = "NotCover";
	private static final String CommonCover = "CommonCover";

	public static void trace() {
		Patch patchFile = new Patch(new Project("Math", 85), Constant.AllPatchPath + "/Math/85/ACS-Patch204");
		patchFile.parsePatch();
		PatchInfo.obainAllMethod(Collections.singletonList(patchFile));

		String patchMethodFile = Constant.CACHE + patchFile.getSubject().getName() + "/"
				+ patchFile.getSubject().getId() + "/" + patchFile.getPatchName() + "_method";
		String initialFile = BuildFilePath.tmpLine("initial", patchFile.getSubject());
		String patchedFile = BuildFilePath.tmpMapTraceLine(patchFile.getPatchName(), patchFile.getSubject());
		// System.out.println("initialFile: " + initialFile + ", patchedFile: " +
		// patchedFile + "\n");
		Method method = JSONObject.parseObject(FileIO.readFileToString(patchMethodFile), Method.class);

		String methodStartLine = method.getMethodName() + " START#0";
		// System.out.println("methodStartLine: " + methodStartLine + "\n");
		List<String> initialFileTraceList = Arrays.asList(FileIO.readFileToString(initialFile).split("\n"));
		List<String> patchedFileTraceList = Arrays.asList(FileIO.readFileToString(patchedFile).split("\n"));
		Long methodCnt = initialFileTraceList.stream().map(String::trim).filter(StringUtils::isNoneBlank)
				.filter(traceLine -> traceLine.startsWith(methodStartLine)).count();
		// System.out.println("methodCnt: " + methodCnt + "\n");

		String initialColorContentString = getColorTrace(initialFileTraceList, methodStartLine, methodCnt);
		String patchedColorContentString = getColorTrace(patchedFileTraceList, methodStartLine, methodCnt);
		// System.out.println("initialColorContentString: \n" +
		// initialColorContentString + "\n");
		// System.out.println("patchedColorContentString: \n" +
		// patchedColorContentString + "\n");

		List<String> initialContentList = codeFormatting(method, initialColorContentString);
		List<String> patchedContentList = codeFormatting(method, patchedColorContentString);
		System.out.println("initialContentList:");
		for (String line : initialContentList) {
			System.out.println(line);
		}
		System.out.println();
		// System.out.println("patchedContentList:");
		// for (String line : patchedContentList) {
		// System.out.println(line);
		// }
		// System.out.println();

		int index = initialContentList.size() - 1;
		while (index >= 0) {
			String initialLineString = initialContentList.get(index);
			String patchedLineString = patchedContentList.get(index);
			if (initialLineString.contains("Cover") && patchedLineString.contains("Cover")
					&& (!initialLineString.contains("NotCover") && (!patchedLineString.contains("NotCover")))) {
				initialContentList.set(index, "<span color=\"" + CommonCover + "\">"
						+ initialLineString.split("<span color=\"" + Cover + "\">")[1]);
				patchedContentList.set(index, "<span color=\"" + CommonCover + "\">"
						+ initialLineString.split("<span color=\"" + Cover + "\">")[1]);
			}
			--index;
		}
		System.out.println();
		System.out.println("initialContentList:");
		for (String line : initialContentList) {
			System.out.println(line);
		}
		System.out.println();
		// System.out.println("patchedContentList:");
		// for (String line : patchedContentList) {
		// System.out.println(line);
		// }
		// System.out.println();

		// System.out.println();
		// System.out.println("initialContentList: " +
		// initialContentList.stream().collect(Collectors.joining()));
		// System.out.println();
		// System.out.println("patchedContentList: " +
		// patchedContentList.stream().collect(Collectors.joining()));
		// System.out.println();
	}

	private static String getColorTrace(List<String> traceContentList, String methodStartLine, long methodCnt) {
		if (CollectionUtils.isEmpty(traceContentList) || StringUtils.isBlank(methodStartLine)) {
			return "";
		}
		long currentMethodCnt = 0;
		StringBuilder resultContentString = new StringBuilder();
		for (String line : traceContentList) {
			if (StringUtils.isBlank(line.trim())) {
				continue;
			}
			if (line.startsWith(methodStartLine)) {
				++currentMethodCnt;
			}
			if (currentMethodCnt == methodCnt) {
				resultContentString.append(line).append("\n");
			}
		}
		return resultContentString.toString();
	}

	private static List<String> codeFormatting(Method method, String colorContentString) {
		method.updateMethodContent();
		String codeString[] = StringUtils.splitPreserveAllTokens(method.getContentString(), "\n");
		List<String> result = new LinkedList<String>();
		int start = method.getStartLine();
		int end = method.getEndLine();
		result.add("<form><p>");
		for (int i = start; i <= end; i++) {
			String line = codeString[i - start];
			if (line.contains("&")) {
				line = line.replaceAll("&", "&amp;");
			}
			if (line.contains("<")) {
				line = line.replaceAll("<", "&lt;");
			}
			if (line.contains(">")) {
				line = line.replaceAll(">", "&gt;");
			}
			boolean covered = false;
			for (String contentLine : colorContentString.split("\n")) {
				if (contentLine.startsWith(method.getMethodName())) {
					int number = Integer.parseInt(contentLine.split("#")[contentLine.split("#").length - 1]);
					if (number == 0) {
						continue;
					} else if (number < i) {
						continue;
					} else if (number == i) {
						line = "<span color=\"" + Cover + "\">" + line + "</span><br/>";
						covered = true;
						break;
					} else {
						line = "<span color=\"" + NotCover + "\">" + line + "</span><br/>";
						covered = true;
						break;
					}
				} else {
					continue;
				}
			}
			if (!covered) {
				line = "<span color=\"" + NotCover + "\">" + line + "</span><br/>";
				covered = true;
			}
			result.add(line);
		}
		result.add("</p></form>");
		return result;
	}

	public static void main(String[] args) {
		trace();
	}

}
