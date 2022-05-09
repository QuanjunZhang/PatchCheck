package patchfilter.model.initialization.visitor;

import patchfilter.model.entity.Method;
import patchfilter.model.entity.Pair;
import patchfilter.model.util.FileIO;
import org.eclipse.jdt.core.dom.*;
import com.sun.corba.se.impl.encoding.OSFCodeSetRegistry.Entry;

import java.util.LinkedHashMap;
import java.util.Map;

/*
 * 这个类主要用于遍历某个指定的类，最后可以得到以下内容：
 * Map<String, Pair<Integer, Integer>> methodRangeList 这个类内每个 method 的初始和终止行号
 * Map<String, Integer> methodStartList 这个类内每个 method 的初始行号
 * Method _method 指定名称 method 的 函数内容，该起始和结束行号
 */

public class MethodVisitor extends TraversalVisitor {

	private final static String __name__ = "@MethodInstrumentVisitor ";

	private String _clazzName = "";
	private String _clazzFileName = "";
	private CompilationUnit _cu;

	private Method _method;
	private Map<String, Pair<Integer, Integer>> methodRangeList;
	private Map<String, Integer> methodStartList;

	public Map getMethodRange() {
		return methodRangeList;
	}

	public Map getMethodStart() {
		return methodStartList;
	}

	public void setMethod(Method method) {
		this._method = method;
	}

	public Method getMethod() {
		return _method;
	}

	@Override
	public boolean visit(CompilationUnit node) {
		if (node.getPackage().getName() != null
				&& node.getPackage().getName().getFullyQualifiedName().equals("auxiliary")) {
			return false;
		}
		_cu = node;
		_clazzName = node.getPackage().getName().getFullyQualifiedName();
		for (Object object : node.types()) {
			if (object instanceof TypeDeclaration) {
				TypeDeclaration type = (TypeDeclaration) object;
				if (Modifier.isPublic(type.getModifiers())) {
					_clazzName += "." + type.getName().getFullyQualifiedName();
					_clazzFileName = _clazzName;
				}
			} else {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		if (!Modifier.isPublic(node.getModifiers())) {
			if (_clazzFileName.equals("")) {
				_clazzFileName = _clazzName;
				_clazzName = _clazzFileName + "." + node.getName().getFullyQualifiedName();
			} else {
				_clazzName = _clazzFileName + "$" + node.getName().getFullyQualifiedName();
			}
		}
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		// filter those methods that defined in anonymous classes
		ASTNode parent = node.getParent();
		while (parent != null && !(parent instanceof TypeDeclaration)) {
			if (parent instanceof ClassInstanceCreation) {
				return true;
			}
			parent = parent.getParent();
		}

		if (node.getBody() != null) {
			Block body = node.getBody();
			StringBuffer buffer = new StringBuffer(_clazzName + "#");

			String retType = "?";
			if (node.getReturnType2() != null) {
				retType = node.getReturnType2().toString();
			}
			StringBuffer param = new StringBuffer("?");
			for (Object object : node.parameters()) {
				if (!(object instanceof SingleVariableDeclaration)) {
					param.append(",?");
				} else {
					SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) object;
					param.append("," + singleVariableDeclaration.getType().toString());
				}
			}
			// add method return type
			buffer.append(retType + "#");
			// add method name
			buffer.append(node.getName().getFullyQualifiedName() + "#");
			// add method params, NOTE: the first parameter starts at index 1.
			buffer.append(param);
			String message = buffer.toString();

			int lineStartNumber = _cu.getLineNumber(node.getBody().getStartPosition());
			int lineEndNumber = _cu.getLineNumber(node.getBody().getStartPosition() + node.getBody().getLength());
			if (methodRangeList == null) {
				methodRangeList = new LinkedHashMap<>();
			}
			if (methodStartList == null) {
				methodStartList = new LinkedHashMap<String, Integer>();
			}
			methodRangeList.put(message, new Pair<>(lineStartNumber, lineEndNumber));
			methodStartList.put(message, lineStartNumber);
			if (_method != null) {
				if (_method.getMethodName() != null && _method.getMethodName().equals(message)) {
					_method.setContentString(node.toString());
					_method.setStartLine(lineStartNumber);
					_method.setEndLine(lineEndNumber);
				}
			}
			// System.out.println(node.toString());
		}

		return true;
	}

	public static void main(String[] args) {
		String filePath = "D:/Graduation/Dataset/Projects/Math_104_buggy/src/java/org/apache/commons/math/special/Gamma.java";
		MethodVisitor methodVisitor = new MethodVisitor();
		CompilationUnit compilationUnit = FileIO.genASTFromSource(FileIO.readFileToString(filePath),
				ASTParser.K_COMPILATION_UNIT);
		compilationUnit.accept(methodVisitor);
		Map<String, Pair<Integer, Integer>> methodRangeList = methodVisitor.getMethodRange();
		for (Map.Entry<String, Pair<Integer, Integer>> entry : methodRangeList.entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue().getKey() + "-" + entry.getValue().getValue());
		}
	}

}
