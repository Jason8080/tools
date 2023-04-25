package cn.gmlee.tools.base.mod;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class User implements Serializable {
	private String name;
	private List<User> partners;
}