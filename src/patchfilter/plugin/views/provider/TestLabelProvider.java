package patchfilter.plugin.views.provider;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import patchfilter.util.TestInfo;

public class TestLabelProvider implements ITableLabelProvider {

	@Override
	public void addListener(ILabelProviderListener arg0) {

	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener arg0) {
	}

	@Override
	public Image getColumnImage(Object arg0, int arg1) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int index) {
		if (element instanceof TestInfo) {
			TestInfo testLine = (TestInfo) element;
			switch (index) {
			case 0:
				String line = testLine.getTestCaseString();
				return line;
			case 1:
				int patchNum = testLine.getRemainPatchNum();
				return patchNum + "";
			}
		}
		return null;
	}
}
