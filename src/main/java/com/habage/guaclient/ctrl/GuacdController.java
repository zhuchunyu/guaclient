package com.habage.guaclient.ctrl;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/")
@Slf4j
public class GuacdController {

    @RequestMapping("/guacd")
    public String index() {
        return "index";
    }

    @RequestMapping("/ssh")
    public String ssh() {
        return "client";
    }

    @RequestMapping("/socket")
    public String web() {
        return "websocket";
    }
}
