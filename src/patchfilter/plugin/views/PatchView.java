package patchfilter.plugin.views;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.ViewPart;

import com.alibaba.fastjson.JSONObject;

import patchfilter.configuration.Constant;
import patchfilter.controller.TestController;
import patchfilter.model.entity.Method;
import patchfilter.model.entity.Pair;
import patchfilter.model.entity.Patch;
import patchfilter.model.util.BuildFilePath;
import patchfilter.model.util.FileIO;
import patchfilter.model.util.PatchInfo;
import patchfilter.plugin.views.provider.TestCriteriaProvider;
import patchfilter.plugin.views.provider.TestLabelProvider;
import patchfilter.util.TestInfo;
import patchfilter.util.VariableInfo;
import patchfilter.model.entity.Project;

public class PatchView extends ViewPart {

	private TableViewer testCriteriaTableViewer;
	private TableViewer variableTableViewer;

	private FormToolkit toolkit;

	private ScrolledForm codeDiffForm;
	private SashForm testForm;
	private SashForm variableForm;

	private TestController testController;

	private final String Cover = "Cover";
	private final String NotCover = "NotCover";

	@Override
	public void createPartControl(Composite parent) {
		System.out.println("Patch View Start! ");
		parent.setOrientation(SWT.VERTICAL);
		toolkit = new FormToolkit(parent.getDisplay());
		Form form = toolkit.createForm(parent);
		form.setLayoutData(new GridData(1200, 50));
		form.setText("Patch View");
		GridLayout parentLayout = new GridLayout(1, true);
		parent.setLayout(parentLayout);
		createCodeDiff(parent);
		createVariableBody(parent);
		createTestCriteria(parent);
	}

	// 面板1：代码差异
	protected void createCodeDiff(Composite body) {
		toolkit = new FormToolkit(body.getDisplay());
		codeDiffForm = toolkit.createScrolledForm(body);
		codeDiffForm.setLayoutData(new GridData(1200, 150));
		codeDiffForm.setText("Code Difference");
		codeDiffForm.setAlwaysShowScrollBars(true);
		TableWrapLayout tableLayout = new TableWrapLayout();
		tableLayout.numColumns = 2;
		tableLayout.makeColumnsEqualWidth = true;
		codeDiffForm.getBody().setLayout(tableLayout);
		for (Control control : codeDiffForm.getBody().getChildren()) {
			control.dispose();
		}
		body.redraw();
		// test
		// System.out.println("test for codesection");
		// Subject subject = new Subject("Math", 41);
		// subject.initPatchListByPath(Constant.AllPatchPath);
		// createCodeSections(Collections.singletonList(subject.getPatchList().get(0)));
	}

	public void updateCodeDiffForm(Patch patchFile) {
		for (Control control : codeDiffForm.getBody().getChildren()) {
			control.dispose();
		}

		Composite body = codeDiffForm.getBody();
		Section section = toolkit.createSection(body, Section.TWISTIE | Section.EXPANDED | Section.TITLE_BAR);
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		section.setExpanded(true);
		section.setLayout(new TableWrapLayout());
		section.setText("Buggy Code");
		FormText text = toolkit.createFormText(section, true);
		text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		text.setWhitespaceNormalized(false);
		try {
			text.setText(patchFile.getBuggyContent(), false, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		section.setClient(text);

		Section section2 = toolkit.createSection(body, Section.TWISTIE | Section.EXPANDED | Section.TITLE_BAR);
		section2.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		section2.setExpanded(true);
		section2.setLayout(new TableWrapLayout());
		section2.setText(patchFile.getAliaName());
		FormText text2 = toolkit.createFormText(section2, true);
		text2.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		text2.setWhitespaceNormalized(false);
		try {
			text2.setText(patchFile.getFixedContent(), false, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		section2.setClient(text2);

		codeDiffForm.getBody().redraw();
		codeDiffForm.getBody().update();
	}

	public void updateCodeDiffForm(List<Patch> patchFiles) {
		for (Control control : codeDiffForm.getBody().getChildren()) {
			control.dispose();
		}

		PatchInfo.obainAllMethod(patchFiles);
		Patch patchFile = patchFiles.get(0);
		String patchMethodFile = Constant.CACHE + patchFile.getSubject().getName() + "/"
				+ patchFile.getSubject().getId() + "/" + patchFile.getPatchName() + "_method";
		String initialTraceLineFile = BuildFilePath.tmpLine(patchFile.getPatchName() + "_initial",
				patchFile.getSubject());
		String patchedTraceLineFile = BuildFilePath.tmpLine(patchFile.getPatchName(), patchFile.getSubject());
		Method method = JSONObject.parseObject(FileIO.readFileToString(patchMethodFile), Method.class);
		createCodeSection(method, patchFile, initialTraceLineFile, patchedTraceLineFile);
	}

	private void createCodeSection(Method method, Patch patchFile, String initialFile, String patchedFile) {
		String methodStartLine = method.getMethodName() + " START#0";
		List<String> initialFileTraceList = Arrays.asList(FileIO.readFileToString(initialFile).split("\n"));
		List<String> patchedFileTraceList = Arrays.asList(FileIO.readFileToString(patchedFile).split("\n"));
		Long methodCnt = Long.min(
				initialFileTraceList.stream().map(String::trim).filter(StringUtils::isNoneBlank)
						.filter(traceLine -> traceLine.startsWith(methodStartLine)).count(),
				patchedFileTraceList.stream().map(String::trim).filter(StringUtils::isNoneBlank)
						.filter(traceLine -> traceLine.startsWith(methodStartLine)).count());
		String initialColorContentString = getColorTrace(initialFileTraceList, methodStartLine, methodCnt);
		String patchedColorContentString = getColorTrace(patchedFileTraceList, methodStartLine, methodCnt);

		List<String> initialContentList = codeFormatting(method, initialColorContentString);
		List<String> patchedContentList = new ArrayList<>();
		FileIO.backUpFile(patchFile.getFixedFile(), patchFile.getFixedFile() + ".bak");
		if (patchFile.patchToFile()) {
			patchedContentList = codeFormatting(method, patchedColorContentString);
		} else {
			System.err.println("Patch " + patchFile.getPatchName() + " Patches File Fail.");
		}
		FileIO.restoreFile(patchFile.getFixedFile(), patchFile.getFixedFile() + ".bak");
		for (String line : initialContentList) {
			System.out.println(line);
		}
		System.out.println();
		for (String line : patchedContentList) {
			System.out.println(line);
		}

		Composite body = codeDiffForm.getBody();
		Section section = toolkit.createSection(body, Section.TWISTIE | Section.EXPANDED | Section.TITLE_BAR);
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		section.setExpanded(true);
		section.setLayout(new TableWrapLayout());
		section.setText("Initial Trace");
		FormText text = toolkit.createFormText(section, true);
		FormColors colors = toolkit.getColors();
		colors.createColor("Cover", colors.getSystemColor(SWT.COLOR_DARK_GREEN));
		text.setColor("Cover", colors.getColor("Cover"));
		colors.createColor("NotCover", colors.getSystemColor(SWT.COLOR_BLACK));
		text.setColor("NotCover", colors.getColor("NotCover"));
		text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		text.setWhitespaceNormalized(false);
		try {
			text.setText(initialContentList.stream().collect(Collectors.joining()), true, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		section.setClient(text);

		section = toolkit.createSection(body, Section.TWISTIE | Section.EXPANDED | Section.TITLE_BAR);
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		section.setExpanded(true);
		section.setLayout(new TableWrapLayout());
		section.setText(patchFile.getAliaName());
		text = toolkit.createFormText(section, true);
		colors = toolkit.getColors();
		colors.createColor("Cover", colors.getSystemColor(SWT.COLOR_DARK_GREEN));
		text.setColor("Cover", colors.getColor("Cover"));
		colors.createColor("NotCover", colors.getSystemColor(SWT.COLOR_BLACK));
		text.setColor("NotCover", colors.getColor("NotCover"));
		text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		text.setWhitespaceNormalized(false);
		try {
			text.setText(patchedContentList.stream().collect(Collectors.joining()), true, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		section.setClient(text);

		codeDiffForm.getBody().redraw();
		codeDiffForm.getBody().update();
	}

	private String getColorTrace(List<String> traceContentList, String methodStartLine, long methodCnt) {
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

	private List<String> codeFormatting(Method method, String colorContentString) {
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
					System.out.println(number + " " + i);
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

	// 面板2：变量
	protected void createVariableBody(Composite parent) {
		variableForm = new SashForm(parent, SWT.VERTICAL);
		GridData gridData = new GridData(1200, 150);
		variableForm.setLayoutData(gridData);
		List<VariableInfo> variableInfoList = new ArrayList<>();
		variableTableViewer = createVariableTableViewer(variableForm, "Variable Information: ", variableInfoList);
		variableForm.setWeights(new int[] { 5 });
	}

	private TableViewer createVariableTableViewer(SashForm variableForm, String groupName,
			List<VariableInfo> variableInfoList) {
		Group varGroup = new Group(variableForm, SWT.NONE);
		varGroup.setText(groupName);
		varGroup.setLayout(new FillLayout());
		TableViewer viewer = new TableViewer(varGroup);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		viewer.setContentProvider(new ArrayContentProvider());
		// column 1 varName
		TableColumn column = new TableColumn(viewer.getTable(), SWT.LEFT);
		column.setText("Variable");
		column.setWidth(350);
		TableViewerColumn varNameColumn = new TableViewerColumn(viewer, column);
		varNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof VariableInfo) {
					VariableInfo variableLine = (VariableInfo) element;
					String varName = variableLine.getVarName();
					return varName;
				}
				return null;
			}
		});
		// column 2 varType
		column = new TableColumn(viewer.getTable(), SWT.LEFT);
		column.setText("Type");
		column.setWidth(100);
		TableViewerColumn varTypeColumn = new TableViewerColumn(viewer, column);
		varTypeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof VariableInfo) {
					VariableInfo variableLine = (VariableInfo) element;
					String varName = variableLine.getType();
					return varName;
				}
				return null;
			}
		});
		// column 3 initial varValue
		column = new TableColumn(viewer.getTable(), SWT.LEFT);
		column.setText("Initial");
		column.setWidth(375);
		TableViewerColumn initialVarValueColumn = new TableViewerColumn(viewer, column);
		initialVarValueColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof VariableInfo) {
					VariableInfo variableLine = (VariableInfo) element;
					String varValue = variableLine.getInitialValue();
					return varValue;
				}
				return null;
			}
		});
		// column 4 patched varValue
		column = new TableColumn(viewer.getTable(), SWT.LEFT);
		column.setText("Patched");
		column.setWidth(375);
		TableViewerColumn patchedVarValueColumn = new TableViewerColumn(viewer, column);
		patchedVarValueColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof VariableInfo) {
					VariableInfo variableLine = (VariableInfo) element;
					String varValue = variableLine.getPatchedValue();
					return varValue;
				}
				return null;
			}
		});
		viewer.setInput(variableInfoList);
		return viewer;
	}

	public void updateVariableView(List<VariableInfo> variableInfoList) {
		for (Control control : variableForm.getChildren()) {
			control.dispose();
		}
		variableTableViewer = createVariableTableViewer(variableForm, "Variable Information: ", variableInfoList);
		variableTableViewer.refresh(true);
		variableForm.setWeights(new int[] { 5 });
	}

	// 面板2：测试生成达到的标准
	protected void createTestCriteria(Composite body) {
		testForm = new SashForm(body, SWT.VERTICAL);
		GridData gridData = new GridData(1200, 100);
		testForm.setLayoutData(gridData);
		testCriteriaTableViewer = createTestCriteriaTableViewer(testForm, "New Test Criteria: ");
		Map<String, String> criteriaMap = new HashMap<String, String>();
		criteriaMap.put("TargetClass", "NULL");
		criteriaMap.put("LINE", "0");
		criteriaMap.put("BRANCH", "0");
		criteriaMap.put("METHOD", "0");
		criteriaMap.put("average", "0");
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		list.add(criteriaMap);
		createTestCriteriaContent(list);
		testForm.setWeights(new int[] { 5 });
	}

	private TableViewer createTestCriteriaTableViewer(SashForm testForm, String groupName) {
		Group varGroup = new Group(testForm, SWT.NONE);
		varGroup.setText(groupName);
		varGroup.setLayout(new FillLayout());
		Table table = new Table(varGroup, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 30;
		table.setLayoutData(data);
		String[] columnNames = new String[] { "TargetClass", "LINE", "BRANCH", "METHOD", "Avg" };
		int[] columnWidths = new int[] { 500, 100, 100, 100, 100 };
		for (int i = 0; i < columnNames.length; i++) {
			TableColumn tableColumn = new TableColumn(table, SWT.LEFT);
			tableColumn.setText(columnNames[i]);
			tableColumn.setWidth(columnWidths[i]);
		}
		return new TableViewer(table);
	}

	private void createTestCriteriaContent(List<Map<String, String>> criteriaMapList) {
		testCriteriaTableViewer.setContentProvider(ArrayContentProvider.getInstance());
		testCriteriaTableViewer.setLabelProvider(new TestCriteriaProvider());
		testCriteriaTableViewer.setInput(criteriaMapList);
		testCriteriaTableViewer.refresh(true);
	}

	public void updateTestCriteria(TestController testController) {
		this.testController = testController;
		for (Control control : testForm.getChildren()) {
			control.dispose();
		}
		testCriteriaTableViewer = createTestCriteriaTableViewer(testForm, "New Test Criteria: ");
		Map<String, String> criteriaMap = testController.getCoverageCriteria();
		System.out.println(criteriaMap.size());
		criteriaMap.put("TargetClass", testController.getClazz());
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		list.add(criteriaMap);
		createTestCriteriaContent(list);
		testForm.setWeights(new int[] { 5 });
	}

	// 面板3：测试结果

	@Override
	public void setFocus() {
	}

}
