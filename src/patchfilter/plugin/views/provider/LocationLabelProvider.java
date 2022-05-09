package patchfilter.plugin.views.provider;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import patchfilter.util.LocationInfo;
import patchfilter.util.TestInfo;

public class LocationLabelProvider implements ITableLabelProvider {

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
		if (element instanceof LocationInfo) {
			LocationInfo locationLine = (LocationInfo) element;
			switch (index) {
			case 2:
				String line = locationLine.getModifyMethod();
				return line;
			case 3:
				int patchNum = locationLine.getPatchList().size();
				return patchNum + "";
			}
		}
		return null;
	}

}
