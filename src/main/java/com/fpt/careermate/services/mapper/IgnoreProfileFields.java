package com.fpt.careermate.services.mapper;

import org.mapstruct.Mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
@Mapping(target = "candidateId", ignore = true)
@Mapping(target = "account", ignore = true)
@Mapping(target = "fullName", ignore = true)
@Mapping(target = "dob", ignore = true)
@Mapping(target = "gender", ignore = true)
@Mapping(target = "phone", ignore = true)
@Mapping(target = "address", ignore = true)
@Mapping(target = "image", ignore = true)
@Mapping(target = "title", ignore = true)
@Mapping(target = "link", ignore = true)
public @interface IgnoreProfileFields {
}

