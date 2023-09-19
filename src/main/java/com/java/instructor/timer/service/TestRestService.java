package com.java.instructor.timer.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/list")
public class TestRestService  {

	@Autowired
	ApplicationContext context;	

	@GetMapping("/triggerJob")
	public String callJob() throws IOException {
		String currentPath = Paths.get("").toAbsolutePath().toString();
		String inputDir = currentPath + File.separator + "input";
		String fileGen = currentPath +File.separator + "src/main/resources"+File.separator + "a.xml";
		FileUtils.copyFileToDirectory(new File(fileGen), new File(inputDir));
		return "job executed";
	}
	


}