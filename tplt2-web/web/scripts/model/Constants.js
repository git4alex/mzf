/**
 * 系统常量
 */
Constants = function() { 
	return{
		CONFIG:'config.json',
		ENTITY_CONFIG:'entity.json',
		FIELD_CONFIG:'field.json',
		OPER_CONFIG:'operate.json', 
		login: 'entity/user/login', 
		getAppConfig: 'getAppConfig.do',
		queryEntityForList:'entityMetadata/tree', 
		saveEntity:   'entityMetadata',
		updateEntity:'entityMetadata/',
		queryFieldForPage:'fieldMetadata',  
		saveField:'fieldMetadata',
		updateField: 'fieldMetadata/', 
		queryDataForTree: 'entity/tree/menu',
		saveTreeData:'entity/tree/menu',
		updateTreeData:'entity/tree/menu',
		deleteTreeData:'entity/tree/menu', 
		moveNode:'entity/tree/menu',
		queryListColumsByEntityId:'fieldMetadata/listColumnsByTableName/',   
		sysloggor:'entity/logSetting',
		orgmgr:'entity/tree/org',
		orgmgr_moveNode:'entity/tree/org',
		resmgr:'entity/resource',
		resourceAllocate:'entity/resourceAllocate',
		resourceAllocated:'entity/resourceAllocated',
		authmgr:'entity/tree/permission',
		permissionAllocate:'entity/tree/permissionAllocate',
		authmgr_moveNode:'entity/tree/permission',
		rolemgr:'entity/role',
		roleAllocate:'entity/roleAllocate',
		roleAllocated:'entity/roleAllocated',
		roleallotauth:'entity/rolePermission',
		usermgr:'entity/user',
		vendormgr:'entity/vendor', 
		bizcode:'entity/tree/bizCode',		
		biztype:'entity/tree/bizType', 
		allotres:'entity/permissionResource', 
		allotrole:'entity/userRole'		
	};
}(); 

