package patchfilter.model.script;

import java.io.File;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import patchfilter.model.entity.CorrectPatch;
import patchfilter.model.util.FileIO;

public class PatchLabel {

	public void mianProcess() {
		String path = "/Users/liangjingjing/WorkSpace/Project/Patch-Correctness/tool/patches/INFO";
		String file = "./test.json";
		StringBuilder resultBuilder = new StringBuilder();
		for (File f : new File(path).listFiles()) {
			if (f.getName().contains("json")) {
				TmpInfo tmpInfo = JSONObject.parseObject(FileIO.readFileToString(f), TmpInfo.class);
				if (tmpInfo.getCorrectness().equals("Correct") && !tmpInfo.getProject().equals("Mockito")) {
					CorrectPatch correctPatch = new CorrectPatch(tmpInfo.getTool() + "-" + tmpInfo.getID(),
							tmpInfo.getProject(), tmpInfo.getBug_id());
					String str = JSON.toJSONString(correctPatch);
					resultBuilder.append(str).append(",\n");
				}
			}
		}
		FileIO.writeStringToFile(file, resultBuilder.toString());

	}

	public void patchinfo() {
		String file = "./test1.json";
		String path = "/Users/liangjingjing/WorkSpace/Project/PatchFilterBackUp/patchfilter/d4j-info/patchinfo.json";
		String correctPatchString = FileIO.readFileToString(path);
		List<CorrectPatch> correctPatchList = JSONObject.parseArray(correctPatchString, CorrectPatch.class);
		StringBuilder resultBuilder = new StringBuilder();
		for (CorrectPatch correctPatch : correctPatchList) {
			String str = JSON.toJSONString(correctPatch);
			resultBuilder.append(str).append(",\n");
		}

		FileIO.writeStringToFile(file, resultBuilder.toString());

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PatchLabel patchLabel = new PatchLabel();
		// patchLabel.mianProcess();
		patchLabel.patchinfo();
	}

}
