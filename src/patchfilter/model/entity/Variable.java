package patchfilter.model.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j

public class Variable {
	private JSONObject jsonObject;
	private String name;
	private String PatchFileName;

	public Variable(JSONObject jsonObject, String name) {
		this.jsonObject = jsonObject;
		this.name = name;
	}
}
