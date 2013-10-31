IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[MZF_BACKUP_INVENTORY_NAKE_DIAMOND]') AND type in (N'P', N'PC'))
    DROP PROCEDURE [dbo].[MZF_BACKUP_INVENTORY_NAKE_DIAMOND]
GO
CREATE PROC [dbo].[MZF_BACKUP_INVENTORY_NAKE_DIAMOND] AS
declare @tableName varchar(100),  @sql varchar(800)
set @tableName = 'HIST_INVENTORY_NAKE_DIAMOND_'+replace(convert(varchar(7),getDate(),23),'-','')

if object_id(@tableName,'u') is not null begin
    set @sql = 'DELETE FROM '+@tableName+' where cdate = '''+convert(varchar(10),getDate(),23)+''''
    exec(@sql)
    set @sql = 'INSERT INTO '+@tableName+'(TARGET_ID,TARGET_NUM,COST,KARAT_UNIT_PRICE,CID1,WEIGHT,COLOR,CLEAN,CUT,SOURCE,SOURCE_ID,CDATE)'
               +' SELECT i.TARGET_ID,r.NUM,r.COST,r.KARAT_UNIT_PRICE,r.CID1,r.SPEC,r.COLOR,r.CLEAN,r.CUT,r.SOURCE_ID,r.SOURCE,convert(varchar(10),getDate(),23) as CDATE'
               +' FROM MZF_INVENTORY i inner join MZF_RAWMATERIAL r on i.TARGET_ID = r.ID'
               +' WHERE i.TARGET_TYPE=''rawmaterial'''
               +' AND i.STORAGE_TYPE=''rawmaterial_nakedDiamond'''
end
else begin
    set @sql = 'SELECT i.TARGET_ID,r.NUM as TARGET_NUM,r.COST,r.KARAT_UNIT_PRICE,r.CID1,r.SPEC as WEIGHT,r.COLOR,r.CLEAN,r.CUT,r.SOURCE_ID,r.SOURCE,convert(varchar(10),getDate(),23) as CDATE'
               +' INTO '+@tableName
               +' FROM MZF_INVENTORY i inner join MZF_RAWMATERIAL r on i.TARGET_ID = r.ID'
               +' WHERE i.TARGET_TYPE=''rawmaterial'''
               +' AND i.STORAGE_TYPE=''rawmaterial_nakedDiamond'''
end

exec(@sql)

GO