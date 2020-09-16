package com.mashibing.springboot.controller;

import com.github.pagehelper.PageInfo;
import com.mashibing.springboot.entity.Account;
import com.mashibing.springboot.service.AccountService;
import com.mashibing.springboot.service.PermissionService;
import com.mashibing.springboot.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Auther: hhr
 * @Date: 2020/9/15 - 09 - 15 - 17:29
 * @Description: com.mashibing.springboot.controller
 * @version: 1.0
 */
@Controller
@RequestMapping("/manager")
public class ManagerController {
    @Autowired
    AccountService accountSrv;

    @Autowired
    PermissionService permissionSrv;

    @Autowired
    RoleService roleSrv;


    @RequestMapping("accountList")
    public  String accountList(@RequestParam(defaultValue = "1") int pageNum,@RequestParam(defaultValue = "5" ) int pageSize,Model model) {

        PageInfo<Account>page = accountSrv.findByPage(pageNum,pageSize);
        model.addAttribute("page", page);

        return "manager/accountList";
    }


    @RequestMapping("permissionList")
    public  String permissionList(@RequestParam(defaultValue = "1") int pageNum,@RequestParam(defaultValue = "5" ) int pageSize,Model model) {

        PageInfo<Account>page = permissionSrv.findByPage(pageNum,pageSize);
        model.addAttribute("page", page);
        return "manager/permissionList";
    }

   /* @RequestMapping("permissionModify")
    public  String permissionModify(@RequestParam int id,Model model) {

        Permission permission = permissionSrv.findById(id);

        model.addAttribute("p", permission);
        return "manager/permissionModify";
    }*/

    @RequestMapping("permissionAdd")
    public  String permissionAdd(Model model) {


        return "manager/permissionModify";
    }

    @RequestMapping("roleList")
    public  String roleList(@RequestParam(defaultValue = "1") int pageNum,@RequestParam(defaultValue = "5" ) int pageSize,Model model) {

        PageInfo<Account>page = roleSrv.findByPage(pageNum,pageSize);
        model.addAttribute("page", page);
        return "manager/roleList";
    }

}
