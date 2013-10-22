package com.zonrong.system.service;

import java.io.IOException;
import java.util.*;

import javax.annotation.Resource;

import com.zonrong.metadata.service.MetadataCRUDService;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.zonrong.core.dao.filter.Filter;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.core.security.User;
import com.zonrong.core.util.TreeBuilder;
import com.zonrong.entity.TreeConfig;
import com.zonrong.entity.code.EntityCode;
import com.zonrong.entity.code.TpltEnumEntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.entity.service.TreeEntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;

@Service
public class AppConfigService {

	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
	private TreeEntityService treeEntityService;
    @Resource
    private MetadataCRUDService metadataCRUDService;

	public Map<String, Object> getAppConfig(IUser user) throws BusinessException {
		Map<String, Object> config = new HashMap<String, Object>();
		config.put("user", user.getDataMap());
		config.put("menuList", getMenuList(user));
		config.put("bizCode", BizCodeService.getBizTypeMap());

		return config;
	}

	public IUser getUser(UserDetails uds) throws BusinessException {
		String userId = uds.getUsername();
		Map<String, Object> userMap = entityService.getById(TpltEnumEntityCode.USER, userId, User.getSystemUser());
		userMap.remove("password");

		List<String> perms = new ArrayList<String>();
		Collection<GrantedAuthority> gas = uds.getAuthorities();
		for(GrantedAuthority ga : gas){
			perms.add(ga.getAuthority());
		}
		userMap.put("perms", perms);
		userMap.put("roles", getRoleList(userId));

		String orgId = MapUtils.getString(userMap, "orgId");
		if(StringUtils.isNotBlank(orgId)){
			Map<String, Object> org = entityService.getById(TpltEnumEntityCode.ORG, orgId,User.getSystemUser());
			userMap.put("orgName", MapUtils.getString(org, "fullName"));
			userMap.put("orgCode", MapUtils.getString(org, "code"));
			userMap.put("orgBizCode", MapUtils.getString(org, "bizCode"));
			userMap.put("orgType", MapUtils.getString(org, "type"));
			userMap.put("orgPid", MapUtils.getString(org, "pid"));
		}

		return new User(userMap);
	}

	private List<String> getRoleList(String userId) throws BusinessException {
		List<String> roleList = new ArrayList<String>();

		List<Map<String,Object>> filterList=new ArrayList<Map<String,Object>>();
		Map<String,Object> roleFilter=new HashMap<String,Object>();
		roleFilter.put(EntityService.FIELD_CODE_KEY, "uid");
		roleFilter.put(EntityService.OPERATOR_KEY,Filter.EQ);
		roleFilter.put(EntityService.VALUE_KEY, userId);
		filterList.add(roleFilter);
		roleFilter=new HashMap<String,Object>();
		roleFilter.put(EntityService.FIELD_CODE_KEY, "urid");
		roleFilter.put(EntityService.OPERATOR_KEY,Filter.NOT_NULL);
		filterList.add(roleFilter);
		List<Map<String,Object>> roles=entityService.list(new EntityCode("userRoleView"), filterList, null, User.getSystemUser());
		if(roles!=null && roles.size()>0){
			for(Map<String,Object> role:roles){
				String str=MapUtils.getString(role, "code");
				if(StringUtils.isNotBlank(str)){
					roleList.add(str);
				}
			}
		}

		return roleList;
	}

	public List<Map<String, Object>> getMenuList(IUser user) throws BusinessException {
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put(TreeConfig.PID_CODE, "pid");
		parameter.put(TreeConfig.INDEX_CODE, "orderBy");
		final TreeConfig treeConfig = TreeConfig.getTreeConfig(parameter);

		Map<String, Object> queryParam = new HashMap<String, Object>();
		queryParam.put("userId", user.getId());

		final EntityMetadata metadata = metadataProvider.getEntityMetadata(new EntityCode("userMenu"));

		List list = treeEntityService.list(metadata, treeConfig, queryParam, user.asSystem());
		TreeBuilder b = new TreeBuilder(list) {
			public String getPid(Object item) {
				Map map = (Map) item;
				String s = MapUtils.getString(map, treeConfig.getPidCode());
				return s;
			}

			public String getId(Object item) {
				Map map = (Map) item;
				return MapUtils.getString(map, metadata.getPkCode());
			}
		};

		return b.getTree("-1");
	}

    /**
     * get config by type and ids
     *
     * @param params {type:[id1,id2],type2:[id1,id2]}
     * @return configs on json format
     */
    public String getJsonConfig(Map<String,Object> params){
        Map<String,Object> configMap = new HashMap<String,Object>();
        for(Iterator<String> keys=params.keySet().iterator();keys.hasNext();){
            String configType = keys.next();
            List ids = (List)params.get(configType);
            List<Map<String,Object>> configs = getConfigs(configType, ids);
            configMap.put(configType,configs);
        }

        String ret = "{}";
        ObjectMapper mapper = new ObjectMapper();
        try {
            ret = mapper.writeValueAsString(configMap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    private List<Map<String,Object>> getConfigs(String type, List ids){
        List<Map<String,Object>> ret = new ArrayList<Map<String, Object>>();
        if("Module".equalsIgnoreCase(type)){
            Filter f = Filter.field("id").in(ids);
            try {
                ret = entityService.list(new EntityCode("Module"),f,null,User.getSystemUser());
            } catch (BusinessException e) {
                e.printStackTrace();
            }
        }else if("Entity".equalsIgnoreCase(type)){
            for(Object id:ids){
                try {
                    Map<String,Object> entityMap = new HashMap<String,Object>();
                    entityMap =metadataCRUDService.getEntityById((Integer) id);

                    Map<String,Object> params = new HashMap<String, Object>();
                    params.put("entityId",id);
                    List<Map<String,Object>> fields = metadataCRUDService.listField(params);
                    entityMap.put("fields",fields);
                    ret.add(entityMap);
                } catch (BusinessException e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    };

    /**
     * update config from uploaded file
     *
     * @param configMap format:{type:[{config1},{config2}],type2:[{config1},{config2}]}
     */
    public void updateJsonConfig(Map<String,Object> configMap) throws BusinessException {
        for (String configType : configMap.keySet()) {
            List<Map<String, Object>> configs = (List<Map<String, Object>>) configMap.get(configType);
            for (Map<String, Object> config : configs) {
                updateConfig(configType, config);
            }
        }
    }

    private void updateConfig(String type,Map<String,Object> config) throws BusinessException {
        if("Module".equals(type)){
            String id = MapUtils.getString(config,"id");
            config.remove("id");
            EntityCode eCode = new EntityCode("Module");
            try {
                if(entityService.getById(eCode,id,User.getSystemUser())!=null){
                    entityService.updateById(eCode,id,config,User.getSystemUser());
                }else{
                    entityService.create(eCode,config,User.getSystemUser());
                }
            } catch (BusinessException e) {
                e.printStackTrace();
            }

        }else if("Entity".equals(type)){
            List<Map<String,Object>> fields = (List<Map<String,Object>>) config.get("fields");
            config.remove("fields");
            int id = MapUtils.getIntValue(config, "ID");
            config.remove("id");
            config.put("pid",-1);

            String code = MapUtils.getString(config,"code");

            Map<String,Object> entityMap = metadataCRUDService.getEntityByCode(new EntityCode(code));
            if(entityMap == null){
                metadataCRUDService.createEntity(config,User.getSystemUser());
                entityMap = metadataCRUDService.getEntityByCode(new EntityCode(code));
                id = MapUtils.getIntValue(entityMap,"id");
            }else{
                id = MapUtils.getIntValue(entityMap,"id");
                metadataCRUDService.updateEntityById(id,config,User.getSystemUser());
            }

            for(Map<String,Object> field:fields){
                field.put("entityId",id);
                field.remove("id");

                metadataCRUDService.createField(field,User.getSystemUser());
            }
        }
    }
}
