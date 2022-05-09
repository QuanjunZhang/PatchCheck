package patchfilter.model.service;

import java.util.HashMap;
import java.util.Map;

import patchfilter.model.entity.Patch;
import patchfilter.model.util.FileIO;

public class ESConfigurationService {

	static Map<String, String> typeMap = new HashMap<String, String>();
	static {
		typeMap.put("boolean", "Z");
		typeMap.put("char", "C");
		typeMap.put("byte", "B");
		typeMap.put("short", "S");
		typeMap.put("int", "I");
		typeMap.put("float", "F");
		typeMap.put("long", "J");
		typeMap.put("double", "D");
		typeMap.put("void", "V");
		typeMap.put("String", "Ljava/Lang/String;");
		typeMap.put("Object", "Ljava/Lang/Object;");
	}

	public static String targetMethod(Patch patch, String cacheFile) {
		StringBuilder methodDescriptor = new StringBuilder();

		String formattedString = FileIO.readFileToString(cacheFile);
		String[] strs = formattedString.split("\\.");
		String method = strs[strs.length - 1];
		strs = method.split("#");

		String methodName = strs[2];
		methodDescriptor.append(methodName).append("(");

		String methodType = strs[3];
		String[] types = methodType.split(",");
		for (int i = 1; i < types.length; i++) {
			if (typeMap.containsKey(types[i])) {
				methodDescriptor.append(typeMap.get(types[i]));
			} else {
				if (types[i].endsWith("[]")) {
					while (types[i].endsWith("[]")) {
						methodDescriptor.append("[");
						types[i] = types[i].substring(0, types[i].length() - 2);
					}
					methodDescriptor.append(typeMap.get(types[i]));
				} else {
					methodDescriptor.append(getExternalClass(patch, types[i]));
				}
			}
		}
		methodDescriptor.append(")");

		String methodRet = strs[1];
		if (typeMap.containsKey(methodRet)) {
			methodDescriptor.append(typeMap.get(methodRet));
		} else {
			if (methodRet.endsWith("[]")) {
				while (methodRet.endsWith("[]")) {
					methodDescriptor.append("[");
					methodRet = methodRet.substring(0, methodRet.length() - 2);
				}
				methodDescriptor.append(typeMap.get(methodRet));
			} else {
				methodDescriptor.append(getExternalClass(patch, methodRet));
			}
		}

		return methodDescriptor.toString();
	}

	public static String getExternalClass(Patch patch, String className) {
		String externalClass = "";

		String fixedFile = patch.getFixedFile();
		String fixedFileContent = FileIO.readFileToString(fixedFile);

		String[] fixedFileNames = fixedFile.split("\\/");
		String fixedfileName = fixedFileNames[fixedFileNames.length - 1].split("\\.")[0];

		if (fixedfileName.equals(className)) {
			externalClass = fixedFile.split(patch.getSubject().getSsrc())[1].split("\\.")[0].substring(1);
			return "L" + externalClass + ";";
		}

		for (String line : fixedFileContent.split("\n")) {
			if (line.startsWith("import")) {
				if (line.endsWith(className + ";")) {
					externalClass = line.split(" ")[1];
					return "L" + externalClass;
				}
			}
		}

		return externalClass;
	}

	public static void main(String[] args) {
		String file = "D:\\Graduation\\PatchCheck\\cache\\Math\\85\\ACS-Patch204";
//		System.out.println(ESConfigurationService.targetMethod(file));
	}

}
