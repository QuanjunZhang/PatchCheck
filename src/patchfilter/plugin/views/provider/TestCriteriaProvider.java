package patchfilter.plugin.views.provider;

import java.util.Map;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

public class TestCriteriaProvider implements ITableLabelProvider {

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
		if (element instanceof Map<?, ?>) {
			Map<String, String> coverageCriteria = (Map<String, String>) element;
			switch (index) {
			case 0:
				String str = coverageCriteria.get("TargetClass");
				return str;
			case 1:
				String str1 = coverageCriteria.get("LINE");
				return str1;
			case 2:
				String str2 = coverageCriteria.get("BRANCH");
				return str2;
			case 3:
				String str3 = coverageCriteria.get("METHOD");
				return str3;
			case 4:
				String str4 = coverageCriteria.get("average");
				return str4;
			}
		}
		return null;
	}

}
