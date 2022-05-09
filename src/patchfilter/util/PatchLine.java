package patchfilter.util;

import lombok.Data;
import patchfilter.model.util.FileIO;
import patchfilter.configuration.Constant;
import patchfilter.model.entity.Project;

@Data

public class PatchLine {
	private String patchNameString;
	private Project subject;

	public PatchLine(String patchNameString, Project subject) {
		this.patchNameString = patchNameString;
		this.subject = subject;
	}

	public void initFormatedPatch() {
		String patchFile = Constant.AllPatchPath + "/" + subject.getName() + "/" + subject.getId() + "/"
				+ patchNameString;
	}
}
