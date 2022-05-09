package patchfilter.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import patchfilter.configuration.Constant;
import patchfilter.model.run.Runner;
import patchfilter.model.util.FileIO;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class Patch {

	// 构造方法
	private Project subject;
	private String patchPath;

	// 补丁文件解析
	private String patchName = "";
	private int startLine;
	private String fixedFile = null;
	private String patchContent = "";
	private String buggyContent = "";
	private String fixedContent = "";
	private int lineRange;
	private int modifyLine = 0;
	private List<String> deleteLine = new LinkedList<>();
	private boolean modify = false;
	public boolean isMultiModify = false;
	private String modifiedMethod = "";
	private String aliaName = "";

	private boolean correctness = false;

	public Patch(Project subject, String patchPath) {
		this.subject = subject;
		this.patchPath = patchPath;
	}

	public Patch(Project subject, String patchPath, String fixedFile, String buggyContent, String fixedContent) {
		this.subject = subject;
		this.patchPath = patchPath;
		this.fixedFile = fixedFile;
		this.buggyContent = buggyContent;
		this.fixedContent = fixedContent;
	}

	// 解析补丁文件
	@PostConstruct
	public void parsePatch() {
		File patchFile = new File(patchPath);
		patchName = patchFile.getName();
		String diffConent = FileIO.readFileToString(patchFile);
		int tmpLine = 0;
		for (String line : diffConent.split("\n")) {
			if (line.startsWith("diff") || line.startsWith("+++")) {
				continue;
			}
			// "@@ -319,7 +319,7 @@" --> 开始行号
			if (line.startsWith("@@")) {
				startLine = Integer.parseInt(line.split("-")[1].split(",")[0]);
				lineRange = Integer.parseInt(line.split("-")[1].split(",")[1].split(" ")[0]);
				tmpLine = startLine;
				if (modify) {
					isMultiModify = true;
					break;
				} else {
					modify = true;
				}
				continue;
			}
			// "---" --> 被补丁修改的源文件地址
			if (line.startsWith("---")) {
				if (line.startsWith("--- original")) {
					fixedFile = subject.getHome() + subject.getSsrc() + line.split("--- original")[1];
				} else {
					fixedFile = subject.getHome() + subject.getSsrc()
							+ line.split(subject.getSsrc())[1].split("\\.java")[0] + ".java";
				}
				fixedFile = fixedFile.replaceAll("//", "/");
			} else {
				// 获取补丁修改的内容 "-" --> 被补丁删去的内容 "+" --> 被补丁增加的内容
				tmpLine++;
				patchContent = patchContent + line + "\n";
				if (line.startsWith("-") && !line.startsWith("---")) {
					if (line.split("-").length > 1) {
						buggyContent = buggyContent + line.split("-", 2)[1] + "\n";
						modifyLine = tmpLine;
						deleteLine.add(fixedFile.replaceAll("\\/\\/", "/").replaceAll("/", "\\.") + "#"
								+ String.valueOf(tmpLine - 1));
					}
				} else if (line.startsWith("+") && !line.startsWith("+++")) {
					if (line.split("\\+").length > 1) {
						fixedContent = fixedContent + line.split("\\+", 2)[1] + "\n";
						if (modifyLine == 0) {
							modifyLine = tmpLine;
						}
					}
				} else {
					buggyContent = buggyContent + line + "\n";
					fixedContent = fixedContent + line + "\n";
				}
			}
		}
	}

	// 别名
	private void initAlia() {
		String chars = "abcdefghijklmnopqrstuvwxyz";
		// System.out.println();
		StringBuilder nameBuilder = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			nameBuilder.append(chars.charAt((int) (Math.random() * 26)));
		}
		aliaName = nameBuilder.toString() + "-" + patchName.split("-")[0];
	}

	public boolean patchToFile() {
		log.info("Patch To File");
		if (Runner.patchFile(fixedFile, patchPath)) {
			return true;
		}
		return false;
	}

	public void disPatchFile() {
		FileIO.restoreFile(fixedFile, fixedFile + ".bak");
	}

	@Override
	public String toString() {
		return "PatchFile [patchName = " + patchName + ", subject = " + subject.toString() + "], fixedFile = "
				+ fixedFile + "";
	}

	public static void main(String[] args) {
		String project = "Math";
		int bugid = 2;
		// Subject subject = new Subject(project, bugid);
		String patch_path = Constant.AllPatchPath + "/" + project + "/" + bugid + "/jGenprog-Patch27";
		Patch patchFile = new Patch(new Project(project, bugid), patch_path);
		patchFile.parsePatch();
		System.out.println(patchFile.getFixedFile());
		System.out.println(patchFile.getStartLine());
		System.out.println(patchFile.getLineRange());
		System.out.println("-----------------------------------------");
		System.out.println(patchFile.getBuggyContent());
		System.out.println("-----------------------------------------");
		System.out.println(patchFile.getFixedContent());
		System.out.println("-----------------------------------------");
		for(String line:patchFile.getDeleteLine()) {
			System.out.println(line);
		}

		// File patch_dic = new File(patch_path);
		// File[] patches = patch_dic.listFiles();
		// for (int i = 0; i < patches.length; i++) {
		// String patch = patches[i].getAbsolutePath();
		// if (patch.contains(".DS_Store")) {
		// continue;
		// }
		// PatchFile patchFile = new PatchFile(new Subject(project, bugid), patch);
		// patchFile.parsePatch();
		// System.out.println(patchFile.isMultiModify);
		// System.out.println("");
		// }
	}

}
