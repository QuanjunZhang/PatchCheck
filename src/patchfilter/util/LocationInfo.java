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

public class LocationInfo {
	private String modifyMethod;
	private StateType stateType;
	private List<Patch> patchList = new ArrayList<Patch>();

	public String toString() {
		return "modifyMethod: " + modifyMethod;
	}
}
