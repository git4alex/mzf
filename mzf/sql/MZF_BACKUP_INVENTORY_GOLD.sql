IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[MZF_BACKUP_INVENTORY_GOLD]') AND type in (N'P', N'PC'))
    DROP PROCEDURE [dbo].[MZF_BACKUP_INVENTORY_GOLD]
GO

CREATE PROC [dbo].[MZF_BACKUP_INVENTORY_GOLD] AS
declare @tableName varchar(100),  @sql varchar(800)
set @tableName = 'HIST_INVENTORY_GOLD_'+replace(convert(varchar(7),getDate(),23),'-','')

if object_id(@tableName,'u') is not null begin
    set @sql = 'DELETE FROM '+@tableName+' where cdate = '''+convert(varchar(10),getDate(),23)+''''
    exec(@sql)
    set @sql = 'INSERT INTO '+@tableName+'(TARGET_ID,TARGET_NUM,COST,GOLD_CLASS,WEIGHT,SOURCE,SOURCE_ID,CDATE)'
               +' SELECT i.TARGET_ID,r.NUM as TARGET_NUM,r.COST,r.GOLD_CLASS,i.QUANTITY as WEIGHT,r.SOURCE_ID,r.SOURCE,convert(varchar(10),getDate(),23) as CDATE'
               +' FROM MZF_INVENTORY i inner join MZF_RAWMATERIAL r on i.TARGET_ID = r.ID'
               +' WHERE i.TARGET_TYPE=''rawmaterial'''
               +' AND i.STORAGE_TYPE=''rawmaterial_gold'''
end
else begin
    set @sql = 'SELECT i.TARGET_ID,r.NUM as TARGET_NUM,r.COST,r.GOLD_CLASS,i.QUANTITY as WEIGHT,r.SOURCE_ID,r.SOURCE,convert(varchar(10),getDate(),23) as CDATE'
               +' INTO '+@tableName
               +' FROM MZF_INVENTORY i inner join MZF_RAWMATERIAL r on i.TARGET_ID = r.ID'
               +' WHERE i.TARGET_TYPE=''rawmaterial'''
               +' AND i.STORAGE_TYPE=''rawmaterial_gold'''
end

exec(@sql)

GO