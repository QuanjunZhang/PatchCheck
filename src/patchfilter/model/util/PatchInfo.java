package patchfilter.model.util;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;
import patchfilter.configuration.Constant;
import patchfilter.model.entity.Pair;
import patchfilter.model.entity.Patch;
import patchfilter.model.initialization.visitor.MethodVisitor;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class PatchInfo {

	public static void obainAllMethod(List<Patch> patchFileList) {
		for (Patch patchFile : patchFileList) {
			getModifyMethod(patchFile);
		}
	}

	// 设置补丁文件修改的函数名称
	public static void getModifyMethod(Patch patchFile) {
		log.info("Init Method Range Info for: " + patchFile.getPatchName());
		// System.out.println("Init Method Range Info for: " +
		// patchFile.getPatchName());

		String fixedFile = patchFile.getFixedFile();
		int modifyLine = patchFile.getModifyLine();
		// System.out.println("fixedFile: " + fixedFile + " modifyLine: " + modifyLine);

		// 存储每个补丁修改的对应方法为：类#返回类型#方法名#参数类型
		// 例：/home/xushicheng/eclipse-workspace/InPaFer/cache/Math/41/Cardumen-patch42
		String patchMethodFile = Constant.CACHE + patchFile.getSubject().getName() + "/"
				+ patchFile.getSubject().getId() + "/" + patchFile.getPatchName();
		// 存储 fixedFile 下所有方法始末行号，json 格式存储
		// 例：/home/xushicheng/eclipse-workspace/InPaFer/cache/Math/41/Variance.java
		String fixedFileName = fixedFile.split("/")[fixedFile.split("/").length - 1];
		String methodRangeFile = Constant.CACHE + patchFile.getSubject().getName() + "/"
				+ patchFile.getSubject().getId() + "/" + fixedFileName;

		Map<String, Pair<Integer, Integer>> methodRangeMap = new HashMap<String, Pair<Integer, Integer>>();

		if (new File(patchMethodFile).exists()) {
			String methodName = FileIO.readFileToString(patchMethodFile);
			patchFile.setModifiedMethod(methodName);
		} else if (new File(methodRangeFile).exists()) {
			methodRangeMap = json2Map(FileIO.readFileToString(methodRangeFile));
		} else {
			FileIO.backUpFile(fixedFile, fixedFile + ".bak");
			MethodVisitor methodVisitor = new MethodVisitor();
			CompilationUnit compilationUnit = FileIO.genASTFromSource(FileIO.readFileToString(fixedFile),
					ASTParser.K_COMPILATION_UNIT);
			compilationUnit.accept(methodVisitor);
			methodRangeMap = methodVisitor.getMethodRange();
			String methodRange = JSONObject.toJSONString(methodRangeMap);
			FileIO.writeStringToFile(methodRangeFile, methodRange);
		}

		for (Map.Entry<String, Pair<Integer, Integer>> entry : methodRangeMap.entrySet()) {
			int starLine = entry.getValue().getKey();
			int endLine = entry.getValue().getValue();
			if (modifyLine >= starLine && modifyLine <= endLine) {
				FileIO.writeStringToFile(patchMethodFile, entry.getKey());
				patchFile.setModifiedMethod(entry.getKey());
				break;
			}
		}
		if (patchFile.getModifiedMethod().equals("")) {
			log.error("Patch " + patchFile.getPatchName() + " Cannot get modified Method!");
		}
	}

	public static Map<String, Pair<Integer, Integer>> json2Map(String str) {
		Map<String, Pair<Integer, Integer>> map = new Gson().fromJson(str,
				new TypeToken<HashMap<String, Pair<Integer, Integer>>>() {
				}.getType());
		return map;
	}

}
