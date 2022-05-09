package patchfilter.model.initialization.visitor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.*;

import sun.util.logging.resources.logging;

public class StatementGenerator {

	private static AST ast = AST.newAST(AST.JLS8);

	public static Statement genReturnStatement(String varName) {
	 ReturnStatement returnStatement = ast.newReturnStatement();
	 SimpleName simpleName = ast.newSimpleName(varName);
	 returnStatement.setExpression(simpleName);
	 return returnStatement;
	 }

	private static Statement generatePrinter(Expression writeFile, Expression expression) {
		// auxiliary.Dumper.write(writeFile, expression);
		MethodInvocation methodInvocation = ast.newMethodInvocation();
		methodInvocation.setExpression(ast.newName("auxiliary.Dumper"));
		methodInvocation.setName(ast.newSimpleName("write"));
		methodInvocation.arguments().add(writeFile);
		methodInvocation.arguments().add(expression);
		ExpressionStatement expressionStatement = ast.newExpressionStatement(methodInvocation);
		return expressionStatement;
	}

	public static Statement genDumpLine(String writeFile, String locMessage, int line) {
		StringLiteral stringLiteral = ast.newStringLiteral();
		stringLiteral.setLiteralValue(locMessage + "#" + line);
		StringLiteral fileLiteral = ast.newStringLiteral();
		fileLiteral.setLiteralValue(writeFile);
		return generatePrinter(fileLiteral, stringLiteral);
	}

	// public static Statement
	public static Statement genVariableDumpMethodInvocation(String writeFile, String variableName) {
		// infixExpression is "this.columnCount:"+
		// auxiliary.Dumper.dump(this.columnCount)
		InfixExpression infixExpression = ast.newInfixExpression();
		StringLiteral stringLiteral = ast.newStringLiteral();
		stringLiteral.setLiteralValue(variableName + ": ");

		MethodInvocation methodInvocation = ast.newMethodInvocation();
		methodInvocation.setExpression(ast.newName("auxiliary.Dumper"));
		methodInvocation.setName(ast.newSimpleName("dump"));
		SimpleName simpleName = ast.newSimpleName(variableName);
		methodInvocation.arguments().add(simpleName);

		infixExpression.setLeftOperand(stringLiteral);
		infixExpression.setOperator(InfixExpression.Operator.PLUS);
		infixExpression.setRightOperand(methodInvocation);

		StringLiteral fileLiteral = ast.newStringLiteral();
		fileLiteral.setLiteralValue(writeFile);

		return generatePrinter(fileLiteral, infixExpression);

	}

	public static Statement genThisFieldDumpMethodInvocation(String writeFile) {
		ThisExpression thisExpression = ast.newThisExpression();

		InfixExpression infixExpression = ast.newInfixExpression();
		StringLiteral stringLiteral = ast.newStringLiteral();
		stringLiteral.setLiteralValue("this: ");

		MethodInvocation methodInvocation = ast.newMethodInvocation();
		methodInvocation.setExpression(ast.newName("auxiliary.Dumper"));
		methodInvocation.setName(ast.newSimpleName("dump"));
		methodInvocation.arguments().add(thisExpression);

		infixExpression.setLeftOperand(stringLiteral);
		infixExpression.setOperator(InfixExpression.Operator.PLUS);
		infixExpression.setRightOperand(methodInvocation);

		StringLiteral fileLiteral = ast.newStringLiteral();
		fileLiteral.setLiteralValue(writeFile);

		return generatePrinter(fileLiteral, infixExpression);
	}

	 private static Expression generateDumperWrite(String writeFile, Expression expression) {
		StringLiteral fileLiteral = ast.newStringLiteral();
		fileLiteral.setLiteralValue(writeFile);

		StringLiteral stringLiteral = ast.newStringLiteral();
		stringLiteral.setLiteralValue(expression + ": ");

		// infixExpression.setLeftOperand(stringLiteral);
		// infixExpression.setOperator(InfixExpression.Operator.PLUS);
		// infixExpression.setRightOperand((Expression)
		// ASTNode.copySubtree(ast,expression));

		MethodInvocation methodInvocation = ast.newMethodInvocation();
		methodInvocation.setExpression(ast.newName("auxiliary.Dumper"));
		methodInvocation.setName(ast.newSimpleName("write"));
		methodInvocation.arguments().add(fileLiteral);
		methodInvocation.arguments().add(stringLiteral);
		methodInvocation.arguments().add((Expression) ASTNode.copySubtree(ast, expression));
		return methodInvocation;
	}

	// (Number) auxiliary.Dumper.write("", createLong(numeric));
	public static Statement genReturnWriteStatement(String writeFile, Expression expression, Type reType) {
		if (reType instanceof PrimitiveType) {
			PrimitiveType primitiveType = (PrimitiveType) reType;
			if (primitiveType.getPrimitiveTypeCode() == PrimitiveType.DOUBLE) {
				reType = ast.newSimpleType(ast.newSimpleName("Double"));
			} else if (primitiveType.getPrimitiveTypeCode() == PrimitiveType.BOOLEAN) {
				reType = ast.newSimpleType(ast.newSimpleName("Boolean"));
			} else if (primitiveType.getPrimitiveTypeCode() == primitiveType.LONG) {
				reType = ast.newSimpleType(ast.newSimpleName("Long"));
			} else if (primitiveType.getPrimitiveTypeCode() == primitiveType.INT) {
				reType = ast.newSimpleType(ast.newSimpleName("Integer"));
			} else {
				System.out.println("@GenStatement.genReturnWriteStatement");
				System.out.println("Need to process: " + primitiveType.getPrimitiveTypeCode().toString());
			}
		}

		CastExpression castExpr = ast.newCastExpression();
		castExpr.setType((Type) ASTNode.copySubtree(ast, reType));
		castExpr.setExpression(generateDumperWrite(writeFile, expression));

		ReturnStatement returnStatement = ast.newReturnStatement();
		returnStatement.setExpression(castExpr);
		return returnStatement;
	}

	// (Number) auxiliary.Dumper.write("", createLong(numeric));
	public static Statement genThrowWriteStatement(String writeFile, Expression expression, Type reType) {
		CastExpression castExpr = ast.newCastExpression();
		castExpr.setType((Type) ASTNode.copySubtree(ast, reType));
		castExpr.setExpression(generateDumperWrite(writeFile, expression));

		ThrowStatement throwStatement = ast.newThrowStatement();
		throwStatement.setExpression(castExpr);
		return throwStatement;

	}

	public static Statement genThisFieldWriteStatement(String writeFile) {
		ThisExpression thisExpression = ast.newThisExpression();
		ExpressionStatement expressionStatement = ast
				.newExpressionStatement(generateDumperWrite(writeFile, thisExpression));
		return expressionStatement;
	}

	public static Statement genDumperWriteStatement(String writeFile, Expression expression) {
		// StringLiteral stringLiteral = ast.newStringLiteral();
		// stringLiteral.setLiteralValue(expression);
		ExpressionStatement expressionStatement = ast
				.newExpressionStatement(generateDumperWrite(writeFile, expression));
		return expressionStatement;
	}

	public static List<ASTNode> generate(MethodDeclaration methodDeclaration, String writeFile) {
		List<ASTNode> statements = new ArrayList<>();
		int modifiers = methodDeclaration.getModifiers();
		if (!Modifier.isAbstract(modifiers) && !Modifier.isStatic(modifiers)) {
			statements.add(genThisFieldWriteStatement(writeFile));
		}
		// print parameter information
		List<ASTNode> params = methodDeclaration.parameters();
		for (ASTNode param : params) {
			if (param instanceof SingleVariableDeclaration) {
				SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) param;
				SimpleName praramName = singleVariableDeclaration.getName();
				statements.add(genDumperWriteStatement(writeFile, praramName));
			}
		}
		return statements;
	}

}
