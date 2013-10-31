IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[MZF_BACKUP_INVENTORY_MATERIAL]') AND type in (N'P', N'PC'))
    DROP PROCEDURE [dbo].[MZF_BACKUP_INVENTORY_MATERIAL]
GO

CREATE PROC [dbo].[MZF_BACKUP_INVENTORY_MATERIAL] AS
declare @tableName varchar(100),  @sql varchar(800)
set @tableName = 'HIST_INVENTORY_MATERIAL_'+replace(convert(varchar(7),getDate(),23),'-','')

if object_id(@tableName,'u') is not null begin
    set @sql = 'DELETE FROM '+@tableName+' where cdate = '''+convert(varchar(10),getDate(),23)+''''
    exec(@sql)
    set @sql = 'INSERT INTO '+@tableName+'(ORG_ID,TARGET_ID,TARGET_NUM,NAME,SPEC,QUANTITY,COST,TYPE,UNIT,WHOLESALE_PRICE,RETAIL_PRICE,CDATE)'
               +' SELECT i.ORG_ID,i.TARGET_ID,r.NUM as TARGET_NUM,r.NAME,r.SPEC,i.QUANTITY,r.COST,r.TYPE,r.UNIT,r.WHOLESALE_PRICE,r.RETAIL_PRICE,convert(varchar(10),getDate(),23) as CDATE'
               +' FROM MZF_INVENTORY i inner join MZF_MATERIAL r on i.TARGET_ID = r.ID'
               +' WHERE i.TARGET_TYPE=''material'''
end
else begin
    set @sql = 'SELECT i.ORG_ID,i.TARGET_ID,r.NUM as TARGET_NUM,r.NAME,r.SPEC,i.QUANTITY,r.COST,r.TYPE,r.UNIT,r.WHOLESALE_PRICE,r.RETAIL_PRICE,convert(varchar(10),getDate(),23) as CDATE'
               +' INTO '+@tableName
               +' FROM MZF_INVENTORY i inner join MZF_MATERIAL r on i.TARGET_ID = r.ID'
               +' WHERE i.TARGET_TYPE=''material'''
end

exec(@sql)

GO