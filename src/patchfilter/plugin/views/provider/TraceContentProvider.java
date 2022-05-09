package patchfilter.plugin.views.provider;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;

import patchfilter.model.entity.LineInfo;

public class TraceContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getChildren(Object arg0) {
		return null;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List) {
			List<LineInfo> tracelineList = (List<LineInfo>) inputElement;
			Object[] elements = tracelineList.toArray(new LineInfo[0]);
			return elements;
		}
		return null;
	}

	@Override
	public Object getParent(Object arg0) {
		return null;
	}

	@Override
	public boolean hasChildren(Object arg0) {
		return false;
	}

}
