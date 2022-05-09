package patchfilter.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class PatchAdaption {

	public String read(String filePath) {
		BufferedReader br = null;
		String line = null;
		StringBuffer buf = new StringBuffer();
		try {
			br = new BufferedReader(new FileReader(filePath));
			while ((line = br.readLine()) != null) {
				// 此处根据实际需要修改某些行的内容
				if (line.startsWith("+++") || line.startsWith("---")) {
					buf.append(line.substring(0, 4));
					line = line.substring(4);
					String[] pathArray = line.split("/projects/");
					buf.append("/home/xushicheng/dataset/defects4j/projects/");
					buf.append(pathArray[1]);
				} else {
					buf.append(line);
				}
				buf.append(System.getProperty("line.separator"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					br = null;
				}
			}
		}
		return buf.toString();
	}

	public boolean write(String filePath, String content) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(filePath));
			bw.write(content);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					bw = null;
				}
			}
		}
		return true;
	}

	public static void main(String[] args) {
		String fileListPath = "/home/xushicheng/eclipse-workspace/InPaFer/FinalPatch/Math/41/";
		PatchAdaption obj = new PatchAdaption();
		// String s = obj.read(fileListPath);
		// System.out.println(s);
		File fileList = new File(fileListPath);
		for (File file : fileList.listFiles()) {
			System.out.println("modifying " + file.getAbsolutePath());
			boolean flag = obj.write(file.getAbsolutePath(), obj.read(file.getAbsolutePath()));
			if (flag) {
				System.out.println("Done! ");
			} else {
				System.out.println("Error happens! ");
				System.exit(1);
			}
		}
	}

}
