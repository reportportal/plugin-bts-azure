package com.epam.reportportal.extension.example.entity.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

public class CreateEntity implements Serializable {

	@NotNull
	@Size(min = 1, max = 256)
	@JsonProperty(value = "name")
	private String name;

	@NotNull
	@Size(min = 1, max = 256)
	@JsonProperty(value = "description")
	private String description;

	public CreateEntity() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
