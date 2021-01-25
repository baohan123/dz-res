package com.dz.dzim.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class IndexController {



    @RequestMapping(value = "/index1", method = RequestMethod.GET)
    public String index1(){
        return "index1";
    }

    @RequestMapping(value = "/index2", method = RequestMethod.GET)
    public String index2(){
        return "index2";
    }

    @RequestMapping(value = "/index3", method = RequestMethod.GET)
    public String index3(){
        return "index3";
    }


    @RequestMapping(value = "/mqIndex", method = RequestMethod.GET)
    public String mqIndex(){
        System.out.println("------------");
        return "mq_index";
    }
}
