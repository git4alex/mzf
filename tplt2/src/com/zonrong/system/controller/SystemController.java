package com.zonrong.system.controller;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.core.util.SessionUtils;
import com.zonrong.entity.acl.AclService;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.system.service.AppConfigService;
import com.zonrong.system.service.BizCodeService;
import com.zonrong.system.service.ModuleService;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * date: 2010-7-27
 * <p/>
 * version: 1.0 commonts: ......
 */
@Controller
public class SystemController {
    private Logger logger = Logger.getLogger(SystemController.class);

    @Resource
    private MetadataProvider metadataProvider;
    @Resource
    private BizCodeService bizCodeService;
    @Resource
    private AclService aclService;
    @Resource
    private ModuleService moduleService;
    @Resource
    private AppConfigService appConfigService;

    @RequestMapping(value = "/getAppConfig")
    @ResponseBody
    public Map getAppConfig(HttpServletRequest request) throws Exception {
        return SessionUtils.getAppContextConfig(request);
    }

    @RequestMapping(value = "/heartbeat")
    @ResponseBody
    public Map heartbeat(@RequestParam final int userId, final HttpServletRequest request) throws Exception {
        HttpTemplete templete = new HttpTemplete(request) {
            @Override
            protected void doSomething() throws BusinessException {
                IUser user = this.getUser();
                if (user.getId() != userId) {
//					throw new BusinessException("已经有人重新登录，请退出");
                }
                String add = request.getRemoteAddr();
                String msg = user.getOrgName() + ": " + user.getName() + "(" + add + ") says: \"my heart will go on\", ";
                logger.debug(msg);
                this.put("msg", msg);
            }
        };
        return templete.operate();
    }

    @RequestMapping(value = "/clearMetadataCache")
    @ResponseBody
    public Map clearMetadataCache() {
        OperateTemplete templete = new OperateTemplete() {
            protected void doSomething() throws BusinessException {
                metadataProvider.clearMetadataCache();
                this.put("msg", "元数据缓存已经全部清除");
            }
        };
        return templete.operate();
    }

    @RequestMapping(value = "/reloadBiz")
    @ResponseBody
    public Map reloadBiz() {
        OperateTemplete templete = new OperateTemplete() {
            protected void doSomething() throws BusinessException {
                bizCodeService.load();
                this.put("msg", "加载成功");
            }
        };
        return templete.operate();
    }

    @RequestMapping(value = "/reloadAcl")
    @ResponseBody
    public Map reloadAcl() {
        OperateTemplete templete = new OperateTemplete() {
            protected void doSomething() throws BusinessException {
                aclService.reload();
                this.put("msg", "加载成功");
            }
        };
        return templete.operate();
    }

    @RequestMapping(value = "/module", method = RequestMethod.POST)
    @ResponseBody
    public Map createModule(@RequestBody final Map<String, Object> module, HttpServletRequest request) {
        OperateTemplete templete = new HttpTemplete(request) {
            protected void doSomething() throws BusinessException {
                String id = moduleService.createModule(module);
                this.put("id", id);
            }
        };
        return templete.operate();
    }

    @RequestMapping(value = "/module/{dbId}", method = RequestMethod.PUT)
    @ResponseBody
    public Map updateModule(final @PathVariable String dbId, final @RequestBody Map<String, Object> module, @RequestParam Map<String, Object> params, HttpServletRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug(params);
        }

        final boolean autoSave = MapUtils.getBooleanValue(params, "autoSave");
        OperateTemplete templete = new HttpTemplete(request) {
            protected void doSomething() throws BusinessException {
                if (autoSave) {
                    moduleService.doAutoSave(dbId, module);
                } else {
                    moduleService.updateModule(dbId, module);
                }
            }
        };
        return templete.operate();
    }

    @RequestMapping(value = "/module/{dbId}", method = RequestMethod.DELETE)
    @ResponseBody
    public Map deleteModule(final @PathVariable String dbId, HttpServletRequest request) {
        OperateTemplete templete = new HttpTemplete(request) {
            protected void doSomething() throws BusinessException {
                moduleService.deleteModule(dbId);
            }
        };
        return templete.operate();
    }

    @RequestMapping(value = "/clipboard", method = RequestMethod.GET)
    @ResponseBody
    public Map getClipboard(final HttpServletRequest request) {
        Map<String, String> ret = new HashMap<String, String>();
        HttpSession session = request.getSession(true);
        ret.put("data", ObjectUtils.toString(session.getAttribute("tplt_clipboard")));

        return ret;
    }

    @RequestMapping(value = "/clipboard", method = RequestMethod.PUT)
    @ResponseBody
    public Map setClipboard(final @RequestBody Map<String, Object> data, final HttpServletRequest request) {
        OperateTemplete templete = new HttpTemplete(request) {
            protected void doSomething() throws BusinessException {
                HttpSession session = request.getSession(true);
                session.setAttribute("tplt_clipboard", data.get("data"));
            }
        };
        return templete.operate();
    }

    /**
     * export system config
     *
     * @param params   {type:[ids]}
     * @param response httpServletResponse for the request
     */
    @RequestMapping(value = "/sysConfig", method = RequestMethod.GET)
    public void expConfig(@RequestParam(required = false) String params, HttpServletResponse response) {
        try {
            response.reset();
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=config.cfg");
            OutputStream os = response.getOutputStream();

            Map<String, Object> requestParam = new ObjectMapper().readValue(params, new TypeReference<Map<String, Object>>() {
            });
            String jsonConfig = appConfigService.getJsonConfig(requestParam);

            os.write(jsonConfig.getBytes("UTF-8"));
            os.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * import system config
     *
     * @param configFile format:{type:[{config1},{config2}],type2:[{config1},{config2}]}
     */
    @RequestMapping(value = "/sysConfig", method = RequestMethod.POST)
    public void impConfig(@RequestParam(required = false) final MultipartFile configFile,HttpServletResponse response) {
        OperateTemplete tpl = new OperateTemplete() {
            @Override
            protected void doSomething() throws BusinessException {
                try {
                    Map<String, Object> configMap = new ObjectMapper().readValue(configFile.getInputStream(), new TypeReference<Map<String, Object>>() {
                    });

                    appConfigService.updateJsonConfig(configMap);
                } catch (IOException e) {
                    throw new BusinessException(e.getMessage());
                }
            }
        };

        Map<String,Object> ret = tpl.operate();
        try {
            response.setContentType("text/html");
            response.getWriter().write(new ObjectMapper().writeValueAsString(ret));
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
    }
}
