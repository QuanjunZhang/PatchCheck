package patchfilter.plugin.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.bcel.internal.generic.NEW;

import patchfilter.configuration.Constant;
import patchfilter.controller.ProjectController;
import patchfilter.controller.TestController;
import patchfilter.controller.VariableController;
import patchfilter.model.entity.Patch;
import patchfilter.model.entity.Project;
import patchfilter.model.initialization.Initialization;
import patchfilter.model.run.Runner;
import patchfilter.model.service.GenerateService;
import patchfilter.model.service.InstallPatch4GenService;
import patchfilter.model.entity.LineInfo;
import patchfilter.model.entity.Method;
import patchfilter.model.util.BuildFilePath;
import patchfilter.model.util.FileIO;
import patchfilter.model.util.StateType;
import patchfilter.plugin.views.provider.TestLabelProvider;
import patchfilter.util.TestFile;
import patchfilter.util.LocationInfo;
import patchfilter.util.TechNameMap;
import patchfilter.util.TestInfo;
import patchfilter.util.VariableInfo;
//import patchfilter.util.TraceLine;
import sun.util.logging.resources.logging;

//import clonepedia.views.codesnippet.SnippetInstanceRelation;

public class ProjectView extends ViewPart {

	private TableViewer testTableViewer;
	private TableViewer locationTableViewer;

	private FormToolkit toolkit;
	private ScrolledForm CandidatePatchListForm;
	private ScrolledForm testResultForm;

	private ProjectController projectController;
	private TestController testController;
	private VariableController variableController;

	private Project project;
	private static final int MAX_PATCH_NUM = 5;
	private static Random random = new Random();

	private static String selectType = "location";
	private static String selectTraceLine = "";

	public boolean isGen = false;

	@Override
	public void createPartControl(Composite parent) {
		System.out.println("Subject View Start! ");
		if (createSubject()) {
			toolkit = new FormToolkit(parent.getDisplay());

			Form form = toolkit.createForm(parent);
			form.setLayoutData(new GridData(500, 50));
			form.setText(project.getName() + "-" + project.getId());

			GridLayout parentLayout = new GridLayout(1, true);
			parent.setLayout(parentLayout);

			createSubjectInfo(parent);// 基础信息
			createButtons(parent);// 重置数据
			createTestResult(parent);// 测试结果
			createTestBody(parent);// 错误测试用例信息
			createLocationBody(parent);// 位置信息
			createPatchBody(parent);// 补丁信息
		}
	}

	protected boolean createSubject() {
		IProject currentProject = getCurrentSelectedProject();
		if (currentProject != null) {
			String projectName = currentProject.getName();
			project = new Project(projectName.split("_")[0], Integer.parseInt(projectName.split("_")[1]));
			System.out.println("current select project: " + project.toString());
			project.initPatchListByPath(Constant.AllPatchPath);
			projectController = new ProjectController(project);
			projectController.initSet();
			return true;
		} else {
			project = new Project("Math", 2);
			System.out.println("current default project: " + project.toString());
			project.initPatchListByPath(Constant.AllPatchPath);
			projectController = new ProjectController(project);
			projectController.initSet();
			return true;
		}
		// return false;
	}

	private IProject getCurrentSelectedProject() {
		IProject project = null;
		ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		ISelection selection = selectionService.getSelection();
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof IResource) {
				project = ((IResource) element).getProject();
			} else if (element instanceof PackageFragmentRoot) {
				IJavaProject jProject = ((PackageFragmentRoot) element).getJavaProject();
				project = jProject.getProject();
			} else if (element instanceof IJavaElement) {
				IJavaProject jProject = ((IJavaElement) element).getJavaProject();
				project = jProject.getProject();
			}
		}
		return project;
	}

	// 面板1：基础信息
	protected void createSubjectInfo(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());
		composite.setLayoutData(new GridData(500, 75));
		org.eclipse.swt.widgets.List list = new org.eclipse.swt.widgets.List(composite, SWT.BORDER);
		list.add(project.toString());
		list.add("Subject Path: " + project.getHome());
		list.add("Totol Patch Number: " + project.getPatchList().size());
	}

	// 面板1：按钮
	protected void createButtons(Composite parent) {
		Group feedbackGroup = new Group(parent, SWT.NONE);
		feedbackGroup.setLayoutData(new GridData(500, 50));
		feedbackGroup.setOrientation(SWT.HORIZONTAL);
		GridLayout gridLayout = new GridLayout(3, true);
		gridLayout.makeColumnsEqualWidth = false;
		gridLayout.marginWidth = 1;
		feedbackGroup.setLayout(gridLayout);
		// 重置数据
		Button resetButton = new Button(feedbackGroup, SWT.NONE);
		resetButton.setText("Reset");
		resetButton.setLayoutData(new GridData(150, 30));
		resetButton.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent arg0) {
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				projectController.restoreList();
				if (locationTableViewer != null) {
					locationTableViewer.refresh();
				}
				if (testTableViewer != null) {
					testTableViewer.refresh();
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
			}
		});
		// 测试项目
		Button testButton = new Button(feedbackGroup, SWT.NONE);
		testButton.setText("Test");
		testButton.setLayoutData(new GridData(150, 30));
		testButton.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent arg0) {
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				if (!isGen) {
					showMessage("Please Generate Tests first! ");
				} else {
					testController.testProject();
					updateTestResult(testController);
					isGen = false;
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
			}
		});
		// 完成
		Button doneButton = new Button(feedbackGroup, SWT.NONE);
		doneButton.setText("Done");
		doneButton.setLayoutData(new GridData(150, 30));
		doneButton.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent arg0) {
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				// TODO
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
			}
		});
	}

	// 面板1：测试结果
	protected void createTestResult(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		testResultForm = toolkit.createScrolledForm(parent);
		testResultForm.setLayoutData(new GridData(500, 75));
		testResultForm.setText("Test Result");
		testResultForm.setAlwaysShowScrollBars(true);
		TableWrapLayout tableLayout = new TableWrapLayout();
		tableLayout.numColumns = 1;
		testResultForm.getBody().setLayout(tableLayout);
		for (Control control : testResultForm.getBody().getChildren()) {
			control.dispose();
		}
		parent.redraw();
		FormText text = toolkit.createFormText(testResultForm.getBody(), true);
		text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		text.setWhitespaceNormalized(false);
		text.setText("test result", false, true);
	}

	public void updateTestResult(TestController testController) {
		for (Control control : testResultForm.getBody().getChildren()) {
			control.dispose();
		}

		Composite body = testResultForm.getBody();
		Section section = toolkit.createSection(body, Section.TWISTIE | Section.EXPANDED | Section.TITLE_BAR);
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		section.setExpanded(true);
		section.setLayout(new TableWrapLayout());
		FormText text = toolkit.createFormText(section, true);
		text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		text.setWhitespaceNormalized(false);

		List<String> testMessage = testController.getMessage();
		StringBuilder stringBuilder = new StringBuilder();
		for (String s : testMessage) {
			stringBuilder.append(s).append("\n");
		}
		System.out.println(stringBuilder.toString());

		try {
			text.setText(stringBuilder.toString(), false, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		section.setClient(text);

		testResultForm.getBody().redraw();
		testResultForm.getBody().update();
	}

	// 面板2：触发错误的测试
	protected void createTestBody(Composite parent) {
		SashForm testForm = new SashForm(parent, SWT.VERTICAL);
		GridData gridData = new GridData(500, 100);
		testForm.setLayoutData(gridData);
		testTableViewer = createTestTableViewer(testForm, "Failed Test: ");
		createTestContent(projectController.getFailTestList());
		testForm.setWeights(new int[] { 5 });
		testTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection != null && selection.getFirstElement() != null) {
					System.out.println("Now selecting " + selection.getFirstElement());
					String failingTest = ((TestInfo) selection.getFirstElement()).getTestCaseString();
					openFile2Line(testPatchTransform(failingTest));
					updateCandidatePatchViews(projectController.getCurrentPatchList());
				}
			}
		});
	}

	private TableViewer createTestTableViewer(SashForm testForm, String groupName) {
		Group varGroup = new Group(testForm, SWT.NONE);
		varGroup.setText(groupName);
		varGroup.setLayout(new FillLayout());
		Table table = new Table(varGroup, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 30;
		table.setLayoutData(data);
		String[] columnNames = new String[] { "TestCase", "Remain Patches" };
		int[] columnWidths = new int[] { 300, 100 };
		for (int i = 0; i < columnNames.length; i++) {
			TableColumn tableColumn = new TableColumn(table, SWT.LEFT);
			tableColumn.setText(columnNames[i]);
			tableColumn.setWidth(columnWidths[i]);
		}
		return new TableViewer(table);
	}

	private void createTestContent(List<TestInfo> testlist) {
		testTableViewer.setContentProvider(ArrayContentProvider.getInstance());
		testTableViewer.setLabelProvider(new TestLabelProvider());
		testTableViewer.setInput(testlist);
		testTableViewer.refresh(true);
	}

	// 面板2：错误位置
	protected void createLocationBody(Composite parent) {
		SashForm locationForm = new SashForm(parent, SWT.VERTICAL);
		GridData gridData = new GridData(500, 100);
		locationForm.setLayoutData(gridData);
		this.locationTableViewer = createLocationTableViewer(locationForm, "Location Infomation: ");
		locationForm.setWeights(new int[] { 5 });
		locationTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection != null && selection.getFirstElement() != null) {
					String line = ((LocationInfo) selection.getFirstElement()).getModifyMethod();
					List<Patch> formatedPatchFiles = ((LocationInfo) selection.getFirstElement()).getPatchList();
					// openFile(obtainMethodLine(formatedPatchFiles));
					updateCandidatePatchViews(formatedPatchFiles);
					showDiffWithOriginal(formatedPatchFiles);
				}
			}
		});
	}

	private TableViewer createLocationTableViewer(SashForm locationForm, String groupName) {
		Group varGroup = new Group(locationForm, SWT.NONE);
		varGroup.setText(groupName);
		varGroup.setLayout(new FillLayout());
		TableViewer viewer = new TableViewer(varGroup);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		viewer.setContentProvider(new ArrayContentProvider());
		// Location
		TableColumn column = new TableColumn(viewer.getTable(), SWT.LEFT);
		column.setText("Bug Location");
		column.setWidth(300);
		TableViewerColumn modLineColumn = new TableViewerColumn(viewer, column);
		modLineColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof LocationInfo) {
					LocationInfo locationLine = (LocationInfo) element;
					String location = locationLine.getModifyMethod();
					String fileName = location.split("#")[0];
					String methodName = location.split("#")[2];
					return fileName + "#" + methodName;
				}
				return null;
			}
		});
		// patchNum
		column = new TableColumn(viewer.getTable(), SWT.LEFT);
		column.setText("Remain Patches" );
		column.setWidth(200);
		TableViewerColumn patchNumColumn = new TableViewerColumn(viewer, column);
		patchNumColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof LocationInfo) {
					LocationInfo locationLine = (LocationInfo) element;
					int patchNum = locationLine.getPatchList().size();
					return patchNum + "";
				}
				return null;
			}
		});
		viewer.setInput(projectController.getLocationInfoList());
		return viewer;
	}

	// 面板3：候选补丁列表
	protected void createPatchBody(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		CandidatePatchListForm = toolkit.createScrolledForm(parent);
		CandidatePatchListForm.setLayoutData(new GridData(500, 300));
		CandidatePatchListForm.setText("Candidate Patch List");
		CandidatePatchListForm.setAlwaysShowScrollBars(true);
		TableWrapLayout tableLayout = new TableWrapLayout();
		tableLayout.numColumns = 1;
		CandidatePatchListForm.getBody().setLayout(tableLayout);
		for (Control control : CandidatePatchListForm.getBody().getChildren()) {
			if (control instanceof Section) {
				control.dispose();
			}
			if (control instanceof Button) {
				control.dispose();
			}
			if (control instanceof Composite) {
				control.dispose();
			}
		}
		parent.redraw();
		List<Patch> currentPatchList = projectController.getCurrentPatchList();
		HashSet<String> patchKindSet = new HashSet<String>();
		for (Patch patchFile : currentPatchList) {
			String patchKind = patchFile.getPatchName().split("-")[0];
			if (!patchKindSet.contains(patchKind)) {
				patchKindSet.add(patchKind);
				createCodeSection(CandidatePatchListForm.getBody(), patchFile);
			}
		}
	}

	private void createCodeSection(Composite body, Patch patch) {
		Composite actions = toolkit.createComposite(body);
		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.marginWidth = 1;
		actions.setLayout(gridLayout);
		actions.setOrientation(SWT.HORIZONTAL);
		testController = new TestController(patch);
		// delete button: delete a patch and refresh relative view and data
		Button deleteButton = toolkit.createButton(actions, "Delete", SWT.BUTTON1);
		deleteButton.addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				projectController.deleteCandidatePatches(patch);
				projectController.updateTestInfoList();
				if (locationTableViewer != null) {
					locationTableViewer.refresh();
				}
				if (testTableViewer != null) {
					testTableViewer.refresh();
				}
				updateCandidatePatchViews(projectController.getCurrentPatchList());
			}

			@Override
			public void mouseUp(MouseEvent arg0) {
			}
		});
		// Generate button: install patch and generate tests
		Button genButton = toolkit.createButton(actions, "Generate", SWT.BUTTON1);
		genButton.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent arg0) {
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				testController.genNewTest();
				isGen = true;
				updateTestCriteria();
				List<String> newTestList = testController.getNewTestList();
				for (String newTest : newTestList) {
					openFile(newTest);
				}
				// String
				// newTest="D:\\Graduation\\Dataset\\Projects\\Math\\Math_41_buggy\\evosuite-tests\\org\\apache\\commons\\math\\stat\\descriptive\\moment\\Mean_ESTest.java";
				// openFile(newTest);
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
			}
		});
		// Select button
		Button seButton = toolkit.createButton(actions, "Select", SWT.BUTTON1);
		seButton.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent arg0) {
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				showMessage("You have chosen " + patch.getPatchName() + " as the final patch!\n"
						+ "The interactive job is finished! ");
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
			}
		});
		// code section head
		Section section = toolkit.createSection(body, Section.TWISTIE | Section.EXPANDED | Section.TITLE_BAR);
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		section.setExpanded(false);
		section.setLayout(new TableWrapLayout());
		section.setText(patch.getAliaName());
		section.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent arg0) {
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				String fileName = patch.getFixedFile();
				int fixedLine = patch.getModifyLine() - 1;
				System.out.println("Section mouseDown " + fileName + "#" + fixedLine);
				openFile2Line(fileName + "#" + fixedLine);
				showDiffWithOriginal(Collections.singletonList(patch));
				showVariableDiff(patch);
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
			}
		});
		// code section
		FormText text = toolkit.createFormText(section, true);
		text.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent arg0) {
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				String fileName = patch.getFixedFile();
				int fixedLine = patch.getModifyLine() - 1;
				System.out.println("Text mouseDown" + fileName + "#" + fixedLine);
				openFile2Line(fileName + "#" + fixedLine);
				showDiffWithOriginal(Collections.singletonList(patch));
				showVariableDiff(patch);
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
			}
		});
		FormColors colors = toolkit.getColors();
		colors.createColor("Delete", colors.getSystemColor(SWT.COLOR_RED));
		text.setColor("Delete", colors.getColor("Delete"));
		colors.createColor("Add", colors.getSystemColor(SWT.COLOR_BLUE));
		text.setColor("Add", colors.getColor("Add"));
		colors.createColor("Common", colors.getSystemColor(SWT.COLOR_BLACK));
		text.setColor("Common", colors.getColor("Common"));
		text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		text.setWhitespaceNormalized(false);

		List<String> contentList = new LinkedList<String>();
		String patchContent = patch.getPatchContent();
		int lineNumber = patch.getStartLine();
		contentList.add("<form><p>");
		for (String line : patchContent.split("\n")) {
			if (line.contains("&")) {
				line = line.replaceAll("&", "&amp;");
			}
			if (line.contains("<")) {
				line = line.replaceAll("<", "&lt;");
			}
			if (line.contains(">")) {
				line = line.replaceAll(">", "&gt;");
			}
			if (line.startsWith("+")) {
				line = "<span color=\"Add\">" + String.valueOf(lineNumber - 1) + line + "</span><br/>";
				contentList.add(line);
			} else if (line.startsWith("-")) {
				line = "<span color=\"Delete\">" + String.valueOf(lineNumber) + line + "</span><br/>";
				contentList.add(line);
			} else {
				line = "<span color=\"Common\">" + String.valueOf(lineNumber) + line + "</span><br/>";
				contentList.add(line);
			}
			lineNumber++;
		}
		contentList.add("</p></form>");

		try {
			text.setText(contentList.stream().collect(Collectors.joining()), true, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		section.setClient(text);
	}

	private void updateCandidatePatchViews(List<Patch> formatedPatchFiles) {
		System.out.println("Now FormattedPatchFile: " + formatedPatchFiles.size());
		for (Control control : CandidatePatchListForm.getBody().getChildren()) {
			if (control instanceof Section) {
				control.dispose();
			}
			if (control instanceof Button) {
				control.dispose();
			}
			if (control instanceof Composite) {
				control.dispose();
			}
		}
		HashSet<String> patchSet = new HashSet<String>();
		if (formatedPatchFiles.size() <= MAX_PATCH_NUM) {
			formatedPatchFiles.forEach(
					formatedPatchFile -> createCodeSection(CandidatePatchListForm.getBody(), formatedPatchFile));
		} else {
			Map<String, String> patchTraceMap = getPatchTraceMap(formatedPatchFiles);
			Map<String, List<String>> traceKindMap = patchTraceMap.entrySet().stream()
					.filter(entry -> Objects.nonNull(entry.getKey()) && Objects.nonNull(entry.getValue()))
					.collect(Collectors.groupingBy(Map.Entry<String, String>::getValue,
							Collectors.mapping(Map.Entry<String, String>::getKey, Collectors.toList())));
			Map<String, Patch> formatedPatchFileMap = formatedPatchFiles.stream()
					.collect(Collectors.toMap(Patch::getPatchName, Function.identity(), (v1, v2) -> v2));

			traceKindMap.entrySet().stream().filter(entry -> CollectionUtils.isNotEmpty(entry.getValue()))
					.forEach(entry -> {
						List<Patch> currentPatchFileList = entry.getValue().stream().filter(StringUtils::isNotBlank)
								.map(formatedPatchFileMap::get).filter(Objects::nonNull).collect(Collectors.toList());
						Patch formatedPatchFile = currentPatchFileList.get(0);
						patchSet.add(formatedPatchFile.getPatchName());
						createCodeSection(CandidatePatchListForm.getBody(), formatedPatchFile);
					});
			if (patchSet.size() < MAX_PATCH_NUM) {
				formatedPatchFiles.stream()
						.filter(formatedPatchFile -> !patchSet.contains(formatedPatchFile.getPatchName()))
						.limit(MAX_PATCH_NUM - patchSet.size())
						.forEach(formatedPatchFile -> createCodeSection(CandidatePatchListForm.getBody(),
								formatedPatchFile));
			}
		}
		CandidatePatchListForm.getBody().redraw();
		CandidatePatchListForm.getBody().update();
	}

	private Map<String, String> getPatchTraceMap(List<Patch> patchFileList) {
		if (CollectionUtils.isEmpty(patchFileList)) {
			return Collections.emptyMap();
		}
		Map<String, String> patchTraceMap = new HashMap<String, String>();
		for (Patch patchFile : patchFileList) {
			if (Objects.isNull(patchFile)) {
				continue;
			}
			String traceFile = BuildFilePath.tmpMapTraceLine(patchFile.getPatchName(), patchFile.getSubject());
			if (StringUtils.isEmpty(traceFile)) {
				continue;
			}
			List<String> patchFileTraceList = Arrays.asList(FileIO.readFileToString(traceFile).split("\n"));
			patchTraceMap.put(patchFile.getPatchName(),
					patchFileTraceList.stream().filter(StringUtils::isNotEmpty).collect(Collectors.joining()));
		}
		return patchTraceMap;
	}

	private void openFile2Line(String fileName) {
		String filePath = fileName.split("#")[0].contains("$") ? fileName.split("#")[0].split("\\$")[0] + ".java"
				: fileName.split("#")[0];
		System.out.println(filePath);
		int line = Integer.valueOf(fileName.split("#")[1]);
		final IFile inputFile = ResourcesPlugin.getWorkspace().getRoot()
				.getFileForLocation(Path.fromOSString(filePath));
		if (inputFile != null) {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IEditorPart openEditor;
			try {
				openEditor = IDE.openEditor(page, inputFile);
				if (openEditor instanceof ITextEditor) {
					ITextEditor textEditor = (ITextEditor) openEditor;
					IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
					textEditor.selectAndReveal(document.getLineOffset(line - 1), document.getLineLength(line - 1));
				}
			} catch (BadLocationException | CoreException e) {
				e.printStackTrace();
			}
		}
	}

	private void openFile(String filePath) {
		final IFile inputFile = ResourcesPlugin.getWorkspace().getRoot()
				.getFileForLocation(Path.fromOSString(filePath));
		if (inputFile != null) {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IEditorPart openEditor;
			try {
				openEditor = IDE.openEditor(page, inputFile);
				if (openEditor instanceof ITextEditor) {
					ITextEditor textEditor = (ITextEditor) openEditor;
					IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
					textEditor.selectAndReveal(document.getLineOffset(0), document.getLineLength(0));
				}
			} catch (BadLocationException | CoreException e) {
				e.printStackTrace();
			}
		}
	}

	private void showDiffWithOriginal(Patch patch) {
		PatchView viewpart = (PatchView) getSite().getWorkbenchWindow().getActivePage()
				.findView("patchfilter.plugin.views.PatchView");
		if (viewpart != null) {
			viewpart.updateCodeDiffForm(patch);
		}
	}

	private void showDiffWithOriginal(List<Patch> patchFiles) {
		PatchView viewpart = (PatchView) getSite().getWorkbenchWindow().getActivePage()
				.findView("patchfilter.plugin.views.PatchView");
		if (viewpart != null) {
			viewpart.updateCodeDiffForm(patchFiles);
		}
	}

	private void showVariableDiff(Patch patch) {
		variableController = new VariableController();
		variableController.getVarChange(patch);
		PatchView viewpart = (PatchView) getSite().getWorkbenchWindow().getActivePage()
				.findView("patchfilter.plugin.views.PatchView");
		if (viewpart != null) {
			viewpart.updateVariableView(variableController.getVariableInfoList());
		}
	}

	private void updateTestCriteria() {
		PatchView viewpart = (PatchView) getSite().getWorkbenchWindow().getActivePage()
				.findView("patchfilter.plugin.views.PatchView");
		if (viewpart != null) {
			viewpart.updateTestCriteria(testController);
		}
	}

	private String tracePathTransform(String relativePath) {
		String filePath = relativePath.split("#")[0];
		int line = Integer.valueOf(relativePath.split("#")[relativePath.split("#").length - 1]);
		String absolutePath = project.getHome() + "/" + project.getSsrc() + "/" + filePath.replace(".", "/") + ".java";
		return absolutePath + "#" + line;
	}

	private String obtainMethodLine(List<Patch> patchFiles) {
		Patch patchFile = patchFiles.get(0);
		String patchMethodFile = Constant.CACHE + patchFile.getSubject().getName() + "/"
				+ patchFile.getSubject().getId() + "/" + patchFile.getPatchName() + "_method";
		Method method = JSONObject.parseObject(FileIO.readFileToString(patchMethodFile), Method.class);
		return method.getFixedFile() + "#" + method.getStartLine();
	}

	private String testPatchTransform(String relativePath) {
		String filePath = relativePath.split("::")[0];
		String testCase = relativePath.split("::")[1];
		String absolutePath = project.getHome() + "/" + project.getTsrc() + "/" + filePath.replace(".", "/") + ".java";
		TestFile linenumber = new TestFile(project, absolutePath);
		linenumber.parseFile();
		int lineNum = linenumber.getLine(testCase);
		return absolutePath + "#" + lineNum;
	}

	public void showMessage(String message) {
		MessageBox box = new MessageBox(PlatformUI.getWorkbench().getDisplay().getActiveShell());
		box.setMessage(message);
		box.open();
	}

	@Override
	public void setFocus() {
	}

}
