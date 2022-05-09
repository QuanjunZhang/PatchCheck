package patchfilter.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import patchfilter.configuration.Constant;
import patchfilter.model.entity.Method;
import patchfilter.model.entity.Patch;
import patchfilter.model.entity.Project;
import patchfilter.model.util.BuildFilePath;
import patchfilter.model.util.FileIO;
import patchfilter.util.VariableInfo;

public class VariableController {

	private List<VariableInfo> variableInfoList;

	public List<VariableInfo> getVariableInfoList() {
		return variableInfoList;
	}

	public void getVarChange(Patch patchFile) {
		variableInfoList = new ArrayList<>();

		String patchMethodFile = Constant.CACHE + patchFile.getSubject().getName() + "/"
				+ patchFile.getSubject().getId() + "/" + patchFile.getPatchName() + "_method";

		String initialVarFile = BuildFilePath.tmpState(patchFile.getPatchName() + "_initial", patchFile.getSubject());
		String patchedVarFile = BuildFilePath.tmpState(patchFile.getPatchName(), patchFile.getSubject());
		Method method = JSONObject.parseObject(FileIO.readFileToString(patchMethodFile), Method.class);

		String methodStartLine = method.getMethodName() + " START#0";

		List<String> initialFileVarList = Arrays.asList(FileIO.readFileToString(initialVarFile).split("\n"));
		List<String> patchedFileVarList = Arrays.asList(FileIO.readFileToString(patchedVarFile).split("\n"));
		Long methodCnt = Long.min(
				initialFileVarList.stream().map(String::trim).filter(StringUtils::isNoneBlank)
						.filter(traceLine -> traceLine.startsWith(methodStartLine)).count(),
				patchedFileVarList.stream().map(String::trim).filter(StringUtils::isNoneBlank)
						.filter(traceLine -> traceLine.startsWith(methodStartLine)).count());

		String initialMethodVarContent = getMethodVar(initialFileVarList, methodStartLine, methodCnt);
		String patchedMethodVarContent = getMethodVar(patchedFileVarList, methodStartLine, methodCnt);
		// System.out.println(initialMethodVarContent);
		// System.out.println(patchedMethodVarContent);

		String[] initialMethodVarList = initialMethodVarContent.split("\n");
		String[] patchedMethodVarList = patchedMethodVarContent.split("\n");

		int paraNum = method.getMethodName().split("#")[3].split(",").length;
		String[] type = method.getMethodName().split("#")[3].split(",");
		type[0] = "Object";

		int listLen = initialMethodVarList.length;

		// System.out.println("in: ");
		for (int i = 1; i <= paraNum; i++) {
			VariableInfo varInfo = new VariableInfo();
			varInfo.setVarName("[in]" + initialMethodVarList[i].split(": ")[0]);
			varInfo.setType(type[i - 1]);
			varInfo.setInitialValue(initialMethodVarList[i].split(": ")[1]);
			varInfo.setPatchedValue(patchedMethodVarList[i].split(": ")[1]);
			if (!varInfo.getInitialValue().equals(varInfo.getPatchedValue())) {
				variableInfoList.add(varInfo);
				// System.out.println(varInfo.getVarName() + ": " + varInfo.getInitialValue() +
				// " - "
				// + varInfo.getPatchedValue());
			}
		}

		// System.out.println("out: ");
		for (int i = paraNum + 1; i <= paraNum * 2; i++) {
			VariableInfo varInfo = new VariableInfo();
			varInfo.setVarName("[out]" + initialMethodVarList[i].split(": ")[0]);
			varInfo.setInitialValue(initialMethodVarList[i].split(": ")[1]);
			varInfo.setPatchedValue(patchedMethodVarList[i].split(": ")[1]);
			varInfo.setType(type[i - paraNum - 1]);
			if (!varInfo.getInitialValue().equals(varInfo.getPatchedValue())) {
				variableInfoList.add(varInfo);
				// System.out.println(varInfo.getVarName() + ": " + varInfo.getInitialValue() +
				// " - "
				// + varInfo.getPatchedValue());
			}
		}

		// System.out.println("return: ");
		VariableInfo initialVarInfo = new VariableInfo();
		initialVarInfo.setVarName("[ret]" + initialMethodVarList[paraNum * 2 + 2].split(": ")[0]);
		initialVarInfo.setInitialValue(initialMethodVarList[paraNum * 2 + 2].split(": ")[1]);
		initialVarInfo.setPatchedValue("-");
		initialVarInfo.setType("-");
		VariableInfo patchedVarInfo = new VariableInfo();
		patchedVarInfo.setVarName("[ret]" + patchedMethodVarList[paraNum * 2 + 2].split(": ")[0]);
		patchedVarInfo.setPatchedValue(patchedMethodVarList[paraNum * 2 + 2].split(": ")[1]);
		patchedVarInfo.setInitialValue("-");
		patchedVarInfo.setType("-");
		if (!initialVarInfo.getInitialValue().equals(patchedVarInfo.getPatchedValue())) {
			variableInfoList.add(initialVarInfo);
			variableInfoList.add(patchedVarInfo);
			// System.out.println(initialVarInfo.getVarName() + ": " +
			// initialVarInfo.getInitialValue());
			// System.out.println(patchedVarInfo.getVarName() + ": " +
			// patchedVarInfo.getPatchedValue());
		}

	}

	private String getMethodVar(List<String> varList, String methodStartLine, long methodCnt) {
		if (CollectionUtils.isEmpty(varList) || StringUtils.isBlank(methodStartLine)) {
			return "";
		}
		long currentMethodCnt = 0;
		StringBuilder resultContentString = new StringBuilder();
		for (String line : varList) {
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

	public static void main(String[] args) {
		Patch patch = new Patch(new Project("Math", 2), Constant.AllPatchPath + "/Math/2/jKali-Patch28");
		patch.parsePatch();
		VariableController controller = new VariableController();
		controller.getVarChange(patch);
	}

}
