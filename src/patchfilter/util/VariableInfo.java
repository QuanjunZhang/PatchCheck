package patchfilter.util;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import patchfilter.model.entity.Patch;
import patchfilter.model.util.StateType;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VariableInfo {
	private String varName;
	private String initialValue;
	private String patchedValue;
	private String type;
	private List<Patch> patchFiles = new ArrayList<Patch>();

	public String toString() {
		return "varName = " + varName + ", value = " + initialValue;
	}
}
