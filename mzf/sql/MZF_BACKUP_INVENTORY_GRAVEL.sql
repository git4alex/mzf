IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[MZF_BACKUP_INVENTORY_GRAVEL]') AND type in (N'P', N'PC'))
    DROP PROCEDURE [dbo].[MZF_BACKUP_INVENTORY_GRAVEL]
GO

CREATE PROC [dbo].[MZF_BACKUP_INVENTORY_GRAVEL] AS
declare @tableName varchar(100),  @sql varchar(800)
set @tableName = 'HIST_INVENTORY_GRAVEL_'+replace(convert(varchar(7),getDate(),23),'-','')

if object_id(@tableName,'u') is not null begin
    set @sql = 'DELETE FROM '+@tableName+' where cdate = '''+convert(varchar(10),getDate(),23)+''''
    exec(@sql)
    set @sql = 'INSERT INTO '+@tableName+'(TARGET_ID,TARGET_NUM,COST,GRAVEL_STANDARD,QUANTITY,KARAT_UNIT_PRICE,SHAPE,SOURCE_ID,SOURCE,CDATE)'
               +' SELECT i.TARGET_ID,r.NUM as TARGET_NUM,r.COST,r.GRAVEL_STANDARD,i.QUANTITY,r.KARAT_UNIT_PRICE,r.SHAPE,r.SOURCE_ID,r.SOURCE,convert(varchar(10),getDate(),23) as CDATE'
               +' FROM MZF_INVENTORY i inner join MZF_RAWMATERIAL r on i.TARGET_ID = r.ID'
               +' WHERE i.TARGET_TYPE=''rawmaterial'''
               +' AND i.STORAGE_TYPE=''rawmaterial_gravel'''
end
else begin
    set @sql = 'SELECT i.TARGET_ID,r.NUM as TARGET_NUM,r.COST,r.GRAVEL_STANDARD,i.QUANTITY,r.KARAT_UNIT_PRICE,r.SHAPE,r.SOURCE_ID,r.SOURCE,convert(varchar(10),getDate(),23) as CDATE'
               +' INTO '+@tableName
               +' FROM MZF_INVENTORY i inner join MZF_RAWMATERIAL r on i.TARGET_ID = r.ID'
               +' WHERE i.TARGET_TYPE=''rawmaterial'''
               +' AND i.STORAGE_TYPE=''rawmaterial_gravel'''
end

exec(@sql)

GO