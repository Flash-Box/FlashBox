package com.drive.flashbox.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
	@GetMapping("/")
	public String redirectToPage() {	    
	    return "index";
	}
}
